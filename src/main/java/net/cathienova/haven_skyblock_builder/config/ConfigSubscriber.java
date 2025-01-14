package net.cathienova.haven_skyblock_builder.config;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigSubscriber {
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Reloading || event instanceof ModConfigEvent.Loading) {
            HavenConfig.bake(event.getConfig());
        } else if (event instanceof ModConfigEvent.Unloading) {

        }
    }
}
