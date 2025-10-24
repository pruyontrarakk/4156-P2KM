package com.example.market.service.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class JsonStore {

  private final ObjectMapper mapper = new ObjectMapper();
  // Base folder for persisted files (repo-root /data). Adjust if you prefer another dir.
  private final Path base = Paths.get("data");

  /** Resolve the cache file for a symbol's daily series, e.g. data/aapl-daily.json. */
  public Path dailyPath(final String symbol) {
    return base.resolve(symbol.toLowerCase() + "-daily.json");
  }

  /** True if the cache file exists. */
  public boolean exists(final Path file) {
    return Files.exists(file);
  }

  /** Read and map JSON file to a type. */
  public <T> T read(final Path file, final Class<T> type) throws IOException {
    byte[] bytes = Files.readAllBytes(file);
    return mapper.readValue(bytes, type);
  }

  /** Ensure parent folder exists and write JSON. */
  public void write(final Path file, final Object value) throws IOException {
    Files.createDirectories(file.getParent());
    byte[] bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value);
    Files.write(file, bytes);
  }
}
