package com.example.market.model.news;

/** Holds sentiment analysis result for a given company/news topic. */
public class SentimentResult {
  private final String company;
  private final int sentimentScore;  // e.g. 1 (negative) to 5 (positive)
  private final String sentimentLabel;

  public SentimentResult(String company, int sentimentScore, String sentimentLabel) {
    this.company = company;
    this.sentimentScore = sentimentScore;
    this.sentimentLabel = sentimentLabel;
  }

  public String getCompany() { return company; }
  public int getSentimentScore() { return sentimentScore; }
  public String getSentimentLabel() { return sentimentLabel; }

  @Override
  public String toString() {
    return "SentimentResult{" +
            "company='" + company + '\'' +
            ", sentimentScore=" + sentimentScore +
            ", sentimentLabel='" + sentimentLabel + '\'' +
            '}';
  }
}