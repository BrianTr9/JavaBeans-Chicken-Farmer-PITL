package builder.entities.npc.enemies;

import builder.GameState;
import builder.entities.npc.Expirable;
import builder.player.Player;
import builder.ui.SpriteGallery;

import engine.EngineState;
import engine.art.sprites.SpriteGroup;
import engine.game.HasPosition;
import engine.timing.FixedTimer;
import engine.timing.RepeatingTimer;

public class Magpie extends Enemy implements Expirable {

    private static final SpriteGroup art = SpriteGallery.magpie;
    private FixedTimer lifespan = new FixedTimer(10000);
    public HasPosition trackedTarget;
    public Boolean attacking;
    public int coins = 0;

    private RepeatingTimer directionalUpdateTimer = new RepeatingTimer(30);

    private final int spawnX;
    private final int spawnY;

    public Magpie(int xCoordinate, int yCoordinate, HasPosition trackedTarget) {
        super(xCoordinate, yCoordinate);
        this.spawnX = xCoordinate;
        this.spawnY = yCoordinate;
        this.trackedTarget = trackedTarget;
        this.setSprite(art.getSprite("down"));
        this.attacking = true;

        double deltaX = trackedTarget.getX() - this.getX();
        double deltaY = trackedTarget.getY() - this.getY();
        this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
    }

    @Override
    public FixedTimer getLifespan() {
        return lifespan;
    }

    @Override
    public void setLifespan(FixedTimer timer) {
        this.lifespan = timer;
    }

    @Override
    public void tick(EngineState engine, GameState game) {
        super.tick(engine, game);
        this.lifespan.tick();
        if (this.lifespan.isFinished()) {
            this.markForRemoval();
        }
        if (this.attacking) {
            double deltaX = trackedTarget.getX() - this.getX();
            double deltaY = trackedTarget.getY() - this.getY();
            this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
            /** target is below */
            if (trackedTarget.getY() > this.getY()) {
                this.setSprite(art.getSprite("down"));
            } else {
                this.setSprite(art.getSprite("up"));
            }
        } else {
            double deltaX = this.spawnX - this.getX();
            double deltaY = this.spawnY - this.getY();
            this.setDirection((int) Math.toDegrees(Math.atan2(deltaY, deltaX)));
            if (this.spawnY < this.getY()) {
                this.setSprite(art.getSprite("up"));
            } else {
                this.setSprite(art.getSprite("down"));
            }
        }
        this.move();
        this.directionalUpdateTimer.tick();

        Player player = game.getPlayer();

        final boolean hasHitPlayer =
                this.distanceFrom(player.getX(), player.getY()) < engine.getDimensions().tileSize();
        if (hasHitPlayer && game.getInventory().getCoins() > 0 && this.attacking) {
            game.getInventory().addCoins(-1);
            this.coins += 1;
            this.attacking = false;
            this.setSpeed(2); // book it
        }

        if (!attacking) {
            if (this.distanceFrom(spawnX, spawnY) < engine.getDimensions().tileSize()) {
                this.markForRemoval();
            }
        }

        if (this.isMarkedForRemoval() && attacking) {
            game.getInventory().addCoins(this.coins);
        }
    }

    @Override
    public void interact(EngineState engine, GameState game) {}

    public Boolean getAttacking() {
        return this.attacking;
    }

    public void setAttacking(Boolean attacking) {
        this.attacking = attacking;
    }

    public int getCoins() {
        return this.coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public HasPosition getTrackedTarget() {
        return this.trackedTarget;
    }

    public void setTrackedTarget(HasPosition trackedTarget) {
        this.trackedTarget = trackedTarget;
    }
}
