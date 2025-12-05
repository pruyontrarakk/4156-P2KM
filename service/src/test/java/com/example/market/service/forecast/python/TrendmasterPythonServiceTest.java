package com.example.market.service.forecast.python;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.stock.AlphaVantageService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for PythonService.runTrendMaster that cover success and failure paths.
 */
class TrendmasterPythonServiceTest {

  @Test
  void runTrendMaster_successReturnsLastLine() throws Exception {
    ProcessRunner runner = mock(ProcessRunner.class);
    Process process = mock(Process.class);

    String output = "line1\nline2\nRESULT\n";
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream(output.getBytes()));
    when(process.waitFor()).thenReturn(0);
    when(runner.start(any(ProcessBuilder.class))).thenReturn(process);

    AlphaVantageService mockStockData = mock(AlphaVantageService.class);
    StockDailySeries fakeSeries = new StockDailySeries(
        "AAPL", "2025-02-01", "AlphaVantage", List.of()
    );
    when(mockStockData.fetchDaily(eq("AAPL"), any())).thenReturn(fakeSeries);

    PythonService service = new PythonService(runner, mockStockData);

    String result = service.runTrendMaster();

    assertEquals("RESULT", result);
  }

  @Test
  void runTrendMaster_nonZeroExitCodeThrows() throws Exception {
    ProcessRunner runner = mock(ProcessRunner.class);
    Process process = mock(Process.class);

    String output = "error line\n";
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream(output.getBytes()));
    when(process.waitFor()).thenReturn(1); // non-zero
    when(runner.start(any(ProcessBuilder.class))).thenReturn(process);

    AlphaVantageService stockData = mock(AlphaVantageService.class);
    when(stockData.fetchDaily(eq("AAPL"), any()))
        .thenReturn(new StockDailySeries("AAPL", "2025-02-01", "AlphaVantage", List.of()));

    PythonService service = new PythonService(runner, stockData);

    RuntimeException ex = assertThrows(RuntimeException.class,
        service::runTrendMaster);
    assertTrue(ex.getMessage().contains("exit code"),
        "Expected message to mention exit code");
  }

  @Test
  void runTrendMaster_noOutputThrows() throws Exception {
    ProcessRunner runner = mock(ProcessRunner.class);
    Process process = mock(Process.class);

    // empty stream
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor()).thenReturn(0);
    when(runner.start(any(ProcessBuilder.class))).thenReturn(process);

    AlphaVantageService stockData = mock(AlphaVantageService.class);
    when(stockData.fetchDaily(eq("AAPL"), any()))
        .thenReturn(new StockDailySeries("AAPL", "2025-02-01", "AlphaVantage", List.of()));

    PythonService service = new PythonService(runner, stockData);

    RuntimeException ex = assertThrows(RuntimeException.class,
        service::runTrendMaster);
    assertTrue(ex.getMessage().contains("No output"),
        "Expected message to mention no output");
  }
}
