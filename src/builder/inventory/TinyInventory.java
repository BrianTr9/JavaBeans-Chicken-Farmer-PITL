package builder.inventory;

import builder.inventory.items.Item;

/**
 * An inventory implementation that stores a small, fixed number of items.
 *
 * <p>This implementation is used where a compact inventory is sufficient (typically for tests
 * and seeding). Capacity must be no greater than 10.
 */
public class TinyInventory implements Inventory {
    // note: there's no reason that this class needs to store less than 10 elements,
    //       we've simply made it so.
    private final Item[] contents;
    private int coins = 0;
    private int food = 0;
    private int active = 0;

    /**
     * Construct a new tiny inventory instance with the given capacity.
     *
     * @param size The maximum capacity of the inventory; at most 10.
     */
    public TinyInventory(int size) {
        assert size <= 10;
        contents = new Item[size];
    }

    /**
     * Construct a new tiny inventory instance with initial resources.
     *
     * @param size The maximum capacity of the inventory; at most 10.
     * @param coins The initial coin amount; not negative.
     * @param food The initial food amount; not negative.
     * @throws IllegalArgumentException if coins or food is negative
     */
    public TinyInventory(int size, int coins, int food) {
        assert size <= 10;
        if (coins < 0 || food < 0) {
            throw new IllegalArgumentException(
                    "Starting coins and food must not be negative: coins=" + coins
                            + ", food=" + food);
        }
        contents = new Item[size];
        this.coins = coins;
        this.food = food;
    }

    @Override
    public int getCapacity() {
        assert contents.length >= 0;
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
        assert active >= 0;
        assert active < getCapacity();
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
        int oldCoins = coins;
        coins = Math.max(0, coins + amount);
        assert coins == Math.max(0, oldCoins + amount);
    }

    @Override
    public void addFood(int amount) {
        int oldFood = food;
        food = Math.max(0, food + amount);
        assert food == Math.max(0, oldFood + amount);
    }

    @Override
    public int getCoins() {
        assert coins >= 0;
        return coins;
    }

    @Override
    public int getFood() {
        assert food >= 0;
        return food;
    }
}
