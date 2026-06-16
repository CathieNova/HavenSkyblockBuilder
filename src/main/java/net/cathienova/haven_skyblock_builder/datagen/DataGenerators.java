package net.cathienova.haven_skyblock_builder.datagen;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModEngLangProvider::new);
    }
}