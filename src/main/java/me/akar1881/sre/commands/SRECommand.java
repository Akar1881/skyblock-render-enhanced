package me.akar1881.sre.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.counter.PartySlayerCounter;
import me.akar1881.sre.gui.SREGui;
import me.akar1881.sre.gui.WidgetPositionGui;
import me.akar1881.sre.util.OnlinePlayers;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SRECommand {
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYERS_SUGGESTION = (context, builder) -> {
        String[] players = OnlinePlayers.getListOfPlayerUsernames();
        for (String player : players) {
            if (player.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(player);
            }
        }
        return builder.buildFuture();
    };
    
    private static final SuggestionProvider<FabricClientCommandSource> WHITELISTED_PLAYERS_SUGGESTION = (context, builder) -> {
        List<String> players = ConfigHandler.getPlayersToRenderList();
        for (String player : players) {
            if (player.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(player);
            }
        }
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("sre")
            .executes(context -> {
                MinecraftClient.getInstance().send(() -> 
                    MinecraftClient.getInstance().setScreen(SREGui.createScreen(null))
                );
                return 1;
            })
            .then(ClientCommandManager.literal("help")
                .executes(context -> {
                    context.getSource().sendFeedback(getHelpText());
                    return 1;
                }))
            .then(ClientCommandManager.literal("toggle")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("[SRE] ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("Usage: /sre toggle player OR /sre toggle slayer OR /sre toggle counter")
                            .formatted(Formatting.YELLOW)));
                    return 1;
                })
                .then(ClientCommandManager.literal("player")
                    .executes(context -> {
                        ConfigHandler.renderPlayers = !ConfigHandler.renderPlayers;
                        ConfigHandler.syncAndSave();
                        if (ConfigHandler.renderPlayers) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Rendering players is now ")
                                    .formatted(Formatting.GREEN))
                                .append(Text.literal("on")
                                    .formatted(Formatting.BOLD, Formatting.GREEN)));
                        } else {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Rendering players is now ")
                                    .formatted(Formatting.RED))
                                .append(Text.literal("off")
                                    .formatted(Formatting.BOLD, Formatting.RED)));
                        }
                        return 1;
                    }))
                .then(ClientCommandManager.literal("slayer")
                    .executes(context -> {
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
                        
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Slayer mode: ")
                                .formatted(Formatting.WHITE))
                            .append(Text.literal(ConfigHandler.slayerMode.getDisplayName())
                                .formatted(Formatting.BOLD, color)));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("counter")
                    .executes(context -> {
                        ConfigHandler.counterEnabled = !ConfigHandler.counterEnabled;
                        ConfigHandler.syncAndSave();
                        if (ConfigHandler.counterEnabled) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Party slayer counter is now ")
                                    .formatted(Formatting.GREEN))
                                .append(Text.literal("on")
                                    .formatted(Formatting.BOLD, Formatting.GREEN)));
                        } else {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Party slayer counter is now ")
                                    .formatted(Formatting.RED))
                                .append(Text.literal("off")
                                    .formatted(Formatting.BOLD, Formatting.RED)));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("counter")
                .executes(context -> {
                    Map<String, Integer> killCounts = PartySlayerCounter.getKillCounts();
                    
                    String modeText = ConfigHandler.counterMode == ConfigHandler.CounterMode.AUTO ? "Auto" : "Manual";
                    
                    if (killCounts.isEmpty()) {
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("No boss kills recorded yet. Mode: ")
                                .formatted(Formatting.YELLOW))
                            .append(Text.literal(modeText)
                                .formatted(Formatting.AQUA)));
                        return 1;
                    }
                    
                    context.getSource().sendFeedback(Text.literal("------------")
                        .formatted(Formatting.DARK_PURPLE)
                        .append(Text.literal("[Party Slayer Counter]")
                            .formatted(Formatting.GOLD))
                        .append(Text.literal(" [" + modeText + "]")
                            .formatted(Formatting.AQUA))
                        .append(Text.literal("------------")
                            .formatted(Formatting.DARK_PURPLE)));
                    
                    int total = 0;
                    for (Map.Entry<String, Integer> entry : killCounts.entrySet()) {
                        context.getSource().sendFeedback(Text.literal("  ")
                            .append(Text.literal(entry.getKey())
                                .formatted(Formatting.WHITE))
                            .append(Text.literal(": ")
                                .formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(entry.getValue()))
                                .formatted(Formatting.GREEN, Formatting.BOLD))
                            .append(Text.literal(" kills")
                                .formatted(Formatting.GRAY)));
                        total += entry.getValue();
                    }
                    
                    context.getSource().sendFeedback(Text.literal("  ")
                        .append(Text.literal("Total: ")
                            .formatted(Formatting.YELLOW))
                        .append(Text.literal(String.valueOf(total))
                            .formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(Text.literal(" kills")
                            .formatted(Formatting.GRAY)));
                    
                    context.getSource().sendFeedback(Text.literal("------------------------------------------")
                        .formatted(Formatting.DARK_PURPLE));
                    
                    return 1;
                })
                .then(ClientCommandManager.literal("clear")
                    .executes(context -> {
                        PartySlayerCounter.clearCounter();
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Party slayer counter has been cleared.")
                                .formatted(Formatting.YELLOW)));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("mode")
                    .executes(context -> {
                        ConfigHandler.CounterMode[] modes = ConfigHandler.CounterMode.values();
                        int currentIndex = ConfigHandler.counterMode.ordinal();
                        int nextIndex = (currentIndex + 1) % modes.length;
                        ConfigHandler.counterMode = modes[nextIndex];
                        ConfigHandler.syncAndSave();
                        
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Counter mode: ")
                                .formatted(Formatting.WHITE))
                            .append(Text.literal(ConfigHandler.counterMode.getDisplayName())
                                .formatted(Formatting.AQUA, Formatting.BOLD)));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS_SUGGESTION)
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            int newCount = PartySlayerCounter.incrementKillCount(playerName);
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("+1 for ")
                                    .formatted(Formatting.GREEN))
                                .append(Text.literal(playerName)
                                    .formatted(Formatting.WHITE, Formatting.BOLD))
                                .append(Text.literal(" (total: " + newCount + ")")
                                    .formatted(Formatting.GRAY)));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            Set<String> players = PartySlayerCounter.getTrackedPlayerNames();
                            for (String player : players) {
                                if (player.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                    builder.suggest(player);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            int newCount = PartySlayerCounter.decrementKillCount(playerName);
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("-1 for ")
                                    .formatted(Formatting.RED))
                                .append(Text.literal(playerName)
                                    .formatted(Formatting.WHITE, Formatting.BOLD))
                                .append(Text.literal(" (total: " + newCount + ")")
                                    .formatted(Formatting.GRAY)));
                            return 1;
                        }))))
            .then(ClientCommandManager.literal("widget")
                .executes(context -> {
                    MinecraftClient.getInstance().send(() -> 
                        MinecraftClient.getInstance().setScreen(new WidgetPositionGui(null))
                    );
                    return 1;
                }))
            .then(ClientCommandManager.literal("whitelist")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS_SUGGESTION)
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ConfigHandler.addPlayerToRender(playerName);
                            SREGui.syncFromConfigHandler();
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Added player to whitelist: ")
                                    .formatted(Formatting.BLUE))
                                .append(Text.literal(playerName)
                                    .formatted(Formatting.BOLD)));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests(WHITELISTED_PLAYERS_SUGGESTION)
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ConfigHandler.removePlayerToRender(playerName);
                            SREGui.syncFromConfigHandler();
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Removed player from whitelist: ")
                                    .formatted(Formatting.BLUE))
                                .append(Text.literal(playerName)
                                    .formatted(Formatting.BOLD)));
                            return 1;
                        })))
            )
        );
    }
    
    private static Text getHelpText() {
        return Text.literal("------------")
            .formatted(Formatting.DARK_BLUE)
            .append(Text.literal("[SRE]")
                .formatted(Formatting.GREEN))
            .append(Text.literal("------------\n")
                .formatted(Formatting.DARK_BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre - Open config GUI\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre toggle player - Toggle player render\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre toggle slayer - Cycle slayer mode (Off/Hide/Glow)\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre toggle counter - Toggle party slayer counter\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter - Show party slayer kill counts\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter mode - Toggle auto/manual mode\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter add <player> - Add +1 kill\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter remove <player> - Remove -1 kill\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter clear - Clear counter data\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre widget - Open widget position editor\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre whitelist add <player> - Add player\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre whitelist remove <player> - Remove player\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("------------------------------------------")
                .formatted(Formatting.DARK_BLUE));
    }
}
