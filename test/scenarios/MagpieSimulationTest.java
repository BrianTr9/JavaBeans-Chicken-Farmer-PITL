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
import java.util.ArrayList;
import java.util.List;

/**
 * A world with two magpie spawners.
 * The player never moves.
 * Ensures that the correct number of magpies are spawned at the correct location.
 * Ensures also that the magpies move towards their target,
 * fleeing from their target after hitting it, and deducting resources.
 */
public class MagpieSimulationTest {

    private final int centerX = 400;
    private final int centerY = 400;
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 535;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private static AnalyserManager data;

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(centerX, centerY, 9, 0);
        details.addMagpieSpawner(0, 0, 100);
        details.addMagpieSpawner(500, 0, 400);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/magpieTest.map"),
                        details.toReader());

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

    /** Check that the correct number of magpies were spawned over the sim tests lifespan. */
    @Test
    public void hasCorrectNumberOfMagpies() {
        Assert.assertEquals(
                "expected 6 unique magpies found after 535 ticks",
                6,
                data.getBySpriteGroup("magpie").size());
    }

    /**
     * Confirm magpies are spawning near the top left and top right expected spawn, and nowhere
     * else.
     */
    @Test
    public void magpiesSpawnAtDifLocations() {
        int topLeftSpawn = 0;
        int topRightSpawn = 0;
        int otherSpawn = 0;
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("magpie")) {
            final MovementAnalyser magpie = new MovementAnalyser(renderableAnalyzer);
            final int start = magpie.getFrames().getFirst().getFrame();
            final boolean leftSpawn =
                    magpie.stayedInRectangularAreaBetweenFrames(0, 0, 20, 20, start, start + 1);
            final boolean rightSpawn =
                    magpie.stayedInRectangularAreaBetweenFrames(500, 0, 20, 20, start, start + 1);
            if (leftSpawn) {
                topLeftSpawn += 1;
            } else if (rightSpawn) {
                topRightSpawn += 1;
            } else {
                otherSpawn += 1;
            }
        }

        Assert.assertEquals(
                "5 magpies should have been spawned near the top left", 5, topLeftSpawn);
        Assert.assertEquals(
                "1 magpie should have been spawned near the top right", 1, topRightSpawn);
        Assert.assertEquals(
                "0 magpies should have been spawned at no other locations", 0, otherSpawn);
    }

    /**
     * Confirm every magpie get within a minimum distance of the players location as expected as a
     * proxy for them moving closer to the player.
     */
    @Test
    public void magpiesGetCloserToCorrectTarget() {
        final int magpiesCloseToPlayer =
                data.count(
                        "magpie",
                        magpie ->
                                new MovementAnalyser(magpie)
                                        .visitedRectangularArea(
                                                centerX,
                                                centerY,
                                                dimensions.tileSize() * 2,
                                                dimensions.tileSize() * 2));
        final boolean playerSpawnedAt400x400 =
                data.every(
                        "chickenFarmer",
                        player -> {
                            final int chickenFarmerX = player.getFrames().getFirst().getX();
                            final int chickenFarmerY = player.getFrames().getFirst().getY();
                            return chickenFarmerX == centerX && chickenFarmerY == centerY;
                        });
        Assert.assertTrue(
                "Chicken farmer should have been spawned at 400,400 the center of the screen as per"
                        + " the .details file",
                playerSpawnedAt400x400);
        Assert.assertEquals(
                "5 of the 5 magpies should have gotten close (within 2x tileSize) to the player",
                5,
                magpiesCloseToPlayer);
    }

    /**
     * Confirm magpies leave after 'hitting' the player by confirming at least some are seen back at
     * the top left and right corners after on their final frame.
     */
    @Test
    public void magpiesLeaveAfterHittingTarget() {
        final List<MovementAnalyser> magpies = new ArrayList<>();
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("magpie")) {
            final MovementAnalyser magpie = new MovementAnalyser(renderableAnalyzer);
            final boolean wasNearPlayer =
                    magpie.visitedRectangularArea(
                            centerX, centerY, dimensions.tileSize() * 2, dimensions.tileSize() * 2);
            if (wasNearPlayer) {
                magpies.add(magpie);
            }
        }

        boolean oneMagpieGotWithinTileSizeOfTopLeft = false;
        boolean oneMagpieGotWithinTileSizeOfTopRight = false;
        final int slopOffset = 10;
        for (final MovementAnalyser magpie : magpies) {
            if (magpie.distanceFrom(magpie.getFrames().getLast(), 0, 0)
                    < dimensions.tileSize() + slopOffset) {
                oneMagpieGotWithinTileSizeOfTopLeft = true;
            }
            if (magpie.distanceFrom(magpie.getFrames().getLast(), 500, 0)
                    < dimensions.tileSize() + slopOffset) {
                oneMagpieGotWithinTileSizeOfTopRight = true;
            }
        }
        Assert.assertTrue(
                "One magpie should have been back within tileSize of the top left spawn point on"
                        + " its final frame",
                oneMagpieGotWithinTileSizeOfTopLeft);
        Assert.assertTrue(
                "One magpie should have been back within tileSize of the top right spawn point on"
                        + " its final frame",
                oneMagpieGotWithinTileSizeOfTopRight);
    }

    /**
     * Confirm magpies only deduct 1 coin each, we proxy this by confirm all the digits for coin we
     * would expect are shown during the lifespan of this sim.
     */
    @Test
    public void magpiesDeductCorrectResources() {
        final List<LetterAnalyser> coinDigits = new ArrayList<>();
        boolean nineSeen = false;
        boolean eightSeen = false;
        boolean sevenSeen = false;
        boolean sixSeen = false;
        boolean fiveSeen = false;
        boolean fourSeen = false;
        boolean threeSeen = false;
        boolean twoSeen = false;
        boolean oneSeen = false;
        boolean zeroSeen = false;

        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letter")) {
            final LetterAnalyser digit = new LetterAnalyser(renderableAnalyzer);
            if (digit.is("0")) {
                zeroSeen = true;
            }
            if (digit.is("1")) {
                fourSeen = true;
            }
            if (digit.is("2")) {
                fourSeen = true;
            }
            if (digit.is("3")) {
                fourSeen = true;
            }
            if (digit.is("4")) {
                fourSeen = true;
            }
            if (digit.is("5")) {
                fiveSeen = true;
            }
            if (digit.is("6")) {
                sixSeen = true;
            }
            if (digit.is("7")) {
                sevenSeen = true;
            }
            if (digit.is("8")) {
                eightSeen = true;
            }
            if (digit.is("9")) {
                nineSeen = true;
            }
            final boolean isValidCoinDigit =
                    (digit.is("0")
                            || digit.is("4")
                            || digit.is("5")
                            || digit.is("6")
                            || digit.is("7")
                            || digit.is("8")
                            || digit.is("9"));
            if (isValidCoinDigit) {
                coinDigits.add(digit);
            } else {
                Assert.fail("unexpected digit displayed!");
            }
        }

        Assert.assertTrue("should have seen at least one zero!", zeroSeen);
        Assert.assertFalse("should not have seen at any ones!", oneSeen);
        Assert.assertFalse("should not have seen at any twos!", twoSeen);
        Assert.assertFalse("should not have seen at any threes!", threeSeen);
        Assert.assertTrue("should have seen at least one four!", fourSeen);
        Assert.assertTrue("should have seen at least one five!", fiveSeen);
        Assert.assertTrue("should have seen at least one six!", sixSeen);
        Assert.assertTrue("should have seen at least one seven!", sevenSeen);
        Assert.assertTrue("should have seen at least one eight!", eightSeen);
        Assert.assertTrue("should have seen at least one nine!", nineSeen);
    }

    /** Should have no default colorblock sprites in this sim! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }
}
