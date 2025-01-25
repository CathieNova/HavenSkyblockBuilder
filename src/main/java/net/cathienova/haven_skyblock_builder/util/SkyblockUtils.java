package net.cathienova.haven_skyblock_builder.util;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SkyblockUtils
{
    public static BlockPos findNearestValidBlock(ServerLevel level, BlockPos basePos) {
        int maxSearchRadius = 25;
        for (int radius = 0; radius <= maxSearchRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }
                    BlockPos checkPos = basePos.offset(x, 0, z);
                    BlockPos validPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, checkPos).above();

                    if (level.isEmptyBlock(validPos) && level.isEmptyBlock(validPos.above())) {
                        if (isValidSpawnPosition(level, validPos)) {
                            continue;
                        }

                        return validPos;
                    }
                }
            }
        }
        return basePos;
    }

    private static BlockPos determineSpawnPosition(ServerLevel level, StructureTemplate template, BlockPos basePos, List<String> offsetList)
    {
        Vec3i size = template.getSize();

        // Calculate center position
        int centerX = basePos.getX() + size.getX() / 2;
        int centerY = basePos.getY() + size.getY() / 2;
        int centerZ = basePos.getZ() + size.getZ() / 2;

        // Parse offset from config
        BlockPos offset = parseConfigPosition(offsetList);

        // Calculate the adjusted position using offset
        BlockPos adjustedPos = new BlockPos(centerX + offset.getX(), centerY + offset.getY(), centerZ + offset.getZ());

        // Validate adjusted position, fall back to nearest valid if necessary
        if (isValidSpawnPosition(level, adjustedPos))
        {
            return adjustedPos;
        }

        return findNearestValidBlock(level, adjustedPos);
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
        return !(blockState.is(Blocks.WATER) || blockState.is(Blocks.CHEST) || blockState.is(BlockTags.LOGS) || blockState.is(BlockTags.LEAVES));
    }

    public static BlockPos parseConfigPosition(List<? extends String> position)
    {
        if (position.size() != 3)
        {
            throw new IllegalArgumentException("Invalid spawn_offset format. Expected three values.");
        }
        try
        {
            int x = Integer.parseInt(position.get(0).trim());
            int y = Integer.parseInt(position.get(1).trim());
            int z = Integer.parseInt(position.get(2).trim());
            return new BlockPos(x, y, z);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid spawn_offset values. Must be integers.", e);
        }
    }

    public static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!CooldownManager.canUseIsland(player, HavenConfig.islandCooldown) && HavenConfig.islandCooldown > 0)
        {
            return 0;
        }
        ServerLevel level = context.getSource().getLevel();
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
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.finding_island_location"));
        BlockPos basePosition = TeamManager.findNextAvailableIslandPosition(level);

        if (basePosition == null)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.island_error"));
            return 0;
        }

        // Create the island
        StructureTemplate template = StructureUtils.generateMainIsland(level, basePosition, islandTemplate);

        // Determine the initial spawn position
        BlockPos spawnPosition = determineSpawnPosition(level, template, basePosition, new ArrayList<>(HavenConfig.SpawnOffset));

        // Create the team and set the home position
        Team team = new Team(teamName, player.getUUID(), true, spawnPosition, new Vec2(player.getYRot(), player.getXRot()));
        team.addMember(player.getUUID(), player.getName().getString());
        TeamManager.addTeam(level.getServer(), team);

        // Teleport the player to the initial spawn position
        player.teleportTo(level, spawnPosition.getX() + 0.5, spawnPosition.getY() + 1, spawnPosition.getZ() + 0.5, 0, 0);
        CooldownManager.setIslandCooldown(player);
        player.resetFallDistance();
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
        if (!HavenConfig.keepInventoryOnIslandLeave)
            player.getInventory().clearContent();
        team.removeMember(player.getUUID());
        TeamManager.saveTeam(level.getServer(), team);

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.leave_success", team.getName()));

        if (team.getMembers().isEmpty())
        {
            String oldName = team.getName();
            team.setName(team.getName() + " (disbanded)");
            TeamManager.saveTeam(level.getServer(), team);
            //TeamManager.removeTeam(level.getServer(), team.getUuid());
            context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.disband_success", oldName), true);
            BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
            player.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            //IslandManager.deleteIslandArea(level, team.getHomePosition());
            return 1;
        }
        else
        {
            if (team.getLeader().equals(player.getUUID()))
            {
                team.setLeader(team.getMembers().getFirst().getUuid());
                ServerPlayer newLeader = level.getServer().getPlayerList().getPlayer(team.getLeader());
                if (newLeader != null)
                    newLeader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.new_leader", team.getName()));
                context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.leader_change", team.getLeaderName()), true);
                return 1;
            }
        }
        return 1;
    }

    public static int disbandTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getLeader().equals(player.getUUID()))
                .findFirst();

        if (optionalTeam.isEmpty()) {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        Team team = optionalTeam.get();
        BlockPos homePosition = team.getHomePosition();

        List<Team.Member> members = new ArrayList<>(team.getMembers());
        for (Team.Member member : members) {
            ServerPlayer memberPlayer = level.getServer().getPlayerList().getPlayer(member.getUuid());
            if (memberPlayer != null) {
                try {
                    BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
                    memberPlayer.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
                    memberPlayer.resetFallDistance();
                    if (!HavenConfig.keepInventoryOnIslandLeave)
                        memberPlayer.getInventory().clearContent();
                    team.removeMember(member.getUuid());
                    memberPlayer.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.disband_leave", team.getName()));
                } catch (Exception e) {
                    HavenSkyblockBuilder.Log("Error disbanding team: " + e.getMessage());
                }
            }
        }
        String oldName = team.getName();
        team.setName(team.getName() + " (disbanded)");
        TeamManager.saveTeam(level.getServer(), team);
        //TeamManager.removeTeam(level.getServer(), team.getUuid());
        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.disband_success", oldName), true);
        //IslandManager.deleteIslandArea(level, homePosition);
        return 1;
    }

    public static int goHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!CooldownManager.canUseHome(player, HavenConfig.homeCooldown) && HavenConfig.homeCooldown > 0)
        {
            return 0;
        }

        ServerLevel level = context.getSource().getLevel();
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

        ServerLevel overworld = context.getSource().getServer().getLevel(ServerLevel.OVERWORLD);
        assert overworld != null;

        player.teleportTo(overworld, home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5, homeRotation.y, homeRotation.x);
        CooldownManager.setHomeCooldown(player);
        player.resetFallDistance();
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.home_teleport"));
        return 1;
    }

    public static int setHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel overworld = context.getSource().getServer().getLevel(ServerLevel.OVERWORLD);
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (player.level() != overworld)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.island.not_in_overworld"));
            return 0;
        }
            else
        {

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
    }

    public static int visitIsland(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!CooldownManager.canUseVisit(player, HavenConfig.visitCooldown) && HavenConfig.visitCooldown > 0)
        {
            return 0;
        }
        ServerLevel overworld = context.getSource().getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.error.teleport_failed"));
            return 0;
        }

        String teamName = context.getArgument("team", String.class);

        // Find the team by name
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst();

        if (optionalTeam.isEmpty()) {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_found", teamName));
            return 0;
        }

        Team team = optionalTeam.get();
        if (!team.isAllowVisit()) {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.visit_not_allowed", team.getName()));
            return 0;
        }

        BlockPos home = team.getHomePosition();
        if (home == null) {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.no_home"));
            return 0;
        }

        if (team.getMembers().stream().anyMatch(member -> member.getUuid().equals(player.getUUID()))) {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.can_not_visit_own_island"));
            return 0;
        }

        Vec2 homeRotation = team.getHomeRotation();
        if (homeRotation == null)
            homeRotation = new Vec2(0, 0);

        try {
            player.teleportTo(overworld, home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5, homeRotation.y, homeRotation.x);
            CooldownManager.setVisitCooldown(player);
            player.resetFallDistance();
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.visit_teleport", team.getName()));
        } catch (Exception e) {
            HavenSkyblockBuilder.Log("Error teleporting player to team home: " + e.getMessage());
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.error.teleport_failed"));
            return 0;
        }

        return 1;
    }

    public static int goSpawn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!CooldownManager.canUseSpawn(player, HavenConfig.spawnCooldown) && HavenConfig.spawnCooldown > 0)
        {
            return 0;
        }

        BlockPos home = parseConfigPosition(HavenConfig.spawnPosition);

        ServerLevel overworld = context.getSource().getServer().getLevel(ServerLevel.OVERWORLD);
        assert overworld != null;

        player.teleportTo(overworld, home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5, 0, 0);
        CooldownManager.setSpawnCooldown(player);
        player.resetFallDistance();
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.island.spawn_teleport"));
        return 1;
    }

    public static int setAllowVisit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
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
        boolean allowVisit = BoolArgumentType.getBool(context, "allow");
        team.setAllowVisit(allowVisit);
        TeamManager.saveTeam(level.getServer(), team);

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.visit_" + (allowVisit ? "enabled" : "disabled")));
        return 1;
    }

    public static int invitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer inviter = context.getSource().getPlayerOrException();
        ServerPlayer invitee = EntityArgument.getPlayer(context, "player");

        Team inviterTeam = TeamManager.getTeamByPlayer(inviter.getUUID());

        if (inviterTeam == null) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        if (TeamManager.getTeamByPlayer(invitee.getUUID()) != null) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.already_in_team", invitee.getName().getString()));
            return 0;
        }

        if (TeamManager.getPendingInvite(invitee.getUUID()) != null && !TeamManager.isInviteExpired(invitee.getUUID())) {
            inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.invite_already_sent", invitee.getName().getString()));
            return 0;
        }

        TeamManager.addPendingInvite(invitee.getUUID(), inviterTeam.getUuid());
        inviter.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.invite_sent", invitee.getName().getString()));
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
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer toKick = EntityArgument.getPlayer(context, "player");

        if (toKick.getUUID() == leader.getUUID())
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.cannot_kick_self"));
            return 0;
        }

        Team team = TeamManager.getTeamByPlayer(leader.getUUID());


        if (team == null || !team.getLeader().equals(leader.getUUID()))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        if (team.getLeader() == toKick.getUUID())
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.cannot_kick_leader"));
            return 0;
        }

        if (team.getMembers().stream().noneMatch(member -> member.getUuid().equals(toKick.getUUID())))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.member_not_found", toKick.getName().getString()));
            return 0;
        }

        try {
            team.removeMember(toKick.getUUID());
            BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
            toKick.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.kick_success", toKick.getName().getString()));
            toKick.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.kicked", team.getName()));
            return 1;
        } catch (Exception e) {
            HavenSkyblockBuilder.Log("Error kicking player from team: " + e.getMessage());
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.error.kick_failed"));
            return 0;
        }
    }

    public static int deportPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");

        Team team = TeamManager.getTeamByPlayer(leader.getUUID());

        if (team == null) {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        if (team.getMembers().stream().anyMatch(member -> member.getUuid().equals(targetPlayer.getUUID()))) {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.cannot_deport_team_member"));
            return 0;
        }

        double distance = targetPlayer.position().distanceTo(Vec3.atCenterOf(team.getHomePosition()));
        if (distance > HavenConfig.islandDistance / 5.0) {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_near_island"));
            return 0;
        }

        Team targetTeam = TeamManager.getTeamByPlayer(targetPlayer.getUUID());
        if (targetTeam != null) {
            BlockPos targetHome = targetTeam.getHomePosition();
            targetPlayer.teleportTo(level, targetHome.getX() + 0.5, targetHome.getY(), targetHome.getZ() + 0.5, 0, 0);
            targetPlayer.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.booted_to_own_island"));
        } else {
            BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
            targetPlayer.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            targetPlayer.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.booted_to_spawn"));
        }

        leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.boot_success", targetPlayer.getName().getString()));
        return 1;
    }

    public static int changeTeamName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        String newName = context.getArgument("name", String.class);

        Team team = TeamManager.getTeamByPlayer(player.getUUID());

        if (team == null)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        if (TeamManager.getAllTeams().stream().anyMatch(t -> t.getName().equalsIgnoreCase(newName)))
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.name_exists"));
            return 0;
        }

        team.setName(newName);
        TeamManager.saveTeam(level.getServer(), team);
        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.name_changed", newName), true);
        return 1;
    }

    public static int transferLeadership(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        ServerPlayer newLeader = EntityArgument.getPlayer(context, "player");

        Team team = TeamManager.getTeamByPlayer(leader.getUUID());

        if (team == null)
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        if (!team.getLeader().equals(leader.getUUID()))
        {
            leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_leader"));
            return 0;
        }

        team.setLeader(newLeader.getUUID());
        leader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.transfer_success", newLeader.getName().getString()));
        newLeader.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.new_leader", team.getName()));
        return 1;
    }

    public static int listTeams(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<Team> teams = new ArrayList<>(TeamManager.getAllTeams());

        teams.removeIf(team -> team.getName().contains("disbanded"));

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
        ServerPlayer playerName = EntityArgument.getPlayer(context, "player");

        Team team = TeamManager.getAllTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found"));
            return 0;
        }

        team.addMember(playerName.getUUID(), playerName.getName().getString());
        TeamManager.saveTeam(level.getServer(), team);

        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.member_added", playerName, teamName), true);
        return 1;
    }

    // Admin Remove Member from the team
    public static int removeMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerLevel level = context.getSource().getLevel();
        String teamName = context.getArgument("team", String.class);
        ServerPlayer playerName = EntityArgument.getPlayer(context, "player");

        Team team = TeamManager.getAllTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found"));
            return 0;
        }

        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName.getName().getString());

        if (player == null)
        {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.player_not_found"));
            return 0;
        }

        team.removeMember(player.getUUID());
        if (team.getLeader().equals(player.getUUID()))
        {
            if (team.getMembers().isEmpty())
            {
                String oldName = team.getName();
                team.setName(team.getName() + " (disbanded)");
                TeamManager.saveTeam(level.getServer(), team);
                //TeamManager.removeTeam(level.getServer(), team.getUuid());
                BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
                player.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
                context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.disband_success", oldName), true);

                //IslandManager.deleteIslandArea(level, team.getHomePosition());
                return 1;
            }

            team.setLeader(team.getMembers().getFirst().getUuid());
            context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.leader_change", team.getLeaderName()), true);
        }
        TeamManager.saveTeam(level.getServer(), team);

        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.member_removed", playerName, teamName), true);
        return 1;
    }

    public static int adminChangeTeamName(CommandContext<CommandSourceStack> context)
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

    public static int islandInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = TeamManager.getTeamByPlayer(player.getUUID());

        if (team == null)
        {
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.not_in_team"));
            return 0;
        }

        String teamMembers = team.getMembers().stream().map(Team.Member::getName).reduce((a, b) -> a + ", " + b).orElse("");
        String allowVisit = team.isAllowVisit() ? "true" : "false";

        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.island_information",
                team.getName(), team.getLeaderName(), allowVisit, team.getHomePosition().toShortString(), teamMembers));
        return 1;
    }

    public static int adminRemoveTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        ServerPlayer admin = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        String teamName = context.getArgument("team", String.class);

        // Retrieve the team safely
        Optional<Team> optionalTeam = TeamManager.getAllTeams().stream()
                .filter(t -> t != null && t.getName() != null && t.getName().equalsIgnoreCase(teamName))
                .findFirst();

        if (optionalTeam.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.team.not_found", teamName));
            return 0;
        }

        Team team = optionalTeam.get();

        team.getMembers().forEach(member -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(member.getUuid());
            ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
            if (player != null) {
                BlockPos spawn = parseConfigPosition(HavenConfig.spawnPosition);
                player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
                if (!HavenConfig.keepInventoryOnIslandLeave)
                    player.getInventory().clearContent();
                player.resetFallDistance();
                player.sendSystemMessage(Component.translatable("haven_skyblock_builder.team.disband_leave", admin.getName().getString()));
            }
        });

        TeamManager.removeTeam(level.getServer(), team.getUuid());
        context.getSource().sendSuccess(() -> Component.translatable("haven_skyblock_builder.team.remove_success", teamName), true);
        return 1;
    }

    public static int adminListTeams(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
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
        teams.forEach(team -> player.sendSystemMessage(Component.translatable("haven_skyblock_builder.admin.list_entry", team.getName())));
        return 1;
    }
}
