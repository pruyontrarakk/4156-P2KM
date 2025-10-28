package com.example.market.model.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * OHLCV bar for a given timestamp (daily uses yyyy-MM-dd).
 * be tolerant to extra fields in cached JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class StockBar {
  /** Timestamp string (e.g., "2025-10-22"). */
  private final String timestamp;
  /** Opening price. */
  private final BigDecimal open;
  /** High price. */
  private final BigDecimal high;
  /** Low price. */
  private final BigDecimal low;
  /** Closing price. */
  private final BigDecimal close;
  /** Trading volume. */
  private final long volume;

  /**
   * Creates a {@code StockBar} representing a single OHLC (Open, High,
   * Low, Close) data point with its corresponding volume and timestamp.
   *
   * @param timestampParam the timestamp associated with this stock bar
   * @param openParam      the opening price
   * @param highParam      the highest price
   * @param lowParam       the lowest price
   * @param closeParam     the closing price
   * @param volumeParam    the traded volume for the given period
   */
  @JsonCreator
  public StockBar(
      @JsonProperty(value = "timestamp", required = true)
      final String timestampParam,
      @JsonProperty(value = "open",      required = true)
      final BigDecimal openParam,
      @JsonProperty(value = "high",      required = true)
      final BigDecimal highParam,
      @JsonProperty(value = "low",       required = true)
      final BigDecimal lowParam,
      @JsonProperty(value = "close",     required = true)
      final BigDecimal closeParam,
      @JsonProperty(value = "volume",    required = true)
      final long volumeParam) {
    this.timestamp = Objects.requireNonNull(timestampParam, "timestamp");
    this.open = Objects.requireNonNull(openParam, "open");
    this.high = Objects.requireNonNull(highParam, "high");
    this.low = Objects.requireNonNull(lowParam, "low");
    this.close = Objects.requireNonNull(closeParam, "close");
    this.volume = volumeParam;
  }

  /**
   * Creates a {@code StockBar} instance from an
   * Alpha Vantage "daily" JSON record.
   *
   * @param dateKey the date string (e.g., {@code "2025-10-28"})
   *                representing this record
   * @param node    the JSON node containing Alpha Vantage daily data fields
   * @return a {@link StockBar} populated with the parsed OHLC and volume data
   */
  public static StockBar fromAlphaDaily(final String dateKey,
                                        final JsonNode node) {
    BigDecimal open  = new BigDecimal(text(node, "1. open"));
    BigDecimal high  = new BigDecimal(text(node, "2. high"));
    BigDecimal low   = new BigDecimal(text(node, "3. low"));
    BigDecimal close = new BigDecimal(text(node, "4. close"));
    long volume      = parseLong(text(node, "5. volume"));
    return new StockBar(dateKey, open, high, low, close, volume);
  }

  /* ---------- getters ---------- */
  /** Getter for timestamp.
   *
   * @return timestamp
   */
  public String getTimestamp() {
    return timestamp;
  }

  /** Getter for open.
   *
   * @return open
   */
  public BigDecimal getOpen()  {
    return open;
  }

  /** Getter for high.
   *
   * @return high
   */
  public BigDecimal getHigh()  {
    return high;
  }

  /** Getter for low.
   *
   * @return low
   */
  public BigDecimal getLow()   {
    return low;
  }

  /** Getter for close.
   *
   * @return close
   */
  public BigDecimal getClose() {
    return close;
  }

  /** Getter for volume.
   *
   * @return volume
   */
  public long getVolume() {
    return volume;
  }

  /* ---------- helpers ---------- */

  /**
   * Extracts text value from JSON node.
   *
   * @param node Json node
   * @param field key field in Json
   * @return text
   */
  private static String text(final JsonNode node, final String field) {
    JsonNode v = node.get(field);
    if (v == null || v.isMissingNode() || v.asText().isBlank()) {
      throw new IllegalStateException(
              "Missing field in AlphaVantage daily node: "
              + field);
    }
    return v.asText();
  }

  /**
   * Parses a string to long, handling decimal numbers like "5.0".
   *
   * @param s string version of value
   * @return long value
   */
  private static long parseLong(final String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException ex) {
      return new BigDecimal(s).longValue();
    }
  }

  /* ---------- value semantics (nice for tests/logging) ---------- */

  @Override public String toString() {
    return "StockBar{"
            + "timestamp='"
            + timestamp + '\''
            + ", open=" + open
            + ", high=" + high
            + ", low=" + low
            + ", close=" + close
            + ", volume=" + volume
            + '}';
  }

  @Override public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StockBar)) {
      return false;
    }
    StockBar that = (StockBar) o;
    return volume == that.volume
            && Objects.equals(timestamp, that.timestamp)
            && Objects.equals(open, that.open)
            && Objects.equals(high, that.high)
            && Objects.equals(low, that.low)
            && Objects.equals(close, that.close);
    }

  @Override public int hashCode() {
    return Objects.hash(timestamp, open, high, low, close, volume);
  }
}
