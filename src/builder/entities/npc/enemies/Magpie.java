package builder.entities.npc.enemies;

import builder.GameState;
import builder.player.Player;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;

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

    /** Magpie sprite group. */
    private static final SpriteGroup ART = SpriteGallery.magpie;

    /** Number of coins currently stolen by this magpie. */
    private int coins = 0;

    /**
     * Construct a magpie that will target the provided player-like position.
     *
     * @param x horizontal spawn coordinate
     * @param y vertical spawn coordinate
     * @param trackedTarget the player to track
     */
    public Magpie(int x, int y, HasPosition trackedTarget) {
        super(x, y);
        this.setTrackedTarget(trackedTarget);
        this.setLifespan(new FixedTimer(10000));
        this.setAttacking(true);

        this.setSprite(ART.getSprite("down"));

        double deltaX = trackedTarget.getX() - this.getX();
        double deltaY = trackedTarget.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }

    /**
     * Magpies take two simulation steps per frame. The game's system tests are calibrated
     * to this pace.
     *
     * @return 2
     */
    @Override
    public int ticksPerFrame() {
        return 2;
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        // Birds move twice per tick: once before and once after re-steering.
        this.baseTickMove(engine, game);

        if (this.getLifespan() != null) {
            this.getLifespan().tick();
            if (this.getLifespan().isFinished()) {
                this.markForRemoval();
            }
        }

        if (this.getAttacking()) {
            this.steerTowards(getTrackedTarget());
            this.updateVerticalSprite(ART, getTrackedTarget().getY());
        } else {
            this.steerTowards(this.getSpawnX(), this.getSpawnY());
            this.updateVerticalSpriteTowardsSpawn(ART, this.getSpawnY());
        }

        this.move();

        Player player = game.getPlayer();

        final boolean hasHitPlayer = this.distanceFrom(player.getX(), player.getY())
                < engine.getDimensions().tileSize();
        if (hasHitPlayer && game.getInventory().getCoins() > 0 && this.getAttacking()) {
            game.getInventory().addCoins(-1);
            this.coins += 1;
            this.setAttacking(false);
            this.setSpeed(2); // book it
        }

        if (!this.getAttacking() && this.isNear(engine, this.getSpawnX(), this.getSpawnY())) {
            this.removeAtSpawn();
        }
    }

    /**
     * Gives stolen coins back to the player unless the magpie made it home with them.
     *
     * @param game the current game state
     */
    @Override
    public void onRemoved(GameState game) {
        if (!this.hasReturnedHome() && this.coins > 0) {
            game.getInventory().addCoins(this.coins);
            this.coins = 0;
        }
    }

    /**
     * Get the number of coins currently held by this magpie.
     *
     * @return the number of stolen coins
     */
    public int getCoins() {
        return this.coins;
    }

    /**
     * Set the number of coins currently held by this magpie.
     *
     * @param coins the number of coins to set
     */
    public void setCoins(int coins) {
        this.coins = coins;
    }
}
