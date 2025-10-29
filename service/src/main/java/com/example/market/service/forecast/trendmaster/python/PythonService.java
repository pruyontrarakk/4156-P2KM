package com.example.market.service.forecast.trendmaster.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the Python Service. It defines useful methods
 * for running python scripts, particularly stock prediction scripts.
 */
@Service
public class PythonService {
  /**
   * Constructs a new {@code PythonService}.
   */
  public PythonService() { }

  /**
   * Predicts the next 10 stock prices of a company.
   *
   * @param companyName An {@code String} representing the selected company.
   * @return a {@code Map} where each key is a date (as a {@code String}) and
   *                    each value is the corresponding predicted closing price
   *                    (also as a {@code String})
   */
  public Map<String, String> predictFuturePrices(final String companyName) {
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
    try {

      ProcessBuilder pb = new ProcessBuilder(
              "/bin/bash", "-c",
              "pip install --quiet trendmaster && "
              + "python src/main/java/com/example/market/service/forecast/"
              + "trendmaster/main.py"
      );
      pb.redirectErrorStream(true);
      Process process = pb.start();

      BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream()));
      String lastLine = null;
      String line;
      while ((line = reader.readLine()) != null) {
        lastLine = line;
      }
      if (lastLine == null) {
        throw new RuntimeException("No output from Python script.");
      }
      result = lastLine;
      process.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Predicts future stock prices by invoking the TrendMaster model.
   *
   * @return a {@code Map} where each key is a date string
   *         and each value is the predicted price for that date
   */
  public Map<String, String> predictFuturePrices() {
    String trendMasterResponse = runTrendMaster();
//    Map<String, String> parsed = parseTrendMasterResponse(
//    trendMasterResponse);
    return parseTrendMasterResponse(trendMasterResponse);
  }

  /**
   * Parses predictions made from TrendMaster model.
   *
   * @param response An {@code String} representing the standard output
   *                 from python script.
   * @return a {@code Map} where each key is a date string
   *         and each value is the predicted price for that date
   */
  private Map<String, String> parseTrendMasterResponse(final String response) {
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

      for (int i = 0; i < dateNode.size(); i++) {
        result.put(dateNode.get(Integer.toString(i)).asText(),
                predictionNode.get(Integer.toString(i)).asText());
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}
