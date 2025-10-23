package com.example.market.service.forecast.python;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

/**
 * Service class for making predictions using a Python script.
 */
@Service
public class PythonService2 {

  public String helloWorld() {
    try {
      // Adjust the command line to include input if necessary
      String line = "python3 test.py "; // Assuming getValue() retrieves the necessary input
      CommandLine cmdLine = CommandLine.parse(line);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
      DefaultExecutor executor = new DefaultExecutor();
      executor.setStreamHandler(streamHandler);
      int exitCode = executor.execute(cmdLine);

      // Check if the exit code is successful
      if (exitCode == 0) {
        // Parse the output to get the result
//        String output = outputStream.toString().trim();
        return outputStream.toString().trim(); // Make sure the output is a valid double
      } else {
        System.err.println("Python script exited with code: " + exitCode);
        return "";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
}
