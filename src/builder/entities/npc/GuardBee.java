package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

import java.util.ArrayList;

/**
 * A highly trained Guard Bee used as a short-lived projectile that hunts birds.
 */
public class GuardBee extends Npc implements Expirable {

    private final int spawnX;
    private final int spawnY;
    private static final int SPEED = 2;
    private static final SpriteGroup ART = SpriteGallery.bee;
    private FixedTimer lifespan = new FixedTimer(300);
    private final HasPosition trackedTarget;

    /**
     * Construct a GuardBee spawned at the given coordinates that initially tracks the
     * supplied target.
     *
     * @param x horizontal spawning position
     * @param y vertical spawning position
     * @param trackedTarget target with a position we want this to track (initial aim)
     */
    public GuardBee(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
        this.trackedTarget = trackedTarget;

        this.spawnX = x;
        this.spawnY = y;

        double deltaX = trackedTarget.getX() - this.getX();
        double deltaY = trackedTarget.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
        this.setSpeed(GuardBee.SPEED);
    }

    @Override
    public FixedTimer getLifespan() {
        return lifespan;
    }

    @Override
    public void setLifespan(FixedTimer timer) {
        this.lifespan = timer;
    }

    /**
     * Update the sprite used to render the bee based on its current movement direction.
     */
    public void updateArtBasedOnDirection() {
        boolean goingUp = (this.getDirection() >= 230 && this.getDirection() < 310);
        boolean goingDown = (this.getDirection() >= 40 && this.getDirection() < 140);
        boolean goingRight = (this.getDirection() >= 310 && this.getDirection() < 40);
        if (goingDown) {
            this.setSprite(ART.getSprite("down"));
        } else if (goingUp) {
            this.setSprite(ART.getSprite("up"));
        } else if (goingRight) {
            this.setSprite(ART.getSprite("right"));
        } else {
            this.setSprite(ART.getSprite("left"));
        }
    }

    @Override
    public void tick(EngineState state, GameState game) {
        // Keep legacy double-move: one via base, then an explicit second move
        super.tick(state);
        this.move();

        // Determine closest enemy each tick
        Enemy nearest = null;
        int nearestDist = Integer.MAX_VALUE;
        ArrayList<Enemy> enemies = new ArrayList<>(game.getEnemies().getBirds());
        for (Enemy enemy : enemies) {
            int d = this.distanceFrom(enemy);
            if (d < nearestDist) {
                nearest = enemy;
                nearestDist = d;
            }
        }

        if (nearest != null) {
            // Steer towards closest bird
            double dx = nearest.getX() - this.getX();
            double dy = nearest.getY() - this.getY();
            this.setDirection((int) Math.toDegrees(Math.atan2(dy, dx)));
        } else if (this.trackedTarget != null) {
            // No birds in world: continue towards the last known target (as per existing tests)
            double dx = this.trackedTarget.getX() - this.getX();
            double dy = this.trackedTarget.getY() - this.getY();
            this.setDirection((int) Math.toDegrees(Math.atan2(dy, dx)));
        } else {
            // Fallback: steer back to spawn if no target
            double dx = this.spawnX - this.getX();
            double dy = this.spawnY - this.getY();
            this.setDirection((int) Math.toDegrees(Math.atan2(dy, dx)));
        }

        // Collision with any enemy: remove both
        for (Enemy enemy : enemies) {
            if (this.distanceFrom(enemy) < state.getDimensions().tileSize()) {
                enemy.markForRemoval();
                this.markForRemoval();
            }
        }

        this.updateArtBasedOnDirection();
        lifespan.tick();
        if (lifespan.isFinished()) {
            this.markForRemoval();
        }
    }
}
