package builder.ui;

import engine.renderer.Renderable;

import java.util.List;

/**
 * A collection of multiple renderables. Each of the renderables should be displayed by the game.
 *
 * @provided
 */
public interface RenderableGroup {

    /**
     * A collection of renderables that should each be displayed.
     *
     * @return A collection of renderables to display.
     */
    List<Renderable> render();
}
