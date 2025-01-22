package net.cathienova.haven_skyblock_builder.events;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.networking.NetworkHandler;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.SkyblockUtils;
import net.cathienova.haven_skyblock_builder.world.SkyblockChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Locale;

public class EventHandler
{
    public static void register(IEventBus modBus) {
        var fmlBus = NeoForge.EVENT_BUS;

        fmlBus.addListener(EventHandler::onPlayerLogin);
        fmlBus.addListener(EventHandler::onPlayerLogout);
        fmlBus.addListener(EventHandler::onPlayerRespawn);
        modBus.addListener(EventHandler::registerPayloadHandler);
        modBus.addListener(EventHandler::onCommonSetup);
    }

    public static void registerPayloadHandler(RegisterPayloadHandlersEvent event) {
        NetworkHandler.register(event.registrar(HavenSkyblockBuilder.MOD_ID));
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Team team = TeamManager.getTeamByPlayer(player.getUUID());
        if (team != null) {
            player.teleportTo(team.getHomePosition().getX(), team.getHomePosition().getY() + 1, team.getHomePosition().getZ());
        }
        else
        {
            BlockPos spawn = SkyblockUtils.parseConfigPosition(HavenConfig.spawnPosition);
            player.teleportTo(spawn.getX(), spawn.getY(), spawn.getZ());
        }
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var generator = player.serverLevel().getChunkSource().getGenerator();

            if (generator instanceof SkyblockChunkGenerator || generator.getClass().getName().toLowerCase(Locale.ROOT).contains("skyblock")) {
                NetworkHandler.sendSkyblockWorld(player);

                Team team = TeamManager.getTeamByPlayer(player.getUUID());
                if (team == null)
                {
                    BlockPos pos = SkyblockUtils.findNearestValidBlock((ServerLevel) player.level(), new BlockPos(0, 71, 0));
                    player.teleportTo(pos.getX() + 0.5f, pos.getY() + 1, pos.getZ() + 0.5f);
                    player.sendSystemMessage(Component.translatable("haven_skyblock_builder.message.skyblock_spawn"));
                }
            }
        }
    }

    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Team team = TeamManager.getTeamByPlayer(player.getUUID());
            if (team != null) {
               TeamManager.saveTeam(event.getEntity().getServer(), team);
            }
        }
    }

    private static void onCommonSetup(FMLCommonSetupEvent event)
    {
        ModEvents.generateDefaultTemplates();
    }
}
