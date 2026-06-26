package net.katch0420.macebot.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.katch0420.macebot.utils.SkinManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class SkinCommands {

    // Suggest online player names
    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (ctx, builder) -> {
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().name());
        }
        return builder.buildFuture();
    };

    // Suggest PNG files from macebot/skins/
    private static final SuggestionProvider<CommandSourceStack> FILE_SUGGESTIONS = (ctx, builder) -> {
        File skinDir = new File(System.getProperty("user.dir"), "macebot/skins/");
        if (skinDir.exists() && skinDir.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(skinDir.listFiles((dir, name) -> name.endsWith(".png"))))
                    .forEach(file -> builder.suggest(file.getName()));
        }
        return builder.buildFuture();
    };

    public static void Register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(
                Commands.literal("skin")
                        .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN)) // only ops
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests(PLAYER_SUGGESTIONS)
                                .then(
                                        Commands.literal("from-file")
                                                .then(
                                                        Commands.argument("file", StringArgumentType.string())
                                                                .suggests(FILE_SUGGESTIONS)
                                                                .executes(SkinCommands::file)
                                                )
                                )
                                .then(
                                        Commands.literal("from-url")
                                                .then(
                                                        Commands.argument("url",StringArgumentType.string())
                                                                .executes(SkinCommands::url)
                                                )
                                )
                        )
                )
        );
    }

    private static int file(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        String fileName = StringArgumentType.getString(ctx, "file");

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayer(playerName);

        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Player not found"));
            return 0;
        }

        int a = SkinManager.applySkin(target, fileName);
        switch (a){
            case 0 -> {
                ctx.getSource().sendFailure(Component.literal("Such File Don't Exist"));
            }
            case 1 -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "Applied skin " + fileName + " to " + playerName
                ), true);
            }
            case 2 -> {
                ctx.getSource().sendFailure(Component.literal("Unexpected Error occurred, Check console for further info."));
            }
        }

        return 1;
    }
    private static int url(CommandContext<CommandSourceStack> ctx){
        String playerName = StringArgumentType.getString(ctx, "player");
        String url = StringArgumentType.getString(ctx, "url");

        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayer(playerName);

        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Player not found"));
            return 0;
        }

        int a = SkinManager.applySkin(target, url);
        switch (a){
            case 0 -> {
                ctx.getSource().sendFailure(Component.literal("Invalid URL try again"));
            }
            case 1 -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "Applied skin from " + url + " to " + playerName
                ), true);
            }
            case 2 -> {
                ctx.getSource().sendFailure(Component.literal("Unexpected Error occurred, Check console for further info."));
            }
        }
        return 1;
    }
}
