package net.cathienova.haven_skyblock_builder.item;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HavenSkyblockBuilder.MOD_ID);

    public static String HavenSkyblockBuilder_tab_title = "itemgroup.haven_skyblock_builder.haven_skyblock_builder_tab";
    public static final Supplier<CreativeModeTab> Haven_TAB = CREATIVE_MODE_TABS.register("haven_skyblock_builder_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Blocks.STONE))
                    .title(Component.translatable(HavenSkyblockBuilder_tab_title))
                    .displayItems((pParameters, add) -> {

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
