package net.cathienova.haven_skyblock_builder.events;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.util.StructureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (level == null) {
            return;
        }

        File worldFolder = event.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File configFolder = new File(worldFolder, "serverconfig");
        File markerFile = new File(configFolder, "hsb");

        if (!markerFile.exists()) {
            configFolder.mkdirs();
            try {
                generateSpawnIsland();
                StructureUtils.createSpawnIsland(level, new BlockPos(-3, 68, -3), "spawn_island");
                markerFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create marker file or copy files", e);
            }
        }
    }

    public static void generateSpawnIsland() {
        Path spawnIslandPath = new File("config/HavenSkyblockBuilder").toPath();

        try {
            Files.createDirectories(spawnIslandPath);
            copyFileIfMissing("data/haven_skyblock_builder/structure/spawn_island.nbt", spawnIslandPath.resolve("spawn_island.nbt"));
        } catch (IOException e) {
            HavenSkyblockBuilder.Log("Failed to create SpawnIsland folder: " + e.getMessage());
        }
    }

    public static void generateDefaultTemplates() {
        Path additionalIslandsPath = new File("config/HavenSkyblockBuilder/AdditionalIslands").toPath();
        Path templatesPath = new File("config/HavenSkyblockBuilder/Templates").toPath();

        generateSpawnIsland();

        try {
            Files.createDirectories(additionalIslandsPath);
            copyFileIfMissing("data/haven_skyblock_builder/structure/additional_sand_island.nbt", additionalIslandsPath.resolve("additional_sand_island.nbt"));
        } catch (IOException e) {
            HavenSkyblockBuilder.Log("Failed to create AdditionalIslands folder: " + e.getMessage());
        }

        try {
            Files.createDirectories(templatesPath);
            copyFileIfMissing("data/haven_skyblock_builder/structure/classic_island.nbt", templatesPath.resolve("classic_island.nbt"));
        } catch (IOException e) {
            HavenSkyblockBuilder.Log("Failed to create Templates folder: " + e.getMessage());
        }
    }

    private static void copyFileIfMissing(String sourcePath, Path destination) throws IOException {
        if (Files.exists(destination)) {
            return;
        }

        try (InputStream inputStream = ModEvents.class.getClassLoader().getResourceAsStream(sourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Source file not found: " + sourcePath);
            }

            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}