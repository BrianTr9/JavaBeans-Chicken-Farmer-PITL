package builder.entities.tiles;

import builder.GameState;
import builder.ui.SpriteGallery;
import engine.EngineState;

/**
 * A grass tile is a basic tile. A grass tile can be walked through.
 * A grass tile is rendered as
 * {@link SpriteGallery#grass}. (Stage 3) A hoe can be used on
 * the grass tile to turn it into a
 * {@link Dirt} tile.
 */
public class Grass extends Tile {
    /**
     * Construct a new grass tile at the given x, y position.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public Grass(int x, int y) {
        super(x, y, SpriteGallery.grass);
    }

    /**
     * When a hoe is used on a grass tile, it should be marked for
     * removal and replaced with a dirt
     * tile at the same location. If the tile is already marked for
     * removal then it should not be
     * replaced.
     * @param state The state of the engine provides information
     *              about which tick this interaction occurred during.
     * @param game The game state that can be queried or
     *             updated as needed.
     */
    @Override
    public void use(EngineState state, GameState game) {
        if (this.isMarkedForRemoval()) {
            return; // already processed
        }
        if (game.getInventory().getHolding().inventorySprite()
                == SpriteGallery.tools.getSprite("hoe")) {
            this.markForRemoval();
            game.getWorld().place(new Dirt(this.getX(), this.getY()));
        }
    }
}
