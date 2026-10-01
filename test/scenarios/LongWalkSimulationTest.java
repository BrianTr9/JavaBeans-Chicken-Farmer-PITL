package scenarios;

import builder.JavaBeanFarm;
import builder.world.WorldLoadException;

import engine.Engine;
import engine.game.Game;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import scenarios.analysers.AnalyserManager;
import scenarios.analysers.FrameRecord;
import scenarios.analysers.RenderableAnalyser;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * A world with 2 of each spawner type.
 * The player starts in the left of the board and walked continuously right for 1100 ticks.
 * Confirm map construction, player construction and some very basic magpie and pigeon behaviours on
 * a larger complex map.
 */
public class LongWalkSimulationTest {

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 25;
    private static final int TICKS = 1100;

    private static AnalyserManager data;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(5, 10, 2, 3);
        details.addMagpieSpawner(2, 1, 360);
        details.addMagpieSpawner(5, 5, 70);
        details.addEagleSpawner(1, 2, 200);
        details.addEagleSpawner(5, 5, 300);
        details.addPigeonSpawner(4, 1, 200);
        details.addPigeonSpawner(5, 5, 100);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/base.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions);
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i > 3) { // hold down the d key after the 3rd frame
                state = state.press('d');
            }
            core.setState(state);
            engine.tick();
        }
    }

    /**
     * Check if game run for 1100 ticks, with the d key held down, and the mouse dragging down and
     * to the right with right mouse down spawns magpies
     */
    @Test
    public void hasMagpie() {
        int magpieCount = data.getBySpriteGroup("magpie").size();
        if (magpieCount > 16) {
            Assert.assertEquals(
                    "expected 16 or 18 unique magpie found", 18, magpieCount);
        } else {
            Assert.assertEquals(
                    "expected 16 or 18 unique magpie found", 16, magpieCount);
        }
    }

    /** Pigeons should never spawn when no Cabbages are on the map. */
    @Test
    public void pigeonsDontSpawnWhenNoCabbages() {
        Assert.assertEquals(
                "should have found 0 pigeons spawned since no cabbages on this map",
                0,
                data.getBySpriteGroup("pigeon").size());
    }

    /** Magpies have had lifespans that are within a sensible range. */
    @Test
    public void magpiesPersisted() {
        data.every("magpie", magpie -> magpie.frameLifespan() > 40);
    }

    /**
     * Confirm ground tiles (field, water, tilled etc) have had their positions snapped to the
     * tileGrid.
     */
    @Test
    public void confirmGroundTilesSnappedToTileGrid() {
        for (final RenderableAnalyser renderable : data.getAll()) {
            final boolean isField = Objects.equals(renderable.spriteGroup(), "field");
            final boolean isWater = Objects.equals(renderable.spriteGroup(), "water");
            final boolean isTilled = Objects.equals(renderable.spriteGroup(), "tilled");
            final boolean isGround = isField || isWater || isTilled;
            if (isGround) {
                final FrameRecord frame =
                        renderable.getFrame(
                                1); // we assume there ground isn't moving around so any frame will
                // do.
                final int halfTile = 16;
                Assert.assertEquals(
                        "map tiles like water, field and tilled should have an x cleanly divisible"
                                + " by tileSize",
                        halfTile, // 32 is full frame size so the offset is caught in the remainder
                        frame.getX() % dimensions.tileSize());
                Assert.assertEquals(
                        "map tiles like water, field and tilled should have an y cleanly divisible"
                                + " by tileSize",
                        halfTile, // 32 is full frame size so the offset is caught in the remainder
                        frame.getY() % dimensions.tileSize());
            }
        }
    }

    /** Confirm expected number of rocks (10) was spawned based on the .map file. */
    @Test
    public void correctNumberOfRocksSpawned() {
        Assert.assertEquals(
                "ten rocks should have been spawned from the map",
                10,
                data.getBySpriteGroup("rock").size());
    }

    /** Confirm 1 pretilled tile was spawned as expected based on the .map file. */
    @Test
    public void correctNumberOfTilledTilesSpawned() {
        Assert.assertEquals(
                "map starts with one pretilled tile", 1, data.getBySpriteGroup("tilled").size());
    }

    /** Confirm 86 grass tiles were created as expected per the .map file. */
    @Test
    public void correctNumberOfGrassTilesSpawned() {
        Assert.assertEquals(
                "map should have 86 grass tiles", 86, data.getBySpriteGroup("grass").size());
    }

    /**
     * Confirm 101 dirt Tiles were created as expected per the .map file, 10 are from Ore entries.
     */
    @Test
    public void correctNumberOfFieldTilesSpawned() {
        Assert.assertEquals(
                "Map should 101 dirt tiles, 10 are from ore",
                101,
                data.getBySpriteGroup("field").size());
    }

    /** Confirm 0 Cabbages spawned at the start as expected. */
    @Test
    public void correctNumberOfCabbagesSpawned() {
        Assert.assertEquals(
                "the player has placed no cabbages on this map",
                0,
                data.getBySpriteGroup("cabbage").size());
    }

    /** Confirm 437 Water Tiles were created as expected per the .map file. */
    @Test
    public void correctNumberOfWaterTilesSpawned() {
        Assert.assertEquals(
                "Should be 437 water tiles", 437, data.getBySpriteGroup("water").size());
    }

    /** Should have no default colorblock sprites in the sim lifespan! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }

    /** Confirm the player (chicken farmer) is present. */
    @Test
    public void playerExists() {
        Assert.assertFalse(
                "should have been able to find the chicken farmer",
                data.getBySpriteGroup("chickenFarmer").isEmpty());
    }
}
