package builder.world;

import engine.game.ImmutablePosition;

/**
 * Details required to place an initial cabbage in the world.
 *
 * <p>Implementations provide an immutable position (x/y) where a cabbage should be spawned during
 * world construction or seeding.
 */
public interface CabbageDetails extends ImmutablePosition {

}
