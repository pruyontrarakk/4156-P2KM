package com.example.market.model.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

final class StockBarTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void fromAlphaDaily_parsesAllFields_andIgnoresUnknown() throws Exception {
    String json = "{"
        + "\"1. open\":\"100.12\","
        + "\"2. high\":\"105.50\","
        + "\"3. low\":\"99.90\","
        + "\"4. close\":\"104.00\","
        + "\"5. volume\":\"1234567\","
        + "\"extra\":\"ignored\""
        + "}";
    JsonNode node = MAPPER.readTree(json);

    StockBar bar = StockBar.fromAlphaDaily("2025-10-28", node);

    assertEquals("2025-10-28", bar.getTimestamp());
    assertEquals(new BigDecimal("100.12"), bar.getOpen());
    assertEquals(new BigDecimal("105.50"), bar.getHigh());
    assertEquals(new BigDecimal("99.90"), bar.getLow());
    assertEquals(new BigDecimal("104.00"), bar.getClose());
    assertEquals(1_234_567L, bar.getVolume());
  }

  @Test
  void fromAlphaDaily_acceptsDecimalVolumeStrings() throws Exception {
    String json = "{"
        + "\"1. open\":\"10.0\","
        + "\"2. high\":\"11.0\","
        + "\"3. low\":\"9.0\","
        + "\"4. close\":\"10.5\","
        + "\"5. volume\":\"7.0\""
        + "}";
    JsonNode node = MAPPER.readTree(json);

    StockBar bar = StockBar.fromAlphaDaily("2025-10-22", node);
    assertEquals(7L, bar.getVolume());
  }

  @Test
  void getters_returnValuesProvidedToConstructor() {
    StockBar bar = new StockBar(
        "2025-10-22",
        new BigDecimal("1.23"),
        new BigDecimal("2.34"),
        new BigDecimal("1.11"),
        new BigDecimal("2.00"),
        42L);

    assertAll(
        () -> assertEquals("2025-10-22", bar.getTimestamp()),
        () -> assertEquals(new BigDecimal("1.23"), bar.getOpen()),
        () -> assertEquals(new BigDecimal("2.34"), bar.getHigh()),
        () -> assertEquals(new BigDecimal("1.11"), bar.getLow()),
        () -> assertEquals(new BigDecimal("2.00"), bar.getClose()),
        () -> assertEquals(42L, bar.getVolume())
    );
  }

  @Test
  void equalsAndHashCode_obeyValueSemantics() {
    StockBar a = new StockBar("2025-10-22",
        new BigDecimal("1.23"), new BigDecimal("2.34"),
        new BigDecimal("1.11"), new BigDecimal("2.00"), 42L);

    StockBar b = new StockBar("2025-10-22",
        new BigDecimal("1.23"), new BigDecimal("2.34"),
        new BigDecimal("1.11"), new BigDecimal("2.00"), 42L);

    StockBar c = new StockBar("2025-10-22",
        new BigDecimal("1.23"), new BigDecimal("2.34"),
        new BigDecimal("1.11"), new BigDecimal("2.01"), 42L);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
  }

  @Test
  void toString_containsKeyFields() {
    StockBar bar = new StockBar("2025-10-22",
        new BigDecimal("1.23"), new BigDecimal("2.34"),
        new BigDecimal("1.11"), new BigDecimal("2.00"), 42L);

    String s = bar.toString();
    assertTrue(s.contains("StockBar"));
    assertTrue(s.contains("timestamp='2025-10-22'"));
    assertTrue(s.contains("open=1.23"));
    assertTrue(s.contains("high=2.34"));
    assertTrue(s.contains("low=1.11"));
    assertTrue(s.contains("close=2.00"));
    assertTrue(s.contains("volume=42"));
  }

  @Test
  void fromAlphaDaily_missingField_throwsIllegalStateException() throws Exception {
    // Missing "2. high"
    String json = "{"
        + "\"1. open\":\"100.12\","
        + "\"3. low\":\"99.90\","
        + "\"4. close\":\"104.00\","
        + "\"5. volume\":\"12345\""
        + "}";
    JsonNode node = MAPPER.readTree(json);

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> StockBar.fromAlphaDaily("2025-10-28", node));
    assertTrue(ex.getMessage().contains("2. high"));
  }

  @Test
  void fromAlphaDaily_blankField_throwsIllegalStateException() throws Exception {
    String json = "{"
        + "\"1. open\":\"100.12\","
        + "\"2. high\":\"\","
        + "\"3. low\":\"99.90\","
        + "\"4. close\":\"104.00\","
        + "\"5. volume\":\"12345\""
        + "}";
    JsonNode node = MAPPER.readTree(json);

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> StockBar.fromAlphaDaily("2025-10-28", node));
    assertTrue(ex.getMessage().contains("2. high"));
  }

  @Test
  void fromAlphaDaily_zeroVolume_ok() throws Exception {
    String json = "{"
        + "\"1. open\":\"1.00\","
        + "\"2. high\":\"1.10\","
        + "\"3. low\":\"0.90\","
        + "\"4. close\":\"1.05\","
        + "\"5. volume\":\"0\""
        + "}";
    JsonNode node = MAPPER.readTree(json);

    StockBar bar = StockBar.fromAlphaDaily("2025-10-29", node);
    assertEquals(0L, bar.getVolume());
  }

  @Test
  void constructor_nullGuards_throwNpeWithFieldName() {
    BigDecimal bd = new BigDecimal("1");
    NullPointerException e1 = assertThrows(NullPointerException.class,
        () -> new StockBar(null, bd, bd, bd, bd, 1L));
    assertEquals("timestamp", e1.getMessage());

    NullPointerException e2 = assertThrows(NullPointerException.class,
        () -> new StockBar("t", null, bd, bd, bd, 1L));
    assertEquals("open", e2.getMessage());

    NullPointerException e3 = assertThrows(NullPointerException.class,
        () -> new StockBar("t", bd, null, bd, bd, 1L));
    assertEquals("high", e3.getMessage());

    NullPointerException e4 = assertThrows(NullPointerException.class,
        () -> new StockBar("t", bd, bd, null, bd, 1L));
    assertEquals("low", e4.getMessage());

    NullPointerException e5 = assertThrows(NullPointerException.class,
        () -> new StockBar("t", bd, bd, bd, null, 1L));
    assertEquals("close", e5.getMessage());
  }
}
