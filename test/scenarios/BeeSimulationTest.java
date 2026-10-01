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
import scenarios.analysers.MovementAnalyser;
import scenarios.analysers.RenderableAnalyser;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * A world with one cabbage and one magpie spawner.
 * The player never moves.
 * On frame 40, the player presses the '4' key to switch to the hive placer and
 * hold down the left mouse button.
 * Expected to place one beehive placed near the players location,
 * that hive should then spawn 6 bees during the simulation.
 */
public class BeeSimulationTest {

    private static final int PLAYER_X = 380;
    private static final int PLAYER_Y = 420;
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 630;
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
        MockEngineState state = new MockEngineState(dimensions);
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i == 40) { // flip to hive placer
                state = state.press('4').leftClick();
            }

            core.setState(state);
            engine.tick();
        }
    }

    /**
     * Confirms a single {@link builder.entities.npc.BeeHive} was successfully placed at the
     * expected location.
     */
    @Test
    public void confirmHivePlacement() {
        Assert.assertEquals("expected 1 unique hive", 1, data.getBySpriteGroup("hive").size());

        final MovementAnalyser hive =
                new MovementAnalyser(data.getBySpriteGroup("hive").getFirst());
        Assert.assertTrue(
                "hive should have been placed near the players location",
                hive.stayedInRadialArea(PLAYER_X, PLAYER_Y, dimensions.tileSize() * 2));
    }

    /** Confirm the bees and beehives are appearing when we expect. */
    @Test
    public void confirmHiveNotThereAtStart() {
        final List<Integer> framesWithNoBee = new ArrayList<>();
        for (int i = 0; i < 41; i += 1) {
            framesWithNoBee.add(i);
        }
        final int firstFrameWithBee = 41;
        for (final RenderableAnalyser hive : data.getBySpriteGroup("hive")) {
            for (int frame : framesWithNoBee) {
                Assert.assertFalse(
                        "hive should not have existed in frame " + frame, hive.wasInFrame(frame));
            }
            Assert.assertTrue(
                    "hive should have existed in frame 4", hive.wasInFrame(firstFrameWithBee));
        }
    }

    /** Confirm the expected number of bees are spawned over the lifespan of this sim. */
    @Test
    public void confirmBeesSpawn() {
        int beeCount = data.getBySpriteGroup("bee").size();
        if (beeCount > 5) {
            Assert.assertEquals("expected to see 5 or 6 bees spawn", 6, beeCount);
        } else {
            Assert.assertEquals("expected to see 5 or 6 bees spawn", 5, beeCount);
        }
    }

    /** Confirm all bees are spawned on top of the hive as expected. */
    @Test
    public void confirmBeesSpawnNearHive() {
        final RenderableAnalyser hive = data.getBySpriteGroup("hive").getFirst();
        final int hiveSpawnX = hive.getFirstFrame().getX();
        final int hiveSpawnY = hive.getFirstFrame().getY();
        for (RenderableAnalyser bee : data.getBySpriteGroup("bee")) {
            final int beeSpawnX = bee.getFirstFrame().getX();
            final int beeSpawnY = bee.getFirstFrame().getY();
            Assert.assertEquals("bee should spawn on top of the hive!", beeSpawnY, hiveSpawnY);
            Assert.assertEquals("bee should spawn on top of the hive!", beeSpawnX, hiveSpawnX);
        }
    }

    /** Confirm bees only spawn after magpies are on the field. */
    @Test
    public void confirmBeesSpawnInReactionToBirds() {
        final List<RenderableAnalyser> magpies = data.getBySpriteGroup("magpie");
        Assert.assertFalse("should have found at least one magpie!", magpies.isEmpty());

        final RenderableAnalyser earliestMagpie = data.getFirstSpawnedOfSpriteGroup("magpie");
        // collect bees and confirm no bee is spawned before the magpies while there
        final List<RenderableAnalyser> bees = data.getBySpriteGroup("bee");
        for (RenderableAnalyser bee : bees) {
            int beeSpawnFrame = bee.getFirstFrame().getFrame();
            assert earliestMagpie != null;
            Assert.assertTrue(
                    "bees should never have spawned before birds",
                    beeSpawnFrame >= earliestMagpie.getFirstFrame().getFrame());
        }
    }

    /** Bees shouldn't be staying still on their x or y axis ever. */
    @Test
    public void confirmBeesDontStayStillOnEitherAxis() {
        for (RenderableAnalyser bee : data.getBySpriteGroup("bee")) {
            int prevY = bee.getFrames().getFirst().getY();
            int prevX = bee.getFrames().getFirst().getX();
            int sameYCount = 0;
            int sameXCount = 0;
            for (FrameRecord frame : bee.getFrames()) {
                // checking for getting stuck horizontally
                if (prevX == frame.getX()) { // we tick up how many frames in a row did we just see
                    // the same x position
                    sameXCount += 1;
                } else {
                    sameXCount = 0;
                }
                prevX = frame.getX();
                Assert.assertNotEquals(
                        "bee should not have stayed at the same horizontal position at any point "
                                + "in the games lifespan for more then 5 concurrent frames",
                        5,
                        sameXCount);

                // checking for getting stuck vertically
                if (prevY == frame.getY()) { // we tick up how many frames in a row did we just see
                    // the same y position
                    sameYCount += 1;
                } else {
                    sameYCount = 0;
                }
                prevY = frame.getY();
                Assert.assertNotEquals(
                        "bee should not have stayed at the same vertical position at any point "
                                + "in the games lifespan for more then 5 concurrent frames",
                        5,
                        sameYCount);
            }
        }
    }

    /**
     * Confirm the bee is not suffering from a bug where it vibrates between 2 or more positions.
     */
    @Test
    public void confirmBeesDontVibrate() {
        for (final RenderableAnalyser bee : data.getBySpriteGroup("bee")) {
            final HashMap<Integer, Integer> xCounts = new HashMap<>();
            for (final FrameRecord frame : bee.getFrames()) {
                if (xCounts.get(frame.getX()) == null) {
                    xCounts.put(frame.getX(), 1);
                } else {
                    xCounts.put(frame.getX(), xCounts.get(frame.getX()) + 1);
                }
            }

            final int maxSameX = 15;
            int matchingXCount = 0;

            for (Entry<Integer, Integer> entry : xCounts.entrySet()) {
                if (entry.getValue() > 1) {
                    matchingXCount += entry.getValue();
                }
            }

            Assert.assertTrue(
                    "Expected at most "
                            + maxSameX
                            + " matching x values, but found "
                            + matchingXCount
                            + " instances of a bee at the given position",
                    matchingXCount <= maxSameX);
        }
    }

    /**
     * Confirm the first bee and first magpie are removed at the sametime + are near each other when
     * they are removed.
     */
    @Test
    public void firstBeeIsRemovedWhenHitsFirstMagpie() {
        final RenderableAnalyser firstBee = data.getFirstSpawnedOfSpriteGroup("bee");
        final RenderableAnalyser firstMagpie = data.getFirstSpawnedOfSpriteGroup("magpie");
        final int difBetweenFinalFrameCount =
                Math.abs(
                        firstMagpie.getFrames().getLast().getFrame()
                                - firstBee.getFrames().getLast().getFrame());

        Assert.assertTrue(
                "At most there should only be 1 frame of difference  between when the first magpie"
                        + " and first bee were removed",
                difBetweenFinalFrameCount <= 1);

        final int difBetweenFinalFrameX =
                Math.abs(
                        firstMagpie.getFrames().getLast().getX()
                                - firstBee.getFrames().getLast().getX());
        final int difBetweenFinalFrameY =
                Math.abs(
                        firstMagpie.getFrames().getLast().getY()
                                - firstBee.getFrames().getLast().getY());

        Assert.assertTrue(
                "first magpie and first bee should have been within tileSize of each other on their"
                        + " final frame",
                (difBetweenFinalFrameX < dimensions.tileSize()
                        && difBetweenFinalFrameY < dimensions.tileSize()));
    }

    /**
     * Confirm whether each {@link builder.entities.npc.GuardBee} that likely removed a {@link
     * builder.entities.npc.enemies.Magpie} (using shared last time alive as in the indicator) were
     * near enough each other for that to be the cause of removal as expected.
     */
    @Test
    public void confirmEachBeeIsRemovedWhenHitsMagpieOnSameFrame() {
        for (final RenderableAnalyser bee : data.getBySpriteGroup("bee")) {
            for (final RenderableAnalyser magpie : data.getBySpriteGroup("magpie")) {
                if (bee.getFrames().getLast().getFrame()
                        == magpie.getFrames().getLast().getFrame()) {
                    final int difBetweenFinalFrameX =
                            Math.abs(
                                    magpie.getFrames().getLast().getX()
                                            - bee.getFrames().getLast().getX());
                    final int difBetweenFinalFrameY =
                            Math.abs(
                                    magpie.getFrames().getLast().getY()
                                            - bee.getFrames().getLast().getY());

                    Assert.assertTrue(
                            "each magpie and bee that seem to vanish on the same frame, should have"
                                    + " been within a tileSize of each other",
                            (difBetweenFinalFrameX < dimensions.tileSize()
                                    && difBetweenFinalFrameY < dimensions.tileSize()));
                }
            }
        }
    }

    /** There should be no default colorblock sprite used in this sim! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }
}
