package net.katch0420.macebot.mixin;

import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.playerbot.PlayerBotNetHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManager_playerBotMixin
{
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;initMenu(Lnet/minecraft/world/inventory/AbstractContainerMenu;)V", shift = At.Shift.BEFORE))
    private void fixStartingPos(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci)
    {
        if (player instanceof PlayerBot playerBot)
        {
            playerBot.fixStartingPos.run();
        }
    }

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
            )
    )
    private ServerGamePacketListenerImpl replaceNetworkHandler(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie)
    {
        if (player instanceof PlayerBot playerBot)
        {
            return new PlayerBotNetHandler(this.server, connection, playerBot, cookie);
        }
        else
        {
            return new ServerGamePacketListenerImpl(this.server, connection, player, cookie);
        }
    }
}
