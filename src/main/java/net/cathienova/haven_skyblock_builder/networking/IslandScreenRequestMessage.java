package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum IslandScreenRequestMessage implements CustomPacketPayload {
    INSTANCE;

    public static final StreamCodec<FriendlyByteBuf, IslandScreenRequestMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<IslandScreenRequestMessage> TYPE = new Type<>(HavenSkyblockBuilder.loc("island_screen_request"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}