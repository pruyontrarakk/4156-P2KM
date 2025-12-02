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

/**
 * Service for fetching stock data from the Alpha Vantage API.
 */
@Service
public class AlphaVantageService implements StockDataService {

    /** HTTP client used to make outgoing requests. */
    private final HttpClient http = HttpClient.newHttpClient();
    /** ObjectMapper used for JSON parsing. */

    private final ObjectMapper mapper = new ObjectMapper();

    /** Value for success. */
    private static final int HTTP_SUCCESS = 200;

    /**
     * Fetches the Alpha Vantage {@code TIME_SERIES_DAILY} for a symbol.
     */
    @Override
    public StockDailySeries fetchDaily(
        final String symbol, final String apiKey
    ) throws Exception {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey is required");
        }

        // Use 'compact' instead of 'full' - 'full' is a premium feature
        // 'compact' returns the last 100 data points (free tier)
        final String url = "https://www.alphavantage.co/query"
                + "?function=TIME_SERIES_DAILY&outputsize=compact"
                + "&symbol=" + enc(symbol)
                + "&apikey=" + enc(apiKey);

        final JsonNode root = getJson(url);

        // Check for error messages first
        if (root.hasNonNull("Error Message")) {
            throw new IllegalStateException(
                root.get("Error Message").asText());
        }
        if (root.hasNonNull("Note")) {
            throw new IllegalStateException(root.get("Note").asText());
        }
        // Check for Information field (premium feature requirement)
        if (root.hasNonNull("Information")) {
            throw new IllegalStateException(
                root.get("Information").asText());
        }

        final JsonNode series = root.get("Time Series (Daily)");
        if (series == null
            || series.isMissingNode()
            || !series.fieldNames().hasNext()) {
            // Provide more debugging info about what we actually received
            StringBuilder errorMsg = new StringBuilder(
                "Missing 'Time Series (Daily)' in response. ");
            errorMsg.append("Response fields: ");
            if (root.isObject()) {
                root.fieldNames().forEachRemaining(field ->
                    errorMsg.append(field).append(", "));
            }
            throw new IllegalStateException(errorMsg.toString());
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

    /**
   * Fetches JSON data from a URL.
   *
   * @param url request URL
   * @return parsed JSON root node
   * @throws Exception on IO or non-200 status
   */
    protected JsonNode getJson(final String url) throws Exception {
        final HttpRequest req =
            HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();

        final HttpResponse<String> resp =
            http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != HTTP_SUCCESS) {
            throw new IllegalStateException("HTTP " + resp.statusCode());
        }
        return mapper.readTree(resp.body());
    }

   /**
   * Parses the "Time Series" object into StockBars.
   *
   * @param series the JSON node under "Time Series (Daily)"
   * @return sorted list of daily bars (oldest → newest)
   */
    private List<StockBar> parseDailyBars(final JsonNode series) {
        final List<StockBar> out = new ArrayList<>();
        series.fieldNames().forEachRemaining(date ->
            out.add(StockBar.fromAlphaDaily(date, series.get(date))));
        Collections.sort(out,
            (a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        return out;
    }

    /**
   * URL-encodes a string using UTF-8.
   *
   * @param s the string to encode
   * @return encoded value
   */
    private static String enc(final String s) {
        return java.net.URLEncoder.encode(
            s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
