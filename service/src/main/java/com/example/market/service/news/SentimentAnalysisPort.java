package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;

/**
 * Defines the interface to do sentiment analysis.
 */
public interface SentimentAnalysisPort {
    /** Outlines a method to compute sentiment rating of a company.
     *
     * @param companyName name of company to perform sentiment
     *                    analysis on.
     * @return a {@link SentimentResult} containing the company name,
     *                   a generated sentiment score (1–5),
     *                   and the associated sentiment label
     * */
    SentimentResult analyzeSentiment(String companyName) throws Exception;
}
