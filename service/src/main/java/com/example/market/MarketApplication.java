package com.example.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for market service.
 */
@SpringBootApplication
public final class MarketApplication {
  /**
   * Private constructor to prevent instantiation.
   */
  private MarketApplication() {
    // Utility class
  }

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(MarketApplication.class, args);
  }
}
