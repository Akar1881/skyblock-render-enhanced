package me.akar1881.sre.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.akar1881.sre.SkyblockRenderEnhanced;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sre.json");
    
    private static ConfigData config = new ConfigData();
    
    public static boolean renderPlayers = true;
    public static boolean keybindsEnabled = true;
    public static String playersToRender = "";
    public static String whitelistedPlayers = "";
    
    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, ConfigData.class);
                if (config == null) {
                    config = new ConfigData();
                }
                syncToFields();
            } else {
                save();
            }
        } catch (IOException e) {
            SkyblockRenderEnhanced.LOGGER.error("Failed to load config", e);
            config = new ConfigData();
        }
    }
    
    public static void save() {
        try {
            Path parentDir = CONFIG_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            syncFromFields();
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            SkyblockRenderEnhanced.LOGGER.error("Failed to save config", e);
        }
    }
    
    private static void syncToFields() {
        renderPlayers = config.renderPlayers;
        keybindsEnabled = config.keybindsEnabled;
        playersToRender = config.playersToRender;
        whitelistedPlayers = config.whitelistedPlayers;
    }
    
    private static void syncFromFields() {
        config.renderPlayers = renderPlayers;
        config.keybindsEnabled = keybindsEnabled;
        config.playersToRender = playersToRender;
        config.whitelistedPlayers = whitelistedPlayers;
    }
    
    public static void syncAndSave() {
        syncFromFields();
        save();
    }
    
    public static Set<String> getPlayersToRenderSet() {
        Set<String> players = new LinkedHashSet<>();
        if (!playersToRender.isEmpty()) {
            for (String player : playersToRender.split(",")) {
                if (!player.trim().isEmpty()) {
                    players.add(player.trim());
                }
            }
        }
        return players;
    }
    
    public static void setPlayersToRenderSet(Set<String> players) {
        StringBuilder sb = new StringBuilder();
        for (String player : players) {
            if (!player.trim().isEmpty()) {
                sb.append(player.trim()).append(",");
            }
        }
        playersToRender = sb.toString();
        syncAndSave();
    }
    
    public static void addPlayerToRender(String player) {
        Set<String> players = getPlayersToRenderSet();
        players.add(player.trim());
        setPlayersToRenderSet(players);
    }
    
    public static void removePlayerToRender(String player) {
        Set<String> players = getPlayersToRenderSet();
        players.remove(player.trim());
        setPlayersToRenderSet(players);
    }
    
    public static Set<String> getWhitelistedPlayersSet() {
        Set<String> players = new LinkedHashSet<>();
        if (!whitelistedPlayers.isEmpty()) {
            for (String player : whitelistedPlayers.split(",")) {
                if (!player.trim().isEmpty()) {
                    players.add(player.trim());
                }
            }
        }
        return players;
    }
    
    public static void setWhitelistedPlayersSet(Set<String> players) {
        StringBuilder sb = new StringBuilder();
        for (String player : players) {
            if (!player.trim().isEmpty()) {
                sb.append(player.trim()).append(",");
            }
        }
        whitelistedPlayers = sb.toString();
        syncAndSave();
    }
    
    public static void addWhitelistedPlayer(String player) {
        Set<String> players = getWhitelistedPlayersSet();
        players.add(player.trim());
        setWhitelistedPlayersSet(players);
    }
    
    public static void removeWhitelistedPlayer(String player) {
        Set<String> players = getWhitelistedPlayersSet();
        players.remove(player.trim());
        setWhitelistedPlayersSet(players);
    }
    
    public static List<String> getPlayersToRenderList() {
        return new ArrayList<>(getPlayersToRenderSet());
    }
    
    public static List<String> getWhitelistedPlayersList() {
        return new ArrayList<>(getWhitelistedPlayersSet());
    }
    
    private static class ConfigData {
        boolean renderPlayers = true;
        boolean keybindsEnabled = true;
        String playersToRender = "";
        String whitelistedPlayers = "";
    }
}
