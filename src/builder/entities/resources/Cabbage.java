package builder.entities.resources;

import builder.GameState;
import builder.entities.Interactable;
import builder.ui.SpriteGallery;
import engine.EngineState;
import engine.game.Entity;

/**
 * An entity planted (stacked on) {@link builder.entities.tiles.Dirt}
 * that grows and can be
 * collected by the player once grown. A cabbage is initially
 * rendered as 'default' within
 * {@link SpriteGallery#cabbage}.
 */
public class Cabbage extends Entity implements Interactable {
    public static final int COST = 2;
    private int count;
    private String[] spriteState;

    /**
     * Construct a new cabbage entity at the given x, y position.
     * Initially the cabbage is rendered
     * as 'default' within {@link SpriteGallery#cabbage}.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public Cabbage(int x, int y) {
        super(x, y);
        this.setSprite(SpriteGallery.cabbage.getSprite("default"));
        this.count = 0;
        this.spriteState = new String[]{"default", "budding", "growing", "grown", "collectable"};
    }

    /**
     * Progress the state of the cabbage, updating how it is
     * rendered as required. The cabbage
     * progresses through the following sprites in
     * {@link SpriteGallery#cabbage}: 'default',
     * 'budding', 'growing', 'grown', and finally 'collectable'.
     * The cabbage transitions into its
     * next state every 100 ticks.
     * @param state The state of the engine, including timing and input.
     */
    public void tick(EngineState state) {
        if (state.currentTick() % 100 == 0 && this.count < 4) {
            count++;
            this.setSprite(SpriteGallery.cabbage.getSprite(spriteState[count]));
        }
    }

    /**
     * Handle collecting a fully grown cabbage. When the player
     * interacts with a fully grown
     * ("collectable") cabbage the cost of the cabbage
     * ({@link #COST}) is added to the player's
     * food, 3 coins are added to the player's inventory,
     * and the cabbage is removed from the game.
     * @param state The state of the engine (input/dimensions context).
     * @param game The game state that can be queried or updated as needed.
     */
    public void interact(EngineState state, GameState game) {
        if (count == 4) {
            game.getInventory().addFood(COST);
            game.getInventory().addCoins(3);
            this.markForRemoval();
        }
    }
}
