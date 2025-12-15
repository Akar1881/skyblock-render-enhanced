package me.akar1881.sre.counter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.akar1881.sre.SkyblockRenderEnhanced;
import me.akar1881.sre.party.PartyHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartySlayerCounter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CACHE_PATH = FabricLoader.getInstance().getConfigDir().resolve("sre_counter_cache.json");
    
    private static final Map<String, Integer> partyKillCounts = new ConcurrentHashMap<>();
    
    private static final Map<Integer, TrackedBoss> activeBosses = new ConcurrentHashMap<>();
    private static final long BOSS_TIMEOUT = 60000;
    
    private static boolean wasInParty = false;
    private static Set<UUID> lastPartyMembers = new HashSet<>();
    
    private static class TrackedBoss {
        String spawnerName;
        int armorStandId;
        long detectedTime;
        
        TrackedBoss(String spawnerName, int armorStandId) {
            this.spawnerName = spawnerName;
            this.armorStandId = armorStandId;
            this.detectedTime = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - detectedTime > BOSS_TIMEOUT;
        }
    }
    
    public static void initialize() {
        loadCache();
    }
    
    public static void tick() {
        boolean inParty = PartyHandler.isInParty();
        Set<UUID> currentMembers = PartyHandler.getPartyMemberUuids();
        
        if (wasInParty && !inParty) {
            onPartyDisband();
        } else if (inParty && !currentMembers.equals(lastPartyMembers)) {
            if (!lastPartyMembers.isEmpty() && currentMembers.isEmpty()) {
                onPartyDisband();
            } else if (!lastPartyMembers.isEmpty()) {
                validateCacheAgainstParty();
            }
        }
        
        wasInParty = inParty;
        lastPartyMembers = new HashSet<>(currentMembers);
        
        activeBosses.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public static void onBossDetected(String spawnerName, int armorStandId) {
        if (spawnerName == null || spawnerName.isEmpty()) return;
        if (!PartyHandler.isInParty()) return;
        
        if (isLocalPlayer(spawnerName)) return;
        
        if (!isConfirmedPartyMember(spawnerName)) return;
        
        for (TrackedBoss boss : activeBosses.values()) {
            if (boss.spawnerName.equalsIgnoreCase(spawnerName) && boss.armorStandId == armorStandId) {
                boss.detectedTime = System.currentTimeMillis();
                return;
            }
        }
        
        activeBosses.put(armorStandId, new TrackedBoss(spawnerName, armorStandId));
        SkyblockRenderEnhanced.LOGGER.debug("Tracking boss for party member: {} (stand ID: {})", spawnerName, armorStandId);
    }
    
    public static void onBossKilled(String spawnerName) {
        if (spawnerName == null || spawnerName.isEmpty()) {
            SkyblockRenderEnhanced.LOGGER.debug("Boss killed but no spawner name provided, ignoring");
            return;
        }
        
        if (isLocalPlayer(spawnerName)) {
            SkyblockRenderEnhanced.LOGGER.debug("Boss killed was own boss, not counting");
            return;
        }
        
        if (!isConfirmedPartyMember(spawnerName)) {
            SkyblockRenderEnhanced.LOGGER.debug("Boss killed for non-party member: {}, ignoring", spawnerName);
            return;
        }
        
        partyKillCounts.merge(spawnerName, 1, Integer::sum);
        saveCache();
        
        SkyblockRenderEnhanced.LOGGER.info("Killed boss for {}, total: {}", 
            spawnerName, partyKillCounts.get(spawnerName));
        
        final String finalSpawner = spawnerName;
        activeBosses.entrySet().removeIf(entry -> 
            entry.getValue().spawnerName.equalsIgnoreCase(finalSpawner));
    }
    
    public static void onBossFailed() {
        activeBosses.clear();
    }
    
    public static void onPartyDisband() {
        partyKillCounts.clear();
        activeBosses.clear();
        saveCache();
        SkyblockRenderEnhanced.LOGGER.info("Party disbanded/changed, counter cache cleared");
    }
    
    private static void validateCacheAgainstParty() {
        if (!PartyHandler.isInParty()) {
            partyKillCounts.clear();
            saveCache();
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        
        Set<String> validNames = new HashSet<>();
        for (var player : client.world.getPlayers()) {
            if (PartyHandler.isPartyMember(player.getUuid())) {
                validNames.add(player.getGameProfile().name().toLowerCase());
            }
        }
        
        boolean changed = partyKillCounts.keySet().removeIf(name -> 
            !validNames.contains(name.toLowerCase()));
        
        if (changed) {
            saveCache();
            SkyblockRenderEnhanced.LOGGER.info("Validated cache, removed non-party members");
        }
    }
    
    public static Map<String, Integer> getKillCounts() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(partyKillCounts));
    }
    
    public static int getKillCountFor(String playerName) {
        return partyKillCounts.getOrDefault(playerName, 0);
    }
    
    public static int getTotalKills() {
        return partyKillCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public static void clearCounter() {
        partyKillCounts.clear();
        activeBosses.clear();
        saveCache();
    }
    
    public static boolean clearPlayerCounter(String playerName) {
        if (partyKillCounts.containsKey(playerName)) {
            partyKillCounts.remove(playerName);
            saveCache();
            return true;
        }
        for (String key : partyKillCounts.keySet()) {
            if (key.equalsIgnoreCase(playerName)) {
                partyKillCounts.remove(key);
                saveCache();
                return true;
            }
        }
        return false;
    }
    
    public static void setKillCount(String playerName, int count) {
        if (count <= 0) {
            partyKillCounts.remove(playerName);
        } else {
            partyKillCounts.put(playerName, count);
        }
        saveCache();
    }
    
    public static int incrementKillCount(String playerName) {
        return addKillCount(playerName, 1);
    }
    
    public static int addKillCount(String playerName, int amount) {
        if (amount <= 0) {
            return partyKillCounts.getOrDefault(playerName, 0);
        }
        int newCount = partyKillCounts.merge(playerName, amount, Integer::sum);
        saveCache();
        SkyblockRenderEnhanced.LOGGER.info("Added {} kills for {}, new total: {}", amount, playerName, newCount);
        return newCount;
    }
    
    public static int decrementKillCount(String playerName) {
        return removeKillCount(playerName, 1);
    }
    
    public static int removeKillCount(String playerName, int amount) {
        Integer current = partyKillCounts.get(playerName);
        if (current == null || current <= 0 || amount <= 0) {
            return current != null ? current : 0;
        }
        int newCount = current - amount;
        if (newCount <= 0) {
            partyKillCounts.remove(playerName);
            newCount = 0;
        } else {
            partyKillCounts.put(playerName, newCount);
        }
        saveCache();
        SkyblockRenderEnhanced.LOGGER.info("Removed {} kills for {}, new total: {}", amount, playerName, newCount);
        return newCount;
    }
    
    public static Set<String> getTrackedPlayerNames() {
        return new HashSet<>(partyKillCounts.keySet());
    }
    
    public static boolean hasActiveBosses() {
        return !activeBosses.isEmpty();
    }
    
    private static void loadCache() {
        try {
            if (Files.exists(CACHE_PATH)) {
                String json = Files.readString(CACHE_PATH);
                Type type = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> loaded = GSON.fromJson(json, type);
                if (loaded != null) {
                    partyKillCounts.clear();
                    partyKillCounts.putAll(loaded);
                    SkyblockRenderEnhanced.LOGGER.info("Loaded counter cache with {} entries", partyKillCounts.size());
                }
            }
        } catch (Exception e) {
            SkyblockRenderEnhanced.LOGGER.error("Failed to load counter cache", e);
        }
    }
    
    private static void saveCache() {
        try {
            Path parentDir = CACHE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            String json = GSON.toJson(partyKillCounts);
            Files.writeString(CACHE_PATH, json);
        } catch (IOException e) {
            SkyblockRenderEnhanced.LOGGER.error("Failed to save counter cache", e);
        }
    }
    
    private static boolean isLocalPlayer(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        return client.player.getGameProfile().name().equalsIgnoreCase(name);
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
}
