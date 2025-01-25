package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.handler.ClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientMessageHandler
{
    // Removes the underground fog in the skyblock world
    public static void handleSkyblockWorldMessage(IPayloadContext ctx) {
        ctx.enqueueWork(ClientHandler::disableVoidFogRendering);
    }
}
