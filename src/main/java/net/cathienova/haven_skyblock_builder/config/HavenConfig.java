package net.cathienova.haven_skyblock_builder.config;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.fml.config.ModConfig;

import java.util.List;
import java.util.Map;

public class HavenConfig
{
    public static int islandCreationHeight;
    public static boolean enableNetherSkyblock;
    public static List<? extends String> SpawnOffset;
    public static Integer islandDistance;
    public static List<? extends String> spawnPosition;
    public static List<? extends String> additionalStructures;

    public static void bake(ModConfig config)
    {
        islandCreationHeight = HavenSkyblockBuilder.c_config.islandCreationHeight.get();
        enableNetherSkyblock = HavenSkyblockBuilder.c_config.enableNetherSkyblock.get();
        SpawnOffset = HavenSkyblockBuilder.c_config.spawnOffset.get();
        islandDistance = HavenSkyblockBuilder.c_config.islandDistance.get();
        spawnPosition = HavenSkyblockBuilder.c_config.spawnPosition.get();
        additionalStructures = HavenSkyblockBuilder.c_config.additionalStructures.get();
    }
}
