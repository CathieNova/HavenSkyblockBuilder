package net.cathienova.haven_skyblock_builder.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler
{
    // DO NOT CONVERT the lambdas to method reference. The server will crash loading client code otherwise.
    @SuppressWarnings("Convert2MethodRef")
    public static void register(PayloadRegistrar registrar) {
        registrar.commonToClient(SkyblockWorldMessage.TYPE, SkyblockWorldMessage.STREAM_CODEC, (msg, ctx) -> ClientMessageHandler.handleSkyblockWorldMessage(ctx));
    }

    public static void sendSkyblockWorld(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, SkyblockWorldMessage.INSTANCE);
    }
}
