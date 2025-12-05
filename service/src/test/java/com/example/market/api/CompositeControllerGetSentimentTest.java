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

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CompositeControllerGetSentimentTest {

    private StockDataService stocks;
    private ForecastDataService forecast;
    private NewsDataService news;
    private AdjustedPredictionService adjusted;
    private JsonStore store;

    private CompositeController controller;

    @BeforeEach
    void setup() {
        stocks = mock(StockDataService.class);
        forecast = mock(ForecastDataService.class);
        news = mock(NewsDataService.class);
        adjusted = mock(AdjustedPredictionService.class);
        store = mock(JsonStore.class);

        controller = new CompositeController(stocks, forecast, news, adjusted, store);
    }

    @Test
    void testDefaultSymbolUsedWhenNull() throws Exception {
        Path fake = Path.of("fake.json");
        when(store.newsPath("AMZN")).thenReturn(fake);

        SentimentResult sr = new SentimentResult("AMZN", 5, "POSITIVE");
        when(news.analyzeSentiment("AMZN")).thenReturn(sr);

        ResponseEntity<?> response = controller.getSentiment(null, false);

        assertEquals(200, response.getStatusCode().value());
        verify(store).write(eq(fake), any());
    }

    @Test
    void testCacheHitReturnsStoredValue() throws Exception {
        Path fake = Path.of("target", "test-cache", "fresh.json");
        Files.createDirectories(fake.getParent());
        Files.writeString(fake, "{}");

        when(store.newsPath("AAPL")).thenReturn(fake);

        Map<String,Object> cached = Map.of("cached", true);
        when(store.read(fake, Map.class)).thenReturn(cached);

        ResponseEntity<?> response = controller.getSentiment("AAPL", false);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(cached, response.getBody());

        verify(news, never()).analyzeSentiment(any());
        verify(store, never()).write(any(), any());
    }


    @Test
    void testForceSkipsCache() throws Exception {
        Path fake = Path.of("cache.json");
        when(store.newsPath("AAPL")).thenReturn(fake);

        SentimentResult sr = new SentimentResult("AAPL", 3, "NEUTRAL");
        when(news.analyzeSentiment("AAPL")).thenReturn(sr);

        ResponseEntity<?> response = controller.getSentiment("AAPL", true);

        assertEquals(200, response.getStatusCode().value());
        verify(news).analyzeSentiment("AAPL");
        verify(store).write(eq(fake), any());
    }

    @Test
    void testSentimentFetchWritesToCache() throws Exception {
        Path fake = Path.of("cache2.json");
        when(store.newsPath("NFLX")).thenReturn(fake);

        SentimentResult sr = new SentimentResult("NFLX", 4, "POSITIVE");
        when(news.analyzeSentiment("NFLX")).thenReturn(sr);

        ResponseEntity<?> response = controller.getSentiment("NFLX", false);

        assertEquals(200, response.getStatusCode().value());
        verify(store).write(eq(fake), any());
    }

    @Test
    void testSentimentThrowsReturnsBadGateway() throws Exception {
        Path fake = Path.of("tsla.json");
        when(store.newsPath("TSLA")).thenReturn(fake);

        when(news.analyzeSentiment("TSLA"))
            .thenThrow(new RuntimeException("sentiment broken"));

        ResponseEntity<?> response = controller.getSentiment("TSLA", false);

        assertEquals(502, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("sentiment broken"));
    }
}
