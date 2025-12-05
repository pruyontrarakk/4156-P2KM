package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;

/**
 * Test-only helper that extends SentimentPythonService so it can be
 * used wherever a SentimentPythonService is expected.
 *
 * It records calls to analyzeSentiment and returns a preconfigured result.
 */
class RecordingSentimentPythonService extends SentimentPythonService {

    private boolean called = false;
    private String lastText = null;
    private final SentimentResult resultToReturn;

    /**
     * Default constructor returns a neutral-ish sentiment.
     */
    RecordingSentimentPythonService() {
        this(new SentimentResult("DEFAULT", 3, "neutral"));
    }

    /**
     * Constructor that lets tests control the sentiment result.
     */
    RecordingSentimentPythonService(final SentimentResult resultToReturn) {
        super(); // use the default ProcessRunner (we won't actually invoke Python)
        this.resultToReturn = resultToReturn;
    }

    @Override
    public SentimentResult analyzeSentiment(final String text) throws Exception {
        this.called = true;
        this.lastText = text;
        return resultToReturn;
    }

    boolean wasCalled() {
        return called;
    }

    String getLastText() {
        return lastText;
    }
}
