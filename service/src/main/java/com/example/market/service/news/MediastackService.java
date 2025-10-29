package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;
import java.util.Random;

// @Service
public class MediastackService {

    private static final String[] LABELS = {
        "very negative", "negative", "neutral", "positive", "very positive"
    };

    public SentimentResult getSentiment(String companyName) {
        // TODO: Later, replace this with real API + ML model (FinGPT/NewsAPI)
        // For Iteration 1, return a random sentiment for testing

        Random random = new Random();
        int score = random.nextInt(5) + 1;  // 1–5 scale
        String label = LABELS[score - 1];

        return new SentimentResult(companyName, score, label);
    }
}