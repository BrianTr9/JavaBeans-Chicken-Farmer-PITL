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
