package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private static boolean canUseCommand(ServerPlayer player, String cooldownType, int cooldownTime) {
        long currentTime = System.currentTimeMillis();

        cooldowns.putIfAbsent(player.getUUID(), new ConcurrentHashMap<>());
        ConcurrentHashMap<String, Long> playerCooldowns = cooldowns.get(player.getUUID());

        long lastUsed = playerCooldowns.getOrDefault(cooldownType, 0L);

        if (currentTime - lastUsed < cooldownTime * 1000L) {
            long remainingTime = (cooldownTime * 1000L - (currentTime - lastUsed)) / 1000L;
            player.sendSystemMessage(Component.translatable("haven_skyblock_builder.cooldown_message", remainingTime));
            return false;
        }
        return true;
    }

    private static void setCooldown(ServerPlayer player, String cooldownType, int cooldownTime) {
        cooldowns.putIfAbsent(player.getUUID(), new ConcurrentHashMap<>());

        long cooldownExpiry = System.currentTimeMillis() + cooldownTime * 1000L;
        cooldowns.get(player.getUUID()).put(cooldownType, cooldownExpiry);
    }

    public static boolean canUseHome(ServerPlayer player, int cooldownTime) {
        return canUseCommand(player, "homeCooldown", cooldownTime);
    }

    public static void setHomeCooldown(ServerPlayer player) {
        setCooldown(player, "homeCooldown", HavenConfig.homeCooldown);
    }

    public static boolean canUseSpawn(ServerPlayer player, int cooldownTime) {
        return canUseCommand(player, "spawnCooldown", cooldownTime);
    }

    public static void setSpawnCooldown(ServerPlayer player) {
        setCooldown(player, "spawnCooldown", HavenConfig.spawnCooldown);
    }

    public static boolean canUseIsland(ServerPlayer player, int cooldownTime) {
        return canUseCommand(player, "islandCooldown", cooldownTime);
    }

    public static void setIslandCooldown(ServerPlayer player) {
        setCooldown(player, "islandCooldown", HavenConfig.islandCooldown);
    }

    public static boolean canUseVisit(ServerPlayer player, int cooldownTime) {
        return canUseCommand(player, "visitCooldown", cooldownTime);
    }

    public static void setVisitCooldown(ServerPlayer player) {
        setCooldown(player, "visitCooldown", HavenConfig.visitCooldown);
    }
}
