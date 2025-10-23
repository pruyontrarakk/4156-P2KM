package com.example.market.service.stock;

import com.example.market.model.stock.StockBar;
import com.example.market.model.stock.StockDailySeries;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AlphaVantageService implements StockDataService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public StockDailySeries fetchDaily(final String symbol, final String apiKey) throws Exception {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol is required");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("apiKey is required");

        final String url = "https://www.alphavantage.co/query"
                + "?function=TIME_SERIES_DAILY&outputsize=compact"
                + "&symbol=" + enc(symbol)
                + "&apikey=" + enc(apiKey);

        final JsonNode root = getJson(url);
        if (root.hasNonNull("Error Message")) throw new IllegalStateException(root.get("Error Message").asText());
        if (root.hasNonNull("Note")) throw new IllegalStateException(root.get("Note").asText()); // throttle

        final JsonNode series = root.get("Time Series (Daily)");
        if (series == null || series.isMissingNode() || !series.fieldNames().hasNext()) {
            throw new IllegalStateException("Missing 'Time Series (Daily)' in response");
        }

        final List<StockBar> bars = parseDailyBars(series);
        return new StockDailySeries(
                symbol.toUpperCase(),
                Instant.now().toString(),
                "alphavantage: TIME_SERIES_DAILY, url=" + url,
                bars
        );
    }

    /* ---------- helpers ---------- */

    private JsonNode getJson(final String url) throws Exception {
        final HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        final HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new IllegalStateException("HTTP " + resp.statusCode());
        return mapper.readTree(resp.body());
    }

    private List<StockBar> parseDailyBars(final JsonNode series) {
        final List<StockBar> out = new ArrayList<>();
        series.fieldNames().forEachRemaining(date -> out.add(StockBar.fromAlphaDaily(date, series.get(date))));
        Collections.sort(out, (a, b) -> a.getTimestamp().compareTo(b.getTimestamp())); // ascending
        return out;
    }

    private static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
