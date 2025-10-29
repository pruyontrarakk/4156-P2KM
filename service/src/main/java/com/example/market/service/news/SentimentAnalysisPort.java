package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;

public interface SentimentAnalysisPort {
    SentimentResult analyzeSentiment(String companyName) throws Exception;
}