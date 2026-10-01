package builder.inventory.ui;

import builder.GameState;
import builder.inventory.Inventory;
import builder.inventory.items.Item;
import builder.ui.Overlay;

import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An overlay to display the player's inventory slots. The inventory overlay will display each item
 * in the inventory in order (according to {@link Item#inventorySprite()}) and highlight the active
 * item with a red border.
 *
 * <p>When the player presses a number key, the active slot of the inventory will be updated the
 * number 1 corresponds to the item slot 0 and so on.
 *
 * @provided
 */
public class InventoryOverlay implements Overlay {
    // invariant: slots <= 9
    // invariant: slots == inventorySquares.length == inventorySlots.length
    private final int slots;
    private final InventorySquare[] inventorySquares;
    private final InventorySlot[] inventorySlots;

    /**
     * Construct a new inventory overlay given the dimensions and maximum number of slots to
     * display.
     *
     * @requires slots &le; 9
     * @param dimensions The dimensions used for the overlay.
     * @param slots The number of slots to display for the inventory.
     */
    public InventoryOverlay(Dimensions dimensions, int slots) {
        assert slots <= 9;
        this.slots = slots;
        inventorySquares = new InventorySquare[slots];
        inventorySlots = new InventorySlot[slots];

        int midScreen = dimensions.windowSize() / 2;
        int inventoryWidth = slots * dimensions.tileSize();
        // draw tiles from mid-screen minus half the inventory with
        // so center of the inventory is at mid-screen
        int x = midScreen - (inventoryWidth / 2);
        int y = dimensions.windowSize() - dimensions.tileSize();

        for (int i = 0; i < slots; i++) {
            inventorySquares[i] = new InventorySquare(x, y);
            inventorySlots[i] = new InventorySlot(x, y);
            x += dimensions.tileSize();
        }
    }

    @Override
    public List<Renderable> render() {
        List<Renderable> renderables = new ArrayList<>();
        renderables.addAll(Arrays.asList(inventorySquares));
        renderables.addAll(Arrays.asList(inventorySlots));
        return renderables;
    }

    @Override
    public void tick(EngineState state, GameState game) {
        Inventory inventory = game.getInventory();
        for (int i = 1; i <= slots; i++) {
            char character = (i + "").charAt(0); // invariant slots <= 9 ensures sane behaviour
            if (state.getKeys().isDown(character)) {
                inventory.setActiveSlot(i - 1); // offset slot index by -1
            }
        }

        // update displays
        for (int i = 0; i < slots; i++) {
            inventorySquares[i].setActive(i == inventory.getActiveSlot());
            inventorySlots[i].updateItem(inventory.getItem(i));
        }
    }
}
