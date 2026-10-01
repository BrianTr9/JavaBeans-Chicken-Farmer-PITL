package builder.entities.npc.spawners;

import builder.GameState;
import builder.entities.npc.enemies.Eagle;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.tiles.Tile;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.game.Entity;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the EagleSpawner class.
 * Tests cover:
 * - Construction and initialization
 * - Timer functionality
 * - Eagle spawning behavior
 * - Position management
 * - Player tracking
 */
public class EagleSpawnerTest {

    private EngineState engineState;
    private GameState gameState;
    private MockWorld world;
    private EnemyManager enemyManager;
    private MockPlayer player;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        player = new MockPlayer(400, 400);
        world = new MockWorld();
        enemyManager = new EnemyManager();
        gameState = new MockGameState(player, inventory, world, enemyManager);
    }

    @Test
    public void testConstructionSetsPosition() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 50);

        assertEquals(100, spawner.getX());
        assertEquals(200, spawner.getY());
    }

    @Test
    public void testConstructionInitializesTimer() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 50);

        assertNotNull("Timer should be initialized", spawner.getTimer());
        assertFalse("Timer should not be finished initially", spawner.getTimer().isFinished());
    }

    @Test
    public void testTimerDurationIsRespected() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 3);

        assertFalse(spawner.getTimer().isFinished());

        // Tick twice - timer should not be finished yet
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should not spawn before timer finishes", 0, enemyManager.getBirds().size());

        // Third tick - timer should finish and spawn
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn when timer finishes", 1, enemyManager.getBirds().size());
    }

    @Test
    public void testSpawnsEagleOnTimerFinish() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        spawner.tick(engineState, gameState);

        assertEquals("Should spawn one eagle", 1, enemyManager.getBirds().size());
        assertTrue("Spawned entity should be an Eagle", enemyManager.getBirds().get(0) instanceof Eagle);
    }

    @Test
    public void testSpawnedEagleTracksPlayer() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        spawner.tick(engineState, gameState);

        Eagle eagle = (Eagle) enemyManager.getBirds().get(0);
        assertSame("Eagle should track the player", player, eagle.getTrackedTarget());
    }

    @Test
    public void testSpawnsMultipleEaglesOverTime() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 2);

        // First spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn first eagle", 1, enemyManager.getBirds().size());

        // Second spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn second eagle", 2, enemyManager.getBirds().size());
    }

    @Test
    public void testSetXUpdatesPosition() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        spawner.setX(300);

        assertEquals(300, spawner.getX());
    }

    @Test
    public void testSetYUpdatesPosition() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        spawner.setY(400);

        assertEquals(400, spawner.getY());
    }

    @Test
    public void testAlwaysSpawnsRegardlessOfGameState() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        // Eagles always spawn when timer finishes (unlike pigeons which need cabbages)
        spawner.tick(engineState, gameState);

        assertEquals("Should spawn eagle even with empty world", 1, enemyManager.getBirds().size());
    }

    @Test
    public void testSpawnedEagleHasCorrectSpawnCoordinates() {
        EagleSpawner spawner = new EagleSpawner(150, 250, 1);

        spawner.tick(engineState, gameState);

        Eagle eagle = (Eagle) enemyManager.getBirds().get(0);
        assertEquals("Eagle should have spawn X coordinate", 150, eagle.getSpawnX());
        assertEquals("Eagle should have spawn Y coordinate", 250, eagle.getSpawnY());
    }

    @Test
    public void testConsecutiveSpawnsWithDifferentPositions() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 1);

        // First spawn
        spawner.tick(engineState, gameState);
        Eagle eagle1 = (Eagle) enemyManager.getBirds().get(0);

        // Change position and spawn again
        spawner.setX(300);
        spawner.setY(400);
        spawner.tick(engineState, gameState);
        Eagle eagle2 = (Eagle) enemyManager.getBirds().get(1);

        assertEquals("First eagle should have original spawn X", 100, eagle1.getSpawnX());
        assertEquals("First eagle should have original spawn Y", 200, eagle1.getSpawnY());
        assertEquals("Second eagle should have new spawn X", 300, eagle2.getSpawnX());
        assertEquals("Second eagle should have new spawn Y", 400, eagle2.getSpawnY());
    }

    @Test
    public void testTimerRepeatsAfterSpawn() {
        EagleSpawner spawner = new EagleSpawner(100, 200, 2);

        // First cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals(1, enemyManager.getBirds().size());

        // Timer should repeat
        spawner.tick(engineState, gameState);
        assertEquals("Should not spawn on first tick of new cycle", 1, enemyManager.getBirds().size());

        spawner.tick(engineState, gameState);
        assertEquals("Should spawn on second tick of new cycle", 2, enemyManager.getBirds().size());
    }

    @Test
    public void testGameStateProvidesNonNullInventory() {
        // Ensure inventory is available and functional (kills mutant returning null)
        assertNotNull("Inventory should be non-null", gameState.getInventory());
        gameState.getInventory().addCoins(2);
        gameState.getInventory().addFood(3);
        assertEquals(2, gameState.getInventory().getCoins());
        assertEquals(3, gameState.getInventory().getFood());
    }

    @Test
    public void testGameStateProvidesNonNullWorld() {
        // Ensure world is available (kills mutant returning null)
        assertNotNull("World should be non-null", gameState.getWorld());
        assertNotNull("allTiles should return a list", gameState.getWorld().allTiles());
        assertTrue("Default mock world should be empty", gameState.getWorld().allTiles().isEmpty());
    }

    @Test
    public void testMockPlayerCoordinates() {
        // Ensure player coordinate getters return the configured values (kills getX/getY mutants)
        assertEquals(400, player.getX());
        assertEquals(400, player.getY());
    }

    @Test
    public void testMockPlayerIdAndDamage() {
        // Ensure ID and damage are as expected (kills getID/getDamage mutants)
        assertEquals("mock-player", player.getID());
        assertEquals(1, player.getDamage());
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
        private final World world;
        private final EnemyManager enemyManager;

        public MockGameState(Player player, Inventory inventory, World world, EnemyManager enemyManager) {
            this.player = player;
            this.inventory = inventory;
            this.world = world;
            this.enemyManager = enemyManager;
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public builder.entities.npc.NpcManager getNpcs() {
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
        private final int x;
        private final int y;

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
        }

        @Override
        public void setY(int y) {
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

    private static class MockWorld implements World {
        private final List<Tile> tiles = new ArrayList<>();

        @Override
        public List<Tile> tileSelector(Predicate<Tile> predicate) {
            return new ArrayList<>();
        }

        @Override
        public List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions) {
            return new ArrayList<>();
        }

        @Override
        public List<Tile> allTiles() {
            return new ArrayList<>(tiles);
        }

        @Override
        public void place(Tile tile) {
        }
    }
}
