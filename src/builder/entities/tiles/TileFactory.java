package builder.entities.tiles;

/**
 * A tile factory uses the {@link #fromSymbol(int, int, char)} method to construct new tile
 * instances from a set encoding.
 *
 * @stage2
 */
public class TileFactory {
    /**
     * Construct a new tile factory.
     *
     * @hint You do not need to implement this constructor.
     */
    public TileFactory() {}

    /**
     * Construct a new tile based on the symbol encoded at the given position. The following table
     * enumerates the tile encodings.
     *
     * <table>
     *     <tr><th>Character</th><td>Tile</td></tr>
     *     <tr><td>d</td><td>Dirt</td></tr>
     *     <tr><td>t</td><td>Dirt that has been tilled</td></tr>
     *     <tr><td>w</td><td>Water</td></tr>
     *     <tr><td>g</td><td>Grass</td></tr>
     *     <tr><td>o</td><td>OreVein</td></tr>
     *     <caption>&nbsp;</caption>
     * </table>
     *
     * Any characters not listed above should throw an {@link IllegalArgumentException}.
     *
     * @requires x >= 0, y >= 0
     * @param x The x-axis (horizontal) coordinate.
     * @param y The y-axis (vertical) coordinate.
     * @param symbol A symbol to identify the tile type.
     * @return A new tile at the given x,y coordinate of the type specified by the symbol.
     * @throws IllegalArgumentException If symbol does not correspond to a tile.
     */
    public static Tile fromSymbol(int x, int y, char symbol) {
        assert x >= 0 && y >= 0;
        return switch (symbol) {
            case 'd' -> new Dirt(x, y);
            case 'w' -> new Water(x, y);
            case 'g' -> new Grass(x, y);
            case 'o' -> new OreVein(x, y);
            case 't' -> {
                Dirt dirt = new Dirt(x, y);
                dirt.till();
                yield dirt;
            }
            default -> {
                throw new IllegalArgumentException("Symbol does not represent a tile.");
            }
        };
    }
}
