package builder.inventory.items;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.timing.Animation;

import java.util.Optional;

/** An inventory item used to make a bee hive. */
public class HiveHammer implements Item {

    private static final SpriteGroup toolArt = SpriteGallery.tools;

    @Override
    public Sprite inventorySprite() {
        return toolArt.getSprite("hivehammer");
    }

    @Override
    public Optional<Animation> useAnimation() {
        return Optional.empty();
    }
}
