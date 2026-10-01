package builder.player;

import builder.GameState;
import builder.Tickable;
import builder.entities.Usable;
import builder.entities.tiles.Tile;
import builder.ui.RenderableGroup;
import builder.world.World;

import engine.EngineState;
import engine.game.Direction;
import engine.game.Position;
import engine.input.MouseState;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.util.List;

/**
 * Manages the users interaction with the player through keyboard/mouse interactions. Stores and
 * provides access to the player instance and renders the player via the render method.
 */
public class PlayerManager implements Tickable, RenderableGroup {

    private final ChickenFarmer player;

    /**
     * Construct a new player manager and a new player instance at the given x, y position.
     *
     * @requires x, y is a valid position within the game, i.e. positive and less than the window
     *     size.
     * @param x The x-axis (horizontal) coordinate to spawn the player.
     * @param y The y-axis (vertical) coordinate to spawn the player.
     */
    public PlayerManager(int x, int y) {
        super();
        this.player = new ChickenFarmer(x, y);
    }

    /**
     * Progress the state of the player.
     *
     * <p>The player instance managed by this manager should be progressed (i.e. {@link
     * ChickenFarmer#tick(EngineState)} should be called).
     *
     * <p>If the player is pressing one of the movement keys (from {@link
     * engine.input.KeyState#isDown(char)}) listed in the table below, the player should be moved in
     * the appropriate direction via {@link ChickenFarmer#move(Direction, int)}. The player must
     * only move one pixel each tick.
     *
     * <table>
     *     <tr><th>Key</th><th>Direction</th></tr>
     *     <tr><td>w</td><td>NORTH</td></tr>
     *     <tr><td>s</td><td>SOUTH</td></tr>
     *     <tr><td>a</td><td>WEST</td></tr>
     *     <tr><td>d</td><td>EAST</td></tr>
     *     <caption>&nbsp;</caption>
     * </table>
     *
     * If multiple direction keys are pressed, the player should only move in one direction. The
     * preference order is 'w', 's', 'a', and 'd'. That is, if both 's' and 'd' are pressed, the
     * player should move south ('s').
     *
     * <p>If any tile at the position the player would move to (according to {@link
     * World#tilesAtPosition(int, int, Dimensions)}) cannot be walked through (according to
     * {@link Tile#canWalkThrough()}) then the player must not move there.
     *
     * <p>Any tile at the (potentially new) position of the player should be interacted
     * with via {@link Tile#interact(EngineState, GameState)}. If the player is left-clicking
     * (according to {@link MouseState#isLeftPressed()}), those tiles should be used via {@link
     * Tile#use(EngineState, GameState)}.
     */
    @Override
    public void tick(EngineState state, GameState game) {
        this.player.tick(state);
        this.useControls(state, game);
    }

    /**
     * Returns the player instance managed by this manager.
     *
     * @return The player instance.
     */
    public Player getPlayer() {
        return player;
    }

    private void useControls(EngineState state, GameState game) {
        World world = game.getWorld();
        Dimensions dimensions = state.getDimensions();
        Direction direction = null;
        if (state.getKeys().isDown('w')) {
            direction = Direction.NORTH;
        } else if (state.getKeys().isDown('s')) {
            direction = Direction.SOUTH;
        } else if (state.getKeys().isDown('a')) {
            direction = Direction.WEST;
        } else if (state.getKeys().isDown('d')) {
            direction = Direction.EAST;
        }
        if (direction != null) {
            tryMove(direction, world, dimensions);
        }

        List<Tile> underPlayer =
                tilesAt(world, player.getX(), footY(player.getY(), dimensions), dimensions);
        interact(state, game, underPlayer);
        if (state.getMouse().isLeftPressed()) {
            use(state, game, underPlayer);
        }
    }

    /**
     * Returns the y coordinate of the player's feet: the bottom pixel row of the sprite, which
     * is drawn centred on the player's position. The player stands on the tile under their
     * feet, so the upper body can overlap the tile above (for example, water at the shore).
     */
    private static int footY(int y, Dimensions dimensions) {
        return y + dimensions.tileSize() / 2 - 1;
    }

    /**
     * Moves the player one pixel in the given direction unless their feet would touch a tile
     * that cannot be walked through. The feet are a box half a tile wide, centred on the
     * player's x coordinate, covering the bottom quarter of the sprite; all four corners must
     * land on walkable tiles, so the legs never overlap water while the head may.
     */
    private void tryMove(Direction direction, World world, Dimensions dimensions) {
        Position next = new Position(player.getX(), player.getY()).shift(direction, 1);
        int halfWidth = dimensions.tileSize() / 4;
        int bottom = footY(next.getY(), dimensions);
        int top = bottom - dimensions.tileSize() / 4 + 1;

        for (int x : new int[] {next.getX() - halfWidth, next.getX() + halfWidth}) {
            for (int y : new int[] {top, bottom}) {
                if (!isWalkable(tilesAt(world, x, y, dimensions))) {
                    return;
                }
            }
        }
        player.move(direction, 1);
    }

    /** Returns the tiles at a pixel, or none if the pixel lies outside the world. */
    private static List<Tile> tilesAt(World world, int x, int y, Dimensions dimensions) {
        if (x < 0 || y < 0 || x >= dimensions.windowSize() || y >= dimensions.windowSize()) {
            return List.of();
        }
        return world.tilesAtPosition(x, y, dimensions);
    }

    private static boolean isWalkable(List<Tile> tiles) {
        for (Tile tile : tiles) {
            if (!tile.canWalkThrough()) {
                return false;
            }
        }
        return true;
    }

    private void interact(EngineState state, GameState game, List<Tile> underPlayer) {
        for (Tile tile : underPlayer) {
            tile.interact(state, game);
        }
    }

    private void use(EngineState state, GameState game, List<Tile> underPlayer) {
        this.player.use(game.getInventory().getHolding());

        for (Tile tile : underPlayer) {
            if (tile instanceof Usable usable) {
                usable.use(state, game);
            }
        }
    }

    /**
     * A collection of items to render, for the player manager, this is just the player.
     *
     * @return A list containing a renderable that represents the player, i.e. the player instance.
     */
    @Override
    public List<Renderable> render() {
        return List.of(player);
    }
}
