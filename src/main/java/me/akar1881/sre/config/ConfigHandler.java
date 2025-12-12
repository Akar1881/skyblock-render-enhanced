package me.akar1881.sre.config;

import com.google.gson.*;
import me.akar1881.sre.SkyblockRenderEnhanced;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigHandler {
    
    public enum SlayerMode {
        OFF,
        HIDE,
        GLOW;
        
        public String getDisplayName() {
            return switch (this) {
                case OFF -> "Off (Show All)";
                case HIDE -> "Hide Others";
                case GLOW -> "Highlight Mine";
            };
        }
    }
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ConfigData.class, new ConfigDataDeserializer())
        .create();
    private static final Gson GSON_SIMPLE = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sre.json");
    
    private static ConfigData config = new ConfigData();
    
    public static boolean renderPlayers = true;
    public static boolean keybindsEnabled = true;
    public static boolean renderPartyMembers = true;
    public static List<String> playersToRender = new ArrayList<>();
    
    public static SlayerMode slayerMode = SlayerMode.OFF;
    public static boolean linkSlayerToPlayer = false;
    public static int glowColor = 0x00FF00;
    
    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, ConfigData.class);
                if (config == null) {
                    config = new ConfigData();
                }
                syncToFields();
            }
            save();
        } catch (Exception e) {
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
            String json = GSON_SIMPLE.toJson(config);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            SkyblockRenderEnhanced.LOGGER.error("Failed to save config", e);
        }
    }
    
    private static void syncToFields() {
        renderPlayers = config.renderPlayers;
        keybindsEnabled = config.keybindsEnabled;
        renderPartyMembers = config.renderPartyMembers;
        playersToRender = new ArrayList<>(config.playersToRender);
        slayerMode = config.slayerMode;
        linkSlayerToPlayer = config.linkSlayerToPlayer;
        glowColor = config.glowColor;
    }
    
    private static void syncFromFields() {
        config.renderPlayers = renderPlayers;
        config.keybindsEnabled = keybindsEnabled;
        config.renderPartyMembers = renderPartyMembers;
        config.playersToRender = new ArrayList<>(playersToRender);
        config.slayerMode = slayerMode;
        config.linkSlayerToPlayer = linkSlayerToPlayer;
        config.glowColor = glowColor;
    }
    
    public static void syncAndSave() {
        syncFromFields();
        save();
    }
    
    public static Set<String> getPlayersToRenderSet() {
        return new LinkedHashSet<>(playersToRender);
    }
    
    public static void addPlayerToRender(String player) {
        String trimmed = player.trim();
        if (!trimmed.isEmpty() && !playersToRender.contains(trimmed)) {
            playersToRender.add(trimmed);
            syncAndSave();
        }
    }
    
    public static void removePlayerToRender(String player) {
        playersToRender.remove(player.trim());
        syncAndSave();
    }
    
    public static List<String> getPlayersToRenderList() {
        return new ArrayList<>(playersToRender);
    }
    
    public static class ConfigData {
        public boolean renderPlayers = true;
        public boolean keybindsEnabled = true;
        public boolean renderPartyMembers = true;
        public List<String> playersToRender = new ArrayList<>();
        public SlayerMode slayerMode = SlayerMode.OFF;
        public boolean linkSlayerToPlayer = false;
        public int glowColor = 0x00FF00;
    }
    
    private static class ConfigDataDeserializer implements JsonDeserializer<ConfigData> {
        @Override
        public ConfigData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ConfigData data = new ConfigData();
            
            if (!json.isJsonObject()) {
                return data;
            }
            
            JsonObject obj = json.getAsJsonObject();
            
            if (obj.has("renderPlayers")) {
                data.renderPlayers = obj.get("renderPlayers").getAsBoolean();
            }
            if (obj.has("keybindsEnabled")) {
                data.keybindsEnabled = obj.get("keybindsEnabled").getAsBoolean();
            }
            if (obj.has("renderPartyMembers")) {
                data.renderPartyMembers = obj.get("renderPartyMembers").getAsBoolean();
            }
            if (obj.has("slayerMode")) {
                try {
                    data.slayerMode = SlayerMode.valueOf(obj.get("slayerMode").getAsString());
                } catch (Exception e) {
                    data.slayerMode = SlayerMode.OFF;
                }
            }
            if (obj.has("linkSlayerToPlayer")) {
                data.linkSlayerToPlayer = obj.get("linkSlayerToPlayer").getAsBoolean();
            }
            if (obj.has("glowColor")) {
                data.glowColor = obj.get("glowColor").getAsInt();
            }
            
            Set<String> allPlayers = new LinkedHashSet<>();
            
            if (obj.has("playersToRender")) {
                JsonElement playersElement = obj.get("playersToRender");
                if (playersElement.isJsonArray()) {
                    for (JsonElement e : playersElement.getAsJsonArray()) {
                        String trimmed = e.getAsString().trim();
                        if (!trimmed.isEmpty()) {
                            allPlayers.add(trimmed);
                        }
                    }
                }
            }
            
            data.playersToRender = new ArrayList<>(allPlayers);
            
            return data;
        }
    }
}
