package com.example.market.service.forecast.python;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PythonServiceParsingTest {

  static class StubPythonService extends PythonService {
    private final String payload;
    StubPythonService(String payload) { this.payload = payload; }
    @Override public String runTrendMaster() { return payload; }
  }

  private static final String OBJECT_JSON =
      "{ \"Date\": {\"0\":\"2025-10-23\",\"1\":\"2025-10-24\"}, " +
      "  \"Predicted_Close\": {\"0\":\"100.0\",\"1\":\"101.0\"} }";

  // A JSON string literal that contains the JSON above (outer.isTextual() == true)
  private static final String QUOTED_JSON = "\"" +
      OBJECT_JSON.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";

  @Test
  void parses_whenOuterIsObjectNode() {
    PythonService svc = new StubPythonService(OBJECT_JSON);
    Map<String,String> m = svc.predictFuturePrices("AMZN");
    assertEquals("100.0", m.get("2025-10-23"));
    assertEquals("101.0", m.get("2025-10-24"));
  }

  @Test
  void parses_whenOuterIsTextualJson() {
    PythonService svc = new StubPythonService(QUOTED_JSON);
    Map<String,String> m = svc.predictFuturePrices("AMZN");
    assertEquals("100.0", m.get("2025-10-23"));
    assertEquals("101.0", m.get("2025-10-24"));
  }
}
