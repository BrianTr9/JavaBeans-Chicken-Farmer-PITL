import builder.JavaBeanFarm;
import builder.world.WorldLoadException;

import engine.Engine;
import engine.game.Game;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import java.io.IOException;

/**
 * A main class to execute the JavaBean game.
 *
 * @provided
 */
public class Main {
    private static final int SIZE = 800;
    private static final int TILES_PER_ROW = 25;

    /**
     * Start the game.
     *
     * @param args Command line arguments, unused in this method.
     * @throws IOException If the map file cannot be found or read from.
     * @throws WorldLoadException If the map file is invalid in some way.
     */
    public static void main(String[] args) throws IOException, WorldLoadException {
        Dimensions dimensions = new TileGrid(TILES_PER_ROW, SIZE);

        // Stage 0: Uncomment after implementing JavaBeanFarm
        Game game = new JavaBeanFarm(dimensions);
        Engine engine = new Engine(game, dimensions);

        // Optionally uncomment this line to turn on debug mode
        // engine.debug().on();

        run(engine);
    }

    /**
     * Helper method to run the game loop.
     *
     * @param engine The engine instance to execute.
     */
    private static void run(Engine engine) {
        while (engine.isRunning()) {
            if (engine.isTimeForNextTick()) {
                engine.tick();
            }
        }
    }
}
