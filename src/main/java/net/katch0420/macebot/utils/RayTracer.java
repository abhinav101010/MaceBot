package net.katch0420.macebot.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RayTracer {
    public static Entity rayTraceEntity(ServerPlayer source){
        float a = 0.5F;
        HitResult hr = rayTraceHitResult(source,a,false);
        Entity e;
        if(hr instanceof EntityHitResult){
            EntityHitResult ehr = (EntityHitResult) hr;
            e = ehr.getEntity();
        } else {
            e = null;
        }
        return e;
    }
    public static BlockHitResult rayTraceBlock(ServerPlayer source, boolean fluid){
        float a = 0.5F;
        HitResult hr = rayTraceHitResult(source, a, fluid);
        return hr instanceof BlockHitResult bhr ? bhr : null;
    }
    public static HitResult rayTraceHitResult(ServerPlayer source, float tickDelta, boolean fluid){
        double a = source.blockInteractionRange();
        double b = source.entityInteractionRange();
        Entity c = source.getCamera();

        double d = Math.max(a,b);
        double e = d * d;
        Vec3 vec3d = c.getEyePosition(tickDelta);
        HitResult hitResult = source.level().clip(new ClipContext(
                vec3d,
                vec3d.add(c.getLookAngle().scale(d)),
                ClipContext.Block.COLLIDER,
                fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                source
        ));
        double f = hitResult.getLocation().distanceToSqr(vec3d);
        if(hitResult.getType() != HitResult.Type.MISS){
            e = f;
            d = Math.sqrt(f);
        }

        Vec3 vec3d2 = c.getLookAngle();
        Vec3 vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        AABB box = c.getBoundingBox().expandTowards(vec3d2.scale(d)).inflate(1.0,1.0,1.0);
        HitResult entityHitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(c, (entity) -> !entity.isSpectator() && entity.isPickable(), d);
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3d) < f
                ? ensureTargetInRange(entityHitResult, vec3d, b)
                : ensureTargetInRange(hitResult, vec3d, a);
    }
    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3 cameraPos, double interactionRange) {
        Vec3 vec3d = hitResult.getLocation();
        if (!vec3d.closerThan(cameraPos, interactionRange)) {
            Vec3 vec3d2 = hitResult.getLocation();
            Direction direction = Direction.getNearest(new net.minecraft.core.Vec3i(
                    (int) Math.round(vec3d2.x - cameraPos.x),
                    (int) Math.round(vec3d2.y - cameraPos.y),
                    (int) Math.round(vec3d2.z - cameraPos.z)
            ), null);
            return BlockHitResult.miss(vec3d2, direction, BlockPos.containing(vec3d2));
        } else {
            return hitResult;
        }
    }
    public static double getDistanceToGround(Entity entity) {
        AABB box = entity.getBoundingBox();
        double bottomY = box.minY;
        Vec3 start = new Vec3(entity.getX(), bottomY, entity.getZ());
        Vec3 end = new Vec3(entity.getX(), bottomY - 16, entity.getZ());
        BlockHitResult hit = entity.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = entity.level().getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(entity.level(), pos);
            double groundY = pos.getY() + shape.max(Direction.Axis.Y);
            return bottomY - groundY;
        }
        return 16;
    }
}
