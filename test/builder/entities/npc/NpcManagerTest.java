package builder.entities.npc;

import builder.GameState;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NpcManagerTest {

    private Dimensions dims;
    private EngineState engine;
    private GameState game;

    @Before
    public void setup() {
        dims = new TileGrid(10, 800);
        engine = new MockEngineState(dims);
        game = new MockGameState();
    }

    @Test
    public void testAddNpcAndGetNpcsReturnsCopy() {
        NpcManager mgr = new NpcManager();
        TestNpc a = new TestNpc(10, 10);
        mgr.addNpc(a);

        List<Npc> snapshot = mgr.getNpcs();
        assertEquals(1, snapshot.size());
        assertSame(a, snapshot.get(0));

        // modifying returned list does not affect manager state
        snapshot.clear();
        assertEquals(1, mgr.getNpcs().size());
    }

    @Test
    public void testCleanupRemovesMarkedNpcs() {
        NpcManager mgr = new NpcManager();
        TestNpc a = new TestNpc(0, 0);
        TestNpc b = new TestNpc(0, 0);
        TestNpc c = new TestNpc(0, 0);
        mgr.addNpc(a);
        mgr.addNpc(b);
        mgr.addNpc(c);
        a.markForRemoval();
        c.markForRemoval();

        mgr.cleanup();

        List<Npc> remaining = mgr.getNpcs();
        assertEquals(1, remaining.size());
        assertSame(b, remaining.get(0));
    }

    @Test
    public void testTickCallsTickOnNpcsAndCleansUpFirst() {
        NpcManager mgr = new NpcManager();
        TestNpc removed = new TestNpc(0, 0);
        TestNpc a = new TestNpc(0, 0);
        TestNpc b = new TestNpc(0, 0);
        removed.markForRemoval(); // should be cleaned before ticking
        mgr.addNpc(removed);
        mgr.addNpc(a);
        mgr.addNpc(b);

        mgr.tick(engine, game);

        assertFalse("Removed NPC must not be ticked", removed.ticked);
        assertTrue(a.ticked);
        assertTrue(b.ticked);

        // also ensure removed NPC no longer in list
        assertEquals(2, mgr.getNpcs().size());
        assertFalse(mgr.getNpcs().contains(removed));
    }

    @Test
    public void testInteractDispatchesToInteractableNpcs() {
        NpcManager mgr = new NpcManager();
        TestNpc a = new TestNpc(0, 0);
        TestNpc b = new TestNpc(0, 0);
        mgr.addNpc(a);
        mgr.addNpc(b);

        mgr.interact(engine, game);

        assertTrue(a.interacted);
        assertTrue(b.interacted);
    }

    @Test
    public void testRenderReturnsCopyOfNpcs() {
        NpcManager mgr = new NpcManager();
        TestNpc a = new TestNpc(0, 0);
        TestNpc b = new TestNpc(0, 0);
        mgr.addNpc(a);
        mgr.addNpc(b);

        List<Renderable> renderables = mgr.render();
        assertEquals(2, renderables.size());
        // Mutate returned list; manager state unaffected
        renderables.clear();
        assertEquals(2, mgr.getNpcs().size());
    }

    // --- test helpers ---

    private static class TestNpc extends Npc {
        boolean ticked = false;
        boolean interacted = false;
        public TestNpc(int x, int y) { super(x, y); }
        @Override public void tick(EngineState s, builder.GameState g) { ticked = true; }
        @Override public void interact(EngineState s, builder.GameState g) { interacted = true; }
    }

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
        @Override public NpcManager getNpcs() { return null; }
        @Override public builder.entities.npc.enemies.EnemyManager getEnemies() { return null; }
        @Override public Player getPlayer() { return new Player() {
            int x=0, y=0;
            @Override public int getX() { return x; }
            @Override public int getY() { return y; }
            @Override public void setX(int x) { this.x=x; }
            @Override public void setY(int y) { this.y=y; }
            @Override public String getID() { return "player"; }
            @Override public int getDamage() { return 1; }
        }; }
        @Override public Inventory getInventory() { return new Inventory() {
            private int food, coins;
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
}
