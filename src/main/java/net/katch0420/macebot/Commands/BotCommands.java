package net.katch0420.macebot.Commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.ai.Controller;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.playerbot.PlayerBotSettings;
import net.katch0420.macebot.utils.Colors;
import net.katch0420.macebot.utils.Messenger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.function.Supplier;

public class BotCommands {

    private static ArgumentBuilder<CommandSourceStack, ?> toggle(String name, Supplier<Boolean> toggleAction, String label) {
        return Commands.literal(name)
                .executes(context -> {
                    boolean bl = toggleAction.get();
                    Messenger.add(label + ": ", Colors.BaseColor);
                    Messenger.add(bl ? "enabled" : "disabled", bl ? Colors.TrueColor : Colors.FalseColor);
                    Messenger.send(context.getSource().getPlayerOrException(), true, true);
                    return 1;
                });
    }

    private static int giveKit(CommandContext<CommandSourceStack> context, Kits.Kit kit, boolean unbreakable, ChatFormatting color) {
        Kits.giveKit(context.getSource(), kit, unbreakable, "MaceBot");

        Messenger.add("Gave ", Colors.BaseColor);
        if (unbreakable) Messenger.add("unbreakable ", Colors.BaseColor);
        Messenger.add(kit.displayName(), color);
        Messenger.add(" to ", Colors.BaseColor);
        Messenger.add("MaceBot", Colors.BaseColor);

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
                                        Commands.literal("bot")
                                                .then(
                                                        Commands.literal("spawn")
                                                                .executes(context -> {
                                                                    try {
                                                                        ServerPlayer botSource = context.getSource().getPlayerOrException();
                                                                        PlayerBot.createBot(context.getSource().getServer(), (ServerLevel) botSource.level(), botSource.blockPosition(), context.getSource());
                                                                    } catch (Exception ignored) {}
                                                                    Messenger.add("Spawning macebot", Colors.BaseColor);
                                                                    try {
                                                                        Messenger.send(context.getSource().getPlayerOrException(), true, true);
                                                                    } catch (Exception ignored) {}
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        Commands.literal("pause")
                                                                .executes(context -> {
                                                                    PlayerBot.controller.pauseTheBot();
                                                                    Messenger.add("Paused the bot", Colors.BaseColor);
                                                                    try {
                                                                        Messenger.send(context.getSource().getPlayerOrException(), true, true);
                                                                    } catch (Exception ignored) {}
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        Commands.literal("play")
                                                                .executes(context -> {
                                                                    Controller.difficulty = Controller.Difficulty.EASY;
                                                                    Messenger.add("Resumed the bot", Colors.BaseColor);
                                                                    try {
                                                                        Messenger.send(context.getSource().getPlayerOrException(), true, true);
                                                                    } catch (Exception ignored) {}
                                                                    return 1;
                                                                })
                                                )
                                                .then(
                                                        Commands.literal("settings")
                                                                .then(toggle("auto-refill", PlayerBotSettings::toggleAutoRefill, "Auto Refill"))
                                                                .then(toggle("elytra", PlayerBotSettings::toggleElytra, "Elytra Ability"))
                                                                .then(toggle("attack", PlayerBotSettings::toggleAttack, "Attack Ability"))
                                                                .then(toggle("ordinary-mace", PlayerBotSettings::toggleMace, "Ordinary Mace Attack"))
                                                                .then(toggle("crits", PlayerBotSettings::toggleCrits, "Crit Hits"))
                                                )
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
                                )
                )
        );
    }
}
