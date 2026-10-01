package builder.world;

import engine.game.ImmutablePosition;

/**
 * Details required by spawners to place enemies into the world.
 *
 * <p>Provides the immutable position and spawn interval of a configured spawner.
 */
public interface SpawnerDetails extends ImmutablePosition {
    /**
     * Returns the interval between spawns.
     *
     * @return the number of ticks between spawns
     */
    int getDuration();
}
