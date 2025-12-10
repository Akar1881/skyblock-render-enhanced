package me.akar1881.sre.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OnlinePlayers {
    public static String[] getListOfPlayerUsernames() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        
        if (networkHandler == null) {
            return new String[0];
        }
        
        Collection<PlayerListEntry> players = networkHandler.getPlayerList();
        List<String> list = new ArrayList<>();
        
        for (PlayerListEntry info : players) {
            if (info.getProfile() != null) {
                list.add(info.getProfile().name());
            }
        }
        
        return list.toArray(new String[0]);
    }
}
