package com.example.market.service.stock; // keep for now; you can move to infra later

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Component;

@Component
public class JsonStore {
  /**
   * The JSON object mapper used for serializing and deserializing model data
   * to and from JSON files.
   */
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * The root directory under which all data files (such as news, forecasts, and stock data)
   * are stored or retrieved.
   */
  private final Path base = Paths.get("data"); // repo-root/data

  /** data/stocks/<symbol>-daily.json */
  public Path dailyPath(final String symbol) {
    return base.resolve(Paths.get("stocks", norm(symbol) + "-daily.json"));
  }

  /** data/news/<symbol>.json */
  public Path newsPath(final String symbol) {
    return base.resolve(Paths.get("news", norm(symbol) + ".json"));
  }

  /**
   * Checks whether a file exists at the specified path.
   *
   * @param file the {@link Path} to the file or directory to check
   * @return {@code true} if a file or directory exists at
   * the given path; {@code false} otherwise
   */
  public boolean exists(final Path file) {
    return Files.exists(file);
  }

  /**
   * Reads and deserializes a JSON file into an object of the specified type.
   *
   * @param <T>   the type of object to deserialize to
   * @param file  the {@link Path} to the JSON file to be read
   * @param type  the target class representing the type to deserialize
   * @return an instance of {@code T} populated with data from the JSON file
   * @throws IOException if an I/O error occurs while reading the file
   * or parsing its contents
   */
  public <T> T read(final Path file, final Class<T> type) throws IOException {
    return mapper.readValue(Files.readAllBytes(file), type);
  }
  /**
   * Writes the specified object as a formatted JSON file to the given path.
   *
   * @param file  the target {@link Path} where the JSON file will be written
   * @param value the object to serialize and write to the file
   * @throws IOException if an I/O error occurs while creating directories
   * or writing the file
   */
  public void write(final Path file, final Object value) throws IOException {
    Files.createDirectories(file.getParent());
    byte[] bytes = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsBytes(value);
    Files.write(file, bytes, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static String norm(final String s) {
    return s == null ? "" : s.trim().toLowerCase();
  }
}
