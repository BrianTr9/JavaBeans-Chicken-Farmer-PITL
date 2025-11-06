package builder.world;


import builder.entities.tiles.Tile;
import engine.renderer.Dimensions;

import java.util.List;

/**
 * An interface to query and modify the state of the world.
 * <p>A world consists of a grid of tiles. The tiles at
 * a pixel x and y position can be queried via
 * {@link #tilesAtPosition(int, int, Dimensions)}.
 * New tiles can be placed on the world (at the
 * position contained within the tile instance) using
 * {@link #place(Tile)}.</p>
 */
public interface World {
    /**
     * Return all tiles at the grid position of the x and y position.
     * <p>A tile is at a position if its x and y position
     * occupy the same tile index as the given x
     * and y position (according to
     * {@link Dimensions#pixelToTile(int)}).</p>
     * <p>The order of the tiles is unspecified,
     * any ordering is suitable.</p>
     * @param x The x-axis (horizontal) coordinate in pixels.
     * @param y The y-axis (vertical) coordinate in pixels.
     * @param dimensions The dimensions of the world.
     * @return A list of all tiles occupying the given x, y position.
     */
    List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions);

    /**
     * Return all tiles in the world.
     * <p>Modifying the returned list must not modify
     * the state of the world (although modifying the
     * tiles within the list will).</p>
     * <p>The order of the tiles is unspecified,
     * any ordering is suitable.</p>
     * @return All tiles in the world.
     */
    List<Tile> allTiles();

    /**
     * Place a new tile into the world. The tile will be placed
     * at the position specified by its
     * {@code engine.game.Entity#getX()} and
     * {@code engine.game.Entity#getY()} position.
     * @param tile The tile to place into the world.
     * @ensures Subsequent calls to
     * {@link #tilesAtPosition(int, int, Dimensions)}
     * reflect the existence of this tile.
     */
    void place(Tile tile);
}
