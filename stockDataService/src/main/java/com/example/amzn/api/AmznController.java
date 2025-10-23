package com.example.amzn.api;

import com.example.amzn.core.AlphaVantageService;
import com.example.amzn.core.JsonStore;
import com.example.amzn.model.AmznDaily;
import com.example.amzn.model.IntradaySeries;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * REST API: daily snapshot + intraday endpoints.
 */
@RestController
@RequestMapping("/amzn")
public class AmznController {

  private static final int BAD_REQUEST = 400;
  private static final int NOT_FOUND = 404;
  private static final int BAD_GATEWAY = 502;
  private static final int INTERNAL_ERROR = 500;

  private final AlphaVantageService avService;
  private final JsonStore jsonStore;

  public AmznController(final AlphaVantageService avService, final JsonStore jsonStore) {
    this.avService = avService;
    this.jsonStore = jsonStore;
  }

  // -------- DAILY --------

  /**
   * Returns the cached full daily history JSON from disk, if present.
   * File path: data/amzn-daily.json
   */
  @GetMapping("/daily")
  public ResponseEntity<?> getDaily() {
    final Path file = Path.of("data", "amzn-daily.json");
    final Optional<AmznDaily> data = jsonStore.read(file);
    return data.<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(NOT_FOUND)
            .body("{\"error\":\"not_found\"}"));
  }

  /**
   * Refresh the cached daily history using TIME_SERIES_DAILY.
   * Requires env ALPHAVANTAGE_API_KEY.
   */
  @PostMapping("/daily")
  public ResponseEntity<?> postDaily() {
    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.status(INTERNAL_ERROR)
          .body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    final Path file = Path.of("data", "amzn-daily.json");
    try {
      final AmznDaily out = avService.fetchFullHistory("AMZN", key);
      jsonStore.writeAtomic(file, out);
      return ResponseEntity.accepted().body("{\"status\":\"updated\"}");
    } catch (IOException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"io\"}");
    } catch (RuntimeException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"fetch/parse failed\"}");
    }
  }

  // -------- INTRADAY LATEST --------

  /**
   * Latest ~100 intraday bars for a symbol/interval.
   * Example: /amzn/intraday/latest?symbol=AMZN&interval=5min
   */
  @GetMapping("/intraday/latest")
  public ResponseEntity<?> getIntradayLatest(
      @RequestParam(name = "symbol", defaultValue = "AMZN") final String symbol,
      @RequestParam(name = "interval", defaultValue = "5min") final String interval) {

    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.status(INTERNAL_ERROR)
          .body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    try {
      final IntradaySeries out = avService.fetchIntradayLatest(symbol, interval, key);
      return ResponseEntity.ok(out);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(BAD_REQUEST)
          .body("{\"error\":\"" + e.getMessage() + "\"}");
    } catch (RuntimeException e) {
      return ResponseEntity.status(BAD_GATEWAY)
          .body("{\"error\":\"fetch/parse failed\"}");
    }
  }

    // Save latest intraday to data/<symbol>-<interval>-latest.json
  @PostMapping("/intraday/latest")
  public ResponseEntity<?> postIntradayLatest(
      @RequestParam(name = "symbol", defaultValue = "AMZN") final String symbol,
      @RequestParam(name = "interval", defaultValue = "5min") final String interval) {

    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.status(INTERNAL_ERROR).body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    final Path file = Path.of("data", (symbol + "-" + interval + "-latest.json"));
    try {
      final IntradaySeries out = avService.fetchIntradayLatest(symbol, interval, key); // <-- IntradaySeries
      jsonStore.writeAtomic(file, out);
      return ResponseEntity.accepted().body("{\"status\":\"updated\",\"file\":\"" + file + "\"}");
    } catch (IOException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"io\"}");
    } catch (RuntimeException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"fetch/parse failed\"}");
    }
  }

  // -------- INTRADAY PICK MONTH --------

  /**
   * Intraday bars for a specific month.
   * Example: /amzn/intraday/month?symbol=AMZN&interval=5min&month=2009-01
   */
  @GetMapping("/intraday/month")
  public ResponseEntity<?> getIntradayMonth(
      @RequestParam(name = "symbol", defaultValue = "AMZN") final String symbol,
      @RequestParam(name = "interval", defaultValue = "5min") final String interval,
      @RequestParam(name = "month") final String month) {

    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.status(INTERNAL_ERROR)
          .body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    try {
      final IntradaySeries out = avService.fetchIntradayByMonth(symbol, interval, month, key);
      return ResponseEntity.ok(out);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(BAD_REQUEST)
          .body("{\"error\":\"" + e.getMessage() + "\"}");
    } catch (RuntimeException e) {
      return ResponseEntity.status(BAD_GATEWAY)
          .body("{\"error\":\"fetch/parse failed\"}");
    }
  }

  // Save month intraday to data/<symbol>-<interval>-<month>.json
  @PostMapping("/intraday/month")
  public ResponseEntity<?> postIntradayMonth(
      @RequestParam(name = "symbol", defaultValue = "AMZN") final String symbol,
      @RequestParam(name = "interval", defaultValue = "5min") final String interval,
      @RequestParam(name = "month") final String month) {

    final String key = System.getenv("ALPHAVANTAGE_API_KEY");
    if (key == null || key.isBlank()) {
      return ResponseEntity.status(INTERNAL_ERROR).body("{\"error\":\"missing env ALPHAVANTAGE_API_KEY\"}");
    }
    final Path file = Path.of("data", (symbol + "-" + interval + "-" + month + ".json"));
    try {
      final IntradaySeries out = avService.fetchIntradayByMonth(symbol, interval, month, key); // <-- IntradaySeries
      jsonStore.writeAtomic(file, out);
      return ResponseEntity.accepted().body("{\"status\":\"updated\",\"file\":\"" + file + "\"}");
    } catch (IOException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"io\"}");
    } catch (RuntimeException e) {
      return ResponseEntity.status(BAD_GATEWAY).body("{\"error\":\"fetch/parse failed\"}");
    }
  }


}
