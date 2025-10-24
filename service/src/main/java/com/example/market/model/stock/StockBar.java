package com.example.market.model.stock;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

/**
 * OHLCV bar for a given timestamp (daily uses yyyy-MM-dd).
 */
public final class StockBar {
  /**
   * Timestamp string (e.g., "2025-10-22").
   */
  private final String timestamp;
  /**
   * Opening price.
   */
  private final BigDecimal open;
  /**
   * High price.
   */
  private final BigDecimal high;
  /**
   * Low price.
   */
  private final BigDecimal low;
  /**
   * Closing price.
   */
  private final BigDecimal close;
  /**
   * Trading volume.
   */
  private final long volume;

  /**
   * Constructs a new StockBar.
   *
   * @param timestampParam the timestamp string
   * @param openParam the opening price
   * @param highParam the high price
   * @param lowParam the low price
   * @param closeParam the closing price
   * @param volumeParam the trading volume
   */
  public StockBar(final String timestampParam, final BigDecimal openParam,
      final BigDecimal highParam, final BigDecimal lowParam,
      final BigDecimal closeParam, final long volumeParam) {
    this.timestamp = timestampParam;
    this.open = openParam;
    this.high = highParam;
    this.low = lowParam;
    this.close = closeParam;
    this.volume = volumeParam;
  }

  /**
   * Gets the timestamp.
   *
   * @return the timestamp string
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * Gets the opening price.
   *
   * @return the opening price
   */
  public BigDecimal getOpen() {
    return open;
  }

  /**
   * Gets the high price.
   *
   * @return the high price
   */
  public BigDecimal getHigh() {
    return high;
  }

  /**
   * Gets the low price.
   *
   * @return the low price
   */
  public BigDecimal getLow() {
    return low;
  }

  /**
   * Gets the closing price.
   *
   * @return the closing price
   */
  public BigDecimal getClose() {
    return close;
  }

  /**
   * Gets the trading volume.
   *
   * @return the volume
   */
  public long getVolume() {
    return volume;
  }

  /**
   * Factory for Alpha Vantage DAILY JSON node
   * (keys "1. open", "2. high", ...).
   *
   * @param dateKey the date key
   * @param node the JSON node
   * @return a new StockBar instance
   */
  public static StockBar fromAlphaDaily(
      final String dateKey, final JsonNode node
  ) {
    BigDecimal open = new BigDecimal(text(node, "1. open"));
    BigDecimal high = new BigDecimal(text(node, "2. high"));
    BigDecimal low = new BigDecimal(text(node, "3. low"));
    BigDecimal close = new BigDecimal(text(node, "4. close"));
    long volume = parseLong(text(node, "5. volume"));
    return new StockBar(dateKey, open, high, low, close, volume);
  }

  /**
   * Extracts text value from JSON node.
   *
   * @param node the JSON node
   * @param field the field name
   * @return the text value
   */
  private static String text(final JsonNode node, final String field) {
    JsonNode v = node.get(field);
    if (v == null || v.isMissingNode() || v.asText().isBlank()) {
      throw new IllegalStateException(
          "Missing field in AlphaVantage daily node: " + field);
    }
    return v.asText();
  }

  /**
   * Parses a string to long, handling decimal numbers.
   *
   * @param s the string to parse
   * @return the parsed long value
   */
  private static long parseLong(final String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException ex) {
      return new BigDecimal(s).longValue();
    }
  }
}
