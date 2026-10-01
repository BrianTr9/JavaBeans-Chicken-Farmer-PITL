package builder.inventory.items;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.timing.Animation;

import java.util.Optional;

/** An inventory item used to plant a scarecrow. */
public class Pole implements Item {
    private static final SpriteGroup toolArt = SpriteGallery.tools;

    @Override
    public Sprite inventorySprite() {
        return toolArt.getSprite("pole");
    }

    @Override
    public Optional<Animation> useAnimation() {
        return Optional.empty();
    }
}
