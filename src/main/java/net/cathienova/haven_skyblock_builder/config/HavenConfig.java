package net.cathienova.haven_skyblock_builder.config;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.fml.config.ModConfig;

import java.util.List;

public class HavenConfig
{
    public static String SkyblockIsland;
    public static int islandCreationHeight;
    public static List<? extends String> SpawnOffset;
    public static Integer islandDistance;

    public static void bake(ModConfig config)
    {
        SkyblockIsland = HavenSkyblockBuilder.c_config.islandTemplate.get();
        islandCreationHeight = HavenSkyblockBuilder.c_config.islandCreationHeight.get();
        SpawnOffset = HavenSkyblockBuilder.c_config.spawnOffset.get();
        islandDistance = HavenSkyblockBuilder.c_config.islandDistance.get();
    }
}
