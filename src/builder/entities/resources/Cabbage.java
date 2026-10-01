package builder.entities.resources;

import builder.GameState;
import builder.entities.Interactable;
import builder.ui.SpriteGallery;

import builder.entities.tiles.Tile;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.Entity;
import engine.timing.RepeatingTimer;
import engine.timing.TickTimer;
import engine.timing.TimerDuration;

/**
 * An entity planted (stacked on) {@link builder.entities.tiles.Dirt} that grows and can be
 * collected by the player once grown. A cabbage is initially rendered as 'default' within {@link
 * SpriteGallery#cabbage}.
 *
 * <p>This class represents the in-game cabbage resource and manages its growth and harvesting
 * behaviour.
 */
public class Cabbage extends Entity implements Interactable {

    /** Timer used to advance cabbage growth stages. */
    private final TickTimer timer = new RepeatingTimer(TimerDuration.SHORT);

    /** Sprite group used to render cabbage states. */
    private static final SpriteGroup ART = SpriteGallery.cabbage;

    /** Growth state: 0..4 representing progression from 'default' to 'collectable'. */
    private int growthState = 0;

    /** The cost of planting a cabbage, 2 coins. */
    public static final int COST = 2;

    /** Food gained by harvesting a fully grown cabbage. */
    public static final int FOOD_YIELD = 2;

    /** Coins gained by harvesting a fully grown cabbage. */
    public static final int COIN_YIELD = 3;

    /**
     * Construct a new cabbage entity at the given x, y position.
     *
     * <p>Initially the cabbage is rendered as 'default' within {@link SpriteGallery#cabbage}.
     *
     * <p>x and y must be non-negative and within the window bounds.
     *
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Cabbage(int x, int y) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
    }

    /**
     * Progress the state of the cabbage, updating how it is rendered as required.
     *
     * <p>The cabbage progresses through the following sprites in {@link SpriteGallery#cabbage}:
     * 'default', 'budding', 'growing', 'grown', and finally 'collectable'. The cabbage transitions
     * into its next state periodically.
     *
     * <p>Use {@link RepeatingTimer} and {@link TimerDuration#SHORT} to track timed transitions.
     */
    @Override
    public void tick(EngineState state) {
        this.timer.tick();
        if (this.timer.isFinished()) {
            if (this.growthState < 4) {
                this.growthState++;
            }
            this.updateArt();
        }
    }

    /** Updates the displayed art of this entity based on the current growth state. */
    private void updateArt() {
        String spriteName =
            switch (this.growthState) {
                case 0 -> "default";
                case 1 -> "budding";
                case 2 -> "growing";
                case 3 -> "grown";
                default -> "collectable";
            };

        this.setSprite(ART.getSprite(spriteName));
    }

    /**
     * Returns whether the tile holds a cabbage that has not been harvested or stolen yet.
     * A taken cabbage stays stacked on its tile, marked for removal, until the tile next ticks,
     * so it must not be offered to the player or to pigeons again in the meantime.
     *
     * @param tile the tile to inspect
     * @return true if a live cabbage is stacked on the tile
     */
    public static boolean growsOn(Tile tile) {
        return liveCabbageOn(tile) != null;
    }

    /**
     * Returns the live cabbage stacked on the tile, if any.
     *
     * @param tile the tile to inspect
     * @return the cabbage, or null if there is no cabbage that can still be taken
     */
    public static Cabbage liveCabbageOn(Tile tile) {
        for (Entity entity : tile.getStackedEntities()) {
            if (entity instanceof Cabbage cabbage && !cabbage.isMarkedForRemoval()) {
                return cabbage;
            }
        }
        return null;
    }

    /**
     * Handle collecting a fully grown cabbage. When the player interacts with a fully grown cabbage
     * that has not already been taken, {@link #FOOD_YIELD} food and {@link #COIN_YIELD} coins are
     * added to the player's inventory and the cabbage is removed from the game.
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement. Note that for
     *     left-click behaviour, {@link builder.entities.Usable} should be used instead.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     */
    @Override
    public void interact(EngineState state, GameState game) {
        if (this.growthState >= 4 && !this.isMarkedForRemoval()) {
            game.getInventory().addFood(FOOD_YIELD);
            game.getInventory().addCoins(COIN_YIELD);
            this.markForRemoval();
        }
    }
}
