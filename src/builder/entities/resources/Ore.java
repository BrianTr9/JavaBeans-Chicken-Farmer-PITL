package builder.entities.resources;

import builder.GameState;
import builder.entities.Usable;
import builder.ui.SpriteGallery;
import engine.EngineState;
import engine.game.Entity;

/**
 * An entity that is stacked on an {@code OreVein} and
 * yields coins when mined. The ore initially
 * has 10 coins of value and can be mined by
 * the player using the jackhammer. The ore is initially
 * rendered as 'default' within {@link SpriteGallery#rock}.
 */
public class Ore extends Entity implements Usable {
    private static final int INITIAL_COINS = 10;
    private int coins = INITIAL_COINS;

    /**
     * Construct a new ore entity at the given x, y position.
     * Initially the ore is rendered as
     * 'default' within {@link SpriteGallery#rock}.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public Ore(int x, int y) {
        super(x, y);
        this.setSprite(SpriteGallery.rock.getSprite("default"));
    }

    /**
     * Progress the state of the ore, updating the sprite to render.
     * <p>If the ore has greater than 90% of its original value
     * remaining it should remain rendered
     * using 'default'. If the ore has less than or equal to 90%
     * of its original value remaining but
     * more than 10%, it should be rendered using
     * 'damaged' in {@link SpriteGallery#rock}.
     * Otherwise, if the ore has less than or equal to 10% remaining,
     * it should be rendered with
     * 'depleted' in {@link SpriteGallery#rock}.</p>
     * @param state The state of the engine, including
     *             input and dimensions.
     */
    public void tick(EngineState state) {
        // thresholds based on INITIAL_COINS
        // > 90% => default, <=90% & >10% => damaged, <=10% => depleted
        if (coins > (int) (INITIAL_COINS * 0.9)) { // i.e. >9 when initial is 10
            this.setSprite(SpriteGallery.rock.getSprite("default"));
        } else if (coins > (int) (INITIAL_COINS * 0.1)) { // >1 when initial is 10
            this.setSprite(SpriteGallery.rock.getSprite("damaged"));
        } else { // <=1
            this.setSprite(SpriteGallery.rock.getSprite("depleted"));
        }
    }

    /**
     * When a jackhammer is used on an ore, it takes damage
     * and the player collects coins from it.
     * <p>If the following conditions are met: (i) The player
     * is holding a jackhammer, (ii) the
     * current tick is a multiple of 5, (iii) the remaining value
     * of the ore is greater than zero,
     * then the amount of damage dealt by the player
     * ({@code Player.getDamage()}) is subtracted from
     * the ore's value and added as coins to the player's
     * inventory (if this value is more than the
     * remaining ore's value, all the remaining value
     * is removed and added to the player's
     * inventory).</p>
     * @param state The state of the engine provides
     *              timing information (for tick multiple logic).
     * @param game The game state that can be queried or updated as needed.
     */
    public void use(EngineState state, GameState game) {
        if (game.getInventory().getHolding().inventorySprite()
                == SpriteGallery.tools.getSprite("jackhammer")
                && state.currentTick() % 5 == 0 && this.coins > 0) {
            if (this.coins < game.getPlayer().getDamage()) {
                game.getInventory().addCoins(this.coins);
                this.coins = 0;
            } else {
                this.coins -= game.getPlayer().getDamage();
                game.getInventory().addCoins(game.getPlayer().getDamage());
            }
        }
    }
}
