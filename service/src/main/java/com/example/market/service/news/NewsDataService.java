package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

@Service
public class NewsDataService {

    /** PythonService object to run sentiment analysis model. */
    private final SentimentPythonService sentimentPythonService;

    /** All-args constructor.
     *
     * @param thisSentimentService a {@link SentimentPythonService}
     *                                    object
     */
    public NewsDataService(final SentimentPythonService thisSentimentService) {
        this.sentimentPythonService = thisSentimentService;
    }

    /** No-arg fallback constructor for compatibility. */
    public NewsDataService() {
        this.sentimentPythonService = new SentimentPythonService();
    }

    /**
     * Calculates sentiment score of a company.
     *
     * @param company the name of the company for
     *                    which sentiment is being requested;
     * @return a {@link SentimentResult} containing the company name,
     *                   a generated sentiment score (1–5),
     *                   and the associated sentiment label
     */
    public SentimentResult analyzeSentiment(final String company)
            throws Exception {
        return sentimentPythonService.analyzeSentiment(company);
    }
}
