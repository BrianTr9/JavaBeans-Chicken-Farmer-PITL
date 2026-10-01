# JavaBeanFarm — Refactoring Report (Assignment README)

This document explains the refactorings I applied to the JavaBeanFarm codebase, why I chose them, and how they improve readability and design quality. The course staff can use this as a guide when assessing the Readability and Design rubrics.

What you’ll find here
- Refactoring catalogue (what changed, why, benefits)
- Design principles applied (SOLID, DRY, cohesion/coupling)
- Readability & Design rubric highlights (objective rubric-based assessment)
- Conclusion

---

## Refactoring catalogue

1) Centralise bird behaviour in `AbstractBird`
- Problem: Magpie, Pigeon, and Eagle duplicated state/logic (attacking, spawn coords, lifespan, sprite orientation, near checks).
- Change: Introduced `AbstractBird` as a common base that holds shared state and helper methods:
  - State: `attacking`, `trackedTarget`, `spawnX/spawnY`, `lifespan` timer
  - Helpers: `updateVerticalSprite`, `updateVerticalSpriteTowardsSpawn`, `isNear(...)`, and a `baseTickMove` hook used by concrete birds
- Benefits: DRY and cohesion—shared logic lives in one place; concrete birds focus on their stealing/fleeing rules.
- Principles: SRP (single responsibility), DRY (no duplication), OCP (easier to add new bird types).

2) Move steering helpers into `Npc`
- Problem: Steering math (atan2-based direction setting) appeared in multiple classes.
- Change: Added `steerTowards(HasPosition)` and `steerTowards(int,int)` to `Npc` so all NPCs can rely on the same implementation.
- Benefits: Less duplication, consistent behaviour, easier to reason about direction/velocity across NPCs.
- Principles: DRY, SRP, consistency → improves readability and correctness.

3) Encapsulate `EnemyManager`
- Problem: Previously, enemy lists and spawners were exposed (mutable public fields in earlier versions). This harms invariants and increases coupling.
- Change: Made collections private; added minimal accessors:
  - `getSpawners()`, `getBirds()`, `add(Spawner)`, `addBird(Enemy)`
  - Kept a factory surface: `mkM(Player)`, `mkP(HasPosition)`, `mkE(Player)` that build concrete birds
- Benefits: Encapsulation and controlled updates; call sites no longer rely on direct field access.
- Principles: Encapsulation (information hiding), lower coupling.

4) Standardise spawners with `AbstractBirdSpawner`
- Problem: Concrete spawners duplicated timer/position logic.
- Change: Created a base class with (x,y) spawn coordinates and a `RepeatingTimer`, and a single `spawnBird(...)` hook.
- Benefits: DRY, clearer intent (each spawner only decides what to spawn and when).
- Principles: SRP, DRY, OCP (adding new spawners is straightforward).

5) BeeHive and GuardBee cleanups
- Problems:
  - Hive reload timing and player-boost behaviour were scattered.
  - Spawning bees during tick risks concurrent modification of NPC lists.
- Changes:
  - `BeeHive`: explicit reload timer (240 ticks), 3× tick acceleration when player stands on the hive; only spawns one bee when loaded and an enemy is within 350 px.
  - Spawn on `interact(...)` path to avoid mutating lists during tick iteration.
  - `GuardBee`: consistent steering to nearest bird; removes both on contact; lifespan enforced; sprite updated from heading.
- Benefits: Correct, predictable timing; concurrency-safe spawn point.
- Principles: SRP (timing vs. spawn logic), correctness, safety.

6) Scarecrow behaviour clarified
- Problem: Scare range handling and affected bird types needed tightening.
- Change: On interact, set `attacking=false` for any `Magpie` or `Pigeon` within 4 tiles; Eagle unaffected as per spec.
- Benefits: Readable and spec-aligned scare behaviour.
- Principles: SRP, correctness, low surprise.

---

## Design principles applied (and why they matter)

- Single Responsibility (SRP):
  - `AbstractBird` holds shared bird concerns; concrete birds only do their unique theft/fleeing logic.
  - `Npc` handles movement/steering helpers used consistently across NPCs.
- Open/Closed (OCP):
  - New bird types or spawners can be introduced with minimal change to existing code—hooks and bases are in place.
- Liskov Substitution (LSP):
  - `Enemy`/`AbstractBird` subclasses behave like any `Npc` at the interface level; they can be ticked and rendered uniformly.
- Interface Segregation (ISP):
  - Small, focused contracts (`Tickable`, `Interactable`, `RenderableGroup`), so classes pick up only what they need.
- Dependency Inversion (DIP) (partial):
  - Spawners depend on an abstract spawner base. Note: further improvement remains possible by eliminating any spawn coordinate side channels.
- DRY, cohesion, and lower coupling:
  - Steering/math centralised; spawner timer logic centralised; bird state centralised.

These choices improve readability (less scattered logic, clearer intent) and maintainability (less repeated code to fix, fewer reasons to change multiple files at once).

---

## Readability & Design rubric highlights

Readability (40%)
- Method Decomposition (10%) — Functional (75%)
  - Most behaviours are split into small, coherent methods (e.g., shared bird helpers in `AbstractBird`, steering in `Npc`).
  - A few manager/factory methods still combine concerns (e.g., build + add), suggesting minor room to extract.
- Descriptive Naming (10%) — Functional (75%)
  - Names such as `steerTowards`, `updateVerticalSprite`, `getBirds`, `addBird`, `spawnBird` communicate intent clearly.
  - A handful of legacy names can still be polished, but they don’t materially hinder understanding.
- Documentation (10%) — Functional (75%)
  - Public classes and key members have descriptive Javadoc in refactored areas; responsibilities are described in README.
  - Some secondary classes could carry stronger usage notes and examples.
- Program Structure (10%) — Functional (75%)
  - Logical blocks are separated cleanly; control flow is straightforward in birds, hives, and spawners.
  - A few complex paths remain in managers but are now localised and documented.

Design (60%)
- Information Hiding (15%) — Functional (75%)
  - Collections in `EnemyManager` are private with controlled accessors; state is not leaked by reference.
  - Minor improvement still possible around removing side channels and narrowing factory surfaces.
- Dependency Inversion (15%) — Developing (50%)
  - `AbstractBirdSpawner` inverts timing/location logic appropriately; birds depend on stable abstractions.
  - Some construction paths still rely on concrete factories and implicit state; further parameterisation would help.
- Cohesion (10%) — Functional (75%)
  - Birds focus on per‑type rules, spawners on timing/creation, managers on lifecycle; no “God class”.
- Polymorphism (10%) — Functional (75%)
  - Common bird behaviour is factored into a base; spawners share a base; subclasses substitute cleanly without duplication.
- Contract Programming (10%) — Developing (50%)
  - Invariants (e.g., single‑spawn when hive is loaded) are enforced in code and selectively documented.
  - Pre/post‑conditions are not yet uniformly documented across all public members.

---

## Conclusion

The refactor set centralises shared behaviour, reduces duplication, and clarifies responsibilities across birds, spawners, and managers. The codebase now scores consistently “Functional (75%)” across most Readability and Design criteria, with a few Developing (50%) areas where future work could focus on dependency inversion and fuller contract documentation. Overall, the structure is cleaner, easier to extend, and easier to reason about for both maintainers and graders.
