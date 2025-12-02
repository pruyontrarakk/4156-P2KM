package com.example.market.service.analysis;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service that adjusts stock price predictions based on news sentiment analysis
 * to produce sentiment-adjusted price forecasts.
 */
@Service
public class AdjustedPredictionService {

  /** Base sentiment adjustment strength (0.0 to 1.0). */
  private static final double BASE_SENTIMENT_STRENGTH = 0.15;

  /** Time decay factor - how quickly sentiment impact diminishes. */
  private static final double TIME_DECAY_RATE = 0.12;

  /** Neutral sentiment score (no adjustment). */
  private static final int NEUTRAL_SENTIMENT = 3;

  /**
   * Adjusts stock price predictions based on news sentiment analysis.
   * Uses a sophisticated non-linear formula with time decay to model how
   * sentiment impacts future prices differently over time.
   *
   * @param stockPricePredictions a {@code Map} where keys are dates
   *                              and values are predicted closing prices
   * @param newsSentimentPrediction a {@code SentimentResult} containing
   *                                sentiment score (1-5) and metadata
   * @return a {@code Map} with the same date keys but sentiment-adjusted prices
   *         (as strings)
   */
  public Map<String, String> adjustPricesWithSentiment(
          final Map<String, String> stockPricePredictions,
          final SentimentResult newsSentimentPrediction) {

    if (stockPricePredictions == null || stockPricePredictions.isEmpty()) {
      return new HashMap<>();
    }

    if (newsSentimentPrediction == null) {
      return new HashMap<>(stockPricePredictions);
    }

    final int sentimentScore = newsSentimentPrediction.getSentimentScore();

    // Normalize sentiment to [-1, 1] range (-1 neg, 1 pos)
    final double normalizedSentiment = normalizeSentiment(sentimentScore);

    // Lambda for sentiment adjustment factor (non-linear, exponential)
    final Function<Double, Double> sentimentAdjustmentFactor =
        (sentiment) -> BASE_SENTIMENT_STRENGTH * Math.tanh(2.0 * sentiment);

    // Lambda function for time decay for a given day index
    final Function<Integer, Double> timeDecayFactor =
        (dayIndex) -> Math.exp(-TIME_DECAY_RATE * dayIndex);

    // Calculate base adjustment multiplier from sentiment
    final double baseAdjustment =
        sentimentAdjustmentFactor.apply(normalizedSentiment);

    final Map<String, String> adjustedPredictions = new HashMap<>();
    final LocalDate today = LocalDate.now();
    final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    int dayIndex = 0;
    for (Map.Entry<String, String> entry : stockPricePredictions.entrySet()) {
      try {
        final String dateStr = entry.getKey();
        final String priceStr = entry.getValue();

        // Parse the date to calculate days from today
        final LocalDate predictionDate = LocalDate.parse(dateStr, formatter);
        final long daysFromToday = java.time.temporal.ChronoUnit.DAYS
                .between(today, predictionDate);
        dayIndex = (int) Math.max(0, daysFromToday);

        // Parse original price
        final double originalPrice = Double.parseDouble(priceStr);

        // Calculate time-decayed adjustment factor
        final double decayFactor = timeDecayFactor.apply(dayIndex);
        final double effectiveAdjustment = baseAdjustment * decayFactor;

        // Apply adjustment: positive sentiment increases price,
        // negative decreases. Uses multiplicative adjustment.
        final double adjustedPrice =
            originalPrice * (1.0 + effectiveAdjustment);

        // Format adjusted price to 2 decimal places
        adjustedPredictions.put(dateStr,
            String.format("%.2f", adjustedPrice));

      } catch (Exception e) {
        // If parsing fails, keep original value
        adjustedPredictions.put(entry.getKey(), entry.getValue());
      }
    }

    return adjustedPredictions;
  }

  /**
   * @param sentimentScore the raw sentiment score (1-5)
   * @return normalized sentiment in [-1, 1] range
   */
  private double normalizeSentiment(final int sentimentScore) {
    // Clamp to valid range
    final int clamped = Math.max(1, Math.min(5, sentimentScore));
    // Linear mapping: 1 -> -1, 3 -> 0, 5 -> 1
    return (clamped - NEUTRAL_SENTIMENT) / 2.0;
  }
}

