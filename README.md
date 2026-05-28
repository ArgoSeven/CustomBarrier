# CustomBarrier

![Minecraft 1.19.2](https://img.shields.io/badge/Minecraft-1.19.2-blue?style=flat-square)
![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

A Fabric mod that adds a **configurable barrier block** with selective player pass-through, custom particle effects, and optional opaque rendering.

## Features

- **Three pass-through modes** configurable per-block via an in-game GUI:
  - `TAG` — player must have specific scoreboard tags (comma-separated for multiple)
  - `PLAYER` — player's display name must match (comma-separated for multiple)
  - `PREDICATE` — this mode will use minecraft vanilla predicate system
- **Ethereal Effect** — a status effect granted on successful check that allows temporary passthrough (10 ticks)
- **Custom particles** — any particle type ID configurable per block
- **Opaque toggle** — renders as an End Gateway when enabled; invisible otherwise
- **Waterloggable**
- **Configuration GUI** — creative-level-2-op players right-click (offhand empty, not holding the barrier) to open
- **No collision** until the player's check passes

## Usage

1. Place the Custom Barrier block
2. Right-click it as a creative op (offhand empty, not holding the barrier item)
3. Set the particle ID, check string, mode, and opaque toggle in the GUI
4. Press Save — settings persist via BlockEntity NBT

## Mode Reference

| Mode | Check field value | Behavior |
|------|-------------------|----------|
| `TAG` | Scoreboard tag (or comma-separated list) | Player must have **all** listed tags |
| `PLAYER` | Display name (or comma-separated list) | Player's display name must match |
| `PREDICATE` | Loot predicate ID | Evaluated via `PredicateManager` ([predicate generator](https://misode.github.io/predicate/)) |

## Requirements

- Minecraft **1.19.2**
- Fabric Loader **≥0.16.0**
- Fabric API **≥0.77.0+1.19.2**


## License

MIT
