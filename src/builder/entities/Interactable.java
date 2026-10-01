package builder.entities;

import builder.GameState;

import engine.EngineState;

/**
 * A game component that has an interaction behaviour with the player. The interaction event
 * (calling of {@link #interact}) is triggered when the player occupies the same grid square as this
 * component. The event continues to be fired each tick for as long as the player occupies the
 * space.
 *
 * <p>Note that for left-click behaviour, {@link Usable} should be used instead.
 *
 * @stage3
 */
public interface Interactable {

    /**
     * Handles interaction behaviour with the player.
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     */
    void interact(EngineState state, GameState game);
}
