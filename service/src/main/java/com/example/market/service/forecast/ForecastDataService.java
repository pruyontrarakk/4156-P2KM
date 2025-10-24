package com.example.market.service.forecast;

import com.example.market.service.forecast.python.PythonService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ForecastDataService {

  private final PythonService pythonService;

  // Primary constructor for DI (and for tests to pass a mock)
  public ForecastDataService(PythonService pythonService) {
    this.pythonService = pythonService;
  }

  // If some old code calls no-arg ctor, keep this fallback; remove if not needed.
  public ForecastDataService() {
    this.pythonService = new com.example.market.service.forecast.python.PythonService();
  }

  public Map<String, String> predictFuturePrices(String companyName) {
    return pythonService.predictFuturePrices(companyName);
  }
}
