package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum SkyblockWorldMessage implements CustomPacketPayload
{
    INSTANCE;

    public static final StreamCodec<FriendlyByteBuf, SkyblockWorldMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<SkyblockWorldMessage> TYPE = new Type<>(HavenSkyblockBuilder.loc("skyblock_world_msg"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
