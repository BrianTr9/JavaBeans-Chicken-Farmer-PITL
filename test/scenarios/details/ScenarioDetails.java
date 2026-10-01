package scenarios.details;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Simulate a .details file. Useful for describing the test scenario in the code. */
public class ScenarioDetails {
    private final String farmerSpec;
    private final List<String> cabbage = new ArrayList<>();
    private final List<String> magpieSpawner = new ArrayList<>();
    private final List<String> eagleSpawner = new ArrayList<>();
    private final List<String> pigeonSpawner = new ArrayList<>();

    /**
     * Construct a new scenario with a chicken farmer at the given location with the given coin and
     * food.
     */
    public ScenarioDetails(int x, int y, int coin, int food) {
        farmerSpec = "|x:" + x + " y:" + y + " coins:" + coin + " food:" + food;
    }

    private void addSpawner(List<String> spawner, int x, int y, int interval) {
        spawner.add("|x:" + x + " y:" + y + " duration:" + interval);
    }

    public void addMagpieSpawner(int x, int y, int interval) {
        addSpawner(magpieSpawner, x, y, interval);
    }

    public void addEagleSpawner(int x, int y, int interval) {
        addSpawner(eagleSpawner, x, y, interval);
    }

    public void addPigeonSpawner(int x, int y, int interval) {
        addSpawner(pigeonSpawner, x, y, interval);
    }

    public void addCabbage(int x, int y) {
        cabbage.add("|x:" + x + " y:" + y);
    }

    private void writeSection(StringJoiner joiner, String sectionName, List<String> contents) {
        joiner.add(":" + sectionName + ":");
        for (String content : contents) {
            joiner.add(content);
        }
        joiner.add("end;").add("");
    }

    public Reader toReader() {
        StringJoiner result = new StringJoiner("\n");
        writeSection(result, "chickenFarmer", List.of(farmerSpec));
        writeSection(result, "cabbages", cabbage);
        writeSection(result, "magpiespawner", magpieSpawner);
        writeSection(result, "eaglespawner", eagleSpawner);
        writeSection(result, "pigeonspawner", pigeonSpawner);

        return new StringReader(result.toString());
    }
}
