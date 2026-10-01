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
import scenarios.analysers.XyPair;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;

/**
 * A world with 2 cabbages, 2 magpie spawners and 2 pigeon spawners.
 * Simulation Test to confirm scarecrows behave as expected. <br>
 * Player Spawned and doesn't move. Player holds down their left mouse button for entire sim. <br>
 * At Frame 3 the '2' key will be pressed down to switch to the hoe tool. At Frame 5 the '5' key
 * will be pressed down to switch to the scarecrow placement tool. Player will till a nearby tile,
 * then place a Scarecrow on it to protect some nearby cabbages.
 */
public class ScarecrowSimulationTest {

    private final int playerX = 380;
    private final int playerY = 420;
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 530;
    private static AnalyserManager data;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(playerX, playerY, 9, 0);
        details.addCabbage(380, 350);
        details.addCabbage(380, 300);
        details.addMagpieSpawner(0, 0, 100);
        details.addMagpieSpawner(800, 0, 200);
        details.addPigeonSpawner(800, 800, 200);
        details.addPigeonSpawner(0, 800, 300);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/scarecrowTest.map"),
                        details.toReader());
        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions).leftClick();
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i == 3) { // flip to hoe and till the ground tile on
                core.setState(state.press('2'));
            } else if (i == 5) { // flip to the pole placement tool
                core.setState(state.press('5'));
            } else {
                core.setState(state);
            }
            engine.tick();
        }
    }

    /** Check that a scarecrow can be placed by the player, near said player. */
    @Test
    public void scarecrowPlaced() {
        Assert.assertEquals(
                "expected 1 unique scarecrow was placed",
                1,
                data.getBySpriteGroup("scarecrow").size());

        final MovementAnalyser scarecrow =
                new MovementAnalyser(data.getBySpriteGroup("scarecrow").getFirst());
        final boolean scareCrowPlacedWithinTileSizeOfExpectedPlayerLocation =
                scarecrow.visitedRadialArea(playerX, playerY, dimensions.tileSize());

        Assert.assertTrue(
                "Scarecrow should have been placed near the players location",
                scareCrowPlacedWithinTileSizeOfExpectedPlayerLocation);
    }

    /** Confirm the scarecrow was placed with its location snapped to the tileGrid. */
    @Test
    public void confirmSnappedToTileGrid() {
        final int halfTile = dimensions.tileSize() / 2;
        Assert.assertEquals(
                "placed scarecrows should have an x cleanly divisible by tileSize",
                halfTile, // 32 is full frame size so the offset is caught in the remainder
                data.getBySpriteGroup("scarecrow").getFirst().getFirstFrame().getX()
                        % dimensions.tileSize());
        Assert.assertEquals(
                "placed scarecrows should have an y cleanly divisible by tileSize",
                halfTile, // 32 is full frame size so the offset is caught in the remainder
                data.getBySpriteGroup("scarecrow").getFirst().getFirstFrame().getY()
                        % dimensions.tileSize());
    }

    /** Confirm 2 Cabbages were spawned at different locations. */
    @Test
    public void cabbageSpawned() {
        Assert.assertEquals(
                "expected 2 unique cabbages", 2, data.getBySpriteGroup("cabbage").size());

        final RenderableAnalyser firstCabbage = data.getBySpriteGroup("cabbage").getFirst();
        final RenderableAnalyser secondCabbage = data.getBySpriteGroup("cabbage").getLast();

        final boolean sameX =
                (firstCabbage.getFirstFrame().getX() == secondCabbage.getFirstFrame().getX());
        final boolean sameY =
                (firstCabbage.getFirstFrame().getY() == secondCabbage.getFirstFrame().getY());
        Assert.assertFalse(
                "1st and second cabbage should not be spawned at the same coordinates",
                sameX && sameY);
    }

    /** Confirm we saw no instances of the default color brick sprite. */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }

    /** Confirm 3 pigeons were spawned over the sims lifespan. */
    @Test
    public void pigeonsSpawned() {
        Assert.assertEquals(
                "3 pigeons should have been spawned!", 3, data.getBySpriteGroup("pigeon").size());
    }

    /** Confirm 7 magpies were spawned over the sims lifespan. */
    @Test
    public void magpiesSpawned() {
        Assert.assertEquals(
                "7 magpies should have been spawned!", 7, data.getBySpriteGroup("magpie").size());
    }

    /** Confirm pigeons moved towards the player at expected rates and directions. */
    @Test
    public void pigeonsMoveTowardsPlayer() {
        int countMovedDownRight = 0;
        int countMovedUpLeft = 0;
        int countMovedUpRight = 0;
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("pigeon")) {
            final MovementAnalyser pigeon = new MovementAnalyser(renderableAnalyzer);
            final int start = pigeon.getFrames().getFirst().getFrame();
            final int end = pigeon.getFrames().getFirst().getFrame() + 5;

            final XyPair delta = pigeon.measureOverallMoveBetween(start, end);
            final boolean movedDownRight = (delta.getX() == 20 && delta.getY() == 20);
            final boolean movedUpLeft = (delta.getX() == -20 && delta.getY() == -20);
            final boolean movedUpRight = (delta.getX() == 20 && delta.getY() == -20);
            if (movedUpLeft) {
                countMovedUpLeft += 1;
            }
            if (movedDownRight) {
                countMovedDownRight += 1;
            }
            if (movedUpRight) {
                countMovedUpRight += 1;
            }
        }

        // allows either the fixed or broken version of pigeon spawner
        if (countMovedDownRight > countMovedUpRight) {
            Assert.assertEquals(
                    "1 pigeon should have been spawned initially flying either down and to the right or up and to the right",
                    1,
                    countMovedDownRight);
        } else {
            Assert.assertEquals(
                    "1 pigeon should have been spawned initially flying either down and to the right or up and to the right",
                    1,
                    countMovedUpRight);
        }
        Assert.assertEquals(
                "2 pigeons should have been spawned initially flying up and to the left",
                2,
                countMovedUpLeft);
    }

    /**
     * Confirm magpies moved towards the player at expected rates and directions over the lifespan
     * of the sim. Expect: 2 to move down and to the left 5 to move down and to the right
     */
    @Test
    public void magpiesMoveTowardsPlayer() {
        int countMovedDownLeftOnSpawn = 0;
        int countMovedDownRightOnSpawn = 0;
        for (final RenderableAnalyser renderableAnalyzer : data.getBySpriteGroup("magpie")) {
            final MovementAnalyser magpie = new MovementAnalyser(renderableAnalyzer);
            final int start = magpie.getFrames().getFirst().getFrame();
            final int end = magpie.getFrames().getFirst().getFrame() + 10;

            final boolean movedDownRight =
                    (magpie.measureOverallMoveBetween(start, end).getX()
                            == magpie.measureOverallMoveBetween(start, end).getY());
            if (movedDownRight) {
                countMovedDownRightOnSpawn += 1;
            }
            final boolean movedDownLeft =
                    (magpie.measureOverallMoveBetween(start, end).getX()
                            == -magpie.measureOverallMoveBetween(start, end).getY());
            if (movedDownLeft) {
                countMovedDownLeftOnSpawn += 1;
            }
        }
        Assert.assertEquals(
                "2 of the magpies should have been moving down and to the left close to when they"
                        + " first spawned",
                2,
                countMovedDownLeftOnSpawn);
        Assert.assertEquals(
                "5 of the magpies should have been moving down and to the right close to when they"
                        + " first spawned",
                5,
                countMovedDownRightOnSpawn);
    }

    /** Confirm Pigeons are unable to enter the 'scare' radius of the placed scarecrow. */
    @Test
    public void pigeonsScaredOff() {
        final boolean pigeonEnteredSpace =
                data.every(
                        "pigeon",
                        pigeon ->
                                new MovementAnalyser(pigeon)
                                        .visitedRadialArea(
                                                playerX, playerY, dimensions.tileSize() * 3));
        Assert.assertFalse("pigeon should not have entered the given space", pigeonEnteredSpace);
    }

    /** Confirm magpies are unable to enter the 'scare' radius of the placed scarecrow. */
    @Test
    public void magpiesScaredOff() {
        final boolean magpieEnteredSpace =
                data.every(
                        "magpie",
                        magpie ->
                                new MovementAnalyser(magpie)
                                        .visitedRadialArea(
                                                playerX, playerY, dimensions.tileSize() * 3));
        Assert.assertFalse(magpieEnteredSpace);
    }

    /**
     * Confirm the digits for coin/food are only within the the values we expect as a proxy for if
     * theft occurred.
     */
    @Test
    public void noCoinTheft() {
        final boolean onlyValidDigits =
                data.every(
                        "letters",
                        letter -> {
                            LetterAnalyser digit = new LetterAnalyser(letter);
                            return digit.is("0") || digit.is("9") || digit.is("7");
                        });

        Assert.assertTrue("Only digits found should be 0, 7 or 9", onlyValidDigits);
    }

    /**
     * Confirm all cabbages lived the entire lifespan of the sim as expected as an indication they
     * were not stolen by the cabbages.
     */
    @Test
    public void noCabbageTheft() {
        final boolean allCabbagesLivedFullSpan =
                data.every("cabbage", cabbage -> cabbage.frameLifespan() == TICKS);
        Assert.assertTrue(
                "All cabbages should have lived for the entire span of the game",
                allCabbagesLivedFullSpan);
    }
}
