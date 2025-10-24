package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class newsUnitTest {

    @Test
    public void testAnalyzeSentiment() {
        NewsDataService newsService = new NewsDataService();

        // Test Amazon
        SentimentResult amazon = newsService.analyzeSentiment("Amazon");
        assertEquals(4, amazon.getSentimentScore());
        assertEquals("positive", amazon.getSentimentLabel());

        // Test Meta
        SentimentResult meta = newsService.analyzeSentiment("Meta");
        assertEquals(3, meta.getSentimentScore());
        assertEquals("neutral", meta.getSentimentLabel());

        // Test a company not listed (should be negative)
        SentimentResult tesla = newsService.analyzeSentiment("Tesla");
        assertEquals(2, tesla.getSentimentScore());
        assertEquals("negative", tesla.getSentimentLabel());
    }
}