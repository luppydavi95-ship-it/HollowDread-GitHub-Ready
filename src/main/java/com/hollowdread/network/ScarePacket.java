package com.hollowdread.network;

import com.hollowdread.client.ClientScareManager;
import com.hollowdread.scare.ScareType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScarePacket {
    public final ScareType type;
    public final double x, y, z;
    public final int extra;

    public ScarePacket(ScareType type, double x, double y, double z, int extra) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.extra = extra;
    }

    public static void encode(ScarePacket m, FriendlyByteBuf b) {
        b.writeEnum(m.type);
        b.writeDouble(m.x);
        b.writeDouble(m.y);
        b.writeDouble(m.z);
        b.writeVarInt(m.extra);
    }

    public static ScarePacket decode(FriendlyByteBuf b) {
        return new ScarePacket(b.readEnum(ScareType.class), b.readDouble(), b.readDouble(), b.readDouble(), b.readVarInt());
    }

    public static void handle(ScarePacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientScareManager.handle(m.type, m.x, m.y, m.z, m.extra)));
        ctx.get().setPacketHandled(true);
    }
}
