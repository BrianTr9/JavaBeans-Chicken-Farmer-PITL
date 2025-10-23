package builder;

import builder.entities.npc.NpcManager;
import builder.entities.npc.enemies.EnemyManager;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;

/**
 * An implementation of the game state for the JavaBean game.
 *
 * <p>This class stores the world, player, inventory and managers required by the game and
 * provides accessors used by game subsystems.
 */
public class JavaBeanGameState implements GameState {
    private final World world;
    private final Player player;
    private final Inventory inventory;
    private final NpcManager npcs;
    private final EnemyManager enemies;

    /**
     * Construct a new instance storing the given world, player, and inventory.
     *
     * @param world The world of the game.
     * @param player The player of the game.
     * @param inventory The inventory of the player.
     * @param npcs The NPC manager for the game.
     * @param enemies The enemy manager for the game.
     */
    public JavaBeanGameState(
            World world,
            Player player,
            Inventory inventory,
            NpcManager npcs,
            EnemyManager enemies) {
        this.world = world;
        this.player = player;
        this.inventory = inventory;
        this.npcs = npcs;
        this.enemies = enemies;
    }

    /** Returns the NPC manager for this game state. */
    @Override
    public NpcManager getNpcs() {
        return this.npcs;
    }

    /** Returns the enemy manager for this game state. */
    @Override
    public EnemyManager getEnemies() {
        return this.enemies;
    }

    /** Returns the current game world. */
    @Override
    public World getWorld() {
        return world;
    }

    /** Returns the current player. */
    @Override
    public Player getPlayer() {
        return player;
    }

    /** Returns the current inventory for the player. */
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
