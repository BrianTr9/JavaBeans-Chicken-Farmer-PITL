package builder;

import builder.entities.npc.NpcManager;
import builder.entities.npc.enemies.EnemyManager;
import builder.inventory.Inventory;
import builder.player.Player;
import builder.world.World;

/**
 * An interface to the game state information, including world, player, and inventory data.
 *
 * <p>Implementations provide access to the world, player, inventory and managers used by the
 * game subsystems.
 */
public interface GameState {
    /** Returns the current state of the game world. */
    World getWorld();

    /** Returns the NPC manager for the game. */
    NpcManager getNpcs();

    /** Returns the enemy manager for the game. */
    EnemyManager getEnemies();

    /** Returns the current state of the player. */
    Player getPlayer();

    /** Returns the current state of the inventory. */
    Inventory getInventory();
}
