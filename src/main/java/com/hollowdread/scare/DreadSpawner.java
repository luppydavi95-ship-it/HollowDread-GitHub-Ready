package com.hollowdread.scare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class DreadSpawner {
    private DreadSpawner() {}

    /**
     * Procura uma posicao valida ao redor do jogador.
     * @param behind  tenta ficar atras de onde o jogador olha
     * @param needLos exige linha de visao livre ate o jogador
     */
    @Nullable
    public static Vec3 findPos(ServerLevel level, ServerPlayer sp, EntityType<?> type,
                               double min, double max, boolean behind, boolean needLos) {
        RandomSource r = level.random;
        Vec3 look = sp.getLookAngle();
        double baseAngle = Math.atan2(-look.z, -look.x);
        int[] dys = {0, 1, -1, 2, -2, 3, -3, 4, -4};
        for (int i = 0; i < 40; i++) {
            double ang = behind ? baseAngle + (r.nextDouble() - 0.5) * 1.4 : r.nextDouble() * Math.PI * 2;
            double dist = min + r.nextDouble() * (max - min);
            double x = sp.getX() + Math.cos(ang) * dist;
            double z = sp.getZ() + Math.sin(ang) * dist;
            for (int dy : dys) {
                BlockPos bp = BlockPos.containing(x, sp.getY() + dy, z);
                if (!level.hasChunkAt(bp)) continue;
                BlockPos below = bp.below();
                if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
                if (!level.noCollision(type.getAABB(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5))) continue;
                Vec3 pos = new Vec3(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
                if (needLos) {
                    Vec3 eye = sp.getEyePosition();
                    Vec3 target = pos.add(0, type.getHeight() * 0.8, 0);
                    ClipContext ctx = new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sp);
                    if (level.clip(ctx).getType() != HitResult.Type.MISS) continue;
                }
                return pos;
            }
        }
        return null;
    }

    @Nullable
    public static <T extends Entity> T spawn(ServerLevel level, EntityType<T> type, Vec3 pos, ServerPlayer facing) {
        T e = type.create(level);
        if (e == null) return null;
        float yaw = (float) (Mth.atan2(facing.getZ() - pos.z, facing.getX() - pos.x) * (180.0 / Math.PI)) - 90f;
        e.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
        if (e instanceof Mob m) {
            m.setYHeadRot(yaw);
            m.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)), MobSpawnType.EVENT, null, null);
        }
        level.addFreshEntity(e);
        return e;
    }

    public static int countNear(ServerLevel level, Entity center, Class<? extends Entity> cls, double radius) {
        return level.getEntitiesOfClass(cls, center.getBoundingBox().inflate(radius)).size();
    }
}
