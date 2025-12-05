package com.example.market.service.stock;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonStoreSymbolNormalizationTest {

  @Test
  void dailyPath_withNonNullSymbol_trimsAndLowercases() {
    JsonStore store = new JsonStore();
    Path p = store.dailyPath("  AaPl  ");

    String path = p.toString().replace("\\", "/");
    assertTrue(path.endsWith("data/stocks/aapl-daily.json"),
        "Expected normalized path to end with data/stocks/aapl-daily.json but was " + path);
  }

  @Test
  void dailyPath_withNullSymbol_usesDashDailyJson() {
    JsonStore store = new JsonStore();
    Path p = store.dailyPath(null);

    String path = p.toString().replace("\\", "/");
    // Actual behaviour: data/stocks/-daily.json
    assertTrue(path.endsWith("data/stocks/-daily.json"),
        "Expected path for null symbol to end with data/stocks/-daily.json but was " + path);
  }
}
