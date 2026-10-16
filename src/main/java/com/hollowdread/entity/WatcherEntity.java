package com.hollowdread.entity;

import com.hollowdread.network.ModNetwork;
import com.hollowdread.sanity.Sanity;
import com.hollowdread.scare.Ghosts;
import com.hollowdread.scare.ScareType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * O Observador: uma silhueta pale parada ao longe, sempre virada para voce.
 * Drena sanidade enquanto e visto, desaparece se for encarado por tempo demais
 * ou se voce se aproximar - e rasteja um pouco mais perto quando voce desvia o olhar.
 */
public class WatcherEntity extends PathfinderMob implements IDread {
    private int observedTicks;
    private int life;
    private boolean ghost;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override protected boolean isImmobile() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return true; }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (!level().isClientSide && source.getEntity() instanceof Player) vanish();
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (ghost) Ghosts.clientTick(this);
            return;
        }
        life++;
        Player nearest = level().getNearestPlayer(this, 80);
        if (nearest == null) {
            if (life > 100) discard();
            return;
        }
        Ghosts.face(this, nearest);
        if (life > 2400) {
            vanish();
            return;
        }
        if (nearest.distanceTo(this) < 12f) {
            if (nearest instanceof ServerPlayer sp) ModNetwork.sendScare(sp, ScareType.HEARTBEAT, getX(), getY(), getZ(), 0);
            vanish();
            return;
        }
        if (tickCount % 5 != 0) return;

        boolean seen = false;
        for (Player p : level().players()) {
            if (Ghosts.isLookingAt(p, this, 70)) {
                seen = true;
                if (p instanceof ServerPlayer sp) Sanity.add(sp, -0.12f);
            }
        }
        if (seen) {
            observedTicks += 5;
            if (observedTicks > 80) vanish();
        } else if (tickCount % 40 == 0 && random.nextFloat() < 0.3f && nearest.distanceTo(this) > 16f) {
            creepCloser(nearest);
        }
    }

    /** Move-se ~3 blocos na direcao do jogador - so quando ninguem esta olhando. */
    private void creepCloser(Player target) {
        Vec3 dir = target.position().subtract(position()).multiply(1, 0, 1).normalize().scale(3);
        AABB moved = getBoundingBox().move(dir);
        if (level().noCollision(this, moved)) {
            moveTo(getX() + dir.x, getY(), getZ() + dir.z, getYRot(), 0f);
        }
    }

    public void vanish() {
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 0.4f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), 18, 0.3, 0.8, 0.3, 0.01);
        }
        discard();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (packet.getData() == Ghosts.GHOST_MARKER) this.ghost = true;
    }
}
