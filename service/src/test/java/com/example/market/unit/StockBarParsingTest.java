package com.example.market.unit;

import com.example.market.model.stock.StockBar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockBarParsingTest {

  private static ObjectNode node(ObjectMapper m, String open, String high, String low, String close, String vol) {
    ObjectNode n = m.createObjectNode();
    n.put("1. open", open);
    n.put("2. high", high);
    n.put("3. low", low);
    n.put("4. close", close);
    n.put("5. volume", vol);
    return n;
  }

  @Test
  void parsesDecimalVolumeByFallingBackToBigDecimalLongValue() {
    ObjectMapper mapper = new ObjectMapper();
    var n = node(mapper, "1.00", "2.00", "0.50", "1.50", "5.0");
    StockBar bar = StockBar.fromAlphaDaily("2025-10-22", n);
    assertEquals(5L, bar.getVolume());
  }

  @Test
  void missingFieldThrowsHelpfulException() {
    ObjectMapper mapper = new ObjectMapper();
    var n = mapper.createObjectNode();
    // only set some fields; omit "2. high"
    n.put("1. open", "1.00");
    n.put("3. low", "0.50");
    n.put("4. close", "1.50");
    n.put("5. volume", "10");

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> StockBar.fromAlphaDaily("2025-10-22", n));
    assertTrue(ex.getMessage().contains("Missing field"));
  }
}
