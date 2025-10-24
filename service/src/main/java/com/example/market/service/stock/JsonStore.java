package com.example.market.service.stock; // keep for now; you can move to infra later

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class JsonStore {

  private final ObjectMapper mapper = new ObjectMapper();
  private final Path base = Paths.get("data"); // repo-root/data

  /** data/stocks/<symbol>-daily.json */
  public Path dailyPath(final String symbol) {
    return base.resolve(Paths.get("stocks", norm(symbol) + "-daily.json"));
  }

  /** data/news/<symbol>.json */
  public Path newsPath(final String symbol) {
    return base.resolve(Paths.get("news", norm(symbol) + ".json"));
  }

  public boolean exists(final Path file) {
    return Files.exists(file);
  }

  public <T> T read(final Path file, final Class<T> type) throws IOException {
    return mapper.readValue(Files.readAllBytes(file), type);
  }

  public void write(final Path file, final Object value) throws IOException {
    Files.createDirectories(file.getParent());
    byte[] bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value);
    Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static String norm(String s) {
    return s == null ? "" : s.trim().toLowerCase();
  }
}
