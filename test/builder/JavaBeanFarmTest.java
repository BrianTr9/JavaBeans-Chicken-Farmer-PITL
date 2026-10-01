package builder;

import static org.junit.Assert.assertFalse;

import builder.world.WorldLoadException;

import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loading a game from map and details sources. */
public class JavaBeanFarmTest {

    private static final Dimensions DIMENSIONS = new TileGrid(25, 800);

    private static String windowsLineEndings(Path path) throws IOException {
        return Files.readString(path).replace("\r\n", "\n").replace("\n", "\r\n");
    }

    @Test
    public void loadsTheDefaultLevelFromFilePaths() throws IOException, WorldLoadException {
        JavaBeanFarm game = new JavaBeanFarm(
                DIMENSIONS, "resources/uqLogo.map", "resources/uqLogo.details");
        assertFalse(game.render().isEmpty());
    }

    @Test
    public void loadsLevelsSavedWithWindowsLineEndings() throws IOException, WorldLoadException {
        // Regression: on Windows the loader re-joined lines with "\r\n", which the map parser
        // then rejected as an unknown tile symbol.
        JavaBeanFarm game = new JavaBeanFarm(DIMENSIONS,
                new StringReader(windowsLineEndings(Path.of("resources/uqLogo.map"))),
                new StringReader(windowsLineEndings(Path.of("resources/uqLogo.details"))));
        assertFalse(game.render().isEmpty());
    }
}
