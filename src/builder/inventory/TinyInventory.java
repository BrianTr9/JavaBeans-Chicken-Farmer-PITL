package builder.inventory;

import builder.inventory.items.Item;

/**
 * An inventory implementation that stores some small number of items.
 *
 * @invariant getCapacity() &le; 10
 * @test
 * @provided
 */
public class TinyInventory implements Inventory {
    // note: there's no reason that this class needs to store less than 10 elements,
    //       we've simply made it so.
    private final Item[] contents;
    private int coins = 0;
    private int food = 0;
    private int active = 0;

    /**
     * Construct a new tiny inventory instance.
     *
     * @requires size &le; 10
     * @param size The maximum capacity of the inventory.
     */
    public TinyInventory(int size) {
        contents = new Item[size];
    }

    @Override
    public int getCapacity() {
        return contents.length;
    }

    @Override
    public void setItem(int slot, Item item) {
        assert slot >= 0;
        assert slot < getCapacity();
        contents[slot] = item;
    }

    @Override
    public void setActiveSlot(int index) {
        assert index >= 0;
        assert index < getCapacity();
        active = index;
    }

    @Override
    public int getActiveSlot() {
        return active;
    }

    @Override
    public Item getHolding() {
        // null if not holding anything
        // won't throw index out of bounds by the invariant
        return contents[getActiveSlot()];
    }

    @Override
    public Item getItem(int index) {
        assert index >= 0;
        assert index < getCapacity();
        return contents[index];
    }

    @Override
    public void addCoins(int amount) {
        coins = Math.max(0, coins + amount);
    }

    @Override
    public void addFood(int amount) {
        food = Math.max(0, food + amount);
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public int getFood() {
        return food;
    }
}
