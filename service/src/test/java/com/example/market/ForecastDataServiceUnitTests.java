package com.example.market;

import com.example.market.service.forecast.ForecastDataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class contains the unit tests for the ForecastDataService class.
 */
@SpringBootTest
public class ForecastDataServiceUnitTests {
  public static ForecastDataService forecast;

  @BeforeAll
  public static void setup() {
    forecast = new ForecastDataService();
  }

  @Test
  public void testHelloWorld() {
    Map<String, String> test = forecast.predictFuturePricesMain();
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
    assertEquals(prices, test);
  }
}
