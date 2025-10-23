package builder.entities.npc.spawners;

import builder.GameState;
import builder.Tickable;

import engine.EngineState;
import engine.game.HasPosition;
import engine.timing.TickTimer;

/**
 * A spawner is responsible for spawning specific types of {@link builder.entities.npc.Npc}s or
 * {@link builder.entities.npc.enemies.Enemy}s.
 */
public interface Spawner extends HasPosition, Tickable {

    /**
     * Get the internal timer used to control spawn intervals.
     *
     * @return the TickTimer used by this spawner
     */
    TickTimer getTimer();

    /**
     * Progress the spawner by one tick. Implementations may spawn entities when their timer
     * finishes.
     *
     * @param state the engine state for this tick
     * @param game the current game state
     */
    @Override
    void tick(EngineState state, GameState game);

    /**
     * Get the X coordinate of this spawner.
     *
     * @return the horizontal coordinate in pixels
     */
    @Override
    int getX();

    /**
     * Set the X coordinate of this spawner.
     *
     * @param x the horizontal coordinate in pixels
     */
    @Override
    void setX(int x);

    /**
     * Get the Y coordinate of this spawner.
     *
     * @return the vertical coordinate in pixels
     */
    @Override
    int getY();

    /**
     * Set the Y coordinate of this spawner.
     *
     * @param y the vertical coordinate in pixels
     */
    @Override
    void setY(int y);
}
