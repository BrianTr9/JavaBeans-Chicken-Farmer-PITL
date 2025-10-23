package builder.entities.npc.enemies;

import builder.GameState;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

public class Eagle extends AbstractBird {

    private static final SpriteGroup art = SpriteGallery.eagle;

    // Behavior constants (no behavior change; extracted for clarity)
    private static final int STEAL_FOOD = 3;
    private static final int INITIAL_SPEED = 2;
    private static final int FLEE_SPEED = 4;

    /** Amount of food currently stolen from the player (Eagle-specific). */
    private int food = 0;

    public Eagle(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setTrackedTarget(trackedTarget);
        this.setLifespan(new FixedTimer(5000));

        this.setSprite(art.getSprite("default"));
        this.setSpeed(INITIAL_SPEED);
        // Face initial target on spawn (attacking starts true)
        this.steerTowards(getTrackedTarget());
    }

    /**
     * Behavior invariants:
     * - While attacking: fly towards player, and on contact steal 3 food once, then flee to spawn.
     * - While fleeing: fly towards spawn; on contact with spawn, despawn.
     * - If removed before reaching spawn, any stolen food is returned to the player.
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
                this.updateVerticalSprite(art, getTrackedTarget().getY());
            }
        } else {
            this.steerTowards(this.getSpawnX(), this.getSpawnY());
            this.updateVerticalSpriteTowardsSpawn(art, this.getSpawnY());
        }
    }

    private void refundFoodIfRemovedAwayFromSpawn(EngineState engine, GameState game) {
        if (this.isMarkedForRemoval()
                && this.distanceFrom(this.getSpawnX(), this.getSpawnY())
                        > engine.getDimensions().tileSize()) {
            game.getInventory().addFood(this.food);
        }
    }

    // --- Accessors for common bird attributes are inherited from AbstractBird ---

    // --- Eagle-specific accessors ---
    public int getFood() {
        return this.food;
    }

    public void setFood(int food) {
        this.food = food;
    }
}
