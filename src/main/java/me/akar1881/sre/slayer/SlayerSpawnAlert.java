package me.akar1881.sre.slayer;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SlayerSpawnAlert {
    private static final Set<Integer> previousBossIds = ConcurrentHashMap.newKeySet();
    
    public static void onBossSpawned(int bossId, String spawnerName, String bossType) {
        if (!ConfigHandler.slayerSpawnAlertEnabled) {
            return;
        }
        
        if (spawnerName == null || spawnerName.isEmpty()) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        
        boolean isOwnBoss = spawnerName.equalsIgnoreCase(client.player.getGameProfile().name());
        boolean isPartyMemberBoss = ConfigHandler.renderPartyMembers && isPartyMember(spawnerName);
        
        if (!isOwnBoss && !isPartyMemberBoss) {
            return;
        }
        
        String spawnerDisplay = spawnerName;
        Formatting color = isOwnBoss ? Formatting.GREEN : Formatting.AQUA;
        
        client.player.sendMessage(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("Boss Spawned: ")
                .formatted(Formatting.WHITE))
            .append(Text.literal(bossType)
                .formatted(color))
            .append(Text.literal(" by ")
                .formatted(Formatting.WHITE))
            .append(Text.literal(spawnerDisplay)
                .formatted(color)), false);
        
        playSpawnSound();
    }
    
    private static boolean isPartyMember(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        
        for (var player : client.world.getPlayers()) {
            if (player.getGameProfile().name().equalsIgnoreCase(playerName)) {
                return PartyHandler.isPartyMember(player.getUuid());
            }
        }
        
        return false;
    }
    
    private static void playSpawnSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            client.world.playSound(
                client.player,
                client.player.getX(),
                client.player.getY(),
                client.player.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.PLAYERS,
                1.5f,
                1.0f,
                0L
            );
        }
    }
    
    public static void checkForNewSpawns(Map<Integer, SlayerHandler.SlayerBossData> trackedBosses) {
        for (int bossId : trackedBosses.keySet()) {
            if (!previousBossIds.contains(bossId)) {
                previousBossIds.add(bossId);
                SlayerHandler.SlayerBossData data = trackedBosses.get(bossId);
                if (data != null) {
                    String bossType = getBossType(data);
                    onBossSpawned(bossId, data.spawnerName, bossType);
                }
            }
        }
        
        previousBossIds.retainAll(trackedBosses.keySet());
    }
    
    private static String getBossType(SlayerHandler.SlayerBossData data) {
        if (data == null) return "Unknown";
        return "Slayer Boss";
    }
    
    public static void clear() {
        previousBossIds.clear();
    }
}
