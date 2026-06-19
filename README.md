# CustomBarrier

![Minecraft 1.19.2](https://img.shields.io/badge/Minecraft-1.19.2-blue?style=flat-square)
![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

A Fabric mod that adds a **configurable barrier block** with selective player pass-through, custom particle effects, optional opaque rendering, and an **effect block** that applies status effects on contact.

## Features

- **Custom Barrier Block** — selective player pass-through with configurable checks:
  - `TAG` — player must have specific scoreboard tags (comma-separated for multiple)
  - `PLAYER` — player's display name must match (comma-separated for multiple)
  - `MAINHAND` — player's main hand item registry ID must match
  - `PREDICATE` — evaluated via Minecraft's vanilla predicate system
- **Effect Block** — applies any status effect to entities that walk into it
- **Ethereal Effect** — a status effect granted on successful barrier check that allows temporary passthrough
- **Custom particles** — any particle type ID configurable per block
- **Opaque toggle** — barrier renders as End Gateway when enabled; invisible otherwise
- **Waterloggable** (both blocks)
- **In-game GUI** — creative-level-2-op players right-click to configure (offhand must not hold the respective block item)
- **No collision** until the player's barrier check passes

## Custom Barrier Block

A non-solid, indestructible barrier with selective pass-through logic.

### Usage

1. Place the Custom Barrier block
2. Right-click it as a creative op (offhand empty, not holding the barrier item)
3. Set the particle ID, check string, mode, and opaque toggle in the GUI
4. Press Save — settings persist via BlockEntity NBT

### Mode Reference

| Mode | Check field value | Behavior |
|------|-------------------|----------|
| `TAG` | Scoreboard tag (or comma-separated list) | Player must have **all** listed tags |
| `PLAYER` | Display name (or comma-separated list) | Player's display name must match |
| `MAINHAND` | Item registry ID (e.g. `minecraft:diamond`) | Player's main hand item must match |
| `PREDICATE` | Loot predicate ID | Evaluated via `PredicateManager` ([predicate generator](https://misode.github.io/predicate/)) |

## Effect Block

A solid, breakable block that applies a status effect to any living entity touching it.

### Usage

1. Place the Effect Block
2. Right-click it as a creative op (offhand empty, not holding the effect block item)
3. Enter the effect string: `<effect_id> <duration_ticks> <amplifier>` (e.g. `minecraft:speed 100 2`)
4. Press Save — any entity that walks through the block receives the effect

Default value: `minecraft:luck 0 1`

In creative mode, a green wireframe outline is visible when holding the Effect Block item in either hand.

## Requirements

- Minecraft **1.19.2**
- Fabric Loader **≥0.16.0**
- Fabric API **≥0.77.0+1.19.2**


## License

MIT
