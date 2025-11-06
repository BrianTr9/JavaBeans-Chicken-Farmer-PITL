package builder.ui;

import builder.Tickable;

/**
 * An overlay to be displayed relative to the screen. An overlay consists of a collection of fixed
 * renderables; updates to the overlay are performed via the {@link Tickable#tick} method.
 *
 * @provided
 */
public interface Overlay extends RenderableGroup, Tickable {}
