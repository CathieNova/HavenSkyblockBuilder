package net.cathienova.haven_skyblock_builder.Gui;

import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder.MOD_ID;


public class IslandScreen extends Screen {
    private static final Component TITLE = Component.translatable("HeavenSkyBlock.IslandScreen");
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/hsb_gui.png");

    public IslandScreen(Component title) {
        super(title);
    }
    public IslandScreen(){
        super(TITLE);
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

        // Then the widgets if this is a direct child of the Screen
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BACKGROUND_LOCATION,graphics.guiWidth()/2-256/3,graphics.guiHeight()/2-256/3,0,0,256,256);
        assert minecraft != null;
        int i = 0;
        Collection<Team> teams = TeamManager.getAllTeams();
        for (Team team : teams) {
            graphics.drawCenteredString(minecraft.font, team.getName(), graphics.guiWidth() / 3+10, graphics.guiHeight() / 2+(-10*i), 0);
            i++;
        }
        // Render things after widgets (tooltips)
    }
}
