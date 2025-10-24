package com.example.market.api;

import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CompositeControllerPrivateTest {

  @Test
  void getDailySeries_blankSymbol_throwsIllegalArgument() throws Exception {
    CompositeController controller = new CompositeController(
        mock(StockDataService.class),
        mock(ForecastDataService.class),
        mock(NewsDataService.class),
        mock(JsonStore.class)
    );

    Method m = CompositeController.class
        .getDeclaredMethod("getDailySeries", String.class, boolean.class);
    m.setAccessible(true);

    InvocationTargetException ex = assertThrows(InvocationTargetException.class,
        () -> m.invoke(controller, "  ", false));

    assertTrue(ex.getCause() instanceof IllegalArgumentException);
    assertEquals("symbol is required", ex.getCause().getMessage());
  }
}
