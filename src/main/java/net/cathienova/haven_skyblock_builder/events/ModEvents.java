package net.cathienova.haven_skyblock_builder.events;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.util.StructureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
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

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        MinecraftServer server = level.getServer();
        File worldFolder = server.getWorldPath(LevelResource.ROOT).toFile();
        File configFolder = new File(worldFolder, "serverconfig");
        File markerFile = new File(configFolder, "hsb");

        if (!markerFile.exists()) {
            configFolder.mkdirs();
            try {
                markerFile.createNewFile();

                generateSpawnIsland();

                StructureUtils.createSpawnIsland(level, new BlockPos(-3, 68, -3), "spawn_island");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create marker file or copy files", e);
            }
        }
    }
    public static void generateSpawnIsland()
    {
        Path spawnIslandPath = new File("config/HavenSkyblockBuilder").toPath();

        if (!Files.exists(spawnIslandPath))
        {
            try
            {
                Files.createDirectories(spawnIslandPath);
                copyFile("assets/haven_skyblock_builder/structures/spawn_island.nbt", new File(spawnIslandPath.toFile(), "spawn_island.nbt"));
            } catch (IOException e)
            {
                HavenSkyblockBuilder.Log("Failed to create SpawnIsland folder: " + e.getMessage());
            }
        }
    }

    public static void generateDefaultTemplates()
    {
        Path additionalIslandsPath = new File("config/HavenSkyblockBuilder/AdditionalIslands").toPath();
        Path templatesPath = new File("config/HavenSkyblockBuilder/Templates").toPath();

        generateSpawnIsland();

        if (!Files.exists(additionalIslandsPath))
        {
            try
            {
                Files.createDirectories(additionalIslandsPath);
                copyFile("assets/haven_skyblock_builder/structures/additional_sand_island.nbt", new File(additionalIslandsPath.toFile(), "additional_sand_island.nbt"));

            } catch (IOException e)
            {
                HavenSkyblockBuilder.Log("Failed to create AdditionalIslands folder: " + e.getMessage());
            }
        }

        if (!Files.exists(templatesPath))
        {
            try
            {
                Files.createDirectories(templatesPath);
                copyFile("assets/haven_skyblock_builder/structures/classic_island.nbt", new File(templatesPath.toFile(), "classic_island.nbt"));
            } catch (IOException e)
            {
                HavenSkyblockBuilder.Log("Failed to create Templates folder: " + e.getMessage());
            }
        }
    }

    private static void copyFile(String sourcePath, File destination) throws IOException {
        InputStream inputStream = MinecraftServer.class.getClassLoader().getResourceAsStream(sourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Source file not found: " + sourcePath);
        }
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
    }
}
