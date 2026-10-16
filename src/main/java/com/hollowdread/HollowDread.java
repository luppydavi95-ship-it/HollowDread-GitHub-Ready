package com.hollowdread;

import com.hollowdread.config.DreadConfig;
import com.hollowdread.init.ModBlocks;
import com.hollowdread.init.ModCreativeTabs;
import com.hollowdread.init.ModEffects;
import com.hollowdread.init.ModEntities;
import com.hollowdread.init.ModItems;
import com.hollowdread.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HollowDread.MODID)
public class HollowDread {
    public static final String MODID = "hollowdread";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HollowDread() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModEffects.EFFECTS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        bus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DreadConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DreadConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
