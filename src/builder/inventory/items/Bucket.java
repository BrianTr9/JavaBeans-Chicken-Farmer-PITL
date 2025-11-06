package builder.inventory.items;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.timing.Animation;
import engine.timing.AnimationDuration;

import java.util.Optional;

/**
 * A bucket tool is used to plant new crops on tilled dirt. A bucket uses the 'bucket' inventory
 * sprite within {@link SpriteGallery#tools} and uses 'plant1', 'plant2' from {@link
 * SpriteGallery#chickenFarmer} when used by the player.
 *
 * @provided
 */
public class Bucket implements Item {
    private static final SpriteGroup toolArt = SpriteGallery.tools;
    private static final SpriteGroup art = SpriteGallery.chickenFarmer;
    private final Animation water =
            new Animation(
                    AnimationDuration.MEDIUM,
                    new Sprite[] {art.getSprite("plant1"), art.getSprite("plant2")});

    /** Construct a new bucket instance. */
    public Bucket() {}

    @Override
    public Sprite inventorySprite() {
        return toolArt.getSprite("bucket");
    }

    @Override
    public Optional<Animation> useAnimation() {
        return Optional.of(water);
    }
}
