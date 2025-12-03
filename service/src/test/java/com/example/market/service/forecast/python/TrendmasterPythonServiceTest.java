package com.example.market.service.forecast.python;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.forecast.python.ProcessRunner;
import com.example.market.service.stock.AlphaVantageService;
import com.example.market.service.stock.StockDataService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  @Test
  void testRunTrendMaster_returnsLastLine() throws Exception {
    // Mock Process
    Process process = mock(Process.class);

    // Mock output: 3 lines, last is RESULT
    ByteArrayInputStream fakeOutput = new ByteArrayInputStream(
            "line1\nline2\nRESULT\n".getBytes()
    );
    when(process.getInputStream()).thenReturn(fakeOutput);
    when(process.waitFor()).thenReturn(0);

    // Mock ProcessRunner
    ProcessRunner runner = mock(ProcessRunner.class);
    when(runner.start(any())).thenReturn(process);

    AlphaVantageService mockStockData = mock(AlphaVantageService.class);
    StockDailySeries fakeSeries = new StockDailySeries(
            "AAPL", "2025-02-01", "AlphaVantage", List.of()
    );
    when(mockStockData.fetchDaily(eq("AAPL"), any())).thenReturn(fakeSeries);

    PythonService service = new PythonService(runner, mockStockData);

    String result = service.runTrendMaster();

    assertEquals("RESULT", result);
  }
}
