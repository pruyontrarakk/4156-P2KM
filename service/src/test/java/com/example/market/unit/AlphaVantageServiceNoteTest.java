package com.example.market.unit;

import com.example.market.service.stock.AlphaVantageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphaVantageServiceNoteTest {

  static class StubAlpha extends AlphaVantageService {
    private final String payload;
    StubAlpha(String payload) { this.payload = payload; }
    @Override
    protected com.fasterxml.jackson.databind.JsonNode getJson(String url) throws Exception {
      return new ObjectMapper().readTree(payload);
    }
  }

  @Test
  void noteFieldTriggersIllegalState() {
    ObjectNode root = new ObjectMapper().createObjectNode();
    root.put("Note", "Thank you for using Alpha Vantage!");
    AlphaVantageService svc = new StubAlpha(root.toString());
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> svc.fetchDaily("AMZN", "key"));
    // The message is the note text, so assert on its content:
    assertTrue(ex.getMessage().contains("Alpha Vantage"));
  }
}
