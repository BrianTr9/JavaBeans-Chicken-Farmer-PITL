package builder.entities.npc;

import builder.GameState;
import builder.entities.npc.enemies.Enemy;
import builder.entities.npc.enemies.EnemyManager;
import builder.entities.npc.enemies.Magpie;
import builder.entities.npc.enemies.Pigeon;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;

import java.util.ArrayList;

public class Scarecrow extends Npc {

    public static final int COIN_COST = 2;
    private static final SpriteGroup art = SpriteGallery.scarecrow;

    public Scarecrow(int x, int y) {
        super(x, y);
        this.setSprite(art.getSprite("default"));
        this.setSpeed(0);
    }

    @Override
    public void tick(EngineState state) {
        super.tick(state);
    }

    @Override
    public void interact(EngineState state, GameState game) {
        super.interact(state, game);
        EnemyManager enemies = game.getEnemies();
        final ArrayList<Magpie> magpies = new ArrayList<>();
        final ArrayList<Pigeon> pigeons = new ArrayList<>();
        for (Enemy bird : enemies.Birds) {
            if (bird instanceof Magpie) {
                magpies.add((Magpie) bird);
            }
            if (bird instanceof Pigeon) {
                pigeons.add((Pigeon) bird);
            }
        }

        final int scareRadius = state.getDimensions().tileSize() * 4;

        for (Magpie magpie : magpies) {
            if (this.distanceFrom(magpie) < scareRadius) {
                magpie.attacking = false;
                // trigger the scare animation
            }
        }

        for (Pigeon pigeon : pigeons) {
            if (this.distanceFrom(pigeon) < scareRadius) {
                pigeon.attacking = false;
                // trigger the scare animation
            }
        }
    }
}
