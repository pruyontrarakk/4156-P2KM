package com.example.market;

import com.example.market.service.forecast.ForecastDataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
    String test = forecast.printHelloWorld();
    assertEquals("Hello World", test);
  }
}
