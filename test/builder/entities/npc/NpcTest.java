package builder.entities.npc;

import builder.GameState;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;
import engine.EngineState;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the Npc base class.
 * Tests cover (focused on mutation detection):
 * - Construction and initialization
 * - Speed and direction management
 * - Movement calculations (cos, sin, rounding)
 * - Distance calculations (sqrt, deltaX/Y)
 * - Steering calculations (atan2, toDegrees)
 * - Null safety in steering
 * - Tick behavior
 */
public class NpcTest {

    private EngineState engineState;
    private GameState gameState;
    private TestNpc npc;

    @Before
    public void setUp() {
        Dimensions dimensions = new TileGrid(10, 800);
        engineState = new MockEngineState(dimensions);
        gameState = new MockGameState();
        npc = new TestNpc(100, 100);
    }

    // === Construction Tests ===

    @Test
    public void testConstructionSetsPosition() {
        TestNpc npc = new TestNpc(150, 250);

        assertEquals(150, npc.getX());
        assertEquals(250, npc.getY());
    }

    @Test
    public void testConstructionInitializesDefaultSpeed() {
        TestNpc npc = new TestNpc(100, 100);

        assertEquals("Default speed should be 1.0", 1.0, npc.getSpeed(), 0.001);
    }

    @Test
    public void testConstructionInitializesDefaultDirection() {
        TestNpc npc = new TestNpc(100, 100);

        assertEquals("Default direction should be 0", 0, npc.getDirection());
    }

    // === Speed Tests (Mutation Detection) ===

    @Test
    public void testSetSpeedChangesSpeed() {
        npc.setSpeed(5);

        assertEquals(5.0, npc.getSpeed(), 0.001);
    }

    @Test
    public void testSetSpeedAffectsMovement() {
        // Detects mutation: speed used in movement calculation
        npc.setSpeed(5);
        npc.setDirection(0); // Move right
        int startX = npc.getX();

        npc.move();

        int endX = npc.getX();
        assertEquals("Should move 5 pixels with speed=5", 5, endX - startX);
    }

    @Test
    public void testSpeedZeroNoMovement() {
        // Detects mutation: speed=0 should not move
        npc.setSpeed(0);
        npc.setDirection(0);
        int startX = npc.getX();
        int startY = npc.getY();

        npc.move();

        assertEquals("X should not change with speed=0", startX, npc.getX());
        assertEquals("Y should not change with speed=0", startY, npc.getY());
    }

    // === Direction Tests (Mutation Detection) ===

    @Test
    public void testSetDirectionChangesDirection() {
        npc.setDirection(90);

        assertEquals(90, npc.getDirection());
    }

    @Test
    public void testDirectionAffectsMovementAngle() {
        // Detects mutation: direction used in cos/sin calculations
        npc.setSpeed(1);
        npc.setDirection(90); // Move down (90 degrees)
        int startY = npc.getY();

        npc.move();

        int endY = npc.getY();
        assertEquals("Should move down with direction=90", 1, endY - startY);
    }

    @Test
    public void testDirection180MovesLeft() {
        // Detects mutation: cos(180) should be negative
        npc.setSpeed(1);
        npc.setDirection(180); // Move left
        int startX = npc.getX();

        npc.move();

        int endX = npc.getX();
        assertEquals("Should move left with direction=180", -1, endX - startX);
    }

    @Test
    public void testDirection270MovesUp() {
        // Detects mutation: sin(270) should be negative
        npc.setSpeed(1);
        npc.setDirection(270); // Move up
        int startY = npc.getY();

        npc.move();

        int endY = npc.getY();
        assertEquals("Should move up with direction=270", -1, endY - startY);
    }

    @Test
    public void testDirection45MovesDiagonal() {
        // Detects mutation: both cos and sin affect diagonal movement
        npc.setSpeed(1);
        npc.setDirection(45); // Move diagonally (right-down)
        int startX = npc.getX();
        int startY = npc.getY();

        npc.move();

        int endX = npc.getX();
        int endY = npc.getY();

        // cos(45) ≈ 0.707, sin(45) ≈ 0.707, rounded = 1
        assertTrue("X should increase", endX > startX);
        assertTrue("Y should increase", endY > startY);
    }

    // === Movement Tests (Mutation Detection) ===

    @Test
    public void testMoveUpdatesPositionBasedOnDirectionAndSpeed() {
        npc.setSpeed(3);
        npc.setDirection(0); // Move right
        int startX = npc.getX();

        npc.move();

        assertEquals("Should move 3 pixels right", 3, npc.getX() - startX);
    }

    @Test
    public void testMoveUsesRounding() {
        // Detects mutation: Math.round must be used, not truncate/ceil
        npc.setSpeed(2);
        npc.setDirection(30); // cos(30)≈0.866, sin(30)=0.5
        int startX = npc.getX();
        int startY = npc.getY();

        npc.move();

        // cos(30)*2 ≈ 1.732 rounds to 2
        // sin(30)*2 = 1.0 rounds to 1
        assertEquals("DeltaX should be rounded", 2, npc.getX() - startX);
        assertEquals("DeltaY should be rounded", 1, npc.getY() - startY);
    }

    @Test
    public void testMoveDeltaXUsesCosineDegrees() {
        // Detects mutation: must convert to radians before cos
        npc.setSpeed(10);
        npc.setDirection(0); // cos(0) = 1
        int startX = npc.getX();

        npc.move();

        assertEquals("cos(0)*10 should be 10", 10, npc.getX() - startX);
    }

    @Test
    public void testMoveDeltaYUsesSineDegrees() {
        // Detects mutation: must convert to radians before sin
        npc.setSpeed(10);
        npc.setDirection(90); // sin(90) = 1
        int startY = npc.getY();

        npc.move();

        assertEquals("sin(90)*10 should be 10", 10, npc.getY() - startY);
    }

    @Test
    public void testMultipleMovesAccumulate() {
        npc.setSpeed(2);
        npc.setDirection(0);
        int startX = npc.getX();

        npc.move();
        npc.move();
        npc.move();

        assertEquals("Three moves should accumulate", 6, npc.getX() - startX);
    }

    // === Distance Tests (Mutation Detection) ===

    @Test
    public void testDistanceFromPosition() {
        // Detects mutation: sqrt, deltaX, deltaY calculations
        npc.setX(0);
        npc.setY(0);
        MockPosition target = new MockPosition(300, 400);

        int distance = npc.distanceFrom(target);

        // sqrt(300^2 + 400^2) = sqrt(250000) = 500
        assertEquals("Distance should use Pythagorean theorem", 500, distance);
    }

    @Test
    public void testDistanceFromCoordinates() {
        npc.setX(0);
        npc.setY(0);

        int distance = npc.distanceFrom(300, 400);

        assertEquals("Distance should be 500", 500, distance);
    }

    @Test
    public void testDistanceZeroWhenSamePosition() {
        npc.setX(100);
        npc.setY(100);

        int distance = npc.distanceFrom(100, 100);

        assertEquals("Distance should be 0 when at same position", 0, distance);
    }

    @Test
    public void testDistanceSymmetric() {
        // Detects mutation: deltaX/deltaY sign shouldn't matter
        npc.setX(100);
        npc.setY(100);

        int dist1 = npc.distanceFrom(200, 200);

        npc.setX(200);
        npc.setY(200);
        int dist2 = npc.distanceFrom(100, 100);

        assertEquals("Distance should be symmetric", dist1, dist2);
    }

    @Test
    public void testDistanceUsesSquareRoot() {
        // Detects mutation: must use sqrt, not just sum
        npc.setX(0);
        npc.setY(0);

        int distance = npc.distanceFrom(3, 4);

        assertEquals("Distance 3-4-5 triangle should be 5", 5, distance);
    }

    @Test
    public void testDistanceCastToInt() {
        // Detects mutation: result must be cast to int
        npc.setX(0);
        npc.setY(0);

        int distance = npc.distanceFrom(5, 5);

        // sqrt(50) ≈ 7.07, cast to int = 7
        assertEquals("Distance should be truncated to int", 7, distance);
    }

    // === Steering Tests (Mutation Detection) ===

    @Test
    public void testSteerTowardsPositionSetsDirection() {
        npc.setX(0);
        npc.setY(0);
        MockPosition target = new MockPosition(100, 0);

        npc.callSteerTowards(target);

        assertEquals("Should steer to 0 degrees (right)", 0, npc.getDirection());
    }

    @Test
    public void testSteerTowardsCoordinatesSetsDirection() {
        npc.setX(0);
        npc.setY(0);

        npc.callSteerTowards(0, 100);

        assertEquals("Should steer to 90 degrees (down)", 90, npc.getDirection());
    }

    @Test
    public void testSteerTowardsUsesAtan2() {
        // Detects mutation: atan2(deltaY, deltaX) order matters
        npc.setX(0);
        npc.setY(0);

        npc.callSteerTowards(100, 100);

        assertEquals("atan2(100, 100) should be 45 degrees", 45, npc.getDirection());
    }

    @Test
    public void testSteerTowardsToDegrees() {
        // Detects mutation: must convert from radians to degrees
        npc.setX(0);
        npc.setY(0);

        npc.callSteerTowards(-100, 0);

        assertEquals("Should steer to 180 degrees (left)", 180, npc.getDirection());
    }

    @Test
    public void testSteerTowardsNullTargetNoOp() {
        // Detects mutation: null check must be present
        npc.setDirection(45);

        npc.callSteerTowards((HasPosition) null);

        assertEquals("Direction should not change when target is null", 45, npc.getDirection());
    }

    @Test
    public void testSteerTowardsNegativeAngles() {
        // Detects mutation: atan2 can return negative angles
        npc.setX(0);
        npc.setY(0);

        npc.callSteerTowards(100, -100);

        assertEquals("Should steer to -45 degrees (up-right)", -45, npc.getDirection());
    }

    @Test
    public void testSteerTowardsCalculatesDeltaFromCurrentPosition() {
        // Detects mutation: must use current position in calculation
        npc.setX(50);
        npc.setY(50);

        npc.callSteerTowards(150, 50);

        assertEquals("Should steer to 0 degrees (right)", 0, npc.getDirection());
    }

    // === Tick Tests ===

    @Test
    public void testTickCallsMove() {
        npc.setSpeed(5);
        npc.setDirection(0);
        int startX = npc.getX();

        npc.tick(engineState);

        assertEquals("Tick should call move()", 5, npc.getX() - startX);
    }

    @Test
    public void testTickWithGameStateCallsMove() {
        npc.setSpeed(5);
        npc.setDirection(0);
        int startX = npc.getX();

        npc.tick(engineState, gameState);

        assertEquals("Tick with GameState should call move()", 5, npc.getX() - startX);
    }

    @Test
    public void testInteractIsNoOp() {
        // Just verify it doesn't crash
        npc.interact(engineState, gameState);
        // No assertion needed - just testing it doesn't throw
    }

    // === Integration Tests (Multiple Operations) ===

    @Test
    public void testSteerThenMove() {
        npc.setX(0);
        npc.setY(0);
        npc.setSpeed(10);

        npc.callSteerTowards(100, 0);
        npc.move();

        assertEquals("Should move toward target after steering", 10, npc.getX());
    }

    @Test
    public void testMultipleSteerAndMove() {
        npc.setX(0);
        npc.setY(0);
        npc.setSpeed(5);

        // Move right
        npc.callSteerTowards(100, 0);
        npc.move();
        assertEquals(5, npc.getX());

        // Move down
        npc.callSteerTowards(5, 100);
        npc.move();
        assertTrue("Y should increase", npc.getY() > 0);
    }

    // === Test Helper Class ===

    /**
     * Concrete test implementation of Npc that exposes protected methods for testing.
     */
    private static class TestNpc extends Npc {
        public TestNpc(int x, int y) {
            super(x, y);
        }

        // Expose protected steerTowards methods for testing
        public void callSteerTowards(HasPosition target) {
            super.steerTowards(target);
        }

        public void callSteerTowards(int x, int y) {
            super.steerTowards(x, y);
        }
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
        @Override
        public World getWorld() {
            return null;
        }

        @Override
        public builder.entities.npc.NpcManager getNpcs() {
            return null;
        }

        @Override
        public builder.entities.npc.enemies.EnemyManager getEnemies() {
            return null;
        }

        @Override
        public Player getPlayer() {
            return null;
        }

        @Override
        public Inventory getInventory() {
            return null;
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

