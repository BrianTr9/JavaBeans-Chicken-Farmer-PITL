package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.npc.Npc;

import engine.EngineState;

/**
 * A simple enemy base class used for bird-like enemies.
 */
public class Enemy extends Npc {

    /**
     * Construct an Enemy at the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Enemy(int x, int y) {
        super(x, y);
    }

    /**
     * Progress this enemy by one tick. Subclasses may override to add behaviour.
     *
     * @param state The engine state for this tick.
     * @param game The current game state.
     */
    @Override
    public void tick(EngineState state, GameState game) {
        super.tick(state, game);
    }

    /**
     * Returns how many times {@link EnemyManager} ticks this enemy each frame.
     *
     * <p>Faster enemies take several simulation steps per frame rather than moving further
     * per step, which keeps their steering and arrival checks precise.
     *
     * @return the number of ticks per frame, at least 1
     */
    public int ticksPerFrame() {
        return 1;
    }

    /**
     * Called exactly once by {@link EnemyManager} when this enemy is removed from the world,
     * whatever the cause (returning home, expiring or being caught by a bee). No-op by default.
     *
     * @param game the current game state
     */
    public void onRemoved(GameState game) {
        // intentionally empty
    }

    /**
     * Interact with the game state (no-op by default).
     *
     * @param state The engine state for this tick.
     * @param game The current game state.
     */
    @Override
    public void interact(EngineState state, GameState game) {
        // intentionally empty
    }
}
