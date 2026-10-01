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
import builder.ui.SpriteGallery;

/**
 * Comprehensive unit tests for the Eagle class.
 * Tests cover:
 * - Construction and initialization
 * - Lifespan management
 * - Attacking behavior (tracking player)
 * - Food stealing mechanics (steals 3 food)
 * - Fleeing behavior after stealing
 * - Food return when removed before reaching spawn
 * - Direction and sprite updates
 * - Speed changes (2 when attacking, 4 when fleeing)
 * - Removal conditions
 */
public class EagleTest {

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
     * Test that Eagle is constructed with correct initial values
     */
    @Test
    public void testConstruction() {
        Eagle eagle = new Eagle(100, 100, player);

        assertEquals(100, eagle.getX());
        assertEquals(100, eagle.getY());
        assertNotNull(eagle.getTrackedTarget());
        assertEquals(player, eagle.getTrackedTarget());
        assertFalse(eagle.isMarkedForRemoval());
        assertEquals(2.0, eagle.getSpeed(), 0.01);
    }

    /**
     * Test that lifespan is initialized correctly
     */
    @Test
    public void testLifespanInitialization() {
        Eagle eagle = new Eagle(100, 100, player);

        assertNotNull(eagle.getLifespan());
        assertFalse(eagle.getLifespan().isFinished());
    }

    /**
     * Test that lifespan can be set and retrieved
     */
    @Test
    public void testSetLifespan() {
        Eagle eagle = new Eagle(100, 100, player);
        FixedTimer newTimer = new FixedTimer(3000);

        eagle.setLifespan(newTimer);

        assertEquals(newTimer, eagle.getLifespan());
    }

    /**
     * Test that eagle is marked for removal when lifespan expires
     */
    @Test
    public void testLifespanExpiration() {
        Eagle eagle = new Eagle(100, 100, player);
        FixedTimer shortTimer = new FixedTimer(1);
        eagle.setLifespan(shortTimer);

        assertFalse(eagle.isMarkedForRemoval());

        eagle.tick(engineState, gameState);

        assertTrue(eagle.isMarkedForRemoval());
    }

    /**
     * Test that eagle tracks player position when attacking
     */
    @Test
    public void testTracksPlayerWhenAttacking() {
        MockPlayer targetPlayer = new MockPlayer(500, 500);
        gameState = new MockGameState(targetPlayer, inventory);
        Eagle eagle = new Eagle(100, 100, targetPlayer);

        int initialX = eagle.getX();
        int initialY = eagle.getY();

        eagle.tick(engineState, gameState);

        // Eagle should move towards target
        assertTrue(eagle.getX() != initialX || eagle.getY() != initialY);
    }

    /**
     * Test that eagle updates direction towards player when attacking
     */
    @Test
    public void testDirectionTowardsPlayer() {
        MockPlayer targetPlayer = new MockPlayer(200, 100);
        Eagle eagle = new Eagle(100, 100, targetPlayer);

        eagle.tick(engineState, gameState);

        // Direction should be towards the right (0 degrees)
        assertEquals(0, eagle.getDirection());
    }

    /**
     * Test that eagle steals 3 food when hitting player
     */
    @Test
    public void testSteals3FoodWhenHittingPlayer() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110); // Within tile size
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 100, closePlayer);

        assertEquals(10, inventory.getFood());

        eagle.tick(engineState, gameState);

        assertEquals(7, inventory.getFood()); // 10 - 3 = 7
        assertEquals(4.0, eagle.getSpeed(), 0.01); // Speed should increase to 4
    }

    /**
     * Test that eagle steals food even if player has less than 3 food
     */
    @Test
    public void testStealsWhenPlayerHasLessThan3Food() {
        inventory.addFood(2);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 100, closePlayer);

        eagle.tick(engineState, gameState);

        // Should still attempt to steal 3, resulting in 0 (or negative handled by inventory)
        assertTrue(inventory.getFood() <= 0);
    }

    /**
     * Test that eagle only steals food once
     */
    @Test
    public void testOnlyStealsOnce() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 100, closePlayer);

        eagle.tick(engineState, gameState);
        assertEquals(7, inventory.getFood());

        eagle.tick(engineState, gameState);
        assertEquals(7, inventory.getFood()); // Should not steal again
    }

    /**
     * Test that eagle flees to spawn after stealing
     */
    @Test
    public void testFleeToSpawnAfterStealing() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(200, 200, closePlayer);

        eagle.tick(engineState, gameState); // Steal food

        // Move player away so eagle can flee
        closePlayer.setX(500);
        closePlayer.setY(500);

        // After stealing, eagle should start fleeing back to spawn (200, 200)
        // Eagle speed is 4 after stealing, so it should move towards spawn
        int initialDistance = eagle.distanceFrom(200, 200);

        // Tick multiple times to move towards spawn
        for (int i = 0; i < 3; i++) {
            if (!eagle.isMarkedForRemoval()) {
                eagle.tick(engineState, gameState);
            }
        }

        // Eagle should either be closer to spawn or already removed
        assertTrue(eagle.isMarkedForRemoval() || eagle.distanceFrom(200, 200) <= initialDistance);
    }

    /**
     * Test that eagle is removed when reaching spawn after stealing
     */
    @Test
    public void testRemovedWhenReachingSpawnAfterStealing() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 100, closePlayer);

        eagle.tick(engineState, gameState); // Steal food

        // Move player away
        closePlayer.setX(500);
        closePlayer.setY(500);

        // Eagle should be near spawn, tick should remove it
        eagle.tick(engineState, gameState);

        assertTrue(eagle.isMarkedForRemoval());
    }

    /**
     * Test that eagle returns food when removed before reaching spawn
     */
    @Test
    public void testReturnsFoodWhenRemovedBeforeReachingSpawn() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(300, 300, closePlayer); // spawn far from the player
        eagle.setX(105);
        eagle.setY(105);

        eagle.tick(engineState, gameState); // within a tile of the player: steals food
        assertEquals("eagle should have stolen 3 food", 7, inventory.getFood());
        assertEquals(3, eagle.getFood());

        // Removed far from spawn (e.g. caught by a bee) before getting home.
        eagle.setX(500);
        eagle.setY(500);
        eagle.markForRemoval();
        eagle.onRemoved(gameState);

        assertEquals("stolen food should be returned", 10, inventory.getFood());

    }

    /**
     * Test that eagle uses down sprite when flying towards player below
     */
    @Test
    public void testSpriteDownWhenAttackingPlayerBelow() {
        MockPlayer playerBelow = new MockPlayer(100, 300);
        gameState = new MockGameState(playerBelow, inventory);
        Eagle eagle = new Eagle(100, 100, playerBelow);

        eagle.tick(engineState, gameState);

        assertSame("Sprite should be 'down' when target is below",
                SpriteGallery.eagle.getSprite("down"), eagle.getSprite());
    }

    /**
     * Test that eagle uses up sprite when flying towards player above
     */
    @Test
    public void testSpriteUpWhenAttackingPlayerAbove() {
        MockPlayer playerAbove = new MockPlayer(100, 50);
        gameState = new MockGameState(playerAbove, inventory);
        Eagle eagle = new Eagle(100, 300, playerAbove);

        eagle.tick(engineState, gameState);

        assertSame("Sprite should be 'up' when target is above",
                SpriteGallery.eagle.getSprite("up"), eagle.getSprite());
    }

    /**
     * Test that eagle uses up sprite when fleeing to spawn above
     */
    @Test
    public void testSpriteUpWhenFleeingToSpawnAbove() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 50, closePlayer); // spawnY = 50 (spawn above)

        // Place eagle near the player so it will steal on the first tick
        eagle.setX(100);
        eagle.setY(110);

        eagle.tick(engineState, gameState); // Steal food (sets attacking=false and speed=4)

        // Move player away so subsequent tick uses fleeing branch
        closePlayer.setX(500);
        closePlayer.setY(500);

        eagle.tick(engineState, gameState); // Flee toward spawn at y=50

        assertSame("Sprite should be 'up' when fleeing towards spawn above",
                SpriteGallery.eagle.getSprite("up"), eagle.getSprite());
    }

    @Test
    public void testSpriteDownWhenFleeingToSpawnBelow() {
        // Spawn at y=300; place eagle above spawn and set fleeing
        Eagle eagle = new Eagle(100, 300, player);
        eagle.setAttacking(false);
        eagle.setY(50); // current y < spawn y => should aim downwards

        eagle.tick(engineState, gameState);

        assertSame("Sprite should be 'down' when fleeing towards spawn below",
                SpriteGallery.eagle.getSprite("down"), eagle.getSprite());
    }

    /**
     * Test that eagle speed increases to 4 after stealing
     */
    @Test
    public void testSpeedIncreasesAfterStealing() {
        inventory.addFood(10);
        MockPlayer closePlayer = new MockPlayer(110, 110);
        gameState = new MockGameState(closePlayer, inventory);
        Eagle eagle = new Eagle(100, 100, closePlayer);

        assertEquals(2.0, eagle.getSpeed(), 0.01);

        eagle.tick(engineState, gameState);

        assertEquals(4.0, eagle.getSpeed(), 0.01);
    }

    /**
     * Test that eagle moves correctly over multiple ticks
     */
    @Test
    public void testMultipleTicksMovement() {
        MockPlayer targetPlayer = new MockPlayer(500, 500);
        gameState = new MockGameState(targetPlayer, inventory);
        Eagle eagle = new Eagle(100, 100, targetPlayer);

        int initialDistance = eagle.distanceFrom(500, 500);

        for (int i = 0; i < 10; i++) {
            eagle.tick(engineState, gameState);
        }

        int newDistance = eagle.distanceFrom(500, 500);

        assertTrue(newDistance < initialDistance);
    }

    /**
     * Test distance calculation
     */
    @Test
    public void testDistanceCalculation() {
        Eagle eagle = new Eagle(0, 0, player);

        int distance = eagle.distanceFrom(300, 400);

        assertEquals(500, distance);
    }

    // Mock Classes
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

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
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
            food += amount;
            if (food < 0) food = 0;
        }

        @Override
        public int getFood() {
            return food;
        }

        @Override
        public void addCoins(int amount) {
            coins += amount;
            if (coins < 0) coins = 0;
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
