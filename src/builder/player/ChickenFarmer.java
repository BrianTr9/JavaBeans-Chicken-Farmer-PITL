package builder.player;

import builder.inventory.items.Item;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.game.Direction;
import engine.game.Entity;
import engine.timing.Animation;
import engine.timing.AnimationDuration;

import java.util.Optional;

/**
 * An instance of the player entity.
 *
 * <p>The chicken farmer is rendered to the screen and moved by the {@link PlayerManager}.
 *
 * <p>Note: All references to sprites are sprites within {@link SpriteGallery#chickenFarmer}.
 *
 * @hint Use the provided {@link builder.entities.Brutus} class as a rough guide.
 * @hint The player must maintain three animations (see {@link Animation}): the left walking
 *     animation ('left', 'left1', 'left2' on {@link AnimationDuration#SLOW}), the right walking
 *     animation ('right', 'right1', 'right2' on {@link AnimationDuration#SLOW}), and the use
 *     animation (in stage 3) set by the {@link #use(Item)} method. The player instance should store
 *     each animation as a member variable and progress it during the tick method. When the player
 *     needs to run the animation, it should call {@link #setSprite(Sprite)} and pass the stored
 *     animation.
 */
public class ChickenFarmer extends Entity implements Player {

    private static final int DAMAGE = 2;

    private static final SpriteGroup art = SpriteGallery.chickenFarmer;
    private Animation useAnimation = null;
    private final Animation walkLeft =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                        art.getSprite("left"), art.getSprite("left1"), art.getSprite("left2")
                    });
    private final Animation walkRight =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                        art.getSprite("right"), art.getSprite("right1"), art.getSprite("right2")
                    });

    /**
     * Constructs a chicken farmer instance at the given coordinates.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public ChickenFarmer(int x, int y) {
        super(x, y);
        assert x >= 0 && y >= 0;
    }

    /**
     * Returns the amount of damage dealt by a player hit. A chicken farmer deals 2 damage with each
     * hit.
     *
     * @return The amount of damage a player deals.
     */
    @Override
    public int getDamage() {
        return DAMAGE;
    }

    /**
     * Move the player by the given amount in the given direction.
     *
     * <p>Update the player's x or y position according to the following table.
     *
     * <table>
     *     <tr><th>Direction</th><th>x</th><th>y</th></tr>
     *     <tr><td>NORTH</td><td></td><td>-amount</td></tr>
     *     <tr><td>SOUTH</td><td></td><td>amount</td></tr>
     *     <tr><td>EAST</td><td>amount</td><td></td></tr>
     *     <tr><td>WEST</td><td>-amount</td><td></td></tr>
     *     <caption>&nbsp;</caption>
     * </table>
     *
     * The player's sprite should also be updated based on the move. If the player moves north, the
     * sprite should be set to 'up'. If the player moves south, the sprite should be set to 'down'.
     * If the player moves either east or west, the sprite should be set to the appropriate
     * animation step of that direction, see the hint in the class comment.
     *
     * <p>Note: Moving to a negative position is unspecified and won't be tested.
     *
     * @requires amount > 0
     * @param direction The direction to move in.
     * @param amount How many pixels to move the player.
     */
    public void move(Direction direction, int amount) {
        switch (direction) {
            case NORTH -> {
                setY(getY() - amount);
                this.setSprite(art.getSprite("up"));
            }
            case SOUTH -> {
                setY(getY() + amount);
                this.setSprite(art.getSprite("down"));
            }
            case EAST -> {
                setX(getX() + amount);
                this.setSprite(this.walkRight);
            }
            case WEST -> {
                setX(getX() - amount);
                this.setSprite(this.walkLeft);
            }
            default -> {
                this.setSprite(art.getSprite("down"));
            }
        }
    }

    /**
     * Progress the state of the player. The player is progressed by first setting the displayed
     * sprite to 'down' (to undo any moving animations). Then any animations stored by the player
     * should be progressed (by calling {@link Animation#tick(EngineState)}).
     */
    @Override
    public void tick(EngineState state) {
        this.setSprite(art.getSprite("down"));

        // progress animations forward
        this.walkLeft.tick(state);
        this.walkRight.tick(state);
        if (useAnimation != null) {
            useAnimation.tick(state);
        }
    }

    /**
     * Animate the player using an item.
     *
     * <p>If the given item is null (the player is not holding an item) nothing should happen.
     *
     * <p>If the item's {@link Item#useAnimation()} is not empty (i.e. {@link Optional#isPresent()}
     * is true) then the player should store that animation and set its sprite to show the
     * animation.
     *
     * @param item The item that the player is currently holding.
     * @stage3
     */
    public void use(Item item) {
        if (item != null && item.useAnimation().isPresent()) {
            this.useAnimation = item.useAnimation().get();
            this.setSprite(this.useAnimation);
        }
    }
}
