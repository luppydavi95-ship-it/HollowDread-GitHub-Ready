package com.hollowdread.init;

import com.hollowdread.HollowDread;
import com.hollowdread.entity.CrawlerEntity;
import com.hollowdread.entity.StalkerEntity;
import com.hollowdread.entity.WatcherEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HollowDread.MODID);

    public static final RegistryObject<EntityType<StalkerEntity>> STALKER = ENTITIES.register("stalker",
            () -> EntityType.Builder.of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.6f).fireImmune().clientTrackingRange(10).build("stalker"));

    public static final RegistryObject<EntityType<WatcherEntity>> WATCHER = ENTITIES.register("watcher",
            () -> EntityType.Builder.of(WatcherEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.0f).fireImmune().clientTrackingRange(12).build("watcher"));

    public static final RegistryObject<EntityType<CrawlerEntity>> CRAWLER = ENTITIES.register("crawler",
            () -> EntityType.Builder.of(CrawlerEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.6f).clientTrackingRange(8).build("crawler"));

    private ModEntities() {}
}
