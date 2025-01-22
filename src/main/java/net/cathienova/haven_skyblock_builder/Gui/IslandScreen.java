package net.cathienova.haven_skyblock_builder.Gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import static net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder.MOD_ID;


public class IslandScreen extends Screen {
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/7pp55ytrrpy31.png");

    public IslandScreen(Component title) {
        super(title);
    }
    public IslandScreen(){
        super(new Component() {
            @Override
            public Style getStyle() {
                String string = "return null;";
                return new Style();
            }

            @Override
            public ComponentContents getContents() {
                return null;
            }

            @Override
            public List<Component> getSiblings() {
                return List.of();
            }

            @Override
            public FormattedCharSequence getVisualOrderText() {
                return null;
            }
        });
    }
    @Override
    protected void init() {
        super.init();

    }
    @Override
    public void tick(){
        super.tick();
    }
    @Override
    public void onClose() {
        // Stop any handlers here

        // Call last in case it interferes with the override
        super.onClose();
    }

    @Override
    public void removed() {
        // Reset initial states here

        // Call last in case it interferes with the override
        super.removed();
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background is typically rendered first
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render things here before widgets (background textures)
        graphics.blit(BACKGROUND_LOCATION,0,0,0,0,100,100);
        // Then the widgets if this is a direct child of the Screen
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
    }
}
