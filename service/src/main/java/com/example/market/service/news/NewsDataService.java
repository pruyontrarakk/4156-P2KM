package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

@Service
public class NewsDataService {

    private final SentimentPythonService sentimentPythonService;

    public NewsDataService(SentimentPythonService sentimentPythonService) {
        this.sentimentPythonService = sentimentPythonService;
    }

    // No-arg fallback constructor for compatibility
    public NewsDataService() {
        this.sentimentPythonService = new SentimentPythonService();
    }

    public SentimentResult analyzeSentiment(String company) throws Exception {
        return sentimentPythonService.analyzeSentiment(company);
    }
}