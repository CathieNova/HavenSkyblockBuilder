package net.cathienova.haven_skyblock_builder.screens;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record StructurePreviewRenderState(List<PreviewBlock> blocks, float yaw, float pitch, float centerX,
                                          float centerY, float centerZ, int x0, int y0, int x1, int y1, float scale,
                                          @Nullable ScreenRectangle bounds,
                                          @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {
    public StructurePreviewRenderState(List<PreviewBlock> blocks, float yaw, float pitch, float centerX, float centerY, float centerZ,
                                       int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
        this(blocks, yaw, pitch, centerX, centerY, centerZ, x0, y0, x1, y1, scale, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), scissorArea);
    }

    public StructurePreviewRenderState {
        blocks = List.copyOf(blocks);
    }

    public record PreviewBlock(BlockModelRenderState renderState, int x, int y, int z) {
    }
}