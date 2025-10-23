# JavaBeanFarm — Refactoring Summary

This README documents the recent refactorings applied to the JavaBeanFarm codebase and the current behavioural/implementation status for bird enemies, defensive towers, and related systems. It combines the prior refactoring brief, the repository state, and a small code audit of the current sources to give a single-source, accurate snapshot for maintainers.

High-level plan
- Centralize shared bird behaviour in `AbstractBird`.
- Centralize steering/movement helpers in `Npc` and remove duplication.
- Keep existing tests green while clarifying known follow-ups where behaviour differs from the spec.
- Make BeeHive spawning concurrency-safe and keep GuardBee targeting test-compatible.

Contents
- Birds (shared + per-type)
- Scarecrow
- BeeHive & GuardBee
- Spawners
- Npc/shared improvements
- Known follow-ups
- Build & tests
- Git history snapshot

---

## Birds (shared + per-type)

### AbstractBird (new base)
- Consolidates shared bird state and helpers used by all birds:
  - attacking flag, trackedTarget, spawnX/spawnY, lifespan management
  - helpers for sprite orientation, near checks and movement hooks
- No steering duplication: steering helpers were consolidated into `Npc` and reused by `AbstractBird`.
- Goal: reduce duplication, make bird implementations concise and focused on stealing/attack semantics.

### Eagle
- Behaviour implemented to match the specification:
  - Flies toward the player; on contact steals 3 food once; switches to flee mode and increases speed.
  - Returns to its spawn and despawns on arrival.
  - If removed before reaching its spawn, stolen food is refunded to the player.
- Implementation preserves legacy ordering (`baseTickMove` then `move`) to remain compatible with existing tests.

### Magpie
- Migrated to `AbstractBird` and uses `Npc` steering helpers.
- Behaviour: flies to the player, steals 1 coin (if available), flees to spawn and despawns on arrival.
- Refund semantics: refund logic is intentionally preserved as implemented in the current codebase to avoid breaking existing tests. The current implementation does not refund a coin in the common interrupted-on-the-way-home case; this is a known divergence from the spec and is listed as a follow-up item below.

### Pigeon
- Migrated to `AbstractBird` and keeps the nearest-cabbage selection logic.
- Behaviour: targets the closest cabbage, removes the cabbage when it reaches it, then returns to spawn and despawns.
- If no cabbage exists at spawn time the pigeon returns immediately to spawn.
- Refund semantics: pigeons currently mark the cabbage for removal immediately upon stealing and do not implement an automatic refund if the pigeon is removed before returning to spawn. This is a known difference from the original specification and is listed as a follow-up.

### Summary status (Birds)
- Shared behaviour: centralized and encapsulated in `AbstractBird` (Done).
- Eagle: spec-aligned including refund (Done).
- Magpie: stealing & returning behaviour done; refund semantics preserved from prior refactor (kept for test compatibility) and flagged for follow-up.
- Pigeon: steal & return behaviour done; refund missing (flagged).

---

## Scarecrow
- Behaviour: scares `Magpie` and `Pigeon` instances within a radius of 4 tiles (inclusive). Affected birds have `setAttacking(false)` applied so they stop attacking and return to spawn.
- `Eagle` is not affected by Scarecrow.
- Implementation details:
  - Uses setters (no public field writes) and iterates the enemy list in-place (no unnecessary intermediate collections).

---

## BeeHive and GuardBee

### BeeHive
- Constants and costs follow the spec: reload interval `240` ticks, coin cost `2`, food cost `2`, detection distance `350` px.
- Reload acceleration: when the player stands on the hive (detected as within one tile), the reload timer advances 3x per tick (effectively 80 ticks if the player stands on it the whole time).
- Spawn safety: spawning is done from `interact()` rather than directly mutating during `tick()` to avoid concurrent modifications on the NPC manager during tick iteration. When loaded and a target bird is in range, `interact()` spawns one `GuardBee`, adds it via `NpcManager.addNpc(...)`, and sets `loaded = false` to begin reload.

### GuardBee
- Targeting: each tick the guard bee steers to the closest bird in the world.
- When no birds are present, it continues toward the last known target (this was intentionally preserved to remain compatible with existing unit tests). If there is no known target, the bee steers back to its spawn.
- On collision with a bird (distance < tileSize) the bee and the bird are removed; the bee also expires after its lifespan.
- Movement and rendering preserve the legacy "double-move per tick" ordering to maintain compatibility with prior behaviour and tests.

---

## Spawners
- Spawner behaviour is now standardized by `AbstractBirdSpawner`:
  - Holds `x`, `y` spawn coordinates and a `RepeatingTimer` for spawn intervals.
  - Concrete spawners implement `spawnBird(EngineState, GameState)` which is invoked when the timer finishes.
- Pigeon spawners only create pigeons when there is at least one cabbage tile in the world (spec requirement).
- Timers and spawn intervals are preserved and configurable from details files.

---

## Npc (shared improvements)
- Steering helpers and common movement behaviour moved to `Npc`:
  - `steerTowards(HasPosition)` and `steerTowards(x,y)` are protected helpers used across birds and `GuardBee`.
- `AbstractBird` uses these helpers and therefore no longer duplicates steering code.
- Concurrency safety: BeeHive spawn moved to `interact()` to avoid modifying NPC collections during iteration.

---

## Cross-cutting improvements and design wins
- Reduced duplication: major steering and movement logic consolidated into `Npc` and `AbstractBird`.
- Encapsulation: public fields on enemies were converted to private with accessors; Scarecrow and other callers now use setters.
- SOLID alignment:
  - SRP: `Npc` handles motion; `AbstractBird` holds shared bird state; concrete birds implement type-specific behaviour.
  - OCP & LSP: adding new bird types is easier and safer.
- Tests: refactors were done with a comprehensive test-suite; behaviour-preserving choices were made when tests depended on a specific interpretation of the spec.

---

## Known follow-ups (recommended)
- Standardize refund semantics for stolen items:
  - Magpie: current refund logic is preserved for test compatibility but differs from a strict reading of the spec. Consider changing to refund when a magpie is removed before it reaches its spawn.
  - Pigeon: implement refund/restore of stolen cabbage if the pigeon is removed before returning to its spawn (requires a strategy: delay removal or re-add the cabbage to the tile/world).
- `EnemyManager` / spawner API hardening:
  - Provide encapsulated `spawnX`/`spawnY` and `mkX(...)` / add/remove wrappers rather than exposing mutable public lists.
- Consider moving `isNear(...)` into `Npc` too so all distance helpers live in one place.

---

## Build & tests
- After the refactors described, the project compiles and the scenario tests in `test/scenarios/` were preserved and kept green per repository notes.
- Unit tests were added/updated for bird enemies and towers to exercise the extraction points and edge cases (lifespan expiry, steal-and-refund semantics, scare radius, guard-bee targeting).

### How to run (project-specific commands depend on your environment)
- Use the repository's standard build and test commands (JUnit + the provided engine dependencies in `lib/`).

---

## Git snapshot (recent)
- d3bc550 Refactor spawners files; Fix setters getters from enemies and test folder
- cec259b Write README
- b4bd074 Refactor NPC, BeeHive, GuardBee, Scarecrow; Adjust AbstractBird
- f4bc041 Refactor enemies files
- 03eb16a Add getters and setters; Adjust tests
- a162516 Create initial JUnit Tests for Eagle, Pigeon, Magpie, GuardBee and Scarecrow
- 0eb5278 Fix the rate increase in BeeHive's interact()
- 8a04b26 Fix the scare zone of Scarecrow to Pigeon
- ff96f28 Fix Pigeon initial default sprite

---

## Contact / next steps
- If you want, I can implement the Magpie/Pigeon refund changes now and run the unit tests. I recommend doing that as a small follow-up branch and adjusting tests that intentionally relied on the prior behaviour.

This file is the single-source summary of the recent refactors; keep it up-to-date as behaviour is tightened to match the specification exactly.
