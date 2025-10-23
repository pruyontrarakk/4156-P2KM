package com.example.market.model.stock;

import java.util.List;

/**
 * Ticker-agnostic compact daily time series payload.
 * Bars are ascending by date.
 */
public final class StockDailySeries {
  /**
   * Stock symbol.
   */
  private final String symbol;
  /**
   * Fetch time in ISO format.
   */
  private final String asOfIso;
  /**
   * Provenance string for debugging.
   */
  private final String source;
  /**
   * List of stock bars.
   */
  private final List<StockBar> bars;

  /**
   * Constructs a new StockDailySeries.
   *
   * @param symbolParam the stock symbol
   * @param asOfIsoParam the fetch time in ISO format
   * @param sourceParam the provenance string
   * @param barsParam the list of stock bars
   */
  public StockDailySeries(final String symbolParam, final String asOfIsoParam,
      final String sourceParam, final List<StockBar> barsParam) {
    this.symbol = symbolParam;
    this.asOfIso = asOfIsoParam;
    this.source = sourceParam;
    this.bars = barsParam;
  }

  /**
   * Gets the stock symbol.
   *
   * @return the stock symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Gets the fetch time in ISO format.
   *
   * @return the fetch time
   */
  public String getAsOfIso() {
    return asOfIso;
  }

  /**
   * Gets the provenance string.
   *
   * @return the source string
   */
  public String getSource() {
    return source;
  }

  /**
   * Gets the list of stock bars.
   *
   * @return the list of bars
   */
  public List<StockBar> getBars() {
    return bars;
  }
}
