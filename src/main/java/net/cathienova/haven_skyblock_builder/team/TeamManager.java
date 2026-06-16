package net.cathienova.haven_skyblock_builder.team;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.util.CooldownManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
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
        teams.clear();
        pendingInvites.clear();
        inviteExpiry.clear();
        CooldownManager.ClearCooldowns();

        if (!teamFolder.exists()) {
            teamFolder.mkdirs();
            return;
        }

        File[] files = teamFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Team team = GSON.fromJson(reader, Team.class);
                if (team == null) {
                    continue;
                }

                team.repairMissingData(file.lastModified() > 0 ? file.lastModified() : System.currentTimeMillis());
                if (team.isDisbanded() && HavenConfig.removeDisbandedTeams) {
                    file.delete();
                    continue;
                }
                teams.put(team.getUuid(), team);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveTeam(MinecraftServer server, Team team) {
        if (team.isDisbanded() && HavenConfig.removeDisbandedTeams) {
            deleteTeam(server, team.getUuid());
            return;
        }

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
        File file = new File(getTeamFolder(server), teamId + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void addTeam(MinecraftServer server, Team team) {
        teams.put(team.getUuid(), team);
        saveTeam(server, team);
        removeJoinRequestsForPlayer(server, team.getLeader());
    }

    public static void removeTeam(MinecraftServer server, UUID teamId) {
        deleteTeam(server, teamId);
    }

    public static void disbandTeam(MinecraftServer server, Team team) {
        team.clearMembers();
        team.markDisbanded();
        if (HavenConfig.removeDisbandedTeams) {
            removeTeam(server, team.getUuid());
        } else {
            saveTeam(server, team);
        }
    }

    public static Collection<Team> getAllTeams() {
        return teams.values();
    }

    public static void addPendingInvite(UUID inviteeUuid, UUID teamId) {
        pendingInvites.put(inviteeUuid, teamId);
        inviteExpiry.put(inviteeUuid, System.currentTimeMillis() + 60 * 1000);
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
        if (!pendingInvites.containsKey(inviteeUuid)) {
            return null;
        }

        if (isInviteExpired(inviteeUuid)) {
            removePendingInvite(inviteeUuid);
            return null;
        }
        return pendingInvites.get(inviteeUuid);
    }

    public static Team getTeamByPlayer(UUID playerUuid) {
        return teams.values().stream()
                .filter(team -> !team.isDisbanded())
                .filter(team -> team.getMembers().stream().anyMatch(member -> member.getUuid().equals(playerUuid)))
                .findFirst()
                .orElse(null);
    }

    public static Team getTeamById(UUID teamId) {
        return teams.get(teamId);
    }

    public static void removeJoinRequestsForPlayer(MinecraftServer server, UUID playerUuid) {
        for (Team team : java.util.List.copyOf(teams.values())) {
            int requestCount = team.getJoinRequests().size();
            team.removeJoinRequest(playerUuid);
            if (team.getJoinRequests().size() != requestCount) {
                saveTeam(server, team);
            }
        }
    }

    public static void removeInvalidJoinRequests(MinecraftServer server, Team team) {
        boolean changed = false;
        for (Team.JoinRequest request : java.util.List.copyOf(team.getJoinRequests())) {
            if (getTeamByPlayer(request.getUuid()) != null) {
                team.removeJoinRequest(request.getUuid());
                changed = true;
            }
        }
        if (changed) {
            saveTeam(server, team);
        }
    }

    public static BlockPos findNextAvailableIslandPosition(ServerLevel level) {
        int layer = 1;
        int islandDistance = HavenConfig.islandDistance;

        while (true) {
            for (int x = -layer * islandDistance; x <= layer * islandDistance; x += islandDistance) {
                for (int z = -layer * islandDistance; z <= layer * islandDistance; z += islandDistance) {
                    if (Math.abs(x) != layer * islandDistance && Math.abs(z) != layer * islandDistance) {
                        continue;
                    }

                    BlockPos candidate = new BlockPos(x, 70, z);
                    if (candidate.equals(new BlockPos(0, 70, 0))) {
                        continue;
                    }

                    if (isPositionAvailable(candidate, islandDistance) && isBiomeAllowed(level, candidate)) {
                        return candidate;
                    }
                }
            }

            layer++;
            if (layer > 1000) {
                return null;
            }
        }
    }

    private static boolean isBiomeAllowed(ServerLevel level, BlockPos position) {
        ResourceKey<Biome> biomeKey = level.getBiome(position).unwrapKey().orElse(null);
        if (biomeKey == null) {
            return false;
        }

        String biomeName = biomeKey.identifier().toString();
        return !HavenConfig.blacklistBiomesForIslands.contains(biomeName);
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