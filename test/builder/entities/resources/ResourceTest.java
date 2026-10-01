package builder.entities.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.inventory.items.Hoe;
import builder.inventory.items.Jackhammer;
import builder.ui.SpriteGallery;

import org.junit.Before;
import org.junit.Test;

/** Growing and harvesting cabbages, and mining ore. */
public class ResourceTest {

    /** Cabbages advance one growth stage every TimerDuration.SHORT (100) ticks. */
    private static final int TICKS_PER_STAGE = 100;

    private GameFixture fixture;

    @Before
    public void setUp() {
        fixture = new GameFixture(); // 10 coins, 10 food
    }

    private static void tick(Cabbage cabbage, int times) {
        for (int i = 0; i < times; i++) {
            cabbage.tick(GameFixture.engine(i));
        }
    }

    // --- Cabbage ---------------------------------------------------------------------------

    @Test
    public void cabbageGrowsThroughEachStage() {
        Cabbage cabbage = new Cabbage(0, 0);
        assertSame(SpriteGallery.cabbage.getSprite("default"), cabbage.getSprite());
        String[] stages = {"budding", "growing", "grown", "collectable"};
        for (String stage : stages) {
            tick(cabbage, TICKS_PER_STAGE - 1);
            assertFalse("should not advance early", cabbage.getSprite()
                    == SpriteGallery.cabbage.getSprite(stage));
            tick(cabbage, 1);
            assertSame(SpriteGallery.cabbage.getSprite(stage), cabbage.getSprite());
        }
    }

    @Test
    public void cabbageStaysCollectable() {
        Cabbage cabbage = new Cabbage(0, 0);
        tick(cabbage, TICKS_PER_STAGE * 10);
        assertSame(SpriteGallery.cabbage.getSprite("collectable"), cabbage.getSprite());
    }

    @Test
    public void unripeCabbageCannotBeHarvested() {
        Cabbage cabbage = new Cabbage(0, 0);
        tick(cabbage, TICKS_PER_STAGE * 4 - 1);
        cabbage.interact(GameFixture.engine(0), fixture.game);
        assertFalse(cabbage.isMarkedForRemoval());
        assertEquals(10, fixture.inventory.getFood());
        assertEquals(10, fixture.inventory.getCoins());
    }

    @Test
    public void ripeCabbageIsHarvestedForFoodAndCoins() {
        Cabbage cabbage = new Cabbage(0, 0);
        tick(cabbage, TICKS_PER_STAGE * 4);
        cabbage.interact(GameFixture.engine(0), fixture.game);
        assertTrue(cabbage.isMarkedForRemoval());
        assertEquals(12, fixture.inventory.getFood());
        assertEquals(13, fixture.inventory.getCoins());
    }

    @Test
    public void cabbagesGrowIndependently() {
        Cabbage early = new Cabbage(0, 0);
        tick(early, TICKS_PER_STAGE * 2);
        Cabbage late = new Cabbage(0, 0);
        tick(early, TICKS_PER_STAGE * 2);
        tick(late, TICKS_PER_STAGE * 2);
        assertSame(SpriteGallery.cabbage.getSprite("collectable"), early.getSprite());
        assertSame(SpriteGallery.cabbage.getSprite("growing"), late.getSprite());
    }

    // --- Ore -------------------------------------------------------------------------------

    private void mine(Ore ore, int frame) {
        ore.use(GameFixture.engine(frame), fixture.game);
        ore.tick(GameFixture.engine(frame));
    }

    @Test
    public void jackhammerMinesPlayerDamageEveryFifthTick() {
        Ore ore = new Ore(0, 0);
        fixture.hold(new Jackhammer());
        mine(ore, 1);
        mine(ore, 4);
        assertEquals("off-beat ticks mine nothing", 10, fixture.inventory.getCoins());
        mine(ore, 5);
        assertEquals(10 + fixture.player.getDamage(), fixture.inventory.getCoins());
    }

    @Test
    public void oreIsWorthTenCoinsInTotal() {
        Ore ore = new Ore(0, 0);
        fixture.hold(new Jackhammer());
        for (int frame = 0; frame <= 100; frame += 5) {
            mine(ore, frame);
        }
        assertEquals(20, fixture.inventory.getCoins());
    }

    @Test
    public void oreSpriteReflectsRemainingValue() {
        Ore ore = new Ore(0, 0);
        fixture.hold(new Jackhammer());
        ore.tick(GameFixture.engine(0));
        assertSame(SpriteGallery.rock.getSprite("default"), ore.getSprite());

        mine(ore, 0); // 8 of 10 left
        assertSame(SpriteGallery.rock.getSprite("damaged"), ore.getSprite());

        for (int frame = 5; frame <= 15; frame += 5) {
            mine(ore, frame); // 2 left after three more hits
        }
        assertSame(SpriteGallery.rock.getSprite("damaged"), ore.getSprite());

        mine(ore, 20); // depleted
        assertSame(SpriteGallery.rock.getSprite("depleted"), ore.getSprite());
    }

    @Test
    public void otherToolsDoNotMine() {
        Ore ore = new Ore(0, 0);
        fixture.hold(new Hoe());
        mine(ore, 5);
        fixture.hold(null);
        mine(ore, 10);
        assertEquals(10, fixture.inventory.getCoins());
    }
}
