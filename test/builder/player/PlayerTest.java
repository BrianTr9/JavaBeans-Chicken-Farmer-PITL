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

    @Test
    public void waterBlocksMovement() {
        // Water directly above; the player stands at the top edge of their tile.
        fixture.world.place(new Water(CENTRE, CENTRE - 80));
        fixture.world.place(new Grass(CENTRE, CENTRE));
        ChickenFarmer farmer = (ChickenFarmer) player;
        farmer.setY(80); // first pixel row of the tile below the water

        manager.tick(input(false, 'w'), fixture.game);

        assertEquals("cannot step onto water", 80, player.getY());
    }

    @Test
    public void walkableTilesDoNotBlock() {
        fixture.world.place(new Grass(CENTRE, CENTRE - 80));
        ((ChickenFarmer) player).setY(80);
        manager.tick(input(false, 'w'), fixture.game);
        assertEquals(79, player.getY());
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
