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

    /** Whether this bird is currently attacking (true) or fleeing/returning (false). */
    private boolean attacking = true;

    /** The target the bird is tracking; may be a Player or a Tile (HasPosition). */
    private HasPosition trackedTarget;

    /** Spawn X coordinate for this bird. */
    private final int spawnX;

    /** Spawn Y coordinate for this bird. */
    private final int spawnY;

    /** Optional lifespan timer; if set the bird will expire when finished. */
    private FixedTimer lifespan;

    /** True once the bird has flown back to its spawn and removed itself. */
    private boolean returnedHome = false;

    /**
     * Construct a new AbstractBird located at the given coordinates.
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     */
    protected AbstractBird(int x, int y) {
        super(x, y);
        this.spawnX = x;
        this.spawnY = y;
        this.lifespan = null; // subclasses should set a lifespan duration explicitly
    }

    // --- Expirable implementation -------------------------------------------------

    /**
     * Get the lifespan timer for this bird, or null if none is set.
     *
     * @return the FixedTimer representing the lifespan, or null
     */
    @Override
    public FixedTimer getLifespan() {
        return this.lifespan;
    }

    /**
     * Set the lifespan timer for this bird.
     *
     * @param lifespan the FixedTimer to set
     */
    @Override
    public void setLifespan(FixedTimer lifespan) {
        this.lifespan = lifespan;
    }

    // --- Common bird attribute accessors -----------------------------------------

    /**
     * Whether the bird is currently in attacking mode.
     *
     * @return true if attacking, false if returning/fleeing
     */
    public boolean getAttacking() {
        return this.attacking;
    }

    /**
     * Set whether the bird is attacking.
     *
     * @param attacking true to set attacking mode, false to set fleeing/return mode
     */
    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    /**
     * Get the current tracked target for this bird.
     *
     * @return a HasPosition representing the tracked target, or null
     */
    public HasPosition getTrackedTarget() {
        return this.trackedTarget;
    }

    /**
     * Set the tracked target for this bird.
     *
     * @param trackedTarget the HasPosition to track
     */
    public void setTrackedTarget(HasPosition trackedTarget) {
        this.trackedTarget = trackedTarget;
    }

    /**
     * Get the X coordinate of this bird's spawn location.
     *
     * @return the spawn X coordinate
     */
    public int getSpawnX() {
        return this.spawnX;
    }

    /**
     * Get the Y coordinate of this bird's spawn location.
     *
     * @return the spawn Y coordinate
     */
    public int getSpawnY() {
        return this.spawnY;
    }

    /**
     * Returns whether this bird got back to its spawn before being removed. Loot carried
     * home is kept; loot of a bird removed anywhere else goes back to the player.
     *
     * @return true if the bird removed itself at its spawn
     */
    public boolean hasReturnedHome() {
        return this.returnedHome;
    }

    /** Removes this bird from the world because it has reached its spawn. */
    protected void removeAtSpawn() {
        this.returnedHome = true;
        this.markForRemoval();
    }

    // --- Protected helper methods -----------------------------------------------

    /**
     * Update sprite based on whether the target y is below or above current y, using provided
     * art. Chooses "down" when target is below current y, otherwise "up".
     *
     * @param art the sprite group to use (may be null)
     * @param targetY the y coordinate of the target
     */
    protected void updateVerticalSprite(SpriteGroup art, int targetY) {
        if (art == null) {
            return;
        }

        if (targetY > this.getY()) {
            this.setSprite(art.getSprite("down"));
        } else {
            this.setSprite(art.getSprite("up"));
        }
    }

    /**
     * Update sprite based on moving towards/away from spawn.
     *
     * @param art the sprite group to use (may be null)
     * @param spawnY the spawn y coordinate
     */
    protected void updateVerticalSpriteTowardsSpawn(SpriteGroup art, int spawnY) {
        if (art == null) {
            return;
        }

        if (spawnY < this.getY()) {
            this.setSprite(art.getSprite("up"));
        } else {
            this.setSprite(art.getSprite("down"));
        }
    }

    /** Whether the entity is within a tile of the given coordinates. */
    protected boolean isNear(EngineState engine, int x, int y) {
        return this.distanceFrom(x, y)
                < engine.getDimensions().tileSize();
    }

    /**
     * Common per-tick hook for subclasses to call at start if they need base movement.
     *
     * @param engine the engine state
     * @param game the game state
     */
    protected void baseTickMove(EngineState engine, GameState game) {
        super.tick(engine, game); // moves once along the current heading
    }

    /**
     * Subclasses must implement their per-tick behaviour.
     *
     * @param engine engine state for this tick
     * @param game current game state
     */
    @Override
    public abstract void tick(EngineState engine, GameState game);
}
