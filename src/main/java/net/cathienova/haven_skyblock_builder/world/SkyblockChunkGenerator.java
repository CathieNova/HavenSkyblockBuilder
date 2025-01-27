package net.cathienova.haven_skyblock_builder.world;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkyblockChunkGenerator extends NoiseBasedChunkGenerator
{
    public static final MapCodec<SkyblockChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings),
            TagKey.codec(Registries.STRUCTURE_SET).fieldOf("allowed_structure_sets").forGetter(gen -> gen.allowedStructureSets)
    ).apply(inst, inst.stable(SkyblockChunkGenerator::new)));
    private final Holder<NoiseGeneratorSettings> settings;
    private final TagKey<StructureSet> allowedStructureSets;
    private final boolean generateNormal;

    public SkyblockChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, TagKey<StructureSet> allowedStructureSets)
    {
        super(biomeSource, settings);
        this.settings = settings;
        this.allowedStructureSets = allowedStructureSets;
        this.generateNormal = (settings.is(ResourceLocation.parse("minecraft:end"))) ||
                (settings.is(ResourceLocation.parse("minecraft:the_nether")) && !HavenConfig.enableNetherSkyblock);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec()
    {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step)
    {
        if (this.generateNormal)
        {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
            return;
        }

        List<? extends String> carversConfig = HavenConfig.worldCarvers;
        if (carversConfig == null || carversConfig.isEmpty())
        {
            return;
        }

        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        ChunkPos chunkPos = chunk.getPos();
        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk(p -> this.createNoiseChunk(p, structureManager, Blender.of(level), random));
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(
                this, level.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk, random, this.settings.value().surfaceRule()
        );
        CarvingMask carvingMask = ((ProtoChunk) chunk).getOrCreateCarvingMask(step);

        int carvingRange = 8;
        for (int dx = -carvingRange; dx <= carvingRange; dx++)
        {
            for (int dz = -carvingRange; dz <= carvingRange; dz++)
            {
                ChunkPos targetPos = new ChunkPos(chunkPos.x + dx, chunkPos.z + dz);
                ChunkAccess targetChunk = level.getChunk(targetPos.x, targetPos.z);

                BiomeGenerationSettings biomeGenSettings = targetChunk.carverBiome(() ->
                        this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(
                                QuartPos.fromBlock(targetPos.getMinBlockX()),
                                0,
                                QuartPos.fromBlock(targetPos.getMinBlockZ()),
                                random.sampler()
                        ))
                );

                for (String carverId : carversConfig)
                {
                    ConfiguredWorldCarver<?> configuredCarver = level.registryAccess()
                            .registryOrThrow(Registries.CONFIGURED_CARVER)
                            .get(ResourceLocation.parse(carverId));

                    if (configuredCarver != null)
                    {
                        worldgenRandom.setLargeFeatureSeed(seed, targetPos.x, targetPos.z);
                        if (configuredCarver.isStartChunk(worldgenRandom))
                        {
                            configuredCarver.carve(carvingContext, chunk, biomeManager::getBiome, worldgenRandom, aquifer, targetPos, carvingMask);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> lookup, RandomState pRandomState, long pSeed)
    {
        return this.generateNormal ? super.createState(lookup, pRandomState, pSeed) : super.createState(new FilteredLookup(lookup, this.allowedStructureSets),
                pRandomState, pSeed);
    }

    @Override
    public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk)
    {
        if (this.generateNormal)
        {
            super.buildSurface(pLevel, pStructureManager, pRandom, pChunk);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion pLevel)
    {
        if (this.generateNormal)
        {
            super.spawnOriginalMobs(pLevel);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager manager, ChunkAccess chunk)
    {
        ResourceKey<Level> dimension = inferDimension();

        if (dimension == null)
        {
            return CompletableFuture.completedFuture(chunk);
        }

        List<BlockState> layers = parseLayerConfig(dimension);
        if (layers.isEmpty())
        {
            return CompletableFuture.completedFuture(chunk);
        }

        int minY = chunk.getMinBuildHeight();
        int maxY = minY + chunk.getHeight();

        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                for (int y = minY; y < maxY && y - minY < layers.size(); y++)
                {
                    chunk.setBlockState(new BlockPos(x, y, z), layers.get(y - minY), false);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    private ResourceKey<Level> inferDimension()
    {
        Optional<ResourceKey<NoiseGeneratorSettings>> key = settings.unwrapKey();

        if (key.isPresent())
        {
            ResourceKey<NoiseGeneratorSettings> noiseKey = key.get();

            if (noiseKey.equals(NoiseGeneratorSettings.OVERWORLD))
            {
                return Level.OVERWORLD;
            }
            else if (noiseKey.equals(NoiseGeneratorSettings.NETHER))
            {
                return Level.NETHER;
            }
            else if (noiseKey.equals(NoiseGeneratorSettings.END))
            {
                return Level.END;
            }
        }

        return null;
    }

    @Override
    public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom)
    {
        if (this.generateNormal)
        {
            return super.getBaseHeight(pX, pZ, pType, pLevel, pRandom);
        }
        else
        {
            return getMinY();
        }
    }

    @Override
    public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor heightAccessor, RandomState pRandom)
    {
        if (this.generateNormal)
        {
            return super.getBaseColumn(pX, pZ, heightAccessor, pRandom);
        }

        ResourceKey<Level> dimension = inferDimension();
        if (dimension == null)
        {
            return new NoiseColumn(heightAccessor.getMinBuildHeight(), new BlockState[0]);
        }

        List<BlockState> layers = parseLayerConfig(dimension);
        if (layers.isEmpty())
        {
            return super.getBaseColumn(pX, pZ, heightAccessor, pRandom);
        }

        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        int minY = heightAccessor.getMinBuildHeight();
        int maxY = minY + heightAccessor.getHeight();

        int currentLayer = 0;
        for (int y = minY; y < maxY; y++)
        {
            if (currentLayer < layers.size())
            {
                states[y - minY] = layers.get(currentLayer);
                currentLayer++;
            }
            else
            {
                states[y - minY] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos)
    {
        if (this.generateNormal)
        {
            super.addDebugScreenInfo(pInfo, pRandom, pPos);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager)
    {
        if (this.generateNormal)
        {
            super.applyBiomeDecoration(level, chunk, structureManager);
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        SectionPos sectionPos = SectionPos.of(chunkPos, level.getMinSection());
        BlockPos origin = sectionPos.origin();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long decorationSeed = worldgenRandom.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());

        // Retrieve placed features registry
        Registry<PlacedFeature> placedFeatureRegistry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);

        List<? extends String> featuresConfig = HavenConfig.worldPlacedFeatures;
        if (featuresConfig == null || featuresConfig.isEmpty())
        {
            return;
        }

        for (String featureId : featuresConfig)
        {
            PlacedFeature placedFeature = placedFeatureRegistry.get(ResourceLocation.parse(featureId));

            if (placedFeature == null)
            {
                HavenSkyblockBuilder.Log("Feature not found: " + featureId);
                continue;
            }

            try
            {
                // Place feature with biome check
                placedFeature.placeWithBiomeCheck(level, this, worldgenRandom, origin);
            } catch (Exception e)
            {
                HavenSkyblockBuilder.Log("Error placing feature " + featureId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager pStructureManager, ChunkAccess pChunk)
    {
        if (this.generateNormal || hasStructures(level.registryAccess()))
        {
            super.createReferences(level, pStructureManager, pChunk);
        }
    }

    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState pStructureState, StructureManager pStructureManager,
                                 ChunkAccess pChunk, StructureTemplateManager pStructureTemplateManager)
    {
        if (this.generateNormal || hasStructures(registries))
        {
            super.createStructures(registries, pStructureState, pStructureManager, pChunk, pStructureTemplateManager);
        }
    }

    private boolean hasStructures(RegistryAccess registries)
    {
        return registries.registryOrThrow(Registries.STRUCTURE_SET).getTagOrEmpty(this.allowedStructureSets).iterator().hasNext();
    }

    private record FilteredLookup(HolderLookup<StructureSet> parent,
                                  TagKey<StructureSet> allowedValues) implements HolderLookup<StructureSet>
    {
        @Override
        public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> key)
        {
            return this.parent.get(key).filter(obj -> obj.is(this.allowedValues));
        }

        @Override
        public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> tagKey)
        {
            return this.parent.get(tagKey);
        }

        @Override
        public Stream<Holder.Reference<StructureSet>> listElements()
        {
            return this.parent.listElements().filter(obj -> obj.is(this.allowedValues));
        }

        @Override
        public Stream<HolderSet.Named<StructureSet>> listTags()
        {
            return this.parent.listTags();
        }
    }

    private List<BlockState> parseLayerConfig(ResourceKey<Level> dimension)
    {
        String config;
        if (dimension == Level.OVERWORLD)
        {
            config = HavenConfig.overworldLayerGeneration;
        }
        else if (dimension == Level.NETHER)
        {
            config = HavenConfig.netherLayerGeneration;
        }
        else
        {
            return new ArrayList<>();
        }

        if (config == null || config.isEmpty())
        {
            return new ArrayList<>();
        }

        List<BlockState> layers = new ArrayList<>();
        String[] entries = config.split(",");
        for (String entry : entries)
        {
            String[] parts = entry.split("\\*");
            int count = parts.length > 1 ? Integer.parseInt(parts[0]) : 1;
            BlockState blockState = Blocks.AIR.defaultBlockState();

            try
            {
                blockState = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), parts[parts.length - 1], false).blockState();
            } catch (CommandSyntaxException e)
            {
                HavenSkyblockBuilder.Log("Failed to parse block state: " + parts[parts.length - 1]);
            }

            for (int i = 0; i < count; i++)
            {
                layers.add(blockState);
            }
        }

        return layers;
    }
}
