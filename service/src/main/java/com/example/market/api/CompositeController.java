package com.example.market.api;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/market")
public final class CompositeController {
  private final StockDataService stocks;
  private final ForecastDataService forecast; 
  private final NewsDataService news;         
  private final JsonStore store;

  private static final String DEFAULT_SYMBOL = "AMZN";
  private static final Duration DAILY_CACHE_TTL = Duration.ofDays(1);
  private static final Duration NEWS_CACHE_TTL  = Duration.ofHours(6);

  public CompositeController(StockDataService stocks,
                             ForecastDataService forecast,
                             NewsDataService news,
                             JsonStore store) {
    this.stocks = stocks;
    this.forecast = forecast;
    this.news = news;
    this.store = store;
  }

  /** Returns daily OHLCV for AMZN (hardcoded for iteration 1). */
  @GetMapping("/daily")
  public ResponseEntity<?> getDaily(@RequestParam(required = false) String symbol,
                                    @RequestParam(defaultValue = "false") boolean force) {
    try {
      StockDailySeries series = getDailySeries(DEFAULT_SYMBOL, force);
      return ResponseEntity.ok(series);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(502).body(jsonError(e.getMessage()));
    }
  }

  /** Returns placeholder prediction for AMZN (hardcoded), ensuring data is cached/fresh. */
  @GetMapping("/predict")
  public ResponseEntity<?> predict(@RequestParam(required = false) String symbol,
                                   @RequestParam(defaultValue = "next-day") String horizon,
                                   @RequestParam(defaultValue = "false") boolean force) {
    try {
      StockDailySeries series = getDailySeries(DEFAULT_SYMBOL, force);
      Map<String, String> map = forecast.predictFuturePrices(DEFAULT_SYMBOL); // placeholder

      return ResponseEntity.ok(Map.of(
          "symbol", DEFAULT_SYMBOL,
          "horizon", horizon,
          "prediction", map,
          "source", series.getSource()
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(502).body(jsonError(e.getMessage()));
    }
  }

  @GetMapping("/sentiment")
public ResponseEntity<?> getSentiment(@RequestParam(required = false) String symbol,
                                      @RequestParam(defaultValue = "false") boolean force) {
  try {
    final String s = (symbol == null || symbol.isBlank()) ? DEFAULT_SYMBOL : symbol;
    final Path cache = store.newsPath(s);
    if (!force && isFresh(cache, NEWS_CACHE_TTL)) {
      return ResponseEntity.ok(store.read(cache, Map.class));
    }

    // call your new NewsDataService (it reads the key inside itself)
    var result = news.analyzeSentiment(s.toUpperCase());

    Map<String, Object> payload = Map.of(
        "company", result.getCompany(),
        "sentimentScore", result.getSentimentScore(),
        "sentimentLabel", result.getSentimentLabel(),
        "source", "HuggingFaceModel"
    );

    store.write(cache, payload);
    return ResponseEntity.ok(payload);

  } catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
  } catch (Exception e) {
    return ResponseEntity.status(502).body(jsonError(e.getMessage()));
  }
}

  /* ---------------- helpers ---------------- */

private StockDailySeries getDailySeries(String symbol, boolean force) throws Exception {
  if (symbol == null || symbol.isBlank()) {
    throw new IllegalArgumentException("symbol is required");
  }

  final Path cache = store.dailyPath(symbol);

  // 1) Cache-first: if fresh and not forced, return without needing an API key.
  if (!force && isFresh(cache, DAILY_CACHE_TTL)) {
    return store.read(cache, StockDailySeries.class);
  }

  // 2) Only now require a key (env var, then system property fallback for tests/CI).
  String key = System.getenv("ALPHAVANTAGE_API_KEY");
  if (key == null || key.isBlank()) {
    key = System.getProperty("alphavantage.api.key", "");
  }
  if (key.isBlank()) {
    throw new IllegalStateException("missing ALPHAVANTAGE_API_KEY (or -Dalphavantage.api.key)");
  }

  // 3) Fetch & persist.
  StockDailySeries fresh = stocks.fetchDaily(symbol, key);
  store.write(cache, fresh);
  return fresh;
}


  private static boolean isFresh(Path file, Duration ttl) {
    try {
      if (!Files.exists(file)) return false;
      Instant mtime = Files.getLastModifiedTime(file).toInstant();
      return mtime.isAfter(Instant.now().minus(ttl));
    } catch (Exception e) {
      return false;
    }
  }

  private static String jsonError(String msg) {
    return "{\"error\":\"" + msg.replace("\"","'") + "\"}";
  }
}
