package com.example.amzn.tools;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IntradayClient {
  public static void main(String[] args) throws Exception {
    // Vars (env or defaults)
    final String base     = env("BASE_URL", "http://localhost:8080");
    final String symbol   = env("SYMBOL",   "AMZN");
    final String interval = env("INTERVAL", "5min");
    final String month    = env("MONTH",    "2024-06");

    final String savePath = env("SAVE_PATH", "");

    final String url = base + "/amzn/intraday/month"
        + "?symbol="   + enc(symbol)
        + "&interval=" + enc(interval)
        + "&month="    + enc(month);

    final HttpClient client = HttpClient.newHttpClient();
    final HttpRequest req = HttpRequest.newBuilder(URI.create(url))
        .GET()
        .header("Accept", "application/json")
        .build();

    final HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() / 100 != 2) {
      System.err.println("HTTP " + resp.statusCode() + " → " + resp.body());
      System.exit(1);
    }

    final String body = resp.body();
    // Preview (first 500 chars)
    System.out.println(body.substring(0, Math.min(body.length(), 500)));
    System.out.println("\nURL: " + url);

    // ---- Write to file ----
    final Path out = (savePath.isBlank())
        ? Path.of("data", symbol + "-" + interval + "-" + month + ".json")
        : Path.of(savePath);

    final Path parent = out.getParent();
    if (parent != null) Files.createDirectories(parent);
    Files.writeString(out, body, StandardCharsets.UTF_8);

    System.out.println("Saved: " + out.toAbsolutePath());
  }

  private static String env(String k, String d) { return System.getenv().getOrDefault(k, d); }
  private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
