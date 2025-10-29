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
//    Map<String, String> fake = Map.of(
//        "2025-10-23", "1007.8867682902",
//        "2025-10-24", "1007.8825640767"
//    );

    Map<String, String> fake = Map.of(
            "2025-10-25", "1007.8772940063",
            "2025-10-24", "1007.8825640767",
            "2025-10-27", "1007.8708396505",
            "2025-10-26", "1007.8733858643",
            "2025-10-31", "1007.8668130798",
            "2025-10-23", "1007.8867682902",
            "2025-11-01", "1007.8654511515",
            "2025-10-30", "1007.8678789368",
            "2025-10-29", "1007.8688263652",
            "2025-10-28", "1007.8698922221"
    );

    // NOTE: PythonService now requires a String parameter
    when(py.predictFuturePrices(anyString())).thenReturn(fake);

    ForecastDataService svc = new ForecastDataService(py);
    Map<String, String> got = svc.predictFuturePrices("AMZN");

    assertEquals(fake, got);
//    verify(py).predictFuturePrices("AMZN");
  }
}
