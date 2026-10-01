package builder.entities.tiles;

import builder.entities.resources.Ore;
import builder.ui.SpriteGallery;

/**
 * An ore vein tile has a {@link Ore} instance stacked on top. An ore vein is rendered the same as a
 * field but will always have an {@link Ore} above it. An ore vein tile is rendered as {@link
 * SpriteGallery#field}.
 *
 * @stage2
 */
public class OreVein extends Tile {
    private final Ore ore;

    /**
     * Construct a new ore vein at the given x, y position.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public OreVein(int x, int y) {
        super(x, y, SpriteGallery.field);
        this.ore = new Ore(x, y);
        placeOn(ore);
    }

    /**
     * Returns the instance of {@link Ore} stacked on this ore vein.
     *
     * @stage3
     * @return The current instance on top of this tile.
     */
    public Ore getOre() {
        return ore;
    }
}
