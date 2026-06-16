package net.cathienova.haven_skyblock_builder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    private static final String RESOURCE_LOCATION_REGEX = "[a-z0-9_]+:[a-z0-9_/]+";
    public final ModConfigSpec.ConfigValue<Integer> islandCreationHeight;
    public final ModConfigSpec.ConfigValue<Boolean> enableNetherSkyblock;
    public final ModConfigSpec.ConfigValue<Boolean> keepInventoryOnIslandLeave;
    public final ModConfigSpec.ConfigValue<Boolean> removeDisbandedTeams;
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
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldStructures;
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldCarvers;
    public final ModConfigSpec.ConfigValue<List<? extends String>> worldPlacedFeatures;
    public final ModConfigSpec.ConfigValue<String> messagePrefix;

    public CommonConfig(ModConfigSpec.Builder builder) {

        builder.comment("Message Prefix").push("message_prefix");
        messagePrefix = builder
                .comment("Prefix shown before Haven Skyblock Builder chat messages. Color codes are supported.")
                .define("message_prefix", "§6[§5Haven §2Skyblock §3Builder§6]§r ");
        builder.pop();
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

        builder.comment("Remove Disbanded Teams").push("remove_disbanded_teams");
        removeDisbandedTeams = builder
                .comment("If true, disbanded team files are removed completely.")
                .define("remove_disbanded_teams", true);
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
                .defineListAllowEmpty("island_specific_offsets",
                        List.of("classic_island=1,1,-3,-90"),
                        () -> "classic_island=0,1,0,0",
                        obj -> {
                            if (!(obj instanceof String value)) {
                                return false;
                            }
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
                .defineList("spawn_position", List.of("0", "70", "0"), () -> "0", obj -> obj instanceof String && ((String) obj).matches("-?\\d+"));
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
                .defineListAllowEmpty("additional_structures",
                        List.of("classic_island=additional_sand_island,0,0,-75"),
                        () -> "classic_island=additional_sand_island,0,0,0",
                        obj -> {
                            if (!(obj instanceof String value)) {
                                return false;
                            }
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
                .defineInRange("home_cooldown", 15, 0, Integer.MAX_VALUE);

        spawnCooldown = builder
                .comment("Cooldown time (in seconds) for using the '/havensb spawn' command.")
                .defineInRange("spawn_cooldown", 5, 0, Integer.MAX_VALUE);

        islandCooldown = builder
                .comment("Cooldown time (in seconds) for creating a new island.")
                .defineInRange("create_cooldown", 30, 0, Integer.MAX_VALUE);

        visitCooldown = builder
                .comment("Cooldown time (in seconds) for visiting another team's island.")
                .defineInRange("visit_cooldown", 10, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Blacklist Biomes for Islands").push("blacklist_biomes_for_islands");
        blacklistBiomesForIslands = builder.comment("Biomes that are blacklisted for island generation.")
                .defineListAllowEmpty("blacklist_biomes_for_islands",
                        List.of("minecraft:ocean", "minecraft:deep_ocean", "minecraft:warm_ocean", "minecraft:lukewarm_ocean",
                                "minecraft:deep_lukewarm_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
                                "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:jagged_peaks",
                                "minecraft:frozen_peaks", "minecraft:grove", "minecraft:snowy_slopes", "minecraft:windswept_hills",
                                "minecraft:frozen_river", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:ice_spikes",
                                "minecraft:badlands", "minecraft:eroded_badlands"),
                        () -> "minecraft:plains",
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

        builder.comment("World Structures").push("world_structures");
        worldStructures = builder
                .comment("""
                        Defines structures allowed in skyblock generation.
                        """)
                .defineListAllowEmpty("world_structures", List.of(), () -> "minecraft:stronghold",
                        obj -> obj instanceof String && ("*".equals(obj) || ((String) obj).matches(RESOURCE_LOCATION_REGEX)));
        builder.pop();

        builder.comment("World Carvers").push("world_carvers");
        worldCarvers = builder
                .comment("Defines the carvers for Overworld and Nether, format: \"minecraft:carver1\", \"minecraft:carver2\"")
                .defineListAllowEmpty("world_carvers", List.of(), () -> "minecraft:cave",
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();

        builder.comment("World Placed Features").push("world_placed_features");
        worldPlacedFeatures = builder
                .comment("Defines the placed features for Overworld and Nether, format: \"minecraft:feature1\", \"minecraft:feature2\"")
                .defineListAllowEmpty("world_placed_features", List.of(),
                        () -> "minecraft:oak_checked",
                        obj -> obj instanceof String && ((String) obj).matches(RESOURCE_LOCATION_REGEX));
        builder.pop();
    }
}