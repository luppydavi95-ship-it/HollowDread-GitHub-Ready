package com.hollowdread.event;

import com.hollowdread.HollowDread;
import com.hollowdread.entity.CrawlerEntity;
import com.hollowdread.entity.StalkerEntity;
import com.hollowdread.entity.WatcherEntity;
import com.hollowdread.init.ModEntities;
import com.hollowdread.sanity.PlayerSanity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowDread.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    private ModBusEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.STALKER.get(), StalkerEntity.createAttributes().build());
        e.put(ModEntities.WATCHER.get(), WatcherEntity.createAttributes().build());
        e.put(ModEntities.CRAWLER.get(), CrawlerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void capabilities(RegisterCapabilitiesEvent e) {
        e.register(PlayerSanity.class);
    }

    @SubscribeEvent
    public static void spawnPlacements(SpawnPlacementRegisterEvent e) {
        e.register(ModEntities.CRAWLER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
}
