package com.example.market.service.forecast;

import com.example.market.api.dto.PredictionResponse;
import com.example.market.model.stock.StockDailySeries;

public interface ForecastingPort {
  /**
   * Defines the contract for generating forecast predictions
   * based on historical stock data.
   *
   * @param symbol   the stock ticker symbol to forecast
   *                 (e.g., "AAPL", "AMZN")
   * @param horizon  the forecast duration or time window
   *                 (e.g., "7d", "30d")
   * @param series   the historical daily stock data used as input
   *                 for the forecast model
   * @return {@code PredictionResponse}
   */
  PredictionResponse predict(String symbol,
                             String horizon,
                             StockDailySeries series);
}
