package com.smartcrop.market.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.market.dto.MarketPriceResponse;
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
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class GroqMarketDataService {

    private static final Logger log = LoggerFactory.getLogger(GroqMarketDataService.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "openai/gpt-oss-20b";

    private final String groqApiKey;
    private final CropRepository cropRepository;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public GroqMarketDataService(
            @Value("${app.groq.api-key:}") String groqApiKey,
            CropRepository cropRepository,
            ObjectMapper objectMapper) {
        this.groqApiKey = groqApiKey;
        this.cropRepository = cropRepository;
        this.objectMapper = objectMapper;
    }

    public List<MarketPriceResponse> getPricesForFarmer(
            Farmer farmer,
            String cropName,
            String district,
            String state) {

        if (farmer == null || farmer.getId() == null) {
            return List.of();
        }

        String effectiveState = normalize(farmer.getState(), state);
        String effectiveDistrict = normalize(farmer.getDistrict(), district);
        List<String> cropNames = resolveCropNames(farmer, cropName);

        if (cropNames.isEmpty()) {
            return List.of();
        }

        final String cacheKey = buildCacheKey(farmer.getId(), cropNames, effectiveDistrict, effectiveState);
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.expired()) {
            log.info("Returning cached Groq market dataset for farmer {} and crops {}", farmer.getId(), cropNames);
            return cached.values();
        }

        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("Groq market generation skipped because GROQ_API_KEY is not configured for farmer {}",
                    farmer.getId());
            return List.of();
        }

        List<MarketPriceResponse> generated = generateMarketData(farmer, cropNames, effectiveDistrict, effectiveState);
        if (generated.isEmpty()) {
            return List.of();
        }

        cache.put(cacheKey, new CacheEntry(generated, LocalDate.now().plusDays(1)));
        return generated;
    }

    private List<MarketPriceResponse> generateMarketData(
            Farmer farmer,
            List<String> cropNames,
            String district,
            String state) {

        String languageCode = Optional.ofNullable(farmer.getUser())
                .map(user -> user.getPreferredLanguage())
                .filter(value -> value != null && !value.isBlank())
                .orElse("en");

        String prompt = buildPrompt(farmer, cropNames, district, state, languageCode);
        String response = callGroq(prompt);
        if (response == null || response.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode pricesNode = root.has("prices") ? root.get("prices") : root;
            if (pricesNode == null || !pricesNode.isArray()) {
                return List.of();
            }

            List<MarketPriceResponse> result = new ArrayList<>();
            for (JsonNode item : pricesNode) {
                if (!item.isObject()) {
                    continue;
                }

                try {
                    MarketPriceResponse record = parseRecord(item, district, state, cropNames);
                    if (record != null) {
                        result.add(record);
                    }
                } catch (IllegalArgumentException ex) {
                    log.warn("Rejecting malformed Groq market price: {} | reason: {}", item, ex.getMessage());
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("Failed to parse Groq market dataset. Raw response: {}", response, ex);
            return List.of();
        }
    }

    private String buildPrompt(
            Farmer farmer,
            List<String> cropNames,
            String district,
            String state,
            String languageCode) {

        String languageName = switch (languageCode.toLowerCase(Locale.ROOT)) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            case "or" -> "Odia";
            default -> "English";
        };

        String cropsText = cropNames.stream().sorted().map(name -> name.trim()).filter(value -> !value.isBlank())
                .distinct().collect(Collectors.joining(", "));

        return "You are generating a market-estimation dataset for an Indian farmer. " +
                "The output must be STRICT JSON with a top-level object containing a 'prices' array. " +
                "Do not claim these are verified live mandi prices. These are informational AI-generated estimates only. "
                +
                "Use the farmer's real context and only generate values for the requested crops and locations. " +
                "The farmer selected language is " + languageName + ". " +
                "Farmer district: " + safeValue(district) + ". Farmer state: " + safeValue(state) + ". " +
                "Requested crops: " + safeValue(cropsText) + ". " +
                "Current date: " + LocalDate.now() + ". " +
                "Critical rules: " +
                "- Do not claim these are verified live mandi prices. " +
                "- If a value cannot be known reliably, return null or 'unavailable' instead of inventing confidence. "
                +
                "- Return only structured JSON, no markdown fences and no explanation. " +
                "- Generate realistic informational estimates only for the requested crops and nearby relevant local markets. "
                +
                "- Do not generate arbitrary crops, markets, or locations unrelated to this farmer. " +
                "- Each record must include: crop, market, district, state, price, unit, date, source. " +
                "- Use 'source': 'AI-generated estimate'. " +
                "- The 'price' must be numeric and positive. " +
                "- Use the same date format as YYYY-MM-DD. " +
                "- Keep unit as 'quintal' or a realistic agricultural unit. " +
                "- Use district/state names matching the farmer context. " +
                "Example output shape: {\"prices\":[{\"crop\":\"Rice\",\"market\":\"Sundargarh\",\"district\":\"Sundargarh\",\"state\":\"Odisha\",\"price\":2250,\"unit\":\"quintal\",\"date\":\"2026-08-31\",\"source\":\"AI-generated estimate\"}]}";
    }

    private String callGroq(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return null;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", GROQ_MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You generate only valid JSON for agricultural market estimates."),
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
                throw new IllegalStateException(
                        "Groq API request failed with status " + response.statusCode() + ": " + response.body());
            }

            JsonNode body = objectMapper.readTree(response.body());
            String content = body.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                return null;
            }
            return content.trim();
        } catch (Exception ex) {
            log.warn("Groq market generation failed", ex);
            return null;
        }
    }

    private MarketPriceResponse parseRecord(JsonNode item, String district, String state, List<String> cropNames) {
        String crop = readString(item, "crop", cropNames.get(0));
        String market = readString(item, "market", "Local Market");
        String recordDistrict = readString(item, "district", district);
        String recordState = readString(item, "state", state);
        String unit = readString(item, "unit", "quintal");
        String source = readString(item, "source", "AI-generated estimate");
        String dateText = readString(item, "date", LocalDate.now().toString());

        LocalDate arrivalDate;
        try {
            arrivalDate = LocalDate.parse(dateText);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date: " + dateText, ex);
        }

        Double priceValue = readDouble(item, "price");
        if (priceValue == null || !Double.isFinite(priceValue) || priceValue <= 0) {
            throw new IllegalArgumentException("Invalid or non-positive price value");
        }

        if (crop == null || crop.isBlank() || market == null || market.isBlank() ||
                recordDistrict == null || recordDistrict.isBlank() ||
                recordState == null || recordState.isBlank() ||
                unit == null || unit.isBlank() ||
                source == null || source.isBlank()) {
            throw new IllegalArgumentException("Missing required market field");
        }

        MarketPriceResponse response = new MarketPriceResponse();
        response.setCommodity(crop);
        response.setMarket(market);
        response.setDistrict(recordDistrict);
        response.setState(recordState);
        response.setArrivalDate(arrivalDate);
        response.setModalPrice(priceValue);
        response.setMinPrice(Math.max(0.0, priceValue * 0.9));
        response.setMaxPrice(priceValue * 1.1);
        response.setUnit(unit);
        response.setCurrency("INR");
        response.setSource(source);
        return response;
    }

    private String readString(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return defaultValue;
        }
        String value = field.asText();
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private Double readDouble(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isNumber()) {
            return field.doubleValue();
        }
        try {
            return Double.parseDouble(field.asText());
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> resolveCropNames(Farmer farmer, String cropName) {
        List<String> names = new ArrayList<>();
        if (cropName != null && !cropName.isBlank()) {
            names.add(cropName.trim());
            return names;
        }

        if (farmer == null || farmer.getId() == null) {
            return names;
        }

        List<Crop> crops = cropRepository.findByFarmerId(farmer.getId());
        for (Crop crop : crops) {
            if (crop != null && crop.getCropName() != null && !crop.getCropName().isBlank()) {
                names.add(crop.getCropName().trim());
            }
        }

        return names.stream().distinct().sorted().toList();
    }

    private String normalize(String preferred, String fallback) {
        String candidate = preferred != null && !preferred.isBlank() ? preferred : fallback;
        return candidate == null || candidate.isBlank() ? "Not set" : candidate.trim();
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "not available" : value;
    }

    private String buildCacheKey(Long farmerId, List<String> cropNames, String district, String state) {
        return farmerId + "|" + cropNames.stream().sorted().distinct().collect(Collectors.joining(",")) + "|" + district
                + "|" + state;
    }

    private record CacheEntry(List<MarketPriceResponse> values, LocalDate expiresAt) {
        boolean expired() {
            return LocalDate.now().isAfter(expiresAt);
        }
    }
}
