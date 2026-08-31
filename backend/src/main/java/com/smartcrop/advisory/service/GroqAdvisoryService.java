package com.smartcrop.advisory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GroqAdvisoryService {

    private static final Logger log = LoggerFactory.getLogger(GroqAdvisoryService.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "openai/gpt-oss-20b";

    private final String groqApiKey;
    private final ObjectMapper objectMapper;

    public GroqAdvisoryService(
            @Value("${app.groq.api-key:}") String groqApiKey,
            ObjectMapper objectMapper) {
        this.groqApiKey = groqApiKey;
        this.objectMapper = objectMapper;
    }

    public List<AdvisoryRecommendation> generateForFarmer(
            Farmer farmer,
            Crop crop,
            WeatherForecastResponse weather,
            RiskEngine.RiskResult riskResult,
            String languageCode) {

        if (farmer == null || crop == null || weather == null || weather.daily() == null) {
            return List.of();
        }

        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("Groq advisory generation skipped because GROQ_API_KEY is not configured.");
            return List.of();
        }

        String prompt = buildPrompt(farmer, crop, weather, riskResult, languageCode);
        String response = callGroq(prompt);
        if (response == null || response.isBlank()) {
            return List.of();
        }

        List<AdvisoryRecommendation> parsed = parseRecommendations(response);
        return parsed.isEmpty() ? List.of() : parsed;
    }

    private String buildPrompt(
            Farmer farmer,
            Crop crop,
            WeatherForecastResponse weather,
            RiskEngine.RiskResult riskResult,
            String languageCode) {

        String languageName = switch (languageCode == null ? "en" : languageCode.toLowerCase(Locale.ROOT)) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            case "or" -> "Odia";
            default -> "English";
        };

        String location = String.format(Locale.US, "%s, %s",
                farmer.getDistrict() == null ? "Not set" : farmer.getDistrict(),
                farmer.getState() == null ? "Not set" : farmer.getState());

        CurrentWeatherResponse currentWeather = weather.current();
        String weatherText = currentWeather == null
                ? "Current weather unavailable."
                : String.format(Locale.US,
                        "Temperature %.1f°C, humidity %.0f%%, rainfall %.1f mm, wind %.1f km/h, weather code %s.",
                        currentWeather.temperature(),
                        currentWeather.relativeHumidity(),
                        currentWeather.precipitation(),
                        currentWeather.windSpeed(),
                        currentWeather.weatherCode());

        WeatherForecastResponse.DailyForecast daily = weather.daily();
        String dailySummary = daily == null ? "Daily forecast unavailable." : String.format(Locale.US,
                "Rainfall probability %.0f%%, rainfall %.1f mm, max temperature %.1f°C, min temperature %.1f°C, wind %.1f km/h, evapotranspiration %.1f mm.",
                daily.precipitationProbabilityMax() == null || daily.precipitationProbabilityMax().isEmpty()
                        ? 0.0
                        : daily.precipitationProbabilityMax().get(0),
                daily.precipitationSum() == null || daily.precipitationSum().isEmpty()
                        ? 0.0
                        : daily.precipitationSum().get(0),
                daily.temperatureMax() == null || daily.temperatureMax().isEmpty()
                        ? 0.0
                        : daily.temperatureMax().get(0),
                daily.temperatureMin() == null || daily.temperatureMin().isEmpty()
                        ? 0.0
                        : daily.temperatureMin().get(0),
                daily.windSpeedMax() == null || daily.windSpeedMax().isEmpty()
                        ? 0.0
                        : daily.windSpeedMax().get(0),
                daily.evapotranspiration() == null || daily.evapotranspiration().isEmpty()
                        ? 0.0
                        : daily.evapotranspiration().get(0));

        String riskSummary = riskResult == null
                ? "Risk assessment unavailable."
                : String.format(Locale.US, "Risk score %d, risk level %s, recommended action: %s.",
                        riskResult.score(),
                        riskResult.riskLevel(),
                        riskResult.recommendedAction());

        return "You are a practical agricultural advisor for Indian farmers. "
                + "Reply ONLY in " + languageName + ". Keep the answer concise, actionable, and specific to the farmer's field conditions. "
                + "Use only the real facts provided below. Never invent dates, chemical dosages, products, or guarantees. "
                + "If required information is missing, say it clearly instead of guessing. "
                + "Farmer location: " + location + ". "
                + "Crop: " + crop.getCropName() + ". "
                + "Crop stage: " + (crop.getCropStage() == null ? "Not set" : crop.getCropStage()) + ". "
                + "Land area: " + (farmer.getLandArea() == null ? "Not set" : farmer.getLandArea()) + " acres. "
                + "Current weather: " + weatherText + " "
                + "Forecast summary: " + dailySummary + " "
                + "Risk assessment: " + riskSummary + " "
                + "Return ONLY valid JSON with this exact shape: {\"recommendations\":[{\"category\":\"string\",\"severity\":\"string\",\"title\":\"string\",\"recommendation\":\"string\",\"reason\":\"string\"}]}. "
                + "Use 2 to 4 recommendations. Severity must be one of URGENT, WARNING, ADVISORY, or INFO. "
                + "Do not include markdown fences or any other text outside the JSON.";
    }

    private String callGroq(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return null;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", GROQ_MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You generate only valid JSON for agricultural advisories."),
                            Map.of("role", "user", "content", prompt)),
                    "max_completion_tokens", 2048,
                    "temperature", 0.2,
                    "response_format", Map.of("type", "json_object")));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Groq API request failed with status " + response.statusCode() + ": " + response.body());
            }

            JsonNode body = objectMapper.readTree(response.body());
            String content = body.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                return null;
            }
            return content.trim();
        } catch (Exception ex) {
            log.warn("Groq advisory generation failed", ex);
            return null;
        }
    }

    private List<AdvisoryRecommendation> parseRecommendations(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        if (normalized.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(normalized);
            JsonNode recommendations = root.has("recommendations") ? root.get("recommendations") : root;
            if (recommendations == null || !recommendations.isArray()) {
                return List.of();
            }

            List<AdvisoryRecommendation> results = new ArrayList<>();
            for (JsonNode item : recommendations) {
                if (!item.isObject()) {
                    continue;
                }

                String category = readRequiredString(item, "category");
                String severity = readRequiredString(item, "severity");
                String title = readRequiredString(item, "title");
                String recommendation = readRequiredString(item, "recommendation");
                String reason = readRequiredString(item, "reason");
                results.add(new AdvisoryRecommendation(category, severity, title, recommendation, reason));
            }

            return results;
        } catch (Exception ex) {
            log.warn("Unable to parse Groq advisory JSON. Raw response: {}", normalized, ex);
            return List.of();
        }
    }

    private String readRequiredString(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.asText() == null || field.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required advisory field: " + fieldName);
        }
        return field.asText().trim();
    }
}
