# 🐔 JavaBeans - Chicken Farmer Game

[![CI](https://github.com/BrianTr9/JavaBeans-Chicken-Farmer-PITL/actions/workflows/ci.yml/badge.svg)](https://github.com/BrianTr9/JavaBeans-Chicken-Farmer-PITL/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Educational-blue?style=flat-square)](#-license)

A 2D farming game in Java, inspired by Stardew Valley. You play a chicken farmer who mines ore,
grows cabbages and defends the farm from thieving birds with bee hives and scarecrows.

<p align="center">
  <img src="docs/screenshot.png" alt="Gameplay: a row of bee hives releasing guard bees while birds
  approach the cabbage fields" width="480">
</p>

The project combines both CSSE2002 (Programming in the Large) assignments from the University
of Queensland, Semester 2 2025, into a single codebase:

- **Assignment 1**: the core game (player, world, tiles, tools, inventory), built from a
  method-level specification.
- **Assignment 2**: an extension that adds birds, spawners, towers and data-driven levels, plus
  bug fixing, refactoring and JUnit tests.
- **Consolidation**: merging both histories, fixing the remaining bugs, filling the test gaps
  and adding a Gradle build, Checkstyle and CI.

## 🎮 Quick start

Requires a JDK (17 to 24) to start Gradle. The wrapper downloads Gradle, and the build uses
Java 21, which is downloaded automatically if it is not installed.

```bash
./gradlew run      # play the game
./gradlew test     # run all 414 JUnit tests
./gradlew check    # tests + Checkstyle (CSSE2002 style guide)
```

On Windows use `gradlew.bat`. In IntelliJ IDEA, open the folder and import it as a Gradle
project.

## 🕹️ How to play

| Input | Action |
|-------|--------|
| **W A S D** | Walk (water blocks you) |
| **1 – 5** | Select an inventory slot |
| **Left click** | Use the held tool on the tile you are standing on |

Walking over a fully grown cabbage harvests it for **+2 food and +3 coins**. You start with
20 coins and 30 food (set in `resources/uqLogo.details`).

### Tools

| Slot | Tool | Use on | Effect |
|:---:|------|--------|--------|
| 1 | Bucket | Tilled dirt | Plant a cabbage (2 coins). It ripens in 4 stages of 100 ticks |
| 2 | Hoe | Grass / dirt | Turn grass into dirt; till dirt |
| 3 | Jackhammer | Ore | Mine 2 coins every 5 ticks (10 coins per rock) |
| 4 | Hive hammer | Empty grass | Build a bee hive (2 coins + 2 food) |
| 5 | Pole | Empty tilled dirt | Build a scarecrow (2 coins) |

### Birds and defences

| Bird | Goes after | Steals | Stopped by |
|------|-----------|--------|-----------|
| 🐦 Magpie | The player | 1 coin | Bees, scarecrows |
| 🦅 Eagle | The player | 3 food | Bees |
| 🕊️ Pigeon | Nearest cabbage | The cabbage | Bees, scarecrows |

Birds fly home after a theft. If a bird is removed before it gets home (for example, caught by a
bee), it **returns what it stole**. Pigeons only spawn while there is a cabbage to steal.

- **Bee hive**: when a bird comes within 350 px, the hive releases a guard bee. The bee chases
  the nearest bird and removes it on contact, or flies home when there are no birds left. The
  hive reloads in 240 ticks, or 3× faster while you stand on it.
- **Scarecrow**: magpies and pigeons within 4 tiles give up their attack and fly home.

## 🏗️ Architecture

`JavaBeanFarm` is the game controller. The engine (`lib/engine.jar`, provided by course staff)
calls `tick()` and `render()` every frame:

```
tick:   player → NPCs (hives, bees, scarecrows) → enemies (spawners, birds) → world tiles
        → overlays → NPC interactions → cleanup of removed entities
render: world tiles (+ stacked crops/rocks/towers) → NPCs → birds → player → overlays
```

| Package | Responsibility |
|---------|----------------|
| `builder` | `JavaBeanFarm` (controller), `GameState` (what every tick can see) |
| `builder.player` | `ChickenFarmer` and `PlayerManager` (movement, collisions, tool use) |
| `builder.world` | `BeanWorld` (tile storage), `WorldBuilder` (`.map` loading), `OverlayBuilder` (`.details` loading) |
| `builder.entities.tiles` | `Tile` base class with stacked entities, plus `Grass`, `Dirt`, `Water`, `OreVein`, `TileFactory` |
| `builder.entities.resources` | `Cabbage`, `Ore` |
| `builder.entities.npc` | `Npc` (heading + speed movement), `NpcManager`, `BeeHive`, `GuardBee`, `Scarecrow` |
| `builder.entities.npc.enemies` | `Enemy` → `AbstractBird` → `Magpie` / `Eagle` / `Pigeon`, `EnemyManager` |
| `builder.entities.npc.spawners` | `Spawner` → `AbstractBirdSpawner` → one spawner per bird |
| `builder.inventory` | `TinyInventory`, the five tools and the inventory/resource overlays |

Design techniques used in the code:

- **Factory**: `TileFactory` maps map symbols to tiles.
- **Template method**: `AbstractBirdSpawner.tick` owns the timer and calls each spawner's
  `spawnBird`.
- **Composite rendering**: tiles render themselves plus whatever is stacked on them;
  `RenderableGroup` lets managers render collections.
- **Polymorphism**: each tile decides what a tool does to it (`use`) and what happens when the
  player walks over it (`interact`). Birds declare their own pace (`ticksPerFrame`) and their
  clean-up when removed (`onRemoved`).
- **Encapsulation**: managers expose read-only views of their collections.

## 🗺️ Level files

A level is a `.map` file plus a `.details` file, both in `resources/`.

**`.map`**: one character per tile, one line per row (25 × 25 for the default window):

| `g` grass | `d` dirt | `t` tilled dirt | `w` water | `o` ore |
|---|---|---|---|---|

**`.details`**: sections of `key:value` entries (keys in any order, `|` prefix optional):

```
:chickenFarmer:
|x:380 y:380 coins:20 food:30
end;

:cabbages:
|x:340 y:340
end;

:magpiespawner:
|x:5 y:5 duration:800
end;
```

The `eaglespawner` and `pigeonspawner` sections use the same format as `magpiespawner`. Every
section must be present (it may be empty). A malformed entry is reported with the section and
line at fault.

## 🧪 Testing

| Suite | Location | Classes | Tests |
|-------|----------|:---:|:---:|
| Unit tests | `test/builder` | 21 | 334 |
| System tests (course staff) | `test/scenarios` | 11 | 80 |

The system tests replay scripted input through the real engine with mocked input and analyse
every rendered frame. The unit tests cover all game classes, including `TinyInventory` and the
Assignment 1 tiles, tools, player and resources. CI runs `./gradlew check` (tests + Checkstyle)
on every push and pull request.

## 📜 Project history

| Phase | What happened |
|-------|---------------|
| **Assignment 1** (Stages 1–3) | Player movement, tile world and map loading, collisions, mining, tilling, planting, harvesting, inventory and overlays |
| **Assignment 2** | Provided with a buggy, poorly structured extension. Fixed the bugs found by the system tests, refactored birds, spawners, towers and level loading (see [`docs/assignment2-refactoring-report.md`](docs/assignment2-refactoring-report.md)), and wrote unit tests |
| **Consolidation** | Merged both git histories and fixed the issues below. Added tests for the Assignment 1 classes (the missing Stage 4), a Gradle build, Checkstyle and CI, and removed the assignment scaffolding |

Bugs fixed during consolidation:

- Spawners added every magpie and pigeon to the enemy list twice, so they were silently ticked
  twice per frame. Each bird is now stored once, and the pace the system tests rely on is
  explicit (`Enemy.ticksPerFrame`).
- Birds caught by a bee never returned stolen goods, because they were cleaned up before their
  refund code ran. Refunds now happen in `Enemy.onRemoved`, exactly once.
- An eagle could "return" food the player never had.
- Guard bees showed the wrong sprite when flying up or right, and kept chasing a dead target
  instead of flying home.
- Malformed `.details` files crashed with index or number errors. They now produce clear
  load errors.

The assignment specifications are in [`docs/`](docs/).

## 🤖 AI usage

- **Assignment 1** was written without generative AI tools.
- **Assignment 2** used ChatGPT and Claude for Javadoc comments and an initial `PigeonTest`
  baseline, as declared in [`docs/ai-declaration.txt`](docs/ai-declaration.txt).
- **Consolidation** (history merge, bug fixes, new tests, build, CI and this README) was done
  with the help of Claude Code.

## 🤝 Academic integrity

This is an educational project for CSSE2002 at the University of Queensland. You may learn from
it and reference it. Do not submit it as your own work. See the
[UQ Academic Integrity policy](https://uq.mu/rl553).

## 📄 License

**Educational License**: University of Queensland CSSE2002 (Semester 2, 2025).

- ✅ Portfolio display, GitHub showcase, code review and technical interviews
- ✅ Learning from the architecture and design
- ❌ Copying code to submit for assignments or courses
- ❌ Commercial use without permission

The game engine (`lib/engine.jar`), the system tests in `test/scenarios`, the assignment
specifications and the Checkstyle configuration are provided by the CSSE2002 course staff.

## 👤 Author

**Trung Bao Truong**, University of Queensland, GitHub [@BrianTr9](https://github.com/BrianTr9)
