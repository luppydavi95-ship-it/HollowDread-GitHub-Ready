package com.hollowdread.block;

import com.hollowdread.sanity.Sanity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Lampada que pisca (e apaga) conforme a sanidade de quem esta perto. */
public class FlickerLampBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FlickerLampBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(LIT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide) level.scheduleTick(pos, this, 20);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        boolean lit = state.getValue(LIT);
        Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, false);
        float s = p == null ? 100f : Sanity.get(p);

        if (s >= 60f) {
            if (!lit) level.setBlock(pos, state.setValue(LIT, true), 3);
            level.scheduleTick(pos, this, 40);
            return;
        }

        float intensity = (60f - s) / 60f;
        if (s < 20f && rand.nextFloat() < 0.2f) {
            if (lit) level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.6f, 0.5f);
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.scheduleTick(pos, this, 60 + rand.nextInt(100));
            return;
        }
        if (rand.nextFloat() < intensity * 0.8f) {
            level.setBlock(pos, state.setValue(LIT, !lit), 3);
            level.scheduleTick(pos, this, 2 + rand.nextInt(8));
        } else {
            if (!lit) level.setBlock(pos, state.setValue(LIT, true), 3);
            level.scheduleTick(pos, this, 5 + rand.nextInt(15));
        }
    }
}
