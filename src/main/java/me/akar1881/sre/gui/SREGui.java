package me.akar1881.sre.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.keybinds.Keybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class SREGui {
    private static final String MODRINTH_URL = "https://modrinth.com/mod/skyblocker-render-enhanced";
    
    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("Skyblock Render Enhanced"))
            .setSavingRunnable(ConfigHandler::syncAndSave);
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        createGeneralCategory(builder, entryBuilder);
        createPlayerListCategory(builder, entryBuilder);
        createHowToUseCategory(builder, entryBuilder);
        createAboutCategory(builder, entryBuilder);
        
        return builder.build();
    }
    
    private static void createGeneralCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        
        general.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Render All Players"),
                ConfigHandler.renderPlayers)
            .setDefaultValue(true)
            .setTooltip(Text.literal(
                "When enabled, all players will be visible (mod disabled).\n" +
                "When disabled, only whitelisted players will be visible."))
            .setSaveConsumer(value -> ConfigHandler.renderPlayers = value)
            .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Enable Keybinds"),
                ConfigHandler.keybindsEnabled)
            .setDefaultValue(true)
            .setTooltip(Text.literal(
                "When enabled, keyboard shortcuts will work.\n" +
                "Disable if keybinds conflict with other mods."))
            .setSaveConsumer(value -> ConfigHandler.keybindsEnabled = value)
            .build());
        
        SubCategoryBuilder keybindsCategory = entryBuilder.startSubCategory(Text.literal("Keybinds Info"));
        keybindsCategory.setTooltip(Text.literal("View keyboard shortcuts"));
        
        keybindsCategory.add(entryBuilder.startTextDescription(
            Text.literal("Toggle Mod: ")
                .append(Text.literal(Keybinds.toggleSre != null ? 
                    Keybinds.toggleSre.getBoundKeyLocalizedText().getString() : "V")
                    .formatted(Formatting.YELLOW)))
            .build());
        
        keybindsCategory.add(entryBuilder.startTextDescription(
            Text.literal("Open GUI: ")
                .append(Text.literal(Keybinds.openGui != null ? 
                    Keybinds.openGui.getBoundKeyLocalizedText().getString() : "M")
                    .formatted(Formatting.YELLOW)))
            .build());
        
        keybindsCategory.add(entryBuilder.startTextDescription(
            Text.literal("Configure keybinds in Minecraft Options > Controls > Key Binds")
                .formatted(Formatting.GRAY))
            .build());
        
        general.addEntry(keybindsCategory.build());
    }
    
    private static void createPlayerListCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory playerList = builder.getOrCreateCategory(Text.literal("Player Whitelist"));
        
        playerList.addEntry(entryBuilder.startTextDescription(
            Text.literal("Players in this list will always be visible when the mod is active.\n")
                .formatted(Formatting.GRAY)
                .append(Text.literal("Add player usernames below or use commands:\n")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/sre whitelist add <player>")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" and ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/sre whitelist remove <player>")
                    .formatted(Formatting.YELLOW)))
            .build());
        
        List<String> currentPlayers = new ArrayList<>(ConfigHandler.getPlayersToRenderSet());
        
        playerList.addEntry(entryBuilder.startStrList(
                Text.literal("Whitelisted Players"),
                currentPlayers)
            .setDefaultValue(new ArrayList<>())
            .setTooltip(Text.literal("Add player usernames to show them when mod is active"))
            .setSaveConsumer(list -> {
                ConfigHandler.setPlayersToRenderSet(new LinkedHashSet<>(list));
            })
            .setAddButtonTooltip(Text.literal("Add new player"))
            .setRemoveButtonTooltip(Text.literal("Remove player"))
            .setInsertButtonEnabled(true)
            .setDeleteButtonEnabled(true)
            .build());
    }
    
    private static void createHowToUseCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory howToUse = builder.getOrCreateCategory(Text.literal("How to Use"));
        
        howToUse.addEntry(entryBuilder.startTextDescription(
            Text.literal("GETTING STARTED\n")
                .formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal("\nSkyblock Render Enhanced helps you see better in\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("crowded areas like hub drops!\n\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("BASIC USAGE:\n")
                    .formatted(Formatting.GREEN, Formatting.BOLD))
                .append(Text.literal("1. Disable 'Render All Players' in General settings\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("2. Add players you want to see in Player Whitelist\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("3. All other players will be hidden!\n\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("QUICK TOGGLE:\n")
                    .formatted(Formatting.GREEN, Formatting.BOLD))
                .append(Text.literal("Press ")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("V")
                    .formatted(Formatting.YELLOW, Formatting.BOLD))
                .append(Text.literal(" (default) to quickly toggle visibility\n\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("COMMANDS:\n")
                    .formatted(Formatting.GREEN, Formatting.BOLD))
                .append(Text.literal("/sre")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Open this config screen\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("/sre toggle")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Toggle mod on/off\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("/sre whitelist add <player>")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Add player\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("/sre whitelist remove <player>")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Remove player\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("/sre whitelist list")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Show whitelisted players\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("/sre help")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" - Show all commands")
                    .formatted(Formatting.WHITE)))
            .build());
        
        SubCategoryBuilder tips = entryBuilder.startSubCategory(Text.literal("Pro Tips"));
        tips.setExpanded(false);
        tips.add(entryBuilder.startTextDescription(
            Text.literal("- Use during Hypixel Skyblock hub events\n")
                .formatted(Formatting.AQUA)
                .append(Text.literal("- Add friends and party members before events\n")
                    .formatted(Formatting.AQUA))
                .append(Text.literal("- Quickly toggle with V key when needed\n")
                    .formatted(Formatting.AQUA))
                .append(Text.literal("- Whitelist persists between sessions\n")
                    .formatted(Formatting.AQUA))
                .append(Text.literal("- You are always visible to yourself!")
                    .formatted(Formatting.AQUA)))
            .build());
        howToUse.addEntry(tips.build());
    }
    
    private static void createAboutCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory about = builder.getOrCreateCategory(Text.literal("About"));
        
        about.addEntry(entryBuilder.startTextDescription(
            Text.literal("SKYBLOCK RENDER ENHANCED\n")
                .formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal("Version: 1.0.0\n\n")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("A Fabric mod that allows you to selectively\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("hide/show other players in Hypixel Skyblock.\n\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("Perfect for crowded areas like hub drops,\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("where you only want to see specific players.\n\n")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("Created for Minecraft 1.21.10")
                    .formatted(Formatting.GRAY)))
            .build());
        
        about.addEntry(entryBuilder.startTextDescription(
            Text.literal("\nMODRINTH PAGE:\n")
                .formatted(Formatting.GREEN, Formatting.BOLD)
                .append(Text.literal(MODRINTH_URL)
                    .formatted(Formatting.AQUA, Formatting.UNDERLINE)))
            .build());
        
        about.addEntry(entryBuilder.startTextDescription(
            Text.literal("\nClick the link above or copy it to your browser\n")
                .formatted(Formatting.GRAY)
                .append(Text.literal("for updates, downloads, and more!")
                    .formatted(Formatting.GRAY)))
            .build());
    }
}
