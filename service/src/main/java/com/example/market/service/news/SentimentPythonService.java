package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.python.DefaultProcessRunner;
import com.example.market.service.forecast.python.ProcessRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SentimentPythonService implements SentimentAnalysisPort {

    /** {@link ProcessRunner} object. */
    private final ProcessRunner processRunner;

    /** All-args constructor. */
    public SentimentPythonService(final ProcessRunner thisProcessRunner) {
        this.processRunner = thisProcessRunner;
    }

    /** No-args constructor. */
    public SentimentPythonService() {
        this.processRunner = new DefaultProcessRunner();
    }

    /**
     * Calls Python script with RAW TEXT and returns sentiment.
     */
    @Override
    public SentimentResult analyzeSentiment(final String text) throws Exception {

        // Run the Python script and capture JSON output
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                "src/main/java/com/example/market/service/news/python/sentiment_model.py",
                text
        );
        pb.redirectErrorStream(true);

        Process process = processRunner.start(pb);

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python process failed: " + output);
        }

        // Extract JSON line from Python output
        String jsonLine = output.lines()
                .filter(line -> line.trim().startsWith("{") && line.trim().endsWith("}"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No JSON output found from Python script"));

        // Parse JSON safely
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(jsonLine, Map.class);

        int score = (int) result.get("sentimentScore");
        String label = (String) result.get("sentimentLabel");

        return new SentimentResult("News", score, label);
    }
}