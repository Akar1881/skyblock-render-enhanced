package me.akar1881.sre;

import me.akar1881.sre.commands.SRECommand;
import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.events.SREEventHandler;
import me.akar1881.sre.keybinds.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyblockRenderEnhanced implements ClientModInitializer {
    public static final String MOD_ID = "sre";
    public static final String MOD_NAME = "Skyblock Render Enhanced";
    public static final String VERSION = "1.0.0";
    
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}...", MOD_NAME);
        
        ConfigHandler.load();
        
        Keybinds.register();
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SRECommand.register(dispatcher);
        });
        
        SREEventHandler.register();
        
        LOGGER.info("{} initialized successfully!", MOD_NAME);
    }
}
