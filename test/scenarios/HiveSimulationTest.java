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
import scenarios.analysers.MovementAnalyser;
import scenarios.analysers.RenderableAnalyser;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;

/**
 * A world with one cabbage and one magpie spawner.
 * The player never moves.
 * On frame 3, the player presses '4' to switch to the hive placer and holds the left mouse down.
 * Expected to place one beehive placed near the player's location.
 */
public class HiveSimulationTest {

    private static final int PLAYER_X = 380;
    private static final int PLAYER_Y = 420;

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 530;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private AnalyserManager data;

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(PLAYER_X, PLAYER_Y, 9, 2);
        details.addCabbage(380, 350);
        details.addMagpieSpawner(0, 0, 100);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/beeTest.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions).leftClick();
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i == 3) { // flip to hive hammer
                core.setState(state.press('4'));
            } else {
                core.setState(state);
            }
            engine.tick();
        }
    }

    /** Confirm 1 hive was spawned and that it was spawned at/near the correct location. */
    @Test
    public void confirmPlacement() {
        Assert.assertEquals("expected 1 unique hive", 1, data.getBySpriteGroup("hive").size());

        final MovementAnalyser hive =
                new MovementAnalyser(data.getBySpriteGroup("hive").getFirst());
        Assert.assertTrue(
                "hive was placed near its expected location",
                hive.stayedInRadialArea(PLAYER_X, PLAYER_Y, dimensions.tileSize()));
    }

    /** Confirm the spawned hive had its location snapped to the tileGrid as expected. */
    @Test
    public void confirmSnappedToTileGrid() {
        final int halfTile = dimensions.tileSize() / 2;
        Assert.assertEquals(
                "placed hive should have an x cleanly divisible by tileSize",
                halfTile, // 32 is full frame size so the offset is caught in the remainder
                data.getBySpriteGroup("hive").getFirst().getFirstFrame().getX()
                        % dimensions.tileSize());
        Assert.assertEquals(
                "placed hive should have an y cleanly divisible by tileSize",
                halfTile, // 32 is full frame size so the offset is caught in the remainder
                data.getBySpriteGroup("hive").getFirst().getFirstFrame().getY()
                        % dimensions.tileSize());
    }

    /** No default colorblock sprites should be seen during this sim! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }

    /** Confirm the hive was probably spawned by the players actions, not the detail file. */
    @Test
    public void confirmHiveNotThereAtStart() {
        for (final RenderableAnalyser hive : data.getBySpriteGroup("hive")) {
            Assert.assertFalse("hive should not have existed in frame 0", hive.wasInFrame(0));
            Assert.assertFalse("hive should not have existed in frame 1", hive.wasInFrame(1));
            Assert.assertFalse("hive should not have existed in frame 2", hive.wasInFrame(2));
            Assert.assertFalse("hive should not have existed in frame 3", hive.wasInFrame(3));
            Assert.assertTrue("hive should have existed in frame 4", hive.wasInFrame(4));
        }
    }
}
