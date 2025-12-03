package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class newsUnitTest {

    @Test
    public void testAnalyzeSentiment_withMockedPythonService() throws Exception {
        // Create a mock of the Python service
        SentimentPythonService mockPythonService = mock(SentimentPythonService.class);

        // Create a mock of the NewsApiClient (unused in this test, but required for constructor)
        NewsApiClient mockNewsApiClient = mock(NewsApiClient.class);

        // Define mock responses
        when(mockPythonService.analyzeSentiment("Amazon"))
                .thenReturn(new SentimentResult("Amazon", 4, "positive"));

        when(mockPythonService.analyzeSentiment("Meta"))
                .thenReturn(new SentimentResult("Meta", 3, "neutral"));

        when(mockPythonService.analyzeSentiment("Tesla"))
                .thenReturn(new SentimentResult("Tesla", 2, "negative"));

        // Inject mocks into the NewsDataService
        NewsDataService newsService = new NewsDataService(mockPythonService, mockNewsApiClient);

        // Verify results for Amazon
        SentimentResult amazon = newsService.analyzeSentiment("Amazon");
        assertEquals("Amazon", amazon.getCompany());
        assertEquals(4, amazon.getSentimentScore());
        assertEquals("positive", amazon.getSentimentLabel());

        // Verify results for Meta
        SentimentResult meta = newsService.analyzeSentiment("Meta");
        assertEquals("Meta", meta.getCompany());
        assertEquals(3, meta.getSentimentScore());
        assertEquals("neutral", meta.getSentimentLabel());

        // Verify results for Tesla
        SentimentResult tesla = newsService.analyzeSentiment("Tesla");
        assertEquals("Tesla", tesla.getCompany());
        assertEquals(2, tesla.getSentimentScore());
        assertEquals("negative", tesla.getSentimentLabel());

        // Confirm that the mock was called exactly once per company
        verify(mockPythonService, times(1)).analyzeSentiment("Amazon");
        verify(mockPythonService, times(1)).analyzeSentiment("Meta");
        verify(mockPythonService, times(1)).analyzeSentiment("Tesla");

        // Confirm NewsApiClient was never used
        verifyNoInteractions(mockNewsApiClient);
    }
}