package net.katch0420.macebot.playerbot;

import com.mojang.authlib.GameProfile;
import net.katch0420.macebot.ai.ActionManager;
import net.katch0420.macebot.ai.Controller;
import net.katch0420.macebot.player.Kits;
import net.katch0420.macebot.utils.SkinManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerBot extends ServerPlayer {

    public Runnable fixStartingPos = () -> {
    };

    public static boolean botOnline;
    public static Controller controller;
    public static ActionManager actionManager;

    // Movement control fields (replace old forwardSpeed/sidewaysSpeed)
    public float botForwardSpeed = 0f;
    public float botSidewaysSpeed = 0f;

    public static void createBot(MinecraftServer server, ServerLevel world, BlockPos blockPos, CommandSourceStack source) {

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "MaceBot");

        ClientInformation clientOptions = ClientInformation.createDefault();

        PlayerBot playerBot = new PlayerBot(server, world, gameProfile, clientOptions);
        playerBot.fixStartingPos = () -> playerBot.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        server.getPlayerList().placeNewPlayer(new PlayerBotConnection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), playerBot, new CommonListenerCookie(gameProfile, 0, clientOptions, false));
        playerBot.teleportTo(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), Set.of(), 0.0f, 0.0f, true);

        playerBot.setHealth(20.0F);
        playerBot.unsetRemoved();
        AttributeInstance stepHeight = playerBot.getAttributes().getInstance(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.setBaseValue(0.6f);
        }
        playerBot.setGameMode(GameType.SURVIVAL);
        playerBot.setOnGround(true);
        playerBot.getEntityData().set(net.minecraft.world.entity.Avatar.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);
        playerBot.getAbilities().flying = false;
        actionManager = new ActionManager(playerBot);
        controller = new Controller(actionManager);
        Kits.giveKit(Objects.requireNonNull(((ServerLevel) playerBot.level()).getServer()).createCommandSourceStack(), Kits.Kit.MACE_NETHERITE, false, "MaceBot");
        SkinManager.applyDefaultSkin(playerBot);
    }

    public PlayerBot(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
        super(server, world, profile, clientOptions);
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        super.checkFallDamage(heightDifference, onGround, state, landedPosition);
    }

    @Override
    public void tick() {
        if (((ServerLevel) level()).getServer().getTickCount() % 10 == 0) {
            this.reapplyPosition();
        }
        controller.tick();
        super.tick();
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);
        this.dead = false;
        this.setHealth(20f);
    }

    @Override
    public String getIpAddress() {
        return "127.0.0.1";
    }

    @Override
    public void travel(Vec3 movementInput) {
        float forward = botForwardSpeed;
        float sideways = botSidewaysSpeed;

        if (this.isShiftKeyDown()) {
            forward *= 0.3F;
            sideways *= 0.3F;
        } else if (this.isSprinting()) {
            forward *= 1.3F;
            sideways *= 9.8F;
        }
        if (this.isUsingItem()) {
            ItemStack stack = this.getUseItem();
            if (stack.getUseAnimation() == ItemUseAnimation.EAT || stack.getUseAnimation() == ItemUseAnimation.DRINK) {
                forward *= 0.2F;
                sideways *= 0.2F;
            } else if (stack.is(Items.SHIELD)) {
                forward *= 0.3F;
                sideways *= 0.3F;
            } else if (stack.is(Items.SPYGLASS)) {
                forward *= 0.1F;
                sideways *= 0.1F;
            }
        }

        // Compute movement Vec3 from yaw and forward/sideways speed
        float yaw = this.getYRot() * ((float) Math.PI / 180F);
        float sinYaw = (float) Math.sin(yaw);
        float cosYaw = (float) Math.cos(yaw);
        Vec3 computedMovement = new Vec3(
                sideways * cosYaw - forward * sinYaw,
                movementInput.y(),
                forward * cosYaw + sideways * sinYaw
        );

        super.travel(computedMovement);
    }
}
