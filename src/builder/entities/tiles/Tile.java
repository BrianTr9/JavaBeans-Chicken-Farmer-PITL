package builder.entities.tiles;

import builder.GameState;
import builder.entities.Interactable;
import builder.entities.Usable;
import builder.ui.RenderableGroup;
import engine.EngineState;
import engine.art.ArtNotFoundException;
import engine.art.sprites.SpriteGroup;
import engine.game.Entity;
import engine.game.HasTick;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tile on the 'ground' of our world.
 * Each tile is responsible for managing:
 * <ul>
 *   <li>what entities are stacked upon it,</li>
 *   <li>gathering the {@link Renderable}s for itself
 *   and its stacked entities, and</li>
 *   <li>(in stage 3) interactions with itself and
 *   entities stacked upon it (related:
 *       {@link Interactable} and {@link Usable}).</li>
 * </ul>
 * <p><strong>Invariant:</strong><br>
 * getX() &gt;= 0, getX() is less than the window height,
 * getY() &gt;= 0, getY() is less than the window width.</p>
 */
public abstract class Tile extends Entity
        implements Interactable, Usable, RenderableGroup, HasTick {
    private SpriteGroup art;
    private List<Entity> stackedTiles;

    /**
     * Constructs an instance of Tile.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @param art The sprite group art to use for this tile,
     *           the tile will initially render as the
     *            'default' sprite for this group.
     * @requires x &gt;= 0, x is less than the window width,
     * y &gt;= 0, y is less than the window height,
     *           The given sprite group must contain a 'default' sprite.
     */
    public Tile(int x, int y, SpriteGroup art) {
        super(x, y);
        setArt(art);
        this.stackedTiles = new ArrayList<>();
    }

    /**
     * Set the sprite group for this tile and updates
     * the current sprite (see
     * {@link #updateSprite(String)}) to the 'default'
     * sprite of the given group.
     * @param art A sprite group to use for this tile's sprites.
     * @requires The given sprite group must
     * contain a 'default' sprite.
     */
    public void setArt(SpriteGroup art) {
        this.art = art;
        this.updateSprite("default");
    }

    /**
     * Change the current sprite (see Entity.setSprite(Object))
     * to the given artwork name
     * within the tile's current art (i.e.
     * the sprite group provided to the constructor or set by
     * {@link #setArt(SpriteGroup)}).
     * @param artName The name of the art within the sprite group.
     * @throws ArtNotFoundException If the given name
     * doesn't exist within the sprite group.
     * <p><strong>Hint:</strong> No special handling needed;
     * {@link SpriteGroup#getSprite(String)}
     * throws the exception if the art is missing.</p>
     */
    public void updateSprite(String artName) throws ArtNotFoundException {
        this.setSprite(this.art.getSprite(artName));
    }

    /**
     * Progress the state of the tile. The tile's state is
     * progressed by first cleaning up (removing
     * any stacked entities that are marked
     * for removal according to {@link Entity#isMarkedForRemoval()})
     * then progressing each of the stacked entities
     * by calling their {@link HasTick#tick(EngineState)}
     * method.
     * @param engine The state of the engine,
     *               including the mouse, keyboard information and dimension.
     */
    public void tick(EngineState engine) { // Improve later
        List<Entity> toRemove = new ArrayList<>();

        for (Entity i : this.stackedTiles) {
            if (i.isMarkedForRemoval()) {
                toRemove.add(i);
            }
        }

        this.stackedTiles.removeAll(toRemove);

        for (Entity i : this.stackedTiles) {
            i.tick(engine);
        }
    }

    /**
     * Return the list of entities stacked upon this tile.
     * <p>Modifying the returned list must not modify
     * the tile's state (although modifying the
     * entities within will).</p>
     * @return Any entities stacked onto this tile, e.g. Cabbage.
     */
    public List<Entity> getStackedEntities() {
        return new ArrayList<>(this.stackedTiles);
    }

    /**
     * Place the given tile (entity) on top of this tile.
     * @param tile The tile instance to place.
     * @ensures The tile is contained within {@link #getStackedEntities()}.
     */
    public void placeOn(Entity tile) {
        this.stackedTiles.add(tile);
    }

    /**
     * Handle player interaction with the tile.
     * When a tile is interacted with, any of its
     * interactable stacked entities (i.e. {@link Interactable}
     * instances in {@link #getStackedEntities()})
     * must also be interacted with.
     * @param state The state of the engine (input and dimension data).
     *             Note for left-click behaviour,
     *              {@link Usable} should be used instead.
     * @param game The state of the game (player, world, inventory, etc.).
     */
    public void interact(EngineState state, GameState game) {
        for (Entity i : this.getStackedEntities()) {
            if (i instanceof Interactable) {
                ((Interactable) i).interact(state, game);
            }
        }
    }

    /**
     * Handle the player attempting to use this tile.
     * When a tile is used, any of its usable stacked
     * entities (i.e. {@link Usable} instances in
     * {@link #getStackedEntities()}) must also be used.
     * @param state The state of the engine providing timing/input context.
     * @param game The game state that can be queried or updated as needed.
     */
    public void use(EngineState state, GameState game) {
        for (Entity i : this.getStackedEntities()) {
            if (i instanceof Usable u) {
                u.use(state, game);
            }
        }
    }

    /**
     * Whether this tile can be walked through
     * by other entities. True by default.
     * @return true if this tile can be walked through,
     * false otherwise.
     */
    public boolean canWalkThrough() {
        return true;
    }

    /**
     * A collection of items to render, including the tile
     * and any entities stacked on it. This tile
     * must be the first renderable in the list so that
     * it is rendered behind each stacked entity.
     * The remaining list must match the order of {@link #getStackedEntities()}.
     * @return The list of renderables required to draw
     * this tile to the screen.
     */
    public List<Renderable> render() {
        List<Renderable> renderables = new ArrayList<>();
        renderables.add(this);
        renderables.addAll(this.getStackedEntities());
        return renderables;
    }
}