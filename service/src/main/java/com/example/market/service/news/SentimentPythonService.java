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

    /**
     * All-args constructor.
     *
     * @param thisProcessRunner given {@link ProcessRunner} object.
     * */
    public SentimentPythonService(final ProcessRunner thisProcessRunner) {
        this.processRunner = thisProcessRunner;
    }

    /**
     * No-args constructor.
     * */
    public SentimentPythonService() {
        this.processRunner = new DefaultProcessRunner();
    }

    /**
     * Calls upon Python model to get sentiment rating.
     *
     * @param companyName the name of the company for
     *                    which sentiment is being requested;
     * @return a {@link SentimentResult} containing the company name,
     *                   a generated sentiment score (1–5),
     *                   and the associated sentiment label
     */
    @Override
    public SentimentResult analyzeSentiment(final String companyName)
            throws Exception {
        // Run the Python script and capture JSON output
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                "src/main/java/com/example/market/service/news/python/"
                + "sentiment_model.py",
                companyName
        );
        pb.redirectErrorStream(true);

        //Process process = pb.start();
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

        // Filter for the JSON line only
        // (ignore "Device set to use cpu" or warnings)
        String jsonLine = output.lines()
                .filter(line -> line.trim().startsWith("{")
                        && line.trim().endsWith("}"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No JSON output found from Python script"
                ));

        // Parse JSON safely with Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(jsonLine, Map.class);

        String company = (String) result.get("company");
        int score = (int) result.get("sentimentScore");
        String label = (String) result.get("sentimentLabel");

        return new SentimentResult(company, score, label);
    }
}
