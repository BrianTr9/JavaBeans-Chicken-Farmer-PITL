package builder.entities.npc.spawners;

import builder.GameState;

import engine.EngineState;

/**
 * Spawns eagles at regular intervals that fly towards and steal food from the player.
 *
 * <p>According to the specification, eagles fly towards the player, steal 3 food upon
 * reaching them, then return to spawn and remove themselves from the world.
 */
public class EagleSpawner extends AbstractBirdSpawner {

    private static final int DEFAULT_DURATION = 1000;

    /**
     * Construct an eagle spawner with default spawn interval.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     */
    public EagleSpawner(int x, int y) {
        super(x, y, DEFAULT_DURATION);
    }

    /**
     * Construct an eagle spawner with custom spawn interval.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     * @param duration The interval (in ticks) between eagle spawns.
     */
    public EagleSpawner(int x, int y, int duration) {
        super(x, y, duration);
    }

    @Override
    protected void spawnBird(EngineState state, GameState game) {
        game.getEnemies().setSpawnX(this.getX());
        game.getEnemies().setSpawnY(this.getY());
        game.getEnemies().getBirds().add(game.getEnemies().mkE(game.getPlayer()));
    }
}
