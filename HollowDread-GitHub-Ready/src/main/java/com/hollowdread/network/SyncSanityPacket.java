package com.hollowdread.network;

import com.hollowdread.client.ClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSanityPacket {
    public final float sanity;

    public SyncSanityPacket(float sanity) { this.sanity = sanity; }

    public static void encode(SyncSanityPacket m, FriendlyByteBuf b) { b.writeFloat(m.sanity); }

    public static SyncSanityPacket decode(FriendlyByteBuf b) { return new SyncSanityPacket(b.readFloat()); }

    public static void handle(SyncSanityPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientState.setSanity(m.sanity)));
        ctx.get().setPacketHandled(true);
    }
}
