package com.example.market.service.analysis;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdjustedPredictionServiceTest {

  private AdjustedPredictionService service;
  private DateTimeFormatter formatter;

  @BeforeEach
  void setUp() {
    service = new AdjustedPredictionService();
    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  @Test
  void adjustPricesWithSentiment_nullPredictions_returnsEmptyMap() {
    SentimentResult sentiment = new SentimentResult("AMZN", 3, "neutral");
    Map<String, String> result = service.adjustPricesWithSentiment(null, sentiment);
    assertTrue(result.isEmpty());
  }

  @Test
  void adjustPricesWithSentiment_emptyPredictions_returnsEmptyMap() {
    SentimentResult sentiment = new SentimentResult("AMZN", 3, "neutral");
    Map<String, String> empty = new HashMap<>();
    Map<String, String> result = service.adjustPricesWithSentiment(empty, sentiment);
    assertTrue(result.isEmpty());
  }

  @Test
  void adjustPricesWithSentiment_nullSentiment_returnsOriginalPredictions() {
    Map<String, String> predictions = Map.of("2025-12-02", "100.00");
    Map<String, String> result = service.adjustPricesWithSentiment(predictions, null);
    assertEquals(predictions, result);
  }

  @Test
  void adjustPricesWithSentiment_neutralSentiment_noAdjustment() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");
    SentimentResult neutral = new SentimentResult("AMZN", 3, "neutral");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, neutral);

    assertEquals(1, result.size());
    assertEquals("100.00", result.get(dateStr));
  }

  @Test
  void adjustPricesWithSentiment_veryPositiveSentiment_increasesPrice() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");
    SentimentResult positive = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    assertEquals(1, result.size());
    String adjustedPrice = result.get(dateStr);
    assertNotNull(adjustedPrice);
    double adjusted = Double.parseDouble(adjustedPrice);
    assertTrue(adjusted > 100.00, "Positive sentiment should increase price");
  }

  @Test
  void adjustPricesWithSentiment_veryNegativeSentiment_decreasesPrice() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");
    SentimentResult negative = new SentimentResult("AMZN", 1, "very negative");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, negative);

    assertEquals(1, result.size());
    String adjustedPrice = result.get(dateStr);
    assertNotNull(adjustedPrice);
    double adjusted = Double.parseDouble(adjustedPrice);
    assertTrue(adjusted < 100.00, "Negative sentiment should decrease price");
  }

  @Test
  void adjustPricesWithSentiment_timeDecay_furtherDatesHaveLessImpact() {
    LocalDate today = LocalDate.now();
    String date1 = today.plusDays(1).format(formatter);
    String date2 = today.plusDays(5).format(formatter);
    String date3 = today.plusDays(10).format(formatter);

    Map<String, String> predictions = Map.of(
        date1, "100.00",
        date2, "100.00",
        date3, "100.00"
    );
    SentimentResult positive = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    double adjusted1 = Double.parseDouble(result.get(date1));
    double adjusted2 = Double.parseDouble(result.get(date2));
    double adjusted3 = Double.parseDouble(result.get(date3));

    // All should be increased, but further dates should have less impact
    assertTrue(adjusted1 > 100.00);
    assertTrue(adjusted2 > 100.00);
    assertTrue(adjusted3 > 100.00);
    assertTrue(adjusted1 > adjusted2, "Day 1 should have more impact than day 5");
    assertTrue(adjusted2 > adjusted3, "Day 5 should have more impact than day 10");
  }

  @Test
  void adjustPricesWithSentiment_pastDate_usesZeroDayIndex() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String dateStr = yesterday.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");
    SentimentResult positive = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    // Past dates should be treated as day 0 (maximum impact)
    String adjustedPrice = result.get(dateStr);
    double adjusted = Double.parseDouble(adjustedPrice);
    assertTrue(adjusted > 100.00);
  }

  @Test
  void adjustPricesWithSentiment_invalidDate_keepsOriginalValue() {
    Map<String, String> predictions = Map.of("invalid-date", "100.00");
    SentimentResult sentiment = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, sentiment);

    // Should keep original value when date parsing fails
    assertEquals("100.00", result.get("invalid-date"));
  }

  @Test
  void adjustPricesWithSentiment_invalidPrice_keepsOriginalValue() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "not-a-number");
    SentimentResult sentiment = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, sentiment);

    // Should keep original value when price parsing fails
    assertEquals("not-a-number", result.get(dateStr));
  }

  @Test
  void adjustPricesWithSentiment_mixedValidInvalid_handlesBoth() {
    LocalDate today = LocalDate.now();
    String validDate = today.plusDays(1).format(formatter);
    Map<String, String> predictions = Map.of(
        validDate, "100.00",
        "invalid-date", "50.00"
    );
    SentimentResult positive = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    assertEquals(2, result.size());
    // Valid date should be adjusted
    double adjusted = Double.parseDouble(result.get(validDate));
    assertTrue(adjusted > 100.00);
    // Invalid date should keep original
    assertEquals("50.00", result.get("invalid-date"));
  }

  @Test
  void adjustPricesWithSentiment_sentimentScoreOutOfRange_clamped() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");

    // Test score below range (should be clamped to 1)
    SentimentResult tooLow = new SentimentResult("AMZN", 0, "invalid");
    Map<String, String> resultLow = service.adjustPricesWithSentiment(
        predictions, tooLow);
    double adjustedLow = Double.parseDouble(resultLow.get(dateStr));
    assertTrue(adjustedLow < 100.00);

    // Test score above range (should be clamped to 5)
    SentimentResult tooHigh = new SentimentResult("AMZN", 10, "invalid");
    Map<String, String> resultHigh = service.adjustPricesWithSentiment(
        predictions, tooHigh);
    double adjustedHigh = Double.parseDouble(resultHigh.get(dateStr));
    assertTrue(adjustedHigh > 100.00);
  }

  @Test
  void adjustPricesWithSentiment_exactSentimentValues_allBoundaries() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");

    // Test all exact sentiment scores
    for (int score = 1; score <= 5; score++) {
      SentimentResult sentiment = new SentimentResult("AMZN", score, "test");
      Map<String, String> result = service.adjustPricesWithSentiment(
          predictions, sentiment);
      double adjusted = Double.parseDouble(result.get(dateStr));

      if (score == 3) {
        assertEquals(100.00, adjusted, 0.01, "Score 3 should be neutral");
      } else if (score < 3) {
        assertTrue(adjusted < 100.00, "Score " + score + " should decrease");
      } else {
        assertTrue(adjusted > 100.00, "Score " + score + " should increase");
      }
    }
  }

  @Test
  void adjustPricesWithSentiment_formatsToTwoDecimalPlaces() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    String dateStr = tomorrow.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.123456");
    SentimentResult positive = new SentimentResult("AMZN", 4, "positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    String adjustedPrice = result.get(dateStr);
    // Should be formatted to 2 decimal places
    assertTrue(adjustedPrice.matches("\\d+\\.\\d{2}"));
  }

  @Test
  void adjustPricesWithSentiment_sameDay_usesZeroDayIndex() {
    LocalDate today = LocalDate.now();
    String dateStr = today.format(formatter);
    Map<String, String> predictions = Map.of(dateStr, "100.00");
    SentimentResult positive = new SentimentResult("AMZN", 5, "very positive");

    Map<String, String> result = service.adjustPricesWithSentiment(
        predictions, positive);

    String adjustedPrice = result.get(dateStr);
    double adjusted = Double.parseDouble(adjustedPrice);
    assertTrue(adjusted > 100.00);
  }
}
