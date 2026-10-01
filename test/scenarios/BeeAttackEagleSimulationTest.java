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
 * A world with one cabbage and one eagle spawner.
 * In the first tick, the player switches to the hive hammer by pressing key 4.
 * Otherwise, the player continuously presses 'w' to go up.
 * The player continuously holds left click while moving up.
 * Expected to place 3 hives that will then shoot at incoming eagles spawned at a slow rate.
 */
public class BeeAttackEagleSimulationTest {

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 530;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private AnalyserManager data;

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(380, 420, 9, 6);
        details.addCabbage(380, 350);
        details.addEagleSpawner(800, 400, 300);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/beeSlowEagleSpawnTest.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions).leftClick();
        for (int i = 0; i < TICKS; i += 1) {
            MockEngineState currentState = state.withFrame(i);
            if (i == 0) { // set to hive hammer
                currentState = currentState.press('4');
            } else { // go up the screen
                currentState = currentState.press('w');
            }

            core.setState(currentState);
            engine.tick();
        }
    }

    /** Confirm the 3 hives were placed at different locations and not all at the same location. */
    @Test
    public void confirmHivesNotPlacedOnTopOfEachOther() {
        for (final RenderableAnalyser hive : data.getBySpriteGroup("hive")) {
            for (final RenderableAnalyser otherHive : data.getBySpriteGroup("hive")) {
                if (hive != otherHive) {
                    final boolean sameX =
                            (hive.getFirstFrame().getX() == otherHive.getFirstFrame().getX());
                    final boolean sameY =
                            (hive.getFirstFrame().getY() == otherHive.getFirstFrame().getY());
                    final boolean samePosition = (sameX && sameY);
                    Assert.assertFalse(
                            "no 2 hives should be sharing the same position", samePosition);
                }
            }
        }
    }

    /** Confirm 3 hives have been placed over the lifespan of this sim. */
    @Test
    public void confirmBeeHivesPlaced() {
        Assert.assertEquals(
                "only 3 hives should have been placed over the lifespan of this game",
                3,
                data.getBySpriteGroup("hive").size());
    }

    /** Confirm each hive was placed with its position snapped to the tileGrid. */
    @Test
    public void confirmSnappedToTileGrid() {
        final int halfTile = dimensions.tileSize() / 2;
        for (final RenderableAnalyser hive : data.getBySpriteGroup("hive")) {
            Assert.assertEquals(
                    "placed hive should have an x cleanly divisible by tileSize",
                    halfTile, // offset should be caught in the remainder
                    hive.getFirstFrame().getX() % dimensions.tileSize());
            Assert.assertEquals(
                    "placed hive should have an y cleanly divisible by tileSize",
                    halfTile, // offset should be caught in the remainder
                    hive.getFirstFrame().getY() % dimensions.tileSize());
        }
    }

    /** Confirm 3 bees were spawned as expected. */
    @Test
    public void confirmBeesSpawned() {
        Assert.assertEquals("3 bees expected", 3, data.getBySpriteGroup("bee").size());
    }

    /** Confirm one eagle was spawned as expected. */
    @Test
    public void confirmEagleSpawned() {
        Assert.assertEquals("1 eagle expected", 1, data.getBySpriteGroup("eagle").size());
    }

    /**
     * Confirm that the eagle was likely hit by a bee by comparing the final frame of the eagle
     * against the final frame of the bees, checking for matching final frame and if the there is a
     * matching final frame that bee and the eagle were within a tileSize of each other.
     */
    @Test
    public void confirmEagleLikelyHitByABee() {
        RenderableAnalyser eagle = data.getFirstSpawnedOfSpriteGroup("eagle");
        final int finalEagleFrame = eagle.getFrames().getLast().getFrame();
        for (RenderableAnalyser bee : data.getBySpriteGroup("bee")) {
            if (bee.getFrames().getLast().getFrame() == finalEagleFrame) {
                final int eagleX = eagle.getFrames().getLast().getX();
                final int eagleY = eagle.getFrames().getLast().getY();
                Assert.assertTrue(
                        "bee that likely removed eagle (based on matching finalFrames) "
                                + "should have been within tileSize "
                                + "of the eagles final location",
                        new MovementAnalyser(bee)
                                .visitedRadialArea(eagleX, eagleY, dimensions.tileSize()));
                return;
            }
        }
        Assert.fail("eagle did not get removed on the same frame as a bee!");
    }
}
