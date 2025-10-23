package com.example.market.api;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

/**
 * REST controller for market data operations.
 */
@RestController
@RequestMapping("/market")
public final class CompositeController {
  /**
   * Stock data service.
   */
  private final StockDataService stockDataService;
  /**
   * JSON store service.
   */
  private final JsonStore jsonStore;

  /**
   * Constructs a new CompositeController.
   *
   * @param stockDataServiceParam the stock data service
   * @param jsonStoreParam the JSON store service
   */
  public CompositeController(final StockDataService stockDataServiceParam,
      final JsonStore jsonStoreParam) {
    this.stockDataService = stockDataServiceParam;
    this.jsonStore = jsonStoreParam;
  }

  /**
   * Retrieves daily stock data for a given symbol.
   *
   * @param symbol the stock symbol
   * @return ResponseEntity containing the stock data or error
   */
  @GetMapping("/daily")
  public ResponseEntity<?> getDaily(@RequestParam final String symbol) {
    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.internalServerError()
          .body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    try {
      StockDailySeries data = stockDataService.fetchDaily(symbol, key);
      // Persist (optional). Remove if you only want POSTs to write.
      jsonStore.write(Path.of(symbol.toLowerCase() + "-daily.json"), data);
      return ResponseEntity.ok(data);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("{\"error\":\"" + e.getMessage() + "\"}");
    }
  }
}
