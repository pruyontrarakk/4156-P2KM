package com.example.market.unit;

import com.example.market.model.stock.StockBar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional branch coverage for StockBar: equals() short-circuits and
 * blank-field handling in fromAlphaDaily().
 */
class StockBarAdditionalBranchesTest {

  @Test
  void equals_sameInstanceReturnsTrue() {
    StockBar bar = new StockBar(
        "2025-01-01",
        new BigDecimal("1.0"),
        new BigDecimal("2.0"),
        new BigDecimal("0.5"),
        new BigDecimal("1.5"),
        10L
    );

    assertTrue(bar.equals(bar), "equals must be reflexive");
  }

  @Test
  void equals_differentTypeReturnsFalse() {
    StockBar bar = new StockBar(
        "2025-01-01",
        new BigDecimal("1.0"),
        new BigDecimal("2.0"),
        new BigDecimal("0.5"),
        new BigDecimal("1.5"),
        10L
    );

    assertFalse(bar.equals("not-a-stockbar"),
        "equals should return false for different type");
  }

  @Test
  void blankFieldThrowsIllegalStateException() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode n = mapper.createObjectNode();
    n.put("1. open", "1.00");
    n.put("2. high", ""); // blank field
    n.put("3. low", "0.50");
    n.put("4. close", "1.50");
    n.put("5. volume", "10");

    // We only care that an IllegalStateException is thrown;
    // exact message can change without breaking the test.
    assertThrows(IllegalStateException.class,
        () -> StockBar.fromAlphaDaily("2025-10-22", n));
  }
}
