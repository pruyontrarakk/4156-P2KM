package com.example.amzn.core;

import com.example.amzn.model.AmznDaily;
import com.example.amzn.model.Bar;
import com.example.amzn.model.IntradaySeries;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Alpha Vantage API client (HTTP + JSON parsing).
 * Uses TIME_SERIES_DAILY (non-adjusted) and TIME_SERIES_INTRADAY.
 */
@Component
public final class AlphaVantageService {

  private static final int TIMEOUT_SECONDS = 30;
  private static final int HTTP_SUCCESS_MIN = 200;
  private static final int HTTP_SUCCESS_MAX = 300;

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public AlphaVantageService() {
    this.objectMapper = new ObjectMapper();
    this.httpClient = HttpClient.newHttpClient();
  }

  // ---------------------------------------------------------------------------
  // DAILY 
  // ---------------------------------------------------------------------------

  /**
   * Fetch full daily history (20+ years) using TIME_SERIES_DAILY.
   * We mirror "adjClose = close" to keep the Bar record stable.
   */
  public AmznDaily fetchFullHistory(final String symbol, final String apiKey) {
    final String url = "https://www.alphavantage.co/query?"
        + "function=TIME_SERIES_DAILY"
        + "&symbol=" + symbol
        + "&outputsize=full"
        + "&apikey=" + apiKey
        + "&datatype=json";

    final JsonNode root = httpGetJson(url);
    final JsonNode series = root.get("Time Series (Daily)");
    if (series == null || series.isNull()) {
      throw new RuntimeException("Missing daily series: Time Series (Daily)");
    }

    final List<Bar> bars = parseDaily(series);
    Collections.sort(bars, (a, b) -> a.date().compareTo(b.date())); // ascending

    return new AmznDaily(
        symbol,
        Instant.now().toString(),
        "alphavantage: TIME_SERIES_DAILY (full), url=" + url,
        bars
    );
  }

  // ---------------------------------------------------------------------------
  // INTRADAY
  // ---------------------------------------------------------------------------

  /**
   * Latest intraday bars (Alpha Vantage returns ~100 bars by default.
   * Requires interval in {1min,5min,15min,30min,60min}.
   */
  public IntradaySeries fetchIntradayLatest(final String symbol,
                                            final String interval,
                                            final String apiKey) {
    validateInterval(interval);
    final String url = "https://www.alphavantage.co/query?"
        + "function=TIME_SERIES_INTRADAY"
        + "&symbol=" + symbol
        + "&interval=" + interval
        + "&apikey=" + apiKey
        + "&datatype=json";

    final JsonNode root = httpGetJson(url);
    final String seriesKey = "Time Series (" + interval + ")";
    final JsonNode series = root.get(seriesKey);
    if (series == null || series.isNull()) {
      throw new RuntimeException("Missing intraday series: " + seriesKey);
    }

    final List<Bar> bars = parseIntraday(series);
    Collections.sort(bars, (a, b) -> a.date().compareTo(b.date()));

    return new IntradaySeries(
        symbol,
        interval,
        Instant.now().toString(),
        "alphavantage: TIME_SERIES_INTRADAY (latest default ~100), url=" + url,
        bars
    );
  }

  /**
   * Intraday bars for a specific month in history (since 2000-01).
   */
  public IntradaySeries fetchIntradayByMonth(final String symbol,
                                             final String interval,
                                             final String month,
                                             final String apiKey) {
    validateInterval(interval);
    validateMonth(month);
    final String url = "https://www.alphavantage.co/query?"
        + "function=TIME_SERIES_INTRADAY"
        + "&symbol=" + symbol
        + "&interval=" + interval
        + "&month=" + month
        + "&outputsize=full"
        + "&apikey=" + apiKey
        + "&datatype=json";

    final JsonNode root = httpGetJson(url);
    final String seriesKey = "Time Series (" + interval + ")";
    final JsonNode series = root.get(seriesKey);
    if (series == null || series.isNull()) {
      throw new RuntimeException("Missing intraday series: " + seriesKey);
    }

    final List<Bar> bars = parseIntraday(series);
    Collections.sort(bars, (a, b) -> a.date().compareTo(b.date()));

    return new IntradaySeries(
        symbol,
        interval,
        Instant.now().toString(),
        "alphavantage: TIME_SERIES_INTRADAY (month=" + month + ", full), url=" + url,
        bars
    );
  }

  // ---------------------------------------------------------------------------
  // HTTP/JSON
  // ---------------------------------------------------------------------------

  private JsonNode httpGetJson(final String url) {
    try {
      final HttpRequest req = HttpRequest.newBuilder(URI.create(url))
          .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
          .header("User-Agent", "amzn-service")
          .GET()
          .build();
      final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      final int code = resp.statusCode();
      if (code < HTTP_SUCCESS_MIN || code >= HTTP_SUCCESS_MAX) {
        throw new RuntimeException("HTTP " + code + " from Alpha Vantage");
      }
      return objectMapper.readTree(resp.body());
    } catch (Exception e) {
      throw new RuntimeException("HTTP/JSON error: " + e.getMessage(), e);
    }
  }

  // ---------------------------------------------------------------------------
  // Parsers
  // ---------------------------------------------------------------------------

  /**
   * TIME_SERIES_DAILY object:
   * {
   *   "2025-10-22": {
   *     "1. open": "x", "2. high": "x", "3. low": "x",
   *     "4. close": "x", "5. volume": "n"
   *   }, ...
   * }
   */
  private List<Bar> parseDaily(final JsonNode series) {
    final List<Bar> out = new ArrayList<>();
    series.fieldNames().forEachRemaining(ts -> {
      final JsonNode o = series.get(ts);
      final Double open  = asDouble(o.get("1. open"));
      final Double high  = asDouble(o.get("2. high"));
      final Double low   = asDouble(o.get("3. low"));
      final Double close = asDouble(o.get("4. close"));
      final Long volume  = asLong(o.get("5. volume"));
      out.add(new Bar(
          ts, open, high, low, close,
          close,      // adjClose = close (non-adjusted endpoint)
          volume
      ));
    });
    return out;
  }

  /**
   * TIME_SERIES_INTRADAY object:
   * {
   *   "2025-10-22 15:35:00": {
   *     "1. open": "x", "2. high": "x", "3. low": "x",
   *     "4. close": "x", "5. volume": "n"
   *   }, ...
   * }
   */
  private List<Bar> parseIntraday(final JsonNode series) {
    final List<Bar> out = new ArrayList<>();
    series.fieldNames().forEachRemaining(ts -> {
      final JsonNode o = series.get(ts);
      final Double open  = asDouble(o.get("1. open"));
      final Double high  = asDouble(o.get("2. high"));
      final Double low   = asDouble(o.get("3. low"));
      final Double close = asDouble(o.get("4. close"));
      final Long volume  = asLong(o.get("5. volume"));
      out.add(new Bar(
          ts, open, high, low, close,
          close,  
          volume
      ));
    });
    return out;
  }

  private static Double asDouble(final JsonNode node) {
    if (node == null || node.isNull()) return null;
    try { return Double.parseDouble(node.asText()); } catch (Exception e) { return null; }
  }

  private static Long asLong(final JsonNode node) {
    if (node == null || node.isNull()) return null;
    try { return Long.parseLong(node.asText()); } catch (Exception e) { return null; }
  }

  private static void validateInterval(final String interval) {
    if (!("1min".equals(interval) || "5min".equals(interval) || "15min".equals(interval)
        || "30min".equals(interval) || "60min".equals(interval))) {
      throw new IllegalArgumentException("invalid interval: " + interval);
    }
  }

  private static void validateMonth(final String month) {
    if (month == null || !month.matches("\\d{4}-\\d{2}")) {
      throw new IllegalArgumentException("invalid month, expected YYYY-MM");
    }
  }
}
