package com.example.market.api;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/market")
public final class CompositeController {
  /** Service for retrieving stock market data. */
  private final StockDataService stocks;
  /** Service for generating and managing forecast data. */
  private final ForecastDataService forecast;
  /** Service for retrieving and processing news data. */
  private final NewsDataService news;
  /** JSON-backed cache or storage handler. */
  private final JsonStore store;

  /** Default symbol or company name. */
  private static final String DEFAULT_SYMBOL = "AMZN";
  /** Time-to-live for cached daily stock data. */
  private static final Duration DAILY_CACHE_TTL = Duration.ofDays(1);
  /** Time-to-live for cached news data. */
  private static final Duration NEWS_CACHE_TTL  = Duration.ofHours(6);

  /**
   * Creates a new {@code CompositeController} that coordinates stock, forecast,
   * and news data services with shared JSON storage.
   *
   * @param thisStocks   the stock data service {@code StockDataService}
   * @param thisForecast the forecast data service {@code ForecastDataService}
   * @param thisNews     the news data service {@code NewsDataService}
   * @param thisStore    the JSON storage service {@code JsonStore}
   */
  public CompositeController(final StockDataService thisStocks,
                             final ForecastDataService thisForecast,
                             final NewsDataService thisNews,
                             final JsonStore thisStore) {
    this.stocks = thisStocks;
    this.forecast = thisForecast;
    this.news = thisNews;
    this.store = thisStore;
  }

  /**
   * Returns daily OHLCV for AMZN (hardcoded for iteration 1).
   *
   *  @param symbol the stock or asset symbol to analyze sentiment for;
   *                may be {@code null} if sentiment should be computed
   *                for a default or global context
   *  @param force whether to force recomputation of sentiment rather than
   *               using cached results
   *  @return a {@link ResponseEntity} containing the present info about a
   *              company's stock or an appropriate error response if the
   *              request cannot be processed
   */
  @GetMapping("/daily")
  public ResponseEntity<?> getDaily(@RequestParam(required = false)
                                      final String symbol,
                                    @RequestParam(defaultValue = "false")
                                    final boolean force) {
    try {
      StockDailySeries series = getDailySeries(DEFAULT_SYMBOL, force);
      return ResponseEntity.ok(series);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(jsonError(e.getMessage()));
    }
  }

  /**
   * Returns placeholder prediction for AMZN (hardcoded),
   * ensuring data is cached/fresh.
   *
   *  @param symbol the stock or asset symbol to analyze sentiment for;
   *                may be {@code null} if sentiment should be computed
   *                for a default or global context
   * @param horizon the prediction time frame (e.g. {@code "weekly"});
   *                defaults to {@code "next-day"}
   *  @param force whether to force recomputation of sentiment rather than
   *               using cached results
   *  @return a {@link ResponseEntity} containing the predicted stock prices
   *              or an appropriate error response if the
   *              request cannot be processed
   */
  @GetMapping("/predict")
  public ResponseEntity<?> predict(@RequestParam(required = false)
                                     final String symbol,
                                   @RequestParam(defaultValue = "next-day")
                                   final String horizon,
                                   @RequestParam(defaultValue = "false")
                                     final boolean force) {
    try {
      StockDailySeries series = getDailySeries(DEFAULT_SYMBOL, force);
      Map<String, String> map = forecast
              .predictFuturePrices(DEFAULT_SYMBOL); // placeholder

      return ResponseEntity.ok(Map.of(
          "symbol", DEFAULT_SYMBOL,
          "horizon", horizon,
          "prediction", map,
          "source", series.getSource()
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(jsonError(e.getMessage()));
    }
  }

  /**
   * Retrieves sentiment analysis results for the specified stock symbol.
   *
   *  @param symbol the stock or asset symbol to analyze sentiment for;
   *                may be {@code null} if sentiment should be computed
   *                for a default or global context
   *  @param force whether to force recomputation of sentiment rather than
   *               using cached results
   *  @return a {@link ResponseEntity} containing the sentiment analysis
   *              results or an appropriate error response if the
   *              request cannot be processed
   */
  @GetMapping("/sentiment")
  public ResponseEntity<?> getSentiment(@RequestParam(required = false)
                                          final String symbol,
                                        @RequestParam(defaultValue = "false")
                                        final boolean force) {
    try {
      final String s = DEFAULT_SYMBOL;
      final Path cache = store.newsPath(s);
      if (!force && isFresh(cache, NEWS_CACHE_TTL)) {
        return ResponseEntity.ok(store.read(cache, Map.class));
      }
      var result = news.analyzeSentiment(s); // placeholder returns a POJO
      // persist as generic Map for simplicity in the cache
      Map<String, Object> payload = Map.of(
          "company", result.getCompany(),
          "sentimentScore", result.getSentimentScore(),
          "sentimentLabel", result.getSentimentLabel(),
          "source", "news-placeholder"
      );
      store.write(cache, payload);
      return ResponseEntity.ok(payload);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(jsonError(e.getMessage()));
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

private StockDailySeries getDailySeries(final String symbol,
                                        final boolean force) throws Exception {
  if (symbol == null || symbol.isBlank()) {
    throw new IllegalArgumentException("symbol is required");
  }

  final Path cache = store.dailyPath(symbol);

  // 1) Cache-first: if fresh and not forced, return without needing an API key.
  if (!force && isFresh(cache, DAILY_CACHE_TTL)) {
    return store.read(cache, StockDailySeries.class);
  }

  // 2) Now require a key (env var, system property fallback for tests/CI).
  String key = System.getenv("ALPHAVANTAGE_API_KEY");
  if (key == null || key.isBlank()) {
    key = System.getProperty("alphavantage.api.key", "");
  }
  if (key.isBlank()) {
    throw new IllegalStateException(
            "missing ALPHAVANTAGE_API_KEY (or -Dalphavantage.api.key)");
  }

  // 3) Fetch & persist.
  StockDailySeries fresh = stocks.fetchDaily(symbol, key);
  store.write(cache, fresh);
  return fresh;
}


  private static boolean isFresh(final Path file, final Duration ttl) {
    try {
      if (!Files.exists(file)) {
        return false;
      }
      Instant mtime = Files.getLastModifiedTime(file).toInstant();
      return mtime.isAfter(Instant.now().minus(ttl));
    } catch (Exception e) {
      return false;
    }
  }

  private static String jsonError(final String msg) {
    return "{\"error\":\"" + msg.replace("\"", "'") + "\"}";
  }
}
