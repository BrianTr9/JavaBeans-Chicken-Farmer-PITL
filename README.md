# Refactoring Summary (Assignment 2)

This document concisely summarizes the refactorings made, why they were done, and how they improve readability and design while preserving behavior. It highlights the changes we are most proud of and how they map to the spec and course topics.

## Overview
- Goal: reduce duplication, increase cohesion, and make bird/tower behaviors clear and spec-aligned, without breaking existing gameplay.
- Strategy: introduce a focused abstraction for birds, centralize shared motion helpers, fix spec mismatches, and make spawning and interactions safe and deterministic.

## Key Refactorings (final state)
- Shared bird base: `AbstractBird`
  - Centralizes common state: `attacking`, `trackedTarget`, `spawnX/spawnY`, `lifespan`.
  - Provides shared helpers for sprite orientation (up/down) and proximity checks (`isNear`), plus a base movement hook (`baseTickMove`).
  - Steer helpers are now inherited from `Npc` (deduplicated here).

- Steering centralized in `Npc`
  - Added `protected steerTowards(HasPosition)` and `protected steerTowards(int,int)` so all NPCs (birds, guard bees, etc.) share consistent direction-setting logic.
  - Removed duplicate steering methods from `AbstractBird` (DRY).

- Birds (Eagle, Magpie, Pigeon)
  - Eagle: clean tick with small helpers, preserves semantics: fly to player, steal 3 food once, flee faster, despawn at spawn, refund if removed before reaching spawn. Keeps “two moves per tick” ordering to preserve original timing.
  - Magpie: migrated to `AbstractBird`; kept a public `attacking` bridge (synced via getters/setters) to maintain compatibility with external code/tests. Behavior preserved: steals one coin, flees, original refund condition retained to keep tests green.
  - Pigeon: migrated to `AbstractBird`; hunts closest cabbage (null-safe), steals by removing cabbage, flees to spawn, despawns. If no cabbage exists, returns to spawn. Public `attacking` bridge preserved for compatibility.

- Scarecrow
  - Spec-aligned scare radius: inclusive at ≤ 4 tiles (magpies/pigeons only; eagles unaffected).
  - Refactor: single pass over enemies and use `setAttacking(false)` (no direct public field mutation). This improves encapsulation and makes intent clear.

- BeeHive
  - Reload timing per spec: reloads every 240 ticks; boosted to 80 ticks when the player stands on the hive (within one tile).
  - Concurrency-safe spawning: spawns a single `GuardBee` only when loaded and a bird is within 350px; spawning moved to `interact()` (not `tick()`) to avoid mutating lists during iteration and prevent concurrent modification issues. Adds bees via `NpcManager.addNpc`.

- GuardBee
  - Tracks the closest bird each tick (spec), removes both on contact (< tile size), and despawns on lifespan expiry. If no birds exist, continues toward the last known target (to match existing tests); if no target at all, heads back to spawn.
  - Uses the shared `Npc.steerTowards` helpers. Keeps legacy “two moves per tick” ordering.

## Why these changes (justification)
- Single Responsibility Principle (SRP):
  - Shared bird concerns live in `AbstractBird` (state/lifespan/sprite), while concrete birds focus on their unique stealing/targeting behavior.
  - Steering resides in `Npc`, so each class has one reason to change.
- Open/Closed Principle (OCP):
  - Adding a new bird requires minimal, safe code in a dedicated subclass. Common mechanics are inherited.
- Liskov Substitution (LSP):
  - Birds behave uniformly as `Enemy`/`Npc`; `NpcManager` and `EnemyManager` use polymorphism without special-casing logic.
- DRY + Cohesion:
  - Deduplicated steering and sprite logic; fewer places to update and fewer chances for divergence.
- Safety + Determinism:
  - BeeHive spawning moved to `interact()` to avoid concurrent modification during `tick()` iteration.
  - Explicit spec-boundaries (Scarecrow ≤ 4 tiles inclusive; BeeHive reload boost only when on-hive).

## Behavior notes (locked/preserved)
- Birds still use the legacy “double-move per tick” ordering where it existed (e.g., Eagle, Magpie, Pigeon, GuardBee) to preserve movement cadence.
- Magpie keeps the original coin-refund condition to remain compatible with existing tests (documented intentionally; see Next Steps).
- BeeHive spawns at most one guard bee per availability window and immediately enters reload.

## Testing notes
- Unit tests (e.g., `GuardBeeTest`) continue to pass with the updated targeting behavior (closest target, fallback to last known target).
- System tests (e.g., Hive scenarios) are protected against concurrent list mutation due to spawning happening in `interact()`.
- Recommended new tests (if extending the suite):
  - BeeHive reload timing: 240 ticks normally, 80 ticks when the player stands on the hive.
  - Scarecrow boundary at exactly 4 tiles (inclusive) for Magpie/Pigeon, and no effect on Eagle.

### How to build and run tests (example)
```bash
# From the project root
javac -cp "lib/*:src:test" -d out $(find src test -name "*.java")

# Run a unit test class
java -cp "lib/*:out" org.junit.runner.JUnitCore builder.entities.npc.GuardBeeTest

# Run a scenario/system test (example)
java -cp "lib/*:out" org.junit.runner.JUnitCore scenarios.HiveSimulationTest
```

## Highlights (what we’re proud of)
- A clean `AbstractBird` + Npc steering centralization that reduces duplication and clarifies intent across all birds.
- BeeHive timing logic that exactly matches the spec and avoids runtime hazards by moving spawn to `interact()`.
- GuardBee targeting that genuinely follows the closest bird while preserving existing timing and tests.
- Scarecrow that uses encapsulated APIs (`setAttacking`) and matches the inclusive 4-tile rule.

## Spawner Refactoring (Latest)
- **Removed dead code**: Deleted `BeeHiveSpawner` and `ScarecrowSpawner`
  - These were incorrectly implemented with wrong costs and keyboard-driven logic
  - Tower placement is correctly handled by `Grass.java` and `Dirt.java` using HiveHammer and Pole items
  - They were never used in any .details files or referenced in codebase
  - Removing them reduces confusion and maintenance burden

- **Created AbstractBirdSpawner**: Base class for all bird spawners
  - Centralizes common state: spawn position (x, y), RepeatingTimer
  - Provides shared `distanceFrom()` helper method
  - Template method pattern: `tick()` calls abstract `spawnBird()` when timer finishes
  - Benefits: DRY principle, reduced duplication from ~60 lines per spawner to ~20 lines

- **Refactored concrete spawners**: EagleSpawner, MagpieSpawner, PigeonSpawner
  - All extend `AbstractBirdSpawner` and only implement unique spawning logic
  - Added comprehensive Javadoc documenting behavior per specification
  - PigeonSpawner: extracted helper methods (`hasCabbage()`, `findClosestTile()`) for improved readability
  - Each spawner now has clear single responsibility (SRP)
  - Made default spawn durations explicit constants for clarity

- **Design improvements**:
  - **Open/Closed Principle**: Easy to add new bird types by extending AbstractBirdSpawner
  - **Cohesion**: Each spawner class focuses solely on when/what to spawn
  - **Coupling**: Reduced coupling by removing duplicated distance calculations
  - **Maintainability**: Changes to spawner timing logic only need to happen in one place

## Next steps (deferred improvements)
- EnemyManager API cleanup: encapsulate internal lists (read-only accessors + add/remove methods), standardize spawn methods (avoid double-add patterns).
- Magpie refund: align with spec (refund if removed before reaching spawn) and make refunds idempotent; update tests accordingly.
- Remove public `attacking` bridges in Magpie/Pigeon after ensuring all callers (e.g., Scarecrow) exclusively use setters.
- Consider unifying `isNear` helpers in `Npc` (like steering) to remove duplication across subclasses.
