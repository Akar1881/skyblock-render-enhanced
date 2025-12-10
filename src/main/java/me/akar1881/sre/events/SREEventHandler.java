package me.akar1881.sre.events;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.gui.SREGui;
import me.akar1881.sre.keybinds.Keybinds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SREEventHandler {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            if (ConfigHandler.keybindsEnabled) {
                while (Keybinds.toggleSre.wasPressed()) {
                    ConfigHandler.renderPlayers = !ConfigHandler.renderPlayers;
                    ConfigHandler.syncAndSave();
                    
                    if (ConfigHandler.renderPlayers) {
                        client.player.sendMessage(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Rendering players is now ")
                                .formatted(Formatting.GREEN))
                            .append(Text.literal("on")
                                .formatted(Formatting.BOLD, Formatting.GREEN)), false);
                    } else {
                        client.player.sendMessage(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Rendering players is now ")
                                .formatted(Formatting.RED))
                            .append(Text.literal("off")
                                .formatted(Formatting.BOLD, Formatting.RED)), false);
                    }
                }
                
                while (Keybinds.openGui.wasPressed()) {
                    MinecraftClient.getInstance().setScreen(SREGui.createScreen(null));
                }
            } else {
                Keybinds.toggleSre.wasPressed();
                Keybinds.openGui.wasPressed();
            }
        });
    }
}
