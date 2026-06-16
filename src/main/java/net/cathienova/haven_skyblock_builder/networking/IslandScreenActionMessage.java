package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record IslandScreenActionMessage(
        Action action,
        String teamId,
        String name,
        String template,
        String playerName,
        String playerId,
        boolean allowVisit,
        boolean allowJoinRequests,
        boolean canInvite,
        boolean canAcceptRequests,
        boolean canChangeSpawn,
        boolean canChangeVisits,
        boolean canChangeJoinRequests,
        int homeX,
        int homeY,
        int homeZ
) implements CustomPacketPayload {
    public static final Type<IslandScreenActionMessage> TYPE = new Type<>(HavenSkyblockBuilder.loc("island_screen_action"));
    public static final StreamCodec<FriendlyByteBuf, IslandScreenActionMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public IslandScreenActionMessage decode(FriendlyByteBuf buffer) {
            int actionIndex = buffer.readVarInt();
            if (actionIndex < 0 || actionIndex >= Action.values().length) {
                throw new IllegalArgumentException("Invalid island screen action: " + actionIndex);
            }

            return new IslandScreenActionMessage(
                    Action.values()[actionIndex],
                    buffer.readUtf(64),
                    buffer.readUtf(256),
                    buffer.readUtf(256),
                    buffer.readUtf(64),
                    buffer.readUtf(64),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buffer, IslandScreenActionMessage message) {
            buffer.writeVarInt(message.action().ordinal());
            buffer.writeUtf(message.teamId(), 64);
            buffer.writeUtf(message.name(), 256);
            buffer.writeUtf(message.template(), 256);
            buffer.writeUtf(message.playerName(), 64);
            buffer.writeUtf(message.playerId(), 64);
            buffer.writeBoolean(message.allowVisit());
            buffer.writeBoolean(message.allowJoinRequests());
            buffer.writeBoolean(message.canInvite());
            buffer.writeBoolean(message.canAcceptRequests());
            buffer.writeBoolean(message.canChangeSpawn());
            buffer.writeBoolean(message.canChangeVisits());
            buffer.writeBoolean(message.canChangeJoinRequests());
            buffer.writeInt(message.homeX());
            buffer.writeInt(message.homeY());
            buffer.writeInt(message.homeZ());
        }
    };

    private static IslandScreenActionMessage empty(Action action) {
        return new IslandScreenActionMessage(action, "", "", "", "", "", false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage createTeam(String name, String template, boolean allowVisit, boolean allowJoinRequests) {
        return new IslandScreenActionMessage(Action.CREATE_TEAM, "", name, template, "", "", allowVisit, allowJoinRequests, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage updateTeam(String name, boolean allowVisit, boolean allowJoinRequests, int homeX, int homeY, int homeZ) {
        return new IslandScreenActionMessage(Action.UPDATE_TEAM, "", name, "", "", "", allowVisit, allowJoinRequests, false, false, false, false, false, homeX, homeY, homeZ);
    }

    public static IslandScreenActionMessage inviteMember(String playerName) {
        return new IslandScreenActionMessage(Action.INVITE_MEMBER, "", "", "", playerName, "", false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage leaveTeam() {
        return empty(Action.LEAVE_TEAM);
    }

    public static IslandScreenActionMessage requestJoin(String teamId) {
        return new IslandScreenActionMessage(Action.REQUEST_JOIN, teamId, "", "", "", "", false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage acceptJoinRequest(String playerId) {
        return new IslandScreenActionMessage(Action.ACCEPT_JOIN_REQUEST, "", "", "", "", playerId, false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage denyJoinRequest(String playerId) {
        return new IslandScreenActionMessage(Action.DENY_JOIN_REQUEST, "", "", "", "", playerId, false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage removeMember(String playerId) {
        return new IslandScreenActionMessage(Action.REMOVE_MEMBER, "", "", "", "", playerId, false, false, false, false, false, false, false, 0, 0, 0);
    }

    public static IslandScreenActionMessage updateMemberPermissions(
            String playerId,
            boolean canInvite,
            boolean canAcceptRequests,
            boolean canChangeSpawn,
            boolean canChangeVisits,
            boolean canChangeJoinRequests) {
        return new IslandScreenActionMessage(
                Action.UPDATE_MEMBER_PERMISSIONS,
                "",
                "",
                "",
                "",
                playerId,
                false,
                false,
                canInvite,
                canAcceptRequests,
                canChangeSpawn,
                canChangeVisits,
                canChangeJoinRequests,
                0,
                0,
                0
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        CREATE_TEAM,
        UPDATE_TEAM,
        INVITE_MEMBER,
        LEAVE_TEAM,
        REQUEST_JOIN,
        ACCEPT_JOIN_REQUEST,
        DENY_JOIN_REQUEST,
        REMOVE_MEMBER,
        UPDATE_MEMBER_PERMISSIONS
    }
}