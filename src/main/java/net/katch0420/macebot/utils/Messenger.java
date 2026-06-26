package net.katch0420.macebot.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

public class Messenger {

    private static final Map<Integer, MutableComponent> messageComponents = new HashMap<>();
    private static int index = 0;
    public static void add(String a, ChatFormatting b){
        MutableComponent text = Component.literal(a).withStyle(b);
        if(messageComponents.isEmpty()){
            index = 0;
        }
        messageComponents.put(index,text);
        index++;
    }
    public static boolean send(ServerPlayer player, boolean overlay, boolean sound){
        if(player != null) {
            MutableComponent msg = Component.literal("");
            for (int a = 0; a < messageComponents.size(); a++) {
                msg.append(messageComponents.get(a));
            }
            player.sendSystemMessage(msg, overlay);
            if (sound) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            messageComponents.clear();
            return true;
        } else {
            return false;
        }
    }
}
