package com.hollowdread.client;

import com.hollowdread.HollowDread;
import com.hollowdread.config.DreadConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowDread.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) ClientScareManager.tick();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.reset();
    }

    /** Camera inclinada e tremida conforme a mente desmorona. */
    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        if (!DreadConfig.CLIENT.cameraShake.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float sev = 1f - ClientState.smoothSanity / 100f;
        double t = mc.level.getGameTime() + e.getPartialTick();
        float amp = DreadConfig.CLIENT.shakeIntensity.get().floatValue();

        if (sev > 0.45f) {
            float k = (sev - 0.45f) / 0.55f * amp;
            e.setRoll(e.getRoll() + (float) Math.sin(t * 0.11) * k * 3f);
            e.setPitch(e.getPitch() + (float) Math.sin(t * 0.37) * k * 0.6f);
            e.setYaw(e.getYaw() + (float) Math.cos(t * 0.29) * k * 0.6f);
        }
        if (ClientScareManager.shake > 0.01f) {
            float s = ClientScareManager.shake * amp;
            e.setRoll(e.getRoll() + (mc.level.random.nextFloat() - 0.5f) * s * 6f);
            e.setPitch(e.getPitch() + (mc.level.random.nextFloat() - 0.5f) * s * 3f);
            e.setYaw(e.getYaw() + (mc.level.random.nextFloat() - 0.5f) * s * 3f);
        }
    }

    /** Neblina fechando em volta do jogador. */
    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog e) {
        if (!DreadConfig.CLIENT.sanityFog.get()) return;
        if (e.getMode() != FogRenderer.FogMode.FOG_TERRAIN || e.getType() != FogType.NONE) return;
        float sev = 1f - ClientState.smoothSanity / 100f;
        if (sev < 0.35f) return;
        float k = Mth.clamp((sev - 0.35f) / 0.65f, 0f, 1f);
        float far = Mth.lerp(k, e.getFarPlaneDistance(), 14f);
        e.setNearPlaneDistance(far * 0.05f);
        e.setFarPlaneDistance(Math.min(far, e.getFarPlaneDistance()));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor e) {
        float sev = 1f - ClientState.smoothSanity / 100f;
        if (sev < 0.35f) return;
        float k = Mth.clamp((sev - 0.35f) / 0.65f, 0f, 1f);
        e.setRed(Mth.lerp(k, e.getRed(), 0.10f));
        e.setGreen(Mth.lerp(k, e.getGreen(), 0.0f));
        e.setBlue(Mth.lerp(k, e.getBlue(), 0.0f));
    }
}
