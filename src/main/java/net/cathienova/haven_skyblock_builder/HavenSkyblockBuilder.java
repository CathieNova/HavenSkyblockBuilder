package net.cathienova.haven_skyblock_builder;

import com.mojang.logging.LogUtils;
import net.cathienova.haven_skyblock_builder.commands.ModCommands;
import net.cathienova.haven_skyblock_builder.config.CommonConfig;
import net.cathienova.haven_skyblock_builder.events.EventHandler;
import net.cathienova.haven_skyblock_builder.events.ModEvents;
import net.cathienova.haven_skyblock_builder.handler.ClientHandler;
import net.cathienova.haven_skyblock_builder.item.*;
import net.cathienova.haven_skyblock_builder.networking.NetworkHandler;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.DistUtils;
import net.cathienova.haven_skyblock_builder.world.ModChunkGenerators;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod(HavenSkyblockBuilder.MOD_ID)
public class HavenSkyblockBuilder
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "haven_skyblock_builder";
    public static final String MOD_NAME = "Haven Skyblock Builder";
    static final ModConfigSpec commonSpec;
    public static final CommonConfig c_config;

    static
    {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        commonSpec = specPair.getRight();
        c_config = specPair.getLeft();
    }

    public HavenSkyblockBuilder(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        modContainer.registerConfig(ModConfig.Type.SERVER, commonSpec);
        ModItems.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);
        EventHandler.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.register(modEventBus);
        }
        DistUtils.runIfOn(Dist.CLIENT, HavenSkyblockBuilderClient::new);
    }

    public static void Log(String message)
    {
        LogUtils.getLogger().info("[" + MOD_NAME + "] " + message);
    }

    private void registerCommands(RegisterCommandsEvent evt) {
        ModCommands.register(evt.getDispatcher());
    }

    private void onServerStarting(ServerStartingEvent event)
    {
        TeamManager.loadAllTeams(event.getServer());
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
