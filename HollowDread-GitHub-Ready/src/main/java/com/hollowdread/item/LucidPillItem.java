package com.hollowdread.item;

import com.hollowdread.init.ModEffects;
import com.hollowdread.sanity.PlayerSanity;
import com.hollowdread.sanity.Sanity;
import com.hollowdread.scare.DreadDirector;
import com.hollowdread.scare.DreadEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Restaura sanidade, mas 3 doses em pouco tempo causam overdose e alucinacoes. */
public class LucidPillItem extends Item {
    public LucidPillItem(Properties props) {
        super(props.stacksTo(16).food(new FoodProperties.Builder()
                .nutrition(0).saturationMod(0f).alwaysEat().fast().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            Sanity.add(sp, 35f);
            sp.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, false, false));
            PlayerSanity d = Sanity.data(sp);
            if (d != null) {
                d.pillDose++;
                if (d.pillDose >= 3) {
                    sp.addEffect(new MobEffectInstance(ModEffects.DREAD.get(), 600));
                    DreadDirector.run(sp, DreadEvent.GHOST);
                    DreadDirector.run(sp, DreadEvent.WHISPER);
                }
            }
            Sanity.sync(sp);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
