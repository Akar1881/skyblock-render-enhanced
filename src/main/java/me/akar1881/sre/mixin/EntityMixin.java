package me.akar1881.sre.mixin;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.slayer.SlayerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        
        if (ConfigHandler.slayerMode != ConfigHandler.SlayerMode.GLOW) {
            return;
        }
        
        if (!(self instanceof MobEntity)) {
            return;
        }
        
        if (!SlayerHandler.isSlayerBoss(self)) {
            return;
        }
        
        if (SlayerHandler.shouldGlowSlayerEntity(self)) {
            cir.setReturnValue(true);
        }
    }
    
    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        
        if (ConfigHandler.slayerMode != ConfigHandler.SlayerMode.GLOW) {
            return;
        }
        
        if (!(self instanceof MobEntity)) {
            return;
        }
        
        if (!SlayerHandler.isSlayerBoss(self)) {
            return;
        }
        
        if (SlayerHandler.shouldGlowSlayerEntity(self)) {
            cir.setReturnValue(SlayerHandler.getGlowColor());
        }
    }
}
