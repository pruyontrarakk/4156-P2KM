package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SentimentPythonService implements SentimentAnalysisPort {

    @Override
    public SentimentResult analyzeSentiment(String companyName) throws Exception {
        // Run the Python script and capture JSON output
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                "src/main/java/com/example/market/service/news/python/sentiment_model.py",
                companyName
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python process failed: " + output);
        }

        // Filter for the JSON line only (ignore "Device set to use cpu" or warnings)
        String jsonLine = output.lines()
                .filter(line -> line.trim().startsWith("{") && line.trim().endsWith("}"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No JSON output found from Python script"));

        // Parse JSON safely with Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(jsonLine, Map.class);

        String company = (String) result.get("company");
        int score = (int) result.get("sentimentScore");
        String label = (String) result.get("sentimentLabel");

        return new SentimentResult(company, score, label);
    }
}