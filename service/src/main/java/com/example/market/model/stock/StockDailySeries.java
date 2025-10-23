package com.example.market.model.stock;

import java.util.List;

/** Ticker-agnostic compact daily time series payload. Bars are ascending by date. */
public class StockDailySeries {
  private final String symbol;
  private final String asOfIso;   // fetch time
  private final String source;    // provenance string for debugging
  private final List<StockBar> bars;

  public StockDailySeries(String symbol, String asOfIso, String source, List<StockBar> bars) {
    this.symbol = symbol;
    this.asOfIso = asOfIso;
    this.source = source;
    this.bars = bars;
  }

  public String getSymbol() { return symbol; }
  public String getAsOfIso() { return asOfIso; }
  public String getSource() { return source; }
  public List<StockBar> getBars() { return bars; }
}
