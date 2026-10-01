package builder.inventory.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.inventory.items.Hoe;
import builder.inventory.items.Jackhammer;
import builder.ui.SpriteGallery;

import engine.renderer.Dimensions;
import engine.renderer.Renderable;
import engine.ui.Icon;
import engine.ui.Letter;

import org.junit.Before;
import org.junit.Test;

import scenarios.mocks.MockEngineState;
import scenarios.mocks.MockKeys;
import scenarios.mocks.MockMouse;

import java.util.List;

/** The inventory bar and the coin/food display. */
public class OverlayTest {

    private static final Dimensions DIMENSIONS = GameFixture.DIMENSIONS; // 800px, 80px tiles
    private static final int SLOTS = 5;

    private GameFixture fixture;
    private InventoryOverlay inventoryOverlay;

    @Before
    public void setUp() {
        fixture = new GameFixture();
        inventoryOverlay = new InventoryOverlay(DIMENSIONS, SLOTS);
    }

    private static MockEngineState pressing(char key) {
        return new MockEngineState(DIMENSIONS, new MockMouse(0, 0, false, false, false),
                new MockKeys(key), 0);
    }

    /** Renderables are the slot borders followed by the slot contents. */
    private List<Renderable> borders() {
        return inventoryOverlay.render().subList(0, SLOTS);
    }

    private List<Renderable> contents() {
        return inventoryOverlay.render().subList(SLOTS, 2 * SLOTS);
    }

    // --- InventoryOverlay layout -----------------------------------------------------------

    @Test
    public void barIsCentredHorizontally() {
        // Sprites are drawn centred on (x, y), so the bar's middle is the mean slot centre.
        int sum = 0;
        for (Renderable border : borders()) {
            sum += border.getX();
        }
        assertEquals(DIMENSIONS.windowSize() / 2, sum / SLOTS);
    }

    @Test
    public void slotsAreOneTileApartOnOneRow() {
        List<Renderable> borders = borders();
        for (int i = 1; i < SLOTS; i++) {
            assertEquals(DIMENSIONS.tileSize(), borders.get(i).getX() - borders.get(i - 1).getX());
            assertEquals(borders.get(0).getY(), borders.get(i).getY());
        }
        assertEquals(DIMENSIONS.windowSize() - DIMENSIONS.tileSize(), borders.get(0).getY());
    }

    @Test
    public void bordersAreDrawnBeneathMatchingContents() {
        List<Renderable> borders = borders();
        List<Renderable> contents = contents();
        for (int i = 0; i < SLOTS; i++) {
            assertEquals(borders.get(i).getX(), contents.get(i).getX());
            assertEquals(borders.get(i).getY(), contents.get(i).getY());
        }
    }

    // --- InventoryOverlay behaviour --------------------------------------------------------

    @Test
    public void onlyTheActiveSlotIsHighlighted() {
        inventoryOverlay.tick(pressing('3'), fixture.game);
        assertEquals(2, fixture.inventory.getActiveSlot());
        List<Renderable> borders = borders();
        for (int i = 0; i < SLOTS; i++) {
            String expected = i == 2 ? "activeborder" : "border";
            assertSame(SpriteGallery.inventory.getSprite(expected), borders.get(i).getSprite());
        }
    }

    @Test
    public void keysBeyondTheBarAreIgnored() {
        inventoryOverlay.tick(pressing('4'), fixture.game);
        inventoryOverlay.tick(pressing('6'), fixture.game);
        inventoryOverlay.tick(pressing('0'), fixture.game);
        assertEquals(3, fixture.inventory.getActiveSlot());
    }

    @Test
    public void slotsShowTheirItemOrEmpty() {
        Hoe hoe = new Hoe();
        Jackhammer jackhammer = new Jackhammer();
        fixture.inventory.setItem(0, hoe);
        fixture.inventory.setItem(4, jackhammer);
        inventoryOverlay.tick(GameFixture.engine(0), fixture.game);

        List<Renderable> contents = contents();
        assertSame(hoe.inventorySprite(), contents.get(0).getSprite());
        assertSame(jackhammer.inventorySprite(), contents.get(4).getSprite());
        assertSame(SpriteGallery.inventory.getSprite("empty"), contents.get(1).getSprite());
    }

    @Test
    public void slotContentsFollowInventoryChanges() {
        fixture.inventory.setItem(1, new Hoe());
        inventoryOverlay.tick(GameFixture.engine(0), fixture.game);
        fixture.inventory.setItem(1, null);
        inventoryOverlay.tick(GameFixture.engine(1), fixture.game);
        assertSame(SpriteGallery.inventory.getSprite("empty"), contents().get(1).getSprite());
    }

    // --- ResourceOverlay -------------------------------------------------------------------

    /** Counts the letters that make up the food and coin read-outs. */
    private static int letters(List<Renderable> rendered) {
        return (int) rendered.stream().filter(r -> r instanceof Letter).count();
    }

    @Test
    public void resourceOverlayShowsOneLetterPerDigitPlusIcons() {
        ResourceOverlay overlay = new ResourceOverlay(DIMENSIONS);
        fixture.inventory.addFood(2);    // 12 food: two digits
        fixture.inventory.addCoins(-3);  // 7 coins: one digit
        overlay.tick(GameFixture.engine(0), fixture.game);

        List<Renderable> rendered = overlay.render();
        assertEquals(3, letters(rendered));
        assertEquals(2, rendered.stream().filter(r -> r instanceof Icon).count());
    }

    @Test
    public void resourceOverlayUpdatesEveryTick() {
        ResourceOverlay overlay = new ResourceOverlay(DIMENSIONS);
        overlay.tick(GameFixture.engine(0), fixture.game); // 10 food, 10 coins
        assertEquals(4, letters(overlay.render()));
        fixture.inventory.addCoins(990); // 1000 coins
        overlay.tick(GameFixture.engine(1), fixture.game);
        assertEquals(6, letters(overlay.render()));
    }

    @Test
    public void coinsAreShownBelowFood() {
        ResourceOverlay overlay = new ResourceOverlay(DIMENSIONS);
        overlay.tick(GameFixture.engine(0), fixture.game);
        List<Renderable> icons = overlay.render().stream()
                .filter(r -> r instanceof Icon).toList();
        assertSame(SpriteGallery.icons.getSprite("food"), icons.get(0).getSprite());
        assertSame(SpriteGallery.icons.getSprite("material"), icons.get(1).getSprite());
        assertTrue(icons.get(1).getY() > icons.get(0).getY());
    }
}
