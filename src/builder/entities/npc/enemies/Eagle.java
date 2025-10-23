package builder.entities.npc.enemies;

import builder.GameState;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

/**
 * An eagle enemy that flies towards the player and steals food.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Flies towards the player and on contact steals a fixed amount of food</li>
 *   <li>After stealing, returns to spawn and removes itself</li>
 *   <li>If removed before reaching spawn, any stolen food is refunded to the player</li>
 * </ul>
 */
public class Eagle extends AbstractBird {

    /** Eagle sprite group. */
    private static final SpriteGroup ART = SpriteGallery.eagle;

    /** Food stolen when the eagle successfully robs the player. */
    private static final int STEAL_FOOD = 3;

    /** Initial flying speed while attacking. */
    private static final int INITIAL_SPEED = 2;

    /** Speed while fleeing/returning to spawn. */
    private static final int FLEE_SPEED = 4;

    /** Amount of food currently stolen from the player (Eagle-specific). */
    private int food = 0;

    /**
     * Construct an Eagle targeting a player-like HasPosition.
     *
     * @param x horizontal spawn coordinate
     * @param y vertical spawn coordinate
     * @param trackedTarget the target to track (player)
     */
    public Eagle(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setTrackedTarget(trackedTarget);
        this.setLifespan(new FixedTimer(5000));

        this.setSprite(ART.getSprite("default"));
        this.setSpeed(INITIAL_SPEED);
        // Face initial target on spawn (attacking starts true)
        this.steerTowards(getTrackedTarget());
    }

    /**
     * Per-tick behaviour for the eagle.
     *
     * @param engine the engine state
     * @param game the game state
     */
    @Override
    public void tick(EngineState engine, GameState game) {
        // Preserve original ordering: one move via base, later a second move below
        this.baseTickMove(engine, game);

        tickLifespanAndMaybeDespawn();
        tryStealFromPlayerIfAttacking(engine, game);
        tryDespawnAtSpawnIfReturning(engine);

        // Second move (as per original code)
        this.move();

        updateHeadingAndSprite();
        refundFoodIfRemovedAwayFromSpawn(engine, game);
    }

    private void tickLifespanAndMaybeDespawn() {
        if (this.getLifespan() != null) {
            this.getLifespan().tick();
            if (this.getLifespan().isFinished()) {
                this.markForRemoval();
            }
        }
    }

    private void tryStealFromPlayerIfAttacking(EngineState engine, GameState game) {
        final int px = game.getPlayer().getX();
        final int py = game.getPlayer().getY();
        if (this.getAttacking() && this.isNear(engine, px, py)) {
            this.setAttacking(false);
            if (this.food == 0) { // steal at most once
                game.getInventory().addFood(-STEAL_FOOD);
                this.food = STEAL_FOOD;
            }
            this.setSpeed(FLEE_SPEED);
        }
    }

    private void tryDespawnAtSpawnIfReturning(EngineState engine) {
        if (!this.getAttacking() && this.isNear(engine, this.getSpawnX(), this.getSpawnY())) {
            this.markForRemoval();
        }
    }

    private void updateHeadingAndSprite() {
        if (getAttacking()) {
            this.steerTowards(getTrackedTarget());
            if (getTrackedTarget() != null) {
                this.updateVerticalSprite(ART, getTrackedTarget().getY());
            }
        } else {
            this.steerTowards(this.getSpawnX(), this.getSpawnY());
            this.updateVerticalSpriteTowardsSpawn(ART, this.getSpawnY());
        }
    }

    private void refundFoodIfRemovedAwayFromSpawn(EngineState engine, GameState game) {
        if (this.isMarkedForRemoval()
                && this.distanceFrom(this.getSpawnX(), this.getSpawnY())
                        > engine.getDimensions().tileSize()) {
            game.getInventory().addFood(this.food);
        }
    }

    // --- Eagle-specific accessors -----------------------------------------------

    /** Get the amount of food currently held by this eagle (stolen). */
    public int getFood() {
        return this.food;
    }

    /** Set the amount of food currently held by this eagle (stolen). */
    public void setFood(int food) {
        this.food = food;
    }
}
