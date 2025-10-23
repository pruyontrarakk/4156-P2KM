package com.example.market.model.stock;


import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

/** OHLCV bar for a given timestamp (daily uses yyyy-MM-dd). */
public class StockBar {
  private final String timestamp;       // e.g., "2025-10-22"
  private final BigDecimal open;
  private final BigDecimal high;
  private final BigDecimal low;
  private final BigDecimal close;
  private final long volume;

  public StockBar(String timestamp, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
    this.timestamp = timestamp;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
  }

  public String getTimestamp() { return timestamp; }
  public BigDecimal getOpen() { return open; }
  public BigDecimal getHigh() { return high; }
  public BigDecimal getLow() { return low; }
  public BigDecimal getClose() { return close; }
  public long getVolume() { return volume; }

  /** Factory for Alpha Vantage DAILY JSON node (keys "1. open", "2. high", ...). */
  public static StockBar fromAlphaDaily(String dateKey, JsonNode node) {
    BigDecimal open  = new BigDecimal(text(node, "1. open"));
    BigDecimal high  = new BigDecimal(text(node, "2. high"));
    BigDecimal low   = new BigDecimal(text(node, "3. low"));
    BigDecimal close = new BigDecimal(text(node, "4. close"));
    long volume      = parseLong(text(node, "5. volume"));
    return new StockBar(dateKey, open, high, low, close, volume);
  }

  private static String text(JsonNode node, String field) {
    JsonNode v = node.get(field);
    if (v == null || v.isMissingNode() || v.asText().isBlank()) {
      throw new IllegalStateException("Missing field in AlphaVantage daily node: " + field);
    }
    return v.asText();
  }

  private static long parseLong(String s) {
    try { return Long.parseLong(s); }
    catch (NumberFormatException ex) { return new BigDecimal(s).longValue(); }
  }
}
