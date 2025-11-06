package builder.entities.tiles;

import builder.GameState;
import builder.entities.resources.Cabbage;
import builder.ui.SpriteGallery;
import engine.EngineState;

/**
 * A dirt tile may be used for farming.
 * A dirt tile has two states: tilled and untilled. The tile
 * begins untilled and may become tilled by using a hoe on it (stage 3).
 * When untilled, dirt is
 * rendered as {@link SpriteGallery#field};
 * when tilled, dirt is rendered as
 * {@link SpriteGallery#tilled}.
 * (Stage 3) A bucket can be used on tilled dirt to plant a
 * {@link Cabbage} on it.
 */
public class Dirt extends Tile {
    private boolean tilled;

    /**
     * Construct a new untilled dirt tile at the given x, y position.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public Dirt(int x, int y) {
        super(x, y, SpriteGallery.field);
        this.tilled = false;
    }

    /**
     * Whether the dirt is tilled or not.
     * @return true if the dirt is tilled, false otherwise.
     */
    public boolean isTilled() {
        return this.tilled;
    }

    /**
     * Till the dirt, changing its rendering to its tilled state.
     */
    public void till() {
        this.tilled = true;
        setArt(SpriteGallery.tilled);
    }

    /**
     * When a hoe is used on a dirt tile, it should become tilled.
     * When a bucket is used on a dirt
     * tile and all the following conditions are
     * met a cabbage should be planted (placed) upon it:
     * <ul>
     *   <li>There are no other entities stacked on the tile,</li>
     *   <li>The dirt is tilled, and</li>
     *   <li>The inventory has coins &gt;= {@link Cabbage#COST}.</li>
     * </ul>
     * The cost of the cabbage is subtracted from
     * the inventory if planting succeeds.
     * @param state The state of the engine provides
     *              information about which tick this interaction occurred during.
     * @param game The game state that can be queried or updated as needed.
     */
    public void use(EngineState state, GameState game) {
        if (game.getInventory().getHolding().inventorySprite()
                == SpriteGallery.tools.getSprite("hoe")) {
            this.till();
        } else if (game.getInventory().getHolding().inventorySprite()
                == SpriteGallery.tools.getSprite("bucket")) {
            if (this.getStackedEntities().isEmpty() && tilled
                    && game.getInventory().getCoins() >= Cabbage.COST) {
                this.placeOn(new Cabbage(this.getX(), this.getY()));
                game.getInventory().addCoins(- Cabbage.COST);
            }
        }
    }
}
