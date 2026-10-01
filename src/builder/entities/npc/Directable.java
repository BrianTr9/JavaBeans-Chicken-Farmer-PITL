package builder.entities.npc;

/**
 * Interface for objects that have a direction and can be moved.
 */
public interface Directable {

    /**
     * Get the current direction in degrees.
     *
     * @return the direction in degrees
     */
    int getDirection();

    /**
     * Set the current direction in degrees.
     *
     * @param direction the direction in degrees
     */
    void setDirection(int direction);

    /**
     * Advance the implementing object one movement step according to its direction/speed.
     */
    void move();
}
