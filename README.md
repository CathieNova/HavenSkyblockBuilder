# HavenSkyblockBuilder

Haven Skyblock Builder adds tools to create, manage, and customize Skyblock islands. Features include team management, custom templates, dynamic spawns, and advanced admin controls for a better Skyblock experience.

Everything is configurable: island distance, island height, spawn position, cooldowns, Nether skyblock generation, block layers, biome blacklist, extra structures, and island spawn offsets.

Whether you want a simple classic skyblock setup or a more custom world, it gives modpack devs control without forcing one fixed layout.

---

## How It Works

Create a world with the **Haven Skyblock** world preset.

When the world starts:

* The spawn island is created
* Default template files are copied to the config folder
* New players without an island are sent to spawn
* Players can create their own island from an `.nbt` template
* Each island gets its own team
* Team leaders can invite, kick, rename, visit-toggle, and transfer ownership

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

---

## Templates

Island templates are `.nbt` structure files.

Templates are stored in:

```text
config/HavenSkyblockBuilder/Templates
```

The default template is:

```text
classic_island.nbt
```

The command name is the file name without `.nbt`.

```text
classic_island.nbt = classic_island
```

Example:

```text
/havensb island create classic_island My Island
```

To add more templates:

1. Add the `.nbt` file to `config/HavenSkyblockBuilder/Templates`
2. Restart the server if needed
3. Use the file name in the island create command

Example files:

```text
config/HavenSkyblockBuilder/Templates/classic_island.nbt
config/HavenSkyblockBuilder/Templates/desert_island.nbt
config/HavenSkyblockBuilder/Templates/jungle_island.nbt
```

Example commands:

```text
/havensb island create desert_island Desert Base
/havensb island create jungle_island Jungle Base
```

---

## Additional Islands

Additional islands are extra `.nbt` structures that spawn together with a main island template.

They are stored in:

```text
config/HavenSkyblockBuilder/AdditionalIslands
```

Default file:

```text
additional_sand_island.nbt
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
* `world_carvers`: Carvers allowed in skyblock generation
* `nether_placed_features`: Placed features allowed in skyblock generation

---

## Default Common Config

```toml
[island_creation_height]
	#The height at which islands will be created
	#Range: 1 ~ 2147483647
	island_creation_height = 70

[enable_nether_skyblock]
	#If false, it will generate regular nether.
	enable_nether_skyblock = true

[keep_inventory_on_island_leave]
	#If true, players will keep their inventory when leaving the island.
	keep_inventory_on_island_leave = true

[island_specific_offsets]
	#The spawn offsets and look direction for specific islands.
	#Format: "islandName=x,y,z,lookDirection".
	island_specific_offsets = ["classic_island=1,1,-3,-90"]

[island_distance]
	#The distance between each island
	#Range: 1 ~ 2147483647
	island_distance = 8192

[spawn_position]
	#The X, Y, Z coordinates of the world spawn position.
	spawn_position = ["0", "70", "0"]

[additional_structures]
	#Additional structures to spawn for each island template.
	#Format: "islandTemplate=structureName,xOffset,yOffset,zOffset".
	additional_structures = ["classic_island=additional_sand_island,0,0,-75"]

[cooldowns]
	#Cooldown time in seconds for using the '/havensb island home' command.
	#Range: 0 ~ 2147483647
	home_cooldown = 30
	#Cooldown time in seconds for using the '/havensb spawn' command.
	#Range: 0 ~ 2147483647
	spawn_cooldown = 5
	#Cooldown time in seconds for creating a new island.
	#Range: 0 ~ 2147483647
	create_cooldown = 120
	#Cooldown time in seconds for visiting another team's island.
	#Range: 0 ~ 2147483647
	visit_cooldown = 30

[blacklist_biomes_for_islands]
	#Biomes that are blacklisted for island generation.
	blacklist_biomes_for_islands = [
		"minecraft:ocean",
		"minecraft:deep_ocean",
		"minecraft:warm_ocean",
		"minecraft:lukewarm_ocean",
		"minecraft:deep_lukewarm_ocean",
		"minecraft:cold_ocean",
		"minecraft:deep_cold_ocean",
		"minecraft:frozen_ocean",
		"minecraft:deep_frozen_ocean",
		"minecraft:jagged_peaks",
		"minecraft:frozen_peaks",
		"minecraft:grove",
		"minecraft:snowy_slopes",
		"minecraft:windswept_hills",
		"minecraft:frozen_river",
		"minecraft:snowy_beach",
		"minecraft:snowy_plains",
		"minecraft:ice_spikes",
		"minecraft:badlands",
		"minecraft:eroded_badlands"
	]

[overworld_layer_config]
	#Defines the block layers for the Overworld. Can be empty.
	#Max 384 layers, format: block1,count*block2,block3
	overworld_layer_config = ""

[nether_layer_config]
	#Defines the block layers for the Nether. Can be empty.
	#Max 256 layers, format: block1,count*block2,block3
	nether_layer_config = "minecraft:bedrock,50*minecraft:lava"

[world_carvers]
	#Defines the carvers for Overworld and Nether.
	world_carvers = [
		"minecraft:cave",
		"minecraft:canyon",
		"minecraft:minecraft:cave_extra_underground",
		"minecraft:nether_cave"
	]

[world_placed_features]
	#Defines the placed features for Overworld and Nether.
	nether_placed_features = [
		"minecraft:glowstone",
		"minecraft:ore_quartz_nether",
		"minecraft:ore_gold_nether"
	]
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

## Config Examples

### Classic Void Skyblock

Use this for normal void skyblock.

```toml
[overworld_layer_config]
overworld_layer_config = ""

[enable_nether_skyblock]
enable_nether_skyblock = true

[nether_layer_config]
nether_layer_config = "minecraft:bedrock,50*minecraft:lava"
```

---

### Normal Nether

Use this if you want skyblock Overworld but normal Nether.

```toml
[enable_nether_skyblock]
enable_nether_skyblock = false
```

---

### Closer Islands

Use this if you want islands closer together.

```toml
[island_distance]
island_distance = 2048
```

Do not lower this too much on an existing world. New islands may generate too close to old islands.

---

### No Extra Islands

Use this if you only want the main template to spawn.

```toml
[additional_structures]
additional_structures = []
```

---

### Multiple Island Templates

```toml
[island_specific_offsets]
island_specific_offsets = [
	"classic_island=1,1,-3,-90",
	"desert_island=0,1,0,180",
	"jungle_island=2,1,2,90"
]

[additional_structures]
additional_structures = [
	"classic_island=additional_sand_island,0,0,-75",
	"desert_island=additional_oasis_island,60,0,0",
	"jungle_island=additional_tree_island,-60,0,0"
]
```

Needed files:

```text
config/HavenSkyblockBuilder/Templates/classic_island.nbt
config/HavenSkyblockBuilder/Templates/desert_island.nbt
config/HavenSkyblockBuilder/Templates/jungle_island.nbt

config/HavenSkyblockBuilder/AdditionalIslands/additional_sand_island.nbt
config/HavenSkyblockBuilder/AdditionalIslands/additional_oasis_island.nbt
config/HavenSkyblockBuilder/AdditionalIslands/additional_tree_island.nbt
```

---

## Template Example

This example adds a desert island template with its own spawn position and an extra oasis island.

Files:

```text
config/HavenSkyblockBuilder/Templates/desert_island.nbt
config/HavenSkyblockBuilder/AdditionalIslands/additional_oasis_island.nbt
```

Config:

```toml
[island_specific_offsets]
island_specific_offsets = [
	"classic_island=1,1,-3,-90",
	"desert_island=0,1,0,180"
]

[additional_structures]
additional_structures = [
	"classic_island=additional_sand_island,0,0,-75",
	"desert_island=additional_oasis_island,50,0,0"
]
```

Command:

```text
/havensb island create desert_island Desert Base
```

What happens:

* `desert_island.nbt` is pasted as the main island
* `additional_oasis_island.nbt` is pasted 50 blocks away on X
* The player spawns using the desert island offset
* A team named `Desert Base` is created
* The player becomes team leader

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
/havensb admin generatejsons biomes
/havensb admin generatejsons features
/havensb admin generatejsons carvers
```

Files are saved in:

```text
config/HavenSkyblockBuilder/generatedjsons
```
