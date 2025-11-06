package builder;

import builder.entities.Brutus;
import builder.inventory.*;
import builder.inventory.items.Bucket;
import builder.inventory.items.Hoe;
import builder.inventory.items.Jackhammer;
import builder.inventory.ui.InventoryOverlay;
import builder.inventory.ui.ResourceOverlay;
import builder.player.PlayerManager;
import builder.ui.Overlay;
import builder.world.BeanWorld;
import builder.world.WorldBuilder;
import builder.world.WorldLoadException;

import engine.EngineState;
import engine.game.Entity;
import engine.game.Game;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static builder.ui.SpriteGallery.inventory;

/**
 * JavaBeans, a farming game.
 *
 * <p>In this game, the player collects coins by mining ores. The player, a chicken farmer, may use
 * those coins to plant cabbages on tilled dirt.
 *
 * <p>This class managers the world instance, player manager, and the inventory instance. The
 * inventory and resource overlays should also be managed by this class.
 *
 * <p>In stage 0, this class will store the Brutus character to wander around.
 *
 * @stage1part This class manages the player manager.
 * @stage2part This class manages the world instance.
 */
public class JavaBeanFarm implements Game {

    // Stage 0: Uncomment this line to store brutus
    private final Entity brutus;

    // Stage 1: Uncomment this line to manage the player.
    private final PlayerManager playerManager;

    // Stage 2: Uncomment this line to manage the world.
    private final BeanWorld world;

    // Stage 3: Uncomment these lines to include the inventory.
    private final Inventory inventory;
    private final List<Overlay> overlays = new ArrayList<>();

    /**
     * Constructs a new JavaBean Farm game using the given dimensions.
     *
     * @param dimensions The dimensions we want for this game.
     * @throws IOException If the game is unable to find or open the default world map file.
     * @throws WorldLoadException If the default world map file cannot be parsed successfully.
     */
    public JavaBeanFarm(Dimensions dimensions) throws IOException, WorldLoadException {
        int centerX = dimensions.windowSize() / 2;
        int centerY = dimensions.windowSize() / 2;

        // Stage 0: Remove Brutus after this stage
        this.brutus = new Brutus(centerX, centerY);

        // Stage 1: Uncomment this line to put the player in the screen center.
        this.playerManager = new PlayerManager(centerX, centerY);

        // Stage 2: Uncomment this line to load the default world from the file.
        this.world = WorldBuilder.fromFile(dimensions, "resources/uqLogo.map");

        // Stage 3: Uncomment these lines to create the default inventory.
        int inventorySize = 4;
        this.inventory = new TinyInventory(inventorySize);
        inventory.setItem(0, new Bucket());
        inventory.setItem(1, new Hoe());
        inventory.setItem(2, new Jackhammer());

        this.overlays.add(new InventoryOverlay(dimensions, inventorySize));
        this.overlays.add(new ResourceOverlay(dimensions));
    }

    /**
     * Ticks the internal game state forward by one frame.
     *
     * @stage1part The player manager should be progressed via {@link
     *     PlayerManager#tick(EngineState, GameState)}.
     * @stage2part The world should be progressed via {@link BeanWorld#tick(EngineState,
     *     GameState)}.
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     */
    public void tick(EngineState state) {
        // Stage 0: Uncomment this line to progress Brutus.
        this.brutus.tick(state);

        // Stage 1: Uncomment these lines to progress the player.
        GameState game = new JavaBeanGameState(world, playerManager.getPlayer(), inventory);
        this.playerManager.tick(state, game);

        // Stage 2: Uncomment this line to progress the world.
        this.world.tick(state, game);

        // Stage 3: Uncomment these lines to progress the inventory overlays.
        for (Overlay overlay : overlays) {
            overlay.tick(state, game);
        }
    }

    /**
     * A collection of items to render, every component of the game to be rendered should be
     * returned.
     *
     * @stage2part Any renderables of the world (i.e. {@link BeanWorld#render()}) must be rendered
     *     behind everything else, i.e., first in the returned list.
     * @stage1part Any renderables of the player (i.e. {@link PlayerManager#render()}) must be
     *     rendered after the world but before overlays.
     *     <p>Overlays, i.e., {@link ResourceOverlay} and {@link InventoryOverlay} must be rendered
     *     last in any order.
     * @return The list of renderables required to draw the whole game.
     */
    @Override
    public List<Renderable> render() {
        List<Renderable> renderables = new ArrayList<>();

        // Stage 2: Uncomment this line to render the world.
        renderables.addAll(this.world.render());

        // Stage 1: Uncomment this line to render the player.
        renderables.addAll(this.playerManager.render());

        // Stage 0: Uncomment this line to render Brutus.
        //renderables.add(this.brutus);

        // Stage 3: Uncomment this line to render the inventory overlays.
        for (Overlay overlay : overlays) {
            renderables.addAll(overlay.render());
        }

        return renderables;
    }
}
