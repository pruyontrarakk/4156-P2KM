package com.example.market.unit;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.stock.AlphaVantageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers branches in AlphaVantageService.fetchDaily.
 */
class AlphaVantageServiceBranchesTest {

    static class StubAlpha extends AlphaVantageService {
        private final JsonNode canned;
        StubAlpha(JsonNode canned) { this.canned = canned; }
        @Override
        protected JsonNode getJson(String url) {
            return canned; // no HTTP; pure unit test
        }
    }

    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void rejectsBlankSymbol() {
        var svc = new AlphaVantageService();
        var ex = assertThrows(IllegalArgumentException.class, () -> svc.fetchDaily("  ", "KEY"));
        assertTrue(ex.getMessage().toLowerCase().contains("symbol"));
    }

    @Test
    void rejectsBlankApiKey() {
        var svc = new AlphaVantageService();
        var ex = assertThrows(IllegalArgumentException.class, () -> svc.fetchDaily("AAPL", " "));
        assertTrue(ex.getMessage().toLowerCase().contains("apikey"));
    }

    @Test
    void providerErrorMessage() throws Exception {
        var canned = M.readTree("{\"Error Message\":\"Bad symbol\"}");
        var svc = new StubAlpha(canned);
        var ex = assertThrows(IllegalStateException.class, () -> svc.fetchDaily("AAPL", "KEY"));
        assertTrue(ex.getMessage().contains("Bad symbol"));
    }

    @Test
    void reachedLimitProviderNote() throws Exception {
        var canned = M.readTree("{\"Note\":\"Thank you for using Alpha Vantage\"}");
        var svc = new StubAlpha(canned);
        var ex = assertThrows(IllegalStateException.class, () -> svc.fetchDaily("AAPL", "KEY"));
        assertTrue(ex.getMessage().contains("Alpha Vantage"));
    }

    @Test
    void missingTimeSeries() throws Exception {
        var canned = M.readTree("{}");
        var svc = new StubAlpha(canned);
        var ex = assertThrows(IllegalStateException.class, () -> svc.fetchDaily("AAPL", "KEY"));
        assertTrue(ex.getMessage().contains("Missing 'Time Series (Daily)'"));
    }

    @Test
    void emptySeriesObject() throws Exception {
        var canned = M.readTree("{\"Time Series (Daily)\":{}}");
        var svc = new StubAlpha(canned);
        var ex = assertThrows(IllegalStateException.class, () -> svc.fetchDaily("AAPL", "KEY"));
        assertTrue(ex.getMessage().contains("Missing 'Time Series (Daily)'"));
    }

    @Test
    void happyPathBranch_parsesAndSorts() throws Exception {
        var body = "{ \"Time Series (Daily)\": { " +
                "\"2025-10-22\":{\"1. open\":\"2\",\"2. high\":\"3\",\"3. low\":\"1\",\"4. close\":\"2.5\",\"5. volume\":\"10\"}," +
                "\"2025-10-21\":{\"1. open\":\"1\",\"2. high\":\"1\",\"3. low\":\"1\",\"4. close\":\"1\",\"5. volume\":\"5\"}" +
                "} }";
        var canned = M.readTree(body);
        var svc = new StubAlpha(canned);

        StockDailySeries out = svc.fetchDaily("aapl", "KEY");
        assertEquals("AAPL", out.getSymbol());
        assertEquals(2, out.getBars().size());
        // ascending by date
        assertEquals("2025-10-21", out.getBars().get(0).getTimestamp());
        assertEquals("2025-10-22", out.getBars().get(1).getTimestamp());
    }
}
