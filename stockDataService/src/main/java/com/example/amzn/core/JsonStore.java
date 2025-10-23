package com.example.amzn.core;

import com.example.amzn.model.AmznDaily;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Component
public final class JsonStore {

  private final ObjectMapper mapper = new ObjectMapper();

  /** Deserialize JSON file into the given type. */
  public <T> Optional<T> read(final Path file, final Class<T> type) {
    try {
      if (!Files.exists(file)) return Optional.empty();
      final byte[] bytes = Files.readAllBytes(file);
      return Optional.of(mapper.readValue(bytes, type));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  /** Convenience overload kept for existing daily callers. */
  public Optional<AmznDaily> read(final Path file) {
    return read(file, AmznDaily.class);
  }

  /** Write as JSON for both dailt and intraday. */
  public void writeAtomic(final Path file, final Object value) throws IOException {
    final Path dir = file.getParent();
    if (dir != null) Files.createDirectories(dir);

    final byte[] json = mapper.writeValueAsBytes(value);
    final Path tmp = Files.createTempFile(dir != null ? dir : file.getParent(), "json-", ".tmp");
    Files.write(tmp, json);
    Files.move(tmp, file,
        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
  }

}
