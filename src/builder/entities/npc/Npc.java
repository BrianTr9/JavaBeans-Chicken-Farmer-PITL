package builder.entities.npc;

import builder.GameState;
import builder.Tickable;
import builder.entities.Interactable;

import engine.EngineState;
import engine.game.Entity;
import engine.game.HasPosition;

/**
 * Base non-player character class used for NPC behaviour in the game.
 * <p>
 * Provides movement, direction and simple steering helpers shared across NPCs.
 */
public class Npc extends Entity implements Interactable, Tickable, Directable {

    private int direction = 0;
    private double speed = 1;

    /**
     * Construct an NPC at the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Npc(int x, int y) {
        super(x, y);
    }

    /**
     * Get the current movement speed of this NPC.
     *
     * @return the speed as a double
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the movement speed of this NPC.
     *
     * @param speed the new speed value
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Get the current movement direction of this NPC in degrees.
     *
     * @return the direction in degrees
     */
    public int getDirection() {
        return this.direction;
    }

    /**
     * Set the movement direction of this NPC in degrees.
     *
     * @param direction the direction in degrees
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Move this NPC one step based on its current direction and speed.
     */
    public void move() {
        final int deltaX = (int) Math.round(Math.cos(Math.toRadians(this.direction)) * this.speed);
        final int deltaY = (int) Math.round(Math.sin(Math.toRadians(this.direction)) * this.speed);
        this.setX(this.getX() + deltaX);
        this.setY(this.getY() + deltaY);
    }

    @Override
    public void tick(EngineState state) {
        this.move();
    }

    @Override
    public void tick(EngineState state, GameState game) {
        this.move();
    }

    @Override
    public void interact(EngineState state, GameState game) {
        // intentionally empty
    }

    /**
     * Return how far away this npc is from the given position.
     *
     * @param position the position we are measuring to from this npc's position
     * @return integer representation for how far apart they are
     */
    public int distanceFrom(HasPosition position) {
        int deltaX = position.getX() - this.getX();
        int deltaY = position.getY() - this.getY();
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Return how far away this npc is from the given coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return integer representation for how far apart they are
     */
    public int distanceFrom(int x, int y) {
        int deltaX = x - this.getX();
        int deltaY = y - this.getY();
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    // --- Shared steering helpers for all NPCs ---
    /**
     * Steer towards the given target position. No-op if target is null.
     *
     * @param target the position to steer towards
     */
    protected void steerTowards(HasPosition target) {
        if (target == null) {
            return;
        }
        final double deltaX = target.getX() - this.getX();
        final double deltaY = target.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }

    /**
     * Steer towards the given x,y coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    protected void steerTowards(int x, int y) {
        final double deltaX = x - this.getX();
        final double deltaY = y - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }
}
