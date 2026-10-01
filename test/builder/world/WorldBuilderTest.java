package builder.world;

import builder.entities.tiles.Tile;
import builder.entities.tiles.TileFactory;
import builder.ui.SpriteGallery;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class WorldBuilderTest {

    private Dimensions dims(int tiles, int tileSize) {
        return new TileGrid(tiles, tiles * tileSize);
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testFromStringHappyPathBuildsTiles() throws Exception {
        Dimensions d = dims(2, 80); // 2x2 grid
        String text = "gg\nwd\n"; // row0: g g, row1: w d
        List<Tile> tiles = WorldBuilder.fromString(d, text);
        assertEquals(4, tiles.size());
        // Check positions are mapped correctly using tileToPixel
        assertEquals(d.tileToPixel(0), tiles.get(0).getX());
        assertEquals(d.tileToPixel(0), tiles.get(0).getY());
        assertEquals(d.tileToPixel(1), tiles.get(1).getX());
        assertEquals(d.tileToPixel(0), tiles.get(1).getY());
        assertEquals(d.tileToPixel(0), tiles.get(2).getX());
        assertEquals(d.tileToPixel(1), tiles.get(2).getY());
        assertEquals(d.tileToPixel(1), tiles.get(3).getX());
        assertEquals(d.tileToPixel(1), tiles.get(3).getY());
    }

    @Test
    public void testFromStringInvalidLineCountThrows() {
        Dimensions d = dims(2, 80);
        String text = "gg\n"; // only 1 line, expect 2
        try {
            WorldBuilder.fromString(d, text);
            fail("Expected WorldLoadException due to line count mismatch");
        } catch (WorldLoadException e) {
            assertTrue(e.getMessage().contains("Expected 2 lines"));
        }
    }

    @Test
    public void testFromStringInvalidLineLengthThrowsWithRow() {
        Dimensions d = dims(2, 80);
        String text = "g\nwd\n"; // first line length 1, expect 2
        try {
            WorldBuilder.fromString(d, text);
            fail("Expected WorldLoadException due to line length mismatch");
        } catch (WorldLoadException e) {
            assertTrue(e.getMessage().contains("Expected 2 characters"));
            assertTrue("Message should include line number", e.getMessage().contains("line 1"));
        }
    }

    @Test
    public void testFromStringUnknownSymbolReportsRowAndCol() {
        Dimensions d = dims(2, 80);
        String text = "gx\nwd\n"; // 'x' invalid at row0, col1
        try {
            WorldBuilder.fromString(d, text);
            fail("Expected WorldLoadException due to unknown symbol");
        } catch (WorldLoadException e) {
            assertTrue(e.getMessage().contains("Unknown symbol"));
            assertTrue(e.getMessage().contains("line 1"));
            assertTrue(e.getMessage().contains("character 2"));
        }
    }

    @Test
    public void testFromFileReadsAndBuildsWorld() throws Exception {
        Dimensions d = dims(2, 80);
        String text = "gg\nwd\n";
        File f = tmp.newFile("world.txt");
        Files.writeString(f.toPath(), text);

        BeanWorld world = WorldBuilder.fromFile(d, f.getAbsolutePath());
        assertNotNull(world);
        assertEquals(4, world.allTiles().size());
    }

    @Test
    public void testEmptyReturnsEmptyWorld() {
        BeanWorld world = WorldBuilder.empty();
        assertNotNull(world);
        assertTrue(world.allTiles().isEmpty());
    }

    @Test
    public void testFromTilesContainsAllTilesAndReversesOrder() {
        Dimensions d = dims(2, 80);
        // Build tiles using factory so sprites init correctly
        Tile a = TileFactory.fromSymbol(d.tileToPixel(0), d.tileToPixel(0), 'g');
        Tile b = TileFactory.fromSymbol(d.tileToPixel(1), d.tileToPixel(0), 'w');
        List<Tile> input = List.of(a, b);
        BeanWorld world = WorldBuilder.fromTiles(input);
        List<Tile> out = world.allTiles();
        assertEquals(2, out.size());
        // Implementation reverses for deterministic ordering in tests
        assertSame(b, out.get(0));
        assertSame(a, out.get(1));
    }
}
