package net.katch0420.macebot.Commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.player.PlayerSettings;
import net.katch0420.macebot.utils.Colors;
import net.katch0420.macebot.utils.Messenger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.ChatFormatting;

import java.util.function.Supplier;

public class PlayerCommands {

    private static ArgumentBuilder<CommandSourceStack, ?> toggle(String name, Supplier<Boolean> toggleAction, String label) {
        return Commands.literal(name)
                .executes(context -> {
                    boolean bl = toggleAction.get();
                    Messenger.add(label + ": ", Colors.ComponentColor);
                    Messenger.add(bl ? "enabled" : "disabled", bl ? Colors.TrueColor : Colors.FalseColor);
                    try {
                        Messenger.send(context.getSource().getPlayerOrException(), true, true);
                    } catch (Exception ignored) {}
                    return 1;
                });
    }

    private static int giveKit(CommandContext<CommandSourceStack> context, Kits.Kit kit, boolean unbreakable, ChatFormatting color) {
        String s;
        try {
            s = context.getSource().isPlayer() ? context.getSource().getPlayerOrException().getName().getString() : "MaceBot";
        } catch (Exception e) {
            s = "MaceBot";
        }
        Kits.giveKit(context.getSource(), kit, unbreakable, s);
        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kit.displayName(), color);
        Messenger.add(" to ", Colors.BaseColor);
        Messenger.add(s, Colors.BaseColor);
        try {
            Messenger.send(context.getSource().getPlayerOrException(), true, true);
        } catch (Exception ignored) {}
        return 1;
    }

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) ->
                dispatcher.register(
                        Commands.literal("macebot")
                                .then(
                                        Commands.literal("player")
                                                .then(
                                                        Commands.literal("mace-kit")
                                                                .then(
                                                                        Commands.literal("netherite")
                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_NETHERITE, true, ChatFormatting.DARK_PURPLE))
                                                                                .then(
                                                                                        Commands.argument("unbreakable", BoolArgumentType.bool())
                                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_NETHERITE, BoolArgumentType.getBool(ctx, "unbreakable"), ChatFormatting.DARK_PURPLE))
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.literal("diamond")
                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_DIAMOND, true, ChatFormatting.AQUA))
                                                                                .then(
                                                                                        Commands.argument("unbreakable", BoolArgumentType.bool())
                                                                                                .executes(ctx -> giveKit(ctx, Kits.Kit.MACE_DIAMOND, BoolArgumentType.getBool(ctx, "unbreakable"), ChatFormatting.AQUA))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        Commands.literal("settings")
                                                                .then(toggle("auto-refill", PlayerSettings::toggleAutoRefill, "Auto Refill"))
                                                )
                                )
                )
        );
    }
}
