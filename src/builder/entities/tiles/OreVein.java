package builder.entities.tiles;

import builder.entities.resources.Ore;
import builder.ui.SpriteGallery;

/**
 * An ore vein tile has an {@link Ore} instance stacked on top.
 * An ore vein is rendered the same as
 * a field ({@link SpriteGallery#field})
 * but will always have an Ore above it.
 */
public class OreVein extends Tile {
    private Ore ore;

    /**
     * Construct a new ore vein at the given x, y position.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public OreVein(int x, int y) {
        super(x, y, SpriteGallery.field);
        this.ore = new Ore(x, y);
        this.placeOn(this.ore);
    }

    /**
     * Returns the instance of {@link Ore} stacked on this ore vein.
     * @return The current instance on top of this tile.
     */
    public Ore getOre() {
        return this.ore;
    }
}
