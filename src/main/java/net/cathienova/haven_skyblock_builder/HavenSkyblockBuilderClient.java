package net.cathienova.haven_skyblock_builder;

import com.mojang.blaze3d.platform.InputConstants;
import net.cathienova.haven_skyblock_builder.screens.*;
import net.cathienova.haven_skyblock_builder.networking.ClientMessageHandler;
import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.cathienova.haven_skyblock_builder.networking.SkyblockWorldMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, value = Dist.CLIENT)
public class HavenSkyblockBuilderClient {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(HavenSkyblockBuilder.loc("key_category"));
    public static final KeyMapping OPEN_SKYBLOCK_SCREEN = new KeyMapping(
            "key.haven_skyblock_builder.open_skyblock_screen",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_M,
            CATEGORY
    );
    private static int ignoreOpenTicks;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_SKYBLOCK_SCREEN);
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(SkyblockWorldMessage.TYPE, (message, context) -> ClientMessageHandler.handleSkyblockWorldMessage(context));
        event.register(IslandScreenDataMessage.TYPE, ClientMessageHandler::handleIslandScreenData);
    }

    @EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, value = Dist.CLIENT)
    public static class GameEventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (ignoreOpenTicks > 0) {
                ignoreOpenTicks--;
                while (OPEN_SKYBLOCK_SCREEN.consumeClick()) {
                }
                return;
            }

            if (OPEN_SKYBLOCK_SCREEN.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen == null) {
                    minecraft.setScreen(new IslandScreen());
                }
            }
        }

        @SubscribeEvent
        public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            if (!isHavenScreen(event.getScreen()) || !InputConstants.getKey(event.getKeyEvent()).equals(OPEN_SKYBLOCK_SCREEN.getKey())) {
                return;
            }

            ignoreOpenTicks = 2;
            while (OPEN_SKYBLOCK_SCREEN.consumeClick()) {
            }
            Minecraft.getInstance().setScreen(null);
            event.setCanceled(true);
        }

        private static boolean isHavenScreen(Screen screen) {
            return screen instanceof IslandScreen
                    || screen instanceof CreateTeamScreen
                    || screen instanceof TeamFilterScreen
                    || screen instanceof TeamInfoScreen
                    || screen instanceof TeamOverviewScreen
                    || screen instanceof FindPlayerScreen;
        }
    }
}