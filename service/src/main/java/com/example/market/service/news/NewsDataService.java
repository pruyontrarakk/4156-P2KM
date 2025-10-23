package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

@Service
public class NewsDataService {

  // Eventually this can call MediastackService for real data
  public SentimentResult analyzeSentiment(String company) {
    // Placeholder logic for now
    int score;
    String label;

    switch (company.toLowerCase()) {
      case "amzn":
      case "amazon":
        score = 4;
        label = "positive";
        break;
      case "meta":
        score = 3;
        label = "neutral";
        break;
      default:
        score = 2;
        label = "negative";
    }

    return new SentimentResult(company, score, label);
  }
}