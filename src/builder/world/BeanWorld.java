package builder.world;

import builder.GameState;
import builder.Tickable;
import builder.entities.tiles.Tile;
import builder.ui.RenderableGroup;

import engine.EngineState;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A world instance for the JavaBeans game.
 *
 * <p>A world consists of a grid of tiles. The tiles must be updated by the world each tick and
 * appropriately rendered via the render method.
 */
public class BeanWorld implements RenderableGroup, Tickable, World {

    private final List<Tile> tiles = new ArrayList<>();

    /**
     * Construct a new empty world with no tiles.
     *
     * <p>This constructor should be avoided and {@link WorldBuilder} methods should be preferred.
     *
     * <p>This constructor should be used when testing the class.
     */
    BeanWorld() {}

    /**
     * Finds all tiles that contain the given pixel coordinates.
     *
     * @param x The x-axis (horizontal) coordinate in pixels.
     * @param y The y-axis (vertical) coordinate in pixels.
     * @param dimensions The dimensions of the world.
     * @return all tiles that contain the given pixel coordinates.
     */
    @Override
    public List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions) {
        List<Tile> result = new ArrayList<>();
        int gridX = dimensions.pixelToTile(x);
        int gridY = dimensions.pixelToTile(y);
        for (Tile tile : tiles) {
            int tileX = dimensions.pixelToTile(tile.getX());
            int tileY = dimensions.pixelToTile(tile.getY());
            if (gridX == tileX && gridY == tileY) {
                result.add(tile);
            }
        }
        return result;
    }

    @Override
    public List<Tile> allTiles() {
        return new ArrayList<>(tiles);
    }

    @Override
    public void place(Tile tile) {
        this.tiles.add(tile);
    }

    /**
     * A flexible selector method to allow accessing tiles that meet specific conditions.
     *
     * @param filter predicate used to filter through the tiles to find those relevant.
     */
    @Override
    public List<Tile> tileSelector(Predicate<Tile> filter) {
        List<Tile> result = new ArrayList<>();
        for (Tile tile : tiles) {
            if (filter.test(tile)) {
                result.add(tile);
            }
        }
        return result;
    }

    /**
     * Progress the state of the world. Tiles marked for removal (for example grass that has
     * been hoed into dirt) are dropped first, so they are no longer ticked, rendered or found
     * by position queries; then {@link Tile#tick(EngineState)} is called on every tile.
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     */
    @Override
    public void tick(EngineState state, GameState game) {
        this.tiles.removeIf(Tile::isMarkedForRemoval);
        for (Tile tile : this.tiles) {
            tile.tick(state);
        }
    }

    /**
     * A collection of items to render, including every tile and stacked entity in the world.
     *
     * <p>The order of the list must be consistent with {@link Tile#render()}; that is, a tile must
     * occur in the list before any of its stacked entities and the stacked entities order must
     * match {@link Tile#getStackedEntities()}.
     *
     * <p>Otherwise, any ordering is appropriate.
     *
     * @return The list of renderables required to draw the world to the screen.
     */
    @Override
    public List<Renderable> render() {
        List<Renderable> result = new ArrayList<>();
        for (Tile tile : tiles) {
            result.addAll(tile.render());
        }
        return result;
    }
}
