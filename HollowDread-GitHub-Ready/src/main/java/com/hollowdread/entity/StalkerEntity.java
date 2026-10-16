package com.hollowdread.entity;

import com.hollowdread.init.ModBlocks;
import com.hollowdread.init.ModEffects;
import com.hollowdread.network.ModNetwork;
import com.hollowdread.sanity.Sanity;
import com.hollowdread.scare.DreadSpawner;
import com.hollowdread.scare.Ghosts;
import com.hollowdread.scare.ScareType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * O Stalker: alto, sem rosto. Congela por completo enquanto algum jogador o
 * observa (estilo SCP-173 / Weeping Angel), corre quando ninguem olha, teleporta
 * para as costas da vitima e some depois de atacar. Velas de protecao o afastam.
 */
public class StalkerEntity extends Monster implements IDread {
    private boolean frozen;
    private int teleportCooldown = 120;
    private int fleeTicks;
    private int life;
    /** Apenas no cliente: alucinacao que o servidor desconhece. */
    private boolean ghost;

    public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32f));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Congelado = nenhuma IA, navegacao ou movimento roda. */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || frozen;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (fleeTicks > 0 && target != null) return;
        super.setTarget(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (ghost) Ghosts.clientTick(this);
            return;
        }
        life++;
        if (fleeTicks > 0) fleeTicks--;
        if (tickCount % 3 == 0) updateObserved();
        if (frozen) {
            getNavigation().stop();
            Vec3 m = getDeltaMovement();
            setDeltaMovement(0, m.y, 0);
        }
        if (tickCount % 20 == 0) serverSecond();
    }

    private void updateObserved() {
        boolean seen = false;
        for (Player p : level().players()) {
            if (Ghosts.isLookingAt(p, this, 48)) {
                seen = true;
                if (p instanceof ServerPlayer sp && sp.distanceToSqr(this) < 28 * 28) Sanity.add(sp, -0.1f);
            }
        }
        frozen = seen;
    }

    private void serverSecond() {
        Player nearest = level().getNearestPlayer(this, 128);
        if (nearest == null) {
            discard();
            return;
        }
        if (life > 7200 || (life > 600 && Sanity.get(nearest) > 85f)) {
            vanish();
            return;
        }
        if (nearWarding()) {
            fleeTicks = 100;
            setTarget(null);
            Vec3 away = position().subtract(nearest.position()).normalize().scale(12);
            getNavigation().moveTo(getX() + away.x, getY(), getZ() + away.z, 1.4D);
        }
        if (!frozen && fleeTicks <= 0 && --teleportCooldown <= 0) {
            teleportCooldown = 80 + random.nextInt(120);
            if (distanceTo(nearest) > 26 && nearest instanceof ServerPlayer sp) {
                Vec3 pos = DreadSpawner.findPos((ServerLevel) level(), sp, getType(), 14, 20, true, false);
                if (pos != null) {
                    moveTo(pos.x, pos.y, pos.z, getYRot(), 0f);
                    getNavigation().stop();
                }
            }
        }
        if (!frozen && random.nextInt(4) == 0 && distanceTo(nearest) < 20) {
            playSound(SoundEvents.WARDEN_NEARBY_CLOSE, 0.6f, 0.6f);
        }
    }

    private boolean nearWarding() {
        BlockPos c = blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-6, -3, -6), c.offset(6, 3, 6))) {
            if (level().getBlockState(p).is(ModBlocks.WARDING_CANDLE.get())) return true;
        }
        return false;
    }

    public void vanish() {
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 0.4f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), 24, 0.3, 0.8, 0.3, 0.01);
        }
        discard();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer sp) {
            Sanity.add(sp, -30f);
            Sanity.sync(sp);
            sp.addEffect(new MobEffectInstance(ModEffects.DREAD.get(), 400));
            sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60));
            ModNetwork.sendScare(sp, ScareType.JUMPSCARE, getX(), getY(), getZ(), 0);
            vanish();
        }
        return hit;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (packet.getData() == Ghosts.GHOST_MARKER) this.ghost = true;
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WARDEN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WARDEN_DEATH; }
    @Override public boolean removeWhenFarAway(double distance) { return true; }
}
