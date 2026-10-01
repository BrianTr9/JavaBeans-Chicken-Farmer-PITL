package builder.entities;

import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.game.Direction;
import engine.game.Entity;
import engine.timing.*;

import java.util.Random;

/**
 * Brutus is a character that runs randomly around the world. He is useful for testing that the game
 * can be run. Brutus is rendered using the {@link builder.ui.SpriteGallery#brutus} sprites.
 *
 * <p>Every 100 ticks, Brutus will pick a new direction to move in randomly and continue moving in
 * that direction until the next change of direction.
 */
public class Brutus extends Entity {
    private final SpriteGroup art = SpriteGallery.brutus;
    private final Animation leftWalking =
            new Animation(
                    AnimationDuration.FAST,
                    new Sprite[] {
                        art.getSprite("left"), art.getSprite("left1"), art.getSprite("left2")
                    });
    private final Animation rightWalking =
            new Animation(
                    AnimationDuration.FAST,
                    new Sprite[] {
                        art.getSprite("right"), art.getSprite("right1"), art.getSprite("right2")
                    });
    private final Animation upWalking =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {art.getSprite("up"), art.getSprite("up1"), art.getSprite("up2")});
    private final Animation downWalking =
            new Animation(
                    AnimationDuration.SLOW,
                    new Sprite[] {
                        art.getSprite("down"), art.getSprite("down1"), art.getSprite("down2")
                    });

    private Direction travelling = Direction.SOUTH;
    private TickTimer timer = new RepeatingTimer(TimerDuration.SHORT);

    /**
     * Construct a new Brutus at the given x, y position.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     */
    public Brutus(int x, int y) {
        super(x, y);
        assert x >= 0 && y >= 0;
    }

    @Override
    public void tick(EngineState state) {
        timer.tick();
        if (timer.isFinished()) {
            // pick a new direction to travel
            Random random = new Random();
            travelling = Direction.values()[random.nextInt(4)];
        }

        // always progress animation
        leftWalking.tick(state);
        rightWalking.tick(state);
        downWalking.tick(state);
        upWalking.tick(state);

        // set the sprites appropriately
        switch (travelling) {
            case EAST -> setSprite(rightWalking);
            case WEST -> setSprite(leftWalking);
            case NORTH -> setSprite(upWalking);
            case SOUTH -> setSprite(downWalking);
        }

        // move player in direction
        switch (travelling) {
            case NORTH -> setY(getY() - 1);
            case SOUTH -> setY(getY() + 1);
            case EAST -> setX(getX() + 1);
            case WEST -> setX(getX() - 1);
        }
    }
}
