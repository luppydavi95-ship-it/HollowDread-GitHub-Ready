package com.hollowdread.init;

import com.hollowdread.HollowDread;
import com.hollowdread.effect.DreadEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HollowDread.MODID);
    public static final RegistryObject<MobEffect> DREAD = EFFECTS.register("dread", DreadEffect::new);

    private ModEffects() {}
}
