package me.akar1881.sre.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.counter.PartySlayerCounter;
import me.akar1881.sre.enchantment.EnchantmentData;
import me.akar1881.sre.enchantment.EnchantmentData.EnchantmentInfo;
import me.akar1881.sre.enchantment.EnchantmentHelper;
import me.akar1881.sre.enchantment.EnchantmentHelper.MissingEnchantment;
import me.akar1881.sre.enchantment.EnchantmentHelper.UltimateEnchantmentResult;
import me.akar1881.sre.gui.SREGui;
import me.akar1881.sre.gui.WidgetPositionGui;
import me.akar1881.sre.util.OnlinePlayers;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
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
                    })
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
                            boolean removed = PartySlayerCounter.clearPlayerCounter(playerName);
                            if (removed) {
                                context.getSource().sendFeedback(Text.literal("[SRE] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("Cleared counter for ")
                                        .formatted(Formatting.YELLOW))
                                    .append(Text.literal(playerName)
                                        .formatted(Formatting.WHITE, Formatting.BOLD)));
                            } else {
                                context.getSource().sendFeedback(Text.literal("[SRE] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("No counter data found for ")
                                        .formatted(Formatting.RED))
                                    .append(Text.literal(playerName)
                                        .formatted(Formatting.WHITE)));
                            }
                            return 1;
                        })))
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
                        })
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                String playerName = StringArgumentType.getString(context, "player");
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                int newCount = PartySlayerCounter.addKillCount(playerName, amount);
                                context.getSource().sendFeedback(Text.literal("[SRE] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("+" + amount + " for ")
                                        .formatted(Formatting.GREEN))
                                    .append(Text.literal(playerName)
                                        .formatted(Formatting.WHITE, Formatting.BOLD))
                                    .append(Text.literal(" (total: " + newCount + ")")
                                        .formatted(Formatting.GRAY)));
                                return 1;
                            }))))
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
                        })
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                String playerName = StringArgumentType.getString(context, "player");
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                int newCount = PartySlayerCounter.removeKillCount(playerName, amount);
                                context.getSource().sendFeedback(Text.literal("[SRE] ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal("-" + amount + " for ")
                                        .formatted(Formatting.RED))
                                    .append(Text.literal(playerName)
                                        .formatted(Formatting.WHITE, Formatting.BOLD))
                                    .append(Text.literal(" (total: " + newCount + ")")
                                        .formatted(Formatting.GRAY)));
                                return 1;
                            })))))
            .then(ClientCommandManager.literal("widget")
                .executes(context -> {
                    MinecraftClient.getInstance().send(() -> 
                        MinecraftClient.getInstance().setScreen(new WidgetPositionGui(null))
                    );
                    return 1;
                }))
            .then(ClientCommandManager.literal("missing")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("[SRE] ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("Usage: /sre missing regular OR /sre missing ultimate")
                            .formatted(Formatting.YELLOW)));
                    return 1;
                })
                .then(ClientCommandManager.literal("regular")
                    .executes(context -> {
                        if (!ConfigHandler.enchantmentHelperEnabled) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Enchantment Helper is disabled. Enable it in settings first.")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        ItemStack heldItem = EnchantmentHelper.getHeldItem();
                        if (heldItem.isEmpty()) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("You are not holding any item!")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        EnchantmentData.ItemType itemType = EnchantmentHelper.detectItemType(heldItem);
                        if (itemType == null) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("This item is not a recognized sword or bow!")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        List<MissingEnchantment> missing = EnchantmentHelper.getMissingEnchantments(heldItem);
                        
                        if (missing.isEmpty()) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("This item has all regular enchantments at max level!")
                                    .formatted(Formatting.GOLD)));
                            return 1;
                        }
                        
                        String itemTypeName = itemType == EnchantmentData.ItemType.BOW ? "Bow" : "Sword";
                        context.getSource().sendFeedback(Text.literal("------------")
                            .formatted(Formatting.DARK_PURPLE)
                            .append(Text.literal("[Missing Regular Enchantments - " + itemTypeName + "]")
                                .formatted(Formatting.GOLD))
                            .append(Text.literal("------------")
                                .formatted(Formatting.DARK_PURPLE)));
                        
                        for (MissingEnchantment enchant : missing) {
                            String missingLevels = enchant.getMissingLevelsString();
                            if (!missingLevels.isEmpty()) {
                                context.getSource().sendFeedback(Text.literal("  ")
                                    .append(Text.literal(enchant.name + " ")
                                        .formatted(Formatting.AQUA))
                                    .append(Text.literal(missingLevels)
                                        .formatted(Formatting.YELLOW)));
                            }
                        }
                        
                        context.getSource().sendFeedback(Text.literal("------------------------------------------")
                            .formatted(Formatting.DARK_PURPLE));
                        
                        return 1;
                    }))
                .then(ClientCommandManager.literal("ultimate")
                    .executes(context -> {
                        if (!ConfigHandler.enchantmentHelperEnabled) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Enchantment Helper is disabled. Enable it in settings first.")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        ItemStack heldItem = EnchantmentHelper.getHeldItem();
                        if (heldItem.isEmpty()) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("You are not holding any item!")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        EnchantmentData.ItemType itemType = EnchantmentHelper.detectItemType(heldItem);
                        if (itemType == null) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("This item is not a recognized sword or bow!")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        UltimateEnchantmentResult result = EnchantmentHelper.getUltimateEnchantmentStatus(heldItem);
                        if (result == null) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Could not check ultimate enchantments!")
                                    .formatted(Formatting.RED)));
                            return 1;
                        }
                        
                        String itemTypeName = itemType == EnchantmentData.ItemType.BOW ? "Bow" : "Sword";
                        context.getSource().sendFeedback(Text.literal("------------")
                            .formatted(Formatting.LIGHT_PURPLE)
                            .append(Text.literal("[Ultimate Enchantments - " + itemTypeName + "]")
                                .formatted(Formatting.GOLD))
                            .append(Text.literal("------------")
                                .formatted(Formatting.LIGHT_PURPLE)));
                        
                        if (result.hasUltimate) {
                            if (result.isMaxLevel) {
                                context.getSource().sendFeedback(Text.literal("  ")
                                    .append(Text.literal("Applied: ")
                                        .formatted(Formatting.WHITE))
                                    .append(Text.literal(result.appliedName + " " + EnchantmentData.toRoman(result.appliedLevel))
                                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                                    .append(Text.literal(" (MAX LEVEL)")
                                        .formatted(Formatting.GREEN, Formatting.BOLD)));
                            } else {
                                context.getSource().sendFeedback(Text.literal("  ")
                                    .append(Text.literal("Applied: ")
                                        .formatted(Formatting.WHITE))
                                    .append(Text.literal(result.appliedName + " " + EnchantmentData.toRoman(result.appliedLevel))
                                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)));
                                context.getSource().sendFeedback(Text.literal("  ")
                                    .append(Text.literal("Next tier: ")
                                        .formatted(Formatting.WHITE))
                                    .append(Text.literal(result.appliedName + " " + EnchantmentData.toRoman(result.getNextLevel()))
                                        .formatted(Formatting.YELLOW)));
                            }
                        } else {
                            context.getSource().sendFeedback(Text.literal("  ")
                                .append(Text.literal("No ultimate enchantment applied!")
                                    .formatted(Formatting.RED)));
                            context.getSource().sendFeedback(Text.literal("  ")
                                .append(Text.literal("Available ultimate enchantments:")
                                    .formatted(Formatting.WHITE)));
                            
                            for (Map.Entry<String, EnchantmentInfo> entry : result.availableUltimates.entrySet()) {
                                EnchantmentInfo info = entry.getValue();
                                context.getSource().sendFeedback(Text.literal("    ")
                                    .append(Text.literal("- " + info.name + " " + info.getLevelRange())
                                        .formatted(Formatting.LIGHT_PURPLE)));
                            }
                        }
                        
                        context.getSource().sendFeedback(Text.literal("------------------------------------------")
                            .formatted(Formatting.LIGHT_PURPLE));
                        
                        return 1;
                    })))
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
            .append(Text.literal("/sre counter add <player> [amount] - Add kills\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter remove <player> [amount] - Remove kills\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter clear [player] - Clear counter data\n")
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
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre missing - Show missing enchantments on held item\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("------------------------------------------")
                .formatted(Formatting.DARK_BLUE));
    }
}
