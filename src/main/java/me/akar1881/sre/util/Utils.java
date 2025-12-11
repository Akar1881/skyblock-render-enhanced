package me.akar1881.sre.util;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
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
