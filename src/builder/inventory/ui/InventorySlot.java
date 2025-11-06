package builder.inventory.ui;

import builder.inventory.items.Item;
import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.ui.FixedDisplay;

/**
 * A slot within the player's inventory used to render the item's inventory sprite ({@link
 * Item#inventorySprite()}).
 *
 * @provided
 */
public class InventorySlot extends FixedDisplay {
    private static final SpriteGroup inventoryArt = SpriteGallery.inventory;
    private Item item = null;

    /**
     * Construct a new inventory slot at the given x, y position.
     *
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public InventorySlot(int x, int y) {
        super(x, y);
    }

    /**
     * Update the item displayed at this slot.
     *
     * <p>The item may be null indicating no item at this slot.
     *
     * <p>If the item is not null, the slot renders as {@link Item#inventorySprite()}.
     *
     * @param item The possibly null item at this slot.
     */
    public void updateItem(Item item) {
        this.item = item;
    }

    @Override
    public Sprite getSprite() {
        if (item == null) {
            return inventoryArt.getSprite("empty");
        }
        return item.inventorySprite();
    }
}
