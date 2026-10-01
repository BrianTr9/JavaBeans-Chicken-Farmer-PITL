package builder.entities.tiles;

import builder.ui.SpriteGallery;

import engine.art.sprites.SpriteGroup;

/**
 * A water tile is a tile that cannot be walked over. A water tile is rendered as {@link
 * SpriteGallery#water}.
 *
 * @stage2
 */
public class Water extends Tile {

    private static final SpriteGroup art = SpriteGallery.water;

    /**
     * Construct a new water tile at the given x, y position.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Water(int x, int y) {
        super(x, y, art);
    }

    /**
     * Whether water can be walked through.
     *
     * @return false for the water tile.
     */
    @Override
    public boolean canWalkThrough() {
        return false;
    }
}
