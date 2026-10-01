package builder.entities.npc.spawners;

import builder.GameState;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Magpie;
import builder.entities.tiles.Tile;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the MagpieSpawner class.
 * Tests cover:
 * - Construction and initialization
 * - Timer functionality
 * - Magpie spawning behavior
 * - Position management
 * - Player tracking
 */
public class MagpieSpawnerTest {

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
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 50);

        assertEquals(100, spawner.getX());
        assertEquals(200, spawner.getY());
    }

    @Test
    public void testConstructionInitializesTimer() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 50);

        assertNotNull("Timer should be initialized", spawner.getTimer());
        assertFalse("Timer should not be finished initially", spawner.getTimer().isFinished());
    }

    @Test
    public void testTimerDurationIsRespected() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 3);

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
    public void testSpawnsMagpieOnTimerFinish() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        spawner.tick(engineState, gameState);

        assertEquals("Should spawn one magpie", 1, enemyManager.getBirds().size());
        assertTrue("Spawned entity should be a Magpie", enemyManager.getBirds().get(0) instanceof Magpie);
    }

    @Test
    public void testSpawnedMagpieTracksPlayer() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        spawner.tick(engineState, gameState);

        Magpie magpie = (Magpie) enemyManager.getBirds().get(0);
        assertSame("Magpie should track the player", player, magpie.getTrackedTarget());
    }

    @Test
    public void testSpawnsMultipleMagpiesOverTime() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 2);

        // First spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn first magpie", 1, enemyManager.getBirds().size());

        // Second spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn second magpie", 2, enemyManager.getBirds().size());
    }

    @Test
    public void testSetXUpdatesPosition() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        spawner.setX(300);

        assertEquals(300, spawner.getX());
    }

    @Test
    public void testSetYUpdatesPosition() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        spawner.setY(400);

        assertEquals(400, spawner.getY());
    }

    @Test
    public void testAlwaysSpawnsRegardlessOfGameState() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        // Magpies always spawn when timer finishes (unlike pigeons which need cabbages)
        spawner.tick(engineState, gameState);

        assertEquals("Should spawn magpie even with empty world", 1, enemyManager.getBirds().size());
    }

    @Test
    public void testSpawnedMagpieHasCorrectSpawnCoordinates() {
        MagpieSpawner spawner = new MagpieSpawner(150, 250, 1);

        spawner.tick(engineState, gameState);

        Magpie magpie = (Magpie) enemyManager.getBirds().get(0);
        assertEquals("Magpie should have spawn X coordinate", 150, magpie.getSpawnX());
        assertEquals("Magpie should have spawn Y coordinate", 250, magpie.getSpawnY());
    }

    @Test
    public void testConsecutiveSpawnsWithDifferentPositions() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        // First spawn
        spawner.tick(engineState, gameState);
        Magpie magpie1 = (Magpie) enemyManager.getBirds().get(0);

        // Change position and spawn again
        spawner.setX(300);
        spawner.setY(400);
        spawner.tick(engineState, gameState);
        Magpie magpie2 = (Magpie) enemyManager.getBirds().get(1);

        assertEquals("First magpie should have original spawn X", 100, magpie1.getSpawnX());
        assertEquals("First magpie should have original spawn Y", 200, magpie1.getSpawnY());
        assertEquals("Second magpie should have new spawn X", 300, magpie2.getSpawnX());
        assertEquals("Second magpie should have new spawn Y", 400, magpie2.getSpawnY());
    }

    @Test
    public void testTimerRepeatsAfterSpawn() {
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 2);

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
    public void testSpawnerIndependentOfPlayerPosition() {
        // Test that spawner works regardless of player position
        MagpieSpawner spawner = new MagpieSpawner(100, 200, 1);

        // Move player far away
        MockPlayer farPlayer = new MockPlayer(1000, 1000);
        GameState farGameState = new MockGameState(farPlayer, new MockInventory(), world, enemyManager);

        spawner.tick(engineState, farGameState);

        assertEquals("Should spawn magpie regardless of player distance", 1, enemyManager.getBirds().size());
        Magpie magpie = (Magpie) enemyManager.getBirds().get(0);
        assertSame("Magpie should still track the far player", farPlayer, magpie.getTrackedTarget());
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

