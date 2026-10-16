package com.hollowdread.network;

import com.hollowdread.HollowDread;
import com.hollowdread.scare.ScareType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HollowDread.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncSanityPacket.class,
                SyncSanityPacket::encode, SyncSanityPacket::decode, SyncSanityPacket::handle);
        CHANNEL.registerMessage(id++, ScarePacket.class,
                ScarePacket::encode, ScarePacket::decode, ScarePacket::handle);
    }

    public static void sendScare(ServerPlayer sp, ScareType type, double x, double y, double z, int extra) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new ScarePacket(type, x, y, z, extra));
    }
}
