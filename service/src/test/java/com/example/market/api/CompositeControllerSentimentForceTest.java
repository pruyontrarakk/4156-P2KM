package com.example.market.api;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.ForecastDataService;
import com.example.market.service.news.NewsDataService;
import com.example.market.service.stock.JsonStore;
import com.example.market.service.stock.StockDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompositeController.class)
class CompositeControllerSentimentForceTest {

  @Autowired MockMvc mvc;

  @MockBean StockDataService stocks;
  @MockBean ForecastDataService forecast;
  @MockBean NewsDataService news;
  @MockBean JsonStore store;

  @TempDir Path tmp;

  @Test
  void sentiment_forceTrue_overridesFreshCache_writesNewPayload() throws Exception {
    Path cache = tmp.resolve("data/news/amzn.json");
    Files.createDirectories(cache.getParent());
    Files.writeString(cache, "{\"company\":\"AMZN\",\"sentimentLabel\":\"positive\"}");
    Files.setLastModifiedTime(cache, FileTime.from(Instant.now()));
    when(store.newsPath("AMZN")).thenReturn(cache);

    // Returning a different label verifies we didn't use the cached payload
    SentimentResult fresh = new SentimentResult("AMZN", 1, "very negative");
    when(news.analyzeSentiment("AMZN")).thenReturn(fresh);

    mvc.perform(get("/market/sentiment").param("force", "true"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.company", is("AMZN")))
        .andExpect(jsonPath("$.sentimentLabel", is("very negative")))
        .andExpect(jsonPath("$.source", is("news-placeholder")));

    // verify the controller wrote the fresh payload
    verify(store).write(eq(cache), any(Map.class));
    verify(news).analyzeSentiment("AMZN");
  }
}
