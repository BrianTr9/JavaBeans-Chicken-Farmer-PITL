package builder.inventory.items;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.timing.Animation;

import java.util.Optional;

/**
 * A hoe item may be used by the player to till dirt tiles so that they can be planted. A hoe uses
 * the 'hoe' inventory sprite within {@link SpriteGallery#tools}. There is no player animation for
 * using a hoe.
 *
 * @provided
 */
public class Hoe implements Item {
    private static final SpriteGroup toolArt = SpriteGallery.tools;

    /** Construct a new hoe instance. */
    public Hoe() {}

    @Override
    public Sprite inventorySprite() {
        return toolArt.getSprite("hoe");
    }

    @Override
    public Optional<Animation> useAnimation() {
        return Optional.empty();
    }
}
