package builder.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Dirt;
import builder.entities.tiles.Grass;
import builder.entities.tiles.Water;
import builder.inventory.items.Bucket;
import builder.inventory.items.Hoe;

import engine.art.sprites.Sprite;
import engine.game.Direction;
import engine.renderer.Renderable;

import org.junit.Before;
import org.junit.Test;

import scenarios.mocks.MockEngineState;
import scenarios.mocks.MockKeys;
import scenarios.mocks.MockMouse;

import java.util.List;

/** The chicken farmer and the controls handled by {@link PlayerManager}. */
public class PlayerTest {

    private static final int CENTRE = 120; // centre of the second tile

    private PlayerManager manager;
    private Player player;
    private GameFixture fixture;

    @Before
    public void setUp() {
        manager = new PlayerManager(CENTRE, CENTRE);
        player = manager.getPlayer();
        fixture = new GameFixture(player);
    }

    private static MockEngineState input(boolean click, Character... keys) {
        return new MockEngineState(GameFixture.DIMENSIONS,
                new MockMouse(0, 0, click, false, false), new MockKeys(List.of(keys)), 0);
    }

    // --- ChickenFarmer ---------------------------------------------------------------------

    @Test
    public void farmerDealsTwoDamage() {
        assertEquals(2, new ChickenFarmer(0, 0).getDamage());
    }

    @Test
    public void moveShiftsPositionInEachDirection() {
        ChickenFarmer farmer = new ChickenFarmer(100, 100);
        farmer.move(Direction.NORTH, 3);
        assertEquals(97, farmer.getY());
        farmer.move(Direction.SOUTH, 5);
        assertEquals(102, farmer.getY());
        farmer.move(Direction.EAST, 4);
        assertEquals(104, farmer.getX());
        farmer.move(Direction.WEST, 10);
        assertEquals(94, farmer.getX());
    }

    @Test
    public void eachDirectionHasItsOwnWalkingAnimation() {
        ChickenFarmer farmer = new ChickenFarmer(100, 100);
        Sprite[] sprites = new Sprite[4];
        Direction[] directions = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        };
        for (int i = 0; i < directions.length; i++) {
            farmer.move(directions[i], 1);
            sprites[i] = farmer.getSprite();
        }
        for (int i = 0; i < sprites.length; i++) {
            for (int j = i + 1; j < sprites.length; j++) {
                assertNotSame(sprites[i], sprites[j]);
            }
        }
    }

    @Test
    public void standingStillFacesTheLastDirectionMoved() {
        ChickenFarmer farmer = new ChickenFarmer(100, 100);
        farmer.tick(GameFixture.engine(0));
        assertSame(builder.ui.SpriteGallery.chickenFarmer.getSprite("down"), farmer.getSprite());

        String[] facing = {"up", "down", "right", "left"};
        Direction[] directions = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        };
        for (int i = 0; i < directions.length; i++) {
            farmer.move(directions[i], 1);
            farmer.tick(GameFixture.engine(i + 1)); // next frame, no movement
            assertSame(builder.ui.SpriteGallery.chickenFarmer.getSprite(facing[i]),
                    farmer.getSprite());
        }
    }

    @Test
    public void usingAnItemWithAnAnimationShowsIt() {
        ChickenFarmer farmer = new ChickenFarmer(100, 100);
        Bucket bucket = new Bucket();
        farmer.use(bucket);
        assertSame(bucket.useAnimation().get(), farmer.getSprite());
    }

    @Test
    public void usingNothingOrAnItemWithoutAnimationKeepsSprite() {
        ChickenFarmer farmer = new ChickenFarmer(100, 100);
        Sprite before = farmer.getSprite();
        farmer.use(null);
        farmer.use(new Hoe());
        assertSame(before, farmer.getSprite());
    }

    // --- PlayerManager ---------------------------------------------------------------------

    @Test
    public void managerRendersOnlyThePlayer() {
        List<Renderable> rendered = manager.render();
        assertEquals(List.of((Renderable) player), rendered);
    }

    @Test
    public void wasdMovesOnePixelPerTick() {
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals(CENTRE - 1, player.getY());
        manager.tick(input(false, 's'), fixture.game);
        assertEquals(CENTRE, player.getY());
        manager.tick(input(false, 'a'), fixture.game);
        assertEquals(CENTRE - 1, player.getX());
        manager.tick(input(false, 'd'), fixture.game);
        assertEquals(CENTRE, player.getX());
    }

    @Test
    public void noKeysMeansNoMovement() {
        manager.tick(input(false), fixture.game);
        assertEquals(CENTRE, player.getX());
        assertEquals(CENTRE, player.getY());
    }

    // The player stands on the tile under their feet: the sprite's bottom pixel row
    // (y + 39 with 80px tiles). For collisions the feet are the box x - 20 .. x + 19 by
    // y + 20 .. y + 39: the middle half of the sprite's columns and its bottom quarter.

    private void walkTo(int x, int y) {
        ((ChickenFarmer) player).setX(x);
        ((ChickenFarmer) player).setY(y);
    }

    @Test
    public void upperBodyMayOverlapWaterAboveTheFeet() {
        fixture.world.place(new Water(CENTRE, CENTRE - 80)); // row 0
        fixture.world.place(new Grass(CENTRE, CENTRE));      // row 1
        walkTo(CENTRE, 80); // sprite spans y 40..119, feet on row 1
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals("head over the water is fine while the feet stay on grass",
                79, player.getY());
    }

    @Test
    public void feetCannotStepOntoWaterAbove() {
        fixture.world.place(new Water(CENTRE, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE));
        walkTo(CENTRE, 60); // top of the feet at y 80, the top row of the grass tile
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals("the next step would put the feet in the water", 60, player.getY());
    }

    @Test
    public void feetCannotStepOntoWaterBelow() {
        fixture.world.place(new Grass(CENTRE, CENTRE));
        fixture.world.place(new Water(CENTRE, CENTRE + 80)); // row 2
        walkTo(CENTRE, 120); // feet at y 159, the bottom row of the grass tile
        manager.tick(input(false, 's'), fixture.game);
        assertEquals(120, player.getY());
    }

    @Test
    public void eitherEndOfTheFeetIsBlockedByWaterAtTheSide() {
        fixture.world.place(new Water(CENTRE - 80, CENTRE)); // column 0
        fixture.world.place(new Grass(CENTRE, CENTRE));      // column 1
        walkTo(100, CENTRE); // left end of the feet at x 80, the edge of the water
        manager.tick(input(false, 'a'), fixture.game);
        assertEquals(100, player.getX());

        fixture.world.place(new Water(CENTRE + 80, CENTRE)); // column 2
        walkTo(139, CENTRE);
        manager.tick(input(false, 'd'), fixture.game);
        assertEquals("feet span x 120..159 at x 140, still on the grass", 140, player.getX());
        manager.tick(input(false, 'd'), fixture.game);
        assertEquals("one more step would put the right foot in the water", 140, player.getX());
    }

    @Test
    public void walkingAlongTheShoreKeepsBothFeetOnLand() {
        // Water in column 0 of row 0; the left foot would enter it while walking up.
        fixture.world.place(new Water(CENTRE - 80, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE));
        walkTo(90, 60); // left foot at x 70 (column 0), feet at the top of row 1
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals(60, player.getY());
    }

    @Test
    public void legsStayOutOfWaterWhenWalkingUp() {
        fixture.world.place(new Water(CENTRE, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE));
        walkTo(CENTRE, 100);
        for (int i = 0; i < 100; i++) {
            manager.tick(input(false, 'w'), fixture.game);
        }
        int legsTop = player.getY() + 20;
        assertEquals("the whole foot box stops on the grass", 80, legsTop);
    }

    @Test
    public void walkableTilesDoNotBlock() {
        fixture.world.place(new Grass(CENTRE, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE));
        walkTo(CENTRE, 41);
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals(40, player.getY());
    }

    @Test
    public void movingNearTheWorldEdgeIsSafe() {
        walkTo(5, 10); // feet extend past the left edge of the world
        manager.tick(input(false, 'a'), fixture.game);
        manager.tick(input(false, 'd'), fixture.game);
        assertEquals(5, player.getX());
    }

    @Test
    public void toolsAffectTheTileUnderTheFeet() {
        Grass bodyTile = new Grass(CENTRE, CENTRE);      // row 1: where the sprite's centre is
        Dirt feetTile = new Dirt(CENTRE, CENTRE + 80);   // row 2: where the feet are
        fixture.world.place(bodyTile);
        fixture.world.place(feetTile);
        fixture.hold(new Hoe());
        walkTo(CENTRE, 140); // centre in row 1, feet at y 179 in row 2

        manager.tick(input(true), fixture.game);

        assertTrue(feetTile.isTilled());
        assertTrue(!bodyTile.isMarkedForRemoval());
    }

    @Test
    public void clickingUsesHeldToolOnTileUnderPlayer() {
        Dirt dirt = new Dirt(CENTRE, CENTRE);
        fixture.world.place(dirt);
        fixture.hold(new Hoe());

        manager.tick(input(false), fixture.game);
        assertTrue("no click, no use", !dirt.isTilled());

        manager.tick(input(true), fixture.game);
        assertTrue(dirt.isTilled());
    }

    @Test
    public void standingOnRipeCabbageHarvestsIt() {
        Dirt dirt = new Dirt(CENTRE, CENTRE);
        Cabbage cabbage = new Cabbage(CENTRE, CENTRE);
        for (int i = 0; i < 400; i++) {
            cabbage.tick(GameFixture.engine(i));
        }
        dirt.placeOn(cabbage);
        fixture.world.place(dirt);

        manager.tick(input(false), fixture.game);

        assertTrue(cabbage.isMarkedForRemoval());
        assertEquals(12, fixture.inventory.getFood());
    }
}
