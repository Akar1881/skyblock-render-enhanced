package me.akar1881.sre.util;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    private static Set<String> cachedTabPlayers = new HashSet<>();
    private static long lastTabCacheTime = 0;
    private static final long TAB_CACHE_DURATION = 1000;
    
    public static boolean isNPC(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        
        PlayerEntity player = (PlayerEntity) entity;
        
        return entity.getUuid().version() == 2 
            && player.getHealth() == 20.0F 
            && !player.isSleeping();
    }
    
    public static boolean isHumanoidMob(PlayerEntity player) {
        String gameName = player.getGameProfile().name();
        if (gameName != null && gameName.startsWith("!")) {
            return true;
        }
        return false;
    }
    
    public static boolean isInTabList(PlayerEntity player) {
        String gameName = player.getGameProfile().name();
        if (gameName == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTabCacheTime > TAB_CACHE_DURATION) {
            refreshTabPlayerCache();
            lastTabCacheTime = currentTime;
        }
        
        return cachedTabPlayers.contains(gameName.toLowerCase());
    }
    
    private static void refreshTabPlayerCache() {
        cachedTabPlayers.clear();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        
        if (networkHandler == null) {
            return;
        }
        
        Collection<PlayerListEntry> players = networkHandler.getPlayerList();
        for (PlayerListEntry entry : players) {
            if (entry.getProfile() != null && entry.getProfile().name() != null) {
                cachedTabPlayers.add(entry.getProfile().name().toLowerCase());
            }
        }
    }
    
    public static boolean shouldRenderPlayer(PlayerEntity player, PlayerEntity localPlayer) {
        if (player.equals(localPlayer)) {
            return true;
        }
        
        if (isNPC(player)) {
            return true;
        }
        
        if (isHumanoidMob(player)) {
            return true;
        }
        
        if (!isInTabList(player)) {
            return true;
        }
        
        if (ConfigHandler.renderPartyMembers && PartyHandler.isPartyMember(player.getUuid())) {
            return true;
        }
        
        String playerName = player.getGameProfile().name();
        
        for (String name : ConfigHandler.playersToRender) {
            if (name.equals(playerName)) {
                return true;
            }
        }
        
        return false;
    }
}
