package com.hollowdread.sanity;

import com.hollowdread.network.ModNetwork;
import com.hollowdread.network.SyncSanityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

/** Acesso estatico simples a sanidade de um jogador. */
public final class Sanity {
    public static final Capability<PlayerSanity> CAP = CapabilityManager.get(new CapabilityToken<PlayerSanity>() {});

    private Sanity() {}

    @Nullable
    public static PlayerSanity data(Player p) {
        return p.getCapability(CAP).orElse(null);
    }

    public static float get(Player p) {
        PlayerSanity d = data(p);
        return d == null ? 100f : d.get();
    }

    public static void add(Player p, float amount) {
        if (p.level().isClientSide) return;
        PlayerSanity d = data(p);
        if (d != null) d.add(amount);
    }

    public static void set(Player p, float value) {
        if (p.level().isClientSide) return;
        PlayerSanity d = data(p);
        if (d != null) d.set(value);
    }

    public static void sync(ServerPlayer sp) {
        PlayerSanity d = data(sp);
        if (d == null) return;
        d.lastSynced = d.get();
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncSanityPacket(d.get()));
    }
}
