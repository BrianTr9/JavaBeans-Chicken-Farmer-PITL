package builder.entities;

import builder.GameState;
import builder.inventory.Inventory;

import engine.EngineState;

/**
 * A game component that can be used when the player left-clicks while on it.
 *
 * <p>When the player left-clicks anywhere on the screen, the {@link #use(EngineState, GameState)}
 * method of any {@link Usable} entities underneath the player will be called.
 *
 * @stage3
 */
public interface Usable {

    /**
     * Process the player using this component by left-clicking.
     *
     * <p>The state of the player and the player's inventory (provided by the game state) may be
     * useful in determining the appropriate effects. In particular the {@link
     * Inventory#getHolding()} method indicates the item currently held by the player.
     *
     * @param state The state of the engine provides information about which tick this interaction
     *     occurred during.
     * @param game The game state that can be queried or updated as needed.
     */
    void use(EngineState state, GameState game);
}
