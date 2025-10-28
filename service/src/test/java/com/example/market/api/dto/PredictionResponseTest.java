package com.example.market.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredictionResponseTest {

  @Test
  void gettersSetters_andErrorFactory() {
    PredictionResponse p = new PredictionResponse();
    p.setSymbol("AMZN");
    p.setHorizon("next-day");
    p.setSentiment("UP");
    p.setConfidence(0.75);
    p.setModel("demo");
    p.setSource("unit");

    assertEquals("AMZN", p.getSymbol());
    assertEquals("next-day", p.getHorizon());
    assertEquals("UP", p.getSentiment());
    assertEquals(0.75, p.getConfidence(), 1e-9);
    assertEquals("demo", p.getModel());
    assertEquals("unit", p.getSource());

    PredictionResponse err = PredictionResponse.error("AMZN", "h", "because");
    assertEquals("UNKNOWN", err.getSentiment());
    assertEquals(0.0, err.getConfidence(), 1e-9);
    assertEquals("none", err.getModel());
    assertEquals("error:because", err.getSource());
  }
}
