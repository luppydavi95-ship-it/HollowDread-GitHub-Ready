package com.hollowdread.entity;

import com.hollowdread.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * O Rastejante: criatura que escala paredes, vive no escuro e foge da luz
 * (e da Lanterna da Razao). Ataca em "bate e corre".
 */
public class CrawlerEntity extends Monster implements IDread {
    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(CrawlerEntity.class, EntityDataSerializers.BOOLEAN);

    public CrawlerEntity(EntityType<? extends CrawlerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CLIMBING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FleeLightGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.35f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) entityData.set(CLIMBING, horizontalCollision);
    }

    @Override public boolean onClimbable() { return entityData.get(CLIMBING); }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motion) {
        if (!state.is(Blocks.COBWEB)) super.makeStuckInBlock(state, motion);
    }

    @Override public float getVoicePitch() { return 0.55f + (random.nextFloat() - random.nextFloat()) * 0.1f; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.SPIDER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SPIDER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SPIDER_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SPIDER_STEP, 0.15f, 0.7f);
    }

    static boolean holdsLantern(Player p) {
        return p.getMainHandItem().is(ModItems.LANTERN.get()) || p.getOffhandItem().is(ModItems.LANTERN.get());
    }

    /** Foge para o escuro quando ha luz forte ou um jogador com a Lanterna da Razao por perto. */
    static class FleeLightGoal extends Goal {
        private final CrawlerEntity mob;
        private Vec3 dest;

        FleeLightGoal(CrawlerEntity mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean threatened() {
            if (mob.level().getMaxLocalRawBrightness(mob.blockPosition()) > 8) return true;
            Player p = mob.level().getNearestPlayer(mob, 7);
            return p != null && holdsLantern(p);
        }

        @Override
        public boolean canUse() {
            if (!threatened()) return false;
            for (int i = 0; i < 10; i++) {
                Vec3 v = DefaultRandomPos.getPos(mob, 10, 4);
                if (v != null && mob.level().getMaxLocalRawBrightness(BlockPos.containing(v)) <= 5) {
                    dest = v;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            mob.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.5D);
        }

        @Override
        public boolean canContinueToUse() {
            return !mob.getNavigation().isDone() && threatened();
        }
    }
}
