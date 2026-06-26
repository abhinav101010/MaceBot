package net.katch0420.macebot.mixin;

import net.katch0420.macebot.playerbot.PlayerBot;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.world.entity.player.Player.class)
public abstract class PlayerEntity_playerBotMixin
{
    @Redirect(
        method = "attack",
        at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
                ordinal = 0
        )
    )
    private boolean velocityModifiedAndNotPlayerBot(Entity target){
        return target.hurtMarked && !(target instanceof PlayerBot);
    }
}
