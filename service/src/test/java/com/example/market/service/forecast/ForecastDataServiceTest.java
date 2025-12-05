package com.example.market.service.forecast;

import com.example.market.service.forecast.python.PythonService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ForecastDataService to drive coverage of:
 *  - default horizon path
 *  - explicit horizon path
 */
class ForecastDataServiceTest {

  /**
   * Stub PythonService that records the last symbol + horizon used.
   */
  static class StubPythonService extends PythonService {
    String lastSymbol;
    Integer lastHorizon; // null when default overload is used without horizon
    Map<String, String> toReturn = new HashMap<>();

    StubPythonService() {
      super(); // use no-arg ctor; we override the prediction methods anyway
    }

    @Override
    public Map<String, String> predictFuturePrices(final String companyName) {
      this.lastSymbol = companyName;
      this.lastHorizon = null;
      return toReturn;
    }

    @Override
    public Map<String, String> predictFuturePrices(final String companyName,
                                                   final int horizon) {
      this.lastSymbol = companyName;
      this.lastHorizon = horizon;
      return toReturn;
    }
  }

  @Test
  void predictFuturePrices_usesDefaultHorizon10() {
    StubPythonService stub = new StubPythonService();
    stub.toReturn.put("2025-01-01", "123.45");

    ForecastDataService service = new ForecastDataService(stub);

    Map<String, String> result = service.predictFuturePrices("AAPL");

    assertEquals("AAPL", stub.lastSymbol, "Symbol should be forwarded to PythonService");
    // IMPORTANT: The real implementation passes 10 as the default horizon.
    assertEquals(10, stub.lastHorizon,
        "Default method should use a horizon of 10");
    assertEquals("123.45", result.get("2025-01-01"));
  }

  @Test
  void predictFuturePrices_withExplicitHorizonForwardsToPythonService() {
    StubPythonService stub = new StubPythonService();
    stub.toReturn.put("2025-01-02", "151.78");

    ForecastDataService service = new ForecastDataService(stub);

    Map<String, String> result = service.predictFuturePrices("MSFT", 7);

    assertEquals("MSFT", stub.lastSymbol);
    assertEquals(7, stub.lastHorizon,
        "Explicit horizon should be passed through unchanged");
    assertEquals("151.78", result.get("2025-01-02"));
  }
}