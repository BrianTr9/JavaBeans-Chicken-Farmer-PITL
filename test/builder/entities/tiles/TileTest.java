package builder.entities.tiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import builder.GameFixture;
import builder.GameState;
import builder.entities.Interactable;
import builder.entities.Usable;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.game.Entity;
import engine.renderer.Renderable;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TileTest {

    private GameFixture fixture;
    private Tile tile;

    @Before
    public void setUp() {
        fixture = new GameFixture();
        tile = new Grass(GameFixture.FIRST_TILE, GameFixture.FIRST_TILE);
    }

    @Test
    public void newTileHasNothingStackedAndIsWalkable() {
        assertTrue(tile.getStackedEntities().isEmpty());
        assertTrue(tile.canWalkThrough());
        assertSame(SpriteGallery.grass.getSprite("default"), tile.getSprite());
    }

    @Test
    public void waterIsNotWalkable() {
        assertFalse(new Water(0, 0).canWalkThrough());
    }

    @Test
    public void stackedEntitiesAreReturnedAsACopy() {
        Probe probe = new Probe();
        tile.placeOn(probe);
        List<Entity> stacked = tile.getStackedEntities();
        stacked.clear();
        assertEquals(1, tile.getStackedEntities().size());
        assertSame(probe, tile.getStackedEntities().get(0));
    }

    @Test
    public void tickRemovesMarkedEntitiesAndTicksTheRest() {
        Probe kept = new Probe();
        Probe removed = new Probe();
        tile.placeOn(kept);
        tile.placeOn(removed);
        removed.markForRemoval();

        tile.tick(GameFixture.engine(0));

        assertEquals(List.of(kept), tile.getStackedEntities());
        assertEquals(1, kept.ticks);
        assertEquals("removed entities are not ticked", 0, removed.ticks);
    }

    @Test
    public void interactAndUseAreForwardedToStackedEntities() {
        Probe probe = new Probe();
        tile.placeOn(probe);
        tile.placeOn(new Plain()); // neither Interactable nor Usable: must be skipped

        tile.interact(GameFixture.engine(0), fixture.game);
        tile.use(GameFixture.engine(0), fixture.game);

        assertEquals(1, probe.interactions);
        assertEquals(1, probe.uses);
    }

    @Test
    public void renderDrawsTileBeneathStackedEntities() {
        Probe first = new Probe();
        Probe second = new Probe();
        tile.placeOn(first);
        tile.placeOn(second);
        List<Renderable> rendered = tile.render();
        assertEquals(List.of(tile, first, second), rendered);
    }

    @Test
    public void setArtShowsTheNewDefaultSprite() {
        tile.setArt(SpriteGallery.tilled);
        assertSame(SpriteGallery.tilled.getSprite("default"), tile.getSprite());
    }

    /** Records every call made on it. */
    private static class Probe extends Entity implements Interactable, Usable {
        private int ticks = 0;
        private int interactions = 0;
        private int uses = 0;

        Probe() {
            super(0, 0);
        }

        @Override
        public void tick(EngineState state) {
            ticks++;
        }

        @Override
        public void interact(EngineState state, GameState game) {
            interactions++;
        }

        @Override
        public void use(EngineState state, GameState game) {
            uses++;
        }
    }

    /** An entity with no interactions. */
    private static class Plain extends Entity {
        Plain() {
            super(0, 0);
        }

        @Override
        public void tick(EngineState state) {}
    }
}
