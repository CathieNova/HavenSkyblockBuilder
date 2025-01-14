package net.cathienova.haven_skyblock_builder.team;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Team {
    private final UUID uuid;
    private String name;
    private UUID leader;
    private boolean allowVisit;
    private BlockPos homePosition;
    private Vec2 homeRotation;
    private final List<Member> members = new ArrayList<>();

    public Team(String name, UUID leader, boolean allowVisit, BlockPos homePosition, Vec2 homeRotation) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.leader = leader;
        this.allowVisit = allowVisit;
        this.homePosition = homePosition;
        this.homeRotation = homeRotation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeader() {
        return leader;
    }

    public String getLeaderName()
    {
        return Objects.requireNonNull(members.stream().filter(member -> member.getUuid().equals(leader)).findFirst().orElse(null)).getName();
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public boolean isAllowVisit() {
        return allowVisit;
    }

    public void setAllowVisit(boolean allowVisit) {
        this.allowVisit = allowVisit;
    }

    public BlockPos getHomePosition() {
        return homePosition;
    }

    public void setHomePosition(BlockPos homePosition, Vec2 homeRotation) {
        this.homePosition = homePosition;
        this.homeRotation = homeRotation;
    }

    public Vec2 getHomeRotation()
    {
        return homeRotation;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (Member member : members) {
            names.add(member.getName());
        }
        return names;
    }

    public Member getMember(UUID uuid) {
        return members.stream().filter(member -> member.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void addMember(UUID uuid, String name) {
        members.add(new Member(uuid, name));
    }

    public void removeMember(UUID uuid) {
        members.removeIf(member -> member.getUuid().equals(uuid));
    }

    public static class Member {
        private final UUID uuid;
        private final String name;

        public Member(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }
}
