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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import javax.annotation.Nullable;
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
        BlockPos bedPos = player.getRespawnPosition();
        boolean hasUsableBed = false;

        if (bedPos != null)
        {
            ResourceKey<Level> dim = player.getRespawnDimension();
            ServerLevel level = player.server.getLevel(dim);
            if (level != null)
            {
                BlockState bedState = level.getBlockState(bedPos);
                hasUsableBed = bedState.getBlock() instanceof BedBlock && BedBlock.canSetSpawn(level);
            }
        }

        if (hasUsableBed)
        {
            player.teleportTo(bedPos.getX() + 0.5, bedPos.getY() + 1, bedPos.getZ() + 0.5);
        }
        else if (team != null)
        {
            BlockPos home = team.getHomePosition();
            player.teleportTo(home.getX(), home.getY() + 1, home.getZ());
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
                    BlockPos pos = new BlockPos(0, 71, 0);
                    BlockPos tpPos = SkyblockUtils.findNearestValidBlock(player.serverLevel(), new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
                    player.teleportTo(tpPos.getX() + 0.5f, tpPos.getY() + 1, tpPos.getZ() + 0.5f);
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
