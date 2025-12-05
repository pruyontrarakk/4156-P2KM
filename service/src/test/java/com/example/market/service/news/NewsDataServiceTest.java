package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.python.ProcessRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NewsDataService}.
 * These tests exercise the branching logic around:
 *  - company lookup success / failure
 *  - no articles vs some articles
 *  - delegating to SentimentPythonService.
 */
class NewsDataServiceTest {

    /**
     * Stub sentiment service that never spawns a real Python process.
     */
    static class StubSentimentPythonService extends SentimentPythonService {
        private boolean called = false;
        private String lastText;
        private final SentimentResult toReturn;

        StubSentimentPythonService(SentimentResult toReturn) {
            super(new ProcessRunner() {
                @Override
                public Process start(ProcessBuilder builder) throws IOException {
                    // Should never be called in these unit tests.
                    throw new IOException("ProcessRunner should not be used in tests");
                }
            });
            this.toReturn = toReturn;
        }

        @Override
        public SentimentResult analyzeSentiment(String newsText) {
            called = true;
            lastText = newsText;
            return toReturn;
        }

        boolean wasCalled() {
            return called;
        }

        String getLastText() {
            return lastText;
        }
    }

    /**
     * Stub news API client that returns a pre-configured response.
     */
    static class StubNewsApiClient extends NewsApiClient {
        private final Map<String, Object> response;
        private String lastQuery;

        StubNewsApiClient(Map<String, Object> response) {
            super();
            this.response = response;
        }

        @Override
        public Map<String, Object> fetchNews(String query) {
            lastQuery = query;
            return response;
        }

        String getLastQuery() {
            return lastQuery;
        }
    }

    /**
     * Stub lookup client that returns a fixed company name for any symbol.
     */
    static class StubCompanyLookupClient extends CompanyLookupClient {
        private final String companyNameToReturn;

        StubCompanyLookupClient(String companyNameToReturn) {
            super();
            this.companyNameToReturn = companyNameToReturn;
        }

        @Override
        public String lookupCompanyName(String symbol) {
            return companyNameToReturn;
        }
    }

    @Test
    void analyzeSentiment_usesLookupAndNewsAndDelegatesToPython() throws Exception {
        // Arrange: company lookup returns a name with "Inc." that should be stripped.
        StubCompanyLookupClient lookupClient = new StubCompanyLookupClient("Apple Inc.");

        Map<String, Object> article = new HashMap<>();
        article.put("title", "Great earnings for Apple");
        article.put("description", "Investors are very optimistic.");
        Map<String, Object> response = new HashMap<>();
        response.put("articles", List.of(article));

        StubNewsApiClient newsClient = new StubNewsApiClient(response);

        SentimentResult pythonResult =
                new SentimentResult("AAPL", 5, "very positive");
        StubSentimentPythonService sentimentService =
                new StubSentimentPythonService(pythonResult);

        // NOTE: constructor order = (sentimentService, newsClient, lookupClient)
        NewsDataService service =
                new NewsDataService(sentimentService, newsClient, lookupClient);

        // Act
        SentimentResult result = service.analyzeSentiment("AAPL");

        // Assert: NewsDataService preserves the stock symbol as the "company" in the result.
        assertEquals("AAPL", result.getCompany());
        assertEquals(5, result.getSentimentScore());
        assertEquals("very positive", result.getSentimentLabel());

        // We should have queried news using "Apple", not "Apple Inc."
        assertEquals("Apple", newsClient.getLastQuery());

        // And the sentiment service should have been called with article text.
        assertTrue(sentimentService.wasCalled());
        assertNotNull(sentimentService.getLastText());
        assertTrue(sentimentService.getLastText().contains("Great earnings"));
    }

    @Test
    void analyzeSentiment_whenNoArticles_returnsNeutralAndSkipsPython() throws Exception {
        StubCompanyLookupClient lookupClient = new StubCompanyLookupClient("Some Corp.");
        Map<String, Object> response = new HashMap<>();
        response.put("articles", List.of()); // empty list

        StubNewsApiClient newsClient = new StubNewsApiClient(response);

        // If Python were called, we'd see this result; we expect NOT to use it.
        SentimentResult pythonResult =
                new SentimentResult("IGNORED", 1, "very negative");
        StubSentimentPythonService sentimentService =
                new StubSentimentPythonService(pythonResult);

        NewsDataService service =
                new NewsDataService(sentimentService, newsClient, lookupClient);

        SentimentResult result = service.analyzeSentiment("XYZ");

        // Neutral fallback should be used.
        assertEquals("XYZ", result.getCompany());
        assertEquals(3, result.getSentimentScore());
        assertEquals("neutral", result.getSentimentLabel());

        // And the Python layer should never be invoked.
        assertFalse(sentimentService.wasCalled());
    }

    @Test
    void analyzeSentiment_whenLookupFails_fallsBackToSymbolForQuery() throws Exception {
        // lookupCompanyName returns null → should fall back to symbol "MSFT"
        StubCompanyLookupClient lookupClient = new StubCompanyLookupClient(null);

        Map<String, Object> article = new HashMap<>();
        article.put("title", "Generic tech news");
        Map<String, Object> response = new HashMap<>();
        response.put("articles", List.of(article));

        StubNewsApiClient newsClient = new StubNewsApiClient(response);

        SentimentResult pythonResult =
                new SentimentResult("MSFT", 4, "positive");
        StubSentimentPythonService sentimentService =
                new StubSentimentPythonService(pythonResult);

        NewsDataService service =
                new NewsDataService(sentimentService, newsClient, lookupClient);

        SentimentResult result = service.analyzeSentiment("MSFT");

        assertEquals("MSFT", result.getCompany());
        assertEquals(4, result.getSentimentScore());
        assertEquals("positive", result.getSentimentLabel());

        // Because lookup returned null, query must fall back to symbol.
        assertEquals("MSFT", newsClient.getLastQuery());
    }
    // Inside your existing NewsDataServiceTest

    @Test
    void analyzeSentiment_whenArticlesMissingFields_fallsBackToQuery() throws Exception {
        // lookup returns a name
        StubCompanyLookupClient lookupClient = new StubCompanyLookupClient("Acme Corp");

        // articles list exists but all entries are empty maps
        Map<String, Object> article = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("articles", List.of(article));

        StubNewsApiClient newsClient = new StubNewsApiClient(response);

        RecordingSentimentPythonService sentimentService =
                new RecordingSentimentPythonService(
                        new SentimentResult("Acme Corp", 3, "neutral")
                );

        NewsDataService service =
                new NewsDataService(sentimentService, newsClient, lookupClient);

        SentimentResult result = service.analyzeSentiment("ACME");

        // Because no fields were present, text should fall back to query ("Acme Corp")
        assertEquals("Acme Corp", sentimentService.getLastText());
        assertEquals("ACME", result.getCompany());

    }

    @Test
    void analyzeSentiment_stopsAppendingWhenTextTooLong() throws Exception {
        // lookup returns some company name, but not super important here
        StubCompanyLookupClient lookupClient = new StubCompanyLookupClient("Big Corp");

        // Make each article extremely long so adding more than one would
        // blow past any reasonable MAX_TEXT_LENGTH.
        String hugeChunk = "X".repeat(10_000);

        Map<String, Object> a1 = new java.util.HashMap<>();
        a1.put("title", "A1 " + hugeChunk);

        Map<String, Object> a2 = new java.util.HashMap<>();
        a2.put("title", "A2 " + hugeChunk);

        Map<String, Object> a3 = new java.util.HashMap<>();
        a3.put("title", "A3 " + hugeChunk);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("articles", java.util.List.of(a1, a2, a3));

        StubNewsApiClient newsClient = new StubNewsApiClient(response);

        RecordingSentimentPythonService sentimentService =
                new RecordingSentimentPythonService(
                        new SentimentResult("Big Corp", 4, "positive")
                );

        NewsDataService service =
                new NewsDataService(sentimentService, newsClient, lookupClient);

        // Run the method under test
        SentimentResult result = service.analyzeSentiment("BIG");

        // Sanity: Python layer must have been called
        org.junit.jupiter.api.Assertions.assertTrue(sentimentService.wasCalled());

        String textSentToPython = sentimentService.getLastText();

        // We should include the first article...
        org.junit.jupiter.api.Assertions.assertTrue(
                textSentToPython.contains("A1"),
                "Expected the first article to be included in the text"
        );

        // ...but once the accumulated text is long enough, we should stop
        // appending further articles. So these markers must NOT appear.
        org.junit.jupiter.api.Assertions.assertFalse(
                textSentToPython.contains("A2"),
                "Expected the second article NOT to be included once the limit is hit"
        );
        org.junit.jupiter.api.Assertions.assertFalse(
                textSentToPython.contains("A3"),
                "Expected the third article NOT to be included once the limit is hit"
        );
    }


}