package com.example.market.service.forecast.python;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
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
  public String helloWorld() {
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
      System.out.println(lastLine);
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
}
