package com.example.market.model.news;

/** Holds sentiment analysis result for a given company/news topic. */
public class SentimentResult {
  /** company name. */
  private final String company;
  /** initial sentiment score. */
  private final int sentimentScore;  // e.g. 1 (negative) to 5 (positive)
  /** initial sentiment label. */
  private final String sentimentLabel;

  /**
   * All args constructor.
   *
   * @param thisCompany name of company
   * @param thisSentimentScore initial sentiment score
   * @param thisSentimentLabel initial sentiment label
   * */
  public SentimentResult(final String thisCompany,
                         final int thisSentimentScore,
                         final String thisSentimentLabel) {
    this.company = thisCompany;
    this.sentimentScore = thisSentimentScore;
    this.sentimentLabel = thisSentimentLabel;
  }

  /** Get company name.
   *
   * @return company name
   * */
  public String getCompany() {
    return company;
  }
  /** Get sentiment score.
   *
   * @return sentiment score
   * */
  public int getSentimentScore() {
    return sentimentScore;
  }
  /** Get sentiment label.
   *
   * @return sentiment label */
  public String getSentimentLabel() {
    return sentimentLabel;
  }

  /** Convert sentiment result to
   * formatted string. */
  @Override
  public String toString() {
    return "SentimentResult{"
            + "company='" + company + '\''
            + ", sentimentScore=" + sentimentScore
            + ", sentimentLabel='" + sentimentLabel + '\''
            + '}';
  }
}
