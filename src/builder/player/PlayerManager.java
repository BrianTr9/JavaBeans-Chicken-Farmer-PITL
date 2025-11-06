package builder.player;

import builder.GameState;
import builder.Tickable;
import builder.entities.tiles.Tile;
import builder.ui.RenderableGroup;
import builder.world.World;
import engine.EngineState;
import engine.game.Direction;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the users interaction with the player through
 * keyboard/mouse interactions. Stores and
 * provides access to the player instance and renders
 * the player via the {@link #render()} method.
 */
public class PlayerManager extends Object implements Tickable, RenderableGroup {
    private ChickenFarmer chickenFarmer;

    /**
     * Construct a new player manager and a new
     * player instance at the given x, y position.
     * @param x The x-axis (horizontal) coordinate to spawn the player.
     * @param y The y-axis (vertical) coordinate to spawn the player.
     * @requires x, y is a valid position within the game,
     * i.e. positive and less than the window size.
     */
    public PlayerManager(int x, int y) {
        this.chickenFarmer = new ChickenFarmer(x, y);
    }

    /**
     * Returns the player instance managed by this manager.
     * @return The player instance.
     */
    public Player getPlayer() {
        return this.chickenFarmer;
    }

    /**
     * A collection of items to render, for the player manager,
     * this is just the player.
     * @return A list containing a renderable that
     * represents the player, i.e. the player instance.
     */
    @Override
    public List<Renderable> render() {
        List<Renderable> renderables = new ArrayList<>();
        renderables.add(this.chickenFarmer);
        return renderables;
    }

    /**
     * Progress the state of the player.
     * <p>The player instance managed by this manager should be progressed (i.e.
     * {@link ChickenFarmer#tick(EngineState)} should be called).</p>
     * <p>If the player is pressing one of the movement keys
     * (from {@code KeyState.isDown(char)})
     * listed in the table below, the player should be moved
     * in the appropriate direction via
     * {@link ChickenFarmer#move(Direction, int)}. The player must only
     * move one pixel each tick.</p>
     * <pre>
     * Key  Direction
     * w    NORTH
     * s    SOUTH
     * a    WEST
     * d    EAST
     * </pre>
     * <p>If multiple direction keys are pressed, the player should only
     * move in one direction. The
     * preference order is 'w', 's', 'a', and 'd'. That is,
     * if both 's' and 'd' are pressed, the
     * player should move south ('s').</p>
     * <p><strong>In Stage 2:</strong> If any tile at the position
     * the player would move to (according
     * to {@link World#tilesAtPosition(int, int, Dimensions)}) cannot be
     * walked through (according
     * to {@link builder.entities.tiles.Tile#canWalkThrough()}) then
     * the player must not move there.</p>
     * <p><strong>In Stage 3:</strong> Any tile at
     * the (potentially new) position of the player should
     * be interacted with via {@link Tile#interact(EngineState, GameState)}.
     * If the player is left-clicking
     * (according to {@code MouseState.isLeftPressed()}),
     * those tiles should be used via
     * {@link Tile#use(EngineState, GameState)}.</p>
     * @param state The state of the engine, including the mouse,
     *              keyboard information and dimension. Useful for
     *              processing keyboard presses or mouse movement.
     * @param game The state of the game, including the player and world.
     *             Can be used to query or update the game state.
     */
    @Override
    public void tick(EngineState state, GameState game) {
        World world = game.getWorld();
        Dimensions dimensions = state.getDimensions();
        int tileSize = dimensions.tileSize();

        if (state.getKeys().isDown('w')) {
            this.chickenFarmer.tick(state);
            List<Tile> tiles = world.tilesAtPosition(chickenFarmer.getX(),
                    chickenFarmer.getY() - 1 + (tileSize / 2), dimensions);
            for (Tile i : tiles) {
                if (!i.canWalkThrough()) {
                    return;
                }
            }
            this.chickenFarmer.move(Direction.NORTH, 1);

        } else if (state.getKeys().isDown('s')) {
            this.chickenFarmer.tick(state);
            List<Tile> tiles = world.tilesAtPosition(chickenFarmer.getX(),
                    chickenFarmer.getY() + 1 + (tileSize / 2), dimensions);
            for (Tile i : tiles) {
                if (!i.canWalkThrough()) {
                    return;
                }
            }
            this.chickenFarmer.move(Direction.SOUTH, 1);

        } else if (state.getKeys().isDown('a')) {
            this.chickenFarmer.tick(state);
            List<Tile> tiles = world.tilesAtPosition(chickenFarmer.getX() - 1 - (tileSize / 4),
                    chickenFarmer.getY() + (tileSize / 2), dimensions);
            for (Tile i : tiles) {
                if (!i.canWalkThrough()) {
                    return;
                }
            }
            this.chickenFarmer.move(Direction.WEST, 1);

        } else if (state.getKeys().isDown('d')) {
            this.chickenFarmer.tick(state);
            List<Tile> tiles = world.tilesAtPosition(chickenFarmer.getX() + 1 + (tileSize / 4),
                    chickenFarmer.getY() + (tileSize / 2), dimensions);
            for (Tile i : tiles) {
                if (!i.canWalkThrough()) {
                    return;
                }
            }
            this.chickenFarmer.move(Direction.EAST, 1);

        }

        List<Tile> tiles = world.tilesAtPosition(chickenFarmer.getX(),
                chickenFarmer.getY() + (tileSize / 2), dimensions);
        for (Tile i : tiles) {
            i.interact(state, game);
        }

        if (state.getMouse().isLeftPressed()) {
            if (game.getInventory().getHolding() != null) { // check != null
                for (Tile i : tiles) {
                    i.use(state, game);
                }
            }
            chickenFarmer.tick(state);
            chickenFarmer.use(game.getInventory().getHolding());
        }

    }

}











