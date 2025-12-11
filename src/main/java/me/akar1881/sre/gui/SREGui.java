package me.akar1881.sre.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.keybinds.Keybinds;
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

        return YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Skyblock Render Enhanced v1.0.1"))
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
                    .option(LabelOption.create(Text.literal("Configure keybinds in Minecraft Options > Controls").formatted(Formatting.GRAY)))
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
                    .option(LabelOption.create(Text.literal("/sre help - Show all commands").formatted(Formatting.YELLOW)))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("About"))
                    .collapsed(false)
                    .option(LabelOption.create(Text.literal("Skyblock Render Enhanced v1.0.1").formatted(Formatting.GOLD)))
                    .option(LabelOption.create(Text.literal("For Minecraft 1.21.10 with Fabric").formatted(Formatting.GRAY)))
                    .option(LabelOption.create(Text.literal("Created for Hypixel Skyblock players").formatted(Formatting.GRAY)))
                    .build())
                .build())
            .build()
            .generateScreen(parent);
    }
}
