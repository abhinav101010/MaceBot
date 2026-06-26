package net.katch0420.macebot.ai;

import net.katch0420.macebot.playerbot.PlayerBot;
import net.katch0420.macebot.utils.RayTracer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager {
    public ServerPlayer player;
    public Inventory inventory;
    private final ServerLevel serverLevel;

    public ServerPlayer nearestPlayer;
    public Entity targetedEntity;

    private final int swordSlot;
    private final int enderPearlSlot;
    private final int shieldSlot;
    private final int breachMaceSlot;
    private final int densityMaceSlot;
    private final int elytraSlot;
    private final int windChargeSlot;
    private final int foodSlot;
    private final int axeSlot;

    private int tickTimer;
    private int executeStep = 1;
    public double distanceToNearbyPlayer;

    public boolean nearestPlayerIsNull;
    public boolean targetedEntityIsNull;
    public boolean attackMarked;
    public boolean elytraEquipped = false;


    private static final Map<Integer, Runnable> regularLaunch = new HashMap<>();
    private static final Map<Integer, Runnable> elytraLaunch = new HashMap<>();

    public ActionManager(ServerPlayer player){
        this.player = player;
        inventory = player.getInventory();
        serverLevel = (ServerLevel) player.level();

        swordSlot = 0;
        enderPearlSlot = 1;
        shieldSlot = 2;
        breachMaceSlot = 3;
        densityMaceSlot = 4;
        elytraSlot = 5;
        windChargeSlot = 6;
        foodSlot = 7;
        axeSlot = 8;
        update();
        loadHashMaps();
    }
    //Core
    public void update(){
        //CoolDowns & Timers Based on Ticks
        if(tickTimer > 0) tickTimer--;

        //Player Entity Updates
        nearestPlayer = getNearestPlayer();
        nearestPlayerIsNull = nearestPlayer == null;
        distanceToNearbyPlayer = nearestPlayerIsNull ? 100 : player.distanceTo(nearestPlayer);

        targetedEntity = RayTracer.rayTraceEntity(player);
        targetedEntityIsNull = targetedEntity == null;
    }
    //Packed Actions
    public Status eat(){
        if(executeStep == 1){
            resetValues();
            unequipElytra();
            inventory.setSelectedSlot(foodSlot);
            boolean bl = use();
            if(!bl){
                System.out.println("eat.fail.use");
                resetValues();
                return Status.FAIL;
            }
            tickTimer = player.getUseItem().getUseDuration(player);
            executeStep++;
            System.out.println(executeStep);
            return Status.PASS;
        }
        if(tickTimer <= 0 && !player.isUsingItem()){
            System.out.println("eat.success");
            resetValues();
            return Status.SUCCESS;
        }
        System.out.println("eat.pass");
        return Status.PASS;
    }
    public Status elytraLaunch(){
        if(commonCancelers()){
            resetValues();
            unequipElytra();
            return Status.FAIL;
        }
        if(executeStep == 1){
            inventory.setSelectedSlot(elytraSlot);
            setBotForwardSpeed(1);
            setSprint(true);
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 2){
            swapElytra();
            doJump();
            look(player.getYRot(), 40F);
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 3){
            if(!player.onGround() && Math.abs(player.getDeltaMovement().y()) < 0.05){
                player.startFallFlying();
                inventory.setSelectedSlot(windChargeSlot);
                executeStep++;
                return Status.PASS;
            } else if (player.onGround()){
                resetValues();
                unequipElytra();
                return Status.FAIL;
            }
        }
        if(executeStep == 4){
            if(RayTracer.getDistanceToGround(player) < 0.25){
                look(Direction.DOWN);
                use();
                executeStep++;
                delayJumpInMillis(20);
                return Status.PASS;
            }
        }
        if(executeStep == 5){
            look(player.getYRot(), -30F);
            doJump();
            executeStep++;
            return Status.PASS;
        }
        if(executeStep == 6){
            player.startFallFlying();
            inventory.setSelectedSlot(elytraSlot);
            resetValues();
            return Status.SUCCESS;
        }
        return Status.PASS;
    }
    public Status elytraAttack(){
        if(commonCancelers()){
            resetValues();
            unequipElytra();
            return Status.FAIL;
        }
        if(player.onGround()){
            if(executeStep > 5){
                resetValues();
                unequipElytra();
                return Status.FAIL;
            } else {
                executeStep++;
            }
        }
        if(player.getDeltaMovement().y() > 0){
            return Status.PASS;
        }
        if(targetedEntityIsNull){
            lookAt(nearestPlayer.getEyePosition(0.5F));
        } else {
            unequipElytra();
            return Status.SUCCESS;
        }
        return Status.PASS;
    }
    public Status regularLaunch(boolean sprint){
        if(commonCancelers()){
            resetValues();
            return Status.FAIL;
        }
        if(executeStep == 1){
            setBotForwardSpeed(sprint ? 1 : 0);
            setSprint(sprint);
        }
        if(executeStep <= regularLaunch.size()){
            if(executeStep == 2) look(player.getYRot(),sprint ? 75.0F : 89.9F);
            regularLaunch.get(executeStep).run();
            if(executeStep == regularLaunch.size()){
                resetValues();
                return Status.SUCCESS;
            } else {
                executeStep++;
            }
        }
        return Status.PASS;
    }
    public Status maceHit(boolean density){
        //checks whether cancel or not
        if(commonCancelers() || player.onGround()){
            resetValues();
            return Status.FAIL;
        }
        lookAt(nearestPlayer.getEyePosition(0.5F));
        if(player.fallDistance > 1){
            inventory.setSelectedSlot(density? densityMaceSlot : breachMaceSlot);
            attack();
        }
        if(!attackMarked && !player.onGround()){
            attackMarked = true;
        }
        return Status.PASS;
    }
    public Status critHit(int slot){
        if(commonCancelers()){
            resetValues();
            return Status.FAIL;
        }
        lookAt(nearestPlayer.getEyePosition(0.5F));
        if(executeStep == 1){
            inventory.setSelectedSlot(slot);
            setBotForwardSpeed(1);
            setSprint(true);
            executeStep++;
            return Status.PASS;
        }
        if(distanceToNearbyPlayer < 6 && executeStep == 2){
            doJump();
            executeStep++;
        }
        if(player.onGround() && executeStep > 2){
            resetValues();
            return Status.FAIL;
        }
        if(player.fallDistance > 0 && player.getAttackStrengthScale(0.5F) > 0.9 && !targetedEntityIsNull){
            attackMarked = true;
        }
        if(attackMarked){
            setSprint(false);
            attack();
            if(!attackMarked){
                resetValues();
                return Status.SUCCESS;
            } else {
                setBotForwardSpeed(1);
                setSprint(true);
            }
        }
        return Status.PASS;
    }

    //Utils
    private ServerPlayer getNearestPlayer(){
        List<ServerPlayer> a = serverLevel.getPlayers(p -> true);
        ServerPlayer b = null;
        float c = 100;
        for(ServerPlayer d : a){
            if(player.distanceTo(d) < c){
                if(d != player) {
                    c = player.distanceTo(d);
                    b = d;
                }
            }
        }
        return b;
    }
    private boolean commonCancelers(){
        return nearestPlayerIsNull;
    }
    public void resetValues(){
        executeStep = 1;
        tickTimer = 0;
    }
    public void unequipElytra(){
        if(elytraEquipped){
            inventory.setSelectedSlot(elytraSlot);
            swapElytra();
        }
    }
    public void resetAllMovements(){
        setSprint(false);
        setSneak(false);
        setBotForwardSpeed(0);
        setBotSidewaysSpeed(0);
    }

    // Movement helpers (delegate to PlayerBot fields)
    public void setBotForwardSpeed(float speed) {
        if (player instanceof PlayerBot bot) {
            bot.botForwardSpeed = speed;
        }
    }
    public void setBotSidewaysSpeed(float speed) {
        if (player instanceof PlayerBot bot) {
            bot.botSidewaysSpeed = speed;
        }
    }

    private void loadHashMaps(){
        regularLaunch.put(1, ()-> lookAt(nearestPlayer.getEyePosition(0.5F)));
        regularLaunch.put(2, ()-> inventory.setSelectedSlot(windChargeSlot));
        regularLaunch.put(3, ()-> {use();delayJumpInMillis(20);});
        regularLaunch.put(4, ()-> lookAt(nearestPlayer.getEyePosition(0.5F)));

        elytraLaunch.put(1, ()-> {inventory.setSelectedSlot(elytraSlot); setBotForwardSpeed(1); setSprint(true);});
        elytraLaunch.put(2, ()-> {swapElytra();doJump();});
        elytraLaunch.put(3, ()-> look(player.getYRot(), 40.0F));
        elytraLaunch.put(4, ()-> {player.startFallFlying();tickTimer = 10;});
        elytraLaunch.put(5, ()-> {look(player.getYRot(), 89.9f);inventory.setSelectedSlot(windChargeSlot);});
        elytraLaunch.put(6, ()-> {use();delayJumpInMillis(20);});
        elytraLaunch.put(7, ()-> {look(player.getYRot(), -35.0F);player.startFallFlying();});
    }
    public enum Status{
        SUCCESS,
        FAIL,
        PASS
    }

    //All Individual Actions (executes in 1 tick mostly)
    public void swapElytra(){
        if(inventory.getSelectedSlot() == elytraSlot) {
            ItemStack a = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            ItemStack b = player.getMainHandItem();
            elytraEquipped = !a.is(Items.ELYTRA);
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, b);
            player.setItemInHand(InteractionHand.MAIN_HAND, a);
        }
    }
    public boolean use(){
        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getOffhandItem();

        if(!player.getCooldowns().isOnCooldown(mainHandStack)) {
            mainHandStack.use(player.level(), player, InteractionHand.MAIN_HAND);
            return true;
        }
        if(!player.getCooldowns().isOnCooldown(offHandStack)) {
            offHandStack.use(player.level(), player, InteractionHand.OFF_HAND);
            return true;
        }
        return false;
    }
    public void attack(){
        if(!targetedEntityIsNull && attackMarked) {
            player.attack(targetedEntity);
            player.swing(InteractionHand.MAIN_HAND);
            attackMarked = false;
        }
    }
    public void doJump(){
        if (player.onGround()) {
            player.jumpFromGround();
        }
    }
    public void setSprint(boolean bl){
        player.setSprinting(bl);
        if(player.isShiftKeyDown() && player.isSprinting()){
            player.setShiftKeyDown(false);
        }
    }
    public void setSneak(boolean bl){
        player.setShiftKeyDown(bl);
        if(player.isSprinting() && player.isShiftKeyDown()){
            player.setSprinting(false);
        }
    }
    public void lookAt(Vec3 position) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, position);
    }
    public void look(float yaw, float pitch){
        player.setYRot(yaw % 360);
        player.setXRot(Math.clamp(pitch, -90, 90));
    }
    public void look(Direction direction) {
        switch (direction)
        {
            case NORTH -> look(180, 0);
            case SOUTH -> look(0, 0);
            case EAST  -> look(-90, 0);
            case WEST  -> look(90, 0);
            case UP    -> look(player.getYRot(), -90);
            case DOWN  -> look(player.getYRot(), 90);
        }
    }
    public void delayJumpInMillis(int millis){
        if(millis == 0){
            millis = 20;
        }
        int finalMillis = millis;
        new Thread(() -> {
            try {
                Thread.sleep(finalMillis);
                doJump();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
