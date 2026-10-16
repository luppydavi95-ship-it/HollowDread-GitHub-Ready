package com.hollowdread.sanity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSanityProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerSanity backend = new PlayerSanity();
    private final LazyOptional<PlayerSanity> optional = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == Sanity.CAP ? optional.cast() : LazyOptional.empty();
    }

    @Override public CompoundTag serializeNBT() { return backend.serializeNBT(); }
    @Override public void deserializeNBT(CompoundTag nbt) { backend.deserializeNBT(nbt); }
}
