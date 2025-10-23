package com.example.amzn.model;

/**
 * Record representing a single bar of stock data.
 *
 * @param date the date of the bar
 * @param open the opening price
 * @param high the highest price
 * @param low the lowest price
 * @param close the closing price
 * @param adjClose the adjusted closing price
 * @param volume the trading volume
 */
public record Bar(
    String date,
    Double open,
    Double high,
    Double low,
    Double close,
    Double adjClose,
    Long volume
) { }
