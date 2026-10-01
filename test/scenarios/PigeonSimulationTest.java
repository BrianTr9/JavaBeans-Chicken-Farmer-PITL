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
import scenarios.analysers.LetterAnalyser;
import scenarios.analysers.MovementAnalyser;
import scenarios.analysers.RenderableAnalyser;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Player will plant a cabbage and walk away from it, a pigeon should spawn after some time and take
 * said cabbage. At frame 100 the player will press 1 and hold down their left mouse button for that
 * frame. At frame 101+ the player will hold down w and thus walk up the screen.
 */
public class PigeonSimulationTest {

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 504;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private AnalyserManager data;

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(340, 400, 3, 0);
        details.addPigeonSpawner(0, 0, 40);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/pigeonTest.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions);
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i == 100) { // select the bucket and water the ground to produce a cabbage
                core.setState(state.press('1').leftClick());
            } else if (i >= 101) { // walk away from the cabbage now it's been placed
                core.setState(state.press('w'));
            } else {
                core.setState(state);
            }
            engine.tick();
        }
    }

    /** Should have no default sprites! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }

    /**
     * Pigeons should only spawn if there is a cabbage on the map, the pigeon details is sets the
     * pigeon spawner being tested to 40 ticks.s
     */
    @Test
    public void pigeonSpawnedWhenCabbageOnFieldNotBefore() {
        Assert.assertFalse(
                "at least one pigeon should have been spawned!",
                data.getBySpriteGroup("pigeon").isEmpty());

        RenderableAnalyser firstPigeon = data.getFirstSpawnedOfSpriteGroup("pigeon");
        Assert.assertTrue(
                "pigeon should not have been spawned before the 100th frame, there are no cabbages"
                        + " planted before then. ",
                firstPigeon.getFirstFrame().getFrame() >= 100);
    }

    /**
     * Confirm the spawned cabbage did not last the entire lifespan of the sim and that 1 pigeon got
     * within stealing distance of it.
     */
    @Test
    public void pigeonTookCabbage() {
        Assert.assertTrue(
                "cabbage should not have lasted over 130 frames",
                data.getFirstSpawnedOfSpriteGroup("cabbage").frameLifespan() < 130);

        // check if a pigeon ever got near the cabbage
        final int x = data.getFirstSpawnedOfSpriteGroup("cabbage").getFirstFrame().getX();
        final int y = data.getFirstSpawnedOfSpriteGroup("cabbage").getFirstFrame().getY();
        final int numOfPigeonsNearCabbage =
                data.count(
                        "pigeon",
                        pigeon ->
                                new MovementAnalyser(pigeon)
                                        .visitedRadialArea(x, y, dimensions.tileSize()));

        Assert.assertEquals(
                "1 pigeon should have gotten within tileSize of the cabbage",
                1,
                numOfPigeonsNearCabbage);
    }

    /**
     * Confirm the player was not the one who took the {@link builder.entities.resources.Cabbage} by
     * confirming that resourced digits were not altered.
     */
    @Test
    public void playerDidNotTakeCabbage() {
        final List<RenderableAnalyser> digits = data.getBySpriteGroup("letters");
        for (RenderableAnalyser renderableAnalyzer : digits) {
            final LetterAnalyser digit = new LetterAnalyser(renderableAnalyzer);
            final boolean validDigit = (digit.is("0") || digit.is("1") || digit.is("3"));
            if (!validDigit) {
                Assert.fail(
                        "invalid digit displayed, only 0, 1 or 3 are expected digits! "
                                + "This likely means the player picked up the cabbage "
                                + "rather then the pigeons in our sim test");
            }
        }
    }
}
