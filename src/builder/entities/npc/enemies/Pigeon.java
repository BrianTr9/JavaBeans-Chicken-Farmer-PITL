package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Tile;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.Entity;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

import java.util.List;

public class Pigeon extends AbstractBird {

    private static final SpriteGroup art = SpriteGallery.pigeon;

    // Backward-compat: Scarecrow sets this field directly; keep it and bridge to base attacking
    public Boolean attacking = true;

    public Pigeon(int x, int y) {
        super(x, y);
        this.setLifespan(new FixedTimer(3000));
        // default speed is 1 from Npc; sprite can be set later
    }

    public Pigeon(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setSprite(art.getSprite("default"));
        this.setTrackedTarget(trackedTarget);
        this.setSpeed(1);
        this.setLifespan(new FixedTimer(3000));
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        // Sync external changes on the public field into the base state before ticking
        if (this.attacking != null && this.attacking.booleanValue() != super.getAttacking()) {
            super.setAttacking(this.attacking);
        }

        // original behavior: call super.tick(engine, game) then call move() later as well
        this.baseTickMove(engine, game);

        if (!this.getAttacking()) {
            handleFleeing(engine);
        }

        this.move();

        if (this.getTrackedTarget() == null && this.getAttacking()) {
            steerToCenter(engine);
        }

        if (this.getTrackedTarget() != null && this.getAttacking()) {
            this.steerTowards(this.getTrackedTarget());
        }

        if (this.getLifespan() != null) {
            this.getLifespan().tick();
            if (this.getLifespan().isFinished()) {
                this.markForRemoval();
            }
        }

        if (!this.getAttacking()) {
            handleFleeing(engine); // duplicate original behavior
        }

        final Tile closest = findClosestCabbage(game);
        if (closest != null) {
            this.setTrackedTarget(closest);
            tryStealFromClosest(engine, closest);
        } else {
            // no cabbages to get
            this.setAttacking(false);
        }

        // Sync base attacking back to public field so external code sees updated state immediately
        this.attacking = this.getAttacking();
    }

    private void handleFleeing(EngineState engine) {
        this.steerTowards(this.getSpawnX(), this.getSpawnY());
        if (this.distanceFrom(this.getSpawnX(), this.getSpawnY()) < engine.getDimensions().tileSize()) {
            this.markForRemoval();
        }
        this.updateVerticalSpriteTowardsSpawn(art, this.getSpawnY());
    }

    private void steerToCenter(EngineState engine) {
        final int cx = engine.getDimensions().windowSize() / 2;
        final int cy = engine.getDimensions().windowSize() / 2;
        this.steerTowards(cx, cy);
        // set sprite relative to center (no null deref)
        if (this.getY() < cy) {
            this.setSprite(art.getSprite("down"));
        } else {
            this.setSprite(art.getSprite("up"));
        }
    }

    private Tile findClosestCabbage(GameState game) {
        List<Tile> tiles =
                game.getWorld()
                        .tileSelector(
                                tile -> {
                                    for (Entity entity : tile.getStackedEntities()) {
                                        if (entity instanceof Cabbage) {
                                            return true;
                                        }
                                    }
                                    return false;
                                });
        if (tiles.isEmpty()) return null;
        int distance = this.distanceFrom(tiles.getFirst());
        Tile closest = tiles.getFirst();
        for (Tile tile : tiles) {
            final int d = this.distanceFrom(tile);
            if (d < distance) {
                closest = tile;
                distance = d;
            }
        }
        return closest;
    }

    private void tryStealFromClosest(EngineState engine, Tile closest) {
        if (this.getAttacking()
                && this.distanceFrom(this.getTrackedTarget()) < engine.getDimensions().tileSize()) {
            for (Entity entity : closest.getStackedEntities()) {
                if (entity instanceof Cabbage cabbage) {
                    cabbage.markForRemoval();
                    this.setAttacking(false); // start fleeing
                    break;
                }
            }
        }
    }

    @Override
    public boolean getAttacking() {
        return (this.attacking != null) ? this.attacking.booleanValue() : super.getAttacking();
    }

    @Override
    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        super.setAttacking(attacking);
    }
}
