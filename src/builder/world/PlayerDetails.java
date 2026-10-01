package builder.world;

import engine.game.ImmutablePosition;

/**
 * Details required to place the player in the world at startup.
 *
 * <p>Provides an immutable position and initial resources for the player.
 */
public interface PlayerDetails extends ImmutablePosition {
    /** Returns the starting food amount for the player. */
    int getStartingFood();

    /** Returns the starting coin amount for the player. */
    int getStartingCoins();
}
