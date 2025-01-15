package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.handler.ClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientMessageHandler
{
    // Removes the black sky/fog that appears when the player is below y=62
    public static void handleSkyblockWorldMessage(IPayloadContext ctx) {
        ctx.enqueueWork(ClientHandler::disableVoidFogRendering);
    }
}
