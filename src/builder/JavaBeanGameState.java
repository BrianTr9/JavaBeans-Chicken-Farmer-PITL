package builder;

import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;

/**
 * An implementation of the game state for the JavaBean game.
 * Stores the world, player, and inventory.
 *
*/
public class JavaBeanGameState extends Object implements GameState {
    private Player player;
    private Inventory inventory;
    private World world;

    /**
     * Construct a new instance storing the given world,
     * player, and inventory.
     *
     * @param world The world of the game.
     * @param player The player of the game.
     * @param inventory The inventory of the game.
     */
    public JavaBeanGameState(World world, Player player, Inventory inventory) {
        this.world = world;
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * Returns the current state of the inventory.
     * The returned inventory is mutable, that is calling mutator methods
     * such as Inventory.addCoins(int) will modify the inventory.
     * @return The current inventory.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Returns the current state of the player. Useful for
     * retrieving the player's location.
     * @return The current player.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the current state of the game world.
     * The returned world is mutable, that is, calling mutator methods
     * such as World.place(Tile) will modify the world.
     * @return The current game world.
     */
    public World getWorld() {
        return this.world;
    }
}