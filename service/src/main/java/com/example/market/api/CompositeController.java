package com.example.market.api;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.market.service.news.NewsDataService;
import com.example.market.model.news.SentimentResult;

import java.nio.file.Path;

@RestController
@RequestMapping("/market")
public class CompositeController {
  private final StockDataService stock;
  private final JsonStore store;
  private final NewsDataService news;

  public CompositeController(StockDataService stock, JsonStore store,  NewsDataService news) {
    this.stock = stock;
    this.store = store;
    this.news = news;
  }

  @GetMapping("/daily")
  public ResponseEntity<?> getDaily(@RequestParam String symbol) {
    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.internalServerError().body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    try {
      StockDailySeries d = stock.fetchDaily(symbol, key);
      // Persist (optional). Remove if you only want POSTs to write.
      store.write(Path.of(symbol.toLowerCase() + "-daily.json"), d);
      return ResponseEntity.ok(d);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("{\"error\":\"" + e.getMessage() + "\"}");
    }
  }

  @GetMapping("/sentiment")
  public ResponseEntity<?> getSentiment(@RequestParam String symbol) {
    try {
      SentimentResult result = news.analyzeSentiment(symbol);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body("{\"error\":\"" + e.getMessage() + "\"}");
    }
  }
}
