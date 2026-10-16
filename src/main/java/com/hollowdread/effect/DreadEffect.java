package com.hollowdread.effect;

import com.hollowdread.sanity.Sanity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Pavor: drena sanidade a cada segundo enquanto durar. */
public class DreadEffect extends MobEffect {
    public DreadEffect() {
        super(MobEffectCategory.HARMFUL, 0x2B0000);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player p && !p.level().isClientSide) {
            Sanity.add(p, -(1.5f + amplifier));
        }
    }
}
