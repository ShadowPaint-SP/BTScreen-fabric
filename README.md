<p align="center">
  <img src="src/main/resources/assets/btscreen/icon.png" width="120" height="120" alt="BT Screen icon">
</p>

<h1 align="center">BT Screen</h1>

<p align="center">
  A Fabric client mod for controlling Baritone through an in-game GUI and running longer jobs with automatic food, tools, storage, lighting, and safety stops.
</p>

BT Screen is built for repetitive Baritone work such as clearing large areas, draining liquids, farming, and building from Litematica schematics. It adds selection controls, task presets, reusable commands, and small automation processes that pause the current job, handle a problem, and continue it afterward.

The automation works best in a world or server with a teleportation system such as named homes or warps. With the right stations configured, the bot can leave its work area to sleep, repair equipment, collect haste, drop items, or restock supplies, then return to the mine and resume.

> Use automation only where it is allowed. Server rules often prohibit Baritone and unattended play.

## Supported versions

Current development targets Minecraft 26.2. Older versions have dedicated builds, but they do not receive every new feature and fix.

| Minecraft | BT Screen | Java | Status |
| --- | --- | --- | --- |
| 26.2 | [0.4.8](https://github.com/ShadowPaint-SP/BTScreen-fabric/releases/tag/0.4.8) | 25 or newer | Current |
| 1.21.10 | [0.4.6](https://github.com/ShadowPaint-SP/BTScreen-fabric/releases/tag/0.4.6) | 21 or newer | Legacy |
| 1.21.5 | [0.4.5](https://github.com/ShadowPaint-SP/BTScreen-fabric/releases/tag/0.4.5) | 21 or newer | Legacy |
| 1.21.4 | [0.3.0](https://github.com/ShadowPaint-SP/BTScreen-fabric/releases/tag/1.21.4-0.3.0) | 21 or newer | Legacy |

Do not mix mod files made for different Minecraft versions. Fabric Loader will reject most mismatches, but matching every dependency before launch saves time.

## Installation

1. Install the Fabric Loader for your Minecraft version.
2. Download the matching BT Screen jar from [GitHub Releases](https://github.com/ShadowPaint-SP/BTScreen-fabric/releases).
3. Install the matching versions of Fabric API, [Baritone for Meteor](https://maven.meteordev.org/#/snapshots/meteordevelopment/baritone), and MaLiLib.
4. Put all four mod jars in the Minecraft `mods` directory and start the game.

Litematica is optional. Install it if you want to use the `Building` preset.

The current Minecraft 26.2 build uses these versions:

| Dependency | Version |
| --- | --- |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.154.2+26.2 |
| Baritone for Meteor | 26.2-SNAPSHOT |
| MaLiLib | 26.2-0.29.2 |
| Mod Menu | Optional, 20.0.1 |

## Quick start

1. Join a world and press `P` to open BT Screen.
2. Click `POS 1` at one corner of the work area and `POS 2` at the opposite corner. `Sel Chunk` selects the current chunk instead.
3. Click the preset button at the bottom until it shows the job you want.
4. Open `Settings` and configure any automation you plan to use. Set up the teleport homes first if you enable station-based automation.
5. Return to the main screen and click `Start`.
6. Use `Pause Mining` when you want to inspect the job without losing it. Use `STOP` to cancel it.

`P` opens the main screen. `P` + `C` opens settings by default. You can change both bindings in `Settings` under `Hotkeys`. The custom-command screen and pause/resume action also have assignable hotkeys.

## Presets

The selected preset is stored per server dimension. BT Screen temporarily applies the Baritone settings needed by that preset and restores the previous settings when the job ends.

| Preset | What it does |
| --- | --- |
| None | Runs Baritone's regular `sel cleararea` command without applying a task-specific preset. |
| Clear Area | Clears the selection from the top down in five-block layers and protects the configured block lists. |
| Remove Liquid | Drains one selection layer by layer with blocks from `Blocks to replace liquids`. The selection must be at least 5 by 5 blocks. |
| Clear Area+ | Drains and then clears the selection in five-block slices. The selection must be at least 5 by 5 blocks. |
| Farming | Runs Baritone's `farm` process without allowing arbitrary block placement or breaking. |
| Building | Builds the active Litematica schematic one layer at a time. |
| Clean Bedrock | Removes solid blocks above bedrock in one selection. The selection can be at most four blocks high. |

`Remove Liquid`, `Clear Area+`, and `Clean Bedrock` require exactly one selection. Liquid removal also needs usable filler blocks in the inventory. BT Screen chooses from the configured filler list.

## Working with selections

The main screen exposes the Baritone selection tools needed for most jobs:

- `POS 1`, `POS 2`, `Sel Chunk`, `Sel Clear`, and `Sel Undo` create and manage selections.
- The six direction controls expand or shrink every selection. Left-click adds one block and right-click removes one block.
- Hold `Alt`, `Shift`, or `Ctrl` while clicking a direction to multiply the step by 5, 10, or 100. You can combine the modifiers.
- The `X`, `Y`, and `Z` controls move the entire selection instead of resizing it.
- `Caveman` clips selected areas to loaded underground subchunks below the surface.
- In the `None` and `Farming` presets, the extra controls can copy and paste a selection, set its blocks, build walls or a shell, and replace one block list with another.

The two text fields beside the extra controls contain the source block list and replacement block. Use Minecraft block IDs such as `stone` or `minecraft:stone`.

## Teleportation and stations

BT Screen's longer-running automation assumes that commands can move the player between the work area and prepared stations. A named-home plugin is the simplest setup, but fixed `/warp` commands work too.

### Named homes

The defaults expect commands shaped like `/home mine` and `/sethome mine`:

| Setting | Default | Meaning |
| --- | --- | --- |
| Home Command | `home` | Command used to visit a named home. Do not include `/`. |
| sethome Command | `sethome` | Command used to create or update a named home. Do not include `/`. |
| Mine Home | `mine` | Return point for the active job. BT Screen updates this before leaving the work area. |

Your home system must allow the Mine Home to be overwritten. When an automation needs a station, BT Screen pauses Baritone, updates the Mine Home, teleports away, performs the task, returns to the Mine Home, and lets Baritone continue.

### Fixed commands

If the server uses warps instead of named homes, put the complete command in a station setting and start it with `/`. For example, set Repair Home to `/warp xp-farm`. BT Screen sends that command as written instead of combining it with Home Command.

Mine Home can also contain a fixed command such as `/warp quarry`. In that case BT Screen will not update the return point automatically, so the command must always lead back to the correct work area.

### Station setup

| Home | Default | How to prepare it |
| --- | --- | --- |
| Sleep | `sleep` | Put usable beds directly north, south, east, or west of the arrival block. The sleep station must return the player to the same dimension as the Mine Home. |
| Drop | `drop` | Choose a safe place where thrown items will be collected. If you use auto-resupply, the configured supply chests must also be within interaction range when you arrive. |
| Haste | `haste` | Place the arrival point inside the range of a beacon that grants Haste. |
| Repair | `repair` | Use an XP farm and save the arrival direction so the player faces the mobs. BT Screen attacks while damaged equipment receives Mending XP. |
| Safety | Empty | Choose a location where an interrupted bot is safe. If this is empty, BT Screen tries Finished Home, then Sleep Home, then disconnects. |
| Finished | Empty | Optional destination used after a job completes normally. Leave it empty to stay at the work area. |

Test every teleport command manually before leaving the bot unattended. The arrival position and facing direction matter for repair, sleep, item collection, and chest access.

## Automation

Automation runs only while Baritone is active. All automation switches are off by default except `No Block Break Cooldown` and the location safety check.

| Option | Behavior and setup |
| --- | --- |
| Auto Eat | Eats food from the offhand when the configured food level is reached. Keep food in the offhand. |
| Auto Repair | Visits Repair Home when a damageable item reaches the durability threshold. Keep a sword in the hotbar if the XP farm needs sweeping attacks. |
| Auto Haste | Visits Haste Home whenever the Haste effect expires, waits for the beacon effect, and returns. |
| Auto Sleep | Visits Sleep Home at night, tries the four adjacent beds, sleeps, and returns. |
| Auto Drop | Uses inventory slots that were empty when the job started as collection slots. When they fill, it visits Drop Home and throws their contents. `Disallow Dropping` protects listed item IDs. |
| Auto Resupply | Tracks configured stackable items in the starting hotbar and offhand. At one item remaining, it visits Drop Home and fills the same slots from nearby chests. |
| Auto Torch | Places a torch at the player's feet when block light drops below the configured level. Keep torches in the hotbar. |
| Repeat Action | Runs the last job command again after the configured delay when it finishes normally. Pressing `STOP` cancels repetition. |
| No Block Break Cooldown | Removes the client-side block break delay while Baritone is active. |

### Configure auto-drop

Auto-drop deliberately touches only main-inventory slots that were empty when Baritone started. Your hotbar and the items already in the main inventory are left alone.

1. Put tools and supplies in their intended slots.
2. Leave at least `Minimum Drop Slots` empty in the main inventory. The default is two.
3. Add every item that must never be thrown to `Disallow Dropping`.
4. Set up Drop Home and enable `Auto Drop`.
5. Start the Baritone job. BT Screen records the empty collection slots at that moment.

If fewer than the configured number of slots are empty, BT Screen disables auto-drop instead of risking the rest of the inventory.

### Configure auto-resupply

Auto-resupply uses the item layout present when Baritone starts. It tracks at most one hotbar or offhand slot for each configured item ID. Empty slots and non-stackable items are ignored.

1. Place each supply in the hotbar or offhand slot where it should stay during the job.
2. At Drop Home, place one or more chests within the player's normal interaction range.
3. Open `Settings`, select `Lists`, and edit `Resupply Sources`.
4. Replace the example rows with each item ID and the chest's absolute X, Y, and Z coordinates.
5. Add more rows with the same item ID to use fallback chests in that order.
6. Enable `Auto Resupply`, then start the Baritone job so BT Screen records the supply slots.

When a tracked stack reaches one item, BT Screen visits Drop Home and fills its original slot to the item's maximum stack size. The item and its components must match the original stack. Keep the supply chests stocked and make sure no other container blocks chest access.

Auto-drop and auto-resupply can share the same Drop Home. Auto-resupply reads only the chests listed in `Resupply Sources`.

## Safety controls

- `Safety: Min Health` cancels the active job when health reaches the configured value, then sends the player to Safety Home.
- `Safety: Location Check` cancels if the player suddenly moves more than five blocks between checks or changes dimension outside BT Screen's own teleport flow.
- If Safety Home is empty, the fallback order is Finished Home, Sleep Home, then disconnect.
- `STOP` cancels Baritone, pending repetition, teleports, and BT Screen processes. Use it before moving the player or changing the setup.

Automation reduces routine failures, but it cannot make an unloaded area, unsafe station, broken command, or empty supply chest safe. Watch a complete cycle before trusting a setup.

## Custom commands

The Custom Commands screen creates named buttons on the main screen for server commands you use often. Separate several commands with `;` to send them in order. Enter commands without the leading `/`.

Custom commands are stored per server, so a button made for one server will not appear on another.

## Building from source

The current branch requires JDK 25.

```sh
./gradlew build
```

Gradle writes the jar to `build/libs/`.

## Credits and license

BT Screen is based on [thojo0/jsmacro-btscreen](https://github.com/thojo0/jsmacro-btscreen) and uses [Baritone](https://github.com/cabaletta/baritone).

The project is licensed under [GPL-3.0](LICENSE).
