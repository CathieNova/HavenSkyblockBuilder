package net.cathienova.haven_skyblock_builder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;


public class CommonConfig {
    public final ModConfigSpec.ConfigValue<Integer> islandCreationHeight;
    public final ModConfigSpec.ConfigValue<Boolean> enableNetherSkyblock;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnOffset;
    public final ModConfigSpec.ConfigValue<Integer> islandDistance;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnPosition;
    public final ModConfigSpec.ConfigValue<List<? extends String>> additionalStructures;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Island Creation Height").push("island_creation_height");
        islandCreationHeight = builder
                .comment("The height at which the island will be created")
                .defineInRange("island_creation_height", 70, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Enable Nether Skyblock").push("enable_nether_skyblock");
        enableNetherSkyblock = builder
                .comment("If true, it will generate a completely empty Nether world where the only feature is your portal.")
                .define("enable_nether_skyblock", true);

        builder.comment("Spawn on Island Offset")
                .push("spawn_offset");
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
                .defineList("spawn_offset", List.of("0", "1", "0"), obj -> {
                    if (!(obj instanceof String)) {
                        return false;
                    }
                    try {
                        Integer.parseInt((String) obj);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
        builder.pop();

        builder.comment("Island Distance").push("island_distance");
        islandDistance = builder
                .comment("The distance between each island")
                .defineInRange("island_offset", 2000, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("World Spawn Position").push("spawn_position");
        spawnPosition = builder
                .comment("""
                The X, Y, Z coordinates of the world spawn position.
                Example: ["0", "71", "0"] (Default spawn at 0, 70, 0).
                """)
                .defineList("spawn_position", List.of("0", "70", "0"), obj -> {
                    if (!(obj instanceof String)) {
                        return false;
                    }
                    try {
                        Integer.parseInt((String) obj);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
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

    }
}