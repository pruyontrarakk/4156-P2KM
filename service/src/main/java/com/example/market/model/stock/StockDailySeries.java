// StockDailySeries.java
package com.example.market.model.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class StockDailySeries {
  private final String symbol;
  private final String asOfIso;
  private final String source;
  private final List<StockBar> bars;

  @JsonCreator
  public StockDailySeries(
      @JsonProperty("symbol") String symbolParam,
      @JsonProperty("asOfIso") String asOfIsoParam,
      @JsonProperty("source") String sourceParam,
      @JsonProperty("bars") List<StockBar> barsParam) {
    this.symbol = symbolParam;
    this.asOfIso = asOfIsoParam;
    this.source = sourceParam;
    this.bars = barsParam;
  }

  public String getSymbol() { return symbol; }
  public String getAsOfIso() { return asOfIso; }
  public String getSource() { return source; }
  public List<StockBar> getBars() { return bars; }
}
