package com.hollowdread.client;

import com.hollowdread.HollowDread;
import com.hollowdread.config.DreadConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public final class DreadOverlays {
    private static final ResourceLocation VIGNETTE = new ResourceLocation(HollowDread.MODID, "textures/misc/dread_vignette.png");
    private static final ResourceLocation JUMPSCARE = new ResourceLocation(HollowDread.MODID, "textures/gui/jumpscare.png");

    private DreadOverlays() {}

    /** Vinheta escura, flash preto e o rosto do jumpscare. */
    public static final IGuiOverlay EFFECTS = (gui, g, partialTick, w, h) -> {
        float s = ClientState.smoothSanity;
        float sev = 1f - s / 100f;

        if (DreadConfig.CLIENT.vignette.get() && sev > 0.2f) {
            float time = (gui.getMinecraft().player == null ? 0 : gui.getMinecraft().player.tickCount) + partialTick;
            float pulse = 0.85f + 0.15f * (float) Math.sin(time * (0.05f + sev * 0.2f));
            float a = Mth.clamp((sev - 0.2f) / 0.8f, 0f, 1f) * 0.95f * pulse;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, a);
            g.blit(VIGNETTE, 0, 0, w, h, 0, 0, 128, 128, 128, 128);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        if (ClientScareManager.flashTicks > 0) {
            g.fill(0, 0, w, h, 0xFF000000);
        }

        if (ClientScareManager.jumpscareTicks > 0) {
            float a = Mth.clamp(ClientScareManager.jumpscareTicks / 4f, 0f, 1f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, a);
            g.fill(0, 0, w, h, 0xFF000000);
            int size = Math.min(w, h);
            g.blit(JUMPSCARE, (w - size) / 2, (h - size) / 2, size, size, 0, 0, 64, 64, 64, 64);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
    };

    /** Barra de sanidade no canto inferior esquerdo. */
    public static final IGuiOverlay HUD = (gui, g, partialTick, w, h) -> {
        if (!DreadConfig.CLIENT.showSanityHud.get()) return;
        float s = ClientState.smoothSanity;
        int x = 12, y = h - 12, bw = 70, bh = 5;
        int color = s > 60f ? 0xFF55FF55 : s > 30f ? 0xFFFFAA00 : 0xFFFF3333;
        if (s < 25f && (gui.getMinecraft().player != null) && (gui.getMinecraft().player.tickCount / 6) % 2 == 0) {
            color = 0xFF880000;
        }
        g.fill(x - 1, y - 1, x + bw + 1, y + bh + 1, 0xAA000000);
        g.fill(x, y, x + (int) (bw * s / 100f), y + bh, color);
        g.drawString(gui.getFont(), Component.translatable("hud.hollowdread.sanity"), x, y - 10, 0xFFBBBBBB, true);
    };
}
