package com.example.market.service.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphaVantageServiceAdditionalBranchesTest {

    /**
     * Simple stub that bypasses the real HTTP call and returns
     * the JsonNode we give it.
     */
    static class StubAlphaVantageService extends AlphaVantageService {
        private final JsonNode json;

        StubAlphaVantageService(JsonNode json) {
            this.json = json;
        }

        @Override
        protected JsonNode getJson(final String url) {
            // Ignore URL; just return our stub JSON.
            return json;
        }
    }

    @Test
    void informationFieldTriggersIllegalStateException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("""
            {
              "Information": "Premium feature required"
            }
            """);

        AlphaVantageService service = new StubAlphaVantageService(root);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.fetchDaily("AAPL", "dummy-key")
        );

        assertTrue(ex.getMessage().contains("Premium feature"),
                "Expected error message to include 'Premium feature'");
    }

    @Test
    void missingTimeSeriesDailyThrowsHelpfulExceptionListingFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("""
            {
              "Meta Data": { "1. Symbol": "AAPL" }
            }
            """);

        AlphaVantageService service = new StubAlphaVantageService(root);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.fetchDaily("AAPL", "dummy-key")
        );

        String msg = ex.getMessage();
        assertTrue(msg.contains("Missing 'Time Series (Daily)'"),
                "Expected message to mention missing Time Series");
        assertTrue(msg.contains("Meta Data"),
                "Expected message to list 'Meta Data' among response fields");
    }
}
