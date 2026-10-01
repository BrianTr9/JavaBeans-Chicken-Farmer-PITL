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
 * A world with two eagle spawners.
 * The player never moves.
 * Ensure that 3 eagles are spawned at approximately the right locations during the simulation.
 * 2 eagles should be spawned at the top left and 1 at the top right.
 * They should move towards the player, and on getting
 * close to the player, steal food from them. Then fly back towards their spawn locations.
 */
public class EagleSimulationTest {

    private final int centerX = 400;
    private final int centerY = 400;
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 530;
    private static AnalyserManager data;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(centerX, centerY, 1, 8);
        details.addEagleSpawner(0, 0, 200);
        details.addEagleSpawner(500, 0, 300);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/eagleTest.map"),
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

    /** Check that the correct number of eagles were spawned over the sim tests lifespan. */
    @Test
    public void hasCorrectNumberOfEagles() {
        Assert.assertEquals(
                "expected 3 unique eagles found after 500 ticks",
                3,
                data.getBySpriteGroup(("eagle")).size());
    }

    /**
     * Confirm eagles are spawning near the top left and top right expected spawn, and nowhere else.
     */
    @Test
    public void eaglesSpawnAtDifLocations() {
        int topLeftSpawn = 0;
        int topRightSpawn = 0;
        int otherSpawn = 0;
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("eagle")) {
            final MovementAnalyser eagle = new MovementAnalyser(renderableAnalyzer);
            final int start = eagle.getFrames().getFirst().getFrame();
            final boolean leftSpawn =
                    eagle.stayedInRectangularAreaBetweenFrames(0, 0, 20, 20, start, start + 1);
            final boolean rightSpawn =
                    eagle.stayedInRectangularAreaBetweenFrames(500, 0, 20, 20, start, start + 1);
            if (leftSpawn) {
                topLeftSpawn += 1;
            } else if (rightSpawn) {
                topRightSpawn += 1;
            } else {
                otherSpawn += 1;
            }
        }

        Assert.assertEquals("2 eagles should have been spawned near the top left", 2, topLeftSpawn);
        Assert.assertEquals(
                "1 eagle should have been spawned near the top right", 1, topRightSpawn);
        Assert.assertEquals(
                "0 eagles should have been spawned at no other locations", 0, otherSpawn);
    }

    /**
     * Confirm the eagles moved in roughly the right directions over their lifespan, checking
     * against their overall movement deltas.
     */
    @Test
    public void eaglesGoRoughlyCorrectDirections() {
        final List<MovementAnalyser> leftSpawnedEagles = new ArrayList<>();
        final List<MovementAnalyser> rightSpawnedEagles = new ArrayList<>();

        for (final RenderableAnalyser renderable : data.getBySpriteGroup("eagle")) {
            final MovementAnalyser eagle = new MovementAnalyser(renderable);
            final int start = eagle.getFrames().getFirst().getFrame();
            final int leftX = 0;
            final int rightX = 500;
            final int topY = 0;
            final int size = 20;
            final boolean leftSpawn =
                    eagle.stayedInRectangularAreaBetweenFrames(
                            leftX, topY, size, size, start, start + 1);
            final boolean rightSpawn =
                    eagle.stayedInRectangularAreaBetweenFrames(
                            rightX, topY, size, size, start, start + 1);
            if (leftSpawn) {
                leftSpawnedEagles.add(eagle);
            }
            if (rightSpawn) {
                rightSpawnedEagles.add(eagle);
            }
        }

        // check overall movement deltas, rough check to just make sure the bird isn't being
        // teleported around
        for (final MovementAnalyser eagle : leftSpawnedEagles) {
            Assert.assertTrue(
                    "left eagles should have net total moved more to the right during its lifespan"
                            + " by at least 50 pixels",
                    eagle.measureOverallMove().getX() > 50);
            Assert.assertTrue(
                    "left eagles should have net total moved more downwards then up during its"
                            + " lifespan by at least 50 pixels",
                    eagle.measureOverallMove().getY() > 50);
        }
        for (final MovementAnalyser eagle : rightSpawnedEagles) {
            Assert.assertTrue(
                    "right eagle should have net total moved more to the left then the right during"
                            + " its life span",
                    eagle.measureOverallMove().getX() < 0);
            Assert.assertTrue(
                    "right eagle should have net total moved down at least 50 pixels across its"
                            + " lifespan",
                    eagle.measureOverallMove().getY() > 50);
        }
    }

    /**
     * Confirm eagles were closing to the players location by checking if the expected number of
     * eagles got close to the players location.
     */
    @Test
    public void eaglesGetCloserToCorrectTarget() {
        int eaglesCloseToPlayer = 0;
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("eagle")) {
            final MovementAnalyser eagle = new MovementAnalyser(renderableAnalyzer);
            if (eagle.visitedRectangularArea(
                    centerX, centerY, dimensions.tileSize() * 2, dimensions.tileSize() * 2)) {
                eaglesCloseToPlayer += 1;
            }
        }
        final boolean playerSpawnedAt400x400 =
                data.every(
                        "chickenFarmer",
                        player -> {
                            final int chickenFarmerX = player.getFirstFrame().getX();
                            final int chickenFarmerY = player.getFirstFrame().getY();
                            return chickenFarmerX == centerX && chickenFarmerY == centerY;
                        });
        Assert.assertTrue(
                "Chicken farmer should have been spawned at 400,400 the center of the screen as per"
                        + " the .details file",
                playerSpawnedAt400x400);
        Assert.assertEquals(
                "2 of the 3 eagles should have gotten close (within 2x tileSize) to the player",
                2,
                eaglesCloseToPlayer);
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
     * Confirm eagles that got near enough to the player to hit them then returned to their spawn
     * points.
     *
     * <p>Checking distance between eagle and player + checking if an eagle got back to the top left
     * and top right spawn by their final frames.
     */
    @Test
    public void eaglesLeaveAfterHittingTarget() {
        final List<MovementAnalyser> eagles = new ArrayList<>();
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("eagle")) {
            final MovementAnalyser eagle = new MovementAnalyser(renderableAnalyzer);
            final boolean wasNearPlayer =
                    eagle.visitedRectangularArea(
                            centerX, centerY, dimensions.tileSize() * 2, dimensions.tileSize() * 2);
            if (wasNearPlayer) {
                eagles.add(eagle);
            }
        }

        boolean oneEagleGotWithinTileSizeOfTopLeft = false;
        boolean oneEagleGotWithinTileSizeOfTopRight = false;
        final int slopOffset = 10;
        for (final MovementAnalyser eagle : eagles) {
            if (eagle.distanceFrom(eagle.getFrames().getLast(), 0, 0)
                    < dimensions.tileSize() + slopOffset) {
                oneEagleGotWithinTileSizeOfTopLeft = true;
            }
            if (eagle.distanceFrom(eagle.getFrames().getLast(), 500, 0)
                    < dimensions.tileSize() + slopOffset) {
                oneEagleGotWithinTileSizeOfTopRight = true;
            }
        }
        Assert.assertTrue(
                "One eagle should have been back within tileSize of the top left spawn point on its"
                        + " final frame",
                oneEagleGotWithinTileSizeOfTopLeft);
        Assert.assertTrue(
                "One eagle should have been back within tileSize of the top right spawn point on"
                        + " its final frame",
                oneEagleGotWithinTileSizeOfTopRight);
    }

    /** Confirm eagles despawn at around the times we would expect during this sim. */
    @Test
    public void eaglesDespawnAtRoughlyCorrectTime() {
        final List<Integer> lifespans = new ArrayList<>();
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("eagle")) {
            final MovementAnalyser eagle = new MovementAnalyser(renderableAnalyzer);
            lifespans.add(eagle.frameLifespan());
        }

        // confirm one had a life span of at least 200
        boolean hadLongLifespan = false;

        // confirm one had a life span of at least 125 and no more than 150
        boolean hadMediumLifespan = false;

        // confirm one had a life span of at least 100 and no more than 120
        boolean hadShortLifespan = false;
        for (final Integer despawnTime : lifespans) {
            if (despawnTime >= 200) {
                hadLongLifespan = true;
            }
            if (despawnTime >= 125 && despawnTime <= 150) {
                hadMediumLifespan = true;
            }
            if (despawnTime >= 100 && despawnTime <= 120) {
                hadShortLifespan = true;
            }
        }
        Assert.assertTrue(
                "should have had one eagle with at least 200 frames of lifespan", hadLongLifespan);
        Assert.assertTrue("should had one eagle a lifespan between 125 and 150", hadMediumLifespan);
        Assert.assertTrue("should had one eagle a lifespan between 100 and 120", hadShortLifespan);
    }

    /** Confirm only numeric sprites were shown for the 'letters' used. */
    @Test
    public void ensureOnlyNumericsShownForResources() {
        for (RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letters")) {
            Assert.assertFalse(
                    "should have no alphabetic letters on this screen",
                    new LetterAnalyser(renderableAnalyzer).isAlphabetic());
            Assert.assertTrue(new LetterAnalyser(renderableAnalyzer).isNumeric());
        }
    }

    /**
     * Only the top digit representing food should be getting changed in this eagle testing sim,
     * that digit should only ever be 8, 5, 2 or 0 due to how much food eagles deduct on 'hit'.
     */
    @Test
    public void ensureChangingDigitIsOnlyTheTopOne() {
        final List<LetterAnalyser> frameDigits = new ArrayList<>();
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letters")) {
            final LetterAnalyser digit = new LetterAnalyser(renderableAnalyzer);
            frameDigits.add(digit);
        }

        /*
         * The Food digit can only ever be 0, 2, 5 or 8 if the game is implemented correctly,
         * so we can use that to detect digits relating to food.
         * The Coin digit can only ever be 1 in this test, so we can use that to detect it.
         */
        final ArrayList<Integer> foodY = new ArrayList<>();
        final ArrayList<Integer> coinY = new ArrayList<>();
        for (final LetterAnalyser digit : frameDigits) {
            final boolean isValidFoodDigit =
                    (digit.is("0") || digit.is("2") || digit.is("5") || digit.is("8"));
            if (isValidFoodDigit) {
                foodY.add(digit.getFrames().getFirst().getY());
            }
            final boolean isValidCoinDigit = (digit.is("1"));
            if (isValidCoinDigit) {
                coinY.add(digit.getFrames().getFirst().getY());
            }
            Assert.assertTrue(
                    "For digits game should only have needed to show 0, 1, 2, 5 or 8",
                    isValidCoinDigit || isValidFoodDigit);
        }

        for (int i = 0; i < foodY.size(); i += 1) {
            Assert.assertTrue(
                    "Food Digits should always be above coin digits",
                    (foodY.get(i) < coinY.get(i)));
        }
    }

    /**
     * Eagles should only be taking food in increments of 3, so the food digits shown should only
     * have been 8, 5 ,2 and 0 (since player food shouldn't be able to go below 0).
     */
    @Test
    public void eaglesDeductCorrectResources() {
        final List<LetterAnalyser> foodDigits = new ArrayList<>();
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("letters")) {
            final LetterAnalyser digit = new LetterAnalyser(renderableAnalyzer);
            final boolean isValidFoodDigit =
                    (digit.is("0") || digit.is("2") || digit.is("5") || digit.is("8"));
            if (isValidFoodDigit) {
                foodDigits.add(digit);
            }
        }
    }
}
