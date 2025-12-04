package com.example.market.service.news;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class NewsApiClient {

    private static final String API_KEY = "5c1be135169f488a9b7080e6cb14a6d0";
    private static final String BASE_URL = "https://newsapi.org/v2/everything";

    public Map<String, Object> fetchNews(String query) {

        RestTemplate rest = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("q", query)
                .queryParam("language", "en")
                .queryParam("sortBy", "popularity")
                .queryParam("apiKey", API_KEY)
                .toUriString();

        return rest.getForObject(url, Map.class);
    }
}