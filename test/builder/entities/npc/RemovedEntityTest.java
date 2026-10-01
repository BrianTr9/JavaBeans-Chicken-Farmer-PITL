package builder.entities.npc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.entities.npc.enemies.Pigeon;
import builder.entities.npc.spawners.PigeonSpawner;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Dirt;

import engine.EngineState;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Entities removed earlier in a frame stay in their collections, marked for removal, until the
 * end-of-frame cleanup. Nothing may treat them as live in the meantime.
 */
public class RemovedEntityTest {

    private GameFixture fixture;
    private EngineState engine;

    @Before
    public void setUp() {
        fixture = new GameFixture(); // 80px tiles
        engine = GameFixture.engine(1);
    }

    private Enemy bird(int x, int y) {
        Enemy bird = new Enemy(x, y);
        bird.setSpeed(0);
        fixture.enemies.addBird(bird);
        return bird;
    }

    private Dirt cabbagePatch(int x, int y, boolean taken) {
        Dirt dirt = new Dirt(x, y);
        dirt.till();
        Cabbage cabbage = new Cabbage(x, y);
        dirt.placeOn(cabbage);
        if (taken) {
            cabbage.markForRemoval();
        }
        fixture.world.place(dirt);
        return dirt;
    }

    // --- Guard bees ------------------------------------------------------------------------

    @Test
    public void oneBeeCatchesOnlyOneBird() {
        Enemy first = bird(400, 400);
        Enemy second = bird(410, 400);
        GuardBee bee = new GuardBee(405, 400, first);

        bee.tick(engine, fixture.game);

        assertTrue(bee.isMarkedForRemoval());
        assertEquals("both birds were within reach, but a bee is used up by one",
                1, (first.isMarkedForRemoval() ? 1 : 0) + (second.isMarkedForRemoval() ? 1 : 0));
    }

    @Test
    public void beesIgnoreBirdsAlreadyCaught() {
        Enemy caught = bird(300, 300);
        caught.markForRemoval();
        bird(600, 600);
        GuardBee bee = new GuardBee(290, 290, caught);

        bee.tick(engine, fixture.game);

        assertFalse("must not collide with a bird another bee already caught",
                bee.isMarkedForRemoval());
        assertEquals("heads for the live bird instead", 45, bee.getDirection());
    }

    @Test
    public void hiveAimsAtTheNearestLiveBirdInRange() {
        BeeHive hive = new BeeHive(400, 400);
        Enemy farFirstInList = bird(400, 600);   // 200px, straight down
        Enemy nearest = bird(450, 400);          // 50px, straight right
        Enemy caught = bird(400, 390);           // 10px, but already caught
        caught.markForRemoval();

        Npc bee = hive.checkAndSpawnBee(new ArrayList<>(List.of(farFirstInList, nearest, caught)));

        assertEquals("aimed at the nearest live bird", 0, bee.getDirection());
    }

    @Test
    public void hiveIgnoresBirdsOutOfRangeOrCaught() {
        BeeHive hive = new BeeHive(400, 400);
        Enemy outOfRange = bird(400, 800);
        Enemy caught = bird(400, 410);
        caught.markForRemoval();
        assertNull(hive.checkAndSpawnBee(new ArrayList<>(List.of(outOfRange, caught))));
    }

    // --- Pigeons and cabbages --------------------------------------------------------------

    @Test
    public void pigeonsIgnoreCabbagesAlreadyTaken() {
        Dirt harvested = cabbagePatch(40, 40, true);
        Dirt live = cabbagePatch(600, 600, false);
        Pigeon pigeon = new Pigeon(45, 45, harvested);

        pigeon.tick(engine, fixture.game);

        assertTrue("there was nothing left to steal on the harvested tile",
                pigeon.getAttacking());
        assertSame(live, pigeon.getTrackedTarget());
    }

    @Test
    public void pigeonsAreNotSpawnedForCabbagesAlreadyTaken() {
        cabbagePatch(40, 40, true);
        new PigeonSpawner(0, 0, 1).tick(engine, fixture.game);
        assertTrue(fixture.enemies.getBirds().isEmpty());
    }

    @Test
    public void pigeonsReachingSpawnCountAsHome() {
        Pigeon pigeon = new Pigeon(0, 0);
        pigeon.setAttacking(false);
        pigeon.tick(engine, fixture.game);
        assertTrue(pigeon.isMarkedForRemoval());
        assertTrue(pigeon.hasReturnedHome());
    }

    @Test
    public void aCabbageIsHarvestedOnlyOnce() {
        Cabbage cabbage = new Cabbage(0, 0);
        for (int i = 0; i < 400; i++) {
            cabbage.tick(GameFixture.engine(i));
        }
        cabbage.interact(engine, fixture.game);
        cabbage.interact(engine, fixture.game);
        assertEquals(10 + Cabbage.FOOD_YIELD, fixture.inventory.getFood());
        assertEquals(10 + Cabbage.COIN_YIELD, fixture.inventory.getCoins());
    }

    // --- Enemy stepping --------------------------------------------------------------------

    @Test
    public void birdsRemovedMidFrameTakeNoFurtherSteps() {
        RemovedOnFirstStep bird = new RemovedOnFirstStep();
        fixture.enemies.addBird(bird);
        fixture.enemies.tick(engine, fixture.game);
        assertEquals(1, bird.steps);
    }

    /** Takes two steps per frame and expires on its first. */
    private static class RemovedOnFirstStep extends Enemy {
        private int steps = 0;

        RemovedOnFirstStep() {
            super(0, 0);
        }

        @Override
        public int ticksPerFrame() {
            return 2;
        }

        @Override
        public void tick(EngineState state, GameState game) {
            steps++;
            markForRemoval();
        }
    }
}
