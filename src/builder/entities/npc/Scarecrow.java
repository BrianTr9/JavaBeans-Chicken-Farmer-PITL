package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Magpie;
import builder.entities.npc.enemies.Pigeon;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;

/**
 * A scarecrow that petrifies nearby magpies and pigeons, causing them to return to spawn.
 */
public class Scarecrow extends Npc {

    /**
     * The coin cost to place a scarecrow.
     */
    public static final int COIN_COST = 2;
    private static final SpriteGroup ART = SpriteGallery.scarecrow;

    /**
     * Construct a scarecrow at the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Scarecrow(int x, int y) {
        super(x, y);
        this.setSprite(ART.getSprite("default"));
        this.setSpeed(0);
    }

    @Override
    public void tick(EngineState state) {
        super.tick(state);
    }

    @Override
    public void interact(EngineState state, GameState game) {
        super.interact(state, game);
        final EnemyManager enemies = game.getEnemies();
        final int scareRadius = state.getDimensions().tileSize() * 4; // inclusive radius per spec

        for (Enemy bird : enemies.getBirds()) {
            if (bird instanceof Magpie magpie) {
                if (this.distanceFrom(magpie) <= scareRadius) {
                    magpie.setAttacking(false); // immediate effect
                }
            } else if (bird instanceof Pigeon pigeon) {
                if (this.distanceFrom(pigeon) <= scareRadius) {
                    pigeon.setAttacking(false); // immediate effect
                }
            }
        }
    }
}
