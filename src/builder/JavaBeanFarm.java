package builder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import engine.EngineState;
import engine.game.Game;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import builder.entities.npc.NpcManager;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.spawners.EagleSpawner;
import builder.entities.npc.spawners.MagpieSpawner;
import builder.entities.npc.spawners.PigeonSpawner;
import builder.entities.npc.spawners.Spawner;
import builder.entities.tiles.Dirt;
import builder.entities.tiles.Tile;
import builder.inventory.Inventory;
import builder.inventory.TinyInventory;
import builder.inventory.items.Bucket;
import builder.inventory.items.HiveHammer;
import builder.inventory.items.Hoe;
import builder.inventory.items.Jackhammer;
import builder.inventory.items.Pole;
import builder.inventory.ui.InventoryOverlay;
import builder.inventory.ui.ResourceOverlay;
import builder.player.PlayerManager;
import builder.ui.Overlay;
import builder.world.BeanWorld;
import builder.world.CabbageDetails;
import builder.world.OverlayBuilder;
import builder.world.PlayerDetails;
import builder.world.SpawnerDetails;
import builder.world.WorldBuilder;
import builder.world.WorldLoadException;

/**
 * JavaBeans, a farming game.
 *
 * <p>In this game, the player collects coins by mining ores. The player, a chicken farmer, may use
 * those coins to plant cabbages on tilled dirt.
 *
 * <p>This class manages the world instance, player manager, and the inventory instance. The
 * inventory and resource overlays are also managed by this class.
 */
public class JavaBeanFarm implements Game {

    /** The number of inventory slots available to the player. */
    private static final int INVENTORY_SIZE = 5;

    /** The manager responsible for the player. */
    private final PlayerManager playerManager;

    /** The manager responsible for non-player characters. */
    private final NpcManager npcs;

    /** The manager responsible for enemy entities. */
    private final EnemyManager enemies;

    /** The game world. */
    private final BeanWorld world;

    /** The player's inventory. */
    private final Inventory inventory;

    /** Overlays rendered on top of the game (resource/inventory UI). */
    private final List<Overlay> overlays = new ArrayList<>();

    private String readAllReader(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        StringJoiner sb = new StringJoiner(System.lineSeparator());
        String line;
        while ((line = br.readLine()) != null) {
            sb.add(line);
        }
        return sb.toString();
    }

    /**
     * Constructs a new JavaBean Farm game using the given dimensions,
     * map reader and details reader.
     *
     * @param dimensions The dimensions we want for this game.
     * @param mapReader A reader that contains a description of the world map.
     * @param detailReader A reader that contains the overlay details for the game, e.g. spawner
     *     locations.
     * @throws IOException If the game is unable to find or open the provided readers.
     * @throws WorldLoadException If the provided world details cannot be parsed successfully.
     */
    public JavaBeanFarm(Dimensions dimensions, Reader mapReader, Reader detailReader)
            throws IOException, WorldLoadException {

        final String detailsContent = readAllReader(detailReader);
        final PlayerDetails playerDetails = OverlayBuilder.getPlayerDetailsFromFile(detailsContent);
        this.playerManager = new PlayerManager(playerDetails.getX(), playerDetails.getY());
        this.npcs = new NpcManager();
        this.enemies = new EnemyManager();

        // Wire enemy spawners from details
        addSpawners(
                OverlayBuilder.getMagpieSpawnDetailsFromString(detailsContent),
                sd -> new MagpieSpawner(sd.getX(), sd.getY(), sd.getDuration()));
        addSpawners(
                OverlayBuilder.getEagleSpawnDetailsFromString(detailsContent),
                sd -> new EagleSpawner(sd.getX(), sd.getY(), sd.getDuration()));
        addSpawners(
                OverlayBuilder.getPigeonSpawnDetailsFromString(detailsContent),
                sd -> new PigeonSpawner(sd.getX(), sd.getY(), sd.getDuration()));

        // Build world from map
        String worldContent = readAllReader(mapReader);
        this.world = WorldBuilder.fromTiles(
                WorldBuilder.fromString(dimensions, worldContent));

        // Seed cabbages per details
        populateInitialCabbages(
                dimensions,
                OverlayBuilder.getCabbageSpawnDetailsFromString(detailsContent));

        // Initialize inventory and overlays
        this.inventory = initializeInventory(
                playerDetails.getStartingCoins(), playerDetails.getStartingFood());
        this.overlays.add(new InventoryOverlay(dimensions, INVENTORY_SIZE));
        this.overlays.add(new ResourceOverlay(dimensions));
    }

    /**
     * Constructs a new JavaBean Farm game using file paths for the map and details files.
     *
     * @param dimensions The dimensions we want for this game.
     * @param mapFile Path to the map file.
     * @param detailsFile Path to the details file.
     * @throws IOException If the game is unable to find or open the provided files.
     * @throws WorldLoadException If the provided world details cannot be parsed successfully.
     */
    public JavaBeanFarm(
            Dimensions dimensions,
            String mapFile,
            String detailsFile)
            throws IOException, WorldLoadException {
        this(dimensions, new FileReader(mapFile), new FileReader(detailsFile));
    }

    /**
     * Ticks the internal game state forward by one frame.
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     *
     * <p>Progression order:
     * <ul>
     *   <li>
     *     Advance the player manager via
     *     {@link PlayerManager#tick(EngineState, GameState)}.
     *   </li>
     *   <li>
     *     Advance the world via
     *     {@link BeanWorld#tick(EngineState, GameState)}.
     *   </li>
     * </ul>
     */
    @Override
    public void tick(EngineState state) {
        GameState game =
                new JavaBeanGameState(
                        world, playerManager.getPlayer(), inventory, this.npcs, this.enemies);
        this.playerManager.tick(state, game);
        this.npcs.tick(state, game);
        this.enemies.tick(state, game);

        this.world.tick(state, game);

        for (Overlay overlay : overlays) {
            overlay.tick(state, game);
        }

        this.npcs.interact(state, game);

        this.npcs.cleanup();
        this.enemies.cleanup(game);
    }

    /**
     * A collection of items to render, every component of the game to be rendered should be
     * returned.
     *
     * @return The list of renderables required to draw the whole game.
     *
     * <p>Rendering order:
     * <ol>
     *   <li>
     *     The world's renderables (from {@link BeanWorld#render()}) must be first so they are
     *     drawn behind everything else.
     *   </li>
     *   <li>
     *     The player's renderables (from {@link PlayerManager#render()}) must come after the
     *     world but before overlays.
     *   </li>
     *   <li>
     *     Overlays ({@link ResourceOverlay} and {@link InventoryOverlay}) must be rendered last,
     *     in any order.
     *   </li>
     * </ol>
     */
    @Override
    public List<Renderable> render() {

        List<Renderable> renderables = new ArrayList<>();

        renderables.addAll(this.world.render());

        renderables.addAll(this.npcs.render());
        renderables.addAll(this.enemies.render());

        renderables.addAll(this.playerManager.render());

        for (Overlay overlay : overlays) {
            renderables.addAll(overlay.render());
        }

        return renderables;
    }

    // --- Private helpers to keep constructors lean and avoid duplication ---

    private void addSpawners(
            List<SpawnerDetails> points,
            Function<SpawnerDetails, Spawner> factory) {
        for (SpawnerDetails sd : points) {
            this.enemies.addSpawner(factory.apply(sd));
        }
    }

    private void populateInitialCabbages(
            Dimensions dimensions,
            List<CabbageDetails> cabbages) {
        for (CabbageDetails cabbageDetails : cabbages) {
            final int positionX = cabbageDetails.getX();
            final int positionY = cabbageDetails.getY();
            final List<Tile> tiles =
                    this.world.tilesAtPosition(positionX, positionY, dimensions);
            for (Tile tile : tiles) {
                if (tile instanceof Dirt) {
                    // Till and plant using a temporary inventory with generous resources
                    TinyInventory tempInventory = new TinyInventory(
                            5, 100, 100);
                    ((Dirt) tile).till();
                    ((Dirt) tile).plant(tempInventory);
                }
            }
        }
    }

    private Inventory initializeInventory(int startingCoins, int startingFood) {
        TinyInventory inv = new TinyInventory(
                INVENTORY_SIZE,
                startingCoins,
                startingFood);
        inv.setItem(0, new Bucket());
        inv.setItem(1, new Hoe());
        inv.setItem(2, new Jackhammer());
        inv.setItem(3, new HiveHammer());
        inv.setItem(4, new Pole());
        return inv;
    }
}
