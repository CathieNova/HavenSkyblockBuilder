package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.events.ModEvents;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StructureUtils {
    public static StructureTemplate generateMainIsland(ServerLevel level, BlockPos position, String islandTemplate) {
        ModEvents.generateDefaultTemplates();
        Path structurePath = Path.of("config/HavenSkyblockBuilder/Templates", islandTemplate + ".nbt");
        StructureTemplate template = loadStructure(level, structurePath);
        placeStructure(level, position, template);
        createAdditionalIsland(level, position, islandTemplate);
        return template;
    }

    public static void createAdditionalIsland(ServerLevel level, BlockPos position, String islandTemplate) {
        List<? extends String> additionalStructures = HavenConfig.additionalStructures;
        if (additionalStructures == null) {
            return;
        }

        ModEvents.generateDefaultTemplates();

        for (String entry : additionalStructures) {
            String[] parts = entry.split("=");
            if (parts.length != 2 || !parts[0].equals(islandTemplate)) {
                continue;
            }

            String[] structureParts = parts[1].split(",");
            if (structureParts.length != 4) {
                HavenSkyblockBuilder.Log("Invalid structure entry: " + entry);
                continue;
            }

            String structureName = structureParts[0];
            int xOffset;
            int yOffset;
            int zOffset;

            try {
                xOffset = Integer.parseInt(structureParts[1]);
                yOffset = Integer.parseInt(structureParts[2]);
                zOffset = Integer.parseInt(structureParts[3]);
            } catch (NumberFormatException e) {
                HavenSkyblockBuilder.Log("Invalid offsets in structure entry: " + entry);
                continue;
            }

            BlockPos structurePosition = position.offset(xOffset, yOffset, zOffset);
            Path structurePath = Path.of("config/HavenSkyblockBuilder/AdditionalIslands", structureName + ".nbt");
            StructureTemplate template = loadStructure(level, structurePath);
            placeStructure(level, structurePosition, template);
        }
    }

    public static void createSpawnIsland(ServerLevel level, BlockPos position, String islandTemplate) {
        ModEvents.generateSpawnIsland();
        Path structurePath = Path.of("config/HavenSkyblockBuilder", islandTemplate + ".nbt");
        StructureTemplate template = loadStructure(level, structurePath);
        placeStructure(level, position, template);
    }

    public static StructureTemplate loadStructure(ServerLevel level, Path structurePath) {
        if (!Files.exists(structurePath)) {
            throw new IllegalArgumentException("Structure file not found: " + structurePath);
        }

        try {
            CompoundTag nbtData = NbtIo.readCompressed(structurePath, NbtAccounter.unlimitedHeap());
            StructureTemplate template = new StructureTemplate();
            HolderGetter<Block> blockRegistry = level.registryAccess().lookupOrThrow(Registries.BLOCK);
            template.load(blockRegistry, nbtData);
            return template;
        } catch (Exception e) {
            HavenSkyblockBuilder.Log("Error loading structure '" + structurePath + "': " + e.getMessage());
            throw new RuntimeException("Failed to load structure from path: " + structurePath, e);
        }
    }

    private static void placeStructure(ServerLevel level, BlockPos position, StructureTemplate template) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        RandomSource random = RandomSource.create();
        template.placeInWorld(level, position, position, settings, random, 3);
    }
}