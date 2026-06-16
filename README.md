# HavenSkyblockBuilder

HavenSkyblockBuilder is a flexible and modpack-friendly skyblock builder mod. It adds a skyblock world preset, island templates, team islands, island visits, spawn islands, and full control over how islands are placed.

Everything is configurable: island distance, island height, spawn position, cooldowns, Nether skyblock generation, block layers, biome blacklist, extra structures, and island spawn offsets.

Whether you want a simple classic skyblock setup or a custom modpack island system, it gives modpack devs control without forcing one fixed layout.

---

## How It Works

Create a world with the **Haven Skyblock** world preset.

When the world starts:
* The spawn island is created
* Default template files are copied to the config folder
* New players without an island are sent to spawn
* Players can create their own island from an `.nbt` template
* Each island gets its own team
* Team leaders can manage member permissions, invites, join requests, island settings, and ownership

Players create islands with:

```text
/havensb island create classic_island My Island
```

Island creation is based on the selected template, the configured island distance, the island height, and the biome blacklist.

---

## World Preset

The mod adds a custom world preset:

```text
Haven Skyblock
```

World preset ID:

```text
haven_skyblock_builder:skyblock_world
```

For dedicated servers, set this before the world is generated:

```properties
level-type=haven_skyblock_builder:skyblock_world
```

The preset uses skyblock generation for the Overworld.

The Nether can either use skyblock generation or normal Minecraft generation depending on the config.

The End uses normal generation.

Vanilla and modded structures use the normal structure pipeline in skyblock dimensions and are controlled by `world_structures`.

---

## Templates

Island templates are `.nbt` structure files.

Templates are stored in:

```text
config/HavenSkyblockBuilder/Templates
```

The name is the file name without `.nbt`.

```text
classic_island.nbt = classic_island
```

To add more templates:

1. Add the `.nbt` file to `config/HavenSkyblockBuilder/Templates`
2. Restart the server

---

## Additional Islands

Additional islands are extra `.nbt` structures that spawn together with a main island template.

They are stored in:

```text
config/HavenSkyblockBuilder/AdditionalIslands
```

Additional islands are controlled in the common config.

Format:

```text
"main_template=extra_structure,xOffset,yOffset,zOffset"
```

Example:

```toml
[additional_structures]
additional_structures = ["classic_island=additional_sand_island,0,0,-75"]
```

This means:

* When `classic_island` is created
* Spawn `additional_sand_island.nbt`
* Place it 75 blocks away on Z

Example with more extras:

```toml
[additional_structures]
additional_structures = [
	"classic_island=additional_sand_island,0,0,-75",
	"classic_island=additional_jungle_island,0,0,75",
	"desert_island=additional_oasis_island,60,0,0"
]
```

The structure name must match the `.nbt` file name without `.nbt`.

---

## Spawn Island

The spawn island file is stored here:

```text
config/HavenSkyblockBuilder/spawn_island.nbt
```

On first world start, the mod creates the spawn island near world spawn.

A marker file is created here:

```text
world/serverconfig/hsb
```

If that marker already exists, the spawn island will not be created again.

This prevents the spawn island from being pasted every time the server starts.

---

## Teams

Each island has a team.

Players can:

* Create one island
* Invite players
* Accept or deny invites
* Leave a team
* Visit other islands if visits are enabled
* Return to island home
* Return to spawn

Team leaders can:

* Set island home
* Kick members
* Transfer leadership
* Rename the team
* Enable or disable visits
* Disband the team

Invites expire after 60 seconds.

Team data is saved per world in:

```text
world/serverconfig/HavenSkyblockBuilder/Teams
```

---

## Island Screen

Press `J` in-game to open the Haven Skyblock Builder screen.

The screen refreshes island data when it opens and shows:

* Every team as `Name - Member Count`
* Per-team Info, Visit, and Request Join
* A separate filters window for visits, join requests, empty teams, disbanded teams, and sorting
* Team settings for island spawn, visits, join requests, invitations, requests, and member permissions
* A rotatable and zoomable island template preview when creating an island

Use **Home Island** to return to your team island or **Teleport to Spawn** to return to world spawn.

---

## Commands

### Island Commands

```text
/havensb spawn
```

Teleports you to spawn.

```text
/havensb island create <template> <name>
```

Creates an island from a template and creates your team.

```text
/havensb island home
```

Teleports you to your island home.

```text
/havensb island info
```

Shows your island name, leader, visit status, home position, and members.

---

### Team Commands

```text
/havensb team list
```

Lists active teams.

```text
/havensb team invite <player>
```

Invites a player to your team.

```text
/havensb team accept
```

Accepts a team invite.

```text
/havensb team deny
```

Denies a team invite.

```text
/havensb team leave
```

Leaves your current team.

```text
/havensb team visit <team>
```

Visits another team island if visits are enabled.

```text
/havensb team deport <player>
```

Sends a visitor away from your island.

---

### Leader Commands

```text
/havensb leader sethome
```

Sets your island home to your current position.

```text
/havensb leader disband
```

Disbands your team.

```text
/havensb leader kick <player>
```

Kicks a member from your team.

```text
/havensb leader transfer <player>
```

Transfers team leadership to another member.

```text
/havensb leader allowvisit <true|false>
```

Allows or blocks island visits.

```text
/havensb leader changename <name>
```

Changes the team name.

---

### Admin Commands

Admin commands require permission level 2.

```text
/havensb admin reload
```

Reloads team data.

```text
/havensb admin listteams
```

Lists all teams.

```text
/havensb admin addmember <team> <player>
```

Adds a player to a team.

```text
/havensb admin removemember <team> <player>
```

Removes a player from a team.

```text
/havensb admin removeteam <team>
```

Removes a team and sends its members to spawn.

```text
/havensb admin changename <team> <name>
```

Changes a team name.

```text
/havensb admin generatejsons structures
```

Generates a vanilla and modded structure ID list.

```text
/havensb admin generatejsons biomes
```

Generates a biome ID list.

```text
/havensb admin generatejsons features
```

Generates a placed feature ID list.

```text
/havensb admin generatejsons carvers
```

Generates a carver ID list.

Generated lists are saved in:

```text
config/HavenSkyblockBuilder/generatedjsons
```

---

## Config Options

### Common Config (`haven_skyblock_builder-common.toml`)

These options affect islands, teams, teleporting, world generation, and template behavior.

* `island_creation_height`: Height where new islands are created
* `enable_nether_skyblock`: If false, the Nether generates normally
* `keep_inventory_on_island_leave`: If true, players keep inventory when leaving or being removed from islands
* `remove_disbanded_teams`: If true, disbanded team files are removed completely
* `island_specific_offsets`: Spawn offset and look direction for each island template
* `island_distance`: Distance between islands
* `spawn_position`: Spawn teleport position
* `additional_structures`: Extra structures pasted with island templates
* `home_cooldown`: Cooldown for `/havensb island home`
* `spawn_cooldown`: Cooldown for `/havensb spawn`
* `create_cooldown`: Cooldown for creating an island
* `visit_cooldown`: Cooldown for visiting another island
* `blacklist_biomes_for_islands`: Biomes where islands should not be placed
* `overworld_layer_config`: Optional Overworld block layers
* `nether_layer_config`: Optional Nether block layers
* `world_structures`: Structures allowed in skyblock generation
* `world_carvers`: Carvers allowed in skyblock generation
* `world_placed_features`: Placed features allowed in skyblock generation

---

## Default Common Config

```toml
#Island Creation Height
[island_creation_height]
#The height at which islands will be created
# Default: 70
# Range: > 1
island_creation_height = 70

#Enable Nether Skyblock
[enable_nether_skyblock]
#If false, it will generate regular nether.
enable_nether_skyblock = true

#Keep Inventory on Island Leave
[keep_inventory_on_island_leave]
#If true, players will keep their inventory when leaving the island.
keep_inventory_on_island_leave = true

#Island-Specific Spawn Offsets
[island_specific_offsets]
#The spawn offsets and look direction for specific islands.
#Format: "islandName=x,y,z,lookDirection".
#Example: [
#  "classic_island=0,1,0,90",  // Look east
#  "jungle_island=5,1,-3,180" // Look south
#]
#
island_specific_offsets = ["classic_island=1,1,-3,-90"]

#Island Distance
[island_distance]
#The distance between each island
# Default: 8192
# Range: > 1
island_distance = 8192

#World Spawn Position
[spawn_position]
#The X, Y, Z coordinates of the world spawn position.
#Example: ["0", "71", "0"] (Default spawn at 0, 70, 0).
#
spawn_position = ["0", "70", "0"]

#Additional Structures
[additional_structures]
#Additional structures to spawn for each island template.
#Format: "islandTemplate=structureName,xOffset,yOffset,zOffset".
#Examples:
#  - None: []
#  - One: ["classic_island=additional_sand_island,0,0,-75"]
#  - Two: [
#      "classic_island=additional_sand_island,0,0,-75",
#      "classic_island=additional_jungle_island,0,0,75"
#    ]
#
additional_structures = ["classic_island=additional_sand_island,0,0,-75"]

#Cooldowns
[cooldowns]
#Cooldown time (in seconds) for using the '/havensb island home' command.
# Default: 15
# Range: > 0
home_cooldown = 30
#Cooldown time (in seconds) for using the '/havensb spawn' command.
# Default: 5
# Range: > 0
spawn_cooldown = 5
#Cooldown time (in seconds) for creating a new island.
# Default: 30
# Range: > 0
create_cooldown = 15
#Cooldown time (in seconds) for visiting another team's island.
# Default: 10
# Range: > 0
visit_cooldown = 10

#Blacklist Biomes for Islands
[blacklist_biomes_for_islands]
#Biomes that are blacklisted for island generation.
blacklist_biomes_for_islands = ["minecraft:ocean", "minecraft:deep_ocean", "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:jagged_peaks", "minecraft:frozen_peaks", "minecraft:grove", "minecraft:snowy_slopes", "minecraft:windswept_hills", "minecraft:frozen_river", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:badlands", "minecraft:eroded_badlands"]

#Overworld Layer Configuration
[overworld_layer_config]
#Defines the block layers for the Overworld (can be empty), max 384 layers, format: block1,count*block2,block3
overworld_layer_config = ""

#Nether Layer Configuration
[nether_layer_config]
#Defines the block layers for the Nether (can be empty), max 256 layers, format: block1,count*block2,block3
nether_layer_config = "minecraft:bedrock,50*minecraft:lava"

#World Structures
[world_structures]
#Defines structures allowed in skyblock generation.
#
world_structures = [
    "minecraft:ancient_city",
    "minecraft:end_city",
    "minecraft:fortress",
    "minecraft:mineshaft",
    "minecraft:mineshaft_mesa",
    "minecraft:stronghold",
    "minecraft:trail_ruins",
    "minecraft:trial_chambers",
]

#World Carvers
[world_carvers]
#Defines the carvers for Overworld and Nether, format: "minecraft:carver1", "minecraft:carver2"
world_carvers = [
    "minecraft:canyon",
    "minecraft:cave",
    "minecraft:cave_extra_underground",
    "minecraft:nether_cave"
]

#World Placed Features
[world_placed_features]
#Defines the placed features for Overworld and Nether, format: "minecraft:feature1", "minecraft:feature2"
world_placed_features = []

#Remove Disbanded Teams
[remove_disbanded_teams]
#If true, disbanded team files are removed completely.
remove_disbanded_teams = true
```

---

## Layer Config

Layer config builds terrain from the bottom of the world upward.

Format:

```text
block1,count*block2,block3
```

Example:

```toml
[overworld_layer_config]
overworld_layer_config = "minecraft:bedrock,3*minecraft:dirt,minecraft:grass_block"
```

This creates:

* 1 layer of bedrock
* 3 layers of dirt
* 1 layer of grass block

Default Overworld skyblock uses no layers:

```toml
[overworld_layer_config]
overworld_layer_config = ""
```

Default Nether skyblock uses bedrock and lava:

```toml
[nether_layer_config]
nether_layer_config = "minecraft:bedrock,50*minecraft:lava"
```

This creates:

* 1 layer of bedrock
* 50 layers of lava

---

## Biome Blacklist

The biome blacklist stops islands from being placed in unwanted biomes.

Example:

```toml
[blacklist_biomes_for_islands]
blacklist_biomes_for_islands = [
	"minecraft:ocean",
	"minecraft:deep_ocean",
	"minecraft:frozen_ocean",
	"minecraft:badlands"
]
```

Generate a biome list with:

```text
/havensb admin generatejsons biomes
```

---

## Generated JSON Lists

The admin JSON commands generate valid IDs from the loaded server.

```text
/havensb admin generatejsons structures
/havensb admin generatejsons biomes
/havensb admin generatejsons features
/havensb admin generatejsons carvers
```

Files are saved in:

```text
config/HavenSkyblockBuilder/generatedjsons
```

## Good To Know
* Changing the world preset after world creation will not regenerate old chunks.
* The spawn island is only pasted once per world.
* Team data is saved per world.
* Removing a team does not delete the island blocks.
* Disbanded team files are removed when `remove_disbanded_teams` is enabled.
* If `keep_inventory_on_island_leave` is false, players inventory is cleared when leaving or being removed from an island.
* Template names are file names without `.nbt`.