// StockDailySeries.java
package com.example.market.model.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class StockDailySeries {
  /** The stock symbol this time series represents.*/
  private final String symbol;
  /** The ISO timestamp indicating when this data was last updated. */
  private final String asOfIso;
  /** The data source from which this series was retrieved
   * (e.g., Alpha Vantage). */
  private final String source;
  /** The list of daily stock bars containing OHLC and volume data. */
  private final List<StockBar> bars;

  /**
   * Creates a new {@code StockDailySeries} instance with
   * the given symbol, timestamp, source, and list of stock bars.
   *
   * @param symbolParam the stock symbol represented by this series
   * @param asOfIsoParam the ISO 8601 date or timestamp of the data
   * @param sourceParam the data provider or source of this series
   * @param barsParam the list of daily {@link StockBar} entries
   */
  @JsonCreator
  public StockDailySeries(
      @JsonProperty("symbol")
      final String symbolParam,
      @JsonProperty("asOfIso")
      final String asOfIsoParam,
      @JsonProperty("source")
      final String sourceParam,
      @JsonProperty("bars")
      final List<StockBar> barsParam) {
    this.symbol = symbolParam;
    this.asOfIso = asOfIsoParam;
    this.source = sourceParam;
    this.bars = barsParam;
  }

  /**
   * Getter of symbol.
   *
   * @return symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Getter of iso.
   *
   * @return iso
   */
  public String getAsOfIso() {
    return asOfIso;
  }

  /**
   * Getter of source.
   *
   * @return source
   */
  public String getSource() {
    return source;
  }

  /**
   * Getter of bars.
   *
   * @return list of stock bars
   */
  public List<StockBar> getBars() {
    return bars;
  }
}
