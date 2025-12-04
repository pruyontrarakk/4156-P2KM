package com.example.market.api;

import com.example.market.model.news.SentimentResult;
import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.analysis.AdjustedPredictionService;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  /** Service for adjusting predictions with sentiment analysis. */
  private final AdjustedPredictionService adjustedPrediction;
  /** JSON-backed cache or storage handler. */
  private final JsonStore store;

  /** Default symbol or company name. */
  private static final String DEFAULT_SYMBOL = "AMZN";
  /** Time-to-live for cached daily stock data. */
  private static final Duration DAILY_CACHE_TTL = Duration.ofDays(1);
  /** Time-to-live for cached news data. */
  private static final Duration NEWS_CACHE_TTL  = Duration.ofHours(6);

  /**
   * All args constructor.
   *
   * @param thisStocks a StockDataService object
   * @param thisForecast a ForecastService object
   * @param thisNews a NewsDataService object
   * @param thisAdjustedPrediction an AdjustedPredictionService object
   * @param thisStore a JsonStore object
   * */
  public CompositeController(final StockDataService thisStocks,
                             final ForecastDataService thisForecast,
                             final NewsDataService thisNews,
                             final AdjustedPredictionService
                                 thisAdjustedPrediction,
                             final JsonStore thisStore) {
    this.stocks = thisStocks;
    this.forecast = thisForecast;
    this.news = thisNews;
    this.adjustedPrediction = thisAdjustedPrediction;
    this.store = thisStore;
  }

  /**
   * Retrieves the daily OHLCV (Open–High–Low–Close–Volume) data
   * for the given stock symbol.
   *
   * @param symbol  optional stock symbol to predict;
   *                defaults to a predefined value if omitted
   * @param force  whether to bypass the cache
   *               and fetch a fresh daily series
   * @return a JSON response containing the daily OHLCV series
   *                or an error description
   */
  @GetMapping("/daily")
  public ResponseEntity<?> getDaily(
      @RequestParam(required = false) final String symbol,
      @RequestParam(defaultValue = "false") final boolean force) {
    try {
      final String s = resolveSymbol(symbol);
      StockDailySeries series = getDailySeries(s, force);
      return ResponseEntity.ok(series);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body(jsonError(e.getMessage()));
    }
  }


  /**
   * Generates a stock price prediction for the given symbol
   * and forecast horizon.
   *
   * @param symbol  optional stock symbol to predict;
   *                defaults to a predefined value if omitted
   * @param horizon forecast range (e.g., {@code "next-day"});
   *                determines the prediction scope
   * @param force   whether to bypass cached market data
   *                and fetch fresh values
   * @return a JSON response containing the prediction results
   *                or an error description
   */
  @GetMapping("/predict")
  public ResponseEntity<?> predict(
      @RequestParam(required = false) final String symbol,
      @RequestParam(defaultValue = "10") final int horizon,
      @RequestParam(defaultValue = "false") final boolean force) {
    try {
      final String s = resolveSymbol(symbol);

      // Keep this to warm the cache and surface AlphaVantage errors
      StockDailySeries series = getDailySeries(s, force);

      Map<String, String> map = forecast.predictFuturePrices(s, horizon);

      return ResponseEntity.ok(Map.of(
          "symbol", s,
          "horizon", horizon,
          "prediction", map,
          "source", series.getSource()
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body(jsonError(e.getMessage()));
    }
  }


  /**
   * Retrieves sentiment analysis results for the default symbol.
   *
   * @param symbol symbol representing company name.
   * @param force a Boolean value determining if method uses the cache
   *              or forcefully run analysis again.
   * @return a JSON response containing sentiment data
   *              or an error response on failure
   **/
  @GetMapping("/sentiment")
  public ResponseEntity<?> getSentiment(
        @RequestParam(required = false) final String symbol,
        @RequestParam(defaultValue = "false") final boolean force) {

    try {
        // Use provided symbol or default
        final String s = (symbol != null && !symbol.isBlank())
                ? symbol.toUpperCase()
                : DEFAULT_SYMBOL;

        final Path cache = store.newsPath(s);

        if (!force && isFresh(cache, NEWS_CACHE_TTL)) {
            return ResponseEntity.ok(store.read(cache, Map.class));
        }

        // Sentiment now depends on the symbol
        var result = news.analyzeSentiment(s);

        Map<String, Object> payload = Map.of(
          "company", s,  // include company name (symbol)
          "symbol", s,
          "sentimentScore", result.getSentimentScore(),
          "sentimentLabel", result.getSentimentLabel(),
          "source", "HuggingFaceModel"
          );

        store.write(cache, payload);
        return ResponseEntity.ok(payload);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(jsonError(e.getMessage() != null ? e.getMessage() : e.toString()));
    }
}

  /**
   * Generates sentiment-adjusted stock price predictions by adjusting
   * price forecasts with news sentiment analysis.
   *
   * @param symbol optional stock symbol to predict;
   *               defaults to a predefined value if omitted
   * @param horizon X amount of days to predict into future
   * @param force  whether to bypass cached market data
   *               and fetch fresh values
   * @return a JSON response containing sentiment-adjusted predictions
   *               or an error description
   */
  @GetMapping("/combined-prediction")
  public ResponseEntity<?> getCombinedPrediction(
      @RequestParam(required = false) final String symbol,
      @RequestParam(defaultValue = "10") final int horizon,
      @RequestParam(defaultValue = "false") final boolean force) {
    try {
      final String s = resolveSymbol(symbol);

      // 1) Get price predictions for this symbol
      Map<String, String> pricePredictions;
      try {
        pricePredictions = forecast.predictFuturePrices(s, horizon);
        if (pricePredictions == null || pricePredictions.isEmpty()) {
          return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(jsonError("Forecast service returned empty predictions"));
        }
      } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(jsonError("Forecast service error: " + e.getMessage()));
      }

      // 2) Get sentiment for this symbol
      SentimentResult sentimentResult;
      try {
        sentimentResult = news.analyzeSentiment(s);
        if (sentimentResult == null) {
          return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(jsonError("Sentiment service returned null result"));
        }
      } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(jsonError("Sentiment service error: " + e.getMessage()));
      }

      // 3) Adjust predictions with sentiment
      Map<String, String> adjustedPredictions = adjustedPrediction
          .adjustPricesWithSentiment(pricePredictions, sentimentResult);

      // 4) Build response payload
      return ResponseEntity.ok(Map.of(
          "symbol", s,
          "sentiment", Map.of(
              "score", sentimentResult.getSentimentScore(),
              "label", sentimentResult.getSentimentLabel()
          ),
          "originalPredictions", pricePredictions,
          "adjustedPredictions", adjustedPredictions
      ));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(jsonError(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body(jsonError("Unexpected error: " + e.getMessage()));
    }
  }


  /* ---------------- helpers ---------------- */
  /**
   * Resolve the effective stock symbol to use.
   * Uses the provided symbol if non-blank, otherwise falls back to DEFAULT_SYMBOL.
   */
  private String resolveSymbol(final String symbol) {
    return (symbol != null && !symbol.isBlank())
        ? symbol.toUpperCase()
        : DEFAULT_SYMBOL;
  }

  /**
   * Returns the daily stock series for the given symbol, using cache when valid
   * and refreshing from the remote API when necessary.
   *
   * @param symbol the stock symbol; must be non-blank
   * @param force  whether to bypass the cache and force a fresh fetch
   * @return the resolved {@link StockDailySeries}
   */
  public StockDailySeries getDailySeries(final String symbol,
                                          final boolean force)
          throws Exception {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol is required");
    }

    final Path cache = store.dailyPath(symbol);

    // 1) Cache-first: if fresh and not forced
    // return without needing an API key.
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
              "missing ALPHAVANTAGE_API_KEY (or -Dalphavantage.api.key)"
      );
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
    String safe = (msg == null ? "Unknown error" : msg)
        .replace("\\", "\\\\")  // escape backslashes first
        .replace("\"", "\\\"")  // escape quotes
        .replace("\n", "\\n")   // escape newlines
        .replace("\r", "\\r")   // escape carriage returns
        .replace("\t", "\\t");  // escape tabs
    return "{\"error\":\"" + safe + "\"}";
  }

}
