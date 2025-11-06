package builder.inventory.items;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.timing.Animation;
import engine.timing.AnimationDuration;

import java.util.Optional;

/**
 * A jackhammer item may be used when the player is above an ore to extract its contents. A
 * jackhammer uses the 'jackhammer' inventory sprite within {@link SpriteGallery#tools} and the
 * 'jackhammer1' and 'jackhammer2' animation within {@link SpriteGallery#chickenFarmer}.
 *
 * @provided
 */
public class Jackhammer implements Item {
    private static final SpriteGroup toolArt = SpriteGallery.tools;
    private static final SpriteGroup art = SpriteGallery.chickenFarmer;
    private final Animation dig =
            new Animation(
                    AnimationDuration.MEDIUM,
                    new Sprite[] {art.getSprite("jackhammer1"), art.getSprite("jackhammer2")});

    /** Construct a new jackhammer instance. */
    public Jackhammer() {}

    @Override
    public Sprite inventorySprite() {
        return toolArt.getSprite("jackhammer");
    }

    @Override
    public Optional<Animation> useAnimation() {
        return Optional.of(dig);
    }
}
