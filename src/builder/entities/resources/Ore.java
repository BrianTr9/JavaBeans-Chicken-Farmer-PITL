package builder.entities.resources;

import builder.GameState;
import builder.entities.Usable;
import builder.inventory.items.Jackhammer;
import builder.player.Player;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.Entity;
import engine.timing.RepeatingTimer;
import engine.timing.TickTimer;

/**
 * An entity that is stacked on an {@link builder.entities.tiles.OreVein} and yields coins when
 * mined. The ore initially has 10 coins and can be mined by the player using the jackhammer. The
 * ore is initially rendered as 'default' within {@link SpriteGallery#rock}.
 *
 * @stage3
 */
public class Ore extends Entity implements Usable {

    private static final SpriteGroup art = SpriteGallery.rock;
    private static final int COIN_VALUE = 10;
    private int coins = COIN_VALUE;

    private final TickTimer timer = new RepeatingTimer(5);
    private static final boolean USE_TIMER = false;

    /**
     * Construct a new ore entity at the given x, y position.
     *
     * <p>Initially the ore is rendered as 'default' within {@link SpriteGallery#rock}.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Ore(int x, int y) {
        super(x, y);
        this.setSprite(art.getSprite("default"));
    }

    /**
     * Progress the state of the ore, updating the sprite to render.
     *
     * <p>If the ore has greater than 90% of its original value remaining then it should remain
     * rendered using 'default'. If the ore has less than or equal to 90% of its original value
     * remaining but more than 10%, it should be rendered using 'damaged' in {@link
     * SpriteGallery#rock}. Otherwise, if the ore has less than or equal to 10% remaining, it should
     * be rendered with 'depleted' in {@link SpriteGallery#rock}.
     */
    @Override
    public void tick(EngineState state) {
        timer.tick();
        double remainingRatio = (double) coins / COIN_VALUE;
        if (remainingRatio > 0.9) {
            this.setSprite(art.getSprite("default"));
        } else if (remainingRatio > 0.1) {
            this.setSprite(art.getSprite("damaged"));
        } else {
            this.setSprite(art.getSprite("depleted"));
        }
    }

    /**
     * When a jackhammer is used on an ore, it takes damage and the player collects coins from it.
     *
     * <p>If the following conditions are met: i) The player is holding a jackhammer, ii) the
     * current tick is a multiple of 5, iii) the remaining value of the ore is greater than zero,
     * then the amount of damage dealt by the player ({@link Player#getDamage()}) is subtracted from
     * the ore's value and added as coins to the player's inventory (if this value is more than the
     * remaining ore's value, all the remaining value is removed and added to the player's
     * inventory).
     */
    @Override
    public void use(EngineState state, GameState game) {
        boolean nthFrame;
        if (USE_TIMER) {
            nthFrame = timer.isFinished();
        } else {
            nthFrame = state.currentTick() % 5 == 0;
        }

        if (nthFrame && game.getInventory().getHolding() instanceof Jackhammer) {
            int collection = Math.min(this.coins, game.getPlayer().getDamage());
            if (collection > 0) {
                this.coins -= collection;
                game.getInventory().addCoins(collection);
            }
        }
    }
}
