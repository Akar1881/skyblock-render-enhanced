package me.akar1881.sre.keybinds;

import me.akar1881.sre.SkyblockRenderEnhanced;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static KeyBinding toggleSre;
    public static KeyBinding openGui;
    
    private static final KeyBinding.Category SRE_CATEGORY = KeyBinding.Category.create(Identifier.of("sre", "category"));
    
    public static void register() {
        toggleSre = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sre.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            SRE_CATEGORY
        ));
        
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sre.opengui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            SRE_CATEGORY
        ));
        
        SkyblockRenderEnhanced.LOGGER.info("Keybinds registered");
    }
}
