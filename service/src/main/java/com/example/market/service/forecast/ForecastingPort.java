package com.example.market.service.forecast;

import com.example.market.api.dto.PredictionResponse;
import com.example.market.model.stock.StockDailySeries;

public interface ForecastingPort {
  PredictionResponse predict(String symbol, String horizon, StockDailySeries series);
}
