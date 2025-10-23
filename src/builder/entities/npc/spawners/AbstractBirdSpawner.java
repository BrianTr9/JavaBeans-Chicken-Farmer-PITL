package builder.entities.npc.spawners;

import builder.GameState;

import engine.EngineState;
import engine.game.HasPosition;
import engine.timing.RepeatingTimer;
import engine.timing.TickTimer;

/**
 * Abstract base class for bird spawners that spawn enemies at regular intervals.
 *
 * <p>This class centralizes common spawner state and behavior:
 * <ul>
 *   <li>Spawn position (x, y)</li>
 *   <li>Spawn timing via RepeatingTimer</li>
 *   <li>Distance calculation helper</li>
 * </ul>
 *
 * <p>Subclasses implement {@link #spawnBird(EngineState, GameState)} to define
 * what type of bird to create and under what conditions.
 */
public abstract class AbstractBirdSpawner implements Spawner {

    /** Spawn X coordinate for this spawner. */
    private int spawnX;

    /** Spawn Y coordinate for this spawner. */
    private int spawnY;

    private final RepeatingTimer timer;

    /**
     * Construct a bird spawner at the given position with the specified spawn interval.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     * @param duration The interval (in ticks) between spawn attempts.
     */
    protected AbstractBirdSpawner(int x, int y, int duration) {
        this.spawnX = x;
        this.spawnY = y;
        this.timer = new RepeatingTimer(duration);
    }

    @Override
    public TickTimer getTimer() {
        return this.timer;
    }

    @Override
    public void tick(EngineState state, GameState game) {
        this.timer.tick();
        if (this.timer.isFinished()) {
            spawnBird(state, game);
        }
    }

    /**
     * Spawn a bird if conditions are met. Called automatically when the timer finishes.
     *
     * @param state The engine state.
     * @param game The game state.
     */
    protected abstract void spawnBird(EngineState state, GameState game);

    /**
     * Calculate the distance from this spawner to a target position.
     *
     * @param position The target position.
     * @return The Euclidean distance in pixels.
     */
    protected int distanceFrom(HasPosition position) {
        int deltaX = position.getX() - this.spawnX;
        int deltaY = position.getY() - this.spawnY;
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public int getX() {
        return this.spawnX;
    }

    @Override
    public void setX(int x) {
        this.spawnX = x;
    }

    @Override
    public int getY() {
        return this.spawnY;
    }

    @Override
    public void setY(int y) {
        this.spawnY = y;
    }
}
