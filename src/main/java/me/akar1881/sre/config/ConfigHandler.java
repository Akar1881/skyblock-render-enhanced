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
    
    public enum CounterMode {
        AUTO,
        MANUAL;
        
        public String getDisplayName() {
            return switch (this) {
                case AUTO -> "Auto (with manual adjust)";
                case MANUAL -> "Manual Only";
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
    
    public static boolean counterEnabled = false;
    public static boolean counterWidgetEnabled = true;
    public static float counterWidgetX = 0.01f;
    public static float counterWidgetY = 0.3f;
    public static CounterMode counterMode = CounterMode.AUTO;
    
    public static boolean enchantmentHelperEnabled = true;
    public static boolean noHurtCam = false;
    public static boolean dungeonJoinHelperEnabled = true;
    
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
        counterEnabled = config.counterEnabled;
        counterWidgetEnabled = config.counterWidgetEnabled;
        counterWidgetX = config.counterWidgetX;
        counterWidgetY = config.counterWidgetY;
        counterMode = config.counterMode;
        enchantmentHelperEnabled = config.enchantmentHelperEnabled;
        noHurtCam = config.noHurtCam;
        dungeonJoinHelperEnabled = config.dungeonJoinHelperEnabled;
    }
    
    private static void syncFromFields() {
        config.renderPlayers = renderPlayers;
        config.keybindsEnabled = keybindsEnabled;
        config.renderPartyMembers = renderPartyMembers;
        config.playersToRender = new ArrayList<>(playersToRender);
        config.slayerMode = slayerMode;
        config.linkSlayerToPlayer = linkSlayerToPlayer;
        config.glowColor = glowColor;
        config.counterEnabled = counterEnabled;
        config.counterWidgetEnabled = counterWidgetEnabled;
        config.counterWidgetX = counterWidgetX;
        config.counterWidgetY = counterWidgetY;
        config.counterMode = counterMode;
        config.enchantmentHelperEnabled = enchantmentHelperEnabled;
        config.noHurtCam = noHurtCam;
        config.dungeonJoinHelperEnabled = dungeonJoinHelperEnabled;
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
        public boolean counterEnabled = false;
        public boolean counterWidgetEnabled = true;
        public float counterWidgetX = 0.01f;
        public float counterWidgetY = 0.3f;
        public CounterMode counterMode = CounterMode.AUTO;
        public boolean enchantmentHelperEnabled = true;
        public boolean noHurtCam = false;
        public boolean dungeonJoinHelperEnabled = true;
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
            if (obj.has("counterEnabled")) {
                data.counterEnabled = obj.get("counterEnabled").getAsBoolean();
            }
            if (obj.has("counterWidgetEnabled")) {
                data.counterWidgetEnabled = obj.get("counterWidgetEnabled").getAsBoolean();
            }
            if (obj.has("counterWidgetX")) {
                data.counterWidgetX = obj.get("counterWidgetX").getAsFloat();
            }
            if (obj.has("counterWidgetY")) {
                data.counterWidgetY = obj.get("counterWidgetY").getAsFloat();
            }
            if (obj.has("counterMode")) {
                try {
                    data.counterMode = CounterMode.valueOf(obj.get("counterMode").getAsString());
                } catch (Exception e) {
                    data.counterMode = CounterMode.AUTO;
                }
            }
            if (obj.has("enchantmentHelperEnabled")) {
                data.enchantmentHelperEnabled = obj.get("enchantmentHelperEnabled").getAsBoolean();
            }
            if (obj.has("noHurtCam")) {
                data.noHurtCam = obj.get("noHurtCam").getAsBoolean();
            }
            if (obj.has("dungeonJoinHelperEnabled")) {
                data.dungeonJoinHelperEnabled = obj.get("dungeonJoinHelperEnabled").getAsBoolean();
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
