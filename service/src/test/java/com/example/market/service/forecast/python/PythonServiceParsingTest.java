package com.example.market.service.forecast.python;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PythonService.parseTrendMasterResponse that exercise
 * both valid and invalid JSON responses.
 */
class PythonServiceParsingTest {

  static class StubPythonService extends PythonService {
    private final String payload;
    StubPythonService(String payload) { this.payload = payload; }
    @Override public String runTrendMaster() { return payload; }
  }

  @Test
  void parsesValidJsonIntoDateToPriceMap() {
    StubPythonService pythonService = new StubPythonService("");

    String json = """
        {
          "Date": { "0": "2025-01-01", "1": "2025-01-02" },
          "Predicted_Close": { "0": "150.23", "1": "151.78" }
        }
        """;

    Map<String, String> result = pythonService.parseTrendMasterResponse(json);

    assertEquals(2, result.size());
    assertEquals("150.23", result.get("2025-01-01"));
    assertEquals("151.78", result.get("2025-01-02"));
  }

  @Test
  void blankResponseThrowsHelpfulException() {
    StubPythonService pythonService = new StubPythonService("");
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> pythonService.parseTrendMasterResponse("   "));
    assertTrue(ex.getMessage().contains("Empty response"),
        "Expected message to mention empty response");
  }

  @Test
  void nonJsonResponseThrowsHelpfulException() {
    StubPythonService pythonService = new StubPythonService("");
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> pythonService.parseTrendMasterResponse("not-json"));
    assertTrue(ex.getMessage().contains("not valid JSON"),
        "Expected message to mention invalid JSON");
  }

  @Test
  void missingDateOrPredictedCloseThrowsHelpfulException() {
    StubPythonService pythonService = new StubPythonService("");

    String jsonMissingFields = """
        {
          "somethingElse": { "0": "2025-01-01" }
        }
        """;

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> pythonService.parseTrendMasterResponse(jsonMissingFields));
    assertTrue(ex.getMessage().contains("Missing 'Date' or 'Predicted_Close'"),
        "Expected message to mention missing Date or Predicted_Close");
  }

  @Test
  void textualJsonWrapperIsParsedCorrectly() {
    StubPythonService pythonService = new StubPythonService("");

    String inner = """
        {
          "Date": { "0": "2025-01-01" },
          "Predicted_Close": { "0": "123.45" }
        }
        """.replace("\n", "").replace("  ", "");

    // Outer JSON where the entire payload is a JSON-encoded string
    String wrapped = "\"" + inner.replace("\"", "\\\"") + "\"";

    Map<String, String> result = pythonService.parseTrendMasterResponse(wrapped);

    assertEquals(1, result.size());
    assertEquals("123.45", result.get("2025-01-01"));
  }
  
}
