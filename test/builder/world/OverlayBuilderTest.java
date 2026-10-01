package builder.world;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class OverlayBuilderTest {

    @Test
    public void testGetSectionReturnsLowercasedLinesBetweenLabels() throws Exception {
        String content = ":cabbages:\nX:10 Y:20\nEnd;\n";
        List<String> lines = OverlayBuilder.getSection("Cabbages", content);
        assertEquals(1, lines.size());
        assertEquals("x:10 y:20", lines.get(0)); // lowercased, trimmed
    }

    @Test
    public void testGetSectionThrowsWhenMissing() {
        String content = ":somethingelse:\nend;\n";
        try {
            OverlayBuilder.getSection("cabbages", content);
            fail("Expected IOException when section not found");
        } catch (IOException e) {
            assertTrue(e.getMessage().toLowerCase().contains("section not found"));
        }
    }

    @Test
    public void testExtractSpawnDetailsFromLineParsesValues() {
        SpawnerDetails details = OverlayBuilder.extractSpawnDetailsFromLine("x:100 y:200 duration:300");
        assertEquals(100, details.getX());
        assertEquals(200, details.getY());
        assertEquals(300, details.getDuration());
        assertTrue(details.toString().contains("x:100"));
    }

    @Test
    public void testGetSpawnerDetailsForEachType() throws Exception {
        String content = ":eaglespawner:\nx:10 y:20 duration:30\nend;\n" +
                ":pigeonspawner:\nx:1 y:2 duration:3\nend;\n" +
                ":magpiespawner:\nx:5 y:6 duration:7\nend;\n";
        List<SpawnerDetails> eagles = OverlayBuilder.getEagleSpawnDetailsFromString(content);
        List<SpawnerDetails> pigeons = OverlayBuilder.getPigeonSpawnDetailsFromString(content);
        List<SpawnerDetails> magpies = OverlayBuilder.getMagpieSpawnDetailsFromString(content);
        assertEquals(1, eagles.size());
        assertEquals(1, pigeons.size());
        assertEquals(1, magpies.size());
        assertEquals(10, eagles.get(0).getX());
        assertEquals(2, pigeons.get(0).getY());
        assertEquals(7, magpies.get(0).getDuration());
    }

    @Test
    public void testExtractPlayerDetailsFromLineParsesValues() {
        PlayerDetails p = OverlayBuilder.extractPlayerDetailsFromLine("x:400 y:500 coins:12 food:34");
        assertEquals(400, p.getX());
        assertEquals(500, p.getY());
        assertEquals(12, p.getStartingCoins());
        assertEquals(34, p.getStartingFood());
        assertTrue(p.toString().contains("coins:12"));
    }

    @Test
    public void testGetPlayerDetailsFromFile() throws Exception {
        String content = ":chickenfarmer:\nx:10 y:20 coins:5 food:6\nend;\n";
        PlayerDetails p = OverlayBuilder.getPlayerDetailsFromFile(content);
        assertEquals(10, p.getX());
        assertEquals(20, p.getY());
        assertEquals(5, p.getStartingCoins());
        assertEquals(6, p.getStartingFood());
    }

    @Test
    public void testGetCabbageSpawnDetailsFromString() throws Exception {
        String content = ":cabbages:\nx:7 y:8\nx:9 y:10\nend;\n";
        List<CabbageDetails> cs = OverlayBuilder.getCabbageSpawnDetailsFromString(content);
        assertEquals(2, cs.size());
        assertEquals(7, cs.get(0).getX());
        assertEquals(10, cs.get(1).getY());
    }

    @Test
    public void testEntriesMayUsePipePrefixAnyKeyOrderAndBlankLines() throws Exception {
        String content = ":magpiespawner:\n|duration:9 Y:8 x:7\n\nend;\n";
        List<SpawnerDetails> magpies = OverlayBuilder.getMagpieSpawnDetailsFromString(content);
        assertEquals(1, magpies.size());
        assertEquals(7, magpies.get(0).getX());
        assertEquals(8, magpies.get(0).getY());
        assertEquals(9, magpies.get(0).getDuration());
    }

    @Test
    public void testMissingKeyIsReportedAsIoException() {
        String content = ":eaglespawner:\nx:1 y:2\nend;\n";
        try {
            OverlayBuilder.getEagleSpawnDetailsFromString(content);
            fail("Expected IOException for a spawner without a duration");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("eaglespawner"));
            assertTrue(e.getMessage().contains("duration"));
        }
    }

    @Test
    public void testNonIntegerValueIsReportedAsIoException() {
        String content = ":cabbages:\nx:one y:2\nend;\n";
        try {
            OverlayBuilder.getCabbageSpawnDetailsFromString(content);
            fail("Expected IOException for a non-integer value");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("not an integer"));
        }
    }

    @Test
    public void testPlayerSectionMustHaveExactlyOneEntry() {
        String content = ":chickenfarmer:\nx:1 y:2 coins:3 food:4\nx:5 y:6 coins:7 food:8\nend;\n";
        try {
            OverlayBuilder.getPlayerDetailsFromFile(content);
            fail("Expected IOException for two player entries");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("exactly one"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractRejectsDuplicateKeys() {
        OverlayBuilder.extractSpawnDetailsFromLine("x:1 x:2 duration:3");
    }

    @Test
    public void testOutOfRangeValuesAreRejected() {
        String[] invalid = {
            ":chickenfarmer:\nx:1 y:2 coins:-5 food:4\nend;\n",
            ":chickenfarmer:\nx:-1 y:2 coins:5 food:4\nend;\n",
        };
        for (String content : invalid) {
            try {
                OverlayBuilder.getPlayerDetailsFromFile(content);
                fail("Expected IOException for " + content);
            } catch (IOException e) {
                assertTrue(e.getMessage().contains("must be at least 0"));
            }
        }
    }

    @Test
    public void testSpawnerDurationMustBePositive() {
        // A zero interval would leave the spawner's timer unable to ever fire.
        String content = ":pigeonspawner:\nx:1 y:2 duration:0\nend;\n";
        try {
            OverlayBuilder.getPigeonSpawnDetailsFromString(content);
            fail("Expected IOException for a zero duration");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("'duration' must be at least 1"));
        }
    }

    @Test
    public void testUnclosedSectionIsReportedAsSuch() {
        try {
            OverlayBuilder.getSection("cabbages", ":cabbages:\nx:1 y:2\n");
            fail("Expected IOException for a section without end;");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("missing its closing"));
        }
    }
}
