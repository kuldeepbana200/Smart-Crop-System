package com.smartcrop.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MarketAdvisoryService {

    private final CropRepository cropRepository;
    private final MarketService marketService;
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public MarketAdvisoryService(
            CropRepository cropRepository,
            MarketService marketService,
            GroqClient groqClient,
            ObjectMapper objectMapper) {
        this.cropRepository = cropRepository;
        this.marketService = marketService;
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
    }

    public List<Advice> advise(Farmer farmer, String languageName) {
        List<CropContext> contexts = cropRepository.findByFarmerId(farmer.getId()).stream()
                .filter(crop -> crop.getCropName() != null && !crop.getCropName().isBlank())
                .map(crop -> context(farmer, crop))
                .toList();

        if (contexts.isEmpty()) {
            return List.of();
        }

        String prompt = buildPrompt(farmer, languageName, contexts);
        try {
            String content = groqClient.complete(
                    "You are a practical agricultural market advisor. Return only valid JSON.",
                    prompt,
                    900,
                    0.2);
            List<Advice> generated = parse(content);
            return contexts.stream()
                    .map(context -> generated.stream()
                            .filter(advice -> advice.crop().equalsIgnoreCase(context.cropName()))
                            .findFirst()
                            .orElse(fallback(context)))
                    .toList();
        } catch (Exception exception) {
            return contexts.stream().map(this::fallback).toList();
        }
    }

    private CropContext context(Farmer farmer, Crop crop) {
        List<MarketPriceResponse> prices = marketService.getPrices(
                crop.getCropName(), farmer.getDistrict(), farmer.getState());
        LocalDate today = LocalDate.now();
        List<MarketPriceResponse> history = marketService.getPriceHistory(
                crop.getCropName(), farmer.getState(), today.minusDays(30), today);
        List<Double> modalPrices = prices.stream()
                .map(price -> price.getModalPrice())
                .filter(Objects::nonNull)
                .toList();
        Double best = modalPrices.stream().max(Double::compareTo).orElse(null);
        Double lowest = modalPrices.stream().min(Double::compareTo).orElse(null);
        String scope = prices.stream().anyMatch(price -> same(price.getDistrict(), farmer.getDistrict()))
                ? "exact district and state"
                : prices.stream().anyMatch(price -> same(price.getState(), farmer.getState()))
                        ? "available state markets"
                        : prices.isEmpty() ? "no verified data" : "available verified markets";
        return new CropContext(crop.getCropName(), scope, best, lowest,
                prices.stream().limit(3).toList(), history.stream().limit(3).toList());
    }

    private String buildPrompt(Farmer farmer, String languageName, List<CropContext> contexts) {
        String facts = contexts.stream().map(context -> {
            String observations = context.prices().stream()
                    .map(price -> String.format(Locale.US, "%s: modal %s, min %s, max %s, date %s",
                            price.getMarket(), price.getModalPrice(), price.getMinPrice(), price.getMaxPrice(),
                            price.getArrivalDate()))
                    .collect(Collectors.joining("; "));
            return String.format(Locale.US,
                    "Crop=%s | scope=%s | best=%s | lowest=%s | observations=%s | history=%s",
                    context.cropName(), context.scope(), value(context.best()), value(context.lowest()),
                    observations.isBlank() ? "NOT AVAILABLE" : observations,
                    context.history().isEmpty() ? "NOT AVAILABLE"
                            : context.history().stream()
                                    .map(price -> price.getArrivalDate() + ":" + price.getModalPrice())
                                    .collect(Collectors.joining(", ")));
        }).collect(Collectors.joining("\n"));

        return "You are a practical agricultural market advisor helping an ordinary farmer. "
                + "Reply in " + languageName + " using natural, concise language. "
                + "The following are verified backend market facts for the farmer at "
                + farmer.getDistrict() + ", " + farmer.getState() + ". "
                + "Explain what each crop's available market information means and what the farmer should consider before selling. "
                + "Do not invent prices, markets, dates, trends, or predictions. For a crop with no verified data, clearly say its price is unavailable and advise checking today's local mandi rate, nearby markets, transport costs, market charges, and quality. "
                + "Return exactly one insight for every crop and do not omit crops. Return only JSON in this shape: {\"insights\":[{\"crop\":\"...\",\"summary\":\"...\",\"trend\":\"...\",\"advice\":\"...\",\"caution\":\"...\"}]}\n"
                + facts;
    }

    private List<Advice> parse(String content) throws Exception {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        JsonNode root = objectMapper.readTree(content.trim());
        JsonNode insights = root.has("insights") ? root.get("insights") : root.get("marketAdvice");
        if (insights == null || !insights.isArray()) {
            return List.of();
        }
        List<Advice> result = new ArrayList<>();
        for (JsonNode node : insights) {
            if (node.isObject() && node.hasNonNull("crop") && node.hasNonNull("advice")) {
                result.add(new Advice(
                        node.get("crop").asText(),
                        text(node, "summary"),
                        text(node, "trend"),
                        node.get("advice").asText(),
                        text(node, "caution"),
                        false));
            }
        }
        return result;
    }

    private Advice fallback(CropContext context) {
        if (context.prices().isEmpty()) {
            return new Advice(context.cropName(),
                    "Verified price data is unavailable for this crop.",
                    "Trend unavailable",
                    "Check today's mandi rate, compare nearby markets, and consider transport, selling charges, and produce quality before selling.",
                    "General guidance - verify today's local mandi rate.", true);
        }
        return new Advice(context.cropName(),
                "Verified market prices are available for this crop.",
                "Trend based on available observations",
                "Compare the available market prices before selling. If a higher-priced market is farther away, check whether the extra price covers transport and selling costs.",
                "General guidance - verify today's local mandi rate.", true);
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : "Not available";
    }

    private String value(Double value) {
        return value == null ? "NOT AVAILABLE" : value.toString();
    }

    private boolean same(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private record CropContext(String cropName, String scope, Double best, Double lowest,
            List<MarketPriceResponse> prices, List<MarketPriceResponse> history) {
    }

    public record Advice(String crop, String summary, String trend, String advice, String caution, boolean fallback) {
    }
}
