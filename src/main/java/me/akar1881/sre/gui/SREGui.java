package me.akar1881.sre.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.config.ConfigHandler.SlayerMode;
import me.akar1881.sre.config.ConfigHandler.CounterMode;
import me.akar1881.sre.counter.PartySlayerCounter;
import me.akar1881.sre.keybinds.Keybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class SREGui {

    public static void syncFromConfigHandler() {
    }

    public static Screen createScreen(Screen parent) {
        String toggleKey = Keybinds.toggleSre != null ? 
            Keybinds.toggleSre.getBoundKeyLocalizedText().getString() : "V";
        String guiKey = Keybinds.openGui != null ? 
            Keybinds.openGui.getBoundKeyLocalizedText().getString() : "M";
        String slayerKey = Keybinds.toggleSlayer != null ?
            Keybinds.toggleSlayer.getBoundKeyLocalizedText().getString() : "B";

        return YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Skyblock Render Enhanced v1.0.4"))
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("General"))
                .tooltip(Text.literal("General settings for SRE"))
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Render All Players"))
                    .description(OptionDescription.of(Text.literal("When enabled, all players are visible (mod disabled).\nWhen disabled, only whitelisted players are visible.")))
                    .binding(
                        true,
                        () -> ConfigHandler.renderPlayers,
                        newValue -> {
                            ConfigHandler.renderPlayers = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("ON").formatted(Formatting.GREEN) : Text.literal("OFF").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Render Party Members"))
                    .description(OptionDescription.of(Text.literal("When enabled, Hypixel party members are always visible.\nPerfect for Diana parties and group activities!")))
                    .binding(
                        true,
                        () -> ConfigHandler.renderPartyMembers,
                        newValue -> {
                            ConfigHandler.renderPartyMembers = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("YES").formatted(Formatting.GREEN) : Text.literal("NO").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Enable Keybinds"))
                    .description(OptionDescription.of(Text.literal("Enable keyboard shortcuts.\nDisable if keybinds conflict with other mods.")))
                    .binding(
                        true,
                        () -> ConfigHandler.keybindsEnabled,
                        newValue -> {
                            ConfigHandler.keybindsEnabled = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("YES").formatted(Formatting.GREEN) : Text.literal("NO").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Keybinds Info"))
                    .collapsed(false)
                    .description(OptionDescription.of(Text.literal("Current keybind assignments")))
                    .option(LabelOption.create(Text.literal("Toggle Mod: ").append(Text.literal(toggleKey).formatted(Formatting.YELLOW))))
                    .option(LabelOption.create(Text.literal("Open GUI: ").append(Text.literal(guiKey).formatted(Formatting.YELLOW))))
                    .option(LabelOption.create(Text.literal("Toggle Slayer: ").append(Text.literal(slayerKey).formatted(Formatting.YELLOW))))
                    .option(LabelOption.create(Text.literal("Configure keybinds in Minecraft Options > Controls").formatted(Formatting.GRAY)))
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Slayer"))
                .tooltip(Text.literal("Control slayer boss visibility"))
                .option(Option.<SlayerMode>createBuilder()
                    .name(Text.literal("Slayer Mode"))
                    .description(OptionDescription.of(Text.literal(
                        "OFF: Show all slayer bosses (feature disabled)\n" +
                        "HIDE: Hide other players' slayer bosses\n" +
                        "GLOW: Highlight your/party/whitelist bosses with glow effect")))
                    .binding(
                        SlayerMode.OFF,
                        () -> ConfigHandler.slayerMode,
                        newValue -> {
                            ConfigHandler.slayerMode = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(SlayerMode.class)
                        .formatValue(mode -> Text.literal(mode.getDisplayName()).formatted(
                            mode == SlayerMode.OFF ? Formatting.GRAY :
                            mode == SlayerMode.HIDE ? Formatting.RED :
                            Formatting.GREEN)))
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Link Slayer Toggle To Player Toggle"))
                    .description(OptionDescription.of(Text.literal("When enabled, pressing your player render key will also toggle all slayer boss visibility.")))
                    .binding(
                        false,
                        () -> ConfigHandler.linkSlayerToPlayer,
                        newValue -> {
                            ConfigHandler.linkSlayerToPlayer = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("YES").formatted(Formatting.GREEN) : Text.literal("NO").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Keybind Info"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("Toggle Slayer Bosses: ").append(Text.literal(slayerKey).formatted(Formatting.YELLOW))))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Slayer Bosses"))
                    .collapsed(false)
                    .description(OptionDescription.of(Text.literal("The following slayer bosses are detected:")))
                    .option(LabelOption.create(Text.literal("• Voidgloom Seraph").formatted(Formatting.LIGHT_PURPLE)))
                    .option(LabelOption.create(Text.literal("• Revenant Horror").formatted(Formatting.DARK_GREEN)))
                    .option(LabelOption.create(Text.literal("• Tarantula Broodfather").formatted(Formatting.RED)))
                    .option(LabelOption.create(Text.literal("• Sven Packmaster").formatted(Formatting.GRAY)))
                    .option(LabelOption.create(Text.literal("• Inferno Demonlord").formatted(Formatting.GOLD)))
                    .option(LabelOption.create(Text.literal("• Riftstalker Bloodfiend").formatted(Formatting.DARK_RED)))
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Party Counter"))
                .tooltip(Text.literal("Track slayer boss kills for party members"))
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Enable Party Slayer Counter"))
                    .description(OptionDescription.of(Text.literal(
                        "When enabled, tracks how many slayer bosses you've killed for party members.\n" +
                        "Perfect for slayer carries - count exactly how many bosses you did!\n" +
                        "Data is saved to cache and cleared when party disbands.")))
                    .binding(
                        false,
                        () -> ConfigHandler.counterEnabled,
                        newValue -> {
                            ConfigHandler.counterEnabled = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("ON").formatted(Formatting.GREEN) : Text.literal("OFF").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .option(Option.<CounterMode>createBuilder()
                    .name(Text.literal("Counter Mode"))
                    .description(OptionDescription.of(Text.literal(
                        "AUTO: Automatically counts kills + allows manual adjustments\n" +
                        "MANUAL: Only count manually with commands\n\n" +
                        "Use /sre counter add <player> to add kills\n" +
                        "Use /sre counter remove <player> to subtract kills")))
                    .binding(
                        CounterMode.AUTO,
                        () -> ConfigHandler.counterMode,
                        newValue -> {
                            ConfigHandler.counterMode = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(CounterMode.class)
                        .formatValue(mode -> Text.literal(mode.getDisplayName()).formatted(
                            mode == CounterMode.AUTO ? Formatting.GREEN : Formatting.YELLOW)))
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Show Counter Widget"))
                    .description(OptionDescription.of(Text.literal(
                        "Display a widget on screen showing party member kill counts.\n" +
                        "Use /sre widget to customize the widget position.")))
                    .binding(
                        true,
                        () -> ConfigHandler.counterWidgetEnabled,
                        newValue -> {
                            ConfigHandler.counterWidgetEnabled = newValue;
                            ConfigHandler.syncAndSave();
                        })
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("YES").formatted(Formatting.GREEN) : Text.literal("NO").formatted(Formatting.RED))
                        .coloured(true))
                    .build())
                .option(ButtonOption.createBuilder()
                    .name(Text.literal("Edit Widget Position"))
                    .description(OptionDescription.of(Text.literal("Open widget position editor to drag and place the counter widget anywhere on screen.")))
                    .action((screen, button) -> {
                        MinecraftClient.getInstance().setScreen(new WidgetPositionGui(screen));
                    })
                    .build())
                .option(ButtonOption.createBuilder()
                    .name(Text.literal("Clear Counter Data"))
                    .description(OptionDescription.of(Text.literal("Reset all kill count data for party members.")))
                    .action((screen, button) -> {
                        PartySlayerCounter.clearCounter();
                    })
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Commands"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("/sre counter - View kill counts").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre counter mode - Toggle auto/manual").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre counter add <player> - Add +1 kill").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre counter remove <player> - Remove -1 kill").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre counter clear - Clear all data").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre widget - Edit widget position").formatted(Formatting.YELLOW)))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("How It Works"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("1. Enable the counter and join a party").formatted(Formatting.WHITE)))
                    .option(LabelOption.create(Text.literal("2. When party members spawn slayer bosses,").formatted(Formatting.WHITE)))
                    .option(LabelOption.create(Text.literal("   and YOU kill them, it gets counted").formatted(Formatting.WHITE)))
                    .option(LabelOption.create(Text.literal("3. View counts with /sre counter or widget").formatted(Formatting.WHITE)))
                    .option(LabelOption.create(Text.literal("4. Data clears when party disbands").formatted(Formatting.WHITE)))
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Player Whitelist"))
                .tooltip(Text.literal("Manage which players are always visible"))
                .group(ListOption.<String>createBuilder()
                    .name(Text.literal("Whitelisted Players"))
                    .description(OptionDescription.of(Text.literal("Players in this list will always be visible.\nUse the + button to add players and X to remove them.")))
                    .binding(
                        new ArrayList<>(),
                        () -> new ArrayList<>(ConfigHandler.playersToRender),
                        newValue -> {
                            ConfigHandler.playersToRender = new ArrayList<>(newValue);
                            ConfigHandler.syncAndSave();
                        })
                    .controller(StringControllerBuilder::create)
                    .initial("")
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Commands"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("/sre whitelist add <player>").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre whitelist remove <player>").formatted(Formatting.YELLOW)))
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Help"))
                .tooltip(Text.literal("How to use SRE"))
                .option(LabelOption.create(Text.literal("GETTING STARTED").formatted(Formatting.GOLD, Formatting.BOLD)))
                .option(LabelOption.create(Text.literal("SRE helps you see better in crowded areas!")))
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Basic Usage"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("1. Disable 'Render All Players' in General")))
                    .option(LabelOption.create(Text.literal("2. Add players using the + button in Player Whitelist")))
                    .option(LabelOption.create(Text.literal("3. All other players will be hidden!")))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Commands"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("/sre - Open this config screen").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre toggle - Toggle mod on/off").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre counter - View party kill counts").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre widget - Edit widget position").formatted(Formatting.YELLOW)))
                    .option(LabelOption.create(Text.literal("/sre help - Show all commands").formatted(Formatting.YELLOW)))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("About"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("Skyblock Render Enhanced v1.0.4").formatted(Formatting.GOLD)))
                    .option(LabelOption.create(Text.literal("For Minecraft 1.21.10 with Fabric").formatted(Formatting.GRAY)))
                    .option(LabelOption.create(Text.literal("Created for Hypixel Skyblock players").formatted(Formatting.GRAY)))
                    .build())
                .build())
            .build()
            .generateScreen(parent);
    }
}
