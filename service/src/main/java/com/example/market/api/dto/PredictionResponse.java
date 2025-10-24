package com.example.market.api.dto;

public class PredictionResponse {
  private String symbol;      // e.g., AMZN
  private String horizon;     // e.g., next-day, next-30m
  private String sentiment;   // "UP" | "DOWN" | "UNKNOWN"
  private double confidence;  // 0..1
  private String model;       // model name/version
  private String source;      // notes (e.g., "alphavantage daily; cache-first")

  public PredictionResponse() {}
  public PredictionResponse(String symbol, String horizon, String sentiment,
                            double confidence, String model, String source) {
    this.symbol = symbol; this.horizon = horizon; this.sentiment = sentiment;
    this.confidence = confidence; this.model = model; this.source = source;
  }
  public static PredictionResponse error(String symbol, String horizon, String reason) {
    return new PredictionResponse(symbol, horizon, "UNKNOWN", 0.0, "none", "error:" + reason);
  }
  // getters/setters
  public String getSymbol() { return symbol; }
  public void setSymbol(String s) { this.symbol = s; }
  public String getHorizon() { return horizon; }
  public void setHorizon(String h) { this.horizon = h; }
  public String getSentiment() { return sentiment; }
  public void setSentiment(String s) { this.sentiment = s; }
  public double getConfidence() { return confidence; }
  public void setConfidence(double c) { this.confidence = c; }
  public String getModel() { return model; }
  public void setModel(String m) { this.model = m; }
  public String getSource() { return source; }
  public void setSource(String s) { this.source = s; }
}
