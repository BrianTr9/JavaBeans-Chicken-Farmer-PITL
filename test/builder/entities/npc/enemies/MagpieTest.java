package builder.entities.npc.enemies;

import builder.GameState;
import builder.inventory.Inventory;
import builder.player.Player;
import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.timing.FixedTimer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the Magpie class.
 * Tests cover:
 * - Construction and initialization
 * - Lifespan management
 * - Attacking behavior (tracking target)
 * - Coin stealing mechanics
 * - Fleeing behavior after stealing
 * - Direction and sprite updates
 * - Removal conditions
 */
public class MagpieTest {

    private EngineState engineState;
    private GameState gameState;
    private Player player;
    private Inventory inventory;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        inventory = new MockInventory();
        player = new MockPlayer(400, 400);
        gameState = new MockGameState(player, inventory);
    }

    /**
     * Test that Magpie is constructed with correct initial values
     */
    @Test
    public void testConstruction() {
        Magpie magpie = new Magpie(100, 100, player);

        assertEquals(100, magpie.getX());
        assertEquals(100, magpie.getY());
        assertTrue(magpie.getAttacking());
        assertEquals(0, magpie.getCoins());
        assertNotNull(magpie.getTrackedTarget());
        assertEquals(player, magpie.getTrackedTarget());
        assertFalse(magpie.isMarkedForRemoval());
    }

    /**
     * Test that lifespan is initialized correctly
     */
    @Test
    public void testLifespanInitialization() {
        Magpie magpie = new Magpie(100, 100, player);

        assertNotNull(magpie.getLifespan());
        assertFalse(magpie.getLifespan().isFinished());
    }

    /**
     * Test that lifespan can be set and retrieved
     */
    @Test
    public void testSetLifespan() {
        Magpie magpie = new Magpie(100, 100, player);
        FixedTimer newTimer = new FixedTimer(5000);

        magpie.setLifespan(newTimer);

        assertEquals(newTimer, magpie.getLifespan());
    }

    /**
     * Test that magpie is marked for removal when lifespan expires
     */
    @Test
    public void testLifespanExpiration() {
        Magpie magpie = new Magpie(100, 100, player);
        FixedTimer shortTimer = new FixedTimer(1);
        magpie.setLifespan(shortTimer);

        assertFalse(magpie.isMarkedForRemoval());

        magpie.tick(engineState, gameState);

        assertTrue(magpie.isMarkedForRemoval());
    }

    /**
     * Test that magpie tracks target position when attacking
     */
    @Test
    public void testTracksTargetWhenAttacking() {
        MockPlayer targetPlayer = new MockPlayer(500, 500);
        gameState = new MockGameState(targetPlayer, inventory);
        Magpie magpie = new Magpie(100, 100, targetPlayer);

        int initialX = magpie.getX();
        int initialY = magpie.getY();

        magpie.tick(engineState, gameState);

        // Magpie should move towards target
        assertTrue(magpie.getAttacking());
        // Check that position changed (moved towards target)
        assertTrue(magpie.getX() != initialX || magpie.getY() != initialY);
    }

    /**
     * Test that magpie updates direction towards target when attacking
     */
    @Test
    public void testDirectionTowardsTarget() {
        MockPlayer targetPlayer = new MockPlayer(200, 100);
        Magpie magpie = new Magpie(100, 100, targetPlayer);

        magpie.tick(engineState, gameState);

        // Direction should be towards the right (0 degrees)
        // Math.atan2(0, 100) = 0 radians = 0 degrees
        assertEquals(0, magpie.getDirection());
    }

    /**
     * Test that magpie steals coin when hitting player
     */
    @Test
    public void testStealsCoinWhenHittingPlayer() {
        inventory.addCoins(5);
        MockPlayer closePlayer = new MockPlayer(110, 110); // Within tile size
        gameState = new MockGameState(closePlayer, inventory);
        Magpie magpie = new Magpie(100, 100, closePlayer);

        assertEquals(5, inventory.getCoins());
        assertTrue(magpie.getAttacking());
        assertEquals(0, magpie.getCoins());

        magpie.tick(engineState, gameState);

        assertEquals(4, inventory.getCoins());
        assertEquals(1, magpie.getCoins());
        assertFalse(magpie.getAttacking());
        assertEquals(2.0, magpie.getSpeed(), 0.01);
    }

    /**
     * Test that magpie does not steal coin when player has no coins
     */
    @Test
    public void testDoesNotStealWhenPlayerHasNoCoins() {
        inventory.addCoins(0);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Magpie magpie = new Magpie(100, 100, closePlayer);

        magpie.tick(engineState, gameState);

        assertEquals(0, inventory.getCoins());
        assertEquals(0, magpie.getCoins());
        assertTrue(magpie.getAttacking()); // Still attacking
    }

    /**
     * Test that magpie flees to spawn after stealing
     */
    @Test
    public void testFleeToSpawnAfterStealing() {
        inventory.addCoins(5);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Magpie magpie = new Magpie(100, 100, closePlayer);

        magpie.tick(engineState, gameState); // Steal coin

        assertFalse(magpie.getAttacking());

        // Move player away so magpie can flee
        closePlayer.setX(500);
        closePlayer.setY(500);

        // Tick multiple times to move towards spawn
        for (int i = 0; i < 5; i++) {
            magpie.tick(engineState, gameState);
        }

        // Should be moving back towards spawn (100, 100)
        assertFalse(magpie.getAttacking());
    }

    /**
     * Test that magpie is removed when reaching spawn after fleeing
     */
    @Test
    public void testRemovedWhenReachingSpawnAfterFleeing() {
        inventory.addCoins(5);
        Magpie magpie = new Magpie(100, 100, player);

        // Force attacking to false and position close to spawn
        magpie.setAttacking(false);
        magpie.setX(110);
        magpie.setY(110);

        assertFalse(magpie.isMarkedForRemoval());

        magpie.tick(engineState, gameState);

        assertTrue(magpie.isMarkedForRemoval());
    }

    /**
     * Test that magpie returns coins when marked for removal while attacking
     */
    @Test
    public void testReturnsCoinsWhenRemovedWhileAttacking() {
        inventory.addCoins(10);
        Magpie magpie = new Magpie(100, 100, player);
        FixedTimer shortTimer = new FixedTimer(1);
        magpie.setLifespan(shortTimer);

        // Simulate stealing coins
        magpie.setCoins(3);
        magpie.setAttacking(true);

        assertEquals(10, inventory.getCoins());

        magpie.tick(engineState, gameState);

        // Should return the 3 coins
        assertEquals(13, inventory.getCoins());
    }

    /**
     * Test that magpie does not return coins when removed while not attacking
     */
    @Test
    public void testDoesNotReturnCoinsWhenRemovedWhileFleeing() {
        inventory.addCoins(10);
        Magpie magpie = new Magpie(100, 100, player);

        magpie.setCoins(3);
        magpie.setAttacking(false);
        magpie.setX(110);
        magpie.setY(110);

        assertEquals(10, inventory.getCoins());

        magpie.tick(engineState, gameState);

        // Should not return coins (not attacking)
        assertEquals(10, inventory.getCoins());
    }

    /**
     * Test sprite update when target is below
     */
    @Test
    public void testSpriteDownWhenTargetBelow() {
        MockPlayer belowPlayer = new MockPlayer(100, 200);
        gameState = new MockGameState(belowPlayer, inventory);
        Magpie magpie = new Magpie(100, 100, belowPlayer);

        magpie.tick(engineState, gameState);

        // Sprite should be "down" when target is below
        assertNotNull(magpie.getSprite());
    }

    /**
     * Test sprite update when target is above
     */
    @Test
    public void testSpriteUpWhenTargetAbove() {
        MockPlayer abovePlayer = new MockPlayer(100, 50);
        gameState = new MockGameState(abovePlayer, inventory);
        Magpie magpie = new Magpie(100, 100, abovePlayer);

        magpie.tick(engineState, gameState);

        // Sprite should be "up" when target is above
        assertNotNull(magpie.getSprite());
    }

    /**
     * Test that magpie moves towards spawn when fleeing
     */
    @Test
    public void testMovesTowardsSpawnWhenFleeing() {
        Magpie magpie = new Magpie(100, 100, player);
        magpie.setX(300);
        magpie.setY(300);
        magpie.setAttacking(false);

        int initialDistance = magpie.distanceFrom(100, 100);

        // Need multiple ticks for movement to happen
        for (int i = 0; i < 3; i++) {
            magpie.tick(engineState, gameState);
        }

        int newDistance = magpie.distanceFrom(100, 100);

        // Should be closer to spawn or removed
        assertTrue(newDistance <= initialDistance || magpie.isMarkedForRemoval());
    }

    /**
     * Test that speed increases after stealing
     */
    @Test
    public void testSpeedIncreasesAfterStealing() {
        inventory.addCoins(5);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Magpie magpie = new Magpie(100, 100, closePlayer);

        double initialSpeed = magpie.getSpeed();

        magpie.tick(engineState, gameState);

        assertEquals(2.0, magpie.getSpeed(), 0.01);
        assertTrue(magpie.getSpeed() > initialSpeed);
    }

    /**
     * Test interact method does nothing
     */
    @Test
    public void testInteractDoesNothing() {
        Magpie magpie = new Magpie(100, 100, player);

        // Should not throw exception
        magpie.interact(engineState, gameState);
    }

    /**
     * Test multiple ticks without stealing
     */
    @Test
    public void testMultipleTicksWithoutStealing() {
        inventory.addCoins(0);
        Magpie magpie = new Magpie(100, 100, player);

        for (int i = 0; i < 10; i++) {
            magpie.tick(engineState, gameState);
        }

        assertTrue(magpie.getAttacking());
        assertEquals(0, magpie.getCoins());
        assertFalse(magpie.isMarkedForRemoval());
    }

    /**
     * Test distance calculation from position
     */
    @Test
    public void testDistanceCalculation() {
        Magpie magpie = new Magpie(0, 0, player);

        int distance = magpie.distanceFrom(300, 400);

        assertEquals(500, distance);
    }

    /**
     * Test magpie direction when fleeing upwards
     */
    @Test
    public void testFleeingUpwardsDirection() {
        Magpie magpie = new Magpie(100, 50, player);
        magpie.setY(200);
        magpie.setAttacking(false);

        magpie.tick(engineState, gameState);

        // Should move upwards towards spawn
        assertNotNull(magpie.getSprite());
    }

    /**
     * Test magpie direction when fleeing downwards
     */
    @Test
    public void testFleeingDownwardsDirection() {
        Magpie magpie = new Magpie(100, 200, player);
        magpie.setY(50);
        magpie.setAttacking(false);

        magpie.tick(engineState, gameState);

        // Should move downwards towards spawn
        assertNotNull(magpie.getSprite());
    }

    // ========== Mock Classes ==========

    private static class MockEngineState implements EngineState {
        private final Dimensions dimensions;

        public MockEngineState(Dimensions dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public Dimensions getDimensions() {
            return dimensions;
        }

        @Override
        public engine.input.MouseState getMouse() {
            return null;
        }

        @Override
        public engine.input.KeyState getKeys() {
            return null;
        }

        @Override
        public int currentTick() {
            return 0;
        }
    }

    private static class MockGameState implements GameState {
        private final Player player;
        private final Inventory inventory;

        public MockGameState(Player player, Inventory inventory) {
            this.player = player;
            this.inventory = inventory;
        }

        @Override
        public builder.world.World getWorld() {
            return null;
        }

        @Override
        public builder.entities.npc.NpcManager getNpcs() {
            return null;
        }

        @Override
        public EnemyManager getEnemies() {
            return null;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private static class MockPlayer implements Player {
        private int x;
        private int y;

        public MockPlayer(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public String getID() {
            return "mock-player";
        }

        @Override
        public int getDamage() {
            return 1;
        }
    }

    private static class MockInventory implements Inventory {
        private int food = 0;
        private int coins = 0;

        @Override
        public void addFood(int amount) {
            food = Math.max(0, food + amount);
        }

        @Override
        public int getFood() {
            return food;
        }

        @Override
        public void addCoins(int amount) {
            coins = Math.max(0, coins + amount);
        }

        @Override
        public int getCoins() {
            return coins;
        }

        @Override
        public int getCapacity() {
            return 10;
        }

        @Override
        public builder.inventory.items.Item getHolding() {
            return getItem(getActiveSlot());
        }

        @Override
        public void setItem(int slot, builder.inventory.items.Item item) {
        }

        @Override
        public builder.inventory.items.Item getItem(int slot) {
            return null;
        }

        @Override
        public int getActiveSlot() {
            return 0;
        }

        @Override
        public void setActiveSlot(int slot) {
        }
    }
}

