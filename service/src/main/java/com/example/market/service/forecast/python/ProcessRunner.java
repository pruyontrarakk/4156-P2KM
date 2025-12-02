package com.example.market.service.forecast.python;

/**
 * Abstraction for starting external processes.
 */
public interface ProcessRunner {

  /**
   * Method to start Process Builder.
   *
   * @param pb an {@link ProcessBuilder} object.
   *
   * @return the {@link Process} started by the given {@link ProcessBuilder}
   * */
  Process start(ProcessBuilder pb) throws Exception;
}

