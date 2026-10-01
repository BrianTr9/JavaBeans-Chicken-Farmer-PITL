package builder.player;

import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.HasUUID;

/**
 * An interface to query the player entity in the game.
 */
public interface Player extends HasPosition, HasUUID {
    /**
     * Returns the horizontal (x-axis) coordinate of the player entity.
     *
     * @return The horizontal (x-axis) coordinate.
     * @ensures \result >= 0
     * @ensures \result is less than the window width.
     */
    int getX();

    /**
     * Returns the vertical (y-axis) coordinate of the player entity.
     *
     * @return The vertical (y-axis) coordinate.
     * @ensures \result >= 0
     * @ensures \result is less than the window height.
     */
    int getY();

    /**
     * Returns the amount of damage dealt by a player hit.
     *
     * @return The amount of damage a player deals.
     */
    int getDamage();

    /**
     * Returns the y coordinate of a player's feet: the bottom pixel row of a sprite drawn
     * centred on y. Players stand on the tile under their feet, so the upper body may overlap
     * the tile above.
     *
     * @param y the player's y coordinate
     * @param dimensions the dimensions of the game
     * @return the y coordinate of the player's feet
     */
    static int footY(int y, Dimensions dimensions) {
        return y + dimensions.tileSize() / 2 - 1;
    }

    /**
     * Returns whether this player's feet are on the same tile as the given position.
     *
     * @param position a position inside the tile, such as an entity placed on it
     * @param dimensions the dimensions of the game
     * @return true if the player is standing on that tile
     */
    default boolean isStandingOn(HasPosition position, Dimensions dimensions) {
        final int tileSize = dimensions.tileSize();
        return Math.floorDiv(getX(), tileSize) == Math.floorDiv(position.getX(), tileSize)
                && Math.floorDiv(footY(getY(), dimensions), tileSize)
                        == Math.floorDiv(position.getY(), tileSize);
    }
}
