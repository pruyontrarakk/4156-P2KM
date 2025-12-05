package com.example.market.api;

import com.example.market.model.stock.StockBar;
import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.analysis.AdjustedPredictionService;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompositeController.class)
class CompositeControllerHelperMethodsTest {

  @Autowired MockMvc mvc;

  @MockBean StockDataService stocks;
  @MockBean ForecastDataService forecast;
  @MockBean NewsDataService news;
  @MockBean AdjustedPredictionService adjustedPrediction;
  @MockBean JsonStore store;

  @TempDir Path tmp;

  private CompositeController controller;

  @BeforeEach
  void setUp() {
    controller = new CompositeController(
        stocks, forecast, news, adjustedPrediction, store
    );
    System.setProperty("alphavantage.api.key", "test-key");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("alphavantage.api.key");
  }

  // ========== resolveSymbol tests ==========
  @Test
  void resolveSymbol_null_returnsDefault() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("resolveSymbol", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(controller, (String) null);
    assertEquals("AMZN", result);
  }

  @Test
  void resolveSymbol_blank_returnsDefault() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("resolveSymbol", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(controller, "   ");
    assertEquals("AMZN", result);
  }

  @Test
  void resolveSymbol_empty_returnsDefault() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("resolveSymbol", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(controller, "");
    assertEquals("AMZN", result);
  }

  @Test
  void resolveSymbol_validSymbol_returnsUppercase() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("resolveSymbol", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(controller, "aapl");
    assertEquals("AAPL", result);
  }

  // ========== isFresh tests ==========
  @Test
  void isFresh_fileNotExists_returnsFalse() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("isFresh", Path.class, Duration.class);
    m.setAccessible(true);
    Path nonExistent = tmp.resolve("nonexistent.json");
    Boolean result = (Boolean) m.invoke(null, nonExistent, Duration.ofDays(1));
    assertFalse(result);
  }

  @Test
  void isFresh_staleFile_returnsFalse() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("isFresh", Path.class, Duration.class);
    m.setAccessible(true);
    Path staleFile = tmp.resolve("stale.json");
    Files.createFile(staleFile);
    // Set modification time to 2 days ago
    Files.setLastModifiedTime(staleFile, 
        FileTime.from(Instant.now().minus(Duration.ofDays(2))));
    Boolean result = (Boolean) m.invoke(null, staleFile, Duration.ofDays(1));
    assertFalse(result);
  }

  @Test
  void isFresh_freshFile_returnsTrue() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("isFresh", Path.class, Duration.class);
    m.setAccessible(true);
    Path freshFile = tmp.resolve("fresh.json");
    Files.createFile(freshFile);
    Files.setLastModifiedTime(freshFile, FileTime.from(Instant.now()));
    Boolean result = (Boolean) m.invoke(null, freshFile, Duration.ofDays(1));
    assertTrue(result);
  }

  // ========== jsonError tests ==========
  @Test
  void jsonError_nullMessage_returnsUnknownError() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, (String) null);
    assertEquals("{\"error\":\"Unknown error\"}", result);
  }

  @Test
  void jsonError_withBackslash_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "path\\to\\file");
    assertTrue(result.contains("\\\\"));
    assertFalse(result.contains("\\\""));
  }

  @Test
  void jsonError_withQuotes_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "Error: \"something went wrong\"");
    assertTrue(result.contains("\\\""));
  }

  @Test
  void jsonError_withNewline_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "Line1\nLine2");
    assertTrue(result.contains("\\n"));
    assertFalse(result.contains("\n"));
  }

  @Test
  void jsonError_withCarriageReturn_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "Line1\rLine2");
    assertTrue(result.contains("\\r"));
  }

  @Test
  void jsonError_withTab_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "Field1\tField2");
    assertTrue(result.contains("\\t"));
  }

  @Test
  void jsonError_allSpecialChars_escapesCorrectly() throws Exception {
    Method m = CompositeController.class
        .getDeclaredMethod("jsonError", String.class);
    m.setAccessible(true);
    String result = (String) m.invoke(null, "Test\"with\\all\nspecial\rchars\there");
    assertTrue(result.contains("\\\""));
    assertTrue(result.contains("\\\\"));
    assertTrue(result.contains("\\n"));
    assertTrue(result.contains("\\r"));
    assertTrue(result.contains("\\t"));
  }

  // ========== getDailySeries API key tests ==========
  @Test
  void getDailySeries_missingApiKey_throwsIllegalState() throws Exception {
    System.clearProperty("alphavantage.api.key");
    // Clear environment variable by using a new controller instance
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);
    
    // Create stale cache to force API call
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{}");
    Files.setLastModifiedTime(cache, 
        FileTime.from(Instant.now().minus(Duration.ofDays(2))));

    Exception ex = assertThrows(Exception.class,
        () -> controller.getDailySeries("AMZN", false));
    assertTrue(ex instanceof IllegalStateException);
    assertTrue(ex.getMessage().contains("missing ALPHAVANTAGE_API_KEY"));
  }

  @Test
  void getDailySeries_systemPropertyFallback_works() throws Exception {
    System.clearProperty("alphavantage.api.key");
    System.setProperty("alphavantage.api.key", "fallback-key");
    
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);
    
    // Create stale cache to force API call
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{}");
    Files.setLastModifiedTime(cache, 
        FileTime.from(Instant.now().minus(Duration.ofDays(2))));

    StockDailySeries series = new StockDailySeries("AMZN", 
        Instant.now().toString(), "test", List.of());
    when(stocks.fetchDaily("AMZN", "fallback-key")).thenReturn(series);

    StockDailySeries result = controller.getDailySeries("AMZN", false);
    assertEquals("AMZN", result.getSymbol());
    verify(stocks).fetchDaily("AMZN", "fallback-key");
  }

  // ========== getSentiment custom symbol tests ==========
  @Test
  void sentiment_customSymbol_usesProvidedSymbol() throws Exception {
    Path newsCache = tmp.resolve("data/news/aapl.json");
    when(store.newsPath("AAPL")).thenReturn(newsCache);

    var result = new com.example.market.model.news.SentimentResult("AAPL", 3, "neutral");
    when(news.analyzeSentiment("AAPL")).thenReturn(result);

    mvc.perform(get("/market/sentiment").param("symbol", "AAPL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company").value("AAPL"))
        .andExpect(jsonPath("$.symbol").value("AAPL"));

    verify(news).analyzeSentiment("AAPL");
  }

  @Test
  void sentiment_exceptionWithNullMessage_handlesGracefully() throws Exception {
    Path newsCache = tmp.resolve("data/news/amzn.json");
    when(store.newsPath("AMZN")).thenReturn(newsCache);

    when(news.analyzeSentiment("AMZN"))
        .thenThrow(new RuntimeException((String) null));

    mvc.perform(get("/market/sentiment"))
        .andExpect(status().isBadGateway())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
  }

  // ========== getCombinedPrediction edge cases ==========
  @Test
  void combinedPrediction_illegalArgumentException_returns502() throws Exception {
    when(store.dailyPath("AMZN")).thenReturn(tmp.resolve("data/stocks/amzn-daily.json"));
    when(forecast.predictFuturePrices("AMZN", 10))
        .thenThrow(new IllegalArgumentException("Invalid symbol"));

    mvc.perform(get("/market/combined-prediction"))
        .andExpect(status().isBadGateway())
        .andExpect(content().string(containsString("Forecast service error")));
  }

  @Test
  void combinedPrediction_exceptionWithNullMessage_handlesGracefully() throws Exception {
    when(store.dailyPath("AMZN")).thenReturn(tmp.resolve("data/stocks/amzn-daily.json"));
    when(forecast.predictFuturePrices("AMZN", 10))
        .thenThrow(new RuntimeException((String) null));

    mvc.perform(get("/market/combined-prediction"))
        .andExpect(status().isBadGateway());
  }

  // ========== getDaily edge cases ==========
  @Test
  void daily_exceptionWithNullMessage_handlesGracefully() throws Exception {
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    when(store.dailyPath("AMZN")).thenReturn(cache);
    when(stocks.fetchDaily(anyString(), anyString()))
        .thenThrow(new RuntimeException((String) null));

    mvc.perform(get("/market/daily").param("force", "true"))
        .andExpect(status().isBadGateway());
  }

  // ========== predict edge cases ==========
  @Test
  void predict_exceptionWithNullMessage_handlesGracefully() throws Exception {
    Path cache = tmp.resolve("data/stocks/amzn-daily.json");
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{}");
    Files.setLastModifiedTime(cache, FileTime.from(Instant.now()));
    when(store.dailyPath("AMZN")).thenReturn(cache);

    StockDailySeries series = new StockDailySeries("AMZN", 
        Instant.now().toString(), "test", List.of());
    when(store.read(cache, StockDailySeries.class)).thenReturn(series);
    when(forecast.predictFuturePrices("AMZN", 10))
        .thenThrow(new RuntimeException((String) null));

    mvc.perform(get("/market/predict"))
        .andExpect(status().isBadGateway());
  }
}

