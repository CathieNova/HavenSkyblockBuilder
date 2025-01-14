package net.cathienova.haven_skyblock_builder.team;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, Team> teams = new HashMap<>();
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();
    private static final Map<UUID, Long> inviteExpiry = new HashMap<>();

    private static File getTeamFolder(MinecraftServer server) {
        File worldFolder = server.getWorldPath(LevelResource.ROOT).toFile();
        return new File(worldFolder, "serverconfig/HavenSkyblockBuilder/Teams");
    }

    public static void loadAllTeams(MinecraftServer server) {
        File teamFolder = getTeamFolder(server);

        if (!teamFolder.exists()) {
            teamFolder.mkdirs();
            return;
        }

        File[] files = teamFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    Team team = GSON.fromJson(reader, Team.class);
                    teams.put(team.getUuid(), team);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveTeam(MinecraftServer server, Team team) {
        File teamFolder = getTeamFolder(server);

        if (!teamFolder.exists()) {
            teamFolder.mkdirs();
        }

        File file = new File(teamFolder, team.getUuid() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(team, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTeam(MinecraftServer server, UUID teamId) {
        teams.remove(teamId);
        File teamFolder = getTeamFolder(server);
        File file = new File(teamFolder, teamId + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void addTeam(MinecraftServer server, Team team) {
        teams.put(team.getUuid(), team);
        saveTeam(server, team);
    }

    public static void removeTeam(MinecraftServer server, UUID teamId) {
        deleteTeam(server, teamId);
    }

    public static Collection<Team> getAllTeams() {
        return teams.values();
    }

    public static void addPendingInvite(UUID inviteeUuid, UUID teamId) {
        pendingInvites.put(inviteeUuid, teamId);
        inviteExpiry.put(inviteeUuid, System.currentTimeMillis() + 60 * 1000); // 60 seconds
    }

    public static boolean isInviteExpired(UUID inviteeUuid) {
        Long expiryTime = inviteExpiry.get(inviteeUuid);
        return expiryTime != null && System.currentTimeMillis() > expiryTime;
    }

    public static void removePendingInvite(UUID inviteeUuid) {
        pendingInvites.remove(inviteeUuid);
        inviteExpiry.remove(inviteeUuid);
    }

    public static UUID getPendingInvite(UUID inviteeUuid) {
        if (pendingInvites.containsKey(inviteeUuid)) {
            if (isInviteExpired(inviteeUuid)) {
                removePendingInvite(inviteeUuid);
                return null;
            }
            return pendingInvites.get(inviteeUuid);
        }
        return null;
    }

    public static Team getTeamByPlayer(UUID playerUuid) {
        return teams.values().stream()
                .filter(team -> team.getMembers().stream().anyMatch(member -> member.getUuid().equals(playerUuid)))
                .findFirst()
                .orElse(null);
    }

    public static Team getTeamById(UUID teamId) {
        return teams.get(teamId);
    }

    public static BlockPos findNextAvailableIslandPosition(int offset) {
        int x = 0, z = 0;
        while (true) {
            BlockPos candidate = new BlockPos(x, 70, z);
            if (isPositionAvailable(candidate, HavenConfig.islandDistance)) {
                return candidate;
            }
            x += offset;
            if (x > 1000000) { // Prevent infinite loops
                x = 0;
                z += offset;
            }
            if (z > 1000000) {
                return null; // No available position found
            }
        }
    }

    private static boolean isPositionAvailable(BlockPos position, int radius) {
        return teams.values().stream()
                .map(Team::getHomePosition)
                .noneMatch(homePosition -> isWithinRadius(position, homePosition, radius));
    }

    private static boolean isWithinRadius(BlockPos pos1, BlockPos pos2, int radius) {
        double distance = Math.sqrt(pos1.distSqr(pos2));
        return distance <= radius;
    }

}
