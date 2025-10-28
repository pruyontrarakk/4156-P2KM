package com.example;

import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.forecast.python.PythonService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ForecastDataServiceUnitTests {

  @Test
  void testPredictFuturePrices_deterministic() {
    PythonService py = mock(PythonService.class);
    Map<String, String> fake = Map.of(
        "2025-10-23", "1007.8867682902",
        "2025-10-24", "1007.8825640767"
    );

    // NOTE: PythonService now requires a String parameter
    when(py.predictFuturePrices(anyString())).thenReturn(fake);

    ForecastDataService svc = new ForecastDataService(py);
    Map<String, String> got = svc.predictFuturePrices("AMZN");

    assertEquals(fake, got);
    verify(py).predictFuturePrices("AMZN");
  }
}
