package net.cathienova.haven_skyblock_builder.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private final boolean allowBiomeDecoration;

    public SkyblockChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, TagKey<StructureSet> allowedStructureSets)
    {
        super(biomeSource, settings);
        this.settings = settings;
        this.allowedStructureSets = allowedStructureSets;
        // Add option in the config for the nether
        this.generateNormal = (settings.is(ResourceLocation.parse("minecraft:end")));
        this.allowBiomeDecoration = !settings.is(ResourceLocation.parse("minecraft:overworld"));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec()
    {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager, StructureManager pStructureManager,
                             ChunkAccess pChunk, GenerationStep.Carving pStep)
    {
        if (this.generateNormal)
        {
            super.applyCarvers(pLevel, pSeed, pRandom, pBiomeManager, pStructureManager, pChunk, pStep);
        }
    }

    // Filter structures
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
        if (this.generateNormal)
        {
            return super.fillFromNoise(blender, random, manager, chunk);
        }
        else
        {
            return CompletableFuture.completedFuture(chunk);
        }
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
    public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom)
    {
        if (this.generateNormal)
        {
            return super.getBaseColumn(pX, pZ, pHeight, pRandom);
        }
        else
        {
            return new NoiseColumn(0, new BlockState[0]);
        }
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
    public void applyBiomeDecoration(WorldGenLevel pLevel, ChunkAccess pChunk, StructureManager pStructureManager)
    {
        if (this.generateNormal || this.allowBiomeDecoration)
        {
            super.applyBiomeDecoration(pLevel, pChunk, pStructureManager);
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
}
