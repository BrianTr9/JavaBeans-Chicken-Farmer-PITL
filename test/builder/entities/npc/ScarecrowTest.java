package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Magpie;
import builder.entities.npc.enemies.Pigeon;
import builder.entities.npc.enemies.Eagle;
import builder.inventory.Inventory;
import builder.player.Player;
import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the Scarecrow class.
 * Tests cover:
 * - Construction and initialization
 * - Scaring Magpies within 4 tiles (320 pixels)
 * - Scaring Pigeons within 4 tiles (320 pixels)
 * - Not affecting Eagles
 * - Not affecting birds outside 4 tile radius
 * - Stopping bird attacks (setting attacking to false)
 * - Multiple birds being scared simultaneously
 * - Static behavior (speed = 0)
 * - Cost constant
 */
public class ScarecrowTest {

    private EngineState engineState;
    private GameState gameState;
    private Player player;
    private EnemyManager enemyManager;
    private Dimensions dimensions;

    @Before
    public void setUp() {
        dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        player = new MockPlayer(400, 400);
        enemyManager = new EnemyManager(dimensions);
        gameState = new MockGameState(player, inventory, enemyManager);
    }

    /**
     * Test that Scarecrow is constructed with correct initial values
     */
    @Test
    public void testConstruction() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        assertEquals(100, scarecrow.getX());
        assertEquals(100, scarecrow.getY());
        assertEquals(0.0, scarecrow.getSpeed(), 0.01); // Scarecrow doesn't move
        assertNotNull(scarecrow.getSprite());
        assertFalse(scarecrow.isMarkedForRemoval());
    }

    /**
     * Test that Scarecrow has correct coin cost constant
     */
    @Test
    public void testCoinCost() {
        assertEquals(2, Scarecrow.COIN_COST);
    }

    /**
     * Test that Scarecrow scares Magpie within 4 tiles
     */
    @Test
    public void testScaresMagpieWithin4Tiles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Place magpie within 4 tiles (4 * 80 = 320 pixels)
        Magpie magpie = new Magpie(200, 200, player); // Distance ~141 pixels
        magpie.attacking = true;
        enemyManager.Birds.add(magpie);

        assertTrue(magpie.attacking);

        scarecrow.interact(engineState, gameState);

        assertFalse(magpie.attacking); // Should be scared
    }

    /**
     * Test that Scarecrow scares Pigeon within 4 tiles
     */
    @Test
    public void testScaresPigeonWithin4Tiles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Place pigeon within 4 tiles
        Pigeon pigeon = new Pigeon(250, 250); // Distance ~212 pixels
        pigeon.attacking = true;
        enemyManager.Birds.add(pigeon);

        assertTrue(pigeon.attacking);

        scarecrow.interact(engineState, gameState);

        assertFalse(pigeon.attacking); // Should be scared
    }

    /**
     * Test that Scarecrow scares multiple Magpies simultaneously
     */
    @Test
    public void testScaresMultipleMagpies() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        Magpie magpie1 = new Magpie(150, 150, player);
        Magpie magpie2 = new Magpie(200, 100, player);
        Magpie magpie3 = new Magpie(100, 200, player);

        magpie1.attacking = true;
        magpie2.attacking = true;
        magpie3.attacking = true;

        enemyManager.Birds.add(magpie1);
        enemyManager.Birds.add(magpie2);
        enemyManager.Birds.add(magpie3);

        scarecrow.interact(engineState, gameState);

        assertFalse(magpie1.attacking);
        assertFalse(magpie2.attacking);
        assertFalse(magpie3.attacking);
    }

    /**
     * Test that Scarecrow scares multiple Pigeons simultaneously
     */
    @Test
    public void testScaresMultiplePigeons() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        Pigeon pigeon1 = new Pigeon(150, 150);
        Pigeon pigeon2 = new Pigeon(200, 100);

        pigeon1.attacking = true;
        pigeon2.attacking = true;

        enemyManager.Birds.add(pigeon1);
        enemyManager.Birds.add(pigeon2);

        scarecrow.interact(engineState, gameState);

        assertFalse(pigeon1.attacking);
        assertFalse(pigeon2.attacking);
    }

    /**
     * Test that Scarecrow scares both Magpies and Pigeons
     */
    @Test
    public void testScaresBothMagpiesAndPigeons() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        Magpie magpie = new Magpie(150, 150, player);
        Pigeon pigeon = new Pigeon(200, 100);

        magpie.attacking = true;
        pigeon.attacking = true;

        enemyManager.Birds.add(magpie);
        enemyManager.Birds.add(pigeon);

        scarecrow.interact(engineState, gameState);

        assertFalse(magpie.attacking);
        assertFalse(pigeon.attacking);
    }

    /**
     * Test that Scarecrow does NOT scare Magpie outside 4 tiles
     */
    @Test
    public void testDoesNotScareMagpieOutside4Tiles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Place magpie outside 4 tiles (>320 pixels)
        Magpie magpie = new Magpie(500, 500, player); // Distance ~565 pixels
        magpie.attacking = true;
        enemyManager.Birds.add(magpie);

        scarecrow.interact(engineState, gameState);

        assertTrue(magpie.attacking); // Should still be attacking
    }

    /**
     * Test that Scarecrow does NOT scare Pigeon outside 4 tiles
     */
    @Test
    public void testDoesNotScarePigeonOutside4Tiles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Place pigeon outside 4 tiles
        Pigeon pigeon = new Pigeon(600, 600); // Distance ~707 pixels
        pigeon.attacking = true;
        enemyManager.Birds.add(pigeon);

        scarecrow.interact(engineState, gameState);

        assertTrue(pigeon.attacking); // Should still be attacking
    }

    /**
     * Test that Scarecrow does NOT affect Eagles
     */
    @Test
    public void testDoesNotAffectEagles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Place eagle within 4 tiles
        Eagle eagle = new Eagle(150, 150, player);
        enemyManager.Birds.add(eagle);

        scarecrow.interact(engineState, gameState);

        // Eagle behavior should not be affected
        // Eagles don't have an attacking field accessible, but they shouldn't be affected
        assertFalse(eagle.isMarkedForRemoval());
    }

    /**
     * Test that Scarecrow at exactly 4 tiles distance scares birds
     */
    @Test
    public void testScaresAtExactly4Tiles() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Calculate position at exactly 4 tiles (320 pixels) away
        // Using Pythagorean theorem: if we go 240 pixels right and 210 pixels down
        // sqrt(240^2 + 210^2) ≈ 319 pixels (just under 4 tiles)
        Magpie magpie = new Magpie(340, 310, player);
        magpie.attacking = true;
        enemyManager.Birds.add(magpie);

        scarecrow.interact(engineState, gameState);

        assertFalse(magpie.attacking);
    }

    /**
     * Test that Scarecrow doesn't move (speed = 0)
     */
    @Test
    public void testScarecrowDoesNotMove() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        int initialX = scarecrow.getX();
        int initialY = scarecrow.getY();

        // Tick multiple times
        for (int i = 0; i < 10; i++) {
            scarecrow.tick(engineState);
        }

        assertEquals(initialX, scarecrow.getX());
        assertEquals(initialY, scarecrow.getY());
    }

    /**
     * Test that Scarecrow persists (not marked for removal)
     */
    @Test
    public void testScarecrowPersists() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        for (int i = 0; i < 100; i++) {
            scarecrow.tick(engineState);
        }

        assertFalse(scarecrow.isMarkedForRemoval());
    }

    /**
     * Test that Scarecrow works with mixed bird types and distances
     */
    @Test
    public void testMixedBirdsAndDistances() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        // Within range
        Magpie magpie1 = new Magpie(150, 150, player);
        Pigeon pigeon1 = new Pigeon(200, 100);

        // Outside range
        Magpie magpie2 = new Magpie(500, 500, player);
        Pigeon pigeon2 = new Pigeon(600, 600);

        // Eagle (should not be affected)
        Eagle eagle = new Eagle(150, 150, player);

        magpie1.attacking = true;
        pigeon1.attacking = true;
        magpie2.attacking = true;
        pigeon2.attacking = true;

        enemyManager.Birds.add(magpie1);
        enemyManager.Birds.add(pigeon1);
        enemyManager.Birds.add(magpie2);
        enemyManager.Birds.add(pigeon2);
        enemyManager.Birds.add(eagle);

        scarecrow.interact(engineState, gameState);

        // Within range should be scared
        assertFalse(magpie1.attacking);
        assertFalse(pigeon1.attacking);

        // Outside range should still be attacking
        assertTrue(magpie2.attacking);
        assertTrue(pigeon2.attacking);
    }

    /**
     * Test that Scarecrow can scare already fleeing birds
     */
    @Test
    public void testScaresAlreadyFleeingBirds() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        Magpie magpie = new Magpie(150, 150, player);
        magpie.attacking = false; // Already fleeing
        enemyManager.Birds.add(magpie);

        scarecrow.interact(engineState, gameState);

        assertFalse(magpie.attacking); // Should remain not attacking
    }

    /**
     * Test distance calculation for 4 tiles radius
     */
    @Test
    public void testFourTileRadius() {
        // 4 tiles * 80 pixels/tile = 320 pixels
        int scareRadius = dimensions.tileSize() * 4;

        assertEquals(320, scareRadius);
    }

    /**
     * Test that multiple scarecrows can scare the same bird
     */
    @Test
    public void testMultipleScarecrowsScareSameBird() {
        Scarecrow scarecrow1 = new Scarecrow(100, 100);
        Scarecrow scarecrow2 = new Scarecrow(300, 100);

        Magpie magpie = new Magpie(200, 100, player);
        magpie.attacking = true;
        enemyManager.Birds.add(magpie);

        scarecrow1.interact(engineState, gameState);

        assertFalse(magpie.attacking);

        // Second scarecrow should also work (even though bird already scared)
        scarecrow2.interact(engineState, gameState);

        assertFalse(magpie.attacking);
    }

    /**
     * Test sprite is set correctly
     */
    @Test
    public void testSpriteIsSet() {
        Scarecrow scarecrow = new Scarecrow(100, 100);

        assertNotNull(scarecrow.getSprite());
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
}

