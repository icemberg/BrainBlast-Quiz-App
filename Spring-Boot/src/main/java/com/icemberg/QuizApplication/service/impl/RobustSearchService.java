package com.icemberg.QuizApplication.service.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class RobustSearchService {

    private static final Logger log = LoggerFactory.getLogger(RobustSearchService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Executes an adaptive multi-tier web search with fallback:
     * 1. Primary: DuckDuckGo HTML snippet scraping
     * 2. Secondary: Jina AI Markdown extraction from the top search result URL
     * 3. Fallback: Signals the LLM to use its internal knowledge base
     *
     * @param query the search query string
     * @return contextual text for the LLM prompt
     */
    public String searchWebWithFallback(String query) {
        String searchUrl = "https://duckduckgo.com/html/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        String topUrl = null;

        // --- PRIMARY STRATEGY: DuckDuckGo Snippet Scraping ---
        try {
            log.info("Attempting Primary Strategy: DuckDuckGo Snippet Scrape for query: {}", query);
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                               "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            Elements results = doc.select(".result");

            // Extract snippets for the LLM
            String snippets = results.stream()
                    .map(el -> el.select(".result__snippet").text())
                    .filter(text -> !text.isEmpty())
                    .limit(3)
                    .collect(Collectors.joining("\n"));

            // Capture the first valid outbound link as a fallback anchor
            Element firstLink = doc.select(".result__a").first();
            if (firstLink != null) {
                topUrl = firstLink.attr("href");
            }

            if (!snippets.trim().isEmpty()) {
                log.info("Primary Strategy Successful. Found snippets.");
                return "Context from Web Search Snippets:\n" + snippets;
            }

        } catch (Exception e) {
            log.warn("Primary DuckDuckGo scraping failed or timed out: {}. Trying fallback...", e.getMessage());
        }

        // --- SECONDARY STRATEGY: Jina AI Reader Fallback ---
        if (topUrl != null && !topUrl.isEmpty()) {
            try {
                log.info("Attempting Secondary Strategy: Jina AI Reader on URL: {}", topUrl);
                String jinaUrl = "https://r.jina.ai/" + topUrl;

                String markdownContent = restTemplate.getForObject(jinaUrl, String.class);

                if (markdownContent != null && !markdownContent.trim().isEmpty()) {
                    log.info("Secondary Jina Strategy Successful.");
                    // Truncate to avoid blowing up LLM context token limits
                    return "Deep Context from Web Page Markdown (via Jina AI):\n" +
                            markdownContent.substring(0, Math.min(markdownContent.length(), 4000));
                }
            } catch (Exception e) {
                log.error("Secondary Jina AI strategy failed: {}", e.getMessage());
            }
        }

        log.error("All web retrieval strategies failed. Proceeding with LLM baseline knowledge.");
        return "Web search could not retrieve live data. Please rely on your internal knowledge base to formulate the answer.";
    }
}
