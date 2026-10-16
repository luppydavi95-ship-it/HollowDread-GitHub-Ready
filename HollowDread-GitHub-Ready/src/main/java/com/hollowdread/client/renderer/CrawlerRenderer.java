package com.hollowdread.client.renderer;

import com.hollowdread.HollowDread;
import com.hollowdread.entity.CrawlerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CrawlerRenderer extends MobRenderer<CrawlerEntity, SpiderModel<CrawlerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowDread.MODID, "textures/entity/crawler.png");

    public CrawlerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SpiderModel<>(ctx.bakeLayer(ModelLayers.SPIDER)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(CrawlerEntity e) { return TEXTURE; }

    @Override
    protected void scale(CrawlerEntity e, PoseStack stack, float partialTick) {
        stack.scale(0.9f, 0.7f, 1.1f);
    }

    @Override
    protected float getFlipDegrees(CrawlerEntity e) { return 180.0f; }
}
