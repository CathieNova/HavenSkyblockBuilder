package net.cathienova.haven_skyblock_builder.datagen;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators
{
    @EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModRecipeProvider
    {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event)
        {
            DataGenerator generator = event.getGenerator();
            PackOutput output = generator.getPackOutput();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            boolean includeClient = event.includeClient();
            boolean includeServer = event.includeServer();

            if (includeClient)
            {
                generator.addProvider(includeClient, new ModEngLangProvider(output));
            }
            if (includeServer)
            {
                generator.addProvider(includeServer, new ModStructureTagGenerator(output, lookupProvider, existingFileHelper));
            }
        }
    }
}
