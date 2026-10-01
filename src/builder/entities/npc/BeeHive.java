package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.timing.RepeatingTimer;

import java.util.ArrayList;

/** Spawns bees it fires at enemy's within a set range */
public class BeeHive extends Npc {

    public static final int DETECTION_DISTANCE = 350;
    public static final int TIMER = 240;
    public static final int FOOD_COST = 2;
    public static final int COIN_COST = 2;
    private static final SpriteGroup ART = SpriteGallery.hive;
    private boolean loaded = true;

    private final RepeatingTimer timer = new RepeatingTimer(TIMER);

    /**
     * Construct a new BeeHive located at the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public BeeHive(int x, int y) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
        this.setSpeed(0);
    }

    @Override
    public void tick(EngineState state, GameState game) {
        // Preserve base movement (no-op since speed=0), but call for consistency
        super.tick(state);

        if (!loaded) {
            if (isPlayerOnHive(state, game)) {
                this.timer.tick();
                this.timer.tick();
                this.timer.tick();
            } else {
                this.timer.tick();
            }
            if (this.timer.isFinished()) {
                this.loaded = true;
            }
        }
    }

    private boolean isPlayerOnHive(EngineState state, GameState game) {
        return this.distanceFrom(game.getPlayer().getX(), game.getPlayer().getY())
                < state.getDimensions().tileSize();
    }

    @Override
    public void interact(EngineState state, GameState game) {
        // No timer ticking or spawning here; handled in tick() to avoid double-counting
        super.interact(state, game);
        // Only spawn in interact() to avoid mutating the NpcManager list during tick iteration
        if (this.loaded) {
            Npc bee = this.checkAndSpawnBee(new ArrayList<>(game.getEnemies().getBirds()));
            if (bee != null) {
                game.getNpcs().addNpc(bee);
                this.loaded = false; // begin reload cycle; timer will tick in tick()
            }
        }
    }

    /**
     * Check the provided targets for a bird within detection range and spawn a GuardBee if
     * the hive is loaded.
     *
     * @param targets the list of enemy birds to consider
     * @return a new GuardBee to spawn, or null if none should be spawned
     */
    public Npc checkAndSpawnBee(ArrayList<Enemy> targets) {
        for (Enemy enemy : targets) {
            if (this.distanceFrom(enemy) < DETECTION_DISTANCE && this.loaded) {
                // can only spawn one bee in a frame
                return new GuardBee(this.getX(), this.getY(), enemy);
            }
        }
        return null;
    }
}
