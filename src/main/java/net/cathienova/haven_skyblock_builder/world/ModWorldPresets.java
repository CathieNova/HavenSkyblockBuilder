package net.cathienova.haven_skyblock_builder.world;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class ModWorldPresets
{
    public static final ResourceKey<WorldPreset> SKYBLOCK_WORLD = ResourceKey.create(Registries.WORLD_PRESET, HavenSkyblockBuilder.loc("skyblock_world"));
}
