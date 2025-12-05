package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

public class newsUnitTest {

    @Test
    public void testAnalyzeSentiment_withMockedPythonService() throws Exception {

        // Mock Python service
        SentimentPythonService mockPythonService = mock(SentimentPythonService.class);

        // Mock News API client
        NewsApiClient mockNewsApiClient = mock(NewsApiClient.class);

        // Mock company lookup client
        CompanyLookupClient mockLookupClient = mock(CompanyLookupClient.class);

        // Symbol → Company name mapping
        when(mockLookupClient.lookupCompanyName("AMZN"))
                .thenReturn("Amazon Inc.");
        when(mockLookupClient.lookupCompanyName("META"))
                .thenReturn("Meta Platforms Inc.");
        when(mockLookupClient.lookupCompanyName("TSLA"))
                .thenReturn("Tesla Inc.");


        when(mockNewsApiClient.fetchNews(contains("Amazon")))
                .thenReturn(Map.of(
                        "articles", java.util.List.of(
                                Map.of("title", "Amazon reports strong growth",
                                       "description", "Amazon stock surges")
                        )
                ));

        when(mockNewsApiClient.fetchNews(contains("Meta")))
                .thenReturn(Map.of(
                        "articles", java.util.List.of(
                                Map.of("title", "Meta releases new features",
                                       "description", "Mixed reactions")
                        )
                ));

        when(mockNewsApiClient.fetchNews(contains("Tesla")))
                .thenReturn(Map.of(
                        "articles", java.util.List.of(
                                Map.of("title", "Tesla faces autopilot concerns",
                                       "description", "Investors worried")
                        )
                ));

        when(mockPythonService.analyzeSentiment(anyString()))
                .thenAnswer(invocation -> {
                    String text = invocation.getArgument(0).toString().toLowerCase();

                    if (text.contains("amazon"))
                        return new SentimentResult("AMZN", 4, "positive");

                    if (text.contains("meta"))
                        return new SentimentResult("META", 3, "neutral");

                    if (text.contains("tesla"))
                        return new SentimentResult("TSLA", 2, "negative");

                    return new SentimentResult("UNKNOWN", 3, "neutral");
                });


        NewsDataService newsService =
                new NewsDataService(mockPythonService, mockNewsApiClient, mockLookupClient);


       // Amazon
        SentimentResult amazon = newsService.analyzeSentiment("AMZN");
        assertEquals(4, amazon.getSentimentScore());
        assertEquals("positive", amazon.getSentimentLabel());
        verify(mockLookupClient).lookupCompanyName("AMZN");

        // Meta
        SentimentResult meta = newsService.analyzeSentiment("META");
        assertEquals(3, meta.getSentimentScore());
        assertEquals("neutral", meta.getSentimentLabel());
        verify(mockLookupClient).lookupCompanyName("META");

        // Tesla
        SentimentResult tesla = newsService.analyzeSentiment("TSLA");
        assertEquals(2, tesla.getSentimentScore());
        assertEquals("negative", tesla.getSentimentLabel());
        verify(mockLookupClient).lookupCompanyName("TSLA");

        // Ensure Python service was used
        verify(mockPythonService, atLeastOnce()).analyzeSentiment(anyString());
    }
}