package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class StructureUtils
{
    public static StructureTemplate generateMainIsland(ServerLevel level, BlockPos position, String islandTemplate)
    {
        StructureTemplate template;

        File structureFile = new File("config/HavenSkyblockBuilder/Templates", islandTemplate + ".nbt");
        File parentDirectory = structureFile.getParentFile();
        Path structurePath = parentDirectory.toPath().resolve(structureFile.getName());

        try
        {
            CompoundTag nbtData = NbtIo.readCompressed(structurePath, NbtAccounter.unlimitedHeap());

            template = new StructureTemplate();
            HolderGetter<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK).asLookup();
            template.load(blockRegistry, nbtData);

            StructurePlaceSettings settings = new StructurePlaceSettings();
            RandomSource random = RandomSource.create();
            template.placeInWorld(level, position, position, settings, random, 3);
            createAdditionalIsland(level, position, islandTemplate);
        } catch (Exception e)
        {
            HavenSkyblockBuilder.Log("Error loading custom structure: " + e.getMessage());
            throw new RuntimeException("Failed to load structure from custom path: " + structurePath, e);
        }

        return template;
    }

    public static void createAdditionalIsland(ServerLevel level, BlockPos position, String islandTemplate)
    {
        List<? extends String> additionalStructures = HavenConfig.additionalStructures;
        if (additionalStructures != null)
        {
            for (String entry : additionalStructures)
            {
                String[] parts = entry.split("=");
                if (parts.length != 2 || !parts[0].equals(islandTemplate))
                {
                    continue;
                }

                String[] structureParts = parts[1].split(",");
                if (structureParts.length != 4)
                {
                    HavenSkyblockBuilder.Log("Invalid structure entry: " + entry);
                    continue;
                }

                String structureName = structureParts[0];
                int xOffset, yOffset, zOffset;
                try
                {
                    xOffset = Integer.parseInt(structureParts[1]);
                    yOffset = Integer.parseInt(structureParts[2]);
                    zOffset = Integer.parseInt(structureParts[3]);
                } catch (NumberFormatException e)
                {
                    HavenSkyblockBuilder.Log("Invalid offsets in structure entry: " + entry);
                    continue;
                }

                BlockPos structurePosition = position.offset(xOffset, yOffset, zOffset);

                try
                {
                    Path structurePath = Path.of("config/HavenSkyblockBuilder/AdditionalIslands", structureName + ".nbt");
                    CompoundTag nbtData = NbtIo.readCompressed(structurePath, NbtAccounter.unlimitedHeap());

                    StructureTemplate template = new StructureTemplate();
                    HolderGetter<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK).asLookup();
                    template.load(blockRegistry, nbtData);

                    StructurePlaceSettings settings = new StructurePlaceSettings();
                    RandomSource random = RandomSource.create();
                    template.placeInWorld(level, structurePosition, structurePosition, settings, random, 3);

                } catch (Exception e)
                {
                    HavenSkyblockBuilder.Log("Error loading additional structure '" + structureName + "': " + e.getMessage());
                    throw new RuntimeException("Failed to load additional structure from path: " + structureName, e);
                }
            }
        }
    }

    public static void createSpawnIsland(ServerLevel level, BlockPos pos, String islandTemplate)
    {
        StructureTemplate template;
        File structureFile = new File("config/HavenSkyblockBuilder/", islandTemplate + ".nbt");
        File parentDirectory = structureFile.getParentFile();
        Path structurePath = parentDirectory.toPath().resolve(structureFile.getName());

        try
        {
            CompoundTag nbtData = NbtIo.readCompressed(structurePath, NbtAccounter.unlimitedHeap());

            template = new StructureTemplate();
            HolderGetter<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK).asLookup();
            template.load(blockRegistry, nbtData);

            StructurePlaceSettings settings = new StructurePlaceSettings();
            RandomSource random = RandomSource.create();
            template.placeInWorld(level, pos, pos, settings, random, 3);
        } catch (Exception e)
        {
            HavenSkyblockBuilder.Log("Error loading custom structure: " + e.getMessage());
            throw new RuntimeException("Failed to load structure from custom path: " + structurePath, e);
        }
    }
}
