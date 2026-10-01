package builder.inventory;

import builder.inventory.items.Item;

/**
 * An interface to the player's inventory. The player's inventory tracks resources such as food and
 * coins and the items that a player can hold, including what they are currently holding.
 *
 * @invariant getFood() >= 0
 * @invariant getCoins() >= 0
 * @invariant getCapacity() >= 0
 * @provided
 */
public interface Inventory {
    /**
     * Add (or possibly remove) the given amount of food from the inventory.
     *
     * <p>If the amount is negative, the food in the inventory will be reduced by the amount but
     * will not go below zero.
     *
     * @param amount The amount of food to add or remove from the inventory.
     * @ensures getFood() = max(0, \old(getFood()) + amount)
     */
    void addFood(int amount);

    /**
     * Returns the amount of food in the inventory.
     *
     * @return The amount of food in the inventory.
     * @ensures \result >= 0
     */
    int getFood();

    /**
     * Add (or possibly remove) the given number of coins from the inventory.
     *
     * <p>If the amount is negative, the coins in the inventory will be reduced by the amount but
     * will not go below zero.
     *
     * @param amount The number of coins to add or remove from the inventory.
     * @ensures getCoins() = max(0, \old(getCoins()) - amount)
     */
    void addCoins(int amount);

    /**
     * Returns the number of coins in the inventory.
     *
     * @return The number of coins in the inventory.
     * @ensures \result >= 0
     */
    int getCoins();

    /**
     * The number of items that can be held by this inventory.
     *
     * @return The maximum number of items this inventory can store.
     * @ensures \result >= 0
     */
    int getCapacity();

    /**
     * Set the given slot in the inventory to hold the given item.
     *
     * @requires slot >= 0
     * @requires slot &lt; getCapacity()
     * @param slot The slot index, starting from zero.
     * @param item An item instance to store in the inventory.
     * @ensures getItem(slot) = item
     */
    void setItem(int slot, Item item);

    /**
     * Returns the item stored at the given slot.
     *
     * <p>Returns null if there is no item at that slot.
     *
     * @requires slot >= 0
     * @requires slot &lt; getCapacity()
     * @param slot The slot index, starting from zero.
     * @return The item instance stored at the given index, or null.
     */
    Item getItem(int slot);

    /**
     * Returns the item that the player is currently holding.
     *
     * <p>This method must be consistent with {@link #getItem(int)} and {@link #getActiveSlot()}.
     *
     * @return The item in the inventory at the active slot.
     * @ensures \result = getItem(getActiveSlot())
     */
    Item getHolding();

    /**
     * Returns the slot that the player currently has activated.
     *
     * @return The active slot.
     * @ensures \result >= 0
     * @ensures \result &lt; getCapacity()
     */
    int getActiveSlot();

    /**
     * Set the active slot to be the given slot.
     *
     * @requires slot >= 0
     * @requires slot &lt; getCapacity()
     * @param slot The new active slot.
     * @ensures getActiveSlot() = slot
     */
    void setActiveSlot(int slot);
}
