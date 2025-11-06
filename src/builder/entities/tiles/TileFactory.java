package builder.entities.tiles;

/**
 * A tile factory uses the {@link #fromSymbol(int, int, char)}
 * method to construct new tile
 * instances from a set encoding.
 */
public class TileFactory extends Object {
    /**
     * Construct a new tile factory.
     */
    public TileFactory() {}

    /**
     * Construct a new tile based on the symbol
     * encoded at the given position.
     * <p>The following table enumerates the tile encodings:</p>
     * <pre>
     * Character  Tile
     * d          Dirt
     * t          Dirt (tilled)
     * w          Water
     * g          Grass
     * o          OreVein
     * </pre>
     * Any characters not listed above should throw an
     * {@link IllegalArgumentException}.
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @param symbol A symbol to identify the tile type.
     * @return A new tile at the given x,y coordinate of
     * the type specified by the symbol.
     * @throws IllegalArgumentException If symbol
     * does not correspond to a tile.
     * @requires x >= 0, y >= 0
     */
    public static Tile fromSymbol(int x, int y, char symbol) {
        switch (symbol) {
            case 'd' -> {
                return new Dirt(x, y);
            }
            case 't' -> {
                Dirt dirt = new Dirt(x, y);
                dirt.till();
                return dirt;
            }
            case 'w' -> {
                return new Water(x, y);
            }
            case 'g' -> {
                return new Grass(x, y);
            }
            case 'o' -> {
                return new OreVein(x, y);
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
