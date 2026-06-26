package net.katch0420.macebot.playerbot;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class PlayerBotNetHandler extends ServerGamePacketListenerImpl {
    public PlayerBotNetHandler(MinecraftServer server, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie cookie) {
        super(server, connection, serverPlayer, cookie);
    }

    @Override
    public void send(Packet<?> packet) {
        super.send(packet);
    }

    @Override
    public void disconnect(Component reason) {
        super.disconnect(reason);
        if (reason.getContents() instanceof TranslatableContents text && (text.getKey().equals("multiplayer.disconnect.idling") || text.getKey().equals("multiplayer.disconnect.duplicate_login")))
        {
            player.kill((ServerLevel) player.level());
        }
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
        super.teleport(x, y, z, yaw, pitch);
    }
}
