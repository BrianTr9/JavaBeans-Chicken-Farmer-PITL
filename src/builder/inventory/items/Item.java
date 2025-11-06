package builder.inventory.items;

import engine.art.sprites.Sprite;
import engine.timing.Animation;

import java.util.Optional;

/**
 * An item is stored in the player's inventory and may be used by the player. For example, a hoe may
 * be stored in the player's inventory and held then used.
 *
 * @provided
 */
public interface Item {
    /**
     * Return the sprite to render in the player's inventory.
     *
     * @return A sprite representing this item.
     */
    Sprite inventorySprite();

    /**
     * Return the optional animation to player when the player uses this item.
     *
     * <p>Note that not all items will have a use animation and should return {@link
     * Optional#empty()} if they do not.
     *
     * @return The animation to play for the player.
     */
    Optional<Animation> useAnimation();
}
