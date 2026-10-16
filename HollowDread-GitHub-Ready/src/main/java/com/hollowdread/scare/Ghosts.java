package com.hollowdread.scare;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Logica de observacao ("estou sendo olhado?") e de alucinacoes client-side. */
public final class Ghosts {
    /** Valor enviado no ClientboundAddEntityPacket para marcar uma alucinacao. */
    public static final int GHOST_MARKER = 777;

    private Ghosts() {}

    public static boolean isLookingAt(Player p, LivingEntity e, double maxDist) {
        if (p.isSpectator() || p.isCreative()) return false;
        if (p.hasEffect(MobEffects.BLINDNESS)) return false;
        if (p.distanceTo(e) > maxDist) return false;
        Vec3 look = p.getViewVector(1.0f).normalize();
        Vec3 to = e.getEyePosition().subtract(p.getEyePosition());
        double len = to.length();
        if (len < 1.0E-4) return true;
        to = to.scale(1.0 / len);
        if (look.dot(to) < 0.6) return false;
        return p.hasLineOfSight(e);
    }

    public static void face(LivingEntity e, Player p) {
        e.lookAt(EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
        e.setYBodyRot(e.getYRot());
        e.setYHeadRot(e.getYRot());
    }

    /** Roda no CLIENTE para entidades fantasma (nao existem no servidor). */
    public static void clientTick(Mob e) {
        Level level = e.level();
        Player p = level.getNearestPlayer(e, 80);
        if (p == null || e.tickCount > 260) {
            e.discard();
            return;
        }
        face(e, p);
        CompoundTag tag = e.getPersistentData();
        if (isLookingAt(p, e, 80)) tag.putInt("seen", tag.getInt("seen") + 1);
        if (p.distanceTo(e) < 7.5f || tag.getInt("seen") > 45) {
            level.playLocalSound(e.getX(), e.getY(), e.getZ(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 0.8f, 0.4f, false);
            for (int i = 0; i < 12; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                        e.getX() + (level.random.nextDouble() - 0.5) * 0.8,
                        e.getY() + level.random.nextDouble() * 2.2,
                        e.getZ() + (level.random.nextDouble() - 0.5) * 0.8, 0, 0.02, 0);
            }
            e.discard();
        }
    }
}
