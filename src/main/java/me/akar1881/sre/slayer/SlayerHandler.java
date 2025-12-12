package me.akar1881.sre.slayer;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerHandler {
    private static final Map<Integer, SlayerBossData> trackedBosses = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> armorStandToBoss = new ConcurrentHashMap<>();
    
    private static final Map<Integer, Boolean> renderCache = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> glowCache = new ConcurrentHashMap<>();
    
    private static long lastCacheInvalidation = 0;
    private static final long CACHE_INVALIDATION_INTERVAL = 10;
    
    private static final List<String> SLAYER_BOSS_NAMES = Arrays.asList(
        "Voidgloom Seraph",
        "Revenant Horror",
        "Tarantula Broodfather",
        "Sven Packmaster",
        "Inferno Demonlord",
        "Riftstalker Bloodfiend"
    );
    
    private static final Pattern SPAWNED_BY_PATTERN = Pattern.compile("Spawned by: (.+)");
    
    private static int tickCounter = 0;
    private static final int SCAN_INTERVAL = 5;
    
    private static class SlayerBossData {
        int bossEntityId;
        String spawnerName;
        Set<Integer> armorStandIds = ConcurrentHashMap.newKeySet();
        double lastX, lastY, lastZ;
        long lastSeenTick;
        boolean isFriendly;
        
        SlayerBossData(int bossEntityId, String spawnerName, double x, double y, double z, long tick) {
            this.bossEntityId = bossEntityId;
            this.spawnerName = spawnerName;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.lastSeenTick = tick;
            this.isFriendly = checkFriendly(spawnerName);
        }
        
        void updateFriendlyStatus() {
            this.isFriendly = checkFriendly(spawnerName);
        }
        
        private static boolean checkFriendly(String spawner) {
            if (spawner == null || spawner.isEmpty()) {
                return false;
            }
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && spawner.equalsIgnoreCase(client.player.getGameProfile().name())) {
                return true;
            }
            
            if (ConfigHandler.renderPartyMembers && isPartyMemberByName(spawner)) {
                return true;
            }
            
            for (String whitelisted : ConfigHandler.playersToRender) {
                if (whitelisted.equalsIgnoreCase(spawner)) {
                    return true;
                }
            }
            
            return false;
        }
        
        private static boolean isPartyMemberByName(String playerName) {
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
    
    public static void tick() {
        tickCounter++;
        if (tickCounter >= SCAN_INTERVAL) {
            tickCounter = 0;
            scanForSlayerBosses();
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            long currentTick = client.world.getTime();
            if (currentTick - lastCacheInvalidation >= CACHE_INVALIDATION_INTERVAL) {
                lastCacheInvalidation = currentTick;
                invalidateCaches();
            }
        }
    }
    
    private static void invalidateCaches() {
        renderCache.clear();
        glowCache.clear();
        
        for (SlayerBossData data : trackedBosses.values()) {
            data.updateFriendlyStatus();
        }
    }
    
    private static void scanForSlayerBosses() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }
        
        long currentTick = client.world.getTime();
        Set<Integer> seenBossIds = new HashSet<>();
        Map<Integer, Set<Integer>> bossToArmorStands = new HashMap<>();
        Map<Integer, String> bossToSpawner = new HashMap<>();
        
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity armorStand)) {
                continue;
            }
            
            if (!armorStand.isCustomNameVisible() || armorStand.getCustomName() == null) {
                continue;
            }
            
            String name = armorStand.getCustomName().getString();
            String cleanName = stripColorCodes(name);
            
            boolean isBossNameTag = false;
            for (String bossName : SLAYER_BOSS_NAMES) {
                if (cleanName.contains(bossName)) {
                    isBossNameTag = true;
                    break;
                }
            }
            
            Matcher spawnerMatcher = SPAWNED_BY_PATTERN.matcher(cleanName);
            boolean isSpawnerTag = spawnerMatcher.find();
            String spawnerName = isSpawnerTag ? spawnerMatcher.group(1).trim() : null;
            
            if (isBossNameTag || isSpawnerTag) {
                MobEntity bossEntity = findMobBelowArmorStand(armorStand);
                if (bossEntity != null) {
                    int bossId = bossEntity.getId();
                    seenBossIds.add(bossId);
                    
                    bossToArmorStands.computeIfAbsent(bossId, k -> new HashSet<>()).add(armorStand.getId());
                    
                    if (spawnerName != null && !spawnerName.isEmpty()) {
                        bossToSpawner.put(bossId, spawnerName);
                    }
                    
                    SlayerBossData existing = trackedBosses.get(bossId);
                    if (existing == null) {
                        existing = new SlayerBossData(bossId, spawnerName, 
                            bossEntity.getX(), bossEntity.getY(), bossEntity.getZ(), currentTick);
                        trackedBosses.put(bossId, existing);
                    } else {
                        existing.lastX = bossEntity.getX();
                        existing.lastY = bossEntity.getY();
                        existing.lastZ = bossEntity.getZ();
                        existing.lastSeenTick = currentTick;
                        if (spawnerName != null && !spawnerName.isEmpty() && 
                            (existing.spawnerName == null || existing.spawnerName.isEmpty())) {
                            existing.spawnerName = spawnerName;
                            existing.updateFriendlyStatus();
                        }
                    }
                }
            }
        }
        
        for (Map.Entry<Integer, Set<Integer>> entry : bossToArmorStands.entrySet()) {
            SlayerBossData data = trackedBosses.get(entry.getKey());
            if (data != null) {
                for (Integer oldStandId : data.armorStandIds) {
                    if (!entry.getValue().contains(oldStandId)) {
                        armorStandToBoss.remove(oldStandId);
                    }
                }
                
                data.armorStandIds.clear();
                data.armorStandIds.addAll(entry.getValue());
                
                for (Integer standId : entry.getValue()) {
                    armorStandToBoss.put(standId, entry.getKey());
                }
            }
        }
        
        for (Map.Entry<Integer, String> entry : bossToSpawner.entrySet()) {
            SlayerBossData data = trackedBosses.get(entry.getKey());
            if (data != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (data.spawnerName == null || data.spawnerName.isEmpty()) {
                    data.spawnerName = entry.getValue();
                    data.updateFriendlyStatus();
                }
            }
        }
        
        long expiryThreshold = currentTick - 60;
        trackedBosses.entrySet().removeIf(entry -> {
            if (entry.getValue().lastSeenTick < expiryThreshold) {
                for (Integer standId : entry.getValue().armorStandIds) {
                    armorStandToBoss.remove(standId);
                }
                renderCache.remove(entry.getKey());
                glowCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    private static MobEntity findMobBelowArmorStand(ArmorStandEntity armorStand) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return null;
        
        Box searchBox = new Box(
            armorStand.getX() - 2, armorStand.getY() - 6, armorStand.getZ() - 2,
            armorStand.getX() + 2, armorStand.getY() + 1, armorStand.getZ() + 2
        );
        
        MobEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (Entity entity : client.world.getOtherEntities(armorStand, searchBox)) {
            if (entity instanceof MobEntity mob && !(entity instanceof ArmorStandEntity)) {
                double dx = entity.getX() - armorStand.getX();
                double dz = entity.getZ() - armorStand.getZ();
                double horizontalDist = dx * dx + dz * dz;
                
                if (horizontalDist < 4.0) {
                    double dist = entity.squaredDistanceTo(armorStand);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = mob;
                    }
                }
            }
        }
        
        return closest;
    }
    
    public static boolean isSlayerBoss(Entity entity) {
        return trackedBosses.containsKey(entity.getId());
    }
    
    public static boolean isSlayerArmorStand(Entity entity) {
        return armorStandToBoss.containsKey(entity.getId());
    }
    
    public static boolean isSlayerRelated(Entity entity) {
        int entityId = entity.getId();
        return trackedBosses.containsKey(entityId) || armorStandToBoss.containsKey(entityId);
    }
    
    private static SlayerBossData getBossDataForEntity(Entity entity) {
        int entityId = entity.getId();
        
        SlayerBossData bossData = trackedBosses.get(entityId);
        if (bossData != null) {
            return bossData;
        }
        
        Integer bossId = armorStandToBoss.get(entityId);
        if (bossId != null) {
            return trackedBosses.get(bossId);
        }
        
        return null;
    }
    
    public static boolean shouldRenderSlayerEntity(Entity entity) {
        if (ConfigHandler.slayerMode == ConfigHandler.SlayerMode.OFF) {
            return true;
        }
        
        if (!isSlayerRelated(entity)) {
            return true;
        }
        
        int entityId = entity.getId();
        
        Boolean cached = renderCache.get(entityId);
        if (cached != null) {
            return cached;
        }
        
        SlayerBossData bossData = getBossDataForEntity(entity);
        if (bossData == null) {
            return true;
        }
        
        boolean shouldRender;
        
        if (ConfigHandler.slayerMode == ConfigHandler.SlayerMode.GLOW) {
            shouldRender = true;
        } else {
            if (bossData.spawnerName == null || bossData.spawnerName.isEmpty()) {
                shouldRender = true;
            } else {
                shouldRender = bossData.isFriendly;
            }
        }
        
        renderCache.put(entityId, shouldRender);
        
        if (bossData.armorStandIds != null) {
            for (Integer standId : bossData.armorStandIds) {
                renderCache.put(standId, shouldRender);
            }
        }
        
        return shouldRender;
    }
    
    public static boolean shouldGlowSlayerEntity(Entity entity) {
        if (ConfigHandler.slayerMode != ConfigHandler.SlayerMode.GLOW) {
            return false;
        }
        
        if (!(entity instanceof MobEntity)) {
            return false;
        }
        
        if (!isSlayerBoss(entity)) {
            return false;
        }
        
        int entityId = entity.getId();
        
        Boolean cached = glowCache.get(entityId);
        if (cached != null) {
            return cached;
        }
        
        SlayerBossData bossData = trackedBosses.get(entityId);
        if (bossData == null) {
            glowCache.put(entityId, false);
            return false;
        }
        
        boolean shouldGlow = bossData.isFriendly;
        glowCache.put(entityId, shouldGlow);
        
        return shouldGlow;
    }
    
    public static int getGlowColor() {
        return ConfigHandler.glowColor;
    }
    
    private static String stripColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    public static void clear() {
        trackedBosses.clear();
        armorStandToBoss.clear();
        renderCache.clear();
        glowCache.clear();
    }
    
    public static int getTrackedBossCount() {
        return trackedBosses.size();
    }
    
    public static void forceRefresh() {
        invalidateCaches();
        scanForSlayerBosses();
    }
}
