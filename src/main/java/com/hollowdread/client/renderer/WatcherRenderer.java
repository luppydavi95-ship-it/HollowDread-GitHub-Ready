package com.hollowdread.client.renderer;

import com.hollowdread.HollowDread;
import com.hollowdread.entity.WatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WatcherRenderer extends HumanoidMobRenderer<WatcherEntity, HumanoidModel<WatcherEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowDread.MODID, "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(WatcherEntity e) { return TEXTURE; }

    @Override
    protected void scale(WatcherEntity e, PoseStack stack, float partialTick) {
        stack.scale(0.95f, 1.12f, 0.95f);
    }
}
