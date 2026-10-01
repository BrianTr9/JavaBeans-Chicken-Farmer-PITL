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
}

