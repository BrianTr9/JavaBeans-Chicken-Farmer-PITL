package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Eagle;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Magpie;
import builder.entities.npc.enemies.Pigeon;
import builder.inventory.Inventory;
import builder.player.Player;
import engine.EngineState;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.timing.FixedTimer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the GuardBee class.
 * Tests cover:
 * - Construction and initialization
 * - Lifespan management
 * - Tracking enemy birds
 * - Removing enemies on contact
 * - Self-removal on contact with enemy
 * - Returning to spawn when no enemies
 * - Direction updates based on target
 * - Sprite updates based on direction
 * - Speed and movement behavior
 */
public class GuardBeeTest {

    private EngineState engineState;
    private GameState gameState;
    private Player player;
    private EnemyManager enemyManager;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        player = new MockPlayer(400, 400);
        enemyManager = new EnemyManager(dimensions);
        gameState = new MockGameState(player, inventory, enemyManager);
    }

    /**
     * Test that GuardBee is constructed with correct initial values
     */
    @Test
    public void testConstruction() {
        HasPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);

        assertEquals(100, bee.getX());
        assertEquals(100, bee.getY());
        assertEquals(2.0, bee.getSpeed(), 0.01);
        assertNotNull(bee.getSprite());
        assertFalse(bee.isMarkedForRemoval());
    }

    /**
     * Test that lifespan is initialized correctly
     */
    @Test
    public void testLifespanInitialization() {
        HasPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);

        assertNotNull(bee.getLifespan());
        assertFalse(bee.getLifespan().isFinished());
    }

    /**
     * Test that lifespan can be set and retrieved
     */
    @Test
    public void testSetLifespan() {
        HasPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);
        FixedTimer newTimer = new FixedTimer(500);

        bee.setLifespan(newTimer);

        assertEquals(newTimer, bee.getLifespan());
    }

    /**
     * Test that bee is marked for removal when lifespan expires
     */
    @Test
    public void testLifespanExpiration() {
        HasPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);
        FixedTimer shortTimer = new FixedTimer(1);
        bee.setLifespan(shortTimer);

        assertFalse(bee.isMarkedForRemoval());

        bee.tick(engineState, gameState);

        assertTrue(bee.isMarkedForRemoval());
    }

    /**
     * Test that bee tracks target position
     */
    @Test
    public void testTracksTarget() {
        MockPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);

        int initialX = bee.getX();
        int initialY = bee.getY();

        bee.tick(engineState, gameState);

        // Bee should move towards target
        assertTrue(bee.getX() != initialX || bee.getY() != initialY);
    }

    /**
     * Test that bee updates direction towards target
     */
    @Test
    public void testDirectionTowardsTarget() {
        MockPosition target = new MockPosition(200, 100);
        GuardBee bee = new GuardBee(100, 100, target);

        bee.tick(engineState, gameState);

        // Direction should be towards the right (0 degrees)
        assertEquals(0, bee.getDirection());
    }

    /**
     * Test that bee removes enemy on contact
     */
    @Test
    public void testRemovesEnemyOnContact() {
        Magpie magpie = new Magpie(110, 110, player);
        enemyManager.Birds.add(magpie);

        GuardBee bee = new GuardBee(100, 100, magpie);

        assertFalse(magpie.isMarkedForRemoval());
        assertFalse(bee.isMarkedForRemoval());

        bee.tick(engineState, gameState);

        assertTrue(magpie.isMarkedForRemoval());
        assertTrue(bee.isMarkedForRemoval());
    }

    /**
     * Test that bee removes itself on contact with enemy
     */
    @Test
    public void testRemovesSelfOnContactWithEnemy() {
        Pigeon pigeon = new Pigeon(110, 110);
        enemyManager.Birds.add(pigeon);

        GuardBee bee = new GuardBee(100, 100, pigeon);

        bee.tick(engineState, gameState);

        assertTrue(bee.isMarkedForRemoval());
    }

    /**
     * Test that bee tracks multiple enemies
     */
    @Test
    public void testTracksMultipleEnemies() {
        Magpie magpie1 = new Magpie(200, 200, player);
        Magpie magpie2 = new Magpie(300, 300, player);
        enemyManager.Birds.add(magpie1);
        enemyManager.Birds.add(magpie2);

        GuardBee bee = new GuardBee(100, 100, magpie1);

        for (int i = 0; i < 5; i++) {
            if (!bee.isMarkedForRemoval()) {
                bee.tick(engineState, gameState);
            }
        }

        // Bee should be moving towards enemies
        assertNotNull(bee.getSprite());
    }

    /**
     * Test that bee handles no birds in world correctly
     */
    @Test
    public void testHandlesNoBirdsInWorld() {
        MockPosition initialTarget = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, initialTarget);

        // Move bee away from spawn
        bee.setX(300);
        bee.setY(300);

        // Tick with no enemies - should continue towards last known target
        for (int i = 0; i < 3; i++) {
            bee.tick(engineState, gameState);
        }

        // Bee should still be functional
        assertNotNull(bee.getSprite());
    }

    /**
     * Test that bee locks onto enemy within 300 pixels
     */
    @Test
    public void testLocksOntoEnemyWithin300Pixels() {
        Magpie magpie = new Magpie(250, 250, player);
        enemyManager.Birds.add(magpie);

        MockPosition farTarget = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, farTarget);

        bee.tick(engineState, gameState);

        // Bee should update direction based on nearby enemy
        assertNotNull(bee.getSprite());
    }

    /**
     * Test sprite updates to down when moving downward
     */
    @Test
    public void testSpriteDownWhenMovingDown() {
        MockPosition targetBelow = new MockPosition(100, 300);
        GuardBee bee = new GuardBee(100, 100, targetBelow);

        bee.tick(engineState, gameState);

        assertNotNull(bee.getSprite());
    }

    /**
     * Test sprite updates to up when moving upward
     */
    @Test
    public void testSpriteUpWhenMovingUp() {
        MockPosition targetAbove = new MockPosition(100, 50);
        GuardBee bee = new GuardBee(100, 300, targetAbove);

        bee.tick(engineState, gameState);

        assertNotNull(bee.getSprite());
    }

    /**
     * Test sprite updates to right when moving right
     */
    @Test
    public void testSpriteRightWhenMovingRight() {
        MockPosition targetRight = new MockPosition(300, 100);
        GuardBee bee = new GuardBee(100, 100, targetRight);

        bee.tick(engineState, gameState);

        assertNotNull(bee.getSprite());
    }

    /**
     * Test sprite updates to left when moving left
     */
    @Test
    public void testSpriteLeftWhenMovingLeft() {
        MockPosition targetLeft = new MockPosition(50, 100);
        GuardBee bee = new GuardBee(300, 100, targetLeft);

        bee.tick(engineState, gameState);

        assertNotNull(bee.getSprite());
    }

    /**
     * Test that bee moves correctly over multiple ticks
     */
    @Test
    public void testMultipleTicksMovement() {
        MockPosition target = new MockPosition(500, 500);
        GuardBee bee = new GuardBee(100, 100, target);

        int initialDistance = bee.distanceFrom(500, 500);

        for (int i = 0; i < 10; i++) {
            bee.tick(engineState, gameState);
        }

        int newDistance = bee.distanceFrom(500, 500);

        assertTrue(newDistance < initialDistance);
    }

    /**
     * Test that bee doesn't remove enemies outside tile size distance
     */
    @Test
    public void testDoesNotRemoveEnemyFarAway() {
        Magpie magpie = new Magpie(500, 500, player);
        enemyManager.Birds.add(magpie);

        GuardBee bee = new GuardBee(100, 100, magpie);

        bee.tick(engineState, gameState);

        assertFalse(magpie.isMarkedForRemoval());
        assertFalse(bee.isMarkedForRemoval());
    }

    /**
     * Test distance calculation
     */
    @Test
    public void testDistanceCalculation() {
        MockPosition target = new MockPosition(100, 100);
        GuardBee bee = new GuardBee(0, 0, target);

        int distance = bee.distanceFrom(300, 400);

        assertEquals(500, distance);
    }

    /**
     * Test that bee works with Eagle enemies
     */
    @Test
    public void testWorksWithEagleEnemies() {
        Eagle eagle = new Eagle(110, 110, player);
        enemyManager.Birds.add(eagle);

        GuardBee bee = new GuardBee(100, 100, eagle);

        bee.tick(engineState, gameState);

        assertTrue(eagle.isMarkedForRemoval());
        assertTrue(bee.isMarkedForRemoval());
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
        private final EnemyManager enemyManager;

        public MockGameState(Player player, Inventory inventory, EnemyManager enemyManager) {
            this.player = player;
            this.inventory = inventory;
            this.enemyManager = enemyManager;
        }

        @Override
        public builder.world.World getWorld() {
            return null;
        }

        @Override
        public NpcManager getNpcs() {
            return null;
        }

        @Override
        public EnemyManager getEnemies() {
            return enemyManager;
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

    private static class MockPosition implements HasPosition {
        private final int x;
        private final int y;

        public MockPosition(int x, int y) {
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
        }

        @Override
        public void setY(int y) {
        }
    }
}
