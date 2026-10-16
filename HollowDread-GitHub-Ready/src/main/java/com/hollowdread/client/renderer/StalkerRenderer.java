package com.hollowdread.client.renderer;

import com.hollowdread.HollowDread;
import com.hollowdread.entity.StalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StalkerRenderer extends HumanoidMobRenderer<StalkerEntity, HumanoidModel<StalkerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowDread.MODID, "textures/entity/stalker.png");

    public StalkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEntity e) { return TEXTURE; }

    /** Alto e esguio. */
    @Override
    protected void scale(StalkerEntity e, PoseStack stack, float partialTick) {
        stack.scale(0.92f, 1.45f, 0.92f);
    }
}
