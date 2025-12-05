package com.example.market.service.analysis;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdjustedPredictionServiceTest {

  private final AdjustedPredictionService service = new AdjustedPredictionService();

  @Test
  void nullPredictionsReturnEmptyMap() {
    Map<String, String> result =
        service.adjustPricesWithSentiment(null, new SentimentResult("AAPL", 5, "very positive"));

    assertNotNull(result, "Result map should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for null predictions");
  }

  @Test
  void emptyPredictionsReturnEmptyMap() {
    Map<String, String> result =
        service.adjustPricesWithSentiment(Collections.emptyMap(),
            new SentimentResult("AAPL", 5, "very positive"));

    assertNotNull(result, "Result map should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for empty predictions");
  }

  @Test
  void nullSentimentReturnsOriginalValues() {
    Map<String, String> predictions = new HashMap<>();
    predictions.put(LocalDate.now().toString(), "100.00");

    Map<String, String> adjusted = service.adjustPricesWithSentiment(predictions, null);

    // should be a copy, not the same instance
    assertEquals(predictions, adjusted);
    assertNotSame(predictions, adjusted);
  }

  @Test
  void positiveSentimentIncreasesTodayPrice() {
    LocalDate today = LocalDate.now();
    Map<String, String> predictions = new LinkedHashMap<>();
    predictions.put(today.toString(), "100.00");

    SentimentResult positive =
        new SentimentResult("AAPL", 5, "very positive");

    Map<String, String> adjusted = service.adjustPricesWithSentiment(predictions, positive);
    double original = Double.parseDouble(predictions.get(today.toString()));
    double adjustedPrice = Double.parseDouble(adjusted.get(today.toString()));

    // For strongly positive sentiment, adjusted price should be at least as large
    assertTrue(adjustedPrice >= original,
        "Positive sentiment should not decrease today's price");
  }

  @Test
  void negativeSentimentDecreasesTodayPrice() {
    LocalDate today = LocalDate.now();
    Map<String, String> predictions = new LinkedHashMap<>();
    predictions.put(today.toString(), "100.00");

    SentimentResult negative =
        new SentimentResult("AAPL", 1, "very negative");

    Map<String, String> adjusted = service.adjustPricesWithSentiment(predictions, negative);
    double original = Double.parseDouble(predictions.get(today.toString()));
    double adjustedPrice = Double.parseDouble(adjusted.get(today.toString()));

    // For strongly negative sentiment, adjusted price should be at most the original
    assertTrue(adjustedPrice <= original,
        "Negative sentiment should not increase today's price");
  }

  @Test
  void invalidPriceDoesNotThrow() {
    LocalDate today = LocalDate.now();
    Map<String, String> predictions = new HashMap<>();
    predictions.put(today.toString(), "NOT_A_NUMBER");

    SentimentResult sentiment =
        new SentimentResult("AAPL", 4, "positive");

    // Main thing: this should not crash even if one entry is malformed
    assertDoesNotThrow(() ->
        service.adjustPricesWithSentiment(predictions, sentiment));
  }
}
