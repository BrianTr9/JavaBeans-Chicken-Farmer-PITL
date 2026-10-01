package scenarios;

import builder.JavaBeanFarm;
import builder.ui.SpriteGallery;
import builder.world.WorldLoadException;

import engine.Engine;
import engine.art.sprites.SpriteGroup;
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
import java.util.HashMap;
import java.util.List;

/**
 * A world with 3 cabbages.
 * The player never moves.
 * Player will switch between tools, place a Cabbage and collect it
 * at their location and once it is fully grown.
 *
 * <p>Player will press down a numbered key between their tools at set intervals:
 * frameCount / 3 == 0: key is 5
 * frameCount / 3 == 1: key is 4
 * frameCount / 3 == 2: key is 3
 * frameCount / 3 == 3: key is 2
 * frameCount / 3 == 4: key is 1
 * otherwise: key is 1
 * Player will be holding down their left mouse button the whole time.
 */
public class InventorySimulationTest {

    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 25;
    private static final int TICKS = 505;
    private static HashMap<String, RenderableAnalyser> analyzers;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);
    private static final SpriteGroup art = SpriteGallery.inventory;
    private static AnalyserManager data;

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(340, 400, 1, 3);
        details.addCabbage(340, 400);
        details.addCabbage(380, 400);
        details.addCabbage(325, 430);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/inventoryTest.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        for (int i = 0; i < TICKS; i += 1) {
            MockEngineState state = new MockEngineState(dimensions, i).leftClick();
            if (i / 3 == 0) {
                state = state.press('5');
            } else if (i / 3 == 1) {
                state = state.press('4');
            } else if (i / 3 == 2) {
                state = state.press('3');
            } else if (i / 3 == 3) {
                state = state.press('2');
            } else if (i / 3 == 4) {
                state = state.press('1');
            } else {
                state = state.press('1');
            }
            core.setState(state);
            engine.tick();
        }
    }

    /** Confirm the cabbages on the map on frame 0 match the expected number of 3. */
    @Test
    public void confirmStartingCabbageCount() {
        int cabbagesThereFromFrameZero = 0;
        for (final RenderableAnalyser cabbage : data.getBySpriteGroup("cabbage")) {
            if (cabbage.getFrames().getFirst().getFrame() == 0) {
                cabbagesThereFromFrameZero += 1;
            }
        }

        Assert.assertEquals(
                "should have been 3 cabbages spawned on frame 0", 3, cabbagesThereFromFrameZero);
    }

    @Test
    public void confirmCabbageCollected() {
        final List<Integer> lifespans = new ArrayList<>();
        for (final RenderableAnalyser cabbage : data.getBySpriteGroup("cabbage")) {
            if (cabbage.getFrames().getFirst().getFrame() == 0) {
                lifespans.add(cabbage.frameLifespan());
            }
        }

        boolean foundOneSmaller = false;
        for (final Integer lifespanA : lifespans) {
            for (final Integer lifespanB : lifespans) {
                if (lifespanA < lifespanB) {
                    foundOneSmaller = true;
                    break;
                }
            }
        }
        Assert.assertTrue(
                "At least one cabbage should have a shorter overall lifespan due to being picked"
                        + " up",
                foundOneSmaller);
    }

    @Test
    public void confirmCollectedCabbageIncreasesFood() {
        final List<LetterAnalyser> digits = new ArrayList<>();
        final List<RenderableAnalyser> cabbages = new ArrayList<>();
        for (final RenderableAnalyser digit : data.getBySpriteGroup("letter")) {
            digits.add(new LetterAnalyser(digit));
        }

        for (final RenderableAnalyser cabbage : data.getBySpriteGroup("cabbage")) {
            if (cabbage.getFrames().getFirst().getFrame() == 0) {
                cabbages.add(cabbage);
            }
        }

        boolean seenFive = false;
        boolean seenOne = false;
        boolean seenThree = false;
        boolean seenFour = false;
        final List<LetterAnalyser> foodDigits = new ArrayList<>();
        final List<LetterAnalyser> coinDigits = new ArrayList<>();
        for (final LetterAnalyser digit : digits) {
            if (digit.is("1")) {
                coinDigits.add(digit);
                seenOne = true;
            } else if (digit.is("4")) {
                coinDigits.add(digit);
                seenFour = true;
            } else if (digit.is("3")) {
                foodDigits.add(digit);
                seenThree = true;
            } else if (digit.is("5")) {
                foodDigits.add(digit);
                seenFive = true;
            }
        }
        Assert.assertTrue("should have seen a 5", seenFive);
        Assert.assertTrue("should have seen a 4", seenFour);
        Assert.assertTrue("should have seen a 3", seenThree);
        Assert.assertTrue("should have seen a 1", seenOne);
        // now lets check the heights to make sure the digits we expect to be for food are above the
        // digits we expect to be for coins
        for (final LetterAnalyser coin : coinDigits) {
            for (final LetterAnalyser food : foodDigits) {
                Assert.assertTrue(
                        "all coin related digits should be below all food related digits",
                        coin.getFrames().getFirst().getY() > food.getFrames().getFirst().getY());
            }
        }
    }

    /** Confirm the correct number of cabbages were planting during the lifespan of this sim. */
    @Test
    public void confirmPlantingWorks() {
        Assert.assertEquals(
                "should have been 4 cabbages across the lifespan of the test, "
                        + "due to the player planting one",
                4,
                data.getBySpriteGroup("cabbage").size());
    }

    /**
     * Confirm individual inventory item borders are set to active when expected during the lifespan
     * of this sim.
     */
    @Test
    public void confirmInventoryBordersIndicateWhichIsActive() {
        for (final RenderableAnalyser itemBorder : data.getBySpriteGroup("inventory")) {
            final List<Integer> validActiveFrameCounts = new ArrayList<>();
            validActiveFrameCounts.add(3);
            validActiveFrameCounts.add(493);
            Assert.assertTrue(
                    "each of the tools should have their active border only active for 3 or 493"
                            + " frames",
                    validActiveFrameCounts.contains(
                            itemBorder.lifespanOfSprite(art.getSprite("activeborder"))));
            // confirm it has the activeborder at some point as expected
            Assert.assertTrue(
                    "each inventory item should have had the active border for at least one frame!",
                    itemBorder.allUniqueSprites().contains(art.getSprite("activeborder")));

            Assert.assertTrue(
                    "each inventory square should have had the nonactive border for at least"
                            + " sometime",
                    itemBorder.allUniqueSprites().contains(art.getSprite("border")));
        }
    }

    /** Should have no default colorblock sprites in this sim! */
    @Test
    public void noDefaultSprites() {
        Assert.assertEquals(
                "0 default sprites should have been spawned!",
                0,
                data.getBySpriteGroup("default").size());
    }

    @Test
    public void confirmInventoryBordersNearBottomCenterOfScreen() {
        final int centerX = dimensions.windowSize() / 2;
        final int inventoryY = dimensions.windowSize() - 100;
        Assert.assertTrue(
                data.every(
                        "inventory",
                        inventory ->
                                new MovementAnalyser(inventory)
                                        .stayedInRectangularArea(centerX, inventoryY, 300, 150)));
    }
}
