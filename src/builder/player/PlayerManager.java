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
 *
 * @hint The player manager should hold an instance of {@link ChickenFarmer}.
 * @stage1
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
     * @stage2part If any tile at the position the player would move to (according to {@link
     *     World#tilesAtPosition(int, int, Dimensions)}) cannot be walked through (according to
     *     {@link Tile#canWalkThrough()}) then the player must not move there.
     * @stage3part Any tile at the (potentially new) position of the player should be interacted
     *     with via {@link Tile#interact(EngineState, GameState)}. If the player is left-clicking
     *     (according to {@link MouseState#isLeftPressed()}), those tiles should be used via {@link
     *     Tile#use(EngineState, GameState)}.
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
            tryMove(direction, world, state.getDimensions());
        }

        List<Tile> underPlayer =
                world.tilesAtPosition(player.getX(), player.getY(), state.getDimensions());
        interact(state, game, underPlayer);
        if (state.getMouse().isLeftPressed()) {
            use(state, game, underPlayer);
        }
    }

    private void tryMove(Direction direction, World world, Dimensions dimensions) {
        Position nextPosition = new Position(player.getX(), player.getY()).shift(direction, 1);

        List<Tile> underPlayer =
                world.tilesAtPosition(nextPosition.getX(), nextPosition.getY(), dimensions);
        boolean blocked = false;
        for (Tile tile : underPlayer) {
            if (!tile.canWalkThrough()) {
                blocked = true;
            }
        }
        if (!blocked) {
            player.move(direction, 1);
        }
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
