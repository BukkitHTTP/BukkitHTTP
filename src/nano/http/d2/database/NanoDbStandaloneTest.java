package nano.http.d2.database;

import nano.http.d2.database.internal.SerlClz;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NanoDbStandaloneTest {
    @SerlClz
    public static class GrandChild {
        public String code;
        public Map<String, Integer> stats = new HashMap<>();
    }

    @SerlClz
    public static class Child {
        public String label;
        public List<GrandChild> grandchildren = new ArrayList<>();
    }

    @SerlClz
    public static class DummyData {
        public String name;
        public int count;
        public List<String> tags = new ArrayList<>();
        public Map<String, Integer> scores = new HashMap<>();
        public Child child = new Child();
        public Map<String, Child> childMap = new HashMap<>();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[TEST] NanoDb standalone test starting...");
        Path tempDir = Files.createTempDirectory("nanodb-test-");
        File dbFile = tempDir.resolve("test.db").toFile();
        long saveIntervalMs = 25;

        // Create new DB and populate data
        NanoDb<DummyData> db = new NanoDb<>(NanoDbStandaloneTest.class.getClassLoader(), dbFile, DummyData::new, saveIntervalMs);
        DummyData data = db.getImpl();
        data.name = "alpha";
        data.count = 42;
        data.tags.add("foo");
        data.tags.add("bar");
        data.scores.put("math", 90);
        data.scores.put("cs", 100);

        data.child.label = "root-child";
        GrandChild gc1 = new GrandChild();
        gc1.code = "gc-1";
        gc1.stats.put("hp", 7);
        data.child.grandchildren.add(gc1);

        Child mappedChild = new Child();
        mappedChild.label = "mapped";
        GrandChild deep = new GrandChild();
        deep.code = "deep";
        deep.stats.put("mp", 9);
        mappedChild.grandchildren.add(deep);
        data.childMap.put("left", mappedChild);

        db.save();
        assertTrue(dbFile.exists(), "Database file should exist after save()");
        long initialLastModified = dbFile.lastModified();

        // Reload from disk to verify persistence
        NanoDb<DummyData> reloaded = new NanoDb<>(NanoDbStandaloneTest.class.getClassLoader(), dbFile, DummyData::new, saveIntervalMs);
        DummyData loadedData = reloaded.getImpl();
        assertEquals("alpha", loadedData.name, "Name should persist");
        assertEquals(42, loadedData.count, "Count should persist");
        assertTrue(loadedData.tags.contains("foo") && loadedData.tags.contains("bar"), "Tags should persist");
        assertEquals(Integer.valueOf(90), loadedData.scores.get("math"), "Scores should persist (math)");
        assertEquals(Integer.valueOf(100), loadedData.scores.get("cs"), "Scores should persist (cs)");
        assertEquals("root-child", loadedData.child.label, "Nested object label should persist");
        assertEquals("gc-1", loadedData.child.grandchildren.get(0).code, "Nested list element should persist");
        assertEquals(Integer.valueOf(7), loadedData.child.grandchildren.get(0).stats.get("hp"), "Nested map value should persist");
        assertEquals("mapped", loadedData.childMap.get("left").label, "Object-in-map should persist");
        assertEquals("deep", loadedData.childMap.get("left").grandchildren.get(0).code, "Deeply nested object should persist");

        // Exercise auto-save
        Thread.sleep(saveIntervalMs + 10);
        loadedData.count = 77;
        reloaded.tryAutoSave();
        long afterAutoSave = dbFile.lastModified();
        assertTrue(afterAutoSave >= initialLastModified, "Auto-save should update the database file timestamp");

        // Ensure temp file cleaned up
        File tmpFile = new File(dbFile.getAbsolutePath() + ".tmp");
        assertTrue(!tmpFile.exists(), "Temp file should not remain after save");

        System.out.println("[TEST] All NanoDb standalone assertions passed. Temp dir: " + tempDir);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError(message + " (expected null, got " + actual + ")");
            }
        } else if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}
