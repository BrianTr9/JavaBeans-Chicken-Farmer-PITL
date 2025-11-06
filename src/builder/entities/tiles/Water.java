package builder.entities.tiles;

import builder.ui.SpriteGallery;

/**
 * A water tile is a tile that cannot be walked over.
 * A water tile is rendered as
 * {@link SpriteGallery#water}.
 */
public class Water extends Tile {
    /**
     * Construct a new water tile at the given x, y position.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public Water(int x, int y) {
        super(x, y, SpriteGallery.water);
    }

    /**
     * Whether water can be walked through.
     * @return false for the water tile.
     */
    public boolean canWalkThrough() {
        return false;
    }
}
