package com.example.amzn.model;

import java.util.List;

/**
 * Intraday OHLCV series for an arbitrary symbol/interval.
 *
 * @param symbol    stock ticker
 * @param interval  Alpha Vantage intraday interval (e.g., "1min","5min")
 * @param fetchedAt ISO-8601 timestamp of fetch
 * @param source    provenance string (URL/params)
 * @param bars      list of OHLCV bars (ascending by timestamp)
 */
public record IntradaySeries(
    String symbol,
    String interval,
    String fetchedAt,
    String source,
    List<Bar> bars
) { }
