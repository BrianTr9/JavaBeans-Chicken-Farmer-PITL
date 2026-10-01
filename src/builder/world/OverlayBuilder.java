package builder.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses a details file, which changes the default starting game state: starting resources,
 * placement of the player, initial cabbages and bird spawner locations and intervals.
 *
 * <p>A details file is made of sections. Each section starts with a {@code :label:} line and
 * ends with an {@code end;} line; every line in between is one entry of space-separated
 * {@code key:value} pairs with integer values, for example:
 *
 * <pre>
 * :chickenFarmer:
 * |x:380 y:380 coins:20 food:30
 * end;
 * </pre>
 *
 * <p>Labels and keys are case-insensitive and keys may appear in any order. A leading
 * {@code |} on an entry line is optional.
 */
public class OverlayBuilder {

    private static final String SECTION_END = "end;";

    private static final Set<String> SPAWNER_KEYS = Set.of("x", "y", "duration");
    private static final Set<String> PLAYER_KEYS = Set.of("x", "y", "coins", "food");
    private static final Set<String> CABBAGE_KEYS = Set.of("x", "y");

    /**
     * Search the given string for a line equivalent to the given label surrounded by a pair of
     * ':' then collect all lines of text between that label and the next line that reads as
     * 'end;'. Lines are returned lower-cased and trimmed; blank lines are skipped.
     *
     * @param label label we are searching for
     * @param contents file contents we are searching through
     * @return a {@link List} of lines within the searched for section.
     * @throws IOException if the section is not found
     */
    public static List<String> getSection(String label, String contents) throws IOException {
        final String header = ":" + label.toLowerCase().trim() + ":";
        boolean collectingLines = false;
        final List<String> section = new ArrayList<>();
        for (String rawLine : contents.split("\n")) {
            final String current = rawLine.toLowerCase().trim();
            if (collectingLines && current.equals(SECTION_END)) {
                return section;
            }
            if (collectingLines && !current.isEmpty()) {
                section.add(current);
            }
            if (current.equals(header)) {
                collectingLines = true;
            }
        }
        throw new IOException("Section not Found! Expected a '" + header + "' section ending in '"
                + SECTION_END + "'");
    }

    /**
     * Parses a spawner entry such as {@code x:100 y:200 duration:300}.
     *
     * @param line the entry line
     * @return the parsed spawner details
     * @throws IllegalArgumentException if the line is not a valid spawner entry
     */
    public static SpawnerDetails extractSpawnDetailsFromLine(String line) {
        final Map<String, Integer> values = parseEntry(line, SPAWNER_KEYS);
        requireAtLeast(values, "x", 0, line);
        requireAtLeast(values, "y", 0, line);
        requireAtLeast(values, "duration", 1, line);
        return new Spawner(values.get("x"), values.get("y"), values.get("duration"));
    }

    /**
     * Parses a player entry such as {@code x:380 y:380 coins:20 food:30}.
     *
     * @param line the entry line
     * @return the parsed player details
     * @throws IllegalArgumentException if the line is not a valid player entry
     */
    public static PlayerDetails extractPlayerDetailsFromLine(String line) {
        final Map<String, Integer> values = parseEntry(line, PLAYER_KEYS);
        for (String key : PLAYER_KEYS) {
            requireAtLeast(values, key, 0, line);
        }
        return new Player(
                values.get("x"), values.get("y"), values.get("coins"), values.get("food"));
    }

    /**
     * Returns eagle spawner details parsed from the details content.
     *
     * @param detailsContent the full details file contents
     * @return one entry per eagle spawner
     * @throws IOException if the section is missing or contains a malformed entry
     */
    public static List<SpawnerDetails> getEagleSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("eaglespawner", detailsContent);
    }

    /**
     * Returns pigeon spawner details parsed from the details content.
     *
     * @param detailsContent the full details file contents
     * @return one entry per pigeon spawner
     * @throws IOException if the section is missing or contains a malformed entry
     */
    public static List<SpawnerDetails> getPigeonSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("pigeonspawner", detailsContent);
    }

    /**
     * Returns magpie spawner details parsed from the details content.
     *
     * @param detailsContent the full details file contents
     * @return one entry per magpie spawner
     * @throws IOException if the section is missing or contains a malformed entry
     */
    public static List<SpawnerDetails> getMagpieSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("magpiespawner", detailsContent);
    }

    /**
     * Returns player details parsed from the provided details content.
     *
     * @param detailsContent the full details file contents
     * @return the single player entry
     * @throws IOException if the section is missing, malformed or does not contain exactly one
     *     entry
     */
    public static PlayerDetails getPlayerDetailsFromFile(String detailsContent)
            throws IOException {
        final String label = "chickenFarmer";
        final List<String> section = getSection(label, detailsContent);
        if (section.size() != 1) {
            throw new IOException("Expected exactly one entry in the ':" + label
                    + ":' section but found " + section.size());
        }
        try {
            return extractPlayerDetailsFromLine(section.get(0));
        } catch (IllegalArgumentException e) {
            throw malformed(label, e);
        }
    }

    /**
     * Returns cabbage spawn details parsed from the details content.
     *
     * @param detailsContent the full details file contents
     * @return one entry per initial cabbage
     * @throws IOException if the section is missing or contains a malformed entry
     */
    public static List<CabbageDetails> getCabbageSpawnDetailsFromString(String detailsContent)
            throws IOException {
        final String label = "cabbages";
        final List<CabbageDetails> result = new ArrayList<>();
        for (String entry : getSection(label, detailsContent)) {
            try {
                final Map<String, Integer> values = parseEntry(entry, CABBAGE_KEYS);
                requireAtLeast(values, "x", 0, entry);
                requireAtLeast(values, "y", 0, entry);
                result.add(new Cabbage(values.get("x"), values.get("y")));
            } catch (IllegalArgumentException e) {
                throw malformed(label, e);
            }
        }
        return result;
    }

    private static List<SpawnerDetails> getSpawnerDetailsForLabel(
            String label, String detailsContent) throws IOException {
        final List<SpawnerDetails> result = new ArrayList<>();
        for (String entry : getSection(label, detailsContent)) {
            try {
                result.add(extractSpawnDetailsFromLine(entry));
            } catch (IllegalArgumentException e) {
                throw malformed(label, e);
            }
        }
        return result;
    }

    /**
     * Parses one entry line into its key/value pairs.
     *
     * @param line the entry line, optionally prefixed with '|'
     * @param requiredKeys the exact set of keys the line must contain
     * @return a map from lower-case key to integer value, containing exactly requiredKeys
     * @throws IllegalArgumentException if a pair is malformed, a value is not an integer, or
     *     the keys do not match requiredKeys
     */
    private static Map<String, Integer> parseEntry(String line, Set<String> requiredKeys) {
        String body = line.trim();
        if (body.startsWith("|")) {
            body = body.substring(1).trim();
        }
        final Map<String, Integer> values = new HashMap<>();
        for (String pair : body.split("\\s+")) {
            final String[] parts = pair.split(":", -1);
            if (parts.length != 2 || parts[0].isEmpty()) {
                throw new IllegalArgumentException(
                        "'" + pair + "' is not a key:value pair in '" + line + "'");
            }
            final String key = parts[0].toLowerCase();
            try {
                if (values.put(key, Integer.parseInt(parts[1])) != null) {
                    throw new IllegalArgumentException(
                            "duplicate key '" + key + "' in '" + line + "'");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "value of '" + key + "' is not an integer in '" + line + "'", e);
            }
        }
        if (!values.keySet().equals(requiredKeys)) {
            throw new IllegalArgumentException("expected keys " + requiredKeys + " but found "
                    + values.keySet() + " in '" + line + "'");
        }
        return values;
    }

    /**
     * Checks that a parsed value is at least the given minimum.
     *
     * @throws IllegalArgumentException if it is smaller
     */
    private static void requireAtLeast(
            Map<String, Integer> values, String key, int minimum, String line) {
        if (values.get(key) < minimum) {
            throw new IllegalArgumentException(
                    "'" + key + "' must be at least " + minimum + " in '" + line + "'");
        }
    }

    private static IOException malformed(String label, IllegalArgumentException cause) {
        return new IOException(
                "Malformed entry in ':" + label + ":' section: " + cause.getMessage(), cause);
    }

    /** Parsed spawner entry. */
    private record Spawner(int x, int y, int duration) implements SpawnerDetails {
        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "SpawnerDetails[x:" + x + ",y:" + y + ",duration:" + duration + "]";
        }
    }

    /** Parsed player entry. */
    private record Player(int x, int y, int coins, int food) implements PlayerDetails {
        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getStartingCoins() {
            return coins;
        }

        @Override
        public int getStartingFood() {
            return food;
        }

        @Override
        public String toString() {
            return "PlayerDetails[x:" + x + ",y:" + y + ",coins:" + coins + ",food:" + food + "]";
        }
    }

    /** Parsed cabbage entry. */
    private record Cabbage(int x, int y) implements CabbageDetails {
        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "CabbageDetails[x:" + x + ",y:" + y + "]";
        }
    }
}
