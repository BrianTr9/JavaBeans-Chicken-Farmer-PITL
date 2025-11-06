package builder.world;

/**
 * A world load exception occurs when trying to load an invalid world. This exception indicating
 * some sort of processing error in {@link WorldBuilder}.
 *
 * <p>As this exception occurs when reading from a world encoding, two alternative constructors
 * ({@link #WorldLoadException(String, int)} and {@link #WorldLoadException(String, int, int)}) are
 * used to provide additional line number and character position information in the error message.
 *
 * @provided
 */
public class WorldLoadException extends Exception {
    /** The world tile row number where loading failed. */
    private int row = -1;

    /** The world tile column number where loading failed. */
    private int col = -1;

    /**
     * Construct a new world load exception with a message detailed the problem.
     *
     * @param message Explanation of the problem that occurred.
     */
    public WorldLoadException(String message) {
        super(message);
    }

    /**
     * Construct a new world load exception that occurred loading a particular row.
     *
     * @param message Explanation of the problem that occurred.
     * @param row The row number where the error occurred. Indicates the issue occurs on line row +
     *     1.
     */
    public WorldLoadException(String message, int row) {
        super(message);
        this.row = row;
    }

    /**
     * Construct a new world load exception that occurred loading a particular row and column.
     *
     * @param message Explanation of the problem that occurred.
     * @param row The row number where the error occurred. Indicates the issue occurs on line row +
     *     1.
     * @param column The column number where the error occurred. Indicates the issue occurs at
     *     character column + 1.
     */
    public WorldLoadException(String message, int row, int column) {
        super(message);
        this.row = row;
        this.col = column;
    }

    @Override
    public String getMessage() {
        if (row != -1 && col != -1) {
            return super.getMessage() + " on line " + (row + 1) + ", character " + (col + 1);
        }
        if (row != -1) {
            return super.getMessage() + " on line " + (row + 1);
        }
        return super.getMessage();
    }
}
