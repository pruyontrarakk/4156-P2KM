package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NewsDataService {

    private final SentimentPythonService sentimentPythonService;
    private final NewsApiClient newsApiClient;

    public NewsDataService(SentimentPythonService sentimentPythonService,
                           NewsApiClient newsApiClient) {
        this.sentimentPythonService = sentimentPythonService;
        this.newsApiClient = newsApiClient;
    }

    public NewsDataService() {
        this.sentimentPythonService = new SentimentPythonService();
        this.newsApiClient = new NewsApiClient();
    }

    /**
     * NEW LOGIC:
     * Symbol -> query: "SYMBOL stock"
     */
    public SentimentResult analyzeSentiment(String symbol) throws Exception {

        String query = symbol.toUpperCase() + " stock";

        Map<String, Object> response = newsApiClient.fetchNews(query);
        List<Map<String, Object>> articles =
                (List<Map<String, Object>>) response.get("articles");

        if (articles == null || articles.isEmpty()) {
            return new SentimentResult(symbol, 3, "neutral");
        }

        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> article : articles) {
            if (article.get("title") != null)
                sb.append(article.get("title")).append(". ");
            if (article.get("description") != null)
                sb.append(article.get("description")).append(". ");

            if (sb.length() > 2000) break;
        }

        String text = sb.toString().trim();
        if (text.isEmpty()) {
            text = symbol + " stock";
        }

        return sentimentPythonService.analyzeSentiment(text);
    }
}