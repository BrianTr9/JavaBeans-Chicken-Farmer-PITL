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

/**
 * A pigeon enemy that flies towards cabbages and steals them.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Flies towards the closest cabbage in the world</li>
 *   <li>If no cabbage exists, returns to spawn</li>
 *   <li>When reaching a cabbage, removes it from the world</li>
 *   <li>After stealing, returns to spawn and removes itself</li>
 * </ul>
 */
public class Pigeon extends AbstractBird {

    /** Pigeon sprite group. */
    private static final SpriteGroup ART = SpriteGallery.pigeon;

    /**
     * Construct a pigeon at the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Pigeon(int x, int y) {
        super(x, y);
        this.setLifespan(new FixedTimer(3000));
        this.setAttacking(true);
        // default speed is 1 from Npc; sprite can be set later
    }

    /**
     * Construct a pigeon that immediately tracks a target.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param trackedTarget the position to track (closest cabbage tile)
     */
    public Pigeon(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
        this.setTrackedTarget(trackedTarget);
        this.setSpeed(1);
        this.setLifespan(new FixedTimer(3000));
        this.setAttacking(true);
    }

    /**
     * Pigeons take two simulation steps per frame. The game's system tests are calibrated
     * to this pace.
     *
     * @return 2
     */
    @Override
    public int ticksPerFrame() {
        return 2;
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        // Birds move twice per tick: once before and once after re-steering.
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

        final Tile closest = findClosestCabbage(game);
        if (closest != null) {
            this.setTrackedTarget(closest);
            tryStealFromClosest(engine, closest);
        } else {
            // no cabbages to get
            this.setAttacking(false);
        }
    }

    private void handleFleeing(EngineState engine) {
        this.steerTowards(this.getSpawnX(), this.getSpawnY());
        if (this.distanceFrom(this.getSpawnX(), this.getSpawnY())
                < engine.getDimensions().tileSize()) {
            this.markForRemoval();
        }
        this.updateVerticalSpriteTowardsSpawn(ART, this.getSpawnY());
    }

    private void steerToCenter(EngineState engine) {
        final int cx = engine.getDimensions().windowSize() / 2;
        final int cy = engine.getDimensions().windowSize() / 2;
        this.steerTowards(cx, cy);
        // set sprite relative to center (no null deref)
        if (this.getY() < cy) {
            this.setSprite(ART.getSprite("down"));
        } else {
            this.setSprite(ART.getSprite("up"));
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
        if (tiles.isEmpty()) {
            return null;
        }

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
}
