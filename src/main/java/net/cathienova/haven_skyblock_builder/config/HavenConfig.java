package net.cathienova.haven_skyblock_builder.config;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.fml.config.ModConfig;

import java.util.List;

public class HavenConfig
{
    public static int islandCreationHeight;
    public static boolean enableNetherSkyblock;
    public static boolean keepInventoryOnIslandLeave;
    public static List<? extends String> islandSpecificOffsets;
    public static Integer islandDistance;
    public static List<? extends String> spawnPosition;
    public static List<? extends String> additionalStructures;
    public static Integer homeCooldown;
    public static Integer spawnCooldown;
    public static Integer islandCooldown;
    public static Integer visitCooldown;
    public static List<? extends String> blacklistBiomesForIslands;
    public static String overworldLayerGeneration;
    public static String netherLayerGeneration;
    public static List<? extends String> worldCarvers;

    public static void bake(ModConfig config)
    {
        islandCreationHeight = HavenSkyblockBuilder.c_config.islandCreationHeight.get();
        enableNetherSkyblock = HavenSkyblockBuilder.c_config.enableNetherSkyblock.get();
        keepInventoryOnIslandLeave = HavenSkyblockBuilder.c_config.keepInventoryOnIslandLeave.get();
        islandSpecificOffsets = HavenSkyblockBuilder.c_config.islandSpecificOffsets.get();
        islandDistance = HavenSkyblockBuilder.c_config.islandDistance.get();
        spawnPosition = HavenSkyblockBuilder.c_config.spawnPosition.get();
        additionalStructures = HavenSkyblockBuilder.c_config.additionalStructures.get();
        homeCooldown = HavenSkyblockBuilder.c_config.homeCooldown.get();
        spawnCooldown = HavenSkyblockBuilder.c_config.spawnCooldown.get();
        islandCooldown = HavenSkyblockBuilder.c_config.islandCooldown.get();
        visitCooldown = HavenSkyblockBuilder.c_config.visitCooldown.get();
        blacklistBiomesForIslands = HavenSkyblockBuilder.c_config.blacklistBiomesForIslands.get();
        overworldLayerGeneration = HavenSkyblockBuilder.c_config.overworldLayerGeneration.get();
        netherLayerGeneration = HavenSkyblockBuilder.c_config.netherLayerGeneration.get();
        worldCarvers = HavenSkyblockBuilder.c_config.worldCarvers.get();
    }
}
