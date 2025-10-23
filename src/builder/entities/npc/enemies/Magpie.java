package builder.entities.npc.enemies;

import builder.GameState;
import builder.player.Player;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;
import engine.timing.RepeatingTimer;

/**
 * A magpie enemy that flies towards the player and steals coins.
 *
 * <p>According to the specification:
 * <ul>
 *   <li>Flies towards the player</li>
 *   <li>Steals 1 coin when reaching the player</li>
 *   <li>Returns to spawn after stealing</li>
 *   <li>Refunds stolen coin if removed before reaching spawn</li>
 * </ul>
 */
public class Magpie extends AbstractBird {

    private static final SpriteGroup art = SpriteGallery.magpie;
    private RepeatingTimer directionalUpdateTimer = new RepeatingTimer(30);
    private int coins = 0;

    public Magpie(int xCoordinate, int yCoordinate, HasPosition trackedTarget) {
        super(xCoordinate, yCoordinate);
        this.setTrackedTarget(trackedTarget);
        this.setLifespan(new FixedTimer(10000));
        this.setAttacking(true);

        this.setSprite(art.getSprite("down"));

        double deltaX = trackedTarget.getX() - this.getX();
        double deltaY = trackedTarget.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        // Preserve original behavior: one base move, then call move() again later
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
            this.setSpeed(2); // book it
        }

        if (!this.getAttacking()) {
            if (this.distanceFrom(this.getSpawnX(), this.getSpawnY()) < engine.getDimensions().tileSize()) {
                this.markForRemoval();
            }
        }

        // Refund stolen coin if removed before reaching spawn:
        // - If removed while attacking (e.g., lifespan), refund.
        // - Or if removed and still not within a tile of spawn, refund.
        if (this.isMarkedForRemoval()
                && this.coins > 0
                && this.distanceFrom(this.getSpawnX(), this.getSpawnY())
                                > engine.getDimensions().tileSize()) {
            game.getInventory().addCoins(this.coins);
        }
    }

    public int getCoins() {
        return this.coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
