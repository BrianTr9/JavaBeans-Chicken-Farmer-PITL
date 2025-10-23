package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.npc.Expirable;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

/**
 * Abstract helper class for bird-like enemies.
 * Contains shared steering and sprite-orientation helpers used by concrete birds.
 * Also centralizes common bird state (attacking, trackedTarget, spawn, lifespan).
 */
abstract class AbstractBird extends Enemy implements Expirable {

    // Common bird state
    private boolean attacking = true;
    private HasPosition trackedTarget;
    private final int spawnX;
    private final int spawnY;
    private FixedTimer lifespan;

    protected AbstractBird(int x, int y) {
        super(x, y);
        this.spawnX = x;
        this.spawnY = y;
        this.lifespan = null; // subclasses should set a lifespan duration explicitly
    }

    // Expirable
    @Override
    public FixedTimer getLifespan() {
        return this.lifespan;
    }

    @Override
    public void setLifespan(FixedTimer lifespan) {
        this.lifespan = lifespan;
    }

    // Common bird attributes accessors
    public boolean getAttacking() {
        return this.attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public HasPosition getTrackedTarget() {
        return this.trackedTarget;
    }

    public void setTrackedTarget(HasPosition trackedTarget) {
        this.trackedTarget = trackedTarget;
    }

    public int getSpawnX() {
        return this.spawnX;
    }

    public int getSpawnY() {
        return this.spawnY;
    }

    /**
     * Update sprite based on whether the target y is below or above current y, using provided art.
     * Chooses "down" when target is below current y, otherwise "up".
     */
    protected void updateVerticalSprite(SpriteGroup art, int targetY) {
        if (art == null) return;
        if (targetY > this.getY()) {
            this.setSprite(art.getSprite("down"));
        } else {
            this.setSprite(art.getSprite("up"));
        }
    }

    /** Update sprite based on moving towards/away from spawn. */
    protected void updateVerticalSpriteTowardsSpawn(SpriteGroup art, int spawnY) {
        if (art == null) return;
        if (spawnY < this.getY()) {
            this.setSprite(art.getSprite("up"));
        } else {
            this.setSprite(art.getSprite("down"));
        }
    }

    /** Whether entity is within a tile of the given coordinates. */
    protected boolean isNear(EngineState engine, int x, int y) {
        return this.distanceFrom(x, y) < engine.getDimensions().tileSize();
    }

    /** Whether entity is within a tile of the given target. */
    protected boolean isNear(EngineState engine, HasPosition target) {
        if (target == null) return false;
        return this.distanceFrom(target) < engine.getDimensions().tileSize();
    }

    /** Common per-tick hook for subclasses to call at start if they need base movement. */
    protected void baseTickMove(EngineState engine, GameState game) {
        super.tick(engine, game); // one move via Npc.tick
        // no extra move here; subclasses may call move() later as per original classes
    }

    /** Hook for interacting with game state; subclasses override their own tick(GameState) logic. */
    @Override
    public abstract void tick(EngineState engine, GameState game);
}
