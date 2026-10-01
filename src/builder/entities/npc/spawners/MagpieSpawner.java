package builder.entities.npc.spawners;

import builder.GameState;
import builder.entities.npc.enemies.Magpie;

import engine.EngineState;

/**
 * Spawns magpies at regular intervals that fly towards and steal coins from the player.
 *
 * <p>According to the specification, magpies fly towards the player, steal 1 coin upon
 * reaching them, then return to spawn and remove themselves from the world.
 */
public class MagpieSpawner extends AbstractBirdSpawner {

    /**
     * Construct a magpie spawner with custom spawn interval.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     * @param duration The interval (in ticks) between magpie spawns.
     */
    public MagpieSpawner(int x, int y, int duration) {
        super(x, y, duration);
    }

    @Override
    protected void spawnBird(EngineState state, GameState game) {
        game.getEnemies().addBird(new Magpie(this.getX(), this.getY(), game.getPlayer()));
    }
}
