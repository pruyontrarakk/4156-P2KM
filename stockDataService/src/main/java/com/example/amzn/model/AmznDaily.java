package com.example.amzn.model;

import java.util.List;

/**
 * Record representing daily AMZN stock data.
 *
 * @param symbol the stock symbol
 * @param fetchedAt timestamp when data was fetched
 * @param source the data source information
 * @param bars list of daily bar data
 */
public record AmznDaily(
    String symbol,
    String fetchedAt,
    String source,
    List<Bar> bars
) { }
