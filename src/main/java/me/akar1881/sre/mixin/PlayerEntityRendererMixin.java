package me.akar1881.sre.mixin;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class PlayerEntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(T entity, Frustum frustum, double x, double y, double z, 
                                 CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) {
            return;
        }
        
        if (ConfigHandler.renderPlayers) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity localPlayer = client.player;
        
        if (localPlayer == null) {
            return;
        }
        
        if (!Utils.shouldRenderPlayer(player, localPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
