package net.cathienova.haven_skyblock_builder.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.UUID;

public final class PlayerHeadRenderer {
    private PlayerHeadRenderer() {
    }

    public static void draw(GuiGraphicsExtractor graphics, String uuidText, int x, int y, int size) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidText);
        } catch (IllegalArgumentException e) {
            graphics.fill(x, y, x + size, y + size, 0xFF444444);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PlayerInfo playerInfo = minecraft.getConnection() == null ? null : minecraft.getConnection().getPlayerInfo(uuid);
        PlayerSkin skin = playerInfo == null ? DefaultPlayerSkin.get(uuid) : playerInfo.getSkin();
        PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size);
    }
}