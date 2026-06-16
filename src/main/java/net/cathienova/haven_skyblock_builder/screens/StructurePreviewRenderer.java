package net.cathienova.haven_skyblock_builder.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

public class StructurePreviewRenderer extends PictureInPictureRenderer<StructurePreviewRenderState> {
    public StructurePreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    protected void renderToTexture(StructurePreviewRenderState state, PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw()));
        poseStack.translate(-state.centerX(), -state.centerY(), -state.centerZ());

        FeatureRenderDispatcher dispatcher = minecraft.gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage nodes = dispatcher.getSubmitNodeStorage();

        for (StructurePreviewRenderState.PreviewBlock block : state.blocks()) {
            poseStack.pushPose();
            poseStack.translate(block.x(), block.y(), block.z());
            block.renderState().submitMultiLayer(poseStack, nodes, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        dispatcher.renderAllFeatures();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    public Class<StructurePreviewRenderState> getRenderStateClass() {
        return StructurePreviewRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "haven skyblock island preview";
    }
}