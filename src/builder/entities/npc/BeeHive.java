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
    private static final SpriteGroup art = SpriteGallery.hive;
    private boolean loaded = true;

    private final RepeatingTimer timer = new RepeatingTimer(TIMER);

    public BeeHive(int x, int y) {
        super(x, y);
        this.setSprite(art.getSprite("default"));
        this.setSpeed(0);
    }

    @Override
    public void tick(EngineState state, GameState game) {
        super.tick(state);
        this.timer.tick();
    }

    @Override
    public void interact(EngineState state, GameState game) {
        super.interact(state, game);

        timer.tick();
        timer.tick();
        Npc npc = this.checkAndSpawnBee(game.getEnemies().Birds);
        if (npc != null) {
            game.getNpcs().npcs.add(npc);
        }
        if (timer.isFinished()) {
            this.loaded = true;
        }
    }

    public Npc checkAndSpawnBee(ArrayList<Enemy> targets) {
        for (Enemy enemy : targets) {
            if (this.distanceFrom(enemy) < DETECTION_DISTANCE && this.loaded) {
                this.loaded = false;
                return new GuardBee(
                        this.getX(), this.getY(), enemy); // can only spawn one bee in a frame
            }
        }
        return null;
    }
}
