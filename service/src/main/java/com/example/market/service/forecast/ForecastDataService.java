package com.example.market.service.forecast;


import com.example.market.service.forecast.python.PythonService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This class defines the Forecast Data Service. It defines
 * useful methods for predicting future stock prices and trends.
 */
@Service
public class ForecastDataService {

  private final PythonService pythonService;

  /**
   * Constructs a new {@code ForecastDataService}
   */
  public ForecastDataService() {
    this.pythonService = new PythonService();
  }

  /**
   * Predicts what the stock price will be over the course of 10 days.
   *
   * @param companyName A {@code String} object containing the company name
   */
  public Map<String, String> predictFuturePrices(String companyName) {
    return pythonService.predictFuturePrices();
  }

}