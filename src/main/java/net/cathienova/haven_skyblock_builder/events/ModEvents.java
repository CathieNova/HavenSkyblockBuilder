package net.cathienova.haven_skyblock_builder.events;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.File;
import java.io.IOException;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        MinecraftServer server = level.getServer();
        File worldFolder = server.getWorldPath(LevelResource.ROOT).toFile();
        File configFolder = new File(worldFolder, "serverconfig");
        File markerFile = new File(configFolder, "hsb");

        if (!markerFile.exists()) {
            configFolder.mkdirs();
            try {
                markerFile.createNewFile();
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        level.setBlock(new BlockPos(x, 70, z), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create marker file", e);
            }
        }
    }
}
