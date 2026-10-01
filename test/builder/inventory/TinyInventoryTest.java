package builder.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import builder.inventory.items.Bucket;
import builder.inventory.items.Hoe;
import builder.inventory.items.Item;

import org.junit.Before;
import org.junit.Test;

public class TinyInventoryTest {

    private TinyInventory inventory;

    @Before
    public void setUp() {
        inventory = new TinyInventory(4);
    }

    @Test
    public void newInventoryIsEmpty() {
        assertEquals(4, inventory.getCapacity());
        assertEquals(0, inventory.getCoins());
        assertEquals(0, inventory.getFood());
        assertEquals(0, inventory.getActiveSlot());
        assertNull(inventory.getHolding());
        for (int slot = 0; slot < inventory.getCapacity(); slot++) {
            assertNull(inventory.getItem(slot));
        }
    }

    @Test
    public void startingResourcesConstructor() {
        TinyInventory stocked = new TinyInventory(5, 20, 30);
        assertEquals(5, stocked.getCapacity());
        assertEquals(20, stocked.getCoins());
        assertEquals(30, stocked.getFood());
    }

    @Test
    public void setItemOnlyChangesThatSlot() {
        Item hoe = new Hoe();
        inventory.setItem(2, hoe);
        assertSame(hoe, inventory.getItem(2));
        assertNull(inventory.getItem(1));
        assertNull(inventory.getItem(3));
    }

    @Test
    public void setItemReplacesAndClearsSlot() {
        inventory.setItem(0, new Hoe());
        Item bucket = new Bucket();
        inventory.setItem(0, bucket);
        assertSame(bucket, inventory.getItem(0));
        inventory.setItem(0, null);
        assertNull(inventory.getItem(0));
    }

    @Test
    public void holdingFollowsActiveSlot() {
        Item hoe = new Hoe();
        Item bucket = new Bucket();
        inventory.setItem(0, hoe);
        inventory.setItem(3, bucket);

        assertSame(hoe, inventory.getHolding());
        inventory.setActiveSlot(3);
        assertEquals(3, inventory.getActiveSlot());
        assertSame(bucket, inventory.getHolding());
        inventory.setActiveSlot(1);
        assertNull("empty active slot means holding nothing", inventory.getHolding());
    }

    @Test
    public void addCoinsAccumulates() {
        inventory.addCoins(3);
        inventory.addCoins(4);
        assertEquals(7, inventory.getCoins());
        inventory.addCoins(-5);
        assertEquals(2, inventory.getCoins());
    }

    @Test
    public void coinsNeverGoNegative() {
        inventory.addCoins(2);
        inventory.addCoins(-5);
        assertEquals(0, inventory.getCoins());
        inventory.addCoins(1);
        assertEquals("clamping must not leave a hidden debt", 1, inventory.getCoins());
    }

    @Test
    public void addFoodAccumulates() {
        inventory.addFood(5);
        inventory.addFood(-2);
        assertEquals(3, inventory.getFood());
    }

    @Test
    public void foodNeverGoesNegative() {
        inventory.addFood(1);
        inventory.addFood(-3);
        assertEquals(0, inventory.getFood());
        inventory.addFood(2);
        assertEquals(2, inventory.getFood());
    }

    @Test
    public void coinsAndFoodAreIndependent() {
        inventory.addCoins(5);
        inventory.addFood(-5);
        assertEquals(5, inventory.getCoins());
        assertEquals(0, inventory.getFood());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeStartingCoinsAreRejected() {
        new TinyInventory(5, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeStartingFoodIsRejected() {
        new TinyInventory(5, 0, -1);
    }
}
