package builder;

import engine.EngineState;

/**
 * A game component that performs some progression during each game tick.
 *
 * @provided
 */
public interface Tickable {

    /**
     * The tick method is called on most components on the game each time the tick event is
     * dispatched by the game engine (i.e. {@link JavaBeanFarm#tick(EngineState)} is called).
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     */
    void tick(EngineState state, GameState game);
}
