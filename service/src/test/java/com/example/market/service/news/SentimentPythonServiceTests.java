package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.python.ProcessRunner;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SentimentPythonService exercising both happy-path and
 * error branches (non-zero exit code, missing JSON).
 */
class SentimentPythonServiceTests {

  @Test
  void analyzeSentiment_success_parsesJsonFromMixedOutput() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    String pythonOutput =
        "Some warning...\n" +
        "{ \"company\": \"AAPL\", \"sentimentScore\": 5, \"sentimentLabel\": \"Very Positive\" }\n";

    InputStream fakeInputStream = new ByteArrayInputStream(pythonOutput.getBytes());
    when(mockProcess.getInputStream()).thenReturn(fakeInputStream);
    when(mockProcess.waitFor()).thenReturn(0);

    when(mockRunner.start(any(ProcessBuilder.class))).thenReturn(mockProcess);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act
    SentimentResult result = service.analyzeSentiment("AAPL");

    // Assert
    assertNull(result.getCompany(), "company should be null so NewsDataService can set symbol");
    assertEquals(5, result.getSentimentScore());
    assertEquals("Very Positive", result.getSentimentLabel());
    verify(mockRunner).start(any(ProcessBuilder.class));
  }

  @Test
  void analyzeSentiment_nonZeroExitCode_throwsRuntimeException() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    String pythonOutput = "Some error output\n";
    when(mockProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream(pythonOutput.getBytes()));
    when(mockProcess.waitFor()).thenReturn(1); // non-zero exit code
    when(mockRunner.start(any(ProcessBuilder.class))).thenReturn(mockProcess);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act + Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> service.analyzeSentiment("AAPL"));

    assertTrue(ex.getMessage().contains("Python process failed"),
        "Exception message should indicate Python process failure");
  }

  @Test
  void analyzeSentiment_noJsonLine_throwsRuntimeException() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    // No line that starts with { and ends with }
    String pythonOutput = "just logs\nanother line\n";
    when(mockProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream(pythonOutput.getBytes()));
    when(mockProcess.waitFor()).thenReturn(0);
    when(mockRunner.start(any(ProcessBuilder.class))).thenReturn(mockProcess);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act + Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> service.analyzeSentiment("AAPL"));
    assertTrue(ex.getMessage().contains("No JSON output"),
        "Exception message should indicate missing JSON output");
  }

  @Test
  void analyzeSentiment_whenPythonExitNonZero_throwsHelpfulException() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    when(mockRunner.start(any(ProcessBuilder.class))).thenReturn(mockProcess);
    // Simulate python printing an error then exiting with code 1
    String errorOutput = "Traceback (most recent call last):\nSomething bad\n";
    when(mockProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream(errorOutput.getBytes()));
    when(mockProcess.waitFor()).thenReturn(1);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act + Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> service.analyzeSentiment("AAPL"));

    // The exact message might differ – keep the assert loose but meaningful
    String msg = ex.getMessage();
    assertTrue(msg.contains("Python process failed") || msg.contains("exit code"),
        "Expected message to mention python process failure but was: " + msg);
  }

  @Test
  void analyzeSentiment_whenNoJsonLine_throwsHelpfulException() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    when(mockRunner.start(any(ProcessBuilder.class))).thenReturn(mockProcess);
    // Only non-JSON lines
    String output = "some log line\nanother line\n";
    when(mockProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream(output.getBytes()));
    when(mockProcess.waitFor()).thenReturn(0);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act + Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> service.analyzeSentiment("AAPL"));

    String msg = ex.getMessage();
    assertTrue(msg.contains("No JSON output") || msg.contains("No JSON"),
        "Expected message to mention missing JSON output but was: " + msg);
  }


}
