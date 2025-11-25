package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import java.util.Random;

// @Service
public class MediastackService {

    /** Given labels to categorize the sentiment of a company. */
    private static final String[] LABELS = {
        "very negative", "negative", "neutral", "positive", "very positive"
    };
    /** Upper bound for sentiment rating. */
    private static final int RATING_UPPER_BOUND = 5;

    /**
     * Computes the sentiment score for a given company name.
     *
     * @param companyName the name of the company for
     *                    which sentiment is being requested;
     * @return a {@link SentimentResult} containing the company name,
     *                   a generated sentiment score (1–5),
     *                   and the associated sentiment label
     */
      public SentimentResult getSentiment(final String companyName) {
            // For Iteration 1, return a random sentiment for testing

            Random random = new Random();
            int score = random.nextInt(RATING_UPPER_BOUND) + 1;  // 1–5 scale
            String label = LABELS[score - 1];

            return new SentimentResult(companyName, score, label);
      }
}
