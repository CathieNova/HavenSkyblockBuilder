package net.cathienova.haven_skyblock_builder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;


public class CommonConfig {
    public final ModConfigSpec.ConfigValue<Integer> islandCreationHeight;
    public final ModConfigSpec.ConfigValue<Boolean> enableNetherSkyblock;
    public final ModConfigSpec.ConfigValue<Boolean> keepInventoryOnIslandLeave;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnOffset;
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

        builder.comment("Spawn on Island Offset")
                .push("spawn_on_island_offset");
        spawnOffset = builder
                .comment("""
        The X, Y, Z offset from the center of the island where the player will spawn.
        Example offsets:
        - North: [0, 1, -5] (5 blocks north of center)
        - South: [0, 1, 5] (5 blocks south of center)
        - West: [-5, 1, 0] (5 blocks west of center)
        - East: [5, 1, 0] (5 blocks east of center)
        Default is [0, 1, 0] (center with 1 block height).
        """)
                .defineList("spawn_on_island_offset", List.of("0", "1", "0"), obj -> obj instanceof String && ((String) obj).matches("-?\\d+"));

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
                .defineInRange("cooldowns", 30, 0, Integer.MAX_VALUE);

        spawnCooldown = builder
                .comment("Cooldown time (in seconds) for using the '/havensb spawn' command.")
                .defineInRange("cooldowns", 5, 0, Integer.MAX_VALUE);

        islandCooldown = builder
                .comment("Cooldown time (in seconds) for creating a new island.")
                .defineInRange("cooldowns", 120, 0, Integer.MAX_VALUE);

        visitCooldown = builder
                .comment("Cooldown time (in seconds) for visiting another team's island.")
                .defineInRange("cooldowns", 30, 0, Integer.MAX_VALUE);
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
                        obj -> obj instanceof String && ((String) obj).matches("minecraft:[a-z_]+"));
        builder.pop();

        String overworldLayerConfig = "minecraft:bedrock,30*minecraft:stone,20*minecraft:dirt,minecraft:grass_block";

        builder.comment("Overworld Layer Configuration").push("overworld_layer_config");
        overworldLayerGeneration = builder
                .comment("Defines the block layers for the Overworld, max 384 layers, format: block1,count*block2,block3")
                .define("overworld_layer_config", overworldLayerConfig);
        builder.pop();

        String netherLayerConfig = "minecraft:bedrock,50*minecraft:lava";

        builder.comment("Nether Layer Configuration").push("nether_layer_config");
        netherLayerGeneration = builder
                .comment("Defines the block layers for the Nether, max 256 layers, format: block1,count*block2,block3")
                .define("nether_layer_config", netherLayerConfig);
        builder.pop();
    }
}