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

/**
 * Ensure that details of a .details file are reflected in the created world.
 */
public class LoadDetailsTest {

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 25;
    private static final int TICKS = 25;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private AnalyserManager data;

    private static ScenarioDetails setupScenario() {
        ScenarioDetails details = new ScenarioDetails(440, 440, 7, 8);
        details.addCabbage(5, 3);
        details.addCabbage(5, 5);
        details.addCabbage(125, 500);
        details.addMagpieSpawner(2, 1, 20);
        details.addMagpieSpawner(5, 5, 70);
        details.addEagleSpawner(1, 2, 200);
        details.addEagleSpawner(5, 5, 300);
        details.addPigeonSpawner(4, 1, 200);
        details.addPigeonSpawner(5, 5, 100);
        return details;
    }

    @Before
    public void setUp() throws IOException, WorldLoadException {
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/playerTest.map"),
                        setupScenario().toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions);
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            core.setState(state);
            engine.tick();
        }
    }

    /** Confirm the player spawned based on the file location. */
    @Test
    public void playerSpawnedBasedOnFileLocation() {
        RenderableAnalyser renderable = data.getFirstSpawnedOfSpriteGroup("chickenFarmer");

        Assert.assertEquals(
                "player x should have been set to match the file x",
                440,
                renderable.getFrames().getFirst().getX());
        Assert.assertEquals(
                "player y should have been set to match the file y",
                440,
                renderable.getFrames().getFirst().getY());
    }

    /** Confirm digits for resources set match those given by the file data. */
    @Test
    public void digitsBasedOnFileData() {
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letters")) {
            final LetterAnalyser letter = new LetterAnalyser(renderableAnalyzer);
            Assert.assertTrue(
                    "only letters seen should only be 7 (coins) or 8 (food)",
                    (letter.is("7") || letter.is("8")));
        }
    }

    /** Confirm digits are in the top left corner. */
    @Test
    public void digitsInTopLeftCorner() {
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letters")) {
            final MovementAnalyser letter = new MovementAnalyser(renderableAnalyzer);
            Assert.assertTrue(
                    "letters should only be seen at the top left",
                    letter.stayedInRectangularArea(0, 0, 150, 200));
        }
    }

    /** Confirm One magpie should have been spawned form an assigned spawner */
    @Test
    public void hasMagpie() {
        Assert.assertEquals("expected 1 unique magpie found", 1, data.getBySpriteGroup("magpie").size());
    }

    /** Confirm magpies are spawned at the given location in the file. */
    @Test
    public void magpieSpawnPoint() {
        RenderableAnalyser renderableAnalyser = data.getFirstSpawnedOfSpriteGroup("magpie");
        final MovementAnalyser magpie = new MovementAnalyser(renderableAnalyser);
        // checking within a small range and limited around the expected spawn because
        // depending on tick order the magpie may or may not have moved in its first frame
        Assert.assertTrue(
                "magpie should have spawned near x:2, y:1 and couldn't have gotten far from"
                        + " it within 2 frames",
                magpie.stayedInRectangularAreaBetweenFrames(2, 1, 25, 25, 1, 2));
    }
}
