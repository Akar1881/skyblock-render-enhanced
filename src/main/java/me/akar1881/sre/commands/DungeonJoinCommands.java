package me.akar1881.sre.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.akar1881.sre.config.ConfigHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;

public class DungeonJoinCommands {
    
    public static final Map<String, FloorInfo> FLOOR_OPTIONS = new LinkedHashMap<>();
    
    static {
        FLOOR_OPTIONS.put("f1", new FloorInfo("joindungeon catacombs 1", "Joining Floor 1"));
        FLOOR_OPTIONS.put("f2", new FloorInfo("joindungeon catacombs 2", "Joining Floor 2"));
        FLOOR_OPTIONS.put("f3", new FloorInfo("joindungeon catacombs 3", "Joining Floor 3"));
        FLOOR_OPTIONS.put("f4", new FloorInfo("joindungeon catacombs 4", "Joining Floor 4"));
        FLOOR_OPTIONS.put("f5", new FloorInfo("joindungeon catacombs 5", "Joining Floor 5"));
        FLOOR_OPTIONS.put("f6", new FloorInfo("joindungeon catacombs 6", "Joining Floor 6"));
        FLOOR_OPTIONS.put("f7", new FloorInfo("joindungeon catacombs 7", "Joining Floor 7"));
        FLOOR_OPTIONS.put("m1", new FloorInfo("joindungeon master_catacombs 1", "Joining Master Mode 1"));
        FLOOR_OPTIONS.put("m2", new FloorInfo("joindungeon master_catacombs 2", "Joining Master Mode 2"));
        FLOOR_OPTIONS.put("m3", new FloorInfo("joindungeon master_catacombs 3", "Joining Master Mode 3"));
        FLOOR_OPTIONS.put("m4", new FloorInfo("joindungeon master_catacombs 4", "Joining Master Mode 4"));
        FLOOR_OPTIONS.put("m5", new FloorInfo("joindungeon master_catacombs 5", "Joining Master Mode 5"));
        FLOOR_OPTIONS.put("m6", new FloorInfo("joindungeon master_catacombs 6", "Joining Master Mode 6"));
        FLOOR_OPTIONS.put("m7", new FloorInfo("joindungeon master_catacombs 7", "Joining Master Mode 7"));
    }
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Map.Entry<String, FloorInfo> entry : FLOOR_OPTIONS.entrySet()) {
            String command = entry.getKey();
            FloorInfo info = entry.getValue();
            
            dispatcher.register(ClientCommandManager.literal(command)
                .executes(context -> {
                    if (!ConfigHandler.dungeonJoinHelperEnabled) {
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal("Dungeon Join Helper is disabled. Enable it in settings first.")
                                .formatted(Formatting.RED)));
                        return 1;
                    }
                    
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        client.player.networkHandler.sendChatCommand(info.command);
                        context.getSource().sendFeedback(Text.literal("[SRE] ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal(info.message)
                                .formatted(Formatting.GREEN)));
                    }
                    return 1;
                }));
        }
    }
    
    public static class FloorInfo {
        public final String command;
        public final String message;
        
        public FloorInfo(String command, String message) {
            this.command = command;
            this.message = message;
        }
    }
}
