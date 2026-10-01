package builder.world;

import engine.game.HasPosition;

/**
 * Details required by spawners to place enemies into the world.
 *
 * <p>Provides position and duration information for a configured spawner.
 */
public interface SpawnerDetails extends HasPosition {
    /** Returns the X coordinate. */
    int getX();

    /** Returns the Y coordinate. */
    int getY();

    /** Set the X coordinate. */
    void setX(int x);

    /** Set the Y coordinate. */
    void setY(int y);

    /** Returns the spawn duration or interval for this spawner. */
    int getDuration();
}
