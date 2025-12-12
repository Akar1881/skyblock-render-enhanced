package me.akar1881.sre.events;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.counter.PartyCounterWidget;
import me.akar1881.sre.counter.SlayerKillDetector;
import me.akar1881.sre.gui.SREGui;
import me.akar1881.sre.keybinds.Keybinds;
import me.akar1881.sre.party.PartyHandler;
import me.akar1881.sre.slayer.SlayerHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SREEventHandler {
    private static int tickCounter = 0;
    private static final int PARTY_UPDATE_INTERVAL = 200;
    
    private static int counterTickCounter = 0;
    private static final int COUNTER_SCAN_INTERVAL = 10;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            tickCounter++;
            if (tickCounter >= PARTY_UPDATE_INTERVAL) {
                tickCounter = 0;
                if (ConfigHandler.renderPartyMembers) {
                    PartyHandler.requestPartyInfo();
                }
            }
            
            SlayerHandler.tick();
            
            counterTickCounter++;
            if (counterTickCounter >= COUNTER_SCAN_INTERVAL) {
                counterTickCounter = 0;
                SlayerKillDetector.tick();
            }
            
            if (ConfigHandler.keybindsEnabled) {
                while (Keybinds.toggleSre.wasPressed()) {
                    ConfigHandler.renderPlayers = !ConfigHandler.renderPlayers;
                    
                    if (ConfigHandler.linkSlayerToPlayer) {
                        if (ConfigHandler.renderPlayers) {
                            ConfigHandler.slayerMode = ConfigHandler.SlayerMode.OFF;
                        } else {
                            ConfigHandler.slayerMode = ConfigHandler.SlayerMode.HIDE;
                        }
                    }
                    
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
                    
                    if (ConfigHandler.linkSlayerToPlayer) {
                        Formatting color = ConfigHandler.slayerMode == ConfigHandler.SlayerMode.OFF ? Formatting.GRAY : Formatting.RED;
                        client.player.sendMessage(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Slayer mode also set to: ")
                                .formatted(Formatting.GRAY))
                            .append(Text.literal(ConfigHandler.slayerMode.getDisplayName())
                                .formatted(color, Formatting.BOLD)), false);
                    }
                }
                
                while (Keybinds.toggleSlayer.wasPressed()) {
                    ConfigHandler.SlayerMode[] modes = ConfigHandler.SlayerMode.values();
                    int currentIndex = ConfigHandler.slayerMode.ordinal();
                    int nextIndex = (currentIndex + 1) % modes.length;
                    ConfigHandler.slayerMode = modes[nextIndex];
                    ConfigHandler.syncAndSave();
                    
                    Formatting color = switch (ConfigHandler.slayerMode) {
                        case OFF -> Formatting.GRAY;
                        case HIDE -> Formatting.RED;
                        case GLOW -> Formatting.GREEN;
                    };
                    
                    client.player.sendMessage(Text.literal("[SRE] ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("Slayer mode: ")
                            .formatted(Formatting.WHITE))
                        .append(Text.literal(ConfigHandler.slayerMode.getDisplayName())
                            .formatted(Formatting.BOLD, color)), false);
                }
                
                while (Keybinds.openGui.wasPressed()) {
                    MinecraftClient.getInstance().setScreen(SREGui.createScreen(null));
                }
            } else {
                Keybinds.toggleSre.wasPressed();
                Keybinds.openGui.wasPressed();
                Keybinds.toggleSlayer.wasPressed();
            }
        });
        
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.currentScreen == null) {
                PartyCounterWidget.render(context);
            }
        });
    }
}
