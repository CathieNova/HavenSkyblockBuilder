package net.cathienova.haven_skyblock_builder.datagen;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModStructureTagGenerator extends StructureTagsProvider
{
    public ModStructureTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, HavenSkyblockBuilder.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(ModTags.OVERWORLD_SKYBLOCK_STRUCTURES);

        this.tag(ModTags.THE_NETHER_SKYBLOCK_STRUCTURES)
                .addOptional(BuiltinStructureSets.NETHER_COMPLEXES.location());

        this.tag(ModTags.THE_END_SKYBLOCK_STRUCTURES)
                .addOptional(BuiltinStructureSets.END_CITIES.location());
    }
}
