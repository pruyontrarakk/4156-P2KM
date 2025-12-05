package com.example.market.api;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.analysis.AdjustedPredictionService;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompositeControllerCombinedPredictionTest {

    private StockDataService stocks;
    private ForecastDataService forecast;
    private NewsDataService news;
    private AdjustedPredictionService adjustedPrediction;
    private JsonStore store;

    private CompositeController controller;

    @BeforeEach
    void setup() {
        stocks = mock(StockDataService.class);
        forecast = mock(ForecastDataService.class);
        news = mock(NewsDataService.class);
        adjustedPrediction = mock(AdjustedPredictionService.class);
        store = mock(JsonStore.class);

        controller = new CompositeController(
                stocks, forecast, news, adjustedPrediction, store
        );
    }

    // ------------------------------------------------------------
    // 1. SUCCESS — all services return valid values
    // ------------------------------------------------------------
    @Test
    void testSuccess() throws Exception {
        String symbol = "AAPL";

        Map<String, String> prices = Map.of("DAY1", "100");
        SentimentResult sentiment = new SentimentResult("AAPL", 4, "POSITIVE");
        Map<String, String> adjusted = Map.of("DAY1", "110");

        when(forecast.predictFuturePrices(symbol, 10)).thenReturn(prices);
        when(news.analyzeSentiment(symbol)).thenReturn(sentiment);
        when(adjustedPrediction.adjustPricesWithSentiment(prices, sentiment))
                .thenReturn(adjusted);

        ResponseEntity<?> response = controller.getCombinedPrediction(symbol, 10, false);

        assertEquals(200, response.getStatusCode().value());

        Map body = (Map) response.getBody();
        assertEquals(symbol, body.get("symbol"));
        assertEquals(prices, body.get("originalPredictions"));
        assertEquals(adjusted, body.get("adjustedPredictions"));

        Map sentimentMap = (Map) body.get("sentiment");
        assertEquals(4, sentimentMap.get("score"));
        assertEquals("POSITIVE", sentimentMap.get("label"));
    }

    // ------------------------------------------------------------
    // 2. FORECAST RETURNS EMPTY → 502
    // ------------------------------------------------------------
    @Test
    void testForecastEmpty() throws Exception {
        when(forecast.predictFuturePrices("AMZN", 10))
                .thenReturn(Map.of());

        ResponseEntity<?> response =
                controller.getCombinedPrediction(null, 10, false);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("empty predictions"));
    }

    // ------------------------------------------------------------
    // 3. FORECAST THROWS → 502
    // ------------------------------------------------------------
    @Test
    void testForecastThrows() throws Exception {
        when(forecast.predictFuturePrices("AMZN", 10))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response =
                controller.getCombinedPrediction(null, 10, false);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Forecast service error"));
    }

    // ------------------------------------------------------------
    // 4. NEWS RETURNS NULL → 502
    // ------------------------------------------------------------
    @Test
    void testSentimentNull() throws Exception {
        when(forecast.predictFuturePrices("AMZN", 10))
                .thenReturn(Map.of("D1", "1"));

        when(news.analyzeSentiment("AMZN")).thenReturn(null);

        ResponseEntity<?> response =
                controller.getCombinedPrediction(null, 10, false);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("null result"));
    }

    // ------------------------------------------------------------
    // 5. NEWS THROWS → 502
    // ------------------------------------------------------------
    @Test
    void testSentimentThrows() throws Exception {
        when(forecast.predictFuturePrices("AMZN", 10))
                .thenReturn(Map.of("D1", "1"));

        when(news.analyzeSentiment("AMZN"))
                .thenThrow(new RuntimeException("sent error"));

        ResponseEntity<?> response =
                controller.getCombinedPrediction(null, 10, false);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Sentiment service error"));
    }
}

    