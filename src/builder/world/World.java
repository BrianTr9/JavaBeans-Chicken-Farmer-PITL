package builder.world;

import builder.entities.tiles.Tile;

import engine.renderer.Dimensions;

import java.util.List;
import java.util.function.Predicate;

/**
 * An interface to query and modify the state of the world.
 *
 * <p>A world consists of a grid of tiles. The tiles at a pixel x and y position can be queried via
 * {@link #tilesAtPosition(int, int, Dimensions)}. New tiles can be placed on the world (at the
 * position contained within the tile instance) using {@link #place(Tile)}.
 *
 * @stage2
 */
public interface World {

    /**
     * Return all tiles at the grid position of the x and y position.
     *
     * <p>A tile is at a position if it's x and y position occupy the same tile index as the given x
     * and y position (according to {@link Dimensions#pixelToTile(int)}).
     *
     * <p>The order of the tiles is unspecified, any ordering is suitable.
     *
     * @param x The x-axis (horizontal) coordinate in pixels.
     * @param y The y-axis (vertical) coordinate in pixels.
     * @param dimensions The dimensions of the world.
     * @return A list of all tiles occupying the given x, y position.
     */
    List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions);

    /**
     * A flexible selector method to allow accessing tiles that meet specific conditions.
     *
     * @param filter predicate used to filter through the tiles to find those relevant.
     */
    List<Tile> tileSelector(Predicate<Tile> filter);

    /**
     * Return all tiles in the world.
     *
     * <p>Modifying the returned list must not modify the state of the world (although modifying the
     * tiles within the list will).
     *
     * <p>The order of the tiles is unspecified, any ordering is suitable.
     *
     * @return All tiles in the world.
     */
    List<Tile> allTiles();

    /**
     * Place a new tile into the world.
     *
     * <p>The tile will be placed at the position specified by its {@link Tile#getX()} and {@link
     * Tile#getY()} position.
     *
     * @param tile The tile to place into the world.
     * @ensures Any calls to {@link #tilesAtPosition} will reflect the existence of this new tile in
     *     the world.
     */
    void place(Tile tile);
}
