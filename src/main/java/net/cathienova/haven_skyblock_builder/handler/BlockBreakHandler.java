package net.cathienova.haven_skyblock_builder.handler;


import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BlockBreakHandler
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {

    }
}
