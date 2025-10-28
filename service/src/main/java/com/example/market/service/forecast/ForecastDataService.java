package com.example.market.service.forecast;

import com.example.market.service.forecast.python.PythonService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ForecastDataService {
  /**
   * Service responsible for executing Python-based forecasting scripts
   * and returning their parsed prediction results.
   */
  private final PythonService pythonService;

  /**
   * Constructs a new {@code ForecastDataService}.
   */
  public ForecastDataService() {
    this.pythonService = new PythonService();
  }

  /**
   * Predicts what the stock price will be over the course of 10 days.
   *
   * @param companyName A {@code String} object containing the company name
   * @return a {@code Map} where each key is a date (as a {@code String}) and
   *                    each value is the corresponding predicted closing price
   *                    (also as a {@code String})
   */
  public Map<String, String> predictFuturePrices(final String companyName) {
    return pythonService.predictFuturePrices();
  }
}
