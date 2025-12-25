package me.akar1881.sre.counter;

import me.akar1881.sre.SkyblockRenderEnhanced;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerKillDetector {
    private static final Pattern SPAWNED_BY_PATTERN = Pattern.compile("Spawned by: (.+)");
    private static final Pattern QUEST_FAILED_PATTERN = Pattern.compile("^\\s*(?:Your Slayer Quest has been cancelled!|SLAYER QUEST FAILED!)\\s*$");
    private static final Pattern PARTY_DISBAND_PATTERN = Pattern.compile("^\\s*(?:The party was disbanded|You have been kicked from the party|You left the party|The party has been disbanded|You are not currently in a party).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOOT_SHARE_PATTERN = Pattern.compile("LOOT SHARE You received loot for assisting ([A-Za-z0-9_]+)!");
    private static final Pattern BOSS_NAME_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend|Bloodfiend", Pattern.CASE_INSENSITIVE);
    
    private static final Set<Integer> processedArmorStands = new HashSet<>();
    private static long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL = 30000;
    
    private static final Map<String, Long> recentSpawners = new ConcurrentHashMap<>();
    private static final long SPAWNER_MEMORY_TIMEOUT = 60000;
    
    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerKillDetector::onChatMessage);
        SkyblockRenderEnhanced.LOGGER.info("Registered slayer kill detector");
    }
    
    private static boolean onChatMessage(Text text, boolean overlay) {
        if (overlay || !ConfigHandler.counterEnabled) return true;
        
        String message = text.getString();
        String cleanMessage = stripColorCodes(message);
        
        Matcher lootShareMatcher = LOOT_SHARE_PATTERN.matcher(cleanMessage);
        if (lootShareMatcher.find()) {
            String assistedPlayerName = lootShareMatcher.group(1).trim();
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return true;
            
            String localPlayer = client.player.getGameProfile().name();
            if (assistedPlayerName.equalsIgnoreCase(localPlayer)) {
                SkyblockRenderEnhanced.LOGGER.debug("Loot share for own boss, ignoring");
                return true;
            }
            
            if (!isConfirmedPartyMember(assistedPlayerName)) {
                SkyblockRenderEnhanced.LOGGER.debug("Loot share for non-party member: {}, ignoring", assistedPlayerName);
                return true;
            }
            
            if (ConfigHandler.counterMode == ConfigHandler.CounterMode.MANUAL) {
                SkyblockRenderEnhanced.LOGGER.debug("Counter is in manual mode, not auto-counting for {}", assistedPlayerName);
                return true;
            }
            
            Long spawnerTime = recentSpawners.get(assistedPlayerName.toLowerCase());
            if (spawnerTime != null && System.currentTimeMillis() - spawnerTime < SPAWNER_MEMORY_TIMEOUT) {
                PartySlayerCounter.onBossKilled(assistedPlayerName);
                SkyblockRenderEnhanced.LOGGER.info("Counted boss kill for party member: {} (via loot share)", assistedPlayerName);
                recentSpawners.remove(assistedPlayerName.toLowerCase());
            } else {
                SkyblockRenderEnhanced.LOGGER.debug("Loot share for {} but no recent boss detected, ignoring (might be regular mob)", assistedPlayerName);
            }
            
            return true;
        }
        
        if (QUEST_FAILED_PATTERN.matcher(cleanMessage).find()) {
            PartySlayerCounter.onBossFailed();
            return true;
        }
        
        if (PARTY_DISBAND_PATTERN.matcher(cleanMessage).find()) {
            PartySlayerCounter.onPartyDisband();
            return true;
        }
        
        return true;
    }
    
    public static void tick() {
        if (!ConfigHandler.counterEnabled) return;
        if (!PartyHandler.isInParty()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        
        PartySlayerCounter.tick();
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL) {
            processedArmorStands.clear();
            recentSpawners.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > SPAWNER_MEMORY_TIMEOUT);
            lastCleanupTime = currentTime;
        }
        
        Box searchBox = client.player.getBoundingBox().expand(25);
        
        for (Entity entity : client.world.getOtherEntities(client.player, searchBox)) {
            if (entity instanceof ArmorStandEntity armorStand) {
                scanArmorStand(armorStand);
            }
        }
    }
    
    private static void scanArmorStand(ArmorStandEntity armorStand) {
        if (!armorStand.hasCustomName() || armorStand.getCustomName() == null) return;
        
        int standId = armorStand.getId();
        
        String name = armorStand.getCustomName().getString();
        String cleanName = stripColorCodes(name);
        
        Matcher spawnerMatcher = SPAWNED_BY_PATTERN.matcher(cleanName);
        if (spawnerMatcher.find()) {
            String spawnerName = spawnerMatcher.group(1).trim();
            if (!spawnerName.isEmpty()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null) return;
                
                String localPlayer = client.player.getGameProfile().name();
                
                if (spawnerName.equalsIgnoreCase(localPlayer)) {
                    return;
                }
                
                if (isConfirmedPartyMember(spawnerName)) {
                    if (isActualBoss(armorStand, spawnerName)) {
                        PartySlayerCounter.onBossDetected(spawnerName, standId);
                        
                        recentSpawners.put(spawnerName.toLowerCase(), System.currentTimeMillis());
                        
                        if (!processedArmorStands.contains(standId)) {
                            processedArmorStands.add(standId);
                            SkyblockRenderEnhanced.LOGGER.debug("Found boss for party member: {} (stand: {})", spawnerName, standId);
                        }
                    } else {
                        SkyblockRenderEnhanced.LOGGER.debug("Spawned by tag found for {} but not a recognized boss, ignoring", spawnerName);
                    }
                }
            }
        }
    }
    
    private static boolean isActualBoss(ArmorStandEntity armorStand, String spawnerName) {
        String cleanName = stripColorCodes(armorStand.getCustomName().getString());
        
        if (BOSS_NAME_PATTERN.matcher(cleanName).find()) {
            return true;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        
        Box searchBox = armorStand.getBoundingBox().expand(2.0);
        for (Entity entity : client.world.getOtherEntities(armorStand, searchBox)) {
            if (entity instanceof ArmorStandEntity otherStand && otherStand.hasCustomName()) {
                String otherCleanName = stripColorCodes(otherStand.getCustomName().getString());
                if (BOSS_NAME_PATTERN.matcher(otherCleanName).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static boolean isConfirmedPartyMember(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        
        for (var player : client.world.getPlayers()) {
            if (player.getGameProfile().name().equalsIgnoreCase(playerName)) {
                return PartyHandler.isPartyMember(player.getUuid());
            }
        }
        
        return false;
    }
    
    private static String stripColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    public static void reset() {
        recentSpawners.clear();
        processedArmorStands.clear();
    }
}
