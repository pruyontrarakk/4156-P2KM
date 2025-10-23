package com.example.market.service.stock;

import com.example.market.model.stock.StockDailySeries;

public interface StockDataService {
  /** Fetch compact DAILY series for a symbol from the provider. */
  StockDailySeries fetchDaily(String symbol, String apiKey) throws Exception;
}
