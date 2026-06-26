package net.katch0420.macebot.mixin;

import net.katch0420.macebot.player.PlayerSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "consume", at = @At("HEAD"), cancellable = true)
    private void preventDecrementUnlessCreative(int amount, LivingEntity entity, CallbackInfo ci) {
        if (PlayerSettings.autoRefill && entity instanceof ServerPlayer) {
            ci.cancel();
        }
    }
    @Inject(method = "shrink", at = @At("HEAD"), cancellable = true)
    private void preventDecrement(int amount, CallbackInfo ci) {
        if (PlayerSettings.autoRefill) {
            ci.cancel();
        }
    }
}
