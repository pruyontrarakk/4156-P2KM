package com.example.market.api;

import com.example.market.model.stock.StockBar;
import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompositeController.class)
class CompositeControllerErrorPathsTest {

  @Autowired MockMvc mvc;

  @MockBean StockDataService stocks;
  @MockBean ForecastDataService forecast;
  @MockBean NewsDataService news;
  @MockBean JsonStore store;

  @TempDir Path tmp;

  private static StockDailySeries series(String source) {
    var bar = new StockBar(
        "2025-10-22",
        new BigDecimal("100.00"),
        new BigDecimal("110.00"),
        new BigDecimal("95.00"),
        new BigDecimal("105.00"),
        123L
    );
    return new StockDailySeries("AMZN", Instant.now().toString(), source, List.of(bar));
  }

  @BeforeEach
  void clearKey() {
    System.clearProperty("alphavantage.api.key");
  }

  @Test
  void daily_forceFetch_withoutApiKey_returns502JsonError() throws Exception {
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);

    mvc.perform(get("/market/daily").param("force", "true"))
        .andExpect(status().isBadGateway())
        // error body is a String => text/plain
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
        .andExpect(content().string(containsString("missing ALPHAVANTAGE_API_KEY")));

    verify(stocks, never()).fetchDaily(anyString(), anyString());
    verify(store, never()).write(any(), any());
  }

  @Test
  void predict_whenForecastThrows_returns502_and_skipsWrite() throws Exception {
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    Files.setLastModifiedTime(cache, FileTime.from(Instant.now()));
    when(store.dailyPath("AMZN")).thenReturn(cache);
    when(store.read(cache, StockDailySeries.class)).thenReturn(series("cached"));

    when(forecast.predictFuturePrices("AMZN")).thenThrow(new RuntimeException("boom"));

    mvc.perform(get("/market/predict"))
        .andExpect(status().isBadGateway())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
        .andExpect(content().string(containsString("boom")));

    verify(stocks, never()).fetchDaily(anyString(), anyString());
    verify(store, never()).write(any(), any());
  }

  @Test
  void sentiment_cacheHit_returnsCachedPayload_and_skipsAnalyze() throws Exception {
    Path newsCache = tmp.resolve("data/news/amzn.json");
    Files.createDirectories(newsCache.getParent());
    Files.writeString(newsCache, "{}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    Files.setLastModifiedTime(newsCache, FileTime.from(Instant.now()));
    when(store.newsPath("AMZN")).thenReturn(newsCache);

    Map<String, Object> cached = Map.of(
        "company", "AMZN",
        "sentimentScore", 4,
        "sentimentLabel", "positive",
        "source", "news-placeholder"
    );
    when(store.read(newsCache, Map.class)).thenReturn(cached);

    mvc.perform(get("/market/sentiment"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.company").value("AMZN"))
        .andExpect(jsonPath("$.sentimentLabel").value("positive"))
        .andExpect(jsonPath("$.source").value("news-placeholder"));

    verify(news, never()).analyzeSentiment(anyString());
    verify(store, never()).write(any(), any());
  }
}
