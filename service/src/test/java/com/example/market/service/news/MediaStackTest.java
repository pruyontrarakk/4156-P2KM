package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaStackTest {

  public static MediastackService mediastackService = new MediastackService();

  @Test
  public void testRatingWithinBounds() {
    SentimentResult result = mediastackService.getSentiment("Meta");
    assertTrue(result.getSentimentScore() <= 5, "rating should be less than or equal to 5");
  }
}
