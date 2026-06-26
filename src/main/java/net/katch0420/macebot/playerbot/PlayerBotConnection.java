package net.katch0420.macebot.playerbot;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class PlayerBotConnection extends Connection {

    public PlayerBotConnection(PacketFlow flow) {
        super(flow);
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerBotConnection.class);
    EmbeddedChannel embeddedChannel = new EmbeddedChannel();
    Field channelField;

    {
        try {
            channelField = Connection.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            channelField.set(this, embeddedChannel);
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    @Override
    public void handleDisconnection() {
    }
}
