package com.example.market.service.forecast.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class defines the Python Service. It defines
 * useful methods for running python scripts, particularly stock prediction scripts.
 */
@Service
public class PythonService {

  /**
   * Executes a Python script to predict the next 10 stock prices
   *
   * @return The string "Hello World".
   */
  public String runTrendMaster() {
    String result = "";
    try {
//      ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/python3", "src/main/java/com/example/market/service/forecast/trendmaster/main.py");
      ProcessBuilder pb = new ProcessBuilder(
              "/bin/bash", "-c",
              "pip install --quiet trendmaster && python src/main/java/com/example/market/service/forecast/trendmaster/main.py"
      );
      pb.redirectErrorStream(true);

      Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//      String line;
//      Stream<String> lines = reader.lines();
      String lastLine = null;
      String line;
      while ((line = reader.readLine()) != null) {
        lastLine = line;
      }
      if (lastLine == null) {
        throw new RuntimeException("No output from Python script.");
      }
//      System.out.println(lastLine);
      result = lastLine;
//      if ((line = reader.readLine()) != null) {
//          result = line;
//          System.out.println(lines[-1]);
//
//      } else {
//        throw new RuntimeException("No output from Python script.");
//      }
      process.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public Map<String, String> predictFuturePrices() {
    String trendMasterResponse = runTrendMaster();
//    Map<String, String> parsed = parseTrendMasterResponse(trendMasterResponse);
    return parseTrendMasterResponse(trendMasterResponse);
  }

  private Map<String, String> parseTrendMasterResponse(String response) {
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

      for(int i = 0; i < dateNode.size(); i++) {
        result.put(dateNode.get(Integer.toString(i)).asText(), predictionNode.get(Integer.toString(i)).asText());
      }

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}