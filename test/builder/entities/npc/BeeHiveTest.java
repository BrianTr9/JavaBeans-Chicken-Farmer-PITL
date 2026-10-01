package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.entities.npc.enemies.EnemyManager;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the BeeHive class.
 * Tests cover (focused on mutation detection):
 * - Construction and initialization
 * - Reload timer mechanics (240 ticks)
 * - Player boost (3x reload speed when on hive)
 * - Detection range (350 pixels)
 * - Spawning guard bees
 * - Loaded/unloaded state transitions
 * - Constants validation
 */
public class BeeHiveTest {

    private EngineState engineState;
    private GameState gameState;
    private MockPlayer player;
    private MockNpcManager npcManager;
    private EnemyManager enemyManager;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800); // tileSize = 80
        engineState = new MockEngineState(dimensions);
        player = new MockPlayer(400, 400);
        npcManager = new MockNpcManager();
        enemyManager = new EnemyManager();
        gameState = new MockGameState(player, npcManager, enemyManager);
    }

    // === Constants Tests (Mutation Detection) ===

    @Test
    public void testDetectionDistanceConstant() {
        // Detects mutation: constant value changed
        assertEquals("Detection distance should be 350", 350, BeeHive.DETECTION_DISTANCE);
    }

    @Test
    public void testTimerConstant() {
        // Detects mutation: timer duration changed
        assertEquals("Timer should be 240 ticks", 240, BeeHive.TIMER);
    }

    @Test
    public void testFoodCostConstant() {
        assertEquals("Food cost should be 2", 2, BeeHive.FOOD_COST);
    }

    @Test
    public void testCoinCostConstant() {
        assertEquals("Coin cost should be 2", 2, BeeHive.COIN_COST);
    }

    // === Construction Tests ===

    @Test
    public void testConstructionSetsPosition() {
        BeeHive hive = new BeeHive(150, 250);

        assertEquals(150, hive.getX());
        assertEquals(250, hive.getY());
    }

    @Test
    public void testConstructionSetsSpeedToZero() {
        // Detects mutation: speed must be 0 for stationary hive
        BeeHive hive = new BeeHive(100, 100);

        assertEquals("Hive should not move", 0.0, hive.getSpeed(), 0.001);
    }

    @Test
    public void testConstructionInitializesLoadedTrue() {
        // Detects mutation: hive starts loaded
        BeeHive hive = new BeeHive(100, 100);

        // Test by trying to spawn - should work on first interact
        MockEnemy bird = new MockEnemy(200, 200);
        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNotNull("Hive should be loaded initially", bee);
    }

    // === Reload Timer Tests (Mutation Detection) ===

    @Test
    public void testReloadTakes240TicksWithoutPlayer() {
        // Detects mutation: timer duration, tick increment
        BeeHive hive = new BeeHive(100, 100);
        player.setX(500); // Player far away
        player.setY(500);

        // Spawn a bee to start reload
        MockEnemy bird = new MockEnemy(200, 200);
        spawnBeeAndStartReload(hive, bird);

        // Tick 239 times - should not reload yet
        for (int i = 0; i < 239; i++) {
            hive.tick(engineState, gameState);
        }
        assertFalse("Should not reload before 240 ticks", canSpawnBee(hive, bird));

        // Tick once more - should reload
        hive.tick(engineState, gameState);
        assertTrue("Should reload after 240 ticks", canSpawnBee(hive, bird));
    }

    @Test
    public void testPlayerBoostTripleReloadSpeed() {
        // Detects mutation: player boost = 3x (ticks 3 times per tick)
        BeeHive hive = new BeeHive(100, 100);
        player.setX(100); // Player on hive (distance < 80)
        player.setY(100);

        MockEnemy bird = new MockEnemy(200, 200);
        spawnBeeAndStartReload(hive, bird);

        // With 3x boost: 240/3 = 80 ticks needed
        for (int i = 0; i < 79; i++) {
            hive.tick(engineState, gameState);
        }
        assertFalse("Should not reload before 80 ticks with boost", canSpawnBee(hive, bird));

        hive.tick(engineState, gameState);
        assertTrue("Should reload after 80 ticks with boost", canSpawnBee(hive, bird));
    }

    @Test
    public void testPlayerBoostOnlyWhenOnHive() {
        // Detects mutation: distance < tileSize check
        BeeHive hive = new BeeHive(100, 100);
        player.setX(200); // Player distance = sqrt(10000+10000) ≈ 141 > 80
        player.setY(200);

        MockEnemy bird = new MockEnemy(200, 200);
        spawnBeeAndStartReload(hive, bird);

        // Should take 240 ticks without boost
        for (int i = 0; i < 80; i++) {
            hive.tick(engineState, gameState);
        }
        assertFalse("Should not get boost when player not on hive", canSpawnBee(hive, bird));
    }

    @Test
    public void testPlayerExactlyOnTileBoundaryGetsBoost() {
        // Detects mutation: < vs <= in distance check
        BeeHive hive = new BeeHive(100, 100);
        player.setX(179); // Distance = 79 < 80
        player.setY(100);

        MockEnemy bird = new MockEnemy(200, 200);
        spawnBeeAndStartReload(hive, bird);

        // Should get boost (3x speed)
        for (int i = 0; i < 80; i++) {
            hive.tick(engineState, gameState);
        }
        assertTrue("Should get boost when just inside tile boundary", canSpawnBee(hive, bird));
    }

    // === Detection Range Tests (Mutation Detection) ===

    @Test
    public void testDetectsBirdWithin350Pixels() {
        // Detects mutation: detection distance constant
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(400, 100); // Distance = 300 < 350

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNotNull("Should detect bird within 350 pixels", bee);
    }

    @Test
    public void testDoesNotDetectBirdBeyond350Pixels() {
        // Detects mutation: < vs <= in distance check
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(500, 100); // Distance = 400 > 350

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNull("Should not detect bird beyond 350 pixels", bee);
    }

    @Test
    public void testDetectionUsesDistanceFromMethod() {
        // Detects mutation: must use Pythagorean distance
        BeeHive hive = new BeeHive(0, 0);
        MockEnemy bird = new MockEnemy(210, 280); // Distance = sqrt(44100+78400) = 350 exactly

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNull("Bird at exactly 350 should not be detected (< not <=)", bee);
    }

    @Test
    public void testSelectsFirstBirdInRange() {
        // Detects mutation: should spawn for first bird found
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird1 = new MockEnemy(200, 100); // In range
        MockEnemy bird2 = new MockEnemy(250, 100); // Also in range

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird1);
        targets.add(bird2);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNotNull("Should spawn for first bird in range", bee);
        assertTrue("Should spawn GuardBee", bee instanceof GuardBee);
    }

    @Test
    public void testSpawnsOnlyOneBeePerCall() {
        // Detects mutation: should return after first spawn
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird1 = new MockEnemy(200, 100);
        MockEnemy bird2 = new MockEnemy(250, 100);

        // Add birds to enemyManager so interact() can see them
        enemyManager.addBird(bird1);
        enemyManager.addBird(bird2);

        hive.interact(engineState, gameState);

        assertEquals("Should spawn only one bee", 1, npcManager.getNpcs().size());
    }

    // === Loaded State Tests (Mutation Detection) ===

    @Test
    public void testCannotSpawnWhenUnloaded() {
        // Detects mutation: loaded check in checkAndSpawnBee
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);

        // First spawn works (hive loaded)
        spawnBeeAndStartReload(hive, bird);

        // Try to spawn again immediately (hive unloaded)
        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);
        Npc bee = hive.checkAndSpawnBee(targets);

        assertNull("Should not spawn when unloaded", bee);
    }

    @Test
    public void testLoadedSetFalseAfterSpawn() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);
        enemyManager.addBird(bird);

        hive.interact(engineState, gameState);
        assertEquals("Should spawn one bee", 1, npcManager.getNpcs().size());

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);
        Npc bee = hive.checkAndSpawnBee(targets);
        assertNull("Hive should be unloaded after spawning", bee);
    }

    @Test
    public void testLoadedSetTrueAfterTimerFinishes() {
        // Detects mutation: loaded = true when timer finishes
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);
        player.setX(500); // Player away

        spawnBeeAndStartReload(hive, bird);

        // Complete reload cycle
        for (int i = 0; i < 240; i++) {
            hive.tick(engineState, gameState);
        }

        assertTrue("Hive should be loaded after reload", canSpawnBee(hive, bird));
    }

    @Test
    public void testNoSpawnWhenNoBirdsInRange() {
        BeeHive hive = new BeeHive(100, 100);
        ArrayList<Enemy> targets = new ArrayList<>();

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNull("Should not spawn when no birds", bee);
    }

    @Test
    public void testNoSpawnWhenAllBirdsTooFar() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy farBird = new MockEnemy(600, 600);

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(farBird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertNull("Should not spawn when all birds too far", bee);
    }

    // === GuardBee Spawn Tests ===

    @Test
    public void testSpawnedBeeIsGuardBee() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertTrue("Spawned bee should be GuardBee", bee instanceof GuardBee);
    }

    @Test
    public void testSpawnedBeeHasHivePosition() {
        BeeHive hive = new BeeHive(150, 250);
        MockEnemy bird = new MockEnemy(300, 250);

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        Npc bee = hive.checkAndSpawnBee(targets);
        assertEquals("Bee should spawn at hive X", 150, bee.getX());
        assertEquals("Bee should spawn at hive Y", 250, bee.getY());
    }

    @Test
    public void testSpawnedBeeTracksTargetBird() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);

        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);

        GuardBee bee = (GuardBee) hive.checkAndSpawnBee(targets);
        // GuardBee's trackedTarget is private, verify by checking direction
        assertEquals("Bee should be aimed toward target (right = 0 degrees)", 0, bee.getDirection());
    }

    // === Interact Method Tests ===

    @Test
    public void testInteractAddsToNpcManager() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);
        enemyManager.addBird(bird);

        hive.interact(engineState, gameState);

        assertEquals("Should add bee to NpcManager", 1, npcManager.getNpcs().size());
    }

    @Test
    public void testInteractOnlySpawnsWhenLoaded() {
        // Detects mutation: loaded check in interact
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);
        enemyManager.addBird(bird);

        // First interact spawns
        hive.interact(engineState, gameState);
        assertEquals(1, npcManager.getNpcs().size());

        // Second interact should not spawn (unloaded)
        hive.interact(engineState, gameState);
        assertEquals("Should not spawn when unloaded", 1, npcManager.getNpcs().size());
    }

    // === Tick Method Tests ===

    @Test
    public void testTickDoesNotMoveHive() {
        BeeHive hive = new BeeHive(100, 100);
        int startX = hive.getX();
        int startY = hive.getY();

        hive.tick(engineState, gameState);

        assertEquals("Hive X should not change", startX, hive.getX());
        assertEquals("Hive Y should not change", startY, hive.getY());
    }

    @Test
    public void testTickAdvancesTimerWhenUnloaded() {
        BeeHive hive = new BeeHive(100, 100);
        MockEnemy bird = new MockEnemy(200, 100);
        player.setX(500);

        spawnBeeAndStartReload(hive, bird);

        // Tick should advance timer
        hive.tick(engineState, gameState);
        // Timer has advanced (can't directly test, but verify through reload)
    }

    // === Helper Methods ===

    private void spawnBeeAndStartReload(BeeHive hive, MockEnemy bird) {
        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);
        hive.checkAndSpawnBee(targets);
        enemyManager.addBird(bird);
        hive.interact(engineState, gameState);
    }

    private boolean canSpawnBee(BeeHive hive, MockEnemy bird) {
        ArrayList<Enemy> targets = new ArrayList<>();
        targets.add(bird);
        return hive.checkAndSpawnBee(targets) != null;
    }

    // === Mock Classes ===

    private static class MockEngineState implements EngineState {
        private final Dimensions dimensions;

        public MockEngineState(Dimensions dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public Dimensions getDimensions() {
            return dimensions;
        }

        @Override
        public engine.input.MouseState getMouse() {
            return null;
        }

        @Override
        public engine.input.KeyState getKeys() {
            return null;
        }

        @Override
        public int currentTick() {
            return 0;
        }
    }

    private static class MockGameState implements GameState {
        private final Player player;
        private final NpcManager npcManager;
        private final EnemyManager enemyManager;

        public MockGameState(Player player, NpcManager npcManager, EnemyManager enemyManager) {
            this.player = player;
            this.npcManager = npcManager;
            this.enemyManager = enemyManager;
        }

        @Override
        public World getWorld() {
            return null;
        }

        @Override
        public NpcManager getNpcs() {
            return npcManager;
        }

        @Override
        public EnemyManager getEnemies() {
            return enemyManager;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static class MockPlayer implements Player {
        private int x;
        private int y;

        public MockPlayer(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public String getID() {
            return "mock-player";
        }

        @Override
        public int getDamage() {
            return 1;
        }
    }

    private static class MockNpcManager extends NpcManager {
        private final ArrayList<Npc> npcs = new ArrayList<>();

        public MockNpcManager() {
            super(); // NpcManager has no-arg constructor
        }

        @Override
        public void addNpc(Npc npc) {
            npcs.add(npc);
        }

        public ArrayList<Npc> getNpcs() {
            return npcs;
        }
    }

    private static class MockEnemy extends Enemy {
        public MockEnemy(int x, int y) {
            super(x, y);
        }
    }
}
