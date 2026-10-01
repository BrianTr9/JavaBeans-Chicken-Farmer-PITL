package builder.entities.npc.enemies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import builder.GameState;
import builder.JavaBeanGameState;
import builder.entities.npc.GuardBee;
import builder.entities.npc.NpcManager;
import builder.inventory.Inventory;
import builder.inventory.TinyInventory;
import builder.player.ChickenFarmer;
import builder.world.WorldBuilder;

import engine.EngineState;
import engine.input.KeyState;
import engine.input.MouseState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import org.junit.Before;
import org.junit.Test;

/**
 * Birds that are removed before reaching their spawn must give back what they stole. These
 * tests drive the managers in the same order as {@code JavaBeanFarm.tick}: NPCs (bees) first,
 * then enemies, then end-of-frame cleanup.
 */
public class StolenGoodsRefundTest {

    private static final Dimensions DIMENSIONS = new TileGrid(10, 800); // tileSize = 80

    private EngineState engine;
    private Inventory inventory;
    private NpcManager npcs;
    private EnemyManager enemies;
    private GameState game;

    @Before
    public void setUp() {
        engine = new FixedEngineState();
        inventory = new TinyInventory(5, 10, 10);
        npcs = new NpcManager();
        enemies = new EnemyManager();
        // Player far away from every bird so no further stealing happens.
        game = new JavaBeanGameState(
                WorldBuilder.empty(), new ChickenFarmer(780, 780), inventory, npcs, enemies);
    }

    private void runFrame() {
        npcs.tick(engine, game);
        enemies.tick(engine, game);
        npcs.interact(engine, game);
        npcs.cleanup();
        enemies.cleanup(game);
    }

    @Test
    public void eagleKilledByBeeReturnsStolenFood() {
        Eagle eagle = new Eagle(0, 0, game.getPlayer());
        eagle.setFood(3);
        eagle.setAttacking(false);
        eagle.setX(400);
        eagle.setY(400);
        enemies.addBird(eagle);
        npcs.addNpc(new GuardBee(410, 410, eagle));

        runFrame();

        assertTrue(enemies.getBirds().isEmpty());
        assertEquals("eagle should return the 3 food it stole", 13, inventory.getFood());
    }

    @Test
    public void magpieKilledByBeeReturnsStolenCoinOnce() {
        Magpie magpie = new Magpie(0, 0, game.getPlayer());
        magpie.setCoins(1);
        magpie.setAttacking(false);
        magpie.setX(400);
        magpie.setY(400);
        enemies.addBird(magpie);
        npcs.addNpc(new GuardBee(410, 410, magpie));

        runFrame();
        runFrame();

        assertTrue(enemies.getBirds().isEmpty());
        assertEquals("magpie should return its coin exactly once", 11, inventory.getCoins());
    }

    @Test
    public void birdThatReachesSpawnKeepsLoot() {
        Magpie magpie = new Magpie(0, 0, game.getPlayer());
        magpie.setCoins(1);
        magpie.setAttacking(false);
        magpie.setX(5);
        magpie.setY(5);
        enemies.addBird(magpie);

        runFrame();
        runFrame();

        assertTrue(enemies.getBirds().isEmpty());
        assertEquals("loot that made it home is not refunded", 10, inventory.getCoins());
    }

    @Test
    public void expiredEagleReturnsStolenFood() {
        Eagle eagle = new Eagle(0, 0, game.getPlayer());
        eagle.setFood(3);
        eagle.setAttacking(false);
        eagle.setX(400);
        eagle.setY(400);
        eagle.setLifespan(new engine.timing.FixedTimer(1));
        enemies.addBird(eagle);

        runFrame();
        runFrame();

        assertTrue(enemies.getBirds().isEmpty());
        assertEquals(13, inventory.getFood());
    }

    /** Engine state with fixed dimensions and no input. */
    private static class FixedEngineState implements EngineState {
        @Override
        public Dimensions getDimensions() {
            return DIMENSIONS;
        }

        @Override
        public KeyState getKeys() {
            return null;
        }

        @Override
        public MouseState getMouse() {
            return null;
        }

        @Override
        public int currentTick() {
            return 0;
        }
    }
}
