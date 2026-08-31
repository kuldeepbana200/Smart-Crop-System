package com.smartcrop.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.ai.controller.AiController.EducationTopic;
import com.smartcrop.ai.controller.AiController.YouTubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeService.class);
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final int MAX_RESULTS_PER_QUERY = 3;
    private static final String AGRICULTURAL_INSTITUTION_PREFIX = "(icar|kvk|krishi|agriculture university|agricultural university|government agriculture|department of agriculture|aicrp|agri science|farm science|agricultural research|agriculture department|agricultural extension)";
    private static final Pattern PRODUCT_PROMO_PATTERN = Pattern.compile("(buy now|shop now|order now|limited offer|discount|fertilizer|pesticide|weedicide|insecticide|brand|dealer|supplier|agri input|agri products|agri company)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRUSTED_SOURCE_PATTERN = Pattern.compile("(icar|kvk|krishi vigyan kendra|agricultural university|india agriculture|agriculture department|department of agriculture|farm science|agricultural research|extension|agri university)", Pattern.CASE_INSENSITIVE);

    private final String youTubeApiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public YouTubeService(@Value("${app.youtube.api-key:}") String youTubeApiKey) {
        this.youTubeApiKey = youTubeApiKey;
    }

    public boolean isConfigured() {
        return youTubeApiKey != null && !youTubeApiKey.isBlank();
    }

    public List<EducationTopic> enrichTopicsWithVideos(List<EducationTopic> topics) {
        if (topics == null || topics.isEmpty()) {
            return List.of();
        }

        Map<String, List<YouTubeVideo>> queryCache = new LinkedHashMap<>();
        List<EducationTopic> enriched = new ArrayList<>();

        for (EducationTopic topic : topics) {
            if (topic == null || topic.searchQuery() == null || topic.searchQuery().isBlank()) {
                enriched.add(new EducationTopic(topic == null ? "" : topic.title(),
                        topic == null ? "" : topic.reason(),
                        topic == null ? "" : topic.searchQuery(),
                        List.of()));
                continue;
            }

            String normalizedQuery = topic.searchQuery().trim();
            List<YouTubeVideo> videos = queryCache.computeIfAbsent(
                    normalizedQuery,
                    this::fetchVideosForSearchQuery);
            enriched.add(new EducationTopic(topic.title(), topic.reason(), topic.searchQuery(), videos));
        }

        return enriched;
    }

    private List<YouTubeVideo> fetchVideosForSearchQuery(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return List.of();
        }

        if (!isConfigured()) {
            log.warn("YouTube API key is missing; cannot fetch videos for search query: {}", searchQuery);
            return List.of();
        }

        try {
            String encodedQuery = URLEncoder.encode(buildYouTubeSearchQuery(searchQuery), StandardCharsets.UTF_8);
            String url = String.format(Locale.US,
                    "%s?part=snippet&type=video&maxResults=12&order=relevance&q=%s&key=%s",
                    YOUTUBE_SEARCH_URL,
                    encodedQuery,
                    URLEncoder.encode(youTubeApiKey, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                log.warn("YouTube API request failed for query '{}' with status {} and body: {}",
                        searchQuery,
                        response.statusCode(),
                        response.body());
                return List.of();
            }

            JsonNode body = objectMapper.readTree(response.body());
            JsonNode items = body.path("items");
            if (items == null || !items.isArray()) {
                return List.of();
            }

            List<YouTubeVideo> videos = new ArrayList<>();
            for (JsonNode item : items) {
                if (videos.size() >= MAX_RESULTS_PER_QUERY) {
                    break;
                }

                JsonNode snippet = item.path("snippet");
                JsonNode idNode = item.path("id");
                String videoId = idNode.path("videoId").asText(null);
                if (videoId == null || videoId.isBlank()) {
                    continue;
                }

                String title = snippet.path("title").asText("");
                String description = snippet.path("description").asText("");
                String channelTitle = snippet.path("channelTitle").asText("");

                if (!isCredibleAgricultureVideo(title, description, channelTitle)) {
                    continue;
                }

                String thumbnail = snippet.path("thumbnails").path("high").path("url").asText(
                        snippet.path("thumbnails").path("medium").path("url").asText(
                                snippet.path("thumbnails").path("default").path("url").asText("")));
                String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;

                videos.add(new YouTubeVideo(videoId, title, description, thumbnail, channelTitle, youtubeUrl));
                if (videos.size() >= MAX_RESULTS_PER_QUERY) {
                    break;
                }
            }

            return videos;
        } catch (Exception ex) {
            log.warn("YouTube video lookup failed for query '{}': {}", searchQuery, ex.getMessage(), ex);
            return List.of();
        }
    }

    private String buildYouTubeSearchQuery(String searchQuery) {
        String base = searchQuery == null ? "" : searchQuery.trim();
        if (base.isBlank()) {
            return "agriculture education India";
        }

        return base + " agriculture education India";
    }

    private boolean isCredibleAgricultureVideo(String title, String description, String channelTitle) {
        String combined = (title == null ? "" : title) + " " + (description == null ? "" : description) + " " + (channelTitle == null ? "" : channelTitle);
        if (combined == null || combined.isBlank()) {
            return false;
        }

        String normalized = combined.toLowerCase(Locale.ROOT);

        boolean trustedSource = TRUSTED_SOURCE_PATTERN.matcher(normalized).find();
        boolean productPromotion = PRODUCT_PROMO_PATTERN.matcher(normalized).find();
        boolean suspiciousBrandText = normalized.contains("buy") && (normalized.contains("fertilizer") || normalized.contains("pesticide") || normalized.contains("seed"));

        if (productPromotion && !trustedSource) {
            return false;
        }

        if (suspiciousBrandText && !trustedSource) {
            return false;
        }

        return trustedSource || !(normalized.contains("promotion") || normalized.contains("offer") || normalized.contains("discount") || normalized.contains("dealer") || normalized.contains("supplier"));
    }
}