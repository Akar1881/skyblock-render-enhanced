package me.akar1881.sre.mixin;

import me.akar1881.sre.slayer.SlayerHandler;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class LivingEntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRenderSlayer(T entity, Frustum frustum, double x, double y, double z,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof MobEntity || entity instanceof ArmorStandEntity) {
            if (!SlayerHandler.shouldRenderSlayerEntity(entity)) {
                cir.setReturnValue(false);
            }
        }
    }
}
