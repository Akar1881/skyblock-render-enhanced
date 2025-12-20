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
import me.akar1881.sre.util.SlayerCarryCalculator;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
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
                        ConfigHandler.slayerMode = ConfigHandler.slayerMode == ConfigHandler.SlayerMode.OFF ? 
                            ConfigHandler.SlayerMode.GLOW : ConfigHandler.SlayerMode.OFF;
                        ConfigHandler.syncAndSave();
                        
                        Formatting color = ConfigHandler.slayerMode == ConfigHandler.SlayerMode.OFF ? 
                            Formatting.GRAY : Formatting.GREEN;
                        
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Slayer highlight: ")
                                .formatted(Formatting.WHITE))
                            .append(Text.literal(ConfigHandler.slayerMode.getDisplayName())
                                .formatted(Formatting.BOLD, color)));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("spawn-alert")
                    .executes(context -> {
                        ConfigHandler.slayerSpawnAlertEnabled = !ConfigHandler.slayerSpawnAlertEnabled;
                        ConfigHandler.syncAndSave();
                        if (ConfigHandler.slayerSpawnAlertEnabled) {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Boss spawn alerts are now ")
                                    .formatted(Formatting.GREEN))
                                .append(Text.literal("on")
                                    .formatted(Formatting.BOLD, Formatting.GREEN)));
                        } else {
                            context.getSource().sendFeedback(Text.literal("[SRE] ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal("Boss spawn alerts are now ")
                                    .formatted(Formatting.RED))
                                .append(Text.literal("off")
                                    .formatted(Formatting.BOLD, Formatting.RED)));
                        }
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
                            }))))
                .then(ClientCommandManager.literal("math")
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Usage:\n")
                                .formatted(Formatting.YELLOW))
                            .append(Text.literal("/sre counter math slayer <type> <tier> <price> <amount> [discount%]\n")
                                .formatted(Formatting.AQUA))
                            .append(Text.literal("/sre counter math xp <type> <tier> <currentXP/targetXP> [xpPerBoss]")
                                .formatted(Formatting.AQUA)));
                        return 1;
                    })
                    .then(ClientCommandManager.literal("slayer")
                        .then(ClientCommandManager.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                String[] types = {"voidgloom", "revenant", "tarantula", "sven", "inferno", "riftstalker"};
                                for (String type : types) {
                                    if (type.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                        builder.suggest(type);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(ClientCommandManager.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String[] tiers = {"t1", "t2", "t3", "t4", "t5"};
                                    for (String tier : tiers) {
                                        if (tier.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                            builder.suggest(tier);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(ClientCommandManager.argument("price", StringArgumentType.word())
                                    .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            return executeSlayerMath(context, 0);
                                        })
                                        .then(ClientCommandManager.argument("discount", IntegerArgumentType.integer(0, 100))
                                            .executes(context -> {
                                                int discount = IntegerArgumentType.getInteger(context, "discount");
                                                return executeSlayerMath(context, discount);
                                            })))))))
                    .then(ClientCommandManager.literal("xp")
                        .then(ClientCommandManager.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                String[] types = {"voidgloom", "revenant", "tarantula", "sven", "inferno", "riftstalker"};
                                for (String type : types) {
                                    if (type.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                        builder.suggest(type);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(ClientCommandManager.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String[] tiers = {"t1", "t2", "t3", "t4", "t5"};
                                    for (String tier : tiers) {
                                        if (tier.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                            builder.suggest(tier);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(ClientCommandManager.argument("xprange", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        return executeXPMath(context, -1);
                                    })
                                    .then(ClientCommandManager.argument("xpperboss", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            int customXP = IntegerArgumentType.getInteger(context, "xpperboss");
                                            return executeXPMath(context, customXP);
                                        }))))))))
            .then(ClientCommandManager.literal("sharechat")
                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> {
                        String message = StringArgumentType.getString(context, "message");
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null && client.player.networkHandler != null) {
                            client.player.networkHandler.sendChatMessage(message);
                        }
                        return 1;
                    })))
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
            .append(Text.literal("/sre toggle slayer - Toggle slayer highlight (Off/On)\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre toggle spawn-alert - Toggle boss spawn alerts\n")
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
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter math slayer <type> <tier> <price> <amount> [discount%]\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/sre counter math xp <type> <tier> <currentXP/targetXP> [xpPerBoss]\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("+ ")
                .formatted(Formatting.RED))
            .append(Text.literal("/f1-/f7, /m1-/m7 - Quick dungeon join commands\n")
                .formatted(Formatting.BLUE))
            .append(Text.literal("------------------------------------------")
                .formatted(Formatting.DARK_BLUE));
    }
    
    private static int executeSlayerMath(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, int discount) {
        String typeInput = StringArgumentType.getString(context, "type");
        String tierInput = StringArgumentType.getString(context, "tier");
        String priceInput = StringArgumentType.getString(context, "price");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        
        String normalizedType = SlayerCarryCalculator.normalizeSlayerType(typeInput);
        if (normalizedType == null) {
            context.getSource().sendFeedback(Text.literal("[SRE] ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("Unknown slayer type: " + typeInput)
                    .formatted(Formatting.RED)));
            return 1;
        }
        
        int tier = SlayerCarryCalculator.parseTier(tierInput);
        if (tier < 1 || tier > 5) {
            context.getSource().sendFeedback(Text.literal("[SRE] ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("Invalid tier. Use t1-t5 or 1-5.")
                    .formatted(Formatting.RED)));
            return 1;
        }
        
        double priceEach = SlayerCarryCalculator.parsePrice(priceInput);
        if (priceEach <= 0) {
            context.getSource().sendFeedback(Text.literal("[SRE] ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("Invalid price. Use format like 1.3m, 500k, or 1000000.")
                    .formatted(Formatting.RED)));
            return 1;
        }
        
        SlayerCarryCalculator.SlayerCalcResult result = new SlayerCarryCalculator.SlayerCalcResult(
            normalizedType, tier, amount, priceEach, discount
        );
        
        context.getSource().sendFeedback(Text.literal("======[SRE] Calculating Slayer Carry======")
            .formatted(Formatting.GOLD));
        context.getSource().sendFeedback(Text.literal("Type: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(result.type)
                .formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("Tier: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal("T" + result.tier)
                .formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("Amount: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(String.valueOf(result.amount) + " bosses")
                .formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("Price Each: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(SlayerCarryCalculator.formatPrice(result.priceEach))
                .formatted(Formatting.WHITE)));
        
        if (result.discountPercent > 0) {
            context.getSource().sendFeedback(Text.literal("Discount: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(result.discountPercent + "%")
                    .formatted(Formatting.GREEN)));
        } else {
            context.getSource().sendFeedback(Text.literal("Discount: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("NONE")
                    .formatted(Formatting.GRAY)));
        }
        
        context.getSource().sendFeedback(Text.literal("Total Price: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(SlayerCarryCalculator.formatPrice(result.totalPrice))
                .formatted(Formatting.GREEN, Formatting.BOLD)));
        context.getSource().sendFeedback(Text.literal("==========================================")
            .formatted(Formatting.GOLD));
        
        String shareMessage = "[SRE] Slayer Carry: " + result.type + " T" + result.tier + 
            " x" + result.amount + " @ " + SlayerCarryCalculator.formatPrice(result.priceEach) + " each" +
            (result.discountPercent > 0 ? " (" + result.discountPercent + "% off)" : "") +
            " = " + SlayerCarryCalculator.formatPrice(result.totalPrice) + " total";
        
        context.getSource().sendFeedback(Text.literal("[Click here to share in chat]")
            .formatted(Formatting.AQUA, Formatting.UNDERLINE)
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand("/sre sharechat " + shareMessage))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to send calculation to chat")))));
        
        return 1;
    }
    
    private static int executeXPMath(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, int customXPPerBoss) {
    String typeInput = StringArgumentType.getString(context, "type");
    String tierInput = StringArgumentType.getString(context, "tier");
    String xpRangeInput = StringArgumentType.getString(context, "xprange");
    
    String normalizedType = SlayerCarryCalculator.normalizeSlayerType(typeInput);
    if (normalizedType == null) {
        context.getSource().sendFeedback(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("Unknown slayer type: " + typeInput)
                .formatted(Formatting.RED)));
        return 1;
    }
    
    int tier = SlayerCarryCalculator.parseTier(tierInput);
    if (tier < 1 || tier > 5) {
        context.getSource().sendFeedback(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("Invalid tier. Use t1-t5 or 1-5.")
                .formatted(Formatting.RED)));
        return 1;
    }
    
    String[] allParts = xpRangeInput.trim().split("\\s+");
    String xpRangePart;
    int extractedCustomXP = customXPPerBoss;
    
    if (customXPPerBoss <= 0 && allParts.length >= 2) {
        try {
            xpRangePart = String.join(" ", java.util.Arrays.copyOfRange(allParts, 0, allParts.length - 1));
            String potentialCustomXP = allParts[allParts.length - 1];
            
            if (!potentialCustomXP.contains("/")) {
                extractedCustomXP = (int) SlayerCarryCalculator.parsePrice(potentialCustomXP);
            } else {
                xpRangePart = xpRangeInput.trim();
            }
        } catch (Exception e) {
            xpRangePart = xpRangeInput.trim();
        }
    } else {
        xpRangePart = xpRangeInput.trim();
    }
    
    String[] xpParts = xpRangePart.replace(" ", "").split("/");
    if (xpParts.length != 2) {
        context.getSource().sendFeedback(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("Invalid XP format. Use: currentXP/targetXP (e.g., 50000/100000)")
                .formatted(Formatting.RED)));
        return 1;
    }
    
    int currentXP, targetXP;
    try {
        currentXP = (int) SlayerCarryCalculator.parsePrice(xpParts[0]);
        targetXP = (int) SlayerCarryCalculator.parsePrice(xpParts[1]);
    } catch (Exception e) {
        context.getSource().sendFeedback(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("Invalid XP values. Use numbers like 50000/100000 or 50k/100k")
                .formatted(Formatting.RED)));
        return 1;
    }
    
    if (targetXP <= currentXP) {
        context.getSource().sendFeedback(Text.literal("[SRE] ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("You've already reached your target XP!")
                .formatted(Formatting.GOLD)));
        return 1;
    }
    
    int xpPerBoss;
    if (extractedCustomXP > 0) {
        xpPerBoss = extractedCustomXP;
    } else {
        String slayerKey = SlayerCarryCalculator.getSlayerKey(normalizedType);
        xpPerBoss = SlayerCarryCalculator.getXPPerBoss(slayerKey, tier);
        
        if (xpPerBoss <= 0) {
            context.getSource().sendFeedback(Text.literal("[SRE] ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("This tier is not available for " + normalizedType + ". Specify custom XP per boss at the end.")
                    .formatted(Formatting.RED)));
            return 1;
        }
    }
    
    int bossesNeeded = SlayerCarryCalculator.calculateBossesNeeded(currentXP, targetXP, xpPerBoss);
    
    SlayerCarryCalculator.XPCalcResult result = new SlayerCarryCalculator.XPCalcResult(
        normalizedType, tier, currentXP, targetXP, xpPerBoss, bossesNeeded
    );
    
    context.getSource().sendFeedback(Text.literal("======[SRE] Calculating Slayer XP======")
        .formatted(Formatting.GOLD));
    context.getSource().sendFeedback(Text.literal("Type: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(result.type)
            .formatted(Formatting.WHITE)));
    context.getSource().sendFeedback(Text.literal("Tier: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal("T" + result.tier)
            .formatted(Formatting.WHITE)));
    context.getSource().sendFeedback(Text.literal("Current XP: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(String.format("%,d", result.currentXP))
            .formatted(Formatting.WHITE)));
    context.getSource().sendFeedback(Text.literal("Target XP: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(String.format("%,d", result.targetXP))
            .formatted(Formatting.WHITE)));
    context.getSource().sendFeedback(Text.literal("XP Per Boss: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(String.valueOf(result.xpPerBoss))
            .formatted(Formatting.WHITE)));
    context.getSource().sendFeedback(Text.literal("Bosses Needed: ")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(String.valueOf(result.bossesNeeded))
            .formatted(Formatting.GREEN, Formatting.BOLD)));
    context.getSource().sendFeedback(Text.literal("========================================")
        .formatted(Formatting.GOLD));
    
    String shareMessage = "[SRE] Slayer XP: " + result.type + " T" + result.tier + 
        " | " + String.format("%,d", result.currentXP) + "/" + String.format("%,d", result.targetXP) + " XP" +
        " | Need " + result.bossesNeeded + " bosses (" + result.xpPerBoss + " XP each)";
    
    context.getSource().sendFeedback(Text.literal("[Click here to share in chat]")
        .formatted(Formatting.AQUA, Formatting.UNDERLINE)
        .setStyle(Style.EMPTY
            .withClickEvent(new ClickEvent.RunCommand("/sre sharechat " + shareMessage))
            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to send calculation to chat")))));
    
    return 1;
  }
}
