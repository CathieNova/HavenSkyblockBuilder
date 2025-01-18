package net.cathienova.haven_skyblock_builder.util;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class ModTags
{
    public static final TagKey<Structure> OVERWORLD_SKYBLOCK_STRUCTURES = tag("overworld_skyblock_structure_sets");
    public static final TagKey<Structure> THE_NETHER_SKYBLOCK_STRUCTURES = tag("the_nether_skyblock_structure_sets");
    public static final TagKey<Structure> THE_END_SKYBLOCK_STRUCTURES = tag("the_end_skyblock_structure_sets");

    public static TagKey<Structure> tag(String name) {
        return TagKey.create(Registries.STRUCTURE, HavenSkyblockBuilder.loc(name));
    }
}
