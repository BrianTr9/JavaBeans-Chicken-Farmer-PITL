package builder.entities.npc;

import engine.timing.FixedTimer;

/**
 * Indicates an entity's state is time-limited and can expire after a lifespan.
 *
 * <p>Implementers provide a {@link engine.timing.FixedTimer} that controls when the
 * object should be considered expired.
 */
public interface Expirable {

    /**
     * Set the lifespan timer for this object.
     *
     * @param lifespan the FixedTimer that drives expiration
     */
    void setLifespan(FixedTimer lifespan);

    /**
     * Get the lifespan timer for this object, or null if none is set.
     *
     * @return the FixedTimer used for expiration, or null
     */
    FixedTimer getLifespan();
}
