package com.smartcrop.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GroqClient(
            @Value("${app.groq.api-key:}") String apiKey,
            @Value("${app.groq.model:openai/gpt-oss-20b}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String complete(String systemPrompt, String userPrompt, int maxCompletionTokens, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)),
                    "max_completion_tokens", maxCompletionTokens,
                    "temperature", temperature,
                    "response_format", Map.of("type", "json_object")));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 429) {
                log.warn("Groq request rate limited. Model: {}, status: 429", model);
                throw new GroqRateLimitException();
            }
            if (response.statusCode() >= 400) {
                log.error("Groq request failed. Model: {}, status: {}", model, response.statusCode());
                throw new IllegalStateException("Groq request failed with status " + response.statusCode());
            }

            JsonNode body = objectMapper.readTree(response.body());
            String content = body.path("choices").path(0).path("message").path("content").asText(null);
            return content == null || content.isBlank() ? null : content.trim();
        } catch (GroqRateLimitException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Groq request interrupted", exception);
        } catch (Exception exception) {
            log.error("Groq request failed before a usable completion was returned: {}", exception.getMessage());
            throw new IllegalStateException("Groq request failed", exception);
        }
    }

    public String model() {
        return model;
    }
}
