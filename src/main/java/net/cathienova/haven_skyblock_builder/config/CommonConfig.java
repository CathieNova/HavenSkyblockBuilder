package net.cathienova.haven_skyblock_builder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;


public class CommonConfig {
    public final ModConfigSpec.ConfigValue<String> islandTemplate;
    public final ModConfigSpec.ConfigValue<Integer> islandCreationHeight;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnOffset;
    public final ModConfigSpec.ConfigValue<Integer> islandDistance;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Island Settings").push("island_settings");
        builder.comment("Island Template").push("island_template");
        islandTemplate = builder
                .comment("The name of the island template to  located in \"config/HavenSkyblockBuilder/templates\"")
                .define("skyblock_island", "skyblock_island");
        builder.pop();

        builder.comment("Island Creation Height").push("island_creation_height");
        islandCreationHeight = builder
                .comment("The height at which the island will be created")
                .defineInRange("island_creation_height", 70, 1, Integer.MAX_VALUE);
        builder.pop();

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
        builder.pop();
    }
}