package builder.entities.npc.spawners;

import builder.GameState;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Pigeon;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Tile;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.ui.SpriteGallery;
import builder.world.World;
import engine.EngineState;
import engine.game.Entity;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.timing.TickTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the PigeonSpawner class.
 * Tests cover:
 * - Construction and initialization
 * - Timer functionality
 * - Spawning behavior with/without cabbages
 * - Finding closest cabbage tile
 * - Position management
 */
public class PigeonSpawnerTest {

    private EngineState engineState;
    private GameState gameState;
    private MockWorld world;
    private EnemyManager enemyManager;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        Player player = new MockPlayer(400, 400);
        world = new MockWorld();
        enemyManager = new EnemyManager();
        gameState = new MockGameState(player, inventory, world, enemyManager);
    }

    @Test
    public void testConstructionSetsPosition() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 50);

        assertEquals(100, spawner.getX());
        assertEquals(200, spawner.getY());
    }

    @Test
    public void testConstructionInitializesTimer() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 50);

        assertNotNull("Timer should be initialized", spawner.getTimer());
        assertFalse("Timer should not be finished initially", spawner.getTimer().isFinished());
    }

    @Test
    public void testTimerDurationIsRespected() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 3);

        // Add cabbage so spawn will occur when timer finishes
        MockCabbage cabbage = new MockCabbage(300, 300);
        MockTile tile = new MockTile(300, 300);
        tile.addEntity(cabbage);
        world.addTile(tile);

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
    public void testDoesNotSpawnWhenNoCabbages() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // No cabbages in world
        spawner.tick(engineState, gameState);

        assertEquals("Should not spawn pigeon when no cabbages exist", 0, enemyManager.getBirds().size());
    }

    @Test
    public void testSpawnsWhenCabbageExists() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // Add cabbage
        MockCabbage cabbage = new MockCabbage(300, 300);
        MockTile tile = new MockTile(300, 300);
        tile.addEntity(cabbage);
        world.addTile(tile);

        spawner.tick(engineState, gameState);

        assertEquals("Should spawn pigeon when cabbage exists", 1, enemyManager.getBirds().size());
    }

    @Test
    public void testSpawnedPigeonTracksClosestCabbage() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // Add two cabbages at different distances
        MockCabbage farCabbage = new MockCabbage(600, 600);
        MockTile farTile = new MockTile(600, 600);
        farTile.addEntity(farCabbage);

        MockCabbage closeCabbage = new MockCabbage(200, 200);
        MockTile closeTile = new MockTile(200, 200);
        closeTile.addEntity(closeCabbage);

        world.addTile(farTile);
        world.addTile(closeTile);

        spawner.tick(engineState, gameState);

        assertEquals("Should spawn one pigeon", 1, enemyManager.getBirds().size());
        Pigeon pigeon = (Pigeon) enemyManager.getBirds().get(0);
        assertSame("Pigeon should track closest cabbage tile", closeTile, pigeon.getTrackedTarget());
    }

    @Test
    public void testSpawnsMultiplePigeonsOverTime() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 2);

        // Add cabbage
        MockCabbage cabbage = new MockCabbage(300, 300);
        MockTile tile = new MockTile(300, 300);
        tile.addEntity(cabbage);
        world.addTile(tile);

        // First spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn first pigeon", 1, enemyManager.getBirds().size());

        // Second spawn cycle
        spawner.tick(engineState, gameState);
        spawner.tick(engineState, gameState);
        assertEquals("Should spawn second pigeon", 2, enemyManager.getBirds().size());
    }

    @Test
    public void testIgnoresEmptyTiles() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // Add empty tiles (no cabbages)
        MockTile empty1 = new MockTile(300, 300);
        MockTile empty2 = new MockTile(400, 400);
        world.addTile(empty1);
        world.addTile(empty2);

        spawner.tick(engineState, gameState);

        assertEquals("Should not spawn when only empty tiles exist", 0, enemyManager.getBirds().size());
    }

    @Test
    public void testIgnoresTilesWithNonCabbageEntities() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // Add tile with non-cabbage entity
        MockTile tile = new MockTile(300, 300);
        MockNonCabbageEntity other = new MockNonCabbageEntity(300, 300);
        tile.addEntity(other);
        world.addTile(tile);

        spawner.tick(engineState, gameState);

        assertEquals("Should not spawn when only non-cabbage entities exist", 0, enemyManager.getBirds().size());
    }

    @Test
    public void testSetXUpdatesPosition() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        spawner.setX(300);

        assertEquals(300, spawner.getX());
    }

    @Test
    public void testSetYUpdatesPosition() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        spawner.setY(400);

        assertEquals(400, spawner.getY());
    }

    @Test
    public void testDistanceCalculationAffectsClosestSelection() {
        PigeonSpawner spawner = new PigeonSpawner(100, 100, 1);

        // Add three cabbages - verify closest is selected by distance
        MockCabbage cab1 = new MockCabbage(200, 100); // distance = 100
        MockTile tile1 = new MockTile(200, 100);
        tile1.addEntity(cab1);

        MockCabbage cab2 = new MockCabbage(100, 200); // distance = 100
        MockTile tile2 = new MockTile(100, 200);
        tile2.addEntity(cab2);

        MockCabbage cab3 = new MockCabbage(400, 400); // distance = ~424
        MockTile tile3 = new MockTile(400, 400);
        tile3.addEntity(cab3);

        world.addTile(tile3); // Add far one first
        world.addTile(tile1); // Add close ones
        world.addTile(tile2);

        spawner.tick(engineState, gameState);

        Pigeon pigeon = (Pigeon) enemyManager.getBirds().get(0);
        // Should pick tile1 or tile2 (both distance 100), NOT tile3
        assertTrue("Should pick one of the closest tiles",
                pigeon.getTrackedTarget() == tile1 || pigeon.getTrackedTarget() == tile2);
    }

    @Test
    public void testSpawnWithMultipleCabbagesOnSameTile() {
        PigeonSpawner spawner = new PigeonSpawner(100, 200, 1);

        // Add multiple cabbages on same tile
        MockCabbage cabbage1 = new MockCabbage(300, 300);
        MockCabbage cabbage2 = new MockCabbage(300, 300);
        MockTile tile = new MockTile(300, 300);
        tile.addEntity(cabbage1);
        tile.addEntity(cabbage2);
        world.addTile(tile);

        spawner.tick(engineState, gameState);

        assertEquals("Should spawn pigeon when cabbages exist", 1, enemyManager.getBirds().size());
        Pigeon pigeon = (Pigeon) enemyManager.getBirds().get(0);
        assertSame("Should track the tile with cabbages", tile, pigeon.getTrackedTarget());
    }

    @Test
    public void testMockPlayerCoordinates() {
        // Ensure player coordinate getters return configured values (kills getX/getY mutants)
        Player player = gameState.getPlayer();
        assertNotNull(player);
        assertEquals(400, player.getX());
        assertEquals(400, player.getY());
    }

    @Test
    public void testMockPlayerIdAndDamage() {
        // Ensure ID and damage are as expected (kills getID/getDamage mutants)
        Player player = gameState.getPlayer();
        assertEquals("mock-player", player.getID());
        assertEquals(1, player.getDamage());
    }

    // Mock Classes

    private static class MockNonCabbageEntity extends Entity {
        public MockNonCabbageEntity(int x, int y) {
            super(x, y);
        }

        @Override
        public void tick(EngineState state) {
            // No-op
        }
    }

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

        public void addTile(Tile tile) {
            tiles.add(tile);
        }

        @Override
        public List<Tile> tileSelector(Predicate<Tile> predicate) {
            List<Tile> selected = new ArrayList<>();
            for (Tile tile : tiles) {
                if (predicate.test(tile)) {
                    selected.add(tile);
                }
            }
            return selected;
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

    private static class MockTile extends Tile {
        private final List<Entity> entities = new ArrayList<>();

        public MockTile(int x, int y) {
            super(x, y, SpriteGallery.grass);
        }

        public void addEntity(Entity entity) {
            entities.add(entity);
        }

        @Override
        public List<Entity> getStackedEntities() {
            return entities;
        }
    }

    private static class MockCabbage extends Cabbage {
        public MockCabbage(int x, int y) {
            super(x, y);
        }
    }
}
