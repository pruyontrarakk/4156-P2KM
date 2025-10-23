package com.example.market.service.stock;

import com.example.market.model.stock.StockDailySeries;

/**
 * Interface for stock data services.
 */
public interface StockDataService {
  /**
   * Fetch compact DAILY series for a symbol from the provider.
   * For composite to use.
   *
   * @param symbol the stock symbol
   * @param apiKey the API key
   * @return the stock daily series
   * @throws Exception if an error occurs
   */
  StockDailySeries fetchDaily(String symbol, String apiKey) throws Exception;
}
