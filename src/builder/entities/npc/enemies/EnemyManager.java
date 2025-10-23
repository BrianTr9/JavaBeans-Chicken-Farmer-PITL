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

public class EnemyManager implements Tickable, Interactable, RenderableGroup {

    private final ArrayList<Spawner> spawners = new ArrayList<>();
    private final ArrayList<Enemy> birds = new ArrayList<>();
    private int spawnX;
    private int spawnY;

    public EnemyManager(Dimensions dimensions) {}

    // Accessors for encapsulated fields (minimal API surface)
    public List<Spawner> getSpawners() {
        return this.spawners;
    }

    public List<Enemy> getBirds() {
        return this.birds;
    }

    public void addBird(Enemy enemy) {
        this.birds.add(enemy);
    }

    public int getSpawnX() {
        return this.spawnX;
    }

    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public int getSpawnY() {
        return this.spawnY;
    }

    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    public void cleanup() {
        for (int i = this.birds.size() - 1; i >= 0; i -= 1) {
            if (this.birds.get(i).isMarkedForRemoval()) {
                this.birds.remove(i);
            }
        }
    }

    /**
     * @param spawner
     */
    public void add(Spawner spawner) {
        this.spawners.add(spawner);
    }

    public Magpie mkM(Player player) {
        final Magpie magpie = new Magpie(this.spawnX, this.spawnY, player);
        this.birds.add(magpie);
        return magpie;
    }

    public Pigeon mkP(HasPosition hasPosition) {
        final Pigeon pigeon = new Pigeon(this.spawnX, this.spawnY, hasPosition);
        this.birds.add(pigeon);
        return pigeon;
    }

    public Eagle mkE(Player player) {
        final Eagle eagle = new Eagle(this.spawnX, this.spawnY, player);
        return eagle;
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
     * Get all {@link Magpie}s positions from the enemy manager.
     *
     * @return all {@link Magpie}s positions from the enemy manager.
     */
    public ArrayList<Magpie> getMagpies() {
        final ArrayList<Magpie> magpies = new ArrayList<Magpie>();
        for (Enemy bird : birds) {
            if (bird instanceof Magpie temp) {
                magpies.add(temp);
            }
        }
        return magpies;
    }

    public ArrayList<Enemy> getALl() {
        return this.birds;
    }

    /**
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     */
    @Override
    public void interact(EngineState state, GameState game) {
        /* @todo cleanup */
    }

    @Override
    public List<Renderable> render() {
        return new ArrayList<>(this.birds);
    }
}
