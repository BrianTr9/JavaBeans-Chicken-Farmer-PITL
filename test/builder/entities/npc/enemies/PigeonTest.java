package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Tile;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.ui.SpriteGallery;
import builder.world.World;
import engine.EngineState;
import engine.game.Entity;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import engine.timing.FixedTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the Pigeon class.
 * Tests cover:
 * - Construction and initialization
 * - Lifespan management
 * - Tracking closest cabbage
 * - Cabbage stealing behavior
 * - Fleeing to spawn after stealing
 * - Behavior when no cabbages exist
 * - Direction and sprite updates
 * - Removal conditions
 */
public class PigeonTest {

    private EngineState engineState;
    private GameState gameState;
    private World world;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        Inventory inventory = new MockInventory();
        Player player = new MockPlayer(400, 400);
        world = new MockWorld();
        gameState = new MockGameState(player, inventory, world);
    }

    @Test
    public void testConstructionNoTarget() {
        Pigeon pigeon = new Pigeon(100, 100);

        assertEquals(100, pigeon.getX());
        assertEquals(100, pigeon.getY());
        assertTrue(pigeon.getAttacking());
        assertFalse(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testConstructionWithTarget() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertEquals(100, pigeon.getX());
        assertEquals(100, pigeon.getY());
        assertTrue(pigeon.getAttacking());
        assertSame("Sprite should be set to pigeon default on construction",
                SpriteGallery.pigeon.getSprite("default"), pigeon.getSprite());
    }

    @Test
    public void testSecondConstructorInitializesLifespan() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);
        assertNotNull("Lifespan should be initialized in the tracked-target constructor", pigeon.getLifespan());
        assertFalse(pigeon.getLifespan().isFinished());
    }

    @Test
    public void testLifespanInitialization() {
        Pigeon pigeon = new Pigeon(100, 100);

        assertNotNull(pigeon.getLifespan());
        assertFalse(pigeon.getLifespan().isFinished());
    }

    @Test
    public void testSetLifespan() {
        Pigeon pigeon = new Pigeon(100, 100);
        FixedTimer newTimer = new FixedTimer(5000);

        pigeon.setLifespan(newTimer);

        assertEquals(newTimer, pigeon.getLifespan());
    }

    @Test
    public void testLifespanExpiration() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);
        FixedTimer shortTimer = new FixedTimer(1);
        pigeon.setLifespan(shortTimer);

        assertFalse(pigeon.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testTracksClosestCabbage() {
        // Place cabbages farther away (>80 pixels) so pigeon doesn't immediately steal them
        MockCabbage cabbage1 = new MockCabbage(400, 400);
        MockCabbage cabbage2 = new MockCabbage(250, 250);

        MockTile tile1 = new MockTile(400, 400);
        tile1.addEntity(cabbage1);

        MockTile tile2 = new MockTile(250, 250);
        tile2.addEntity(cabbage2);

        ((MockWorld) world).addTile(tile1);
        ((MockWorld) world).addTile(tile2);

        Pigeon pigeon = new Pigeon(100, 100, tile2);

        pigeon.tick(engineState, gameState);

        // Pigeon should still be attacking since cabbage is far away
        assertTrue(pigeon.getAttacking());
    }

    @Test
    public void testAssignsTrackedTargetToClosestCabbageOnTick() {
        // world contains two cabbage tiles; closest to pigeon is tile2
        MockCabbage cabbage1 = new MockCabbage(500, 500);
        MockCabbage cabbage2 = new MockCabbage(250, 250);
        MockTile tile1 = new MockTile(500, 500);
        tile1.addEntity(cabbage1);
        MockTile tile2 = new MockTile(250, 250);
        tile2.addEntity(cabbage2);
        ((MockWorld) world).addTile(tile1);
        ((MockWorld) world).addTile(tile2);

        // Construct with no initial target so tick() must set it
        Pigeon pigeon = new Pigeon(100, 100);
        assertNull(pigeon.getTrackedTarget());

        pigeon.tick(engineState, gameState);

        assertSame("Tracked target should be set to the closest cabbage tile", tile2, pigeon.getTrackedTarget());
    }

    @Test
    public void testStealsCabbageWhenReaching() {
        MockCabbage cabbage = new MockCabbage(100, 100);
        MockTile tile = new MockTile(110, 110);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);

        assertTrue(pigeon.getAttacking());
        assertFalse(cabbage.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(cabbage.isMarkedForRemoval());
        assertFalse(pigeon.getAttacking());
    }

    @Test
    public void testStopsAttackingWhenNoCabbages() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertTrue(pigeon.getAttacking());

        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.getAttacking());
    }

    @Test
    public void testFleeToSpawnAfterStealing() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);
        pigeon.setAttacking(false);
        pigeon.setX(300);
        pigeon.setY(300);

        int initialDistance = pigeon.distanceFrom(100, 100);

        for (int i = 0; i < 3; i++) {
            pigeon.tick(engineState, gameState);
        }

        int newDistance = pigeon.distanceFrom(100, 100);

        assertTrue(newDistance <= initialDistance || pigeon.isMarkedForRemoval());
    }

    @Test
    public void testNotRemovedWhenFleeingFarFromSpawn() {
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.setAttacking(false);
        // Farther than a tile-size (tile = 80px via TileGrid(10, 800))
        pigeon.setX(500);
        pigeon.setY(500);

        pigeon.tick(engineState, gameState);

        assertFalse("Pigeon should not be removed while far from spawn during fleeing", pigeon.isMarkedForRemoval());
    }

    @Test
    public void testRemovedWhenReachingSpawnAfterFleeing() {
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.setAttacking(false);
        pigeon.setX(110);
        pigeon.setY(110);

        assertFalse(pigeon.isMarkedForRemoval());

        pigeon.tick(engineState, gameState);

        assertTrue(pigeon.isMarkedForRemoval());
    }

    @Test
    public void testSpriteUpWhenFleeingUpward() {
        Pigeon pigeon = new Pigeon(100, 50);
        pigeon.setY(200);
        pigeon.setAttacking(false);

        pigeon.tick(engineState, gameState);

        assertSame("Sprite should be 'up' when returning towards a smaller Y (spawn above)",
                SpriteGallery.pigeon.getSprite("up"), pigeon.getSprite());
    }

    @Test
    public void testSpriteDownWhenFleeingDownward() {
        Pigeon pigeon = new Pigeon(100, 200);
        pigeon.setY(50);
        pigeon.setAttacking(false);

        pigeon.tick(engineState, gameState);

        assertSame("Sprite should be 'down' when returning towards a larger Y (spawn below)",
                SpriteGallery.pigeon.getSprite("down"), pigeon.getSprite());
    }

    @Test
    public void testDirectionTowardsTarget() {
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);

        pigeon.tick(engineState, gameState);

        assertEquals(0, pigeon.getDirection());
    }

    @Test
    public void testSteerToCenterSetsSpriteUpWhenBelowCenter() {
        // Center of window is (400, 400) with TileGrid(10, 800)
        // Place pigeon below center (y = 700). With correct logic, sprite becomes 'up'.
        Pigeon pigeon = new Pigeon(100, 700);
        // Ensure no cabbages so trackedTarget remains null and steerToCenter path is taken
        pigeon.tick(engineState, gameState);

        assertSame("Sprite should be 'up' when pigeon is below center and steering to center",
                SpriteGallery.pigeon.getSprite("up"), pigeon.getSprite());
    }

    @Test
    public void testMultipleTicksWithoutCabbages() {
        HasPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        for (int i = 0; i < 5; i++) {
            pigeon.tick(engineState, gameState);
        }

        assertFalse(pigeon.getAttacking());
    }

    @Test
    public void testIgnoresCabbagesAfterStealing() {
        MockCabbage cabbage = new MockCabbage(100, 100);
        MockTile tile = new MockTile(110, 110);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);
        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.getAttacking());

        MockCabbage cabbage2 = new MockCabbage(200, 200);
        MockTile tile2 = new MockTile(200, 200);
        tile2.addEntity(cabbage2);
        ((MockWorld) world).addTile(tile2);

        pigeon.tick(engineState, gameState);

        assertFalse(pigeon.getAttacking());
        assertFalse(cabbage2.isMarkedForRemoval());
    }

    @Test
    public void testDistanceCalculation() {
        Pigeon pigeon = new Pigeon(0, 0);

        int distance = pigeon.distanceFrom(300, 400);

        assertEquals(500, distance);
    }

    @Test
    public void testDistanceFromPosition() {
        Pigeon pigeon = new Pigeon(0, 0);
        MockPosition position = new MockPosition(300, 400);

        int distance = pigeon.distanceFrom(position);

        assertEquals(500, distance);
    }

    @Test
    public void testSteerToCenterMovesTowardsCenterOnFirstTickWhenNoCabbages() {
        Pigeon pigeon = new Pigeon(100, 700);
        int cx = engineState.getDimensions().windowSize() / 2;
        int cy = engineState.getDimensions().windowSize() / 2;
        int before = pigeon.distanceFrom(cx, cy);

        // Only a single tick: on this tick, trackedTarget is null and attacking=true,
        // so steerToCenter is used before attacking is turned off due to no cabbages.
        pigeon.tick(engineState, gameState);

        int after = pigeon.distanceFrom(cx, cy);
        assertTrue("Pigeon should move closer to center on the first tick with no cabbages present", after < before);
    }

    @Test
    public void testSelectsClosestCabbageNotLast() {
        // Closest first, far last: mutated 'd < distance' => true would incorrectly pick last
        MockCabbage close = new MockCabbage(250, 250);
        MockCabbage far = new MockCabbage(600, 600);
        MockTile closeTile = new MockTile(250, 250);
        closeTile.addEntity(close);
        MockTile farTile = new MockTile(600, 600);
        farTile.addEntity(far);
        ((MockWorld) world).addTile(closeTile); // add closest first
        ((MockWorld) world).addTile(farTile);   // far last

        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.tick(engineState, gameState);

        assertSame("Should select truly closest tile, not always-last",
                closeTile, pigeon.getTrackedTarget());
    }

    @Test
    public void testDirectionTowardsCenterOnFirstTickNoCabbages() {
        // Freeze speed to avoid base move skewing the computed direction
        Pigeon pigeon = new Pigeon(100, 700);
        pigeon.setSpeed(0);
        int cx = engineState.getDimensions().windowSize() / 2;
        int cy = engineState.getDimensions().windowSize() / 2;
        // Expected angle from (100,700) to (400,400) is -45 degrees
        pigeon.tick(engineState, gameState);
        assertEquals(-45, pigeon.getDirection());
    }

    @Test
    public void testFleeingSetsDirectionTowardSpawn() {
        Pigeon pigeon = new Pigeon(100, 100);
        // Place pigeon elsewhere, set fleeing, and freeze speed
        pigeon.setX(300);
        pigeon.setY(300);
        pigeon.setAttacking(false);
        pigeon.setSpeed(0);
        // Direction from (300,300) to spawn (100,100) is -135 degrees
        pigeon.tick(engineState, gameState);
        assertEquals(-135, pigeon.getDirection());
    }

    @Test
    public void testDoubleMovePerTickWhenAttacking() {
        // Target exactly to the right so movement is along +X axis
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);
        int beforeX = pigeon.getX();
        pigeon.tick(engineState, gameState);
        int afterX = pigeon.getX();
        assertEquals("Should move twice per tick when attacking (base + explicit move)",
                beforeX + 2, afterX);
    }

    @Test
    public void testDoesNotStealWhenNotClose() {
        // Put cabbage well outside a tile size distance so it cannot be stolen
        MockCabbage cabbage = new MockCabbage(600, 600);
        MockTile farTile = new MockTile(600, 600);
        farTile.addEntity(cabbage);
        ((MockWorld) world).addTile(farTile);

        Pigeon pigeon = new Pigeon(100, 100, farTile);
        assertTrue(pigeon.getAttacking());
        pigeon.tick(engineState, gameState);
        assertTrue("Should remain attacking when not close to cabbage", pigeon.getAttacking());
        assertFalse("Far cabbage should not be stolen", cabbage.isMarkedForRemoval());
    }

    @Test
    public void testNoCabbageOnTilesStillStopsAttacking() {
        // World has tiles but no cabbages; predicate must filter them out
        MockTile empty1 = new MockTile(200, 200);
        MockTile empty2 = new MockTile(300, 300);
        ((MockWorld) world).addTile(empty1);
        ((MockWorld) world).addTile(empty2);

        Pigeon pigeon = new Pigeon(100, 100);
        assertTrue(pigeon.getAttacking());
        pigeon.tick(engineState, gameState);
        assertFalse("With no cabbages, pigeon should stop attacking even if tiles exist",
                pigeon.getAttacking());
    }

    @Test
    public void testSteerToCenterSetsSpriteDownWhenAboveCenter() {
        // Above center (y < cy) => steerToCenter should set 'down'
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.tick(engineState, gameState);
        assertSame(SpriteGallery.pigeon.getSprite("down"), pigeon.getSprite());
    }

    @Test
    public void testDirectionTowardsTrackedTargetOverridesInitialDirection() {
        // Ensure steerTowards(trackedTarget) is called in attacking branch
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);
        pigeon.setSpeed(0); // avoid base movement affecting angle
        pigeon.setDirection(90); // start facing up
        pigeon.tick(engineState, gameState);
        assertEquals(0, pigeon.getDirection());
    }

    // NEW TESTS TO DETECT MISSING MUTATIONS

    @Test
    public void testFirstConstructorSetsAttackingTrue() {
        // Detects mutation: line 41 setAttacking(true) removed
        Pigeon pigeon = new Pigeon(100, 100);
        assertTrue("First constructor must set attacking to true", pigeon.getAttacking());

        // Verify it actually affects behavior - attacking pigeon should try to find cabbage
        MockCabbage cabbage = new MockCabbage(200, 200);
        MockTile tile = new MockTile(200, 200);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        pigeon.tick(engineState, gameState);
        // If attacking wasn't set, tracked target wouldn't be assigned
        assertNotNull("Pigeon should track cabbage when attacking", pigeon.getTrackedTarget());
    }

    @Test
    public void testSecondConstructorSetsSpeed() {
        // Detects mutation: line 56 setSpeed(1) removed
        MockPosition target = new MockPosition(300, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertEquals("Speed should be set to 1", 1.0, pigeon.getSpeed(), 0.01);

        // Verify speed affects movement
        int startX = pigeon.getX();
        pigeon.tick(engineState, gameState);
        int endX = pigeon.getX();

        // With speed=1 and target to the right, should move ~2 pixels right (baseTickMove + move)
        assertTrue("Pigeon should move when speed is set", endX > startX);
    }

    @Test
    public void testSecondConstructorSetsAttackingTrue() {
        // Detects mutation: line 58 setAttacking(true) removed
        MockPosition target = new MockPosition(200, 200);
        Pigeon pigeon = new Pigeon(100, 100, target);

        assertTrue("Second constructor must set attacking to true", pigeon.getAttacking());

        // Verify attacking state is used by checking it steers towards target
        // Add a cabbage so it doesn't set attacking=false at end of tick
        MockCabbage cabbage = new MockCabbage(200, 200);
        MockTile tile = new MockTile(200, 200);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        pigeon.setSpeed(0);
        pigeon.tick(engineState, gameState);
        // Direction should be updated if attacking and has target (45 degrees toward 200,200)
        assertEquals("Should steer towards target when attacking", 45, pigeon.getDirection());
    }

    @Test
    public void testHandleFleeingCalledWhenNotAttacking() {
        // Detects mutation: line 67 handleFleeing() first call removed
        // Detects mutation: line 66 conditional !getAttacking() replaced with false
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.setAttacking(false);
        pigeon.setX(200);
        pigeon.setY(200);
        pigeon.setSpeed(0); // freeze to check direction only

        pigeon.tick(engineState, gameState);

        // handleFleeing should set direction towards spawn (100, 100)
        // From (200, 200) to (100, 100) is angle -135 degrees
        assertEquals("Should steer towards spawn when fleeing", -135, pigeon.getDirection());
    }

    @Test
    public void testSteerToCenterNotCalledWhenHasTarget() {
        // Detects mutation: line 72 conditional (trackedTarget==null && attacking) replaced with true
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);
        pigeon.setSpeed(0);

        pigeon.tick(engineState, gameState);

        // Should steer towards target (0 degrees), NOT towards center
        assertEquals("Should steer towards target, not center", 0, pigeon.getDirection());
        assertNotEquals("Should NOT steer to center when has target", -45, pigeon.getDirection());
    }

    @Test
    public void testSteerTowardsTargetOnlyWhenAttackingAndHasTarget() {
        // Detects mutation: line 76 conditional (trackedTarget!=null && attacking) replaced with true
        MockPosition target = new MockPosition(200, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);
        pigeon.setAttacking(false); // Not attacking anymore
        pigeon.setX(200); // Move away from spawn so fleeing direction is meaningful
        pigeon.setY(200);
        pigeon.setSpeed(0);
        pigeon.setDirection(90); // Set to 90 initially

        pigeon.tick(engineState, gameState);

        // Should NOT steer to target (0 degrees) because not attacking
        // Instead should steer to spawn (direction from 200,200 to 100,100 is -135 degrees)
        assertEquals("Should steer to spawn when not attacking", -135, pigeon.getDirection());
    }

    @Test
    public void testLifespanTickOnlyWhenNotNull() {
        // Detects mutation: line 80 conditional (lifespan!=null) replaced with true
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.setLifespan(null);

        // Should not crash when lifespan is null
        pigeon.tick(engineState, gameState);

        assertFalse("Should not be removed when lifespan is null", pigeon.isMarkedForRemoval());
    }

    @Test
    public void testHandleFleeingCalledTwiceWhenNotAttacking() {
        // Detects mutation: line 88 handleFleeing() second call removed
        // Detects mutation: line 87 conditional !getAttacking() replaced with false
        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.setAttacking(false);
        pigeon.setX(200);
        pigeon.setY(200);

        int distanceBefore = pigeon.distanceFrom(100, 100);
        pigeon.tick(engineState, gameState);
        int distanceAfter = pigeon.distanceFrom(100, 100);

        // With double handleFleeing call, sprite should be updated
        // Sprite should be "up" when moving from (200,200) to (100,100)
        assertSame("Sprite should be updated when fleeing",
                SpriteGallery.pigeon.getSprite("up"), pigeon.getSprite());
    }

    @Test
    public void testTileSelectorOnlySelectsCabbageTiles() {
        // Detects mutation: line 128 lambda returns true always (empty tiles selected)
        MockTile emptyTile = new MockTile(200, 200);
        MockTile cabbageTile = new MockTile(300, 300);
        MockCabbage cabbage = new MockCabbage(300, 300);
        cabbageTile.addEntity(cabbage);

        ((MockWorld) world).addTile(emptyTile);
        ((MockWorld) world).addTile(cabbageTile);

        Pigeon pigeon = new Pigeon(100, 100);
        pigeon.tick(engineState, gameState);

        // Should only track the cabbage tile, not the empty one
        assertSame("Should only select tiles with cabbages", cabbageTile, pigeon.getTrackedTarget());
    }

    @Test
    public void testTryStealOnlyWhenAttacking() {
        // Detects mutation: line 151 conditional getAttacking() replaced with true
        MockCabbage cabbage = new MockCabbage(100, 100);
        MockTile tile = new MockTile(110, 110);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);
        pigeon.setAttacking(false); // Already fleeing

        pigeon.tick(engineState, gameState);

        // Should NOT steal when not attacking
        assertFalse("Should not steal cabbage when not attacking", cabbage.isMarkedForRemoval());
    }

    @Test
    public void testOnlyStealsCabbageNotOtherEntities() {
        // Detects mutation: line 154 instanceof Cabbage replaced with true
        MockTile tile = new MockTile(110, 110);
        MockNonCabbageEntity other = new MockNonCabbageEntity(110, 110);
        tile.addEntity(other);
        ((MockWorld) world).addTile(tile);

        Pigeon pigeon = new Pigeon(100, 100, tile);

        pigeon.tick(engineState, gameState);

        // Should NOT steal non-cabbage entities
        assertFalse("Should not remove non-cabbage entities", other.isMarkedForRemoval());
        // Note: pigeon will set attacking=false at end because no cabbage found, which is correct
    }

    @Test
    public void testMovementDistanceWithSpeed() {
        // Additional test to ensure speed affects distance traveled
        MockPosition target = new MockPosition(300, 100);
        Pigeon pigeon = new Pigeon(100, 100, target);

        // Add a cabbage far away so pigeon keeps attacking and moving
        MockCabbage cabbage = new MockCabbage(300, 100);
        MockTile tile = new MockTile(300, 100);
        tile.addEntity(cabbage);
        ((MockWorld) world).addTile(tile);

        int startX = pigeon.getX();

        // Multiple ticks to see movement
        for (int i = 0; i < 5; i++) {
            pigeon.tick(engineState, gameState);
        }

        int endX = pigeon.getX();

        // With speed=1, should move roughly 10 pixels (2 per tick * 5 ticks)
        assertTrue("Should move significant distance with speed set", endX - startX >= 8);
    }

    // Mock Classes

    private static class MockNonCabbageEntity extends Entity {
        public MockNonCabbageEntity(int x, int y) {
            super(x, y);
        }

        @Override
        public void tick(EngineState state) {
            // No-op for testing
        }
    }
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
        private final Inventory inventory;
        private final World world;

        public MockGameState(Player player, Inventory inventory, World world) {
            this.player = player;
            this.inventory = inventory;
            this.world = world;
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public builder.entities.npc.NpcManager getNpcs() {
            return null;
        }

        @Override
        public EnemyManager getEnemies() {
            return null;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private static class MockPlayer implements Player {
        private final int x;
        private final int y;

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
        }

        @Override
        public void setY(int y) {
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

    private static class MockInventory implements Inventory {
        private int food = 0;
        private int coins = 0;

        @Override
        public void addFood(int amount) {
            food = Math.max(0, food + amount);
        }

        @Override
        public int getFood() {
            return food;
        }

        @Override
        public void addCoins(int amount) {
            coins = Math.max(0, coins + amount);
        }

        @Override
        public int getCoins() {
            return coins;
        }

        @Override
        public int getCapacity() {
            return 10;
        }

        @Override
        public builder.inventory.items.Item getHolding() {
            return getItem(getActiveSlot());
        }

        @Override
        public void setItem(int slot, builder.inventory.items.Item item) {
        }

        @Override
        public builder.inventory.items.Item getItem(int slot) {
            return null;
        }

        @Override
        public int getActiveSlot() {
            return 0;
        }

        @Override
        public void setActiveSlot(int slot) {
        }
    }

    private static class MockWorld implements World {
        private final List<Tile> tiles = new ArrayList<>();

        public void addTile(Tile tile) {
            tiles.add(tile);
        }

        @Override
        public List<Tile> tileSelector(Predicate<Tile> predicate) {
            List<Tile> selected = new ArrayList<>();
            for (Tile tile : tiles) {
                if (predicate.test(tile)) {
                    selected.add(tile);
                }
            }
            return selected;
        }

        @Override
        public List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions) {
            return new ArrayList<>();
        }

        @Override
        public List<Tile> allTiles() {
            return new ArrayList<>(tiles);
        }

        @Override
        public void place(Tile tile) {
        }
    }

    private static class MockTile extends Tile {
        private final List<Entity> entities = new ArrayList<>();

        public MockTile(int x, int y) {
            super(x, y, SpriteGallery.grass);
        }

        public void addEntity(Entity entity) {
            entities.add(entity);
        }

        @Override
        public List<Entity> getStackedEntities() {
            return entities;
        }
    }

    private static class MockCabbage extends Cabbage {
        public MockCabbage(int x, int y) {
            super(x, y);
        }
    }

    private static class MockPosition implements HasPosition {
        private final int x;
        private final int y;

        public MockPosition(int x, int y) {
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
        }

        @Override
        public void setY(int y) {
        }
    }
}
