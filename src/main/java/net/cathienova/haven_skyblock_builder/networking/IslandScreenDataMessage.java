package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record IslandScreenDataMessage(
        boolean hasTeam,
        boolean canEdit,
        boolean canInvite,
        boolean canAcceptRequests,
        boolean canChangeSpawn,
        boolean canChangeVisits,
        boolean canChangeJoinRequests,
        String currentTeamId,
        String teamName,
        String leaderName,
        boolean allowVisit,
        boolean allowJoinRequests,
        String islandTemplate,
        int homeX,
        int homeY,
        int homeZ,
        long createdAt,
        long lastChangedAt,
        List<MemberEntry> members,
        List<JoinRequestEntry> joinRequests,
        List<TeamEntry> teams,
        List<PlayerEntry> onlinePlayers,
        List<TemplateEntry> templates
) implements CustomPacketPayload {
    public static final Type<IslandScreenDataMessage> TYPE = new Type<>(HavenSkyblockBuilder.loc("island_screen_data"));
    public static final StreamCodec<FriendlyByteBuf, IslandScreenDataMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public IslandScreenDataMessage decode(FriendlyByteBuf buffer) {
            boolean hasTeam = buffer.readBoolean();
            boolean canEdit = buffer.readBoolean();
            boolean canInvite = buffer.readBoolean();
            boolean canAcceptRequests = buffer.readBoolean();
            boolean canChangeSpawn = buffer.readBoolean();
            boolean canChangeVisits = buffer.readBoolean();
            boolean canChangeJoinRequests = buffer.readBoolean();
            String currentTeamId = buffer.readUtf(64);
            String teamName = buffer.readUtf(256);
            String leaderName = buffer.readUtf(64);
            boolean allowVisit = buffer.readBoolean();
            boolean allowJoinRequests = buffer.readBoolean();
            String islandTemplate = buffer.readUtf(256);
            int homeX = buffer.readInt();
            int homeY = buffer.readInt();
            int homeZ = buffer.readInt();
            long createdAt = buffer.readLong();
            long lastChangedAt = buffer.readLong();
            List<MemberEntry> members = readMembers(buffer);
            List<JoinRequestEntry> joinRequests = readJoinRequests(buffer);
            List<TeamEntry> teams = readTeams(buffer);
            List<PlayerEntry> onlinePlayers = readPlayers(buffer, 1024, "online player");
            List<TemplateEntry> templates = readTemplates(buffer);

            return new IslandScreenDataMessage(
                    hasTeam,
                    canEdit,
                    canInvite,
                    canAcceptRequests,
                    canChangeSpawn,
                    canChangeVisits,
                    canChangeJoinRequests,
                    currentTeamId,
                    teamName,
                    leaderName,
                    allowVisit,
                    allowJoinRequests,
                    islandTemplate,
                    homeX,
                    homeY,
                    homeZ,
                    createdAt,
                    lastChangedAt,
                    members,
                    joinRequests,
                    teams,
                    onlinePlayers,
                    templates
            );
        }

        @Override
        public void encode(FriendlyByteBuf buffer, IslandScreenDataMessage message) {
            buffer.writeBoolean(message.hasTeam());
            buffer.writeBoolean(message.canEdit());
            buffer.writeBoolean(message.canInvite());
            buffer.writeBoolean(message.canAcceptRequests());
            buffer.writeBoolean(message.canChangeSpawn());
            buffer.writeBoolean(message.canChangeVisits());
            buffer.writeBoolean(message.canChangeJoinRequests());
            buffer.writeUtf(message.currentTeamId(), 64);
            buffer.writeUtf(message.teamName(), 256);
            buffer.writeUtf(message.leaderName(), 64);
            buffer.writeBoolean(message.allowVisit());
            buffer.writeBoolean(message.allowJoinRequests());
            buffer.writeUtf(message.islandTemplate(), 256);
            buffer.writeInt(message.homeX());
            buffer.writeInt(message.homeY());
            buffer.writeInt(message.homeZ());
            buffer.writeLong(message.createdAt());
            buffer.writeLong(message.lastChangedAt());

            buffer.writeVarInt(message.members().size());
            for (MemberEntry member : message.members()) {
                buffer.writeUtf(member.uuid(), 64);
                buffer.writeUtf(member.name(), 64);
                buffer.writeBoolean(member.leader());
                buffer.writeBoolean(member.canInvite());
                buffer.writeBoolean(member.canAcceptRequests());
                buffer.writeBoolean(member.canChangeSpawn());
                buffer.writeBoolean(member.canChangeVisits());
                buffer.writeBoolean(member.canChangeJoinRequests());
            }

            buffer.writeVarInt(message.joinRequests().size());
            for (JoinRequestEntry request : message.joinRequests()) {
                buffer.writeUtf(request.uuid(), 64);
                buffer.writeUtf(request.name(), 64);
                buffer.writeLong(request.requestedAt());
            }

            buffer.writeVarInt(message.teams().size());
            for (TeamEntry team : message.teams()) {
                buffer.writeUtf(team.uuid(), 64);
                buffer.writeUtf(team.name(), 256);
                buffer.writeUtf(team.leaderName(), 64);
                buffer.writeVarInt(team.memberCount());
                buffer.writeBoolean(team.allowVisit());
                buffer.writeBoolean(team.allowJoinRequests());
                buffer.writeBoolean(team.ownTeam());
                buffer.writeBoolean(team.disbanded());
                buffer.writeUtf(team.islandTemplate(), 256);
                buffer.writeLong(team.createdAt());
                buffer.writeLong(team.lastChangedAt());
                writePlayers(buffer, team.memberPlayers());
            }

            writePlayers(buffer, message.onlinePlayers());

            buffer.writeVarInt(message.templates().size());
            for (TemplateEntry template : message.templates()) {
                buffer.writeUtf(template.name(), 256);
                buffer.writeVarInt(template.sizeX());
                buffer.writeVarInt(template.sizeY());
                buffer.writeVarInt(template.sizeZ());
                buffer.writeVarInt(template.palette().size());
                for (String blockId : template.palette()) {
                    buffer.writeUtf(blockId, 1024);
                }
                buffer.writeVarInt(template.blocks().size());
                for (PreviewBlock block : template.blocks()) {
                    buffer.writeVarInt(block.x());
                    buffer.writeVarInt(block.y());
                    buffer.writeVarInt(block.z());
                    buffer.writeVarInt(block.paletteIndex());
                }
            }
        }
    };

    public IslandScreenDataMessage {
        members = List.copyOf(members);
        joinRequests = List.copyOf(joinRequests);
        teams = List.copyOf(teams);
        onlinePlayers = List.copyOf(onlinePlayers);
        templates = List.copyOf(templates);
    }

    private static List<MemberEntry> readMembers(FriendlyByteBuf buffer) {
        int count = readCount(buffer, 1024, "member");
        List<MemberEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new MemberEntry(
                    buffer.readUtf(64),
                    buffer.readUtf(64),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean()
            ));
        }
        return List.copyOf(entries);
    }

    private static List<JoinRequestEntry> readJoinRequests(FriendlyByteBuf buffer) {
        int count = readCount(buffer, 1024, "join request");
        List<JoinRequestEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new JoinRequestEntry(buffer.readUtf(64), buffer.readUtf(64), buffer.readLong()));
        }
        return List.copyOf(entries);
    }

    private static List<TeamEntry> readTeams(FriendlyByteBuf buffer) {
        int count = readCount(buffer, 4096, "team");
        List<TeamEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String uuid = buffer.readUtf(64);
            String name = buffer.readUtf(256);
            String leaderName = buffer.readUtf(64);
            int memberCount = buffer.readVarInt();
            boolean allowVisit = buffer.readBoolean();
            boolean allowJoinRequests = buffer.readBoolean();
            boolean ownTeam = buffer.readBoolean();
            boolean disbanded = buffer.readBoolean();
            String islandTemplate = buffer.readUtf(256);
            long createdAt = buffer.readLong();
            long lastChangedAt = buffer.readLong();
            List<PlayerEntry> memberPlayers = readPlayers(buffer, 128, "team member");
            entries.add(new TeamEntry(
                    uuid,
                    name,
                    leaderName,
                    memberCount,
                    allowVisit,
                    allowJoinRequests,
                    ownTeam,
                    disbanded,
                    islandTemplate,
                    createdAt,
                    lastChangedAt,
                    memberPlayers
            ));
        }
        return List.copyOf(entries);
    }

    private static List<PlayerEntry> readPlayers(FriendlyByteBuf buffer, int maximum, String name) {
        int count = readCount(buffer, maximum, name);
        List<PlayerEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new PlayerEntry(buffer.readUtf(64), buffer.readUtf(64)));
        }
        return List.copyOf(entries);
    }

    private static void writePlayers(FriendlyByteBuf buffer, List<PlayerEntry> players) {
        buffer.writeVarInt(players.size());
        for (PlayerEntry player : players) {
            buffer.writeUtf(player.uuid(), 64);
            buffer.writeUtf(player.name(), 64);
        }
    }

    private static List<TemplateEntry> readTemplates(FriendlyByteBuf buffer) {
        int count = readCount(buffer, 256, "template");
        List<TemplateEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = buffer.readUtf(256);
            int sizeX = buffer.readVarInt();
            int sizeY = buffer.readVarInt();
            int sizeZ = buffer.readVarInt();
            int paletteCount = readCount(buffer, 4096, "template palette");
            List<String> palette = new ArrayList<>(paletteCount);
            for (int paletteIndex = 0; paletteIndex < paletteCount; paletteIndex++) {
                palette.add(buffer.readUtf(1024));
            }
            int blockCount = readCount(buffer, 131072, "template preview block");
            List<PreviewBlock> blocks = new ArrayList<>(blockCount);
            for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
                blocks.add(new PreviewBlock(
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt()
                ));
            }
            entries.add(new TemplateEntry(name, sizeX, sizeY, sizeZ, palette, blocks));
        }
        return List.copyOf(entries);
    }

    private static int readCount(FriendlyByteBuf buffer, int maximum, String name) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + name + " count: " + count);
        }
        return count;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record MemberEntry(
            String uuid,
            String name,
            boolean leader,
            boolean canInvite,
            boolean canAcceptRequests,
            boolean canChangeSpawn,
            boolean canChangeVisits,
            boolean canChangeJoinRequests
    ) {
    }

    public record JoinRequestEntry(String uuid, String name, long requestedAt) {
    }

    public record PlayerEntry(String uuid, String name) {
    }

    public record TeamEntry(
            String uuid,
            String name,
            String leaderName,
            int memberCount,
            boolean allowVisit,
            boolean allowJoinRequests,
            boolean ownTeam,
            boolean disbanded,
            String islandTemplate,
            long createdAt,
            long lastChangedAt,
            List<PlayerEntry> memberPlayers
    ) {
        public TeamEntry {
            memberPlayers = List.copyOf(memberPlayers);
        }
    }

    public record TemplateEntry(
            String name,
            int sizeX,
            int sizeY,
            int sizeZ,
            List<String> palette,
            List<PreviewBlock> blocks
    ) {
        public TemplateEntry {
            palette = List.copyOf(palette);
            blocks = List.copyOf(blocks);
        }
    }

    public record PreviewBlock(int x, int y, int z, int paletteIndex) {
    }
}