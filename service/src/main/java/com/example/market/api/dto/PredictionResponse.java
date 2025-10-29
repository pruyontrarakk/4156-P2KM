package com.example.market.api.dto;

public class PredictionResponse {
  /** The symbol associated with company. */
  private String symbol;      // e.g., AMZN
  /** The prediction horizon. */
  private String horizon;     // e.g., next-day, next-30m
  /** The predicted sentiment direction. */
  private String sentiment;   // "UP" | "DOWN" | "UNKNOWN"
  /** The confidence score of the prediction. */
  private double confidence;  // 0..1
  /** The name or version of the predictive model used. */
  private String model;       // model name/version
  /** The data source or origin of the prediction. */
  private String source;      // notes (e.g., "alphavantage daily; cache-first")

  /** Creates an empty {@code PredictionResponse}. */
  public PredictionResponse() {
  }

  /**
   * Creates a {@code PredictionResponse} with the specified details.
   *
   * @param thisSymbol      the stock symbol being predicted
   * @param thisHorizon     the prediction time frame
   * @param thisSentiment   the predicted sentiment direction
   * @param thisConfidence  the model’s confidence score
   * @param thisModel       the model name or version
   * @param thisSource      the source or metadata describing the prediction
   */
  public PredictionResponse(final String thisSymbol,
                            final String thisHorizon,
                            final String thisSentiment,
                            final double thisConfidence,
                            final String thisModel,
                            final String thisSource) {
    this.symbol = thisSymbol;
    this.horizon = thisHorizon;
    this.sentiment = thisSentiment;
    this.confidence = thisConfidence;
    this.model = thisModel;
    this.source = thisSource;
  }

  /**
   * Creates a {@code PredictionResponse} representing an error state
   *            for the given request.
   *
   * @param symbol  the stock symbol associated with the failed prediction
   * @param horizon the prediction horizon that was requested
   * @param reason  a brief description of the error cause
   * @return a {@link PredictionResponse} indicating an error
   */
  public static PredictionResponse error(final String symbol,
                                         final String horizon,
                                         final String reason) {
    return new PredictionResponse(symbol,
            horizon, "UNKNOWN",
            0.0,
            "none",
            "error:" + reason);
  }

  /** Get the company's symbol.
   * @return symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /** Sets the symbol.
   *
   * @param s company name or symbol
   */
  public void setSymbol(final String s) {
    this.symbol = s;
  }

  /** Gets the model’s horizon.
   *
   * @return horizon
   */
  public String getHorizon() {
    return horizon;
  }

  /** Sets the model’s horizon.
   *
   * @param h model's horizon
   */
  public void setHorizon(final String h) {
    this.horizon = h;
  }

  /** Gets the sentiment score.
   *
   * @return sentiment
   */
  public String getSentiment() {
    return sentiment;
  }

  /** Sets the sentiment.
   *
   * @param s company's sentiment score
   */
  public void setSentiment(final String s) {
    this.sentiment = s;
  }

  /** Returns the model’s confidence score.
   *
   * @return confidence
   */
  public double getConfidence() {
    return confidence;
  }

  /** Sets the model’s confidence score.
   *
   * @param c model's confidence score
   */
  public void setConfidence(final double c) {
    this.confidence = c;
  }

  /** Gets the model name or identifier.
   *
   * @return model
   */
  public String getModel() {
    return model;
  }

  /** Sets the model name or identifier.
   *
   * @param m model name
   */
  public void setModel(final String m) {
    this.model = m;
  }

  /** Returns the data source or origin of the prediction.
   *
   * @return source
   */
  public String getSource() {
    return source;
  }

  /** Sets the data source or origin of the prediction.
   *
   * @param s name of Source
   */
  public void setSource(final String s) {
    this.source = s;
  }
}
