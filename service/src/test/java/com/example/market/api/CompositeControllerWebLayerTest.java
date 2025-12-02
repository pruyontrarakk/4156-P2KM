package com.example.market.api;

import com.example.market.model.stock.StockBar;
import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.analysis.AdjustedPredictionService;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompositeController.class)
class CompositeControllerWebLayerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @MockBean StockDataService stocks;
  @MockBean ForecastDataService forecast;
  @MockBean NewsDataService news;
  @MockBean AdjustedPredictionService adjustedPrediction;
  @MockBean JsonStore store;

  @TempDir Path tmp;

  @BeforeEach
  void setUp() {
    // let getDailySeries() pass the API-key check when not using cache
    System.setProperty("alphavantage.api.key", "test-key");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("alphavantage.api.key");
  }

  private StockDailySeries sampleSeries() {
    StockBar bar = new StockBar(
        "2025-10-22",
        new BigDecimal("100.00"),
        new BigDecimal("110.00"),
        new BigDecimal("95.00"),
        new BigDecimal("105.00"),
        123456L
    );
    return new StockDailySeries("AMZN", Instant.now().toString(), "mock-source", List.of(bar));
  }

  @Test
  void daily_forceFetch_fetchesAndCaches_then200() throws Exception {
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);

    StockDailySeries fresh = sampleSeries();
    when(stocks.fetchDaily(eq("AMZN"), anyString())).thenReturn(fresh);

    // exercise force=true path (skips cache freshness check)
    mvc.perform(get("/market/daily").param("force", "true"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.symbol").value("AMZN"))
        .andExpect(jsonPath("$.source").value("mock-source"));

    verify(stocks).fetchDaily(eq("AMZN"), anyString());
    verify(store).write(eq(cache), eq(fresh));
  }

  @Test
  void daily_cacheHit_skipsUpstreamFetch_then200() throws Exception {
    // create a fresh cache file the controller's isFresh() will see
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{}",
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    Files.setLastModifiedTime(cache, FileTime.from(Instant.now()));

    when(store.dailyPath("AMZN")).thenReturn(cache);

    StockDailySeries cached = sampleSeries();
    when(store.read(cache, StockDailySeries.class)).thenReturn(cached);

    // default force=false → cache-first path
    mvc.perform(get("/market/daily"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.symbol").value("AMZN"))
        .andExpect(jsonPath("$.source").value("mock-source"));

    // ensure we never hit the provider because cache is fresh
    verify(stocks, never()).fetchDaily(anyString(), anyString());
    verify(store, never()).write(any(), any());
  }

  @Test
  void sentiment_cacheMiss_writesAndReturnsPayload_then200() throws Exception {
    Path newsCache = tmp.resolve("data/news/amzn.json");
    when(store.newsPath("AMZN")).thenReturn(newsCache);

    // Create no file → isFresh() will be false; controller calls news service and store.write
    var result = new com.example.market.model.news.SentimentResult("AMZN", 4, "positive");
    when(news.analyzeSentiment("AMZN")).thenReturn(result);

    mvc.perform(get("/market/sentiment"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.company").value("AMZN"))
        .andExpect(jsonPath("$.sentimentLabel").value("positive"))
        .andExpect(jsonPath("$.source").value("HuggingFaceModel"));

    verify(store).write(eq(newsCache), Mockito.<Map<String, Object>>argThat(m ->
        "AMZN".equals(m.get("company")) && "positive".equals(m.get("sentimentLabel"))));
  }

  @Test
  void predict_usesForecastAndDailySeries_then200() throws Exception {
    // ensure daily path exists but we’ll force fetch to also cover provider branch
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);

    StockDailySeries fresh = new StockDailySeries("AMZN", Instant.now().toString(),
        "mock-source", List.of());
    when(stocks.fetchDaily(eq("AMZN"), anyString())).thenReturn(fresh);

    Map<String, String> forecastMap = Map.of("2025-10-24", "106.50");
    when(forecast.predictFuturePrices("AMZN")).thenReturn(forecastMap);

    mvc.perform(get("/market/predict").param("force", "true"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.symbol").value("AMZN"))
        .andExpect(jsonPath("$.horizon").value("next-day"))
        .andExpect(jsonPath("$.prediction.2025-10-24").value("106.50"))
        .andExpect(jsonPath("$.source").value("mock-source"));

    verify(store).write(eq(cache), eq(fresh));
    verify(forecast).predictFuturePrices("AMZN");
  }
}
