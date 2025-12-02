package com.example.market.service.forecast.python;

/**
 * Default implementation of {@link ProcessRunner}.
 */
public class DefaultProcessRunner implements ProcessRunner {
  /**
   * Uses {@link ProcessRunner} to start a process.
   *
   * @param pb {@link ProcessBuilder} object to run process.
   * */
  @Override
  public Process start(final ProcessBuilder pb) throws Exception {
    return pb.start();
  }
}
