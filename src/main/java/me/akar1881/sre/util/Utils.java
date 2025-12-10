package me.akar1881.sre.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class Utils {
    public static boolean isNPC(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        
        PlayerEntity player = (PlayerEntity) entity;
        
        return entity.getUuid().version() == 2 
            && player.getHealth() == 20.0F 
            && !player.isSleeping();
    }
    
    public static boolean shouldRenderPlayer(PlayerEntity player, PlayerEntity localPlayer) {
        if (player.equals(localPlayer)) {
            return true;
        }
        
        if (isNPC(player)) {
            return true;
        }
        
        String playerName = player.getGameProfile().name();
        
        String[] playersToRender = me.akar1881.sre.config.ConfigHandler.playersToRender.split(",");
        for (String name : playersToRender) {
            if (name.equals(playerName)) {
                return true;
            }
        }
        
        String[] whitelistedPlayers = me.akar1881.sre.config.ConfigHandler.whitelistedPlayers.split(",");
        for (String name : whitelistedPlayers) {
            if (name.equals(playerName)) {
                return true;
            }
        }
        
        return false;
    }
}
