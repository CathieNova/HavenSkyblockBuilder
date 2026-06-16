package net.cathienova.haven_skyblock_builder.networking;

import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.events.ModEvents;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.SkyblockUtils;
import net.cathienova.haven_skyblock_builder.util.StructureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.File;
import java.util.*;

public class NetworkHandler {
    private static final int MAX_PREVIEW_BLOCKS = 131072;

    @SuppressWarnings("Convert2MethodRef")
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(SkyblockWorldMessage.TYPE, SkyblockWorldMessage.STREAM_CODEC);
        registrar.playToServer(IslandScreenRequestMessage.TYPE, IslandScreenRequestMessage.STREAM_CODEC, (message, context) -> sendIslandScreenData(context));
        registrar.playToServer(IslandScreenActionMessage.TYPE, IslandScreenActionMessage.STREAM_CODEC, NetworkHandler::handleIslandScreenAction);
        registrar.playToClient(IslandScreenDataMessage.TYPE, IslandScreenDataMessage.STREAM_CODEC);
    }

    public static void sendSkyblockWorld(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, SkyblockWorldMessage.INSTANCE);
    }

    public static void sendIslandScreenData(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, createIslandScreenData(player));
    }

    private static void sendIslandScreenData(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            context.reply(createIslandScreenData(player));
        }
    }

    private static IslandScreenDataMessage createIslandScreenData(ServerPlayer player) {
        Team playerTeam = TeamManager.getTeamByPlayer(player.getUUID());
        if (playerTeam != null) {
            TeamManager.removeInvalidJoinRequests(player.level().getServer(), playerTeam);
        }
        Team.Member playerMember = playerTeam == null ? null : playerTeam.getMember(player.getUUID());
        boolean canEdit = playerTeam != null && playerTeam.getLeader().equals(player.getUUID());
        boolean canInvite = canEdit || playerMember != null && playerMember.canInvite();
        boolean canAcceptRequests = canEdit || playerMember != null && playerMember.canAcceptRequests();
        boolean canChangeSpawn = canEdit || playerMember != null && playerMember.canChangeSpawn();
        boolean canChangeVisits = canEdit || playerMember != null && playerMember.canChangeVisits();
        boolean canChangeJoinRequests = canEdit || playerMember != null && playerMember.canChangeJoinRequests();

        List<IslandScreenDataMessage.TeamEntry> teams = TeamManager.getAllTeams().stream()
                .sorted(Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(4096)
                .map(team -> new IslandScreenDataMessage.TeamEntry(
                        team.getUuid().toString(),
                        team.getName(),
                        team.getLeaderName(),
                        team.getMembers().size(),
                        team.isAllowVisit(),
                        team.isAllowJoinRequests(),
                        playerTeam != null && team.getUuid().equals(playerTeam.getUuid()),
                        team.isDisbanded(),
                        team.getIslandTemplate(),
                        team.getCreatedAt(),
                        team.getLastChangedAt(),
                        team.getMembers().stream()
                                .limit(128)
                                .map(member -> new IslandScreenDataMessage.PlayerEntry(member.getUuid().toString(), member.getName()))
                                .toList()
                ))
                .toList();

        List<IslandScreenDataMessage.MemberEntry> members = playerTeam == null ? List.of() : playerTeam.getMembers().stream()
                .map(member -> new IslandScreenDataMessage.MemberEntry(
                        member.getUuid().toString(),
                        member.getName(),
                        member.getUuid().equals(playerTeam.getLeader()),
                        member.canInvite(),
                        member.canAcceptRequests(),
                        member.canChangeSpawn(),
                        member.canChangeVisits(),
                        member.canChangeJoinRequests()
                ))
                .toList();

        List<IslandScreenDataMessage.JoinRequestEntry> joinRequests = playerTeam == null || !canAcceptRequests ? List.of() : playerTeam.getJoinRequests().stream()
                .map(request -> new IslandScreenDataMessage.JoinRequestEntry(
                        request.getUuid().toString(),
                        request.getName(),
                        request.getRequestedAt()
                ))
                .toList();

        List<IslandScreenDataMessage.PlayerEntry> onlinePlayers = player.level().getServer().getPlayerList().getPlayers().stream()
                .filter(candidate -> !candidate.getUUID().equals(player.getUUID()))
                .filter(candidate -> TeamManager.getTeamByPlayer(candidate.getUUID()) == null)
                .filter(candidate -> TeamManager.getPendingInvite(candidate.getUUID()) == null)
                .sorted(Comparator.comparing(candidate -> candidate.getName().getString(), String.CASE_INSENSITIVE_ORDER))
                .limit(1024)
                .map(candidate -> new IslandScreenDataMessage.PlayerEntry(candidate.getUUID().toString(), candidate.getName().getString()))
                .toList();

        BlockPos home = playerTeam == null || playerTeam.getHomePosition() == null ? BlockPos.ZERO : playerTeam.getHomePosition();
        ServerLevel overworld = player.level().getServer().getLevel(ServerLevel.OVERWORLD);
        List<IslandScreenDataMessage.TemplateEntry> templates = overworld == null ? List.of() : createTemplateEntries(overworld);

        return new IslandScreenDataMessage(
                playerTeam != null,
                canEdit,
                canInvite,
                canAcceptRequests,
                canChangeSpawn,
                canChangeVisits,
                canChangeJoinRequests,
                playerTeam == null ? "" : playerTeam.getUuid().toString(),
                playerTeam == null ? "" : playerTeam.getName(),
                playerTeam == null ? "" : playerTeam.getLeaderName(),
                playerTeam != null && playerTeam.isAllowVisit(),
                playerTeam != null && playerTeam.isAllowJoinRequests(),
                playerTeam == null ? "" : playerTeam.getIslandTemplate(),
                home.getX(),
                home.getY(),
                home.getZ(),
                playerTeam == null ? 0 : playerTeam.getCreatedAt(),
                playerTeam == null ? 0 : playerTeam.getLastChangedAt(),
                members,
                joinRequests,
                teams,
                onlinePlayers,
                templates
        );
    }

    private static List<IslandScreenDataMessage.TemplateEntry> createTemplateEntries(ServerLevel level) {
        ModEvents.generateDefaultTemplates();
        File templateFolder = new File("config/HavenSkyblockBuilder/Templates");
        File[] files = templateFolder.listFiles((directory, name) -> name.endsWith(".nbt"));
        if (files == null) {
            return List.of();
        }

        List<IslandScreenDataMessage.TemplateEntry> entries = new ArrayList<>();
        java.util.Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        for (File file : files) {
            if (entries.size() >= 256) {
                break;
            }

            try {
                StructureTemplate template = StructureUtils.loadStructure(level, file.toPath());
                entries.add(createTemplateEntry(file.getName().substring(0, file.getName().length() - 4), template));
            } catch (Exception ignored) {
            }
        }

        return List.copyOf(entries);
    }

    private static IslandScreenDataMessage.TemplateEntry createTemplateEntry(String name, StructureTemplate template) {
        if (template.palettes.isEmpty()) {
            return new IslandScreenDataMessage.TemplateEntry(name, template.getSize().getX(), template.getSize().getY(), template.getSize().getZ(), List.of(), List.of());
        }

        List<StructureTemplate.StructureBlockInfo> sourceBlocks = template.palettes.getFirst().blocks.stream()
                .filter(block -> !block.state().isAir())
                .sorted(Comparator.comparingInt((StructureTemplate.StructureBlockInfo block) -> block.pos().getY())
                        .thenComparingInt(block -> block.pos().getX() + block.pos().getZ())
                        .thenComparingInt(block -> block.pos().getX()))
                .limit(MAX_PREVIEW_BLOCKS)
                .toList();

        List<String> palette = new ArrayList<>();
        Map<String, Integer> paletteIndexes = new HashMap<>();
        List<IslandScreenDataMessage.PreviewBlock> blocks = new ArrayList<>(sourceBlocks.size());

        for (StructureTemplate.StructureBlockInfo block : sourceBlocks) {
            String blockStateText = serializeBlockState(block.state());
            int paletteIndex = paletteIndexes.computeIfAbsent(blockStateText, id ->
            {
                palette.add(id);
                return palette.size() - 1;
            });
            blocks.add(new IslandScreenDataMessage.PreviewBlock(
                    block.pos().getX(),
                    block.pos().getY(),
                    block.pos().getZ(),
                    paletteIndex
            ));
        }

        return new IslandScreenDataMessage.TemplateEntry(
                name,
                template.getSize().getX(),
                template.getSize().getY(),
                template.getSize().getZ(),
                palette,
                blocks
        );
    }


    private static String serializeBlockState(BlockState state) {
        StringBuilder value = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        List<Property<?>> properties = state.getProperties().stream()
                .sorted(Comparator.comparing(property -> property.getName()))
                .toList();
        if (!properties.isEmpty()) {
            value.append('[');
            for (int index = 0; index < properties.size(); index++) {
                if (index > 0) {
                    value.append(',');
                }
                Property<?> property = properties.get(index);
                value.append(property.getName()).append('=').append(propertyValue(state, property));
            }
            value.append(']');
        }
        return value.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValue(BlockState state, Property property) {
        return property.getName(state.getValue(property));
    }

    private static void handleIslandScreenAction(IslandScreenActionMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        switch (message.action()) {
            case CREATE_TEAM ->
                    SkyblockUtils.createTeam(player, message.template(), message.name(), message.allowVisit(), message.allowJoinRequests());
            case UPDATE_TEAM -> updateTeam(player, message);
            case INVITE_MEMBER -> inviteMember(player, message.playerName());
            case LEAVE_TEAM -> SkyblockUtils.leaveTeam(player);
            case REQUEST_JOIN -> requestJoin(player, message.teamId());
            case ACCEPT_JOIN_REQUEST -> acceptJoinRequest(player, message.playerId());
            case DENY_JOIN_REQUEST -> denyJoinRequest(player, message.playerId());
            case REMOVE_MEMBER -> removeMember(player, message.playerId());
            case UPDATE_MEMBER_PERMISSIONS -> updateMemberPermissions(player, message);
        }

        context.reply(createIslandScreenData(player));
    }

    private static void updateTeam(ServerPlayer player, IslandScreenActionMessage message) {
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        if (team == null) {
            return;
        }

        boolean changed = false;
        if (team.getLeader().equals(player.getUUID())) {
            String cleanedName = message.name() == null ? "" : message.name().trim();
            if (cleanedName.isEmpty() || cleanedName.length() > 64) {
                player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.island.create.name"));
                return;
            }
            boolean nameExists = TeamManager.getAllTeams().stream()
                    .filter(candidate -> !candidate.getUuid().equals(team.getUuid()))
                    .filter(candidate -> !candidate.isDisbanded())
                    .anyMatch(candidate -> candidate.getName().equalsIgnoreCase(cleanedName));
            if (nameExists) {
                player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.name_exists"));
                return;
            }
            if (!team.getName().equals(cleanedName)) {
                team.setName(cleanedName);
                changed = true;
            }
        }
        if (canChangeVisits(team, player)) {
            team.setAllowVisit(message.allowVisit());
            changed = true;
        }
        if (canChangeJoinRequests(team, player)) {
            team.setAllowJoinRequests(message.allowJoinRequests());
            changed = true;
        }
        if (canChangeSpawn(team, player)) {
            Vec2 rotation = team.getHomeRotation() == null ? new Vec2(0, 0) : team.getHomeRotation();
            team.setHomePosition(new BlockPos(message.homeX(), message.homeY(), message.homeZ()), rotation);
            changed = true;
        }

        if (changed) {
            TeamManager.saveTeam(player.level().getServer(), team);
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.gui.settings_saved"));
        } else {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.no_team_permission"));
        }
    }

    private static void inviteMember(ServerPlayer player, String playerName) {
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        if (team == null || !canInvite(team, player)) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.no_team_permission"));
            return;
        }

        ServerPlayer invitee = player.level().getServer().getPlayerList().getPlayers().stream()
                .filter(candidate -> candidate.getName().getString().equalsIgnoreCase(playerName.trim()))
                .findFirst()
                .orElse(null);

        if (invitee == null) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.player_not_online", playerName));
            return;
        }

        if (TeamManager.getTeamByPlayer(invitee.getUUID()) != null) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.invitee_already_in_team", invitee.getName().getString()));
            return;
        }

        if (TeamManager.getPendingInvite(invitee.getUUID()) != null) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.invite_already_sent", invitee.getName().getString()));
            return;
        }

        TeamManager.addPendingInvite(invitee.getUUID(), team.getUuid());
        player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.invite_sent", invitee.getName().getString()));
        invitee.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.invite_received", player.getName().getString()));
    }

    private static void requestJoin(ServerPlayer player, String teamId) {
        if (TeamManager.getTeamByPlayer(player.getUUID()) != null) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.already_in_team"));
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(teamId);
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.team_not_found"));
            return;
        }

        Team team = TeamManager.getTeamById(uuid);
        if (team == null || team.isDisbanded()) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.team_not_found"));
            return;
        }

        if (!team.isAllowJoinRequests()) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.join_requests_disabled"));
            return;
        }

        if (!team.addJoinRequest(player.getUUID(), player.getName().getString())) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.join_request_exists"));
            return;
        }

        TeamManager.saveTeam(player.level().getServer(), team);
        player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.join_request_sent", team.getName()));
        ServerPlayer leader = player.level().getServer().getPlayerList().getPlayer(team.getLeader());
        if (leader != null) {
            leader.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.join_request_received", player.getName().getString()));
        }
    }

    private static void acceptJoinRequest(ServerPlayer player, String playerId) {
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        UUID uuid = parsePlayerId(player, playerId);
        if (team == null || uuid == null || !canAcceptRequests(team, player)) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.no_team_permission"));
            return;
        }

        Team.JoinRequest request = team.getJoinRequests().stream()
                .filter(entry -> entry.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        if (request == null) {
            return;
        }

        if (TeamManager.getTeamByPlayer(uuid) != null) {
            team.removeJoinRequest(uuid);
            TeamManager.saveTeam(player.level().getServer(), team);
            return;
        }

        team.addMember(uuid, request.getName());
        TeamManager.saveTeam(player.level().getServer(), team);
        TeamManager.removeJoinRequestsForPlayer(player.level().getServer(), uuid);
        ServerPlayer joinedPlayer = player.level().getServer().getPlayerList().getPlayer(uuid);
        if (joinedPlayer != null) {
            joinedPlayer.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.join_success", team.getName()));
        }
    }

    private static void denyJoinRequest(ServerPlayer player, String playerId) {
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        UUID uuid = parsePlayerId(player, playerId);
        if (team == null || uuid == null || !canAcceptRequests(team, player)) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.no_team_permission"));
            return;
        }

        team.removeJoinRequest(uuid);
        TeamManager.saveTeam(player.level().getServer(), team);
    }

    private static void removeMember(ServerPlayer player, String playerId) {
        Team team = getLeaderTeam(player);
        UUID uuid = parsePlayerId(player, playerId);
        if (team == null || uuid == null || team.getLeader().equals(uuid)) {
            return;
        }

        Team.Member member = team.getMember(uuid);
        if (member == null) {
            return;
        }

        team.removeMember(uuid);
        TeamManager.saveTeam(player.level().getServer(), team);
        ServerPlayer removedPlayer = player.level().getServer().getPlayerList().getPlayer(uuid);
        if (removedPlayer != null) {
            BlockPos spawn = SkyblockUtils.parseConfigPosition(HavenConfig.spawnPosition);
            ServerLevel overworld = player.level().getServer().getLevel(ServerLevel.OVERWORLD);
            if (overworld != null) {
                removedPlayer.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, Set.<Relative>of(), 0, 0, true);
                removedPlayer.resetFallDistance();
            }
            removedPlayer.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.kicked", team.getName()));
        }
        player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.kick_success", member.getName()));
    }

    private static void updateMemberPermissions(ServerPlayer player, IslandScreenActionMessage message) {
        Team team = getLeaderTeam(player);
        UUID uuid = parsePlayerId(player, message.playerId());
        if (team == null || uuid == null || team.getLeader().equals(uuid) || team.getMember(uuid) == null) {
            return;
        }

        team.updateMemberPermissions(
                uuid,
                message.canInvite(),
                message.canAcceptRequests(),
                message.canChangeSpawn(),
                message.canChangeVisits(),
                message.canChangeJoinRequests()
        );
        TeamManager.saveTeam(player.level().getServer(), team);
        player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.gui.permissions_saved"));
    }

    private static Team getLeaderTeam(ServerPlayer player) {
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        if (team == null || !team.getLeader().equals(player.getUUID())) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.not_leader"));
            return null;
        }
        return team;
    }

    private static boolean canInvite(Team team, ServerPlayer player) {
        Team.Member member = team.getMember(player.getUUID());
        return team.getLeader().equals(player.getUUID()) || member != null && member.canInvite();
    }

    private static boolean canAcceptRequests(Team team, ServerPlayer player) {
        Team.Member member = team.getMember(player.getUUID());
        return team.getLeader().equals(player.getUUID()) || member != null && member.canAcceptRequests();
    }

    private static boolean canChangeSpawn(Team team, ServerPlayer player) {
        Team.Member member = team.getMember(player.getUUID());
        return team.getLeader().equals(player.getUUID()) || member != null && member.canChangeSpawn();
    }

    private static boolean canChangeVisits(Team team, ServerPlayer player) {
        Team.Member member = team.getMember(player.getUUID());
        return team.getLeader().equals(player.getUUID()) || member != null && member.canChangeVisits();
    }

    private static boolean canChangeJoinRequests(Team team, ServerPlayer player) {
        Team.Member member = team.getMember(player.getUUID());
        return team.getLeader().equals(player.getUUID()) || member != null && member.canChangeJoinRequests();
    }

    private static UUID parsePlayerId(ServerPlayer player, String playerId) {
        try {
            return UUID.fromString(playerId);
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(HavenConfig.message("haven_skyblock_builder.team.player_not_found", playerId));
            return null;
        }
    }
}