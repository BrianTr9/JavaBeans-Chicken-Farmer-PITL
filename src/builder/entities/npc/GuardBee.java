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

    /**
     * Construct a GuardBee spawned at the given coordinates, initially aimed at the supplied
     * target. Each tick it re-targets the nearest bird, or heads home when there is none.
     *
     * @param x horizontal spawning position
     * @param y vertical spawning position
     * @param trackedTarget the position the bee initially flies towards
     */
    public GuardBee(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
        this.spawnX = x;
        this.spawnY = y;
        this.steerTowards(trackedTarget);
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
     *
     * <p>Directions are in degrees with 0 pointing right and 90 pointing down (screen
     * coordinates), and may be negative or exceed 360.
     */
    public void updateArtBasedOnDirection() {
        final int heading = Math.floorMod(this.getDirection(), 360);
        final String spriteName;
        if (heading >= 40 && heading < 140) {
            spriteName = "down";
        } else if (heading >= 140 && heading < 230) {
            spriteName = "left";
        } else if (heading >= 230 && heading < 310) {
            spriteName = "up";
        } else {
            spriteName = "right";
        }
        this.setSprite(ART.getSprite(spriteName));
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
            this.steerTowards(nearest);
        } else {
            // No birds left in the world: return to the hive.
            this.steerTowards(this.spawnX, this.spawnY);
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
