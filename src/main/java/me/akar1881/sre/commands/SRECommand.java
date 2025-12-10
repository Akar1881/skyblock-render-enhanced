package me.akar1881.sre.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.gui.SREGui;
import me.akar1881.sre.util.OnlinePlayers;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    
    private static final SuggestionProvider<FabricClientCommandSource> RENDERED_PLAYERS_SUGGESTION = (context, builder) -> {
        Set<String> players = ConfigHandler.getPlayersToRenderSet();
        for (String player : players) {
            if (player.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(player);
            }
        }
        return builder.buildFuture();
    };
    
    private static final SuggestionProvider<FabricClientCommandSource> WHITELISTED_PLAYERS_SUGGESTION = (context, builder) -> {
        Set<String> players = ConfigHandler.getWhitelistedPlayersSet();
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
            .then(ClientCommandManager.literal("whitelist")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS_SUGGESTION)
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ConfigHandler.addPlayerToRender(playerName);
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
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Removed player from whitelist: ")
                                    .formatted(Formatting.BLUE))
                                .append(Text.literal(playerName)
                                    .formatted(Formatting.BOLD)));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("list")
                    .executes(context -> {
                        Set<String> players = ConfigHandler.getPlayersToRenderSet();
                        String str = players.isEmpty() ? "none" : String.join(", ", players);
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Whitelisted players: ")
                                .formatted(Formatting.BLUE))
                            .append(Text.literal(str)
                                .formatted(Formatting.BOLD)));
                        return 1;
                    }))
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
            .append(Text.literal("/sre toggle - Toggle mod on/off\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre whitelist add <player> - Add player\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre whitelist remove <player> - Remove player\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre whitelist list - Show whitelisted players\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("------------------------------------------")
                .formatted(Formatting.DARK_BLUE));
    }
}
