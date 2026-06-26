package net.katch0420.macebot.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import static net.katch0420.macebot.MaceBot.LOGGER;

public class SkinManager {

    private static final File SKIN_DIR = new File(System.getProperty("user.dir"), "macebot/skins/");

    public static void init() {
        if (!SKIN_DIR.exists()) {
            if(!SKIN_DIR.mkdir()){
                LOGGER.info("Unexpected Error occurred in creating: {}",SKIN_DIR.getAbsolutePath());
            }
            LOGGER.info("Created skin directory: {}",SKIN_DIR.getAbsolutePath());
        }
    }

    public static int applySkin(ServerPlayer player, String fileName) {
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return applySkinFromUrl(player, fileName);
        }

        File skinFile = new File(SKIN_DIR, fileName);
        if (!skinFile.exists()) {
            LOGGER.info("Skin file not found: {}",skinFile.getAbsolutePath());
            return 0;
        }

        try {
            byte[] pngBytes = Files.readAllBytes(skinFile.toPath());
            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"data:image/png;base64," +
                    Base64.getEncoder().encodeToString(pngBytes) + "\" } } }";
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            GameProfile profile = player.getGameProfile();
            profile.properties().removeAll("textures");
            profile.properties().put("textures", new Property("textures", value, null));

            ((ServerLevel) player.level()).getServer().getPlayerList().broadcastAll(
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player))
            );

            LOGGER.info("Applied skin from file: {}" ,fileName);
            return 1;

        } catch (IOException e) {
            LOGGER.info("Couldn't apply skin to {}: {}",player.getName().getString(),e);
            return 2;
        }
    }

    public static int applySkinFromUrl(ServerPlayer player, String skinUrl) {
        if (!skinUrl.startsWith("http://") && !skinUrl.startsWith("https://")) {
            return 0;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(3))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(skinUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            if (statusCode < 200 || statusCode >= 400) {
                return 0;
            }

            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"" + skinUrl + "\" } } }";
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            GameProfile profile = player.getGameProfile();
            profile.properties().removeAll("textures");
            profile.properties().put("textures", new Property("textures", value, null));

            ((ServerLevel) player.level()).getServer().getPlayerList().broadcastAll(
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player))
            );
            LOGGER.info("Applied skin from url:{}", skinUrl);
            return 1;

        } catch (Exception e) {
            LOGGER.info("Couldn't apply skin to {}: {}", player.getName().getString(), e);
            return 2;
        }
    }

    public static void applyDefaultSkin(ServerPlayer player) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("macebot", "textures/bot/skins/default-skin.png");
            String json = "{ \"textures\": { \"SKIN\": { \"url\": \"" + id.toString() + "\" } } }";
            String value = Base64.getEncoder().encodeToString(json.getBytes());

            GameProfile profile = player.getGameProfile();
            profile.properties().removeAll("textures");
            profile.properties().put("textures", new Property("textures", value, null));

            ((ServerLevel) player.level()).getServer().getPlayerList().broadcastAll(
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player))
            );
            LOGGER.info("Applied skin from resources to {}", player.getName().getString());

        } catch (Exception e) {
            LOGGER.info("Couldn't apply skin to {}: {}", player.getName().getString(), e);
        }
    }
}
