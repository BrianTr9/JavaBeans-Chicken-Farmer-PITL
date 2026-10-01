package builder.entities.tiles;

import builder.GameState;
import builder.entities.npc.BeeHive;
import builder.inventory.items.HiveHammer;
import builder.inventory.items.Hoe;
import builder.ui.SpriteGallery;

import engine.EngineState;

/**
 * A grass tile is a basic tile. A grass tile can be walked through. A grass tile is rendered as
 * {@link SpriteGallery#grass}. (Stage 3) A hoe can be used on the grass tile to turn it into a
 * {@link Dirt} tile.
 *
 * @stage2
 */
public class Grass extends Tile {

    /**
     * Construct a new grass tile at the given x, y position.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Grass(int x, int y) {
        super(x, y, SpriteGallery.grass);
    }

    /**
     * When a hoe is used on a grass tile, it should be marked for removal and replaced with a dirt
     * tile at the same location.
     *
     * <p>If the tile is already marked for removal (according to {@link #markForRemoval()}) then it
     * should not be replaced.
     *
     * @stage3
     */
    @Override
    public void use(EngineState state, GameState game) {
        super.use(state, game);
        if (isMarkedForRemoval()) {
            return;
        }
        // confirm they are holding the hoe, and there is nothing already on this grass!
        if (game.getInventory().getHolding() instanceof Hoe
                && this.getStackedEntities().isEmpty()) {
            this.markForRemoval();
            Tile dirt = TileFactory.fromSymbol(this.getX(), this.getY(), 'd');
            game.getWorld().place(dirt);
        }

        if (game.getInventory().getHolding() instanceof HiveHammer
                && this.getStackedEntities().isEmpty()
                && game.getInventory().getCoins() >= BeeHive.COIN_COST
                && game.getInventory().getFood() >= BeeHive.FOOD_COST) {
            game.getInventory().addCoins(-BeeHive.COIN_COST);
            game.getInventory().addFood(-BeeHive.FOOD_COST);
            BeeHive beehive = new BeeHive(this.getX(), this.getY());
            this.placeOn(beehive);
            game.getNpcs().addNpc(beehive);
        }
    }
}
