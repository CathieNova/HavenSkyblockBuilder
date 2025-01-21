package net.cathienova.haven_skyblock_builder.Gui;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class IslandScreen extends Screen {

    public IslandScreen() {
        super(Component.translatable("haven_skyblock_builder.gui.screen"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int spacing = 10;

        Team currentTeam = TeamManager.getTeamByPlayer(Minecraft.getInstance().player.getUUID());

        if (currentTeam == null) {
            Component WIP = Component.literal("This is Work in Progress");
            Font font = Minecraft.getInstance().font;
            StringWidget WIPLabel = new StringWidget(WIP, font);
            WIPLabel.setX(centerX - font.width(WIP.getString()) / 2);
            WIPLabel.setY(centerY);
            this.addRenderableWidget(WIPLabel);
        } else {
            Component WIP = Component.literal("This is Work in Progress");
            Font font = Minecraft.getInstance().font;
            StringWidget WIPLabel = new StringWidget(WIP, font);
            WIPLabel.setX(centerX - font.width(WIP.getString()) / 2);
            WIPLabel.setY(centerY - spacing);
            this.addRenderableWidget(WIPLabel);

            Collection<Team> allTeams = TeamManager.getAllTeams();
            int teamY = centerY;

            for (Team team : allTeams) {
                Component teamName = Component.literal(team.getName());
                StringWidget teamLabel = new StringWidget(teamName, font);
                teamLabel.setX(centerX - font.width(teamName.getString()) / 2);
                teamLabel.setY(teamY);
                this.addRenderableWidget(teamLabel);
                teamY += spacing;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bindForSetup(HavenSkyblockBuilder.loc("textures/gui/hsb_gui.png"));
        int centerX = (this.width - 176) / 2;
        int centerY = (this.height - 166) / 2;
        guiGraphics.blit(
                HavenSkyblockBuilder.loc("textures/gui/hsb_gui.png"),
                centerX, centerY,
                0, 0,
                176, 166,
                256, 256
        );
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}