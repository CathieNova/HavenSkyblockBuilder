package net.cathienova.haven_skyblock_builder;

import com.mojang.blaze3d.platform.InputConstants;
import net.cathienova.haven_skyblock_builder.Gui.IslandScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HavenSkyblockBuilderClient {
    public static final KeyMapping OPEN_SKYBLOCK_SCREEN = new KeyMapping(
            "key.haven_skyblock_builder.open_skyblock_screen",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_M,
            "key.categories.haven_skyblock_builder"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKYBLOCK_SCREEN);
    }

    @EventBusSubscriber(modid = HavenSkyblockBuilder.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (OPEN_SKYBLOCK_SCREEN.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                Screen currentScreen = minecraft.screen;
                if (currentScreen == null) {
                    minecraft.setScreen(new IslandScreen());
                }
            }
        }
    }
}
