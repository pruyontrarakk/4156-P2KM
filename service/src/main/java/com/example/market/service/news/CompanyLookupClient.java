package com.example.market.service.news;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Map;

@Service
public class CompanyLookupClient {

    /** key to access Lookup API. */
    private static final String API_KEY =
            "2K5f4fIVopDrEUgWHMqowmlBXzkQSosz";

    /** url to locate Lookup API. */
    private static final String BASE_URL =
            "https://financialmodelingprep.com/stable/search-symbol";

    /**
     * Looks up the corresponding company name based on
     * stock symbol.
     *
     * @param symbol company stock symbol
     * @return A company's full name
     * */
    public String lookupCompanyName(final String symbol) {
        RestTemplate rest = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("query", symbol)
                .queryParam("apikey", API_KEY)
                .toUriString();

        List<Map<String, Object>> response = rest.getForObject(url, List.class);

        if (response == null || response.isEmpty()) {
            return null;
        }

        Object name = response.get(0).get("name");
        return name != null ? name.toString() : null;
    }
}
