package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.npc.spawners.Spawner;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.renderer.Renderable;
import engine.timing.TickTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EnemyManagerTest {

    private Dimensions dims;
    private EngineState engine;
    private GameState game;

    @Before
    public void setup() {
        // 10 tiles, 800 window => tileSize = 80
        dims = new TileGrid(10, 800);
        engine = new MockEngineState(dims);
        game = new MockGameState();
    }

    @Test
    public void testInitialStateHasNoSpawnersOrBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        assertTrue(mgr.getSpawners().isEmpty());
        assertTrue(mgr.getBirds().isEmpty());
    }

    @Test
    public void testAddSpawnerAndTickCallsSpawner() {
        EnemyManager mgr = new EnemyManager(dims);
        CountingSpawner spawner = new CountingSpawner();
        mgr.add(spawner);
        assertEquals(0, spawner.ticks);
        mgr.tick(engine, game);
        assertEquals(1, spawner.ticks);
    }

    @Test
    public void testAddBirdAndCleanupRemovesMarkedBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        Enemy a = new Enemy(0,0);
        Enemy b = new Enemy(0,0);
        Enemy c = new Enemy(0,0);
        mgr.addBird(a);
        mgr.addBird(b);
        mgr.addBird(c);
        a.markForRemoval();
        c.markForRemoval();
        mgr.cleanup();
        List<Enemy> birds = mgr.getBirds();
        assertEquals(1, birds.size());
        assertSame(b, birds.get(0));
    }

    @Test
    public void testMkMAddsMagpieAtSpawnAndToBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        mgr.setSpawnX(123);
        mgr.setSpawnY(456);
        Player p = new MockPlayer(300, 300);
        Magpie m = mgr.mkM(p);
        assertNotNull(m);
        assertEquals(123, m.getX());
        assertEquals(456, m.getY());
        assertTrue(mgr.getBirds().contains(m));
        assertEquals(1, mgr.getMagpies().size());
        assertSame(m, mgr.getMagpies().get(0));
    }

    @Test
    public void testMkPAddsPigeonAtSpawnAndToBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        mgr.setSpawnX(10);
        mgr.setSpawnY(20);
        HasPosition target = new MockPos(500, 500);
        Pigeon p = mgr.mkP(target);
        assertNotNull(p);
        assertEquals(10, p.getX());
        assertEquals(20, p.getY());
        assertTrue(mgr.getBirds().contains(p));
    }

    @Test
    public void testMkEDoesNotAutoAddToBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        mgr.setSpawnX(7);
        mgr.setSpawnY(9);
        Player player = new MockPlayer(0,0);
        Eagle e = mgr.mkE(player);
        assertNotNull(e);
        assertEquals(7, e.getX());
        assertEquals(9, e.getY());
        assertFalse("Eagle should not be auto-added to birds", mgr.getBirds().contains(e));
    }

    @Test
    public void testTickDispatchesToMagpieEaglePigeonButNotGenericEnemy() {
        EnemyManager mgr = new EnemyManager(dims);
        // Create spies that flip a flag on tick
        SpyMagpie magpie = new SpyMagpie(0, 0, new MockPos(1,1));
        SpyPigeon pigeon = new SpyPigeon(0, 0, new MockPos(2,2));
        SpyEagle eagle = new SpyEagle(0, 0, new MockPlayer(3,3));
        SpyEnemy generic = new SpyEnemy(0, 0);

        mgr.addBird(magpie);
        mgr.addBird(pigeon);
        mgr.addBird(eagle);
        mgr.addBird(generic);

        mgr.tick(engine, game);

        assertTrue("Magpie should have been ticked", magpie.called);
        assertTrue("Pigeon should have been ticked", pigeon.called);
        assertTrue("Eagle should have been ticked", eagle.called);
        assertFalse("Generic Enemy should NOT be ticked by EnemyManager", generic.called);
    }

    @Test
    public void testRenderReturnsCopyOfBirds() {
        EnemyManager mgr = new EnemyManager(dims);
        Enemy a = new Enemy(0,0);
        Enemy b = new Enemy(0,0);
        mgr.addBird(a);
        mgr.addBird(b);
        List<Renderable> renderables = mgr.render();
        assertEquals(2, renderables.size());
        // Mutating returned list shouldn't affect manager state
        renderables.clear();
        assertEquals(2, mgr.getBirds().size());
    }

    @Test
    public void testGetMagpiesFiltersOnlyMagpies() {
        EnemyManager mgr = new EnemyManager(dims);
        Magpie m = new Magpie(0,0, new MockPos(1,1));
        Pigeon p = new Pigeon(0,0, new MockPos(2,2));
        Eagle e = new Eagle(0,0, new MockPlayer(3,3));
        mgr.addBird(m);
        mgr.addBird(p);
        mgr.addBird(e);
        ArrayList<Magpie> magpies = mgr.getMagpies();
        assertEquals(1, magpies.size());
        assertSame(m, magpies.get(0));
    }

    // ---- test helpers ----

    private static class MockEngineState implements EngineState {
        private final Dimensions dims;
        public MockEngineState(Dimensions d) { this.dims = d; }
        @Override public Dimensions getDimensions() { return dims; }
        @Override public engine.input.MouseState getMouse() { return null; }
        @Override public engine.input.KeyState getKeys() { return null; }
        @Override public int currentTick() { return 0; }
    }

    private static class MockGameState implements GameState {
        @Override public World getWorld() { return null; }
        @Override public builder.entities.npc.NpcManager getNpcs() { return null; }
        @Override public EnemyManager getEnemies() { return null; }
        @Override public Player getPlayer() { return new MockPlayer(0,0); }
        @Override public Inventory getInventory() { return new Inventory() {
            private int food = 0, coins = 0;
            @Override public void addFood(int amount) { food = Math.max(0, food + amount); }
            @Override public int getFood() { return food; }
            @Override public void addCoins(int amount) { coins = Math.max(0, coins + amount); }
            @Override public int getCoins() { return coins; }
            @Override public int getCapacity() { return 10; }
            @Override public builder.inventory.items.Item getHolding() { return null; }
            @Override public void setItem(int slot, builder.inventory.items.Item item) { }
            @Override public builder.inventory.items.Item getItem(int slot) { return null; }
            @Override public int getActiveSlot() { return 0; }
            @Override public void setActiveSlot(int slot) { }
        }; }
    }

    private static class MockPlayer implements Player {
        private int x, y;
        public MockPlayer(int x, int y) { this.x = x; this.y = y; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public void setX(int x) { this.x = x; }
        @Override public void setY(int y) { this.y = y; }
        @Override public String getID() { return "mock"; }
        @Override public int getDamage() { return 1; }
    }

    private static class MockPos implements HasPosition {
        private final int x, y;
        public MockPos(int x, int y) { this.x = x; this.y = y; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public void setX(int x) { }
        @Override public void setY(int y) { }
    }

    private static class CountingSpawner implements Spawner {
        int ticks = 0;
        @Override public TickTimer getTimer() { return new TickTimer() {
            @Override public void tick() {}
            @Override public boolean isFinished() { return false; }
        }; }
        @Override public void tick(EngineState state, GameState game) { ticks++; }
        @Override public int getX() { return 0; }
        @Override public void setX(int x) { }
        @Override public int getY() { return 0; }
        @Override public void setY(int y) { }
    }

    private static class SpyMagpie extends Magpie {
        boolean called = false;
        public SpyMagpie(int x, int y, HasPosition pos) { super(x, y, pos); }
        @Override public void tick(EngineState e, GameState g) { called = true; }
    }
    private static class SpyPigeon extends Pigeon {
        boolean called = false;
        public SpyPigeon(int x, int y, HasPosition pos) { super(x, y, pos); }
        @Override public void tick(EngineState e, GameState g) { called = true; }
    }
    private static class SpyEagle extends Eagle {
        boolean called = false;
        public SpyEagle(int x, int y, Player p) { super(x, y, p); }
        @Override public void tick(EngineState e, GameState g) { called = true; }
    }
    private static class SpyEnemy extends Enemy {
        boolean called = false;
        public SpyEnemy(int x, int y) { super(x, y); }
        @Override public void tick(EngineState e, GameState g) { called = true; }
    }
}
