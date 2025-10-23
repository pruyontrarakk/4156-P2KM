package com.example.market.service.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JsonStore {
  private final ObjectMapper mapper = new ObjectMapper();
  private final Path dataDir = Path.of("service", "data");

  public void write(Path file, Object value) throws IOException {
    Files.createDirectories(dataDir);
    final Path out = dataDir.resolve(file.getFileName());
    mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), value);
  }

  public <T> T read(Path file, Class<T> type) throws IOException {
    final Path in = Path.of("service", "data").resolve(file.getFileName());
    return mapper.readValue(in.toFile(), type);
  }
}
