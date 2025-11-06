package builder.inventory.ui;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.ui.FixedDisplay;

/**
 * A square to draw to the screen to represent an inventory slot's background. The inventory square
 * may be activated (meaning the player is holding this slots item) in which case a red border is
 * drawn around it.
 *
 * @provided
 */
public class InventorySquare extends FixedDisplay {
    private static final SpriteGroup inventoryArt = SpriteGallery.inventory;
    private boolean isActive = false;

    /**
     * Construct a new inventory square at the given x, y position.
     *
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public InventorySquare(int x, int y) {
        super(x, y);
    }

    /**
     * Set the inventory square to be either active or inactive.
     *
     * @param active Whether this square should be active (true for active).
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public Sprite getSprite() {
        if (isActive) {
            return inventoryArt.getSprite("activeborder");
        }
        return inventoryArt.getSprite("border");
    }
}
