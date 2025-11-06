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

/**
 * A world instance for the JavaBeans game.
 * <p>A world consists of a grid of tiles. The tiles
 * must be updated by the world each tick and
 * appropriately rendered via the {@link #render()} method.</p>
 */
public class BeanWorld extends Object implements RenderableGroup, Tickable, World {
    private List<Tile> tiles = new ArrayList<>();

    /**
     * Construct a new empty world with no tiles.
     * <p>This constructor should be avoided and {@link WorldBuilder}
     * methods should be preferred.
     * It is primarily intended for testing.</p>
     */
    BeanWorld() {}

    /**
     * {@inheritDoc}
     * <p>Description copied from interface: World.<br>
     * Return all tiles at the grid position of the x and y position.
     * A tile is at a position if its
     * x and y position occupy the same tile index as
     * the given x and y position (according to
     * {@link Dimensions#pixelToTile(int)}). The order of the tiles
     * is unspecified; any ordering is
     * suitable.</p>
     */
    public List<Tile> tilesAtPosition(int x, int y, Dimensions dimensions) {
        List<Tile> inter = new ArrayList<>();
        for (Tile i : tiles) {
            if (dimensions.pixelToTile(x) == dimensions.pixelToTile(i.getX())
                    && dimensions.pixelToTile(y) == dimensions.pixelToTile(i.getY())) {
                inter.add(i);
            }
        }
        return inter;
    }

    /**
     * {@inheritDoc}
     * <p>Description copied from interface: World.<br>
     * Return all tiles in the world. Modifying the returned list
     * must not modify the state of the
     * world (although modifying the tiles within the list will).
     * The order of the tiles is
     * unspecified; any ordering is suitable.</p>
     */
    public List<Tile> allTiles() {
        return new ArrayList<>(this.tiles);
    }

    /**
     * {@inheritDoc}
     * <p>Description copied from interface: World.<br>
     * Place a new tile into the world. The tile will be placed
     * at the position specified by its
     * {@code engine.game.Entity#getX()} and
     * {@code engine.game.Entity#getY()} position. If a tile
     * already exists at that position and is marked for
     * removal it is replaced; otherwise the new
     * tile is stacked on the existing one.</p>
     * @param tile The tile to place into the world.
     */
    public void place(Tile tile) {
        List<Tile> inter = this.allTiles();
        for (Tile i : inter) {
            if (tile.getX() == i.getX() && tile.getY() == i.getY()) {
                if (i.isMarkedForRemoval()) {
                    tiles.remove(i);
                    break;
                }
//                i.placeOn(tile);
//                return;
            }
        }
        this.tiles.add(tile);
    }

    /**
     * Progress the state of the world. The world is progressed
     * by calling the
     * {@link Tile#tick(EngineState)} method on every world tile.
     * @param state The engine state (input, timing, dimensions, etc.).
     * @param game The game state (player, inventory,
     *            world references, etc.).
     */
    public void tick(EngineState state, GameState game) {
        for (Tile i : tiles) {
            i.tick(state);
        }
    }

    /**
     * A collection of items to render, including every tile and
     * stacked entity in the world. The
     * order of the list must be consistent with {@link Tile#render()};
     * that is, a tile must occur
     * in the list before any of its stacked entities
     * and the stacked entities order must match
     * {@link Tile#getStackedEntities()}. Otherwise,
     * any ordering is appropriate.
     * @return The list of renderables required to draw
     * the world to the screen.
     */
    public List<Renderable> render() {
        List<Renderable> renderables = new ArrayList<>();
        for (Tile i : tiles) {
            renderables.addAll(i.render());
        }
        return renderables;
    }
}
