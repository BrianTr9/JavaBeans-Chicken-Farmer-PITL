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

/**
 * An instance of the player entity. The chicken farmer
 * is rendered to the screen and moved by the
 * {@link builder.player.PlayerManager}.
 * <p>Note: All references to sprites are sprites
 * within {@link SpriteGallery#chickenFarmer}.</p>
 */
public class ChickenFarmer extends Entity implements Player {
    private final SpriteGroup art = SpriteGallery.chickenFarmer;
    private final Animation left =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                            art.getSprite("left"), art.getSprite("left1"), art.getSprite("left2")
                    });
    private final Animation right =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                            art.getSprite("right"), art.getSprite("right1"), art.getSprite("right2")
                    });
    private final Animation up =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {art.getSprite("up"), art.getSprite("up1"), art.getSprite("up2")});
    private final Animation down =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                            art.getSprite("down"), art.getSprite("down1"), art.getSprite("down2")
                    });

    private Animation animation = null;

    /**
     * Constructs a chicken farmer instance at the given coordinates.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @requires x >= 0, x is less than the window width,
     * y >= 0, y is less than the window height
     */
    public ChickenFarmer(int x, int y) {
        super(x, y);
        this.setSprite(down);
    }

    /**
     * Returns the amount of damage dealt by a player hit.
     * A chicken farmer deals 2 damage with each hit.
     * @return The amount of damage a player deals.
     */
    @Override
    public int getDamage() {
        return 2;
    }

    /**
     * Move the player by the given amount in the given direction.
     * <p>Update the player's x or y position according to
     * the following table:</p>
     * <pre>
     * Direction  x change  y change
     * NORTH      0        -amount
     * SOUTH      0         amount
     * EAST       amount    0
     * WEST      -amount    0
     * </pre>
     * The player's sprite is also updated: NORTH -> 'up',
     * SOUTH -> 'down', EAST/WEST -> the
     * appropriate directional walking animation.
     * <p>Note: Moving to a negative position is unspecified
     * and won't be tested.</p>
     * @param direction The direction to move in.
     * @param amount How many pixels to move the player.
     * @requires amount > 0
     */
    public void move(Direction direction, int amount) {
        switch (direction) {
            case NORTH -> {
                this.setY(this.getY() - amount);
                this.setSprite(up);
            }
            case SOUTH -> {
                this.setY(this.getY() + amount);
                this.setSprite(down);
            }
            case EAST -> {
                this.setX(this.getX() + amount);
                this.setSprite(right);
            }
            case WEST -> {
                this.setX(this.getX() - amount);
                this.setSprite(left);
            }
            default -> { }
        }
    }

    /**
     * Progress the state of the player. The player is progressed
     * by first setting the displayed
     * sprite to 'down' (to undo any moving animations).
     * Then any animations stored by the player
     * are progressed by calling {@link Animation#tick(EngineState)}.
     * @param state The state of the engine, including input and dimensions.
     */
    @Override
    public void tick(EngineState state) {
        left.tick(state);
        right.tick(state);
        down.tick(state);
        up.tick(state);
        if (this.animation != null) {
            animation.tick(state);
        }
    }

    /**
     * Animate the player using an item. If the given item is null
     * (the player is not holding an
     * item) nothing happens. If the item's {@code Item.useAnimation()}
     * is present the player stores
     * that animation and sets its sprite to show it.
     * @param item The item that the player is currently holding.
     */
    public void use(Item item) {
        if (item == null) {
            this.animation = null;
            setSprite(down);
        } else if (item.useAnimation().isPresent()) {
            if (this.animation != item.useAnimation().get()) {
                this.animation = item.useAnimation().get();
            }
            setSprite(this.animation);
        } else {
            this.animation = null;
            setSprite(down);
        }

    }
}
