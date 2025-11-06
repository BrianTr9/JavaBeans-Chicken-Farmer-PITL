package builder.world;

import builder.entities.tiles.Tile;
import builder.entities.tiles.TileFactory;
import engine.renderer.Dimensions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Load an instance of a world from a string representation.
 * <p>Each line of the file, separated by
 * new line characters, corresponds to a row of tiles in the
 * world. Each character represents a tile according to
 * {@link TileFactory#fromSymbol(int, int, char)}.</p>
 */
public class WorldBuilder extends Object {
    /**
     * Construct a new world builder.
     */
    public WorldBuilder() {}

    /**
     * Read the encoded world text and construct
     * the corresponding list of tiles.
     * <p>Each line in the text corresponds to a
     * horizontal row of tiles. Each character corresponds
     * to a tile. A character in the file at line 3,
     * character 10, will correspond to a tile at
     * y=2 and x=9.</p>
     * <p>The character in the encoding indicates
     * the type of tile to construct based on
     * {@link TileFactory#fromSymbol(int, int, char)}.</p>
     * <p>The number of lines and length of those lines
     * must correspond to the dimensions provided.
     * For example, if the window size is 800 and the tile size
     * is 25 then we expect (800/25 =) 32
     * tiles so there must be 32 lines of text and each line
     * must have 32 characters. Otherwise, a
     * {@link WorldLoadException} is thrown.</p>
     * @param dimensions The dimensions of the world.
     *                   The tile encoding must correspond
     *                   to these dimensions.
     * @param text The text encoding of a world.
     * @return A list of tiles loaded from the given string.
     * @throws WorldLoadException If the number of lines
     * doesn't match the required amount according to the dimensions.
     * @throws WorldLoadException If the length of
     * any line doesn't match the required amount according to the dimensions.
     * @throws WorldLoadException If any character
     * doesn't correspond to a tile according to
     * {@link TileFactory#fromSymbol(int, int, char)}.
     */
    public static List<Tile> fromString(Dimensions dimensions, String text)
            throws WorldLoadException {
        int countR = 0;
        int countC = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                if ((dimensions.windowSize() / dimensions.tileSize()) != countC) {
                    throw new WorldLoadException("The length of line doesn't match "
                            + "the required amount according to the dimensions", countR);
                }
                countC = -1;
                countR++;
            }
            countC++;
        }

        if (text.charAt(text.length() - 1) == '\n'
                && dimensions.windowSize() / dimensions.tileSize() != countR) {
            throw new WorldLoadException("The number of lines doesn't match the required amount "
                    + "according to the dimensions");
        }
        if (text.charAt(text.length() - 1) != '\n'
                && dimensions.windowSize() / dimensions.tileSize() != countR + 1) {
            throw new WorldLoadException("The number of lines doesn't match the required amount "
                    + "according to the dimensions");
        }
        List<Tile> list = new ArrayList<>();
        countR = 0;
        countC = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                countC = 0;
                countR++;
            } else {
                try {
                    list.add(TileFactory.fromSymbol(dimensions.tileToPixel(countC),
                            dimensions.tileToPixel(countR), text.charAt(i)));
                } catch (IllegalArgumentException e) {
                    throw new WorldLoadException("Character doesn't correspond to a tile",
                            countR, countC);
                }
                countC++;
            }
        }

        return list;
    }

    /**
     * Load a world from the given file.
     * @param dimensions The dimensions of the world.
     *                   The tile encoding must correspond to these dimensions.
     * @param filepath The path to the file containing the world encoding.
     * @return A {@link BeanWorld} instance representing
     * the world described in the file.
     * @throws IOException If an I/O error occurs reading from the file.
     * @throws WorldLoadException If the world encoding in the file is invalid.
     */
    public static BeanWorld fromFile(Dimensions dimensions, String filepath)
            throws IOException, WorldLoadException {
        String content = Files.readString(Paths.get(filepath));
        List<Tile> tiles = fromString(dimensions, content);
        return fromTiles(tiles);
    }

    /**
     * Create an empty world.
     * @return A {@link BeanWorld} instance representing an empty world.
     */
    public static BeanWorld empty() {
        List<Tile> tiles = new ArrayList<>();
        return fromTiles(tiles);
    }

    /**
     * Convert a list of tiles to a BeanWorld instance.
     * @param tiles The list of tiles to convert.
     * @return A {@link BeanWorld} instance containing the given tiles.
     */
    public static BeanWorld fromTiles(List<Tile> tiles) {
        BeanWorld beanWorld = new BeanWorld();
        for (Tile i : tiles) {
            beanWorld.place(i);
        }

        return beanWorld;
    }
}
