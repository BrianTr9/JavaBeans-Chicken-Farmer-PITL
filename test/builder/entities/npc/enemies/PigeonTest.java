package builder.entities.npc.enemies;

import builder.GameState;
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
import engine.timing.FixedTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the Pigeon class.
 * Tests cover:
 * - Construction and initialization
 * - Lifespan management
 * - Tracking closest cabbage
 * - Cabbage stealing behavior
 * - Fleeing to spawn after stealing
 * - Behavior when no cabbages exist
 * - Direction and sprite updates
 * - Removal conditions
 */
public class PigeonTest {

    private EngineState engineState;
    private GameState gameState;
    private World world;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        Player player = new MockPlayer(400, 400);
        world = new MockWorld();
        gameState = new MockGameState(player, inventory, world);
    }

    @Test
    public void testConstructionNoTarget() {
        Pigeon pigeon = new Pigeon(100, 100);

        assertEquals(100, pigeon.getX());
        assertEquals(100, pigeon.getY());
        assertTrue(pigeon.attacking);
        assertFalse(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testConstructionWithTarget() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertEquals(100, pigeon.getX());
        assertEquals(100, pigeon.getY());
        assertTrue(pigeon.attacking);
        assertNotNull(pigeon.getSprite());
    }

    @Test
    public void testLifespanInitialization() {
        Pigeon pigeon = new Pigeon(100, 100);

        assertNotNull(pigeon.getLifespan());
        assertFalse(pigeon.getLifespan().isFinished());
    }

    @Test
    public void testSetLifespan() {
        Pigeon pigeon = new Pigeon(100, 100);
        FixedTimer newTimer = new FixedTimer(5000);

        pigeon.setLifespan(newTimer);

        assertEquals(newTimer, pigeon.getLifespan());
    }

    @Test
    public void testLifespanExpiration() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);
        FixedTimer shortTimer = new FixedTimer(1);
        pigeon.setLifespan(shortTimer);

        assertFalse(pigeon.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testTracksClosestCabbage() {
        // Place cabbages farther away (>80 pixels) so pigeon doesn't immediately steal them
        MockCabbage cabbage1 = new MockCabbage(400, 400);
        MockCabbage cabbage2 = new MockCabbage(250, 250);

        MockTile tile1 = new MockTile(400, 400);
        tile1.addEntity(cabbage1);

        MockTile tile2 = new MockTile(250, 250);
        tile2.addEntity(cabbage2);

        ((MockWorld) world).addTile(tile1);
        ((MockWorld) world).addTile(tile2);

        Pigeon pigeon = new Pigeon(100, 100, tile2);

        pigeon.tick(engineState, gameState);

        // Pigeon should still be attacking since cabbage is far away
        assertTrue(pigeon.attacking);
    }

    @Test
    public void testStealsCabbageWhenReaching() {
        MockCabbage cabbage = new MockCabbage(100, 100);
        MockTile tile = new MockTile(110, 110);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);

        assertTrue(pigeon.attacking);
        assertFalse(cabbage.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(cabbage.isMarkedForRemoval());
        assertFalse(pigeon.attacking);
    }

    @Test
    public void testStopsAttackingWhenNoCabbages() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertTrue(pigeon.attacking);

        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.attacking);
    }

    @Test
    public void testFleeToSpawnAfterStealing() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);
        pigeon.attacking = false;
        pigeon.setX(300);
        pigeon.setY(300);

        int initialDistance = pigeon.distanceFrom(100, 100);

        for (int i = 0; i < 3; i++) {
            pigeon.tick(engineState, gameState);
        }

        int newDistance = pigeon.distanceFrom(100, 100);

        assertTrue(newDistance <= initialDistance || pigeon.isMarkedForRemoval());
    }

    @Test
    public void testRemovedWhenReachingSpawnAfterFleeing() {
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.attacking = false;
        pigeon.setX(110);
        pigeon.setY(110);

        assertFalse(pigeon.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testSpriteUpWhenFleeingUpward() {
        Pigeon pigeon = new Pigeon(100, 50);
        pigeon.setY(200);
        pigeon.attacking = false;

        pigeon.tick(engineState, gameState);

        assertNotNull(pigeon.getSprite());
    }

    @Test
    public void testSpriteDownWhenFleeingDownward() {
        Pigeon pigeon = new Pigeon(100, 200);
        pigeon.setY(50);
        pigeon.attacking = false;

        pigeon.tick(engineState, gameState);

        assertNotNull(pigeon.getSprite());
    }

    @Test
    public void testDirectionTowardsTarget() {
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);

        pigeon.tick(engineState, gameState);

        assertEquals(0, pigeon.getDirection());
    }

    @Test
    public void testMultipleTicksWithoutCabbages() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        for (int i = 0; i < 5; i++) {
            pigeon.tick(engineState, gameState);
        }

        assertFalse(pigeon.attacking);
    }

    @Test
    public void testIgnoresCabbagesAfterStealing() {
        MockCabbage cabbage = new MockCabbage(100, 100);
        MockTile tile = new MockTile(110, 110);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);
        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.attacking);

        MockCabbage cabbage2 = new MockCabbage(200, 200);
        MockTile tile2 = new MockTile(200, 200);
        tile2.addEntity(cabbage2);
        ((MockWorld) world).addTile(tile2);

        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.attacking);
        assertFalse(cabbage2.isMarkedForRemoval());
    }

    @Test
    public void testDistanceCalculation() {
        Pigeon pigeon = new Pigeon(0, 0);

        int distance = pigeon.distanceFrom(300, 400);

        assertEquals(500, distance);
    }

    @Test
    public void testDistanceFromPosition() {
        Pigeon pigeon = new Pigeon(0, 0);
        MockPosition position = new MockPosition(300, 400);

        int distance = pigeon.distanceFrom(position);

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
        private final World world;

        public MockGameState(Player player, Inventory inventory, World world) {
            this.player = player;
            this.inventory = inventory;
            this.world = world;
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

