package builder.entities.tiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.entities.npc.BeeHive;
import builder.entities.npc.Scarecrow;
import builder.entities.resources.Cabbage;
import builder.entities.resources.Ore;
import builder.inventory.items.Bucket;
import builder.inventory.items.HiveHammer;
import builder.inventory.items.Hoe;
import builder.inventory.items.Jackhammer;
import builder.inventory.items.Pole;
import builder.ui.SpriteGallery;

import engine.game.Entity;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/** What each tile does when the player uses a tool on it. */
public class TileBehaviourTest {

    private static final int X = GameFixture.FIRST_TILE;
    private static final int Y = GameFixture.FIRST_TILE;

    private GameFixture fixture;

    @Before
    public void setUp() {
        fixture = new GameFixture(); // 10 coins, 10 food
    }

    private void use(Tile tile) {
        tile.use(GameFixture.engine(0), fixture.game);
    }

    // --- TileFactory ---------------------------------------------------------------------

    @Test
    public void factoryBuildsEachSymbol() {
        assertTrue(TileFactory.fromSymbol(X, Y, 'g') instanceof Grass);
        assertTrue(TileFactory.fromSymbol(X, Y, 'w') instanceof Water);
        assertTrue(TileFactory.fromSymbol(X, Y, 'o') instanceof OreVein);
        Tile dirt = TileFactory.fromSymbol(X, Y, 'd');
        assertTrue(dirt instanceof Dirt);
        assertFalse(((Dirt) dirt).isTilled());
        Tile tilled = TileFactory.fromSymbol(X, Y, 't');
        assertTrue(((Dirt) tilled).isTilled());
    }

    @Test
    public void factoryPlacesTileAtGivenPosition() {
        Tile tile = TileFactory.fromSymbol(120, 200, 'g');
        assertEquals(120, tile.getX());
        assertEquals(200, tile.getY());
    }

    @Test(expected = IllegalArgumentException.class)
    public void factoryRejectsUnknownSymbol() {
        TileFactory.fromSymbol(X, Y, 'x');
    }

    @Test(expected = IllegalArgumentException.class)
    public void factorySymbolsAreCaseSensitive() {
        TileFactory.fromSymbol(X, Y, 'G');
    }

    // --- OreVein ---------------------------------------------------------------------------

    @Test
    public void oreVeinStartsWithItsOre() {
        OreVein vein = new OreVein(X, Y);
        Ore ore = vein.getOre();
        assertEquals(List.of((Entity) ore), vein.getStackedEntities());
        assertEquals(X, ore.getX());
        assertEquals(Y, ore.getY());
    }

    // --- Dirt ------------------------------------------------------------------------------

    @Test
    public void hoeTillsDirt() {
        Dirt dirt = new Dirt(X, Y);
        fixture.hold(new Hoe());
        use(dirt);
        assertTrue(dirt.isTilled());
        assertSame(SpriteGallery.tilled.getSprite("default"), dirt.getSprite());
    }

    @Test
    public void bucketPlantsCabbageOnTilledDirtForTwoCoins() {
        Dirt dirt = new Dirt(X, Y);
        dirt.till();
        fixture.hold(new Bucket());
        use(dirt);
        assertEquals(1, dirt.getStackedEntities().size());
        assertTrue(dirt.getStackedEntities().get(0) instanceof Cabbage);
        assertEquals(10 - Cabbage.COST, fixture.inventory.getCoins());
    }

    @Test
    public void bucketDoesNothingOnUntilledDirt() {
        Dirt dirt = new Dirt(X, Y);
        fixture.hold(new Bucket());
        use(dirt);
        assertTrue(dirt.getStackedEntities().isEmpty());
        assertEquals(10, fixture.inventory.getCoins());
    }

    @Test
    public void onlyOneCabbagePerTile() {
        Dirt dirt = new Dirt(X, Y);
        dirt.till();
        fixture.hold(new Bucket());
        use(dirt);
        use(dirt);
        assertEquals(1, dirt.getStackedEntities().size());
        assertEquals(10 - Cabbage.COST, fixture.inventory.getCoins());
    }

    @Test
    public void plantingNeedsEnoughCoins() {
        Dirt dirt = new Dirt(X, Y);
        dirt.till();
        fixture.inventory.addCoins(-9); // 1 coin left
        fixture.hold(new Bucket());
        use(dirt);
        assertTrue(dirt.getStackedEntities().isEmpty());
        assertEquals(1, fixture.inventory.getCoins());
    }

    @Test
    public void poleBuildsScarecrowOnEmptyTilledDirt() {
        Dirt dirt = new Dirt(X, Y);
        dirt.till();
        fixture.hold(new Pole());
        use(dirt);
        assertEquals(1, dirt.getStackedEntities().size());
        assertTrue(dirt.getStackedEntities().get(0) instanceof Scarecrow);
        assertEquals("the scarecrow must also be registered as an NPC",
                1, fixture.npcs.getNpcs().size());
        assertEquals(10 - Scarecrow.COIN_COST, fixture.inventory.getCoins());
    }

    @Test
    public void poleDoesNothingOnUntilledDirtOrWithoutCoins() {
        Dirt untilled = new Dirt(X, Y);
        fixture.hold(new Pole());
        use(untilled);
        assertTrue(untilled.getStackedEntities().isEmpty());

        Dirt tilled = new Dirt(X, Y);
        tilled.till();
        fixture.inventory.addCoins(-9);
        use(tilled);
        assertTrue(tilled.getStackedEntities().isEmpty());
        assertTrue(fixture.npcs.getNpcs().isEmpty());
    }

    // --- Grass -----------------------------------------------------------------------------

    @Test
    public void hoeTurnsGrassIntoDirt() {
        Grass grass = new Grass(X, Y);
        fixture.world.place(grass);
        fixture.hold(new Hoe());
        use(grass);

        assertTrue(grass.isMarkedForRemoval());
        List<Tile> placed = fixture.world.tilesAtPosition(X, Y, GameFixture.DIMENSIONS);
        assertTrue(placed.stream().anyMatch(tile -> tile instanceof Dirt));

        // Once the world ticks, only the new dirt remains at that position.
        fixture.world.tick(GameFixture.engine(1), fixture.game);
        List<Tile> after = fixture.world.tilesAtPosition(X, Y, GameFixture.DIMENSIONS);
        assertEquals(1, after.size());
        assertTrue(after.get(0) instanceof Dirt);
    }

    @Test
    public void grassIsOnlyTurnedOnce() {
        Grass grass = new Grass(X, Y);
        fixture.hold(new Hoe());
        use(grass);
        use(grass);
        long dirtCount = fixture.world.allTiles().stream().filter(t -> t instanceof Dirt).count();
        assertEquals(1, dirtCount);
    }

    @Test
    public void hiveHammerBuildsHiveOnGrass() {
        Grass grass = new Grass(X, Y);
        fixture.hold(new HiveHammer());
        use(grass);
        assertTrue(grass.getStackedEntities().get(0) instanceof BeeHive);
        assertEquals(1, fixture.npcs.getNpcs().size());
        assertEquals(10 - BeeHive.COIN_COST, fixture.inventory.getCoins());
        assertEquals(10 - BeeHive.FOOD_COST, fixture.inventory.getFood());
    }

    @Test
    public void hiveNeedsBothCoinsAndFood() {
        fixture.hold(new HiveHammer());
        fixture.inventory.addFood(-9); // 10 coins, 1 food
        Grass grass = new Grass(X, Y);
        use(grass);
        assertTrue(grass.getStackedEntities().isEmpty());
        assertEquals(10, fixture.inventory.getCoins());
    }

    @Test
    public void hoeLeavesGrassWithAHiveAlone() {
        Grass grass = new Grass(X, Y);
        fixture.hold(new HiveHammer());
        use(grass);
        fixture.hold(new Hoe());
        use(grass);
        assertFalse(grass.isMarkedForRemoval());
    }

    @Test
    public void wrongToolsDoNothing() {
        Grass grass = new Grass(X, Y);
        Dirt dirt = new Dirt(X, Y);
        fixture.hold(new Jackhammer());
        use(grass);
        use(dirt);
        assertFalse(grass.isMarkedForRemoval());
        assertFalse(dirt.isTilled());
        assertTrue(fixture.world.allTiles().isEmpty());
    }

    @Test
    public void plantEnforcesItsOwnPreconditions() {
        // plant() is also used when seeding a level, outside the bucket's checks.
        Dirt untilled = new Dirt(X, Y);
        assertFalse(untilled.plant(fixture.inventory));
        assertTrue(untilled.getStackedEntities().isEmpty());

        Dirt tilled = new Dirt(X, Y);
        tilled.till();
        assertTrue(tilled.plant(fixture.inventory));
        assertFalse("a second cabbage on the same tile", tilled.plant(fixture.inventory));
        assertEquals(1, tilled.getStackedEntities().size());
        assertEquals(10 - Cabbage.COST, fixture.inventory.getCoins());
    }
}
