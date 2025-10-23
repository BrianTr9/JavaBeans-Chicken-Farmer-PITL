package builder.entities.npc.enemies;

import builder.GameState;
import builder.Tickable;
import builder.entities.Interactable;
import builder.entities.npc.spawners.Spawner;
import builder.player.Player;
import builder.ui.RenderableGroup;

import engine.EngineState;
import engine.game.HasPosition;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages enemies (birds) and their spawners.
 *
 * <p>Responsible for advancing spawners, ticking individual enemies and basic lifecycle
 * management (adding, cleanup and rendering).
 */
public class EnemyManager implements Tickable, Interactable, RenderableGroup {

    /** List of configured spawners. */
    private final ArrayList<Spawner> spawners = new ArrayList<>();

    /** List of active enemy birds. */
    private final ArrayList<Enemy> birds = new ArrayList<>();

    /** X coordinate used by spawners to place newly-created birds. */
    private int spawnX;

    /** Y coordinate used by spawners to place newly-created birds. */
    private int spawnY;

    /**
     * Construct a new enemy manager. The provided dimensions are currently unused but kept
     * to match the historical constructor signature.
     *
     * @param dimensions the renderer dimensions for the game
     */
    @SuppressWarnings("unused")
    public EnemyManager(Dimensions dimensions) {}

    // Accessors for encapsulated fields (minimal API surface)
    /**
     * Get the configured spawners.
     *
     * @return a list of spawners
     */
    public List<Spawner> getSpawners() {
        return this.spawners;
    }

    /**
     * Get the active birds.
     *
     * @return a list of enemy birds
     */
    public List<Enemy> getBirds() {
        return this.birds;
    }

    /** Add a bird to the manager's list. */
    public void addBird(Enemy enemy) {
        this.birds.add(enemy);
    }

    /** Get the spawn X coordinate used by spawners. */
    public int getSpawnX() {
        return this.spawnX;
    }

    /** Set the spawn X coordinate used by spawners. */
    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    /** Get the spawn Y coordinate used by spawners. */
    public int getSpawnY() {
        return this.spawnY;
    }

    /** Set the spawn Y coordinate used by spawners. */
    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    /** Remove any birds that have been marked for removal. */
    public void cleanup() {
        for (int i = this.birds.size() - 1; i >= 0; i -= 1) {
            if (this.birds.get(i).isMarkedForRemoval()) {
                this.birds.remove(i);
            }
        }
    }

    /**
     * Add a spawner to be managed by this enemy manager.
     *
     * @param spawner the spawner to add
     */
    public void add(Spawner spawner) {
        this.spawners.add(spawner);
    }

    /**
     * Create and register a magpie that will track the provided player.
     *
     * @param player the player to target
     * @return the created Magpie
     */
    public Magpie mkM(Player player) {
        final Magpie magpie = new Magpie(this.spawnX, this.spawnY, player);
        this.birds.add(magpie);
        return magpie;
    }

    /**
     * Create and register a pigeon that will track the provided position.
     *
     * @param hasPosition a position to track (usually a cabbage tile)
     * @return the created Pigeon
     */
    public Pigeon mkP(HasPosition hasPosition) {
        final Pigeon pigeon = new Pigeon(this.spawnX, this.spawnY, hasPosition);
        this.birds.add(pigeon);
        return pigeon;
    }

    /**
     * Create an eagle. Note: call sites are responsible for adding the returned eagle to the
     * manager's bird list.
     *
     * @param player the player to target
     * @return the created Eagle
     */
    public Eagle mkE(Player player) {
        return new Eagle(this.spawnX, this.spawnY, player);
    }

    @Override
    public void tick(EngineState state, GameState game) {
        this.cleanup();
        for (Spawner spawner : this.spawners) {
            spawner.tick(state, game);
        }
        for (Enemy bird : birds) {
            if (bird instanceof Magpie temp) {
                temp.tick(state, game);
            }
            if (bird instanceof Eagle temp) {
                temp.tick(state, game);
            }
            if (bird instanceof Pigeon temp) {
                temp.tick(state, game);
            }
        }
    }

    /**
     * Get all magpies currently managed.
     *
     * @return an ArrayList of Magpie instances
     */
    public ArrayList<Magpie> getMagpies() {
        final ArrayList<Magpie> magpies = new ArrayList<>();
        for (Enemy bird : birds) {
            if (bird instanceof Magpie temp) {
                magpies.add(temp);
            }
        }
        return magpies;
    }

    /**
     * Get all enemies managed by this manager.
     *
     * @return an ArrayList of Enemy instances
     */
    public ArrayList<Enemy> getAll() {
        return this.birds;
    }

    @Override
    public void interact(EngineState state, GameState game) {
        /* @todo cleanup */
    }

    @Override
    public List<Renderable> render() {
        return new ArrayList<>(this.birds);
    }
}
