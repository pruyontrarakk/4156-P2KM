package com.example.market.service.forecast.python;

import com.example.market.model.stock.StockDailySeries;
import com.example.market.service.stock.AlphaVantageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * This class defines the Python Service. It defines useful methods
 * for running python scripts, particularly stock prediction scripts.
 */
@Service("pythonService")
public class PythonService {
  /** Maximum length for error message preview. */
  private static final int ERROR_PREVIEW_LENGTH = 50;
  /** Maximum length for JSON response preview. */
  private static final int JSON_PREVIEW_LENGTH = 200;

  /** used to run python processes. */
  private final ProcessRunner processRunner;

  /** used to get up-to-date stock data. */
  private AlphaVantageService stockDataService;

  /** Default horizon setting. */
  private static final int DEFAULT_HORIZON = 10;

  /**
   * All-args constructor.
   *
   * @param thisProcessRunner {@link ProcessRunner} object.
   * @param thisStockDataService {@link AlphaVantageService} object.
   * */
  public PythonService(final ProcessRunner thisProcessRunner,
                       final AlphaVantageService thisStockDataService) {
    this.processRunner = thisProcessRunner;
    this.stockDataService = thisStockDataService;
  }

  /**
   * Constructs a new {@code PythonService}.
   */
  public PythonService() {
    this.processRunner = new DefaultProcessRunner();
    this.stockDataService = new AlphaVantageService();
  }

  /**
   * Gets current stock data from StockDataService.
   *
   * @param companyName Symbol representing company.
   */
  public void getStockData(final String companyName) {
    System.out.println("Getting stock data for " + companyName);
    String key = System.getenv("ALPHAVANTAGE_API_KEY");
    try {
      StockDailySeries stockDailySeries = stockDataService
              .fetchDaily(companyName, key);
      // convert to JSON string
      ObjectMapper mapper = new ObjectMapper();

      // write to file
      mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File("stock_daily.json"), stockDailySeries);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * Updates Python model predictions based on given horizon.
   *
   * @param horizon X amount of days into the future to predict.
   */
  public void updatePythonScript(final int horizon) {
    // Update python script based on given horizon
    try {
      Path pythonScript = Paths.get(System.getProperty("user.dir")
              + "/service/src/main/java/com/example"
              + "/market/service/forecast/trendmaster/main.py"
      );
      List<String> lines = Files.readAllLines(pythonScript);
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).contains("future_steps=")) {
          lines.set(i, "future_steps=" + horizon);
          break;
        }
      }
      Files.write(pythonScript, lines);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Predicts the next 10 stock prices of a company.
   *
   * @param companyName An {@code String} representing the selected company.
   * @return a {@code Map} where each key is a date (as a {@code String}) and
   *                    each value is the corresponding predicted closing price
   *                    (also as a {@code String})
   */
  public Map<String, String> predictFuturePrices(final String companyName) {
    return predictFuturePrices(companyName, DEFAULT_HORIZON);
  }
  /**
   * Predicts the next X stock prices of a company.
   *
   * @param companyName An {@code String} representing the selected company.
   * @param horizon X amount of days in the future to predict.
   * @return a {@code Map} where each key is a date (as a {@code String}) and
   *                    each value is the corresponding predicted closing price
   *                    (also as a {@code String})
   */
  public Map<String, String> predictFuturePrices(final String companyName,
                                                 final int horizon) {
    // create stock_daily.json in directory
    try {
      getStockData(companyName);
      updatePythonScript(horizon);
    } catch (Exception e) {
      e.printStackTrace();
    }



    String trendMasterResponse = runTrendMaster();
    return parseTrendMasterResponse(trendMasterResponse);
  }

  /**
   * Executes a Python script responsible for forecasting the
   * next 10 stock prices.
   *
   * @return the standard output from the executed Python script
   *            as a {@code String}
   * @throws RuntimeException if the Python script fails to
   *            execute or produces no output
   */
  public String runTrendMaster() {
    String result = "";
    StringBuilder allOutput = new StringBuilder();
    try {
      System.out.println("Working directory: "
              + new File(".").getAbsolutePath());
      ProcessBuilder pb = new ProcessBuilder(
              "/bin/bash", "-c",
              "python3 -m pip install --quiet trendmaster && "
              + "python3 service/src/main/java"
              + "/com/example/market/service/forecast/"
              + "trendmaster/main.py"
      );
      Map<String, String> env = pb.environment();
      String oldPath = env.get("PATH");
      env.put("PATH", oldPath
              + ":/Library/Frameworks/Python.framework/Versions/3.10/bin");
      pb.redirectErrorStream(true);
      //Process process = pb.start();
      Process process = processRunner.start(pb);

      BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream()));
      String lastLine = null;
      String line;
      while ((line = reader.readLine()) != null) {
        allOutput.append(line).append("\n");
        lastLine = line;
      }

      int exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new RuntimeException("Python script failed with exit code "
            + exitCode + ". Output: " + allOutput.toString());
      }

      if (lastLine == null) {
        throw new RuntimeException("No output from Python script. "
            + "Full output: " + allOutput.toString());
      }
      result = lastLine;
    } catch (Exception e) {
      throw new RuntimeException("Failed to run TrendMaster script: "
          + e.getMessage() + ". Output: " + allOutput.toString(), e);
    }

    return result;
  }

  /**
   * Predicts the next 10 stock prices of a company.
   *
   * @param response An {@code String} representing the standard output from
   *                 TrendMaster Python script.
   * @return a {@code Map} generated from TrendMaster where each key is a date
   *                 (as a {@code String}) and each value is the corresponding
   *                 predicted closing price (also as a {@code String})
   * @throws RuntimeException if the Python script fails to execute
   *                 or produces no output
   */
  public Map<String, String> parseTrendMasterResponse(final String response) {
    if (response == null || response.trim().isEmpty()) {
      throw new RuntimeException("Empty response from Python script");
    }

    // Check if response looks like JSON (should start with { or ")
    String trimmed = response.trim();
    if (!trimmed.startsWith("{") && !trimmed.startsWith("\"")) {
      String preview = trimmed.length() > ERROR_PREVIEW_LENGTH
          ? trimmed.substring(0, ERROR_PREVIEW_LENGTH) + "..."
          : trimmed;
      throw new RuntimeException("Python script output is not valid JSON. "
          + "Response starts with: " + preview);
    }

    Map<String, String> result = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      JsonNode outer = mapper.readTree(response);
      JsonNode rootNode;
      if (outer.isTextual()) {
        rootNode = mapper.readTree(outer.asText());
      } else {
        rootNode = outer;
      }
      JsonNode dateNode = rootNode.get("Date");
      JsonNode predictionNode = rootNode.get("Predicted_Close");

      if (dateNode == null || predictionNode == null) {
        throw new RuntimeException("Missing 'Date' or 'Predicted_Close' "
            + "in JSON response");
      }

      for (int i = 0; i < dateNode.size(); i++) {
        result.put(dateNode.get(Integer.toString(i)).asText(),
                predictionNode.get(Integer.toString(i)).asText());
      }

    } catch (JsonProcessingException e) {
      String preview = response.length() > JSON_PREVIEW_LENGTH
          ? response.substring(0, JSON_PREVIEW_LENGTH) + "..."
          : response;
      throw new RuntimeException("Failed to parse JSON from Python script. "
          + "Response: " + preview, e);
    }

    return result;
  }
}
