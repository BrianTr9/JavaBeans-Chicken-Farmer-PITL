package builder.world;

import builder.GameState;
import builder.entities.tiles.Tile;
import builder.ui.SpriteGallery;
import builder.entities.npc.enemies.EnemyManager;
import engine.EngineState;
import engine.game.Entity;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.renderer.Renderable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BeanWorldTest {

    private Dimensions dims;
    private EngineState engine;
    private GameState game;

    @Before
    public void setup() {
        dims = new TileGrid(10, 800); // tile size 80
        engine = new EngineState() {
            @Override public Dimensions getDimensions() { return dims; }
            @Override public engine.input.MouseState getMouse() { return null; }
            @Override public engine.input.KeyState getKeys() { return null; }
            @Override public int currentTick() { return 0; }
        };
        game = new GameState() {
            @Override public builder.world.World getWorld() { return null; }
            @Override public builder.entities.npc.NpcManager getNpcs() { return null; }
            @Override public EnemyManager getEnemies() { return null; }
            @Override public builder.player.Player getPlayer() { return null; }
            @Override public builder.inventory.Inventory getInventory() { return null; }
        };
    }

    @Test
    public void testTickDropsTilesMarkedForRemoval() {
        // Regression: replaced tiles (grass hoed into dirt) used to stay in the world forever,
        // still ticked, rendered and returned by position queries.
        BeanWorld world = WorldBuilder.empty();
        TestTile replaced = new TestTile(40, 40);
        TestTile replacement = new TestTile(40, 40);
        world.place(replaced);
        world.place(replacement);
        replaced.markForRemoval();

        world.tick(engine, game);

        assertEquals(List.of(replacement), world.allTiles());
        assertEquals(List.of(replacement), world.tilesAtPosition(40, 40, dims));
        assertEquals(0, replaced.getTickCount());
        assertFalse(world.render().contains(replaced));
    }

    @Test
    public void testPlaceAndAllTilesReturnsCopy() {
        BeanWorld world = WorldBuilder.empty();
        Tile t1 = new TestTile(160, 240); // col=2,row=3
        world.place(t1);

        List<Tile> list1 = world.allTiles();
        assertEquals(1, list1.size());
        assertSame(t1, list1.get(0));

        // mutate returned list should not affect world
        list1.clear();
        List<Tile> list2 = world.allTiles();
        assertEquals(1, list2.size());
        assertSame(t1, list2.get(0));
    }

    @Test
    public void testTilesAtPositionFindsTilesInSameGridCell() {
        BeanWorld world = WorldBuilder.empty();
        Tile t1 = new TestTile(dims.tileToPixel(2), dims.tileToPixel(3));
        Tile t2 = new TestTile(dims.tileToPixel(2), dims.tileToPixel(3));
        Tile t3 = new TestTile(dims.tileToPixel(5), dims.tileToPixel(6));
        world.place(t1);
        world.place(t2);
        world.place(t3);

        List<Tile> found = world.tilesAtPosition(dims.tileToPixel(2) + 5, dims.tileToPixel(3) + 7, dims);
        assertTrue(found.contains(t1));
        assertTrue(found.contains(t2));
        assertFalse(found.contains(t3));
    }

    @Test
    public void testTilesAtPositionRequiresBothCoordinatesMatch() {
        BeanWorld world = WorldBuilder.empty();
        // Base tile at (2,3)
        Tile base = new TestTile(dims.tileToPixel(2), dims.tileToPixel(3));
        // Same X grid (2), different Y grid (4)
        Tile sameXDiffY = new TestTile(dims.tileToPixel(2), dims.tileToPixel(4));
        // Different X grid (1), same Y grid (3)
        Tile diffXSameY = new TestTile(dims.tileToPixel(1), dims.tileToPixel(3));
        world.place(base);
        world.place(sameXDiffY);
        world.place(diffXSameY);

        List<Tile> found = world.tilesAtPosition(dims.tileToPixel(2) + 5, dims.tileToPixel(3) + 5, dims);
        assertTrue(found.contains(base));
        assertFalse("Tile sharing only X should not be included", found.contains(sameXDiffY));
        assertFalse("Tile sharing only Y should not be included", found.contains(diffXSameY));
    }

    @Test
    public void testTileSelectorFilters() {
        BeanWorld world = WorldBuilder.empty();
        Tile a = new TestTile(0, 0);
        Tile b = new TestTile(80, 0);
        world.place(a);
        world.place(b);

        List<Tile> onlyA = world.tileSelector(t -> t.getX() == 0);
        assertEquals(1, onlyA.size());
        assertSame(a, onlyA.get(0));
    }

    @Test
    public void testTickPropagatesToTiles() {
        BeanWorld world = WorldBuilder.empty();
        TestTile a = new TestTile(0, 0);
        TestTile b = new TestTile(80, 0);
        world.place(a);
        world.place(b);

        assertEquals(0, a.getTickCount());
        assertEquals(0, b.getTickCount());

        world.tick(engine, game);

        assertEquals(1, a.getTickCount());
        assertEquals(1, b.getTickCount());
    }

    @Test
    public void testRenderAggregatesTileRendersPreservingPerTileOrder() {
        BeanWorld world = WorldBuilder.empty();
        TestTile t = new TestTile(0, 0);
        TestEntity e1 = new TestEntity(0, 0);
        TestEntity e2 = new TestEntity(0, 0);
        t.placeOn(e1);
        t.placeOn(e2);
        world.place(t);

        List<Renderable> renderables = world.render();
        assertEquals(3, renderables.size());
        // Tile first, then stacked entities in insertion order
        assertSame(t, renderables.get(0));
        assertSame(e1, renderables.get(1));
        assertSame(e2, renderables.get(2));
    }

    // --- test helpers ---

    private static class TestTile extends Tile {
        private int tickCount = 0;
        public TestTile(int x, int y) { super(x, y, SpriteGallery.grass); }
        @Override public void tick(EngineState engine) { tickCount++; super.tick(engine); }
        public int getTickCount() { return tickCount; }
    }

    private static class TestEntity extends Entity {
        public TestEntity(int x, int y) { super(x, y); }
        @Override public void tick(EngineState state) { }
    }
}
