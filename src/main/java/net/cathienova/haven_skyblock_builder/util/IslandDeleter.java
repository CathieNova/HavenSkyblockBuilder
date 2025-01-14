package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Somehow i can't get this to delete the island area...

public class IslandDeleter {
    private static final Queue<BlockPos> blocksToClear = new ArrayDeque<>();
    private static final int BLOCKS_PER_TICK = 10;
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final int CLEARING_PERIOD = 100;

    public static void deleteIslandArea(ServerLevel level, BlockPos teamCenter) {
        int radius = 250;
        blocksToClear.clear();
        HavenSkyblockBuilder.Log("Clearing island area around " + teamCenter + " with radius " + radius);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    for (int y = level.getMinBuildHeight(); y <= level.getMaxBuildHeight(); y++) {
                        BlockPos pos = teamCenter.offset(x, y - teamCenter.getY(), z);
                        blocksToClear.add(pos);
                    }
                }
            }
        }
        HavenSkyblockBuilder.Log("Blocks queued for deletion: " + blocksToClear.size());

        // Schedule the clearing task
        executor.scheduleAtFixedRate(() -> clearBlocks(level), 0, CLEARING_PERIOD, TimeUnit.MILLISECONDS);
    }

    private static void clearBlocks(ServerLevel level) {
        if (blocksToClear.isEmpty()) {
            HavenSkyblockBuilder.Log("All blocks cleared.");
            executor.shutdown();
            return;
        }

        for (int i = 0; i < BLOCKS_PER_TICK && !blocksToClear.isEmpty(); i++) {
            BlockPos pos = blocksToClear.poll();
            if (pos != null && level.isLoaded(pos) && !level.isEmptyBlock(pos)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
