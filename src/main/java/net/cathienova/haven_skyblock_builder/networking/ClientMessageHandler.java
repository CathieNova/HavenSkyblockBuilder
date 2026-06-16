package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.screens.CreateTeamScreen;
import net.cathienova.haven_skyblock_builder.screens.IslandScreen;
import net.cathienova.haven_skyblock_builder.screens.TeamInfoScreen;
import net.cathienova.haven_skyblock_builder.handler.ClientHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientMessageHandler {
    // Removes the underground fog in the skyblock world
    public static void handleSkyblockWorldMessage(IPayloadContext ctx) {
        ctx.enqueueWork(ClientHandler::disableVoidFogRendering);
    }

    public static void handleIslandScreenData(IslandScreenDataMessage message, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof IslandScreen screen) {
                screen.updateData(message);
            } else if (minecraft.screen instanceof CreateTeamScreen screen) {
                screen.updateData(message);
            } else if (minecraft.screen instanceof TeamInfoScreen screen) {
                screen.updateData(message);
            }
        });
    }
}