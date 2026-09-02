package com.smartcrop.advisory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.service.CropLifecycle;
import com.smartcrop.crop.service.CropLifecycleCalculator;
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

                return generateForFarmer(farmer, crop, weather, riskResult, languageCode,
                                crop == null ? null
                                                : CropLifecycleCalculator.calculate(crop, java.time.LocalDate.now()));
        }

        public List<AdvisoryRecommendation> generateForFarmer(
                        Farmer farmer,
                        Crop crop,
                        WeatherForecastResponse weather,
                        RiskEngine.RiskResult riskResult,
                        String languageCode,
                        CropLifecycle lifecycle) {

                if (farmer == null || crop == null || weather == null || weather.daily() == null) {
                        return List.of();
                }

                if (groqApiKey == null || groqApiKey.isBlank()) {
                        log.warn("Groq advisory generation skipped because GROQ_API_KEY is not configured.");
                        return List.of();
                }

                String prompt = buildPrompt(farmer, crop, weather, riskResult, languageCode, lifecycle);
                log.info("Advisory facts before Groq. Today: {}, Crop: {}, Stage: {}, Planting date: {}, Harvest date: {}, Lifecycle: {}",
                                java.time.LocalDate.now(), crop.getCropName(), crop.getCropStage(),
                                crop.getSowingDate(), crop.getExpectedHarvestDate(), lifecycle);
                String response = callGroq(prompt);
                if (response == null || response.isBlank()) {
                        return List.of();
                }

                List<AdvisoryRecommendation> parsed = parseRecommendations(response);
                if (parsed.isEmpty()) {
                        return List.of();
                }

                log.info("Groq recommendations parsed. Crop: {}, Lifecycle: {}, Planting date: {}, Count: {}",
                                crop.getCropName(), lifecycle, crop.getSowingDate(), parsed.size());
                parsed.forEach(recommendation -> log.info(
                                "Groq recommendation: Category: {}, Severity: {}, Title: {}",
                                recommendation.category(), recommendation.severity(), recommendation.title()));

                // FACT-LOCK: Validate recommendations against backend facts
                AdvisoryFactValidator validator = new AdvisoryFactValidator(farmer, crop, weather, riskResult,
                                lifecycle);
                try {
                        List<AdvisoryRecommendation> validated = validator.validate(parsed);

                        log.info("Groq advisory generated and validated successfully. " +
                                        "Farmer: {} ({}), Crop: {}, Stage: {}, Risk: {}/{}",
                                        farmer.getId(),
                                        farmer.getDistrict() != null ? farmer.getDistrict() : "unknown",
                                        crop.getCropName(),
                                        crop.getCropStage() != null ? crop.getCropStage() : "not set",
                                        riskResult != null ? riskResult.score() : "N/A",
                                        riskResult != null ? riskResult.riskLevel() : "N/A");

                        return validated;
                } catch (AdvisoryFactValidator.AdvisoryValidationException ex) {
                        log.error("Advisory validation failed - Groq output not fact-grounded: {}", ex.getMessage());
                        throw new GroqAdvisoryGenerationException(
                                        "Generated advisory failed fact validation: " + ex.getMessage());
                }
        }

        private String buildPrompt(
                        Farmer farmer,
                        Crop crop,
                        WeatherForecastResponse weather,
                        RiskEngine.RiskResult riskResult,
                        String languageCode,
                        CropLifecycle lifecycle) {

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
                String dailySummary = daily == null ? "Daily forecast unavailable."
                                : String.format(Locale.US,
                                                "Rainfall probability %.0f%%, rainfall %.1f mm, max temperature %.1f°C, min temperature %.1f°C, wind %.1f km/h, evapotranspiration %.1f mm.",
                                                daily.precipitationProbabilityMax() == null
                                                                || daily.precipitationProbabilityMax().isEmpty()
                                                                                ? 0.0
                                                                                : daily.precipitationProbabilityMax()
                                                                                                .get(0),
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
                                                daily.evapotranspiration() == null
                                                                || daily.evapotranspiration().isEmpty()
                                                                                ? 0.0
                                                                                : daily.evapotranspiration().get(0));

                String riskSummary = riskResult == null
                                ? "Risk assessment unavailable."
                                : String.format(Locale.US, "Risk score %d, risk level %s, recommended action: %s.",
                                                riskResult.score(),
                                                riskResult.riskLevel(),
                                                riskResult.recommendedAction());

                String lifecycleGuidance = lifecycle == CropLifecycle.NOT_YET_PLANTED
                                ? "The crop is not planted yet. Do not describe existing plants, seedlings, crop stress, or routine irrigation. Choose useful preparation actions only if supported by the weather and other facts."
                                : "The crop has been planted or completed. Choose recommendations appropriate to its deterministic lifecycle status. Do not assume facts beyond the supplied data.";

                return "You are a helpful agricultural advisor speaking directly to an ordinary Indian farmer. "
                                + "Your job is to explain what this farmer should do based on the current conditions. "
                                + "Reply ONLY in " + languageName + ". "
                                + "\n\n"
                                + "STYLE:\n"
                                + "- Use simple everyday language a farmer would use in daily conversation.\n"
                                + "- Speak directly to the farmer (e.g., 'You should check...', 'Do not water now...').\n"
                                + "- Explain WHY an action is important using the facts provided.\n"
                                + "- Vary your wording naturally—do not use rigid templates.\n"
                                + "- Prioritize the most important action first.\n"
                                + "- Sound practical and based on real knowledge, not a textbook.\n"
                                + "- Use short sentences and common words.\n"
                                + "- Avoid bureaucratic or formal language.\n"
                                + "\n\n"
                                + "FACTUAL AUTHORITY:\n"
                                + "These backend facts are AUTHORITATIVE. Do not invent, guess, or change them:\n"
                                + "- Crop name, crop stage, location, weather values, forecast values\n"
                                + "- RiskEngine risk level, risk score, risk factors, recommended action\n"
                                + "- Any dates from the backend\n"
                                + "- The deterministic crop lifecycle status supplied by the backend\n"
                                + "\n\n"
                                + "SAFETY BOUNDARIES (Do NOT violate these):\n"
                                + "1. Never invent weather numbers. If you mention rainfall or temperature, use backend values only.\n"
                                + "2. Do NOT claim any disease exists unless the backend risk factors explicitly mention it.\n"
                                + "3. Do NOT recommend pesticides, fertilizers, or chemicals with specific dosages unless that information comes from the backend.\n"
                                + "   If the backend provides no treatment information, say: 'Specific treatment information is not available. If you notice a serious problem, contact a local agriculture officer.'\n"
                                + "4. Do NOT invent future dates or planting/harvest dates. Use only the crop stage provided.\n"
                                + "5. The backend RiskEngine is the authority for risk. Your recommendations must align with it and never contradict it.\n"
                                + "   - If backend risk is LOW, use INFO or ADVISORY severity.\n"
                                + "   - If backend risk is MODERATE, use ADVISORY or WARNING severity.\n"
                                + "   - If backend risk is HIGH or CRITICAL, you may use WARNING or URGENT, but not higher than the backend risk.\n"
                                + "\n\n"
                                + "GENERATE PRACTICAL RECOMMENDATIONS:\n"
                                + "- Each recommendation should be a clear action the farmer can take today or this week.\n"
                                + "- Base recommendations on the supplied facts. You may reason from these facts.\n"
                                + "   (Example: if HIGH risk + 80% rain probability, recommend checking field drains.)\n"
                                + "- Provide 1-4 recommendations, only as many as are useful for this situation.\n"
                                + "- Let titles and wording emerge naturally from the situation, not from a template.\n"
                                + "- Do not repeat the same wording across multiple recommendations.\n"
                                + "\n\n"
                                + "FARMER FACTS (Authoritative source of truth):\n"
                                + "- Location: " + location + "\n"
                                + "- Crop: " + crop.getCropName() + "\n"
                                + "- Current crop stage: "
                                + (crop.getCropStage() == null ? "Not set" : crop.getCropStage()) + "\n"
                                + "- Planting date: "
                                + (crop.getSowingDate() == null ? "Not set" : crop.getSowingDate()) + "\n"
                                + "- Expected harvest date: "
                                + (crop.getExpectedHarvestDate() == null ? "Not set" : crop.getExpectedHarvestDate())
                                + "\n"
                                + "- Deterministic lifecycle status: " + lifecycle + "\n"
                                + "- Lifecycle guidance: " + lifecycleGuidance + "\n"
                                + "- Land area: " + (farmer.getLandArea() == null ? "Not set" : farmer.getLandArea())
                                + " acres\n"
                                + "\n"
                                + "CURRENT WEATHER (Backend-provided, do not modify):\n"
                                + weatherText + "\n"
                                + "\n"
                                + "FORECAST SUMMARY (Backend-provided, do not modify):\n"
                                + dailySummary + "\n"
                                + "\n"
                                + "RISK ASSESSMENT (RiskEngine is authoritative):\n"
                                + riskSummary + "\n"
                                + "\n"
                                + "RESPONSE FORMAT:\n"
                                + "Return ONLY valid JSON (no markdown fences, no additional text):\n"
                                + "{\n"
                                + "  \"recommendations\": [\n"
                                + "    {\n"
                                + "      \"category\": \"string\",\n"
                                + "      \"severity\": \"INFO|ADVISORY|WARNING|URGENT\",\n"
                                + "      \"title\": \"string (short, action-oriented, naturally generated)\",\n"
                                + "      \"recommendation\": \"string (clear action for the farmer)\",\n"
                                + "      \"reason\": \"string (explain why using backend facts)\"\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}\n";
        }

        private String callGroq(String prompt) {

                if (groqApiKey == null || groqApiKey.isBlank()) {
                        throw new IllegalStateException(
                                        "GROQ_API_KEY is not configured");
                }

                try {
                        log.debug("Calling Groq API for advisory generation");

                        String requestBody = objectMapper.writeValueAsString(
                                        Map.of(
                                                        "model", GROQ_MODEL,

                                                        "messages", List.of(
                                                                        Map.of(
                                                                                        "role",
                                                                                        "system",
                                                                                        "content",
                                                                                        "You are an agricultural advisory AI. "
                                                                                                        + "Return ONLY valid JSON. "
                                                                                                        + "Never invent facts."),

                                                                        Map.of(
                                                                                        "role",
                                                                                        "user",
                                                                                        "content",
                                                                                        prompt)),

                                                        "max_completion_tokens",
                                                        1500,

                                                        "temperature",
                                                        0.7,

                                                        "response_format",
                                                        Map.of("type", "json_object")));

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(GROQ_API_URL))
                                        .header(
                                                        HttpHeaders.AUTHORIZATION,
                                                        "Bearer " + groqApiKey)
                                        .header(
                                                        HttpHeaders.CONTENT_TYPE,
                                                        MediaType.APPLICATION_JSON_VALUE)
                                        .POST(
                                                        HttpRequest.BodyPublishers.ofString(
                                                                        requestBody,
                                                                        StandardCharsets.UTF_8))
                                        .build();

                        log.debug("Groq API request prepared (prompt size: {} chars)", prompt.length());

                        HttpResponse<String> response = HttpClient.newHttpClient()
                                        .send(
                                                        request,
                                                        HttpResponse.BodyHandlers.ofString(
                                                                        StandardCharsets.UTF_8));

                        if (response.statusCode() >= 400) {

                                log.error(
                                                "Groq advisory request failed. Status: {}, Body: {}",
                                                response.statusCode(),
                                                response.body());

                                throw new IllegalStateException(
                                                "Groq API request failed with status "
                                                                + response.statusCode());
                        }

                        log.debug("Groq API response received with status {}", response.statusCode());

                        JsonNode body = objectMapper.readTree(response.body());

                        String content = body.path("choices")
                                        .path(0)
                                        .path("message")
                                        .path("content")
                                        .asText(null);

                        if (content == null || content.isBlank()) {

                                log.error(
                                                "Groq returned an empty advisory response: {}",
                                                response.body());

                                throw new IllegalStateException(
                                                "Groq returned an empty response");
                        }

                        log.debug("Groq advisory content received (size: {} chars)", content.length());

                        return content.trim();

                } catch (InterruptedException ex) {

                        Thread.currentThread().interrupt();
                        log.error("Groq request was interrupted");

                        throw new IllegalStateException(
                                        "Groq request interrupted",
                                        ex);

                } catch (Exception ex) {

                        log.error(
                                        "Groq advisory generation failed: {}",
                                        ex.getMessage());

                        throw new IllegalStateException(
                                        "AI advisory generation failed",
                                        ex);
                }
        }

        private List<AdvisoryRecommendation> parseRecommendations(String content) {
                String normalized = content == null ? "" : content.trim();
                if (normalized.isBlank()) {
                        log.warn("Empty content received from Groq");
                        return List.of();
                }

                normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
                if (normalized.isBlank()) {
                        log.warn("Content became empty after removing markdown fences");
                        return List.of();
                }

                try {
                        JsonNode root = objectMapper.readTree(normalized);
                        JsonNode recommendations = root.has("recommendations") ? root.get("recommendations") : root;
                        if (recommendations == null || !recommendations.isArray()) {
                                log.warn("Response does not contain recommendations array");
                                return List.of();
                        }

                        List<AdvisoryRecommendation> results = new ArrayList<>();
                        int count = 0;
                        for (JsonNode item : recommendations) {
                                if (!item.isObject()) {
                                        log.warn("Recommendation item is not an object: {}", item.getNodeType());
                                        continue;
                                }

                                try {
                                        String category = readRequiredString(item, "category");
                                        String severity = readRequiredString(item, "severity");
                                        String title = readRequiredString(item, "title");
                                        String recommendation = readRequiredString(item, "recommendation");
                                        String reason = readRequiredString(item, "reason");

                                        results.add(new AdvisoryRecommendation(category, severity, title,
                                                        recommendation, reason));
                                        count++;

                                        log.debug("Parsed recommendation {}: {} ({})", count, title, severity);
                                } catch (IllegalArgumentException e) {
                                        log.warn("Failed to parse recommendation item: {}", e.getMessage());
                                        continue;
                                }
                        }

                        log.debug("Successfully parsed {} recommendations from Groq response", results.size());
                        return results;

                } catch (Exception ex) {
                        log.error("Unable to parse Groq advisory JSON: {}", ex.getMessage());
                        log.debug("Raw response: {}", normalized, ex);
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
