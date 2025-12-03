package com.example.market.service.news;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.python.ProcessRunner;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SentimentPythonServiceTests {
  @Test
  void testAnalyzeSentiment_success() throws Exception {
    // Arrange
    ProcessRunner mockRunner = mock(ProcessRunner.class);
    Process mockProcess = mock(Process.class);

    String pythonOutput =
            "Some warning...\n" +
                    "{ \"company\": \"AAPL\", \"sentimentScore\": 5, \"sentimentLabel\": \"Very Positive\" }\n";

    InputStream fakeInputStream = new ByteArrayInputStream(pythonOutput.getBytes());
    when(mockProcess.getInputStream()).thenReturn(fakeInputStream);
    when(mockProcess.waitFor()).thenReturn(0);

    // When ProcessRunner is called with ANY ProcessBuilder, return mockProcess
    when(mockRunner.start(any())).thenReturn(mockProcess);

    SentimentPythonService service = new SentimentPythonService(mockRunner);

    // Act
    SentimentResult result = service.analyzeSentiment("AAPL");

    // Assert
    assertEquals("AAPL", result.getCompany());
    assertEquals(5, result.getSentimentScore());
    assertEquals("Very Positive", result.getSentimentLabel());

    verify(mockRunner).start(any(ProcessBuilder.class));
  }
}

