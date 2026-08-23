package com.smartcrop.risk.engine;

import com.smartcrop.crop.entity.Crop;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.dto.RiskFactor;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RiskEngine {

    private static final Map<String, Double> STAGE_MULTIPLIERS = Map.of(
            "GERMINATION", 1.00,
            "VEGETATIVE", 1.00,
            "FLOWERING", 1.25,
            "FRUITING", 1.20,
            "MATURITY", 1.15);

    public RiskResult assess(Crop crop, WeatherForecastResponse weather) {
        WeatherForecastResponse.DailyForecast daily = weather.daily();
        double precipitation = first(daily.precipitationSum());
        double precipitationProbability = first(daily.precipitationProbabilityMax());
        double temperatureMax = first(daily.temperatureMax());
        double temperatureMin = first(daily.temperatureMin());
        double windSpeedMax = first(daily.windSpeedMax());
        double evapotranspiration = first(daily.evapotranspiration());
        String stage = normalizeStage(crop.getCropStage());

        List<RiskFactor> factors = new ArrayList<>();
        if (precipitationProbability >= 70 && precipitation >= 10) {
            factors.add(new RiskFactor(
                    "HEAVY_RAINFALL",
                    "HIGH",
                    30,
                    format(precipitation) + " mm rainfall is forecast with "
                            + format(precipitationProbability) + "% probability."));
        }
        if (precipitationProbability >= 70 && precipitation < 10) {
            factors.add(new RiskFactor(
                    "HIGH_PRECIPITATION_PROBABILITY",
                    "MODERATE",
                    10,
                    "Rainfall probability is " + format(precipitationProbability) + "%."));
        }
        if (temperatureMax >= 38) {
            factors.add(new RiskFactor(
                    "EXTREME_HEAT",
                    "HIGH",
                    25,
                    "The forecast maximum temperature is " + format(temperatureMax) + " C."));
        }
        if (temperatureMin <= 10) {
            factors.add(new RiskFactor(
                    "COLD_STRESS",
                    "HIGH",
                    20,
                    "The forecast minimum temperature is " + format(temperatureMin) + " C."));
        }
        if (windSpeedMax >= 35) {
            factors.add(new RiskFactor(
                    "STRONG_WIND",
                    "MODERATE",
                    15,
                    "The forecast maximum wind speed is " + format(windSpeedMax) + " km/h."));
        }
        if (evapotranspiration >= 5) {
            factors.add(new RiskFactor(
                    "HIGH_EVAPOTRANSPIRATION",
                    "MODERATE",
                    10,
                    "Evapotranspiration is forecast at " + format(evapotranspiration) + " mm."));
        }

        int baseScore = factors.stream()
                .mapToInt(RiskFactor::contribution)
                .sum();
        int score = Math.min(100, (int) Math.round(baseScore * STAGE_MULTIPLIERS.getOrDefault(stage, 1.0)));
        String riskLevel = riskLevel(score);
        String action = recommendedAction(factors, stage);
        return new RiskResult(score, riskLevel, factors, action);
    }

    private String recommendedAction(List<RiskFactor> factors, String stage) {
        if (factors.isEmpty()) {
            return "Continue normal crop monitoring.";
        }
        RiskFactor dominant = factors.stream()
                .max((first, second) -> Integer.compare(first.contribution(), second.contribution()))
                .orElseThrow();
        return switch (dominant.type()) {
            case "HEAVY_RAINFALL" -> "MATURITY".equals(stage)
                    ? "Inspect for rainfall damage and review harvest timing."
                    : "Inspect drainage and avoid irrigation until rainfall risk decreases.";
            case "EXTREME_HEAT" -> "Increase crop monitoring and evaluate irrigation requirements.";
            case "STRONG_WIND" -> "Inspect crop supports and protect vulnerable plants.";
            case "COLD_STRESS" -> "Monitor the crop for cold damage and consider appropriate protective measures.";
            case "HIGH_EVAPOTRANSPIRATION", "HIGH_PRECIPITATION_PROBABILITY" ->
                "Monitor soil moisture and irrigation demand.";
            default -> "Continue normal crop monitoring.";
        };
    }

    private String riskLevel(int score) {
        if (score >= 75) {
            return "CRITICAL";
        }
        if (score >= 50) {
            return "HIGH";
        }
        if (score >= 25) {
            return "MODERATE";
        }
        return "LOW";
    }

    private double first(List<Double> values) {
        if (values == null || values.isEmpty() || values.get(0) == null) {
            throw new InvalidWeatherDataException();
        }
        return values.get(0);
    }

    private String normalizeStage(String cropStage) {
        return cropStage == null ? "" : cropStage.trim().toUpperCase(Locale.ROOT);
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public record RiskResult(
            int score,
            String riskLevel,
            List<RiskFactor> factors,
            String recommendedAction) {
    }

    public static class InvalidWeatherDataException extends RuntimeException {
    }
}
