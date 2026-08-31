package com.smartcrop.ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.ai.dto.AiTestResponse;
import com.smartcrop.ai.service.YouTubeService;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;
import com.smartcrop.risk.service.RiskService;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "openai/gpt-oss-20b";

    private final String groqApiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final WeatherService weatherService;
    private final RiskService riskService;
    private final DistressAlertService distressAlertService;
    private final MarketService marketService;
    private final YouTubeService youTubeService;

    public AiController(
            @Value("${app.groq.api-key:}") String groqApiKey,
            UserRepository userRepository,
            FarmerRepository farmerRepository,
            CropRepository cropRepository,
            WeatherService weatherService,
            RiskService riskService,
            DistressAlertService distressAlertService,
            MarketService marketService,
            YouTubeService youTubeService) {
        this.groqApiKey = groqApiKey;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.weatherService = weatherService;
        this.riskService = riskService;
        this.distressAlertService = distressAlertService;
        this.marketService = marketService;
        this.youTubeService = youTubeService;
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<AiTestResponse> testAi(Authentication authentication) {
        String prompt = "You are a helpful agricultural advisor for a farmer in India. " +
                "Give a brief, practical response in plain language about how to improve soil health and reduce pest pressure "
                +
                "for a smallholder farmer. Mention a simple action plan with 3 points and keep the answer concise.";

        String answer = askGroq(prompt);
        if (answer == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AiTestResponse(
                    prompt,
                    "Groq API key is not configured. Set GROQ_API_KEY before calling this endpoint.",
                    List.of()));
        }

        return ResponseEntity.ok(new AiTestResponse(prompt, answer, List.of()));
    }

    @PostMapping("/personalized-advice")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<AiTestResponse> personalizedAdvice(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Farmer profile not found"));

        String languageCode = Optional.ofNullable(user.getPreferredLanguage()).orElse("en");
        String languageName = switch (languageCode.toLowerCase(Locale.ROOT)) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            case "or" -> "Odia";
            default -> "English";
        };

        String location = String.format("%s, %s",
                Optional.ofNullable(farmer.getDistrict()).orElse("Not set"),
                Optional.ofNullable(farmer.getState()).orElse("Not set"));

        List<Crop> crops = cropRepository.findByFarmerId(farmer.getId());
        List<String> cropSummaries = new ArrayList<>();
        Map<String, List<MarketPriceResponse>> marketSummaries = new LinkedHashMap<>();

        for (Crop crop : crops.stream().limit(5).toList()) {
            String cropSummary = String.format(Locale.US,
                    "Crop: %s | Stage: %s | Sowing: %s | Expected harvest: %s",
                    crop.getCropName(),
                    crop.getCropStage() == null ? "Not set" : crop.getCropStage(),
                    crop.getSowingDate() == null ? "Not set" : crop.getSowingDate(),
                    crop.getExpectedHarvestDate() == null ? "Not set" : crop.getExpectedHarvestDate());
            cropSummaries.add(cropSummary);

            if (crop.getCropName() != null && !crop.getCropName().isBlank()) {
                List<MarketPriceResponse> marketPrices = marketService.getPrices(
                        crop.getCropName(),
                        farmer.getDistrict(),
                        farmer.getState());
                marketSummaries.put(crop.getCropName(), marketPrices.stream().limit(3).toList());
            }
        }

        CurrentWeatherResponse currentWeather = null;
        try {
            currentWeather = weatherService.getCurrentWeather(authentication);
        } catch (Exception ignored) {
            currentWeather = null;
        }

        List<DistressAlertResponse> alerts = new ArrayList<>();
        try {
            alerts = distressAlertService.getFarmerAlerts(authentication);
        } catch (Exception ignored) {
            alerts = List.of();
        }

        String prompt = buildPersonalizedPrompt(
                user,
                farmer,
                languageName,
                location,
                cropSummaries,
                currentWeather,
                alerts,
                marketSummaries);

        String answer = askGroq(prompt);
        if (answer == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AiTestResponse(
                    prompt,
                    "Groq API key is not configured. Set GROQ_API_KEY before calling this endpoint.",
                    List.of()));
        }

        return ResponseEntity.ok(new AiTestResponse(prompt, answer, List.of()));
    }

    @PostMapping("/market-advice")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<?> marketAdvice(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Farmer profile not found"));

        String languageCode = Optional.ofNullable(user.getPreferredLanguage()).orElse("en");
        String languageName = switch (languageCode.toLowerCase(Locale.ROOT)) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            case "or" -> "Odia";
            default -> "English";
        };

        String location = String.format("%s, %s",
                Optional.ofNullable(farmer.getDistrict()).orElse("Not set"),
                Optional.ofNullable(farmer.getState()).orElse("Not set"));

        List<Crop> crops = cropRepository.findByFarmerId(farmer.getId());
        if (crops.isEmpty()) {
            return ResponseEntity.ok(List.of(new MarketAdviceResponse(
                    "No crop data",
                    "Market data is unavailable because no crops are recorded for this farmer.",
                    "Trend unavailable",
                    "Add crop details to receive market guidance.",
                    "Market data is unavailable until crop information is present.")));
        }

        List<MarketAdviceResponse> results = new ArrayList<>();
        for (Crop crop : crops.stream().limit(5).toList()) {
            String cropName = crop.getCropName();
            if (cropName == null || cropName.isBlank()) {
                continue;
            }

            List<MarketPriceResponse> livePrices = marketService.getPrices(
                    cropName,
                    farmer.getDistrict(),
                    farmer.getState());

            List<MarketPriceResponse> priceHistory = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(30);
            try {
                priceHistory = marketService.getPriceHistory(
                        cropName,
                        farmer.getState(),
                        startDate,
                        today);
            } catch (Exception ignored) {
                priceHistory = List.of();
            }

            String trendText = calculatePriceTrend(priceHistory);
            String prompt = buildMarketAdvicePrompt(
                    user,
                    farmer,
                    languageName,
                    location,
                    cropName,
                    livePrices,
                    priceHistory,
                    trendText);

            String content = askGroq(prompt);
            if (content == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "error",
                        "Groq API key is not configured. Set GROQ_API_KEY before calling this endpoint."));
            }

            List<MarketAdviceResponse> parsedAdvice = parseMarketAdviceResponse(content, cropName);
            if (parsedAdvice.isEmpty()) {
                results.add(new MarketAdviceResponse(
                        cropName,
                        "Market data is available, but AI insight could not be generated from the current response.",
                        trendText,
                        "Compare local market prices before selling and confirm the latest available rates.",
                        "Market information is limited and should be verified before acting."));
            } else {
                results.addAll(parsedAdvice);
            }
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/education")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<?> education(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Farmer profile not found"));

        String languageCode = Optional.ofNullable(user.getPreferredLanguage()).orElse("en");
        String languageName = switch (languageCode.toLowerCase(Locale.ROOT)) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            case "or" -> "Odia";
            default -> "English";
        };

        String location = String.format("%s, %s",
                Optional.ofNullable(farmer.getDistrict()).orElse("Not set"),
                Optional.ofNullable(farmer.getState()).orElse("Not set"));

        List<Crop> crops = cropRepository.findByFarmerId(farmer.getId());
        List<String> cropSummaries = new ArrayList<>();

        for (Crop crop : crops.stream().limit(5).toList()) {
            cropSummaries.add(String.format(Locale.US,
                    "Crop: %s | Stage: %s | Sowing: %s | Expected harvest: %s",
                    crop.getCropName(),
                    crop.getCropStage() == null ? "Not set" : crop.getCropStage(),
                    crop.getSowingDate() == null ? "Not set" : crop.getSowingDate(),
                    crop.getExpectedHarvestDate() == null ? "Not set" : crop.getExpectedHarvestDate()));
        }

        CurrentWeatherResponse currentWeather = null;
        try {
            currentWeather = weatherService.getCurrentWeather(authentication);
        } catch (Exception ignored) {
            currentWeather = null;
        }

        List<DistressAlertResponse> alerts = new ArrayList<>();
        try {
            alerts = distressAlertService.getFarmerAlerts(authentication);
        } catch (Exception ignored) {
            alerts = List.of();
        }

        String prompt = buildEducationPrompt(
                user,
                farmer,
                languageName,
                location,
                cropSummaries,
                currentWeather,
                alerts);

        String content = askGroq(prompt);
        if (content == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error",
                    "Groq API key is not configured. Set GROQ_API_KEY before calling this endpoint."));
        }

        try {
            log.info("Raw Groq education response: {}", content);
            List<EducationTopic> topics = parseEducationTopics(content);

            if (!youTubeService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "error",
                        "YOUTUBE_API_KEY is not configured in the backend environment. YouTube video enrichment is unavailable."));
            }

            List<EducationTopic> enrichedTopics = youTubeService.enrichTopicsWithVideos(topics);
            return ResponseEntity.ok(enrichedTopics);
        } catch (Exception ex) {
            log.error("Failed to parse Groq education response. Raw response: {}", content, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error",
                    "Failed to parse Groq education JSON: " + ex.getMessage(),
                    "rawResponse",
                    content));
        }
    }

    private String buildMarketAdvicePrompt(
            User user,
            Farmer farmer,
            String languageName,
            String location,
            String cropName,
            List<MarketPriceResponse> livePrices,
            List<MarketPriceResponse> priceHistory,
            String trendText) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a practical agricultural market advisor for Indian farmers. ");
        prompt.append("Reply ONLY in ").append(languageName)
                .append(" and keep the answer short, clear, and farmer-friendly. ");
        prompt.append(
                "Critical rules: never invent prices, market names, future prices, guaranteed profits, waiting periods, or market movements. Use only the real values provided below. If a required market or price input is missing, explicitly say it is unavailable. ");
        prompt.append("Farmer name: ").append(user.getName()).append(". ");
        prompt.append("Selected language: ").append(languageName).append(". ");
        prompt.append("Location: ").append(location).append(". ");
        prompt.append("Crop: ").append(cropName).append(". ");
        prompt.append("Current market prices: ");
        if (livePrices == null || livePrices.isEmpty()) {
            prompt.append("Current market prices unavailable. ");
        } else {
            for (MarketPriceResponse price : livePrices.stream().limit(5).toList()) {
                prompt.append(String.format(Locale.US,
                        "Market %s in %s, %s: modal %s %s, min %s %s, max %s %s, date %s; ",
                        price.getMarket(),
                        price.getDistrict(),
                        price.getState(),
                        price.getModalPrice(),
                        price.getCurrency() == null ? "INR" : price.getCurrency(),
                        price.getMinPrice(),
                        price.getCurrency() == null ? "INR" : price.getCurrency(),
                        price.getMaxPrice(),
                        price.getCurrency() == null ? "INR" : price.getCurrency(),
                        price.getArrivalDate() == null ? "Not available" : price.getArrivalDate()));
            }
        }

        prompt.append("Available price history: ");
        if (priceHistory == null || priceHistory.isEmpty()) {
            prompt.append("Price history unavailable. ");
        } else {
            prompt.append(String.format(Locale.US,
                    "%s; ",
                    trendText));
            for (MarketPriceResponse price : priceHistory.stream()
                    .sorted(Comparator.comparing(MarketPriceResponse::getArrivalDate)).limit(5).toList()) {
                prompt.append(String.format(Locale.US,
                        "%s:%s, ",
                        price.getArrivalDate() == null ? "date unavailable" : price.getArrivalDate(),
                        price.getModalPrice()));
            }
        }

        prompt.append(
                "Return ONLY valid JSON in this exact shape: {\"marketAdvice\":[{\"crop\":\"string\",\"summary\":\"string\",\"trend\":\"string\",\"advice\":\"string\",\"caution\":\"string\"}]}. Do not output markdown fences or extra text. The summary should explain the current real market situation in this farmer's region. The trend must be based only on the actual price history provided above. The advice should suggest a practical action using available data only. The caution should say that price data is unavailable or that current market values should be verified before selling if the backend data is limited. ");
        return prompt.toString();
    }

    private List<MarketAdviceResponse> parseMarketAdviceResponse(String content, String fallbackCrop) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        normalized = normalized.strip();
        if (normalized.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(normalized);
            JsonNode marketAdvice = root.has("marketAdvice") ? root.get("marketAdvice") : root;
            if (marketAdvice == null || !marketAdvice.isArray()) {
                return List.of();
            }

            List<MarketAdviceResponse> result = new ArrayList<>();
            for (JsonNode node : marketAdvice) {
                if (!node.isObject()) {
                    continue;
                }
                String crop = readOptionalString(node, "crop", fallbackCrop);
                String summary = readRequiredString(node, "summary");
                String trend = readRequiredString(node, "trend");
                String advice = readRequiredString(node, "advice");
                String caution = readRequiredString(node, "caution");
                result.add(new MarketAdviceResponse(crop, summary, trend, advice, caution));
            }
            return result;
        } catch (Exception ex) {
            log.warn("Unable to parse market advice JSON. Raw response: {}", normalized, ex);
            return List.of();
        }
    }

    private String readOptionalString(JsonNode root, String fieldName, String fallbackValue) {
        JsonNode field = root.get(fieldName);
        if (field == null || field.isNull() || field.asText() == null || field.asText().isBlank()) {
            return fallbackValue;
        }
        return field.asText().trim();
    }

    private String calculatePriceTrend(List<MarketPriceResponse> priceHistory) {
        if (priceHistory == null || priceHistory.isEmpty()) {
            return "Trend unavailable: price history is not available.";
        }

        List<MarketPriceResponse> validEntries = priceHistory.stream()
                .filter(price -> price != null && price.getModalPrice() != null && price.getArrivalDate() != null)
                .sorted(Comparator.comparing(MarketPriceResponse::getArrivalDate))
                .toList();

        if (validEntries.size() < 2) {
            return "Trend unavailable: not enough historical dates are available.";
        }

        double first = validEntries.get(0).getModalPrice();
        double last = validEntries.get(validEntries.size() - 1).getModalPrice();
        if (first <= 0 || last <= 0) {
            return "Trend unavailable: price values are invalid.";
        }

        double percentChange = ((last - first) / first) * 100.0;
        if (Math.abs(percentChange) < 1.0) {
            return "Trend stable: price change is under 1% across the available dates.";
        }

        String direction = percentChange > 0 ? "up" : "down";
        return String.format(Locale.US,
                "Trend %s by %.1f%% across the available dates.",
                direction,
                Math.abs(percentChange));
    }

    private String buildPersonalizedPrompt(
            User user,
            Farmer farmer,
            String languageName,
            String location,
            List<String> cropSummaries,
            CurrentWeatherResponse currentWeather,
            List<DistressAlertResponse> alerts,
            Map<String, List<MarketPriceResponse>> marketSummaries) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a practical agricultural advisor for Indian farmers. ");
        prompt.append("Reply ONLY in ").append(languageName)
                .append(" and keep the answer simple, actionable, and farmer-friendly. ");
        prompt.append(
                "Critical rules: never invent weather forecasts, market prices, crop information, pesticide names, pesticide dosages, fertilizer quantities, or dates. If any required information is unavailable, explicitly say the information is unavailable. Do not recommend a chemical pesticide or fertilizer with a specific dosage unless the exact product, dosage, and source are present in the provided farmer context or an existing trusted agricultural rule in the backend. Use only the facts given below. ");
        prompt.append("Farmer name: ").append(user.getName()).append(". ");
        prompt.append("Selected language: ").append(languageName).append(". ");
        prompt.append("Profile/location: ").append(location).append(". ");
        prompt.append("Land area: ").append(farmer.getLandArea() == null ? "Not set" : farmer.getLandArea())
                .append(" acres. ");
        prompt.append("Current weather: ");
        if (currentWeather == null) {
            prompt.append("Weather data unavailable. ");
        } else {
            prompt.append(String.format(Locale.US,
                    "temperature %.1f°C, humidity %.0f%%, rainfall %.1f mm, wind %.1f km/h, weather code %s. ",
                    currentWeather.temperature(),
                    currentWeather.relativeHumidity(),
                    currentWeather.precipitation(),
                    currentWeather.windSpeed(),
                    currentWeather.weatherCode()));
        }

        prompt.append("Crops: ");
        if (cropSummaries.isEmpty()) {
            prompt.append("No crops recorded yet. ");
        } else {
            for (String cropSummary : cropSummaries) {
                prompt.append(cropSummary).append("; ");
            }
        }

        prompt.append("Active risks and alerts: ");
        if (alerts == null || alerts.isEmpty()) {
            prompt.append("No open or acknowledged alerts. ");
        } else {
            for (DistressAlertResponse alert : alerts.stream().limit(3).toList()) {
                prompt.append(String.format(Locale.US,
                        "Crop %s has risk level %s and dominant factor %s; ",
                        alert.cropName(),
                        alert.riskLevel(),
                        alert.dominantFactor()));
            }
        }

        prompt.append("Relevant market prices: ");
        if (marketSummaries.isEmpty()) {
            prompt.append("No market data available. ");
        } else {
            for (Map.Entry<String, List<MarketPriceResponse>> entry : marketSummaries.entrySet()) {
                List<MarketPriceResponse> prices = entry.getValue();
                if (prices == null || prices.isEmpty()) {
                    continue;
                }
                prompt.append(String.format(Locale.US,
                        "%s: %s; ",
                        entry.getKey(),
                        prices.stream()
                                .limit(3)
                                .map(price -> String.format(Locale.US,
                                        "%s market %s modal %s %s",
                                        price.getMarket(),
                                        price.getDistrict(),
                                        price.getModalPrice(),
                                        price.getCurrency() == null ? "INR" : price.getCurrency()))
                                .toList()));
            }
        }

        prompt.append(
                "Give a short, practical advisory recommendation with 3 clear actions, suggest the best next step for crop care, and mention any market or weather caution. If the needed weather or market information is unavailable, say so explicitly instead of guessing. Do not give product names, specific doses, or chemical recommendations unless a trusted source or existing backend rule provides them. Use the farmer's selected language and keep it concise but useful.");
        return prompt.toString();
    }

    private String buildEducationPrompt(
            User user,
            Farmer farmer,
            String languageName,
            String location,
            List<String> cropSummaries,
            CurrentWeatherResponse currentWeather,
            List<DistressAlertResponse> alerts) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an agricultural education assistant for Indian farmers. ");
        prompt.append(
                "Return ONLY valid JSON in this exact shape: {\"topics\":[{\"title\":\"string\",\"reason\":\"string\",\"searchQuery\":\"string\"}]}. ");
        prompt.append(
                "Use exactly 3 to 5 topics. Do not use markdown fences such as ```json. Do not include any text before or after the JSON. ");
        prompt.append("Reply in ").append(languageName).append(". ");
        prompt.append(
                "Do not invent URLs, articles, videos, product names, pesticide names, fertilizer brands, dates, or dosages. ");
        prompt.append(
                "If some context is missing, say that clearly in the reason field and use a searchQuery that reflects the missing information. ");
        prompt.append("Farmer name: ").append(user.getName()).append(". ");
        prompt.append("Location: ").append(location).append(". ");
        prompt.append("Land area: ").append(farmer.getLandArea() == null ? "Not set" : farmer.getLandArea())
                .append(" acres. ");
        prompt.append("Current weather: ");
        if (currentWeather == null) {
            prompt.append("Weather data unavailable. ");
        } else {
            prompt.append(String.format(Locale.US,
                    "temperature %.1f°C, humidity %.0f%%, rainfall %.1f mm, wind %.1f km/h, weather code %s. ",
                    currentWeather.temperature(),
                    currentWeather.relativeHumidity(),
                    currentWeather.precipitation(),
                    currentWeather.windSpeed(),
                    currentWeather.weatherCode()));
        }

        prompt.append("Crop details: ");
        if (cropSummaries.isEmpty()) {
            prompt.append("No crop data available. ");
        } else {
            for (String cropSummary : cropSummaries) {
                prompt.append(cropSummary).append("; ");
            }
        }

        prompt.append("Active risks and alerts: ");
        if (alerts == null || alerts.isEmpty()) {
            prompt.append("No active alerts recorded. ");
        } else {
            for (DistressAlertResponse alert : alerts.stream().limit(3).toList()) {
                prompt.append(String.format(Locale.US,
                        "Crop %s risk %s due to %s; ",
                        alert.cropName(),
                        alert.riskLevel(),
                        alert.dominantFactor()));
            }
        }

        prompt.append(
                "Recommend practical education topics that help this farmer improve crop health and manage risk using this farmer's real crops and current conditions. Make each reason short and specific to this farmer. The searchQuery must be a plain Google/YouTube-friendly search phrase only, with no URL and no article/video link. Output only the JSON object with a topics array, no markdown fences.");
        return prompt.toString();
    }

    private List<EducationTopic> parseEducationTopics(String content) throws Exception {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Groq returned empty response");
        }

        normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        normalized = normalized.strip();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Groq returned blank response after stripping markdown fences");
        }

        JsonNode root = objectMapper.readTree(normalized);
        if (!root.isObject() || !root.has("topics") || !root.get("topics").isArray()) {
            throw new IllegalArgumentException("Expected top-level JSON object with a topics array");
        }

        JsonNode topics = root.get("topics");
        if (topics.size() < 3 || topics.size() > 5) {
            throw new IllegalArgumentException("Expected 3 to 5 topics, received " + topics.size());
        }

        List<EducationTopic> result = new ArrayList<>();
        for (JsonNode topicNode : topics) {
            if (!topicNode.isObject()) {
                throw new IllegalArgumentException("Each topic must be an object");
            }

            String title = readRequiredString(topicNode, "title");
            String reason = readRequiredString(topicNode, "reason");
            String searchQuery = readRequiredString(topicNode, "searchQuery");
            result.add(new EducationTopic(title, reason, searchQuery));
        }

        return result;
    }

    private String readRequiredString(JsonNode root, String fieldName) {
        JsonNode field = root.get(fieldName);
        if (field == null || field.isNull() || field.asText() == null || field.asText().isBlank()) {
            throw new IllegalArgumentException("Missing or empty field: " + fieldName);
        }
        return field.asText().trim();
    }

    private String askGroq(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return null;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", GROQ_MODEL,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are a helpful Indian agricultural education assistant."),
                            Map.of(
                                    "role", "user",
                                    "content", prompt)),
                    "max_completion_tokens", 2048,
                    "temperature", 0.2,
                    "reasoning_effort", "low",
                    "include_reasoning", false,
                    "response_format", Map.of(
                            "type", "json_object")));

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
            String rawResponse = body.path("choices").path(0).path("message").path("content")
                    .asText("No response text generated.");
            log.info("Raw Groq response: {}", rawResponse);
            return rawResponse;
        } catch (Exception ex) {
            throw new IllegalStateException("AI generation failed: " + ex.getMessage(), ex);
        }
    }

    public record MarketAdviceResponse(String crop, String summary, String trend, String advice, String caution) {
    }

    public record EducationTopic(String title, String reason, String searchQuery, List<YouTubeVideo> videos) {
        public EducationTopic(String title, String reason, String searchQuery) {
            this(title, reason, searchQuery, List.of());
        }
    }

    public record YouTubeVideo(String videoId, String title, String description, String thumbnail,
            String channelTitle, String url) {
    }
}
