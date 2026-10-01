package builder;

import builder.entities.npc.NpcManager;
import builder.entities.npc.enemies.EnemyManager;
import builder.inventory.TinyInventory;
import builder.inventory.items.Item;
import builder.player.ChickenFarmer;
import builder.player.Player;
import builder.world.BeanWorld;
import builder.world.WorldBuilder;

import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import scenarios.mocks.MockEngineState;

/**
 * A small, real game (empty world, real managers and inventory) for unit tests that need a
 * {@link GameState}. The grid is 10 tiles of 80 pixels, so tile centres sit at 40, 120, ...
 */
public class GameFixture {

    /** 10 tiles per row in an 800 pixel window: tileSize() == 80. */
    public static final Dimensions DIMENSIONS = new TileGrid(10, 800);

    /** Pixel coordinate of the centre of the first tile. */
    public static final int FIRST_TILE = 40;

    public final TinyInventory inventory = new TinyInventory(5, 10, 10);
    public final BeanWorld world = WorldBuilder.empty();
    public final NpcManager npcs = new NpcManager();
    public final EnemyManager enemies = new EnemyManager();
    public final Player player;
    public final GameState game;

    /** Creates a fixture with a chicken farmer standing on the first tile. */
    public GameFixture() {
        this(new ChickenFarmer(FIRST_TILE, FIRST_TILE));
    }

    /**
     * Creates a fixture around the given player.
     *
     * @param player the player in the game state
     */
    public GameFixture(Player player) {
        this.player = player;
        this.game = new JavaBeanGameState(world, player, inventory, npcs, enemies);
    }

    /**
     * Puts the item in the first slot and makes it the held item.
     *
     * @param item the item to hold, or null for empty hands
     */
    public void hold(Item item) {
        inventory.setItem(0, item);
        inventory.setActiveSlot(0);
    }

    /**
     * Returns an engine state for the given frame with no input.
     *
     * @param frame the current tick
     * @return a mock engine state using {@link #DIMENSIONS}
     */
    public static MockEngineState engine(int frame) {
        return new MockEngineState(DIMENSIONS, frame);
    }
}
