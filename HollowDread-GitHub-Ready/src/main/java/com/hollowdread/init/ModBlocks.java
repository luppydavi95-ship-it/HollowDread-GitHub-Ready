package com.hollowdread.init;

import com.hollowdread.HollowDread;
import com.hollowdread.block.FlickerLampBlock;
import com.hollowdread.block.WardingCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HollowDread.MODID);

    public static final RegistryObject<Block> WARDING_CANDLE = BLOCKS.register("warding_candle",
            () -> new WardingCandleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND).strength(0.3f).sound(SoundType.CANDLE)
                    .noOcclusion().lightLevel(s -> 10)));

    public static final RegistryObject<Block> FLICKER_LAMP = BLOCKS.register("flicker_lamp",
            () -> new FlickerLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD).strength(0.5f).sound(SoundType.GLASS)
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 14 : 0)));

    private ModBlocks() {}
}
