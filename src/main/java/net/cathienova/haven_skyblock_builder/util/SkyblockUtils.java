package net.cathienova.haven_skyblock_builder.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SkyblockUtils
{
    private static StructureTemplate spawnStructure(ServerLevel level, BlockPos position, String islandTemplate)
    {
        StructureTemplate template;

        if (HavenConfig.SkyblockIsland == null)
        {
            template = level.getStructureManager().getOrCreate(ResourceLocation.parse(HavenSkyblockBuilder.MOD_ID + ":" + islandTemplate));
        }
        else
        {
            File structureFile = new File("config/HavenSkyblockBuilder/Templates", islandTemplate + ".nbt");
            File parentDirectory = structureFile.getParentFile();
            Path structurePath = parentDirectory.toPath().resolve(structureFile.getName());

            try
            {
                CompoundTag nbtData = NbtIo.readCompressed(structurePath, NbtAccounter.unlimitedHeap());

                template = new StructureTemplate();
                HolderGetter<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK).asLookup();
                template.load(blockRegistry, nbtData);

                StructurePlaceSettings settings = new StructurePlaceSettings();
                template.placeInWorld(level, position, position, settings, null, 3);
            } catch (Exception e)
            {
                HavenSkyblockBuilder.Log("Error loading custom structure: " + e.getMessage());
                throw new RuntimeException("Failed to load structure from custom path: " + structurePath, e);
            }
        }

        return template;
    }

    private static BlockPos findNearestValidBlock(ServerLevel level, BlockPos basePos)
    {
        int searchRadius = 50;
        BlockPos bestPos = null;
        int highestY = Integer.MIN_VALUE;

        for (int dx = -searchRadius; dx <= searchRadius; dx++)
        {
            for (int dz = -searchRadius; dz <= searchRadius; dz++)
            {
                BlockPos checkPos = basePos.offset(dx, 0, dz);
                BlockPos validPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, checkPos).above();

                // Check if the position is valid for teleportation
                if (level.isEmptyBlock(validPos) && level.isEmptyBlock(validPos.above()))
                {
                    BlockPos blockBelow = validPos.below(); // Block the player will stand on
                    var blockState = level.getBlockState(blockBelow);

                    // Exclude blocks you don't want to spawn on
                    if (blockState.is(Blocks.WATER) ||
                            blockState.is(Blocks.CHEST) ||
                            blockState.is(BlockTags.LOGS) ||
                            blockState.is(BlockTags.LEAVES))
                    {
                        continue;
                    }

                    if (validPos.getY() > highestY)
                    {
                        highestY = validPos.getY();
                        bestPos = validPos;
                    }
                }
            }
        }

        return bestPos != null ? bestPos : basePos;
    }

    private static BlockPos determineSpawnPosition(ServerLevel level, StructureTemplate template, BlockPos basePos, List<String> offsetList)
    {
        Vec3i size = template.getSize();

        // Calculate center position
        int centerX = basePos.getX() + size.getX() / 2;
        int centerY = basePos.getY() + size.getY() / 2;
        int centerZ = basePos.getZ() + size.getZ() / 2;

        // Parse offset from config
        int[] offset = parseSpawnOffset(offsetList);
        int offsetX = offset[0];
        int offsetY = offset[1];
        int offsetZ = offset[2];

        // Apply offset to center position and find the topmost block at that location
        BlockPos offsetPos = new BlockPos(centerX + offsetX, centerY + offsetY, centerZ + offsetZ);
        BlockPos topMostOffsetPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offsetPos);

        // Check if the topmost offset position is valid
        if (isValidSpawnPosition(level, topMostOffsetPos))
        {
            return topMostOffsetPos;
        }

        // Fallback to finding the nearest valid block
        return findNearestValidBlock(level, offsetPos);
    }

    private static boolean isValidSpawnPosition(ServerLevel level, BlockPos position)
    {
        // Check if the position is valid for teleportation
        if (!level.isEmptyBlock(position) || !level.isEmptyBlock(position.above()))
        {
            return false;
        }

        BlockPos blockBelow = position.below();
        var blockState = level.getBlockState(blockBelow);

        // Ensure the block below is suitable for standing on
        return !(blockState.is(Blocks.WATER) ||
                blockState.is(Blocks.CHEST) ||
                blockState.is(BlockTags.LOGS) ||
                blockState.is(BlockTags.LEAVES));
    }

    public static int[] parseSpawnOffset(List<? extends String> offset)
    {
        if (offset.size() != 3)
        {
            throw new IllegalArgumentException("Invalid spawn_offset format. Expected three values.");
        }
        try
        {
            int x = Integer.parseInt(offset.get(0).trim());
            int y = Integer.parseInt(offset.get(1).trim());
            int z = Integer.parseInt(offset.get(2).trim());
            return new int[]{x, y, z};
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid spawn_offset values. Must be integers.", e);
        }
    }

    public static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String teamName = context.getArgument("name", String.class);
        String islandTemplate = context.getArgument("template", String.class);

        if (TeamManager.getAllTeams().stream().anyMatch(team -> team.getMembers().stream().anyMatch(member -> member.getUuid().equals(player.getUUID()))))
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.already_in_team"));
            return 0;
        }

        if (TeamManager.getAllTeams().stream().anyMatch(team -> team.getName().equalsIgnoreCase(teamName)))
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.name_exists"));
            return 0;
        }

        // Determine the next available island position
        int islandOffset = HavenConfig.islandDistance;
        BlockPos basePosition = TeamManager.findNextAvailableIslandPosition(islandOffset);

        if (basePosition == null)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.island_error"));
            return 0;
        }

        // Create the island
        ServerLevel level = context.getSource().getLevel();
        StructureTemplate template = SkyblockUtils.spawnStructure(level, basePosition, islandTemplate);

        // Determine the initial spawn position
        BlockPos spawnPosition = SkyblockUtils.determineSpawnPosition(level, template, basePosition, new ArrayList<>(HavenConfig.SpawnOffset));

        // Create the team and set the home position
        Team team = new Team(teamName, player.getUUID(), spawnPosition);
        team.addMember(player.getUUID(), player.getName().getString());
        TeamManager.addTeam(level.getServer(), team);

        // Teleport the player to the initial spawn position
        player.teleportTo(level, spawnPosition.getX() + 0.5, spawnPosition.getY(), spawnPosition.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.creation_success", teamName));

        return 1;
    }

    public static int leaveTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getMembers().stream().anyMatch(member -> member.getUuid().equals(player.getUUID())))
                .findFirst();

        if (optionalTeam.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        Team team = optionalTeam.get();
        team.removeMember(player.getUUID());
        TeamManager.saveTeam(level.getServer(), team);

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.leave_success", team.getName()));

        if (team.getMembers().isEmpty())
        {
            TeamManager.removeTeam(level.getServer(), team.getUuid());
            context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.disband_success", team.getName()), true);
        }
        else
        {
            if (team.getLeader().equals(player.getUUID()))
            {
                team.setLeader(team.getMembers().get(0).getUuid());
                ServerPlayer newLeader = level.getServer().getPlayerList().getPlayer(team.getLeader());
                if (newLeader != null)
                    newLeader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.new_leader", team.getName()));
                context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.leader_change", team.getLeaderName()), true);
            }
        }
        return 1;
    }

    public static int disbandTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getLeader().equals(player.getUUID()))
                .findFirst();

        if (optionalTeam.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        Team team = optionalTeam.get();
        team.getMembers().forEach(member ->
        {
            ServerPlayer memberPlayer = level.getServer().getPlayerList().getPlayer(member.getUuid());
            if (memberPlayer != null && memberPlayer != player)
            {
                // Convert BlockPos to Double for getSharedSpawnPos
                BlockPos spawnPos = level.getSharedSpawnPos();
                memberPlayer.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                team.removeMember(member.getUuid());
                memberPlayer.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.disband_leave", team.getMember(player.getUUID())));
            }
        });
        TeamManager.removeTeam(level.getServer(), team.getUuid());
        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.disband_success", team.getName()), true);
        return 1;
    }

    public static int goHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getMembers().stream().anyMatch(member -> member.getUuid().equals(player.getUUID())))
                .findFirst();

        if (optionalTeam.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        Team team = optionalTeam.get();
        BlockPos home = team.getHomePosition();
        Vec2 homeRotation = team.getHomeRotation();

        if (home == null)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.no_home"));
            return 0;
        }

        player.teleportTo(home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5);
        player.absRotateTo(homeRotation.y, homeRotation.x);
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.home_teleport"));
        return 1;
    }

    public static int setHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getLeader().equals(player.getUUID()))
                .findFirst();

        if (optionalTeam.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        Team team = optionalTeam.get();
        BlockPos homePosition = player.blockPosition();
        Vec2 homeRotation = player.getRotationVector();
        team.setHomePosition(homePosition, homeRotation);
        TeamManager.saveTeam(level.getServer(), team);

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.home_set", homePosition.toShortString()));
        return 1;
    }

    public static int invitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer inviter = context.getSource().getPlayerOrException();
        String playerName = context.getArgument("player", String.class);

        ServerPlayer invitee = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (invitee == null) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.player_not_online", playerName));
            return 0;
        }

        Team inviterTeam = TeamManager.getTeamByPlayer(inviter.getUUID());

        if (inviterTeam == null) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        if (TeamManager.getTeamByPlayer(invitee.getUUID()) != null) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.already_in_team", playerName));
            return 0;
        }

        if (TeamManager.getPendingInvite(invitee.getUUID()) != null && !TeamManager.isInviteExpired(invitee.getUUID())) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.invite_already_sent", playerName));
            return 0;
        }

        TeamManager.addPendingInvite(invitee.getUUID(), inviterTeam.getUuid());
        inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.invite_sent", playerName));
        invitee.sendSystemMessage(
                Component.translatable("haven_skyblock_builder.team.invite_received", inviter.getName().getString())
                        .append(" ")
                        .append(
                                Component.literal("[Accept]")
                                        .withStyle(style -> style
                                                .withColor(ChatFormatting.GREEN)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/havensb team accept"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to accept the invite")))))
                        .append(" ")
                        .append(
                                Component.literal("[Deny]")
                                        .withStyle(style -> style
                                                .withColor(ChatFormatting.RED)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/havensb team deny"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to deny the invite")))))
        );

        return 1;
    }

    public static int acceptInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer invitee = context.getSource().getPlayerOrException();
        UUID teamId = TeamManager.getPendingInvite(invitee.getUUID());

        if (teamId == null || TeamManager.isInviteExpired(invitee.getUUID())) {
            invitee.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.no_invites"));
            TeamManager.removePendingInvite(invitee.getUUID());
            return 0;
        }

        Team team = TeamManager.getTeamById(teamId);

        if (team == null) {
            invitee.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.team_not_found"));
            TeamManager.removePendingInvite(invitee.getUUID());
            return 0;
        }

        team.addMember(invitee.getUUID(), invitee.getName().getString());
        TeamManager.removePendingInvite(invitee.getUUID());
        invitee.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.join_success", team.getName()));
        return 1;
    }

    public static int denyInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer invitee = context.getSource().getPlayerOrException();
        UUID teamId = TeamManager.getPendingInvite(invitee.getUUID());

        if (teamId == null || TeamManager.isInviteExpired(invitee.getUUID())) {
            invitee.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.no_invites"));
            TeamManager.removePendingInvite(invitee.getUUID());
            return 0;
        }

        TeamManager.removePendingInvite(invitee.getUUID());
        invitee.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.invite_declined"));
        return 1;
    }

    public static int kickPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        String playerName = context.getArgument("player", String.class);

        Team team = TeamManager.getTeamByPlayer(leader.getUUID());

        if (team == null || !team.getLeader().equals(leader.getUUID()))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        ServerPlayer toKick = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (toKick == null || team.getMembers().stream().noneMatch(member -> member.getUuid().equals(toKick.getUUID())))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.member_not_found", playerName));
            return 0;
        }

        team.removeMember(toKick.getUUID());
        leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.kick_success", playerName));
        toKick.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.kicked", team.getName()));
        return 1;
    }

    public static int transferLeadership(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        String playerName = context.getArgument("player", String.class);

        Team team = TeamManager.getTeamByPlayer(leader.getUUID());

        if (team == null || !team.getLeader().equals(leader.getUUID()))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        ServerPlayer newLeader = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (newLeader == null || team.getMembers().stream().noneMatch(member -> member.getUuid().equals(newLeader.getUUID())))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.member_not_found", playerName));
            return 0;
        }

        team.setLeader(newLeader.getUUID());
        leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.transfer_success", playerName));
        newLeader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.new_leader", team.getName()));
        return 1;
    }

    public static int listTeams(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<Team> teams = new ArrayList<>(TeamManager.getAllTeams());

        if (teams.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.no_teams"));
            return 0;
        }

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.list_header"));
        teams.sort(Comparator.comparing(Team::getName));
        teams.forEach(team -> player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.list_entry", team.getName(), team.getLeaderName(), team.getMembers().size())));
        return 1;
    }

    // Admin Add Member to the team
    public static int addMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        String teamName = context.getArgument("team", String.class);
        String playerName = context.getArgument("player", String.class);

        Team team = TeamManager.getAllTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found"));
            return 0;
        }

        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.player_not_found"));
            return 0;
        }

        team.addMember(player.getUUID(), player.getName().getString());
        TeamManager.saveTeam(level.getServer(), team);

        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.member_added", playerName, teamName), true);
        return 1;
    }

    // Admin Remove Member from the team
    public static int removeMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        String teamName = context.getArgument("team", String.class);
        String playerName = context.getArgument("player", String.class);

        Team team = TeamManager.getAllTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found"));
            return 0;
        }

        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.player_not_found"));
            return 0;
        }

        team.removeMember(player.getUUID());
        TeamManager.saveTeam(level.getServer(), team);

        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.member_removed", playerName, teamName), true);
        return 1;
    }

    // Admin Change Team Name
    public static int changeName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        String teamName = context.getArgument("team", String.class);
        String newName = context.getArgument("name", String.class);

        Team team = TeamManager.getAllTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found"));
            return 0;
        }

        team.setName(newName);
        TeamManager.saveTeam(level.getServer(), team);

        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.name_changed", newName), true);
        return 1;
    }
}
