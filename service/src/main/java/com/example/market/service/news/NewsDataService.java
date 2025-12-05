package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NewsDataService {

    /** Python service that analyzes sentiment. */
    private final SentimentPythonService sentimentPythonService;
    /** Client that connects to News API. */
    private final NewsApiClient newsApiClient;
    /** Client that connects to Lookup API. */
    private final CompanyLookupClient lookupClient;
    /** Maximum length of each article pulled. */
    private static final int MAX_ARTICLE_LEN = 2000;
    /** Default sentiment score of company. */
    private static final int DEFAULT_SENTIMENT_SCORE = 3;

    /**
     * All args constructor.
     *
     * @param thisSentimentPythonService service used to analyze sentiment
     * @param thisNewsApiClient API used to look up news articles
     * @param thisLookupClient Used to look up a company name and symbol
     * */
    public NewsDataService(final SentimentPythonService
                                   thisSentimentPythonService,
                           final NewsApiClient thisNewsApiClient,
                           final CompanyLookupClient thisLookupClient) {
        this.sentimentPythonService = thisSentimentPythonService;
        this.newsApiClient = thisNewsApiClient;
        this.lookupClient = thisLookupClient;
    }

    /**
     * No args constructor.
     * */
    public NewsDataService() {
        this.sentimentPythonService = new SentimentPythonService();
        this.newsApiClient = new NewsApiClient();
        this.lookupClient = new CompanyLookupClient();
    }

    /**
     * Analyzes sentiment of news articles that are written about
     * given company.
     *
     * @param symbol company stock symbol
     * @return {@link SentimentResult} object
     */
    public SentimentResult analyzeSentiment(final String symbol)
            throws Exception {

        // 1. Lookup company name using FMP
        String companyName = lookupClient.lookupCompanyName(symbol);

        // Clean fallback
        String query;
        if (companyName != null && !companyName.isBlank()) {
            query = companyName.replace("Inc.", "").trim();
        } else {
            query = symbol;  // fallback
        }

        // 2. Fetch news using company name
        Map<String, Object> response = newsApiClient.fetchNews(query);
        List<Map<String, Object>> articles =
                (List<Map<String, Object>>) response.get("articles");

        if (articles == null || articles.isEmpty()) {
            return new SentimentResult(symbol,
                    DEFAULT_SENTIMENT_SCORE, "neutral");
        }

        // 3. Build article text
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> article : articles) {
            if (article.get("title") != null) {
                sb.append(article.get("title")).append(". ");
            }
            if (article.get("description") != null) {
                sb.append(article.get("description")).append(". ");
            }
            if (sb.length() > MAX_ARTICLE_LEN) {
                break;
            }
        }

        String text = sb.toString().trim();
        if (text.isEmpty()) {
            text = query;
        }

        // 4. Sentiment from Python
        SentimentResult pythonResult = sentimentPythonService
                .analyzeSentiment(text);

        // 5. Final result → include original stock symbol
        return new SentimentResult(symbol,
                pythonResult.getSentimentScore(),
                pythonResult.getSentimentLabel());
    }
}
