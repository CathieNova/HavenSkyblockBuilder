package net.cathienova.haven_skyblock_builder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;


public class CommonConfig {
    public final ModConfigSpec.ConfigValue<Integer> islandCreationHeight;
    public final ModConfigSpec.ConfigValue<Boolean> enableNetherSkyblock;
    public final ModConfigSpec.ConfigValue<Boolean> keepInventoryOnIslandLeave;
    public final ModConfigSpec.ConfigValue<List<? extends String>> islandSpecificOffsets;
    public final ModConfigSpec.ConfigValue<Integer> islandDistance;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnPosition;
    public final ModConfigSpec.ConfigValue<List<? extends String>> additionalStructures;
    public final ModConfigSpec.ConfigValue<Integer> homeCooldown;
    public final ModConfigSpec.ConfigValue<Integer> spawnCooldown;
    public final ModConfigSpec.ConfigValue<Integer> islandCooldown;
    public final ModConfigSpec.ConfigValue<Integer> visitCooldown;
    public final ModConfigSpec.ConfigValue<List<? extends String>> blacklistBiomesForIslands;
    public final ModConfigSpec.ConfigValue<String> overworldLayerGeneration;
    public final ModConfigSpec.ConfigValue<String> netherLayerGeneration;
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldCarvers;
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldPlacedFeatures;
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldStructures;

    private static final String RESOURCE_LOCATION_REGEX = "[a-z0-9_]+:[a-z0-9_/]+";

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Island Creation Height").push("island_creation_height");
        islandCreationHeight = builder
                .comment("The height at which islands will be created")
                .defineInRange("island_creation_height", 70, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Enable Nether Skyblock").push("enable_nether_skyblock");
        enableNetherSkyblock = builder
                .comment("If false, it will generate regular nether.")
                .define("enable_nether_skyblock", true);
        builder.pop();

        builder.comment("Keep Inventory on Island Leave").push("keep_inventory_on_island_leave");
        keepInventoryOnIslandLeave = builder
                .comment("If true, players will keep their inventory when leaving the island.")
                .define("keep_inventory_on_island_leave", true);
        builder.pop();

        builder.comment("Island-Specific Spawn Offsets").push("island_specific_offsets");
        islandSpecificOffsets = builder
                .comment("""
        The spawn offsets and look direction for specific islands. 
        Format: "islandName=x,y,z,lookDirection".
        Example: [
          "classic_island=0,1,0,90",  // Look east
          "jungle_island=5,1,-3,180" // Look south
        ]
        """)
                .defineList("island_specific_offsets",
                        List.of("classic_island=1,1,-3,-90"),
                        obj -> {
                            if (!(obj instanceof String)) {
                                return false;
                            }
                            String value = (String) obj;
                            String[] parts = value.split("=");
                            if (parts.length != 2) {
                                return false;
                            }
                            String[] values = parts[1].split(",");
                            if (values.length != 4) {
                                return false;
                            }
                            try {
                                Integer.parseInt(values[0]); // x
                                Integer.parseInt(values[1]); // y
                                Integer.parseInt(values[2]); // z
                                Integer.parseInt(values[3]); // lookDirection
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
        builder.pop();

        builder.comment("Island Distance").push("island_distance");
        islandDistance = builder
                .comment("The distance between each island")
                .defineInRange("island_distance", 8192, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("World Spawn Position").push("spawn_position");
        spawnPosition = builder
                .comment("""
        The X, Y, Z coordinates of the world spawn position.
        Example: ["0", "71", "0"] (Default spawn at 0, 70, 0).
        """)
                .defineList("spawn_position", List.of("0", "70", "0"), obj -> obj instanceof String && ((String) obj).matches("-?\\d+"));
        builder.pop();

        builder.comment("Additional Structures").push("additional_structures");
        additionalStructures = builder
                .comment("""
                Additional structures to spawn for each island template.
                Format: "islandTemplate=structureName,xOffset,yOffset,zOffset".
                Examples:
                  - None: []
                  - One: ["classic_island=additional_sand_island,0,0,-75"]
                  - Two: [
                      "classic_island=additional_sand_island,0,0,-75",
                      "classic_island=additional_jungle_island,0,0,75"
                    ]
                """)
                .defineList("additional_structures",
                        List.of("classic_island=additional_sand_island,0,0,-75"),
                        obj -> {
                            if (!(obj instanceof String)) {
                                return false;
                            }
                            String value = (String) obj;
                            String[] parts = value.split("=");
                            if (parts.length != 2) {
                                return false;
                            }
                            String[] offsets = parts[1].split(",");
                            if (offsets.length != 4) {
                                return false;
                            }
                            try {
                                Integer.parseInt(offsets[1]);
                                Integer.parseInt(offsets[2]);
                                Integer.parseInt(offsets[3]);
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
        builder.pop();

        builder.comment("Cooldowns").push("cooldowns");
        homeCooldown = builder
                .comment("Cooldown time (in seconds) for using the '/havensb island home' command.")
                .defineInRange("home_cooldown", 30, 0, Integer.MAX_VALUE);

        spawnCooldown = builder
                .comment("Cooldown time (in seconds) for using the '/havensb spawn' command.")
                .defineInRange("spawn_cooldown", 5, 0, Integer.MAX_VALUE);

        islandCooldown = builder
                .comment("Cooldown time (in seconds) for creating a new island.")
                .defineInRange("create_cooldown", 120, 0, Integer.MAX_VALUE);

        visitCooldown = builder
                .comment("Cooldown time (in seconds) for visiting another team's island.")
                .defineInRange("visit_cooldown", 30, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Blacklist Biomes for Islands").push("blacklist_biomes_for_islands");
        blacklistBiomesForIslands = builder.comment("Biomes that are blacklisted for island generation.")
                .defineList("blacklist_biomes_for_islands",
                        List.of("minecraft:ocean", "minecraft:deep_ocean", "minecraft:warm_ocean", "minecraft:lukewarm_ocean",
                                "minecraft:deep_lukewarm_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
                                "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:jagged_peaks",
                                "minecraft:frozen_peaks", "minecraft:grove", "minecraft:snowy_slopes", "minecraft:windswept_hills",
                                "minecraft:frozen_river", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:ice_spikes",
                                "minecraft:badlands", "minecraft:eroded_badlands"),
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();

        String overworldLayerConfig = "";

        builder.comment("Overworld Layer Configuration").push("overworld_layer_config");
        overworldLayerGeneration = builder
                .comment("Defines the block layers for the Overworld (can be empty), max 384 layers, format: block1,count*block2,block3")
                .define("overworld_layer_config", overworldLayerConfig);
        builder.pop();

        String netherLayerConfig = "minecraft:bedrock,50*minecraft:lava";

        builder.comment("Nether Layer Configuration").push("nether_layer_config");
        netherLayerGeneration = builder
                .comment("Defines the block layers for the Nether (can be empty), max 256 layers, format: block1,count*block2,block3")
                .define("nether_layer_config", netherLayerConfig);
        builder.pop();

        builder.comment("World Carvers").push("world_carvers");
        worldCarvers = builder
                .comment("Defines the carvers for Overworld and Nether, format: minecraft:carver1,minecraft:carver2")
                .defineList("world_carvers", List.of("minecraft:cave", "minecraft:canyon", "minecraft:minecraft:cave_extra_underground", "minecraft:nether_cave"),
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();

        builder.comment("World Placed Features").push("world_placed_features");
        worldPlacedFeatures = builder
                .comment("Defines the placed features for Overworld and Nether, format: minecraft:feature1,minecraft:feature2")
                .defineList("nether_placed_features", List.of("minecraft:acacia", "minecraft:acacia_checked", "minecraft:amethyst_geode", "minecraft:bamboo", "minecraft:bamboo_light", "minecraft:bamboo_vegetation", "minecraft:basalt_blobs", "minecraft:basalt_pillar", "minecraft:birch_bees_0002", "minecraft:birch_bees_002", "minecraft:birch_checked", "minecraft:birch_tall", "minecraft:blackstone_blobs", "minecraft:blue_ice", "minecraft:brown_mushroom_nether", "minecraft:brown_mushroom_normal", "minecraft:brown_mushroom_old_growth", "minecraft:brown_mushroom_swamp", "minecraft:brown_mushroom_taiga", "minecraft:cave_vines", "minecraft:cherry_bees_005", "minecraft:cherry_checked", "minecraft:chorus_plant", "minecraft:classic_vines_cave_feature", "minecraft:crimson_forest_vegetation", "minecraft:crimson_fungi", "minecraft:dark_forest_vegetation", "minecraft:dark_oak_checked", "minecraft:delta", "minecraft:desert_well", "minecraft:disk_clay", "minecraft:disk_grass", "minecraft:disk_gravel", "minecraft:disk_sand", "minecraft:dripstone_cluster", "minecraft:end_gateway_return", "minecraft:end_island_decorated", "minecraft:end_platform", "minecraft:end_spike", "minecraft:fancy_oak_bees", "minecraft:fancy_oak_bees_0002", "minecraft:fancy_oak_bees_002", "minecraft:fancy_oak_checked", "minecraft:flower_cherry", "minecraft:flower_default", "minecraft:flower_flower_forest", "minecraft:flower_forest_flowers", "minecraft:flower_meadow", "minecraft:flower_plain", "minecraft:flower_plains", "minecraft:flower_swamp", "minecraft:flower_warm", "minecraft:forest_flowers", "minecraft:forest_rock", "minecraft:fossil_lower", "minecraft:fossil_upper", "minecraft:freeze_top_layer", "minecraft:glow_lichen", "minecraft:glowstone", "minecraft:glowstone_extra", "minecraft:grass_bonemeal", "minecraft:ice_patch", "minecraft:ice_spike", "minecraft:iceberg_blue", "minecraft:iceberg_packed", "minecraft:jungle_bush", "minecraft:jungle_tree", "minecraft:kelp_cold", "minecraft:kelp_warm", "minecraft:lake_lava_surface", "minecraft:lake_lava_underground", "minecraft:large_basalt_columns", "minecraft:large_dripstone", "minecraft:lush_caves_ceiling_vegetation", "minecraft:lush_caves_clay", "minecraft:lush_caves_vegetation", "minecraft:mangrove_checked", "minecraft:mega_jungle_tree_checked", "minecraft:mega_pine_checked", "minecraft:mega_spruce_checked", "minecraft:monster_room", "minecraft:monster_room_deep", "minecraft:mushroom_island_vegetation", "minecraft:nether_sprouts", "minecraft:oak", "minecraft:oak_bees_0002", "minecraft:oak_bees_002", "minecraft:oak_checked", "minecraft:ore_ancient_debris_large", "minecraft:ore_andesite_lower", "minecraft:ore_andesite_upper", "minecraft:ore_blackstone", "minecraft:ore_clay", "minecraft:ore_coal_lower", "minecraft:ore_coal_upper", "minecraft:ore_copper", "minecraft:ore_copper_large", "minecraft:ore_debris_small", "minecraft:ore_diamond", "minecraft:ore_diamond_buried", "minecraft:ore_diamond_large", "minecraft:ore_diamond_medium", "minecraft:ore_diorite_lower", "minecraft:ore_diorite_upper", "minecraft:ore_dirt", "minecraft:ore_emerald", "minecraft:ore_gold", "minecraft:ore_gold_deltas", "minecraft:ore_gold_extra", "minecraft:ore_gold_lower", "minecraft:ore_gold_nether", "minecraft:ore_granite_lower", "minecraft:ore_granite_upper", "minecraft:ore_gravel", "minecraft:ore_gravel_nether", "minecraft:ore_infested", "minecraft:ore_iron_middle", "minecraft:ore_iron_small", "minecraft:ore_iron_upper", "minecraft:ore_lapis", "minecraft:ore_lapis_buried", "minecraft:ore_magma", "minecraft:ore_quartz_deltas", "minecraft:ore_quartz_nether", "minecraft:ore_redstone", "minecraft:ore_redstone_lower", "minecraft:ore_soul_sand", "minecraft:ore_tuff", "minecraft:patch_berry_bush", "minecraft:patch_berry_common", "minecraft:patch_berry_rare", "minecraft:patch_cactus", "minecraft:patch_cactus_decorated", "minecraft:patch_cactus_desert", "minecraft:patch_crimson_roots", "minecraft:patch_dead_bush", "minecraft:patch_dead_bush_2", "minecraft:patch_dead_bush_badlands", "minecraft:patch_fire", "minecraft:patch_grass_badlands", "minecraft:patch_grass_forest", "minecraft:patch_grass_jungle", "minecraft:patch_grass_normal", "minecraft:patch_grass_plain", "minecraft:patch_grass_savanna", "minecraft:patch_grass_taiga", "minecraft:patch_grass_taiga_2", "minecraft:patch_large_fern", "minecraft:patch_melon", "minecraft:patch_melon_sparse", "minecraft:patch_pumpkin", "minecraft:patch_soul_fire", "minecraft:patch_sugar_cane", "minecraft:patch_sugar_cane_badlands", "minecraft:patch_sugar_cane_desert", "minecraft:patch_sugar_cane_swamp", "minecraft:patch_sunflower", "minecraft:patch_taiga_grass", "minecraft:patch_tall_grass", "minecraft:patch_tall_grass_2", "minecraft:patch_waterlily", "minecraft:pile_hay", "minecraft:pile_ice", "minecraft:pile_melon", "minecraft:pile_pumpkin", "minecraft:pile_snow", "minecraft:pine", "minecraft:pine_checked", "minecraft:pine_on_snow", "minecraft:pointed_dripstone", "minecraft:red_mushroom_nether", "minecraft:red_mushroom_normal", "minecraft:red_mushroom_old_growth", "minecraft:red_mushroom_swamp", "minecraft:red_mushroom_taiga", "minecraft:rooted_azalea_tree", "minecraft:sculk_patch_ancient_city", "minecraft:sculk_patch_deep_dark", "minecraft:sculk_vein", "minecraft:sea_pickle", "minecraft:seagrass_cold", "minecraft:seagrass_deep", "minecraft:seagrass_deep_cold", "minecraft:seagrass_deep_warm", "minecraft:seagrass_normal", "minecraft:seagrass_river", "minecraft:seagrass_simple", "minecraft:seagrass_swamp", "minecraft:seagrass_warm", "minecraft:small_basalt_columns", "minecraft:spore_blossom", "minecraft:spring_closed", "minecraft:spring_closed_double", "minecraft:spring_delta", "minecraft:spring_lava", "minecraft:spring_lava_frozen", "minecraft:spring_open", "minecraft:spring_water", "minecraft:spruce", "minecraft:spruce_checked", "minecraft:spruce_on_snow", "minecraft:super_birch_bees", "minecraft:super_birch_bees_0002", "minecraft:tall_mangrove_checked", "minecraft:trees_badlands", "minecraft:trees_birch", "minecraft:trees_birch_and_oak", "minecraft:trees_cherry", "minecraft:trees_flower_forest", "minecraft:trees_grove", "minecraft:trees_jungle", "minecraft:trees_mangrove", "minecraft:trees_meadow", "minecraft:trees_old_growth_pine_taiga", "minecraft:trees_old_growth_spruce_taiga", "minecraft:trees_plains", "minecraft:trees_savanna", "minecraft:trees_snowy", "minecraft:trees_sparse_jungle", "minecraft:trees_swamp", "minecraft:trees_taiga", "minecraft:trees_water", "minecraft:trees_windswept_forest", "minecraft:trees_windswept_hills", "minecraft:trees_windswept_savanna", "minecraft:twisting_vines", "minecraft:underwater_magma", "minecraft:vines", "minecraft:void_start_platform", "minecraft:warm_ocean_vegetation", "minecraft:warped_forest_vegetation", "minecraft:warped_fungi", "minecraft:weeping_vines"),
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();

        builder.comment("World Structures").push("world_structures");
        worldStructures = builder
                .comment("Defines the structures for Overworld and Nether, format: minecraft:structure1,minecraft:structure2")
                .defineList("world_structures", List.of("minecraft:ancient_city", "minecraft:bastion_remnant", "minecraft:buried_treasure", "minecraft:desert_pyramid", "minecraft:end_city", "minecraft:fortress", "minecraft:igloo", "minecraft:jungle_pyramid", "minecraft:mansion", "minecraft:mineshaft", "minecraft:mineshaft_mesa", "minecraft:monument"),
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();
    }
}