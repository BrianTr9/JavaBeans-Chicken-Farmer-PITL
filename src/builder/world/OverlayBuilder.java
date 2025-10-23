package builder.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts a list of information that changes the default beginning game state regarding starting
 * resources, placement of the player, cabbages, spawner locations and times etc
 */
public class OverlayBuilder {

    /**
     * Search the given string for a line equivalent to the given label surrounded by a pair of ':'
     * then collect all lines of text between that label and the next line that reads as 'end;'
     *
     * @param label label we are searching for
     * @param contents file contents we are searching through
     * @return a {@link ArrayList<String>} of lines within the searched for section.
     * @throws IOException if the section is not found
     */
    public static List<String> getSection(String label, String contents) throws IOException {
        final String[] lines = contents.split("\n");
        boolean collectingLines = false;
        final List<String> section = new ArrayList<>();
        for (String rawLine : lines) {
            final String current = rawLine.toLowerCase().trim();
            if (collectingLines && current.equals("end;")) {
                return section;
            }
            if (collectingLines) {
                section.add(current);
            }
            if (current.equals(":" + label.toLowerCase().trim() + ":")) {
                collectingLines = true;
            }
        }
        throw new IOException("Section not Found!");
    }

    /** Returns a new SpawnerDetails parsed from a single line. */
    public static SpawnerDetails extractSpawnDetailsFromLine(String line) {
        String[] chunks = line.split(" ");
        assert chunks.length == 3; // should always be 3 chunks in a correctly shaped line.
        String[] xchunk = chunks[0].split(":");
        String[] ychunk = chunks[1].split(":");
        String[] durationchunk = chunks[2].split(":");
        final int x = Integer.parseInt(xchunk[1]);
        final int y = Integer.parseInt(ychunk[1]);
        final int duration = Integer.parseInt(durationchunk[1]);
        return new SpawnerDetails() {
            @Override
            public int getX() {
                return x;
            }

            @Override
            public int getY() {
                return y;
            }

            @Override
            public void setX(int x) {}

            @Override
            public void setY(int y) {}

            @Override
            public int getDuration() {
                return duration;
            }

            @Override
            public String toString() {
                return "OverlayBuilder["
                        + "x:"
                        + x
                        + ","
                        + "y:"
                        + y
                        + ",duration:"
                        + duration
                        + "]";
            }
        };
    }

    private static List<SpawnerDetails> getSpawnerDetailsForLabel(
            String label, String detailsContent) throws IOException {
        final List<String> section = OverlayBuilder.getSection(label, detailsContent);
        final List<SpawnerDetails> list = new ArrayList<>();
        for (String entry : section) {
            list.add(extractSpawnDetailsFromLine(entry));
        }
        return list;
    }

    /** Returns eagle spawner details parsed from the details content. */
    public static List<SpawnerDetails> getEagleSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("eaglespawner", detailsContent);
    }

    /** Returns pigeon spawner details parsed from the details content. */
    public static List<SpawnerDetails> getPigeonSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("pigeonspawner", detailsContent);
    }

    /** Returns magpie spawner details parsed from the details content. */
    public static List<SpawnerDetails> getMagpieSpawnDetailsFromString(String detailsContent)
            throws IOException {
        return getSpawnerDetailsForLabel("magpiespawner", detailsContent);
    }

    /** Parses a player details line into a PlayerDetails instance. */
    public static PlayerDetails extractPlayerDetailsFromLine(String line) {
        String[] chunks = line.split(" ");
        assert chunks.length == 4; // should always be 4 chunks in a correctly shaped line.
        String[] xchunk = chunks[0].split(":");
        String[] ychunk = chunks[1].split(":");
        String[] coinchunk = chunks[2].split(":");
        String[] foodchunk = chunks[3].split(":");
        final int x = Integer.parseInt(xchunk[1]);
        final int y = Integer.parseInt(ychunk[1]);
        final int coins = Integer.parseInt(coinchunk[1]);
        final int food = Integer.parseInt(foodchunk[1]);
        return new PlayerDetails() {
            @Override
            public int getStartingFood() {
                return food;
            }

            @Override
            public int getStartingCoins() {
                return coins;
            }

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
                return "OverlayBuilder["
                        + "x:"
                        + x
                        + ","
                        + "y:"
                        + y
                        + ",coins:"
                        + coins
                        + ",food:"
                        + food
                        + "]";
            }
        };
    }

    /** Returns player details parsed from the provided details content. */
    public static PlayerDetails getPlayerDetailsFromFile(String detailsContent) throws IOException {
        List<String> section = OverlayBuilder.getSection("chickenFarmer", detailsContent);
        assert section.size()
                == 1; // right now we only expect there to ever be one chicken farmer entry
        final String entry = section.get(0);
        return OverlayBuilder.extractPlayerDetailsFromLine(entry);
    }

    /** Returns cabbage spawn details parsed from the details content. */
    public static List<CabbageDetails> getCabbageSpawnDetailsFromString(String detailsContent)
            throws IOException {
        final List<String> section = OverlayBuilder.getSection("cabbages", detailsContent);
        final List<CabbageDetails> list = new ArrayList<>();
        for (String entry : section) {
            list.add(extractCabbageDetailsFromLine(entry));
        }
        return list;
    }

    private static CabbageDetails extractCabbageDetailsFromLine(String line) {
        final String[] chunks = line.split(" ");
        String[] xchunk = chunks[0].split(":");
        String[] ychunk = chunks[1].split(":");
        final int x = Integer.parseInt(xchunk[1]);
        final int y = Integer.parseInt(ychunk[1]);
        return new CabbageDetails() {
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
                return "OverlayBuilder[" + "x:" + x + "," + "y:" + y + "]";
            }
        };
    }
}
