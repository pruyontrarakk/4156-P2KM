package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NewsDataService {

    private final SentimentPythonService sentimentPythonService;
    private final NewsApiClient newsApiClient;
    private final CompanyLookupClient lookupClient;

    public NewsDataService(SentimentPythonService sentimentPythonService,
                           NewsApiClient newsApiClient,
                           CompanyLookupClient lookupClient) {
        this.sentimentPythonService = sentimentPythonService;
        this.newsApiClient = newsApiClient;
        this.lookupClient = lookupClient;
    }

    public NewsDataService() {
        this.sentimentPythonService = new SentimentPythonService();
        this.newsApiClient = new NewsApiClient();
        this.lookupClient = new CompanyLookupClient();
    }

    /**
     * Symbol → Company Name → NewsAPI → Sentiment
     */
    public SentimentResult analyzeSentiment(String symbol) throws Exception {

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
            return new SentimentResult(symbol, 3, "neutral");
        }

        // 3. Build article text
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> article : articles) {
            if (article.get("title") != null)
                sb.append(article.get("title")).append(". ");
            if (article.get("description") != null)
                sb.append(article.get("description")).append(". ");
            if (sb.length() > 2000) break;
        }

        String text = sb.toString().trim();
        if (text.isEmpty()) text = query;

        // 4. Sentiment from Python
        SentimentResult pythonResult = sentimentPythonService.analyzeSentiment(text);

        // 5. Final result → include original stock symbol
        return new SentimentResult(symbol,
                pythonResult.getSentimentScore(),
                pythonResult.getSentimentLabel());
    }
}