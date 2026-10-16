package com.hollowdread.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Vela de Protecao: afasta o Stalker, e restaura sanidade de quem esta perto. */
public class WardingCandleBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 10, 11);

    public WardingCandleBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.75, z = pos.getZ() + 0.5;
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0, 0);
        if (random.nextInt(8) == 0) level.addParticle(ParticleTypes.SMOKE, x, y + 0.1, z, 0, 0.01, 0);
    }
}
