package com.example;

import com.example.market.service.forecast.ForecastDataService;
//import com.example.market.service.forecast.python.PythonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class contains the unit tests for the ForecastDataService class.
 */

public class ForecastDataServiceUnitTests {
  public static ForecastDataService forecast;

  @BeforeAll
  public static void setup() {
//    PythonService pythonService = new PythonService();
    forecast = new ForecastDataService();
  }

  @Test
  public void testPredictFuturePrices() {
    Map<String, String> test = forecast.predictFuturePrices("amazon");
    Map<String, String> prices = new HashMap<>();
    prices.put("2025-10-25", "1007.8772940063");
    prices.put("2025-10-24", "1007.8825640767");
    prices.put("2025-10-27", "1007.8708396505");
    prices.put("2025-10-26", "1007.8733858643");
    prices.put("2025-10-31", "1007.8668130798");
    prices.put("2025-10-23", "1007.8867682902");
    prices.put("2025-11-01", "1007.8654511515");
    prices.put("2025-10-30", "1007.8678789368");
    prices.put("2025-10-29", "1007.8688263652");
    prices.put("2025-10-28", "1007.8698922221");
    assertEquals(prices, test);
  }
}
