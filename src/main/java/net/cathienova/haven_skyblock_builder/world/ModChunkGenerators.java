package net.cathienova.haven_skyblock_builder.world;

import com.mojang.serialization.MapCodec;
import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModChunkGenerators
{
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
            Registries.CHUNK_GENERATOR, HavenSkyblockBuilder.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<SkyblockChunkGenerator>> SKYBLOCK = CHUNK_GENERATORS.register(
            "skyblock", () -> SkyblockChunkGenerator.CODEC);
}
