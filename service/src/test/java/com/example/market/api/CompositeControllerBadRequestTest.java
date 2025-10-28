package com.example.market.api;

import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompositeController.class)
class CompositeControllerBadRequestTest {

  @Autowired MockMvc mvc;

  @MockBean StockDataService stocks;
  @MockBean ForecastDataService forecast;
  @MockBean NewsDataService news;
  @MockBean JsonStore store;

  @TempDir Path tmp;

  @BeforeEach
  void keyOn() { System.setProperty("alphavantage.api.key", "test-key"); }

  @AfterEach
  void keyOff() { System.clearProperty("alphavantage.api.key"); }

  @Test
  void daily_providerThrowsIllegalArgument_returns400JsonError() throws Exception {
    // cache path exists but is not fresh → controller will call provider
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);

    when(stocks.fetchDaily(anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("symbol is required"));

    mvc.perform(get("/market/daily"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
        .andExpect(content().string(containsString("symbol is required")));
  }
}
