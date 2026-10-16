package com.hollowdread.client;

import com.hollowdread.HollowDread;
import com.hollowdread.client.renderer.CrawlerRenderer;
import com.hollowdread.client.renderer.StalkerRenderer;
import com.hollowdread.client.renderer.WatcherRenderer;
import com.hollowdread.init.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowDread.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.STALKER.get(), StalkerRenderer::new);
        e.registerEntityRenderer(ModEntities.WATCHER.get(), WatcherRenderer::new);
        e.registerEntityRenderer(ModEntities.CRAWLER.get(), CrawlerRenderer::new);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("dread_effects", DreadOverlays.EFFECTS);
        e.registerAboveAll("sanity", DreadOverlays.HUD);
    }
}
