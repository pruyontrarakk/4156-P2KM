package com.example.market;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic smoke test to ensure the Spring application context starts.
 * We intentionally do NOT call MarketApplication.main(...) here, so
 * tests do not try to bind to port 8080.
 */
@SpringBootTest
class MarketApplicationTest {

  @Test
  void contextLoads() {
    // If the application context starts without exceptions, this passes.
  }
}