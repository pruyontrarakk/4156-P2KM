package com.example.market.service.forecast;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the Forecast Data Service. It defines
 * useful methods for predicting future stock prices and trends.
 */
@Service
public class ForecastDataService {

  /**
   * Constructs a new {@code ForecastDataService}
   */
  public ForecastDataService() {}

  /**
   * Predicts what the stock price will be over the course of 10 days.
   *
   * @param companyName A {@code String} object containing the company name
   */
  public Map<String, String> predictFuturePrices(String companyName) {
    Map<String, String> prices = new HashMap<>();
    prices.put("Date1","Price1");
    prices.put("Date2","Price2");
    prices.put("Date3","Price3");
    prices.put("Date4","Price4");
    prices.put("Date5","Price5");
    prices.put("Date6","Price6");
    prices.put("Date7","Price7");
    prices.put("Date8","Price8");
    prices.put("Date9","Price9");
    prices.put("Date10","Price10");

    return prices;
  }

}
