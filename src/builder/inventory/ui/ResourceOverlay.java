package builder.inventory.ui;

import builder.GameState;
import builder.ui.Overlay;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.renderer.Dimensions;
import engine.renderer.Renderable;
import engine.ui.TextWithIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays onscreen indicators for the amount of food and coins in the player's inventory.
 *
 * @provided
 */
public class ResourceOverlay implements Overlay {

    private static final SpriteGroup art = SpriteGallery.icons;
    private final TextWithIcon foodDisplay;
    private final TextWithIcon coinDisplay;

    private static final int HORIZONTAL_INSET = 40;
    private static final int VERTICAL_INSET = 40;

    /**
     * Construct a new resource overlay for the given dimensions.
     *
     * @param dimensions The dimensions used to render the overlay.
     */
    public ResourceOverlay(Dimensions dimensions) {
        int tileWidth = dimensions.tileSize();
        Sprite foodIcon = art.getSprite("food");
        Sprite coinIcon = art.getSprite("material");
        foodDisplay = new TextWithIcon(foodIcon, HORIZONTAL_INSET, VERTICAL_INSET, tileWidth);
        coinDisplay = new TextWithIcon(coinIcon, HORIZONTAL_INSET, VERTICAL_INSET * 2, tileWidth);
    }

    /**
     * Progress the resource overlay by updating the food and coin counts displayed by the overlay.
     */
    @Override
    public void tick(EngineState state, GameState game) {
        this.foodDisplay.update(game.getInventory().getFood() + "");
        this.coinDisplay.update(game.getInventory().getCoins() + "");
    }

    /**
     * A collection of items to render, the food and coin amounts.
     *
     * @return The list of renderables required to draw the resource indicators to the screen.
     */
    @Override
    public List<Renderable> render() {
        List<Renderable> result = new ArrayList<Renderable>();
        result.addAll(this.foodDisplay.render());
        result.addAll(this.coinDisplay.render());
        return result;
    }
}
