package com.example.market.service.stock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonStoreReadWriteTest {

    @Test
    void writeThenReadBackMap() throws IOException {
        JsonStore store = new JsonStore();

        Path file = Paths.get("target/test-output/json-store-readwrite.json");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        Map<String, Object> payload = Map.of(
                "foo", "bar",
                "n", 42
        );

        store.write(file, payload);
        assertTrue(Files.exists(file), "File should exist after write");

        @SuppressWarnings("unchecked")
        Map<String, Object> readBack = store.read(file, Map.class);

        assertEquals("bar", readBack.get("foo"));
        assertEquals(42, ((Number) readBack.get("n")).intValue());
    }

    @Test
    void existsReflectsFilesystemState() throws IOException {
        JsonStore store = new JsonStore();

        Path file = Paths.get("target/test-output/json-store-exists.json");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        assertFalse(store.exists(file), "File should not exist initially");

        store.write(file, Map.of("x", 1));
        assertTrue(store.exists(file), "File should exist after write");
    }

    @Test
    void dailyAndNewsPathsNormalizeSymbol() {
        JsonStore store = new JsonStore();

        // symbol with mixed case and spaces
        var daily = store.dailyPath("  AaPl ");
        var news = store.newsPath(null);

        assertEquals(Paths.get("data", "stocks", "aapl-daily.json"), daily);
        assertEquals(Paths.get("data", "news", ".json"), news);
    }
}
