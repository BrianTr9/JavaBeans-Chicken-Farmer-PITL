package builder.entities.npc.enemies;

import builder.GameState;
import builder.player.Player;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;
import engine.timing.RepeatingTimer;

public class Magpie extends AbstractBird {

    private static final SpriteGroup art = SpriteGallery.magpie;
    private RepeatingTimer directionalUpdateTimer = new RepeatingTimer(30);

    // Backward-compat: Scarecrow sets this field directly; keep it and sync with base attacking
    public Boolean attacking;

    public int coins = 0;

    public Magpie(int xCoordinate, int yCoordinate, HasPosition trackedTarget) {
        super(xCoordinate, yCoordinate);
        this.setTrackedTarget(trackedTarget);
        this.setLifespan(new FixedTimer(10000));

        this.attacking = true; // keep public field for compatibility
        this.setAttacking(true);

        this.setSprite(art.getSprite("down"));

        double deltaX = trackedTarget.getX() - this.getX();
        double deltaY = trackedTarget.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        // Sync external changes on the public field into the base state before ticking
        if (this.attacking != null && this.attacking.booleanValue() != this.getAttacking()) {
            this.setAttacking(this.attacking);
        }

        // preserve original behavior: one base move, then call move() again later
        this.baseTickMove(engine, game);

        if (this.getLifespan() != null) {
            this.getLifespan().tick();
            if (this.getLifespan().isFinished()) {
                this.markForRemoval();
            }
        }

        if (this.getAttacking()) {
            this.steerTowards(getTrackedTarget());
            this.updateVerticalSprite(art, getTrackedTarget().getY());
        } else {
            this.steerTowards(this.getSpawnX(), this.getSpawnY());
            this.updateVerticalSpriteTowardsSpawn(art, this.getSpawnY());
        }
        this.move();
        this.directionalUpdateTimer.tick();

        Player player = game.getPlayer();

        final boolean hasHitPlayer =
                this.distanceFrom(player.getX(), player.getY()) < engine.getDimensions().tileSize();
        if (hasHitPlayer && game.getInventory().getCoins() > 0 && this.getAttacking()) {
            game.getInventory().addCoins(-1);
            this.coins += 1;
            this.setAttacking(false);
            this.attacking = false; // keep public field in sync
            this.setSpeed(2); // book it
        }

        if (!this.getAttacking()) {
            if (this.distanceFrom(this.getSpawnX(), this.getSpawnY()) < engine.getDimensions().tileSize()) {
                this.markForRemoval();
            }
        }

        // keep original refund condition using public field (behavior-preserving)
        if (this.isMarkedForRemoval() && attacking) {
            game.getInventory().addCoins(this.coins);
        }

        // Sync base attacking back to public field so external code sees updated state
        this.attacking = this.getAttacking();
    }

    @Override
    public boolean getAttacking() {
        return (this.attacking != null) ? this.attacking.booleanValue() : super.getAttacking();
    }

    @Override
    public void setAttacking(boolean attacking) {
        this.attacking = attacking; // keep public field for external access
        super.setAttacking(attacking); // keep base state aligned
    }

    public int getCoins() {
        return this.coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
