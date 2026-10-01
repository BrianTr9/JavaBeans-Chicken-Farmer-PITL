package builder.entities.npc.enemies;

import builder.GameState;
import builder.Tickable;
import builder.entities.npc.spawners.Spawner;
import builder.ui.RenderableGroup;

import engine.EngineState;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Manages enemies (birds) and the spawners that create them.
 *
 * <p>Responsible for advancing spawners, ticking each enemy and basic lifecycle management
 * (adding, cleanup and rendering). Spawners decide what to create and where; this class only
 * owns the collections.
 *
 * <p>Invariant: neither collection contains {@code null}, and every enemy is stored at most
 * once, so each enemy is ticked exactly once per frame.
 */
public class EnemyManager implements Tickable, RenderableGroup {

    /** Configured spawners, ticked in insertion order. */
    private final List<Spawner> spawners = new ArrayList<>();

    /** Active enemy birds, ticked and rendered in insertion order. */
    private final List<Enemy> birds = new ArrayList<>();

    /** Construct an enemy manager with no spawners and no birds. */
    public EnemyManager() {}

    /**
     * Returns a read-only view of the configured spawners.
     *
     * @return an unmodifiable list of spawners
     */
    public List<Spawner> getSpawners() {
        return Collections.unmodifiableList(this.spawners);
    }

    /**
     * Returns a read-only view of the active birds.
     *
     * <p>The view reflects later additions and removals; copy it before iterating if the
     * manager may change during iteration.
     *
     * @return an unmodifiable list of enemy birds
     */
    public List<Enemy> getBirds() {
        return Collections.unmodifiableList(this.birds);
    }

    /**
     * Adds a spawner to be ticked by this manager.
     *
     * <p>Precondition: {@code spawner} is not null.
     *
     * @param spawner the spawner to add
     */
    public void addSpawner(Spawner spawner) {
        assert spawner != null;
        this.spawners.add(spawner);
    }

    /**
     * Adds a bird to the manager so it is ticked and rendered each frame.
     *
     * <p>Precondition: {@code enemy} is not null and is not already managed.
     *
     * @param enemy the bird to add
     */
    public void addBird(Enemy enemy) {
        assert enemy != null;
        assert !this.birds.contains(enemy);
        this.birds.add(enemy);
    }

    /**
     * Removes any birds that have been marked for removal, calling
     * {@link Enemy#onRemoved(GameState)} on each one.
     *
     * @param game the current game state
     */
    public void cleanup(GameState game) {
        final Iterator<Enemy> iterator = this.birds.iterator();
        while (iterator.hasNext()) {
            final Enemy bird = iterator.next();
            if (bird.isMarkedForRemoval()) {
                iterator.remove();
                bird.onRemoved(game);
            }
        }
    }

    /**
     * Ticks every spawner, then every bird (including any spawned during this tick).
     *
     * <p>Each bird is ticked {@link Enemy#ticksPerFrame()} times in a row, stopping early once
     * it has been removed (so an expired bird cannot, for example, steal on its second step).
     *
     * @param state the current engine state
     * @param game the current game state
     */
    @Override
    public void tick(EngineState state, GameState game) {
        this.cleanup(game);
        for (Spawner spawner : this.spawners) {
            spawner.tick(state, game);
        }
        for (Enemy bird : new ArrayList<>(this.birds)) {
            for (int step = 0; step < bird.ticksPerFrame()
                    && !bird.isMarkedForRemoval(); step++) {
                bird.tick(state, game);
            }
        }
    }

    @Override
    public List<Renderable> render() {
        return new ArrayList<>(this.birds);
    }
}
