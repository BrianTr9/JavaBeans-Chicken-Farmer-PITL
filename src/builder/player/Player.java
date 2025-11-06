package builder.player;

/** An interface to query the player entity in the game.
 */
public interface Player {
    /**
     * Returns the amount of damage dealt by a player hit.
     * @return The amount of damage a player deals.
     */
    int getDamage();

    /**
     * Returns the horizontal (x-axis) coordinate of the player entity.
     * @return The horizontal (x-axis) coordinate.
     * @ensures \result >= 0
     * @ensures \result is less than the window width
     */
    int getX();

    /**
     * Returns the vertical (y-axis) coordinate of the player entity.
     * @return The vertical (y-axis) coordinate.
     * @ensures \result >= 0
     * @ensures \result is less than the window height
     */
    int getY();
}
