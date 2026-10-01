package builder.world;

import builder.entities.tiles.Tile;
import builder.entities.tiles.TileFactory;

import engine.renderer.Dimensions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Load an instance of a world from a string representation.
 *
 * <p>Each line of the file, separated by new line characters, corresponds to a row of tiles in the
 * world. Each character represents a tile according to {@link TileFactory#fromSymbol(int, int,
 * char)}.
 *
 * @stage2
 */
public class WorldBuilder {

    /**
     * Construct a new world builder.
     *
     * @hint You do not need to implement this.
     */
    public WorldBuilder() {}

    /**
     * Read the encoded world text and construct the corresponding list of tiles.
     *
     * <p>Each line in the text corresponds to a horizontal row of tiles. Each character correspond
     * to a tile. A character in the file at line 3, character 10, will correspond to a tile at y=2
     * and x=9.
     *
     * <p>The character in the encoding indicates the type of tile to construct based on {@link
     * TileFactory#fromSymbol(int, int, char)}.
     *
     * <p>The number of lines and length of those lines must correspond to the dimensions provided.
     * For example, if the window size is 800 and the tile size is 25 then we expect (800/25 =) 32
     * tiles so there must be 32 lines of text and each line must have 32 characters. Otherwise, a
     * {@link WorldLoadException} is thrown.
     *
     * @param dimensions The dimensions of the world. The tile encoding must correspond to these
     *     dimensions.
     * @param text The text encoding of a world.
     * @return A list of tiles loaded from the given string.
     * @throws WorldLoadException If the number of lines doesn't match the required amount according
     *     to the dimensions.
     * @throws WorldLoadException If the length of any line doesn't match the required amount
     *     according to the dimensions.
     * @throws WorldLoadException If any character doesn't correspond to a tile according to {@link
     *     TileFactory#fromSymbol(int, int, char)}.
     */
    public static List<Tile> fromString(Dimensions dimensions, String text)
            throws WorldLoadException {
        int numberOfTiles = dimensions.windowSize() / dimensions.tileSize();
        String[] lines = text.split("\n");
        final boolean lineDesync = lines.length != numberOfTiles;
        if (lineDesync) {
            throw new WorldLoadException(
                    "Expected "
                            + numberOfTiles
                            + " lines to match the given dimensions but got "
                            + lines.length);
        }

        final List<Tile> tiles = new ArrayList<>();
        for (int row = 0; row < numberOfTiles; row++) {
            char[] currentRow = lines[row].toCharArray();

            if (currentRow.length != numberOfTiles) {
                throw new WorldLoadException(
                        "Expected "
                                + numberOfTiles
                                + " characters to match the given dimensions but got "
                                + currentRow.length,
                        row);
            }

            for (int col = 0; col < numberOfTiles; col++) {
                int tileX = dimensions.tileToPixel(col);
                int tileY = dimensions.tileToPixel(row);
                char symbol = currentRow[col];
                Tile tile;
                try {
                    tile = TileFactory.fromSymbol(tileX, tileY, symbol);
                } catch (IllegalArgumentException e) {
                    throw new WorldLoadException("Unknown symbol: '" + symbol + "'", row, col);
                }
                tiles.add(tile);
            }
        }
        return tiles;
    }

    /**
     * Read the provided file and attempt to create a new world based on the tile encoding in the
     * file.
     *
     * <p>See {@link #fromString(Dimensions, String)} for a description of how the tile encoding is
     * read.
     *
     * @param dimensions The dimensions of the world. The tile encoding must correspond to these
     *     dimensions.
     * @param filepath The path to a file containing a tile encoding.
     * @return A new world containing all tiles in the specified file.
     * @throws IOException If the file path doesn't exist or otherwise can't be read.
     * @throws WorldLoadException If the tile encoding is invalid (according to {@link
     *     #fromString(Dimensions, String)}).
     */
    public static BeanWorld fromFile(Dimensions dimensions, String filepath)
            throws IOException, WorldLoadException {
        String text = Files.readString(Path.of(filepath));
        return fromTiles(fromString(dimensions, text));
    }

    /**
     * Construct a new empty world, i.e. with no tiles.
     *
     * @return A new empty world.
     */
    public static BeanWorld empty() {
        return new BeanWorld();
    }

    /**
     * Construct a new world containing all the tiles in the parameter.
     *
     * @param tiles Tiles to populate the world.
     * @return A new world containing all given tiles.
     */
    public static BeanWorld fromTiles(List<Tile> tiles) {
        BeanWorld world = new BeanWorld();
        for (Tile tile : tiles.reversed()) { // reverse so tests don't implicitly rely on order
            world.place(tile);
        }
        return world;
    }
}
