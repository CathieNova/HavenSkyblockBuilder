package net.cathienova.haven_skyblock_builder.handler;

import net.cathienova.haven_skyblock_builder.config.HavenConfig;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.SkyblockUtils;
import net.cathienova.haven_skyblock_builder.world.ModWorldPresets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientHandler
{
    public static boolean isInSkyblockWorld;
    public static Holder<WorldPreset> originalDefaultWorldPreset;

    public static void register(IEventBus modBus)
    {
        var fmlBus = NeoForge.EVENT_BUS;

        fmlBus.addListener(ClientHandler::onPlayerRespawn);
        fmlBus.addListener(ClientHandler::onPlayerLogout);
        fmlBus.addListener(ClientHandler::onScreenOpen);
    }

    private static void onPlayerRespawn(ClientPlayerNetworkEvent.Clone event) {
        if (isInSkyblockWorld) {
            disableVoidFogRendering();
        }
    }

    private static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        isInSkyblockWorld = false;
    }

    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof CreateWorldScreen screen) {
            var uiState = screen.getUiState();
            var originalPreset = uiState.getWorldType().preset();

            if (originalPreset != null) {
                if (originalDefaultWorldPreset == null) {
                    originalDefaultWorldPreset = originalPreset;
                }
                if (originalDefaultWorldPreset.unwrapKey().equals(originalPreset.unwrapKey())) {
                    var skyblockWorldPreset = uiState.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET).getHolder(overrideDefaultWorldPreset()).orElse(null);
                    uiState.setWorldType(new WorldCreationUiState.WorldTypeEntry(skyblockWorldPreset));
                }
            }
        }
    }

    public static ResourceKey<WorldPreset> overrideDefaultWorldPreset() {
        return ModWorldPresets.SKYBLOCK_WORLD;
    }

    public static void disableVoidFogRendering() {
        isInSkyblockWorld = true;

        var level = Minecraft.getInstance().level;
        if (level != null) {
            // Somehow the clientLevelData is private, got to figure out how to access it
            //level.clientLevelData.isFlat = true;
        }
    }
}
