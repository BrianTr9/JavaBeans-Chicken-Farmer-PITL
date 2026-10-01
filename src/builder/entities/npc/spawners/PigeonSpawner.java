package builder.entities.npc.spawners;

import builder.GameState;
import builder.entities.npc.enemies.Pigeon;
import builder.entities.resources.Cabbage;
import builder.entities.tiles.Tile;

import engine.EngineState;

import java.util.List;

/**
 * Spawns pigeons at regular intervals that fly towards and steal cabbages.
 *
 * <p>According to the specification, pigeons are only spawned if there is at least one
 * cabbage in the world to steal. Pigeons fly to the closest cabbage, steal it by removing
 * it from the world, then return to spawn and remove themselves.
 */
public class PigeonSpawner extends AbstractBirdSpawner {

    /**
     * Construct a pigeon spawner with custom spawn interval.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     * @param duration The interval (in ticks) between pigeon spawn attempts.
     */
    public PigeonSpawner(int x, int y, int duration) {
        super(x, y, duration);
    }

    @Override
    protected void spawnBird(EngineState state, GameState game) {
        // Find all tiles with cabbages
        List<Tile> tilesWithCabbages = game.getWorld().tileSelector(Cabbage::growsOn);

        // Only spawn if there are cabbages to steal (per specification)
        if (!tilesWithCabbages.isEmpty()) {
            Tile closestCabbageTile = findClosestTile(tilesWithCabbages);

            game.getEnemies().addBird(
                    new Pigeon(this.getX(), this.getY(), closestCabbageTile));
        }
    }

    /**
     * Check if a tile has a cabbage stacked on it.
     *
     * @param tile The tile to check.
     * @return true if the tile has at least one cabbage, false otherwise.
     */
    private Tile findClosestTile(List<Tile> tiles) {
        final java.util.Iterator<Tile> it = tiles.iterator();
        Tile closest = it.next();
        int minDistance = this.distanceFrom(closest);

        while (it.hasNext()) {
            Tile tile = it.next();
            int distance = this.distanceFrom(tile);
            if (distance < minDistance) {
                closest = tile;
                minDistance = distance;
            }
        }

        return closest;
    }
}
