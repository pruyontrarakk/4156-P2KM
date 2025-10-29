package com.example.market.service.forecast.trendmaster.python;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrendmasterPythonServiceTest {

  // Minimal valid payload with two rows
  private static final String GOOD =
      "{ \"Date\": {\"0\":\"2025-10-23\",\"1\":\"2025-10-24\"}, " +
      "  \"Predicted_Close\": {\"0\":\"100.0\",\"1\":\"101.0\"} }";

  // Same JSON but wrapped in quotes (outer is a JSON string literal)
  private static final String QUOTED =
      "\"" + GOOD.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";

  // Well-formed but useless/wrong-shape JSON to exercise "empty output" branch
  private static final String BAD_SCHEMA =
      "{ \"Date\": {}, \"Predicted_Close\": {} }";

  @Test
  void parses_objectPayload() {
    var svc = new PythonService() {
      @Override public String runTrendMaster() { return GOOD; }
    };
    var out = svc.predictFuturePrices("AMZN");
    assertEquals("100.0", out.get("2025-10-23"));
    assertEquals("101.0", out.get("2025-10-24"));
  }

  @Test
  void parses_quotedJsonPayload() {
    var svc = new PythonService() {
      @Override public String runTrendMaster() { return QUOTED; }
    };
    var out = svc.predictFuturePrices("AMZN");
    assertEquals(2, out.size());
    assertTrue(out.containsKey("2025-10-23"));
    assertTrue(out.containsKey("2025-10-24"));
  }

  @Test
  void returnsEmpty_onBadSchema() {
    var svc = new PythonService() {
      @Override public String runTrendMaster() { return BAD_SCHEMA; }
    };
    var out = svc.predictFuturePrices("AMZN");
    assertTrue(out.isEmpty(), "Well-formed but empty/wrong-shape JSON should yield empty map");
  }
}
