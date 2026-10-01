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

import scenarios.analysers.*;
import scenarios.details.ScenarioDetails;
import scenarios.mocks.MockCore;
import scenarios.mocks.MockEngineState;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation Test to check ore can be jackhammered, and cabbages can be collected. <br>
 * Player will spawn on top of some Ore. Player holds down their left mouse button across entire sim
 * lifespan. On Frame 3 the player will press '3' to switch to their jackhammer. <br>
 * If frame is greater than 5 but less than 200, player will press 's' on their keyboard to move
 * down. <br>
 * If frame is greater than 200, player will press 'a' on their keyboard to move to the left. <br>
 * (Player movement should be blocked by nearby water tiles to prevent the player moving away from a
 * growing cabbage near the final expected position of the player) jackhammer the gold as walk down
 * then to the left to a cabbage near said ore and collect it by walking onto it.
 */
public class ResourceSimulationTest {

    private final int playerX = 340;
    private final int playerY = 380;
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 10;
    private static final int TICKS = 530;
    private static AnalyserManager data;
    private static final Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);

    @Before
    public void setUp() throws IOException, WorldLoadException {
        ScenarioDetails details = new ScenarioDetails(playerX, playerY, 1, 3);
        details.addCabbage(300, 440);
        details.addCabbage(300, 370);
        final Game game =
                new JavaBeanFarm(
                        dimensions,
                        new FileReader("resources/testmaps/resourceTest.map"),
                        details.toReader());

        data = new AnalyserManager();
        final MockCore core = new MockCore(data);
        final Engine engine = new Engine(game, dimensions, core);
        MockEngineState state = new MockEngineState(dimensions).leftClick();
        for (int i = 0; i < TICKS; i += 1) {
            state = state.withFrame(i);
            if (i == 3) { // flip to jackhammer and drill
                core.setState(state.press('3'));
            } else if (i > 200) { // move to the left and down after several frames
                core.setState(state.press('a'));
            } else if (i > 5) {
                core.setState(state.press('s'));
            } else {
                core.setState(state);
            }
            engine.tick();
        }
    }

    /**
     * Confirm rocks changed visually using different sprites over the lifespan of this sim as a
     * proxy that they were being drilled, also Confirm those rocks that changes were within range
     * of the player.
     */
    @Test
    public void confirmOreCollected() {
        final int numOfRocksThatChangedStateVisually =
                data.count("rock", rock -> rock.allUniqueSprites().size() > 1);
        final List<RenderableAnalyser> drilledRocks =
                data.filter("rock", rock -> rock.allUniqueSprites().size() > 1);
        Assert.assertEquals(
                "1 rock should have had its visual state changed",
                1,
                numOfRocksThatChangedStateVisually);
        for (final RenderableAnalyser drilledRock : drilledRocks) {
            final boolean rockNearPlayerStartingLocation =
                    new MovementAnalyser(drilledRock)
                            .stayedInRectangularArea(
                                    playerX, playerY, dimensions.tileSize(), dimensions.tileSize());
            Assert.assertTrue(
                    "Any drilled rock should have been within 1 tileSize of the "
                            + "players original xAxis and yAxis",
                    rockNearPlayerStartingLocation);
        }
    }

    /**
     * Confirms that 2 cabbages were spawned, one was collected early, both got to grow all growth
     * stages. ALso confirm that the cabbage that was collected (i.e. the one wit the shorter
     * lifespan) was the one we expect would be picked up by our moving player.
     */
    @Test
    public void confirmCabbageCollected() {
        final List<RenderableAnalyser> cabbages = data.getBySpriteGroup("cabbage");
        Assert.assertEquals("There were 2 cabbages in the test", 2, cabbages.size());

        Assert.assertNotEquals(
                "One cabbage should have a shortened lifespan as it should have been picked up by"
                        + " our player",
                cabbages.getFirst().frameLifespan(),
                cabbages.getLast().frameLifespan());

        for (final RenderableAnalyser cabbage : cabbages) {
            Assert.assertEquals(
                    "Each cabbage should have gone through all growth stages",
                    5,
                    cabbage.allUniqueSprites().size());
        }

        final RenderableAnalyser shortLivedCabbage =
                data.filter("cabbage", cabbage -> cabbage.frameLifespan() < TICKS).getFirst();
        // Confirm the cabbage that was short-lived was the one to the bottom left of our player
        Assert.assertTrue(
                "Cabbage that did not last the entire simulation (and thus was likely picked up) "
                        + "was to the left of the player as expected",
                shortLivedCabbage.getFirstFrame().getX() < playerX);
        Assert.assertTrue(
                "Cabbage that did not last the entire simulation (and thus was likely picked up) "
                        + "was below the player as expected",
                shortLivedCabbage.getFirstFrame().getY() > playerY);
    }

    // It's too much code to derive the exact state at the right times for the digits given the
    // user could be generating the letters anew per frame which would have new uuids etc.
    // So instead we check a bunch of other proxy values that kind of have to be true if its
    // working correctly to get a close enough check.

    /**
     * Confirm digits for coins were changed overtime to the valid values and only the valid values.
     * For simplicity of the test we are not forming if they changed in the correct order.
     */
    @Test
    public void confirmCoinChangedByCollection() {
        final List<RenderableAnalyser> coinDigits =
                data.filter(
                        "letter",
                        letter ->
                                letter.getFirstFrame().getY() > 60
                                        && new LetterAnalyser(letter).isNumeric());
        final List<String> stringifiedSprites = new ArrayList<>();
        for (final RenderableAnalyser coinDigit : coinDigits) {
            final LetterAnalyser digit = new LetterAnalyser(coinDigit);

            final boolean validDigit =
                    digit.is("1")
                            || digit.is("2")
                            || digit.is("3")
                            || digit.is("5")
                            || digit.is("7")
                            || digit.is("9");
            Assert.assertTrue(
                    "We only expect to see the digits 1,2,3,5,7 or 9 for our coins", validDigit);
            for (final FrameRecord frame : coinDigit.getFrames()) {
                if (!stringifiedSprites.contains(frame.getSprite().toUtfBlockString())) {
                    stringifiedSprites.add(frame.getSprite().toUtfBlockString());
                }
            }
        }
        Assert.assertEquals(
                "should have seen 6 unique digits used for coins across sim lifespan",
                6,
                stringifiedSprites.size());
    }

    // confirm correct number of unique sprites
    // then we go check for at least one instance of each of the numbers we need

    /**
     * Confirm digits for food were changed overtime to the valid values and only the valid values.
     * For simplicity of the test we are not forming if they changed in the correct order.
     */
    @Test
    public void confirmFoodChangedByCabbageCollection() {
        final List<RenderableAnalyser> foodDigits =
                data.filter(
                        "letter",
                        letter ->
                                letter.getFirstFrame().getY() < 50
                                        && new LetterAnalyser(letter).isNumeric());

        final List<String> stringifiedSprites = new ArrayList<>();
        for (final RenderableAnalyser foodDigit : foodDigits) {
            final LetterAnalyser digit = new LetterAnalyser(foodDigit);
            Assert.assertTrue(
                    "Food should only ever have been the digit 3 "
                            + "(pre picking up the cabbage) or 5 after picking up the cabbage",
                    digit.is("3") || digit.is("5"));
            for (final FrameRecord frame : foodDigit.getFrames()) {
                if (!stringifiedSprites.contains(frame.getSprite().toUtfBlockString())) {
                    stringifiedSprites.add(frame.getSprite().toUtfBlockString());
                }
            }
        }

        Assert.assertEquals(
                "Food only changed once so it should only have 2 unique digits displayed",
                2,
                stringifiedSprites.size());
    }
}
