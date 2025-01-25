package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.Queue;

public class IslandManager {
    private static final Queue<BlockPos> blocksToClear = new ArrayDeque<>();
    private static final int BLOCKS_PER_TICK = 100;
    private static final Queue<BlockPos> pendingPopulation = new ArrayDeque<>();

    // This method causes lag but it works, it has to be improved later on..
    /*public static void deleteIslandArea(ServerLevel level, BlockPos teamCenter) {
        int radius = 250;

        HavenSkyblockBuilder.Log("Preparing to delete island area around " + teamCenter + " with radius " + radius);
        populateBlocksQueueAsync(level, teamCenter, radius, level.getMinBuildHeight(), level.getMaxBuildHeight());
    }

    private static void populateBlocksQueueAsync(ServerLevel level, BlockPos center, int radius, int minHeight, int maxHeight) {
        pendingPopulation.clear();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    for (int y = minHeight; y <= maxHeight; y++) {
                        pendingPopulation.add(new BlockPos(center.getX() + x, y, center.getZ() + z));
                    }
                }
            }
        }

        level.getServer().execute(() -> populateQueueIncrementally(level));
    }

    private static void populateQueueIncrementally(ServerLevel level) {
        int processed = 0;

        while (processed < BLOCKS_PER_TICK && !pendingPopulation.isEmpty()) {
            blocksToClear.add(pendingPopulation.poll());
            processed++;
        }

        if (!pendingPopulation.isEmpty()) {
            level.getServer().execute(() -> populateQueueIncrementally(level));
        } else {
            HavenSkyblockBuilder.Log("Block queue population completed. Starting deletion...");
            scheduleDeletion(level);
        }
    }

    private static void scheduleDeletion(ServerLevel level) {
        level.getServer().execute(() -> {
            int processed = 0;
            while (processed < BLOCKS_PER_TICK && !blocksToClear.isEmpty()) {
                BlockPos pos = blocksToClear.poll();
                if (pos != null) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    processed++;
                }
            }

            if (!blocksToClear.isEmpty()) {
                scheduleDeletion(level);
            } else {
                HavenSkyblockBuilder.Log("Island area deletion completed.");
            }
        });
    }*/
}
