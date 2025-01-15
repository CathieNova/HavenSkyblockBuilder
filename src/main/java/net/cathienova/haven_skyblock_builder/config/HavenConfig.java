package net.cathienova.haven_skyblock_builder.config;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.fml.config.ModConfig;

import java.util.List;
import java.util.Map;

public class HavenConfig
{
    public static boolean enableNetherSkyblock;
    public static boolean enableEndSkyblock;
    public static int islandCreationHeight;
    public static List<? extends String> SpawnOffset;
    public static Integer islandDistance;
    public static List<? extends String> spawnPosition;
    public static List<? extends String> additionalStructures;

    public static void bake(ModConfig config)
    {
        enableNetherSkyblock = HavenSkyblockBuilder.c_config.enableNetherSkyblock.get();
        enableEndSkyblock = HavenSkyblockBuilder.c_config.enableEndSkyblock.get();
        islandCreationHeight = HavenSkyblockBuilder.c_config.islandCreationHeight.get();
        SpawnOffset = HavenSkyblockBuilder.c_config.spawnOffset.get();
        islandDistance = HavenSkyblockBuilder.c_config.islandDistance.get();
        spawnPosition = HavenSkyblockBuilder.c_config.spawnPosition.get();
        additionalStructures = HavenSkyblockBuilder.c_config.additionalStructures.get();
    }
}
