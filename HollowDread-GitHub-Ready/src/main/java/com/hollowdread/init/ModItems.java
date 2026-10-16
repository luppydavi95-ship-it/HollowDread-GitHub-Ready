package com.hollowdread.init;

import com.hollowdread.HollowDread;
import com.hollowdread.item.DollItem;
import com.hollowdread.item.LanternItem;
import com.hollowdread.item.LucidPillItem;
import com.hollowdread.item.TomeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HollowDread.MODID);

    public static final RegistryObject<Item> LUCID_PILL = ITEMS.register("lucid_pill",
            () -> new LucidPillItem(new Item.Properties()));
    public static final RegistryObject<Item> TOME = ITEMS.register("dread_tome",
            () -> new TomeItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DOLL = ITEMS.register("cursed_doll",
            () -> new DollItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> LANTERN = ITEMS.register("lantern_of_reason",
            () -> new LanternItem(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> WARDING_CANDLE = ITEMS.register("warding_candle",
            () -> new BlockItem(ModBlocks.WARDING_CANDLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLICKER_LAMP = ITEMS.register("flicker_lamp",
            () -> new BlockItem(ModBlocks.FLICKER_LAMP.get(), new Item.Properties()));

    public static final RegistryObject<Item> STALKER_SPAWN_EGG = ITEMS.register("stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.STALKER, 0x0A0A0A, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<Item> WATCHER_SPAWN_EGG = ITEMS.register("watcher_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WATCHER, 0xD8D8D0, 0x000000, new Item.Properties()));
    public static final RegistryObject<Item> CRAWLER_SPAWN_EGG = ITEMS.register("crawler_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CRAWLER, 0x1A0E0E, 0xB01010, new Item.Properties()));

    private ModItems() {}
}
