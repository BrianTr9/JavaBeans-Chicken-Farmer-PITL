package builder.entities.tiles;

import builder.GameState;
import builder.entities.Interactable;
import builder.entities.Usable;
import builder.ui.RenderableGroup;

import engine.EngineState;
import engine.art.ArtNotFoundException;
import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;
import engine.game.Entity;
import engine.game.HasTick;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tile on the 'ground' of our world. Each tile is responsible for managing:
 *
 * <ul>
 *   <li>what entities are stacked upon it,
 *   <li>gathering the {@link Renderable}s for itself and its stacked entities, and
 *   <li>(in stage 3) interactions with itself and entities stacked upon it (related: {@link
 *       Interactable} and {@link Usable}).
 * </ul>
 *
 * @invariant getX() >= 0, getX() is less than the window height
 * @invariant getY() >= 0, getY() is less than the window width
 * @hint The {@link Interactable} and {@link Usable} interfaces do not need to be implemented until
 *     stage 3.
 * @stage2
 */
public abstract class Tile extends Entity
        implements Interactable, Usable, RenderableGroup, HasTick {

    private SpriteGroup art;
    private final List<Entity> stackedEntities = new ArrayList<>();

    /**
     * Constructs an instance of {@link Tile}.
     *
     * @requires x >= 0, x is less than the window width
     * @requires y >= 0, y is less than the window height
     * @requires The given sprite group must contain a 'default' sprite.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @param art The sprite group art to use for this tile, the tile will initially render as the
     *     'default' sprite for this group.
     */
    public Tile(int x, int y, SpriteGroup art) {
        super(x, y);
        setArt(art);
    }

    /**
     * Set the sprite group for this tile and updates the current sprite (see {@link
     * #updateSprite(String)}) to the 'default' sprite of the given group.
     *
     * @requires The given sprite group must contain a 'default' sprite.
     * @param art A sprite group to use for this tile's sprites.
     */
    public void setArt(SpriteGroup art) {
        this.art = art;
        updateSprite("default");
    }

    /**
     * Change the current sprite (see {@link #setSprite(Sprite)}) to the given artwork name within
     * the tiles current art (i.e. the sprite group provided to the constructor or set by {@link
     * #setArt(SpriteGroup)}).
     *
     * @param artName The name of the art within the sprite group.
     * @throws ArtNotFoundException If the given name doesn't exist within the sprite group.
     * @hint You don't need to do anything special to throw {@link ArtNotFoundException}, {@link
     *     SpriteGroup#getSprite(String)} will do it for you.
     */
    public void updateSprite(String artName) throws ArtNotFoundException {
        this.setSprite(art.getSprite(artName));
    }

    /**
     * Progress the state of the tile. The tile's state is progressed by first cleaning up (removing
     * any stacked entities that are marked for removal according to {@link #isMarkedForRemoval()})
     * then progressing each of the stacked entities by calling their {@link
     * Entity#tick(EngineState)} method.
     *
     * @hint You may have to modify a list while iterating through it, this will throw a {@link
     *     java.util.ConcurrentModificationException}. There are a few ways to work around this, see
     *     <a href="https://edstem.org/au/courses/23940/discussion/2833772">this Ed post</a> for
     *     options.
     */
    @Override
    public void tick(EngineState engine) {
        this.cleanup();
        for (Entity stackedEntity : this.stackedEntities) {
            stackedEntity.tick(engine);
        }
    }

    /** Removes any stacked entities that are marked for removal. */
    private void cleanup() {
        for (int i = this.stackedEntities.size() - 1; i >= 0; i -= 1) {
            if (this.stackedEntities.get(i).isMarkedForRemoval()) {
                this.stackedEntities.remove(i);
            }
        }
    }

    /**
     * Return the list of entities stacked upon this tile.
     *
     * <p>Modifying the returned list must not modify the tile's state (although modifying the
     * entities within will).
     *
     * @return Any entities stacked onto this tile, e.g. {@link builder.entities.resources.Cabbage}.
     */
    public List<Entity> getStackedEntities() {
        return new ArrayList<>(this.stackedEntities);
    }

    /**
     * Place the given tile on top of this tile.
     *
     * @param tile The tile instance to place.
     * @ensures The tile is contained within getStackedEntities()
     */
    public void placeOn(Entity tile) {
        this.stackedEntities.add(tile);
    }

    /**
     * Handle player interaction with the tile. When a tile is interacted with, any of its
     * interactable stacked entities (i.e. {@link Interactable} instances in {@link
     * #getStackedEntities()}) must also be interacted with.
     *
     * @param state The state of the engine, including the mouse, keyboard information and
     *     dimension. Useful for processing keyboard presses or mouse movement. Note that for
     *     left-click behaviour, {@link Usable} should be used instead.
     * @param game The state of the game, including the player and world. Can be used to query or
     *     update the game state.
     * @stage3
     */
    @Override
    public void interact(EngineState state, GameState game) {
        for (Entity stackedEntity : this.stackedEntities) {
            if (stackedEntity instanceof Interactable interactable) {
                interactable.interact(state, game);
            }
        }
    }

    /**
     * Handle the player attempting to use this tile. When a tile is used, any of its usable stacked
     * entities (i.e. {@link Usable} instances in {@link #getStackedEntities()}) must also be used,
     * i.e. have their {@link Usable#use(EngineState, GameState)} method called.
     *
     * @param state The state of the engine provides information about which tick this interaction
     *     occurred during.
     * @param game The game state that can be queried or updated as needed.
     * @stage3
     */
    @Override
    public void use(EngineState state, GameState game) {
        for (Entity stackedEntity : this.stackedEntities) {
            if (stackedEntity instanceof Usable usable) {
                usable.use(state, game);
            }
        }
    }

    /**
     * Whether this tile can be walked through by other entities. True by default.
     *
     * @return true if this tile can be walked through, false otherwise.
     */
    public boolean canWalkThrough() {
        return true;
    }

    /**
     * A collection of items to render, including the tile and any entities stacked on it.
     *
     * <p>This tile must be the first renderable in the list so that it is rendered behind each
     * stacked entity. The remaining list must match the order of {@link #getStackedEntities()}.
     *
     * @return The list of renderables required to draw this tile to the screen.
     */
    @Override
    public List<Renderable> render() {
        List<Renderable> result = new ArrayList<>(List.of(this));
        result.addAll(getStackedEntities());
        return result;
    }
}
