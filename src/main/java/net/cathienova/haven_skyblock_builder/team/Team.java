package net.cathienova.haven_skyblock_builder.team;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private final UUID uuid;
    private String name;
    private UUID leader;
    private boolean allowVisit;
    private boolean allowJoinRequests;
    private boolean disbanded;
    private String islandTemplate;
    private BlockPos homePosition;
    private Vec2 homeRotation;
    private long createdAt;
    private long lastChangedAt;
    private List<Member> members = new ArrayList<>();
    private List<JoinRequest> joinRequests = new ArrayList<>();

    public Team(String name, UUID leader, boolean allowVisit, boolean allowJoinRequests, String islandTemplate, BlockPos homePosition, Vec2 homeRotation) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.leader = leader;
        this.allowVisit = allowVisit;
        this.allowJoinRequests = allowJoinRequests;
        this.islandTemplate = islandTemplate;
        this.homePosition = homePosition;
        this.homeRotation = homeRotation;
        this.createdAt = System.currentTimeMillis();
        this.lastChangedAt = this.createdAt;
    }

    public void repairMissingData(long fallbackTime) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }

        if (this.joinRequests == null) {
            this.joinRequests = new ArrayList<>();
        }

        if (this.islandTemplate == null || this.islandTemplate.isBlank()) {
            this.islandTemplate = "classic_island";
        }

        if (this.name != null && this.name.endsWith(" (disbanded)")) {
            this.name = this.name.substring(0, this.name.length() - " (disbanded)".length());
            this.disbanded = true;
        }

        if (this.createdAt <= 0) {
            this.createdAt = fallbackTime;
        }

        if (this.lastChangedAt <= 0) {
            this.lastChangedAt = this.createdAt;
        }
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.touch();
    }

    public UUID getLeader() {
        return this.leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        this.touch();
    }

    public String getLeaderName() {
        Member leaderMember = this.members.stream()
                .filter(member -> member.getUuid().equals(this.leader))
                .findFirst()
                .orElse(null);

        return leaderMember != null ? leaderMember.getName() : "Unknown";
    }

    public boolean isAllowVisit() {
        return this.allowVisit;
    }

    public void setAllowVisit(boolean allowVisit) {
        this.allowVisit = allowVisit;
        this.touch();
    }

    public boolean isAllowJoinRequests() {
        return this.allowJoinRequests;
    }

    public void setAllowJoinRequests(boolean allowJoinRequests) {
        this.allowJoinRequests = allowJoinRequests;
        this.touch();
    }

    public boolean isDisbanded() {
        return this.disbanded;
    }

    public void markDisbanded() {
        this.disbanded = true;
        this.joinRequests.clear();
        this.touch();
    }

    public String getIslandTemplate() {
        return this.islandTemplate;
    }

    public BlockPos getHomePosition() {
        return this.homePosition;
    }

    public void setHomePosition(BlockPos homePosition, Vec2 homeRotation) {
        this.homePosition = homePosition;
        this.homeRotation = homeRotation;
        this.touch();
    }

    public Vec2 getHomeRotation() {
        return this.homeRotation;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getLastChangedAt() {
        return this.lastChangedAt;
    }

    public List<Member> getMembers() {
        return this.members;
    }

    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (Member member : this.members) {
            names.add(member.getName());
        }
        return names;
    }

    public Member getMember(UUID uuid) {
        return this.members.stream().filter(member -> member.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void addMember(UUID uuid, String name) {
        if (this.getMember(uuid) != null) {
            return;
        }

        this.disbanded = false;
        this.members.add(new Member(uuid, name));
        this.joinRequests.removeIf(request -> request.getUuid().equals(uuid));
        this.touch();
    }

    public void removeMember(UUID uuid) {
        if (this.members.removeIf(member -> member.getUuid().equals(uuid))) {
            this.touch();
        }
    }

    public void clearMembers() {
        if (!this.members.isEmpty()) {
            this.members.clear();
            this.touch();
        }
    }

    public void updateMemberPermissions(
            UUID uuid,
            boolean canInvite,
            boolean canAcceptRequests,
            boolean canChangeSpawn,
            boolean canChangeVisits,
            boolean canChangeJoinRequests) {
        Member member = this.getMember(uuid);
        if (member == null || uuid.equals(this.leader)) {
            return;
        }

        member.setCanInvite(canInvite);
        member.setCanAcceptRequests(canAcceptRequests);
        member.setCanChangeSpawn(canChangeSpawn);
        member.setCanChangeVisits(canChangeVisits);
        member.setCanChangeJoinRequests(canChangeJoinRequests);
        this.touch();
    }

    public List<JoinRequest> getJoinRequests() {
        return this.joinRequests;
    }

    public boolean addJoinRequest(UUID uuid, String name) {
        if (this.disbanded || !this.allowJoinRequests || this.joinRequests.stream().anyMatch(request -> request.getUuid().equals(uuid))) {
            return false;
        }

        this.joinRequests.add(new JoinRequest(uuid, name, System.currentTimeMillis()));
        this.touch();
        return true;
    }

    public void removeJoinRequest(UUID uuid) {
        if (this.joinRequests.removeIf(request -> request.getUuid().equals(uuid))) {
            this.touch();
        }
    }

    private void touch() {
        this.lastChangedAt = System.currentTimeMillis();
    }

    public static class Member {
        private final UUID uuid;
        private final String name;
        private boolean canInvite;
        private boolean canAcceptRequests;
        private boolean canChangeSpawn;
        private boolean canChangeVisits;
        private boolean canChangeJoinRequests;

        public Member(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }

        public boolean canInvite() {
            return this.canInvite;
        }

        public void setCanInvite(boolean canInvite) {
            this.canInvite = canInvite;
        }

        public boolean canAcceptRequests() {
            return this.canAcceptRequests;
        }

        public void setCanAcceptRequests(boolean canAcceptRequests) {
            this.canAcceptRequests = canAcceptRequests;
        }

        public boolean canChangeSpawn() {
            return this.canChangeSpawn;
        }

        public void setCanChangeSpawn(boolean canChangeSpawn) {
            this.canChangeSpawn = canChangeSpawn;
        }

        public boolean canChangeVisits() {
            return this.canChangeVisits;
        }

        public void setCanChangeVisits(boolean canChangeVisits) {
            this.canChangeVisits = canChangeVisits;
        }

        public boolean canChangeJoinRequests() {
            return this.canChangeJoinRequests;
        }

        public void setCanChangeJoinRequests(boolean canChangeJoinRequests) {
            this.canChangeJoinRequests = canChangeJoinRequests;
        }
    }

    public static class JoinRequest {
        private final UUID uuid;
        private final String name;
        private final long requestedAt;

        public JoinRequest(UUID uuid, String name, long requestedAt) {
            this.uuid = uuid;
            this.name = name;
            this.requestedAt = requestedAt;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }

        public long getRequestedAt() {
            return this.requestedAt;
        }
    }
}