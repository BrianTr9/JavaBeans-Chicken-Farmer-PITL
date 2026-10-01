package builder.entities.tiles;

import builder.GameState;
import builder.entities.npc.Scarecrow;
import builder.entities.resources.Cabbage;
import builder.inventory.Inventory;
import builder.inventory.items.Bucket;
import builder.inventory.items.Hoe;
import builder.inventory.items.Pole;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;

/**
 * A dirt tile may be used for farming. A dirt tile has two states: tilled and untilled. The tile
 * begins untilled and becomes tilled when a hoe is used on it. When untilled, dirt is rendered
 * as {@link SpriteGallery#field}, when tilled, dirt is rendered as {@link SpriteGallery#tilled}.
 * A bucket plants a cabbage on tilled dirt, and a pole builds a scarecrow on it.
 */
public class Dirt extends Tile {

    private static final SpriteGroup dirtArt = SpriteGallery.field;
    private static final SpriteGroup tillArt = SpriteGallery.tilled;
    private boolean tilled = false;

    /**
     * Construct a new untilled dirt tile at the given x, y position.
     *
     * <p>x and y must be non-negative and within the window bounds.
     *
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Dirt(int x, int y) {
        super(x, y, dirtArt);
    }

    /**
     * Whether the dirt is tilled or not.
     *
     * @return true if the dirt is tilled, false otherwise.
     */
    public boolean isTilled() {
        return this.tilled;
    }

    /** Till the dirt, changing its rendering to its tilled state. */
    public void till() {
        this.tilled = true;
        this.setArt(tillArt);
    }

    /**
     * Attempt to plant a {@link Cabbage} and adjust the resources accordingly. A cabbage is only
     * planted on tilled dirt with nothing on it, and only if the inventory can pay
     * {@link Cabbage#COST}.
     *
     * @param inventory the inventory that pays for the cabbage
     * @return true if a cabbage was planted
     */
    public boolean plant(Inventory inventory) {
        if (!this.isTilled()
                || !this.getStackedEntities().isEmpty()
                || inventory.getCoins() < Cabbage.COST) {
            return false;
        }
        inventory.addCoins(-Cabbage.COST);
        this.placeOn(new Cabbage(this.getX(), this.getY()));
        return true;
    }

    /**
     * When a hoe is used on a dirt tile, it should become tilled.
     *
     * <p>When a bucket is used on a dirt tile and the following conditions are met a cabbage should
     * be planted (placed) upon it. Conditions:
     *
     * <ol>
     *   <li>There are no other entities stacked on the tile,
     *   <li>the dirt is tilled, and
     *   <li>the inventory has coins greater than or equal to the cost of a cabbage (i.e. {@link
     *       Cabbage#COST}).
     * </ol>
     *
     * <p>The cost of the cabbage should be subtracted from the inventory if it is successfully
     * planted.
     *
     * <p>Hoe, bucket and pole interactions are handled here.
     */
    @Override
    public void use(EngineState state, GameState game) {
        Inventory inventory = game.getInventory();
        if (inventory.getHolding() instanceof Hoe) {
            this.till();
        }
        if (inventory.getHolding() instanceof Bucket) {
            this.plant(inventory);
        }
        if (inventory.getHolding() instanceof Pole
                && this.getStackedEntities().isEmpty()
                && this.isTilled()
                && inventory.getCoins() >= Scarecrow.COIN_COST) {
            inventory.addCoins(-Scarecrow.COIN_COST);
            Scarecrow scarecrow = new Scarecrow(this.getX(), this.getY());
            this.placeOn(scarecrow);
            game.getNpcs().addNpc(scarecrow);
        }
    }
}
