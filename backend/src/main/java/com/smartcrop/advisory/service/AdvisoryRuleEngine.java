package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AdvisoryRuleEngine {

    private static final Map<String, Integer> SEVERITY_ORDER = Map.of(
            "URGENT", 0,
            "WARNING", 1,
            "ADVISORY", 2,
            "INFO", 3);

    public List<AdvisoryRecommendation> generate(Crop crop, WeatherForecastResponse weather) {
        WeatherForecastResponse.DailyForecast daily = weather.daily();
        double precipitationProbability = first(daily.precipitationProbabilityMax());
        double precipitation = first(daily.precipitationSum());
        double maximumTemperature = first(daily.temperatureMax());
        double minimumTemperature = first(daily.temperatureMin());
        double maximumWind = first(daily.windSpeedMax());
        double evapotranspiration = first(daily.evapotranspiration());
        String stage = normalizeStage(crop.getCropStage());

        List<AdvisoryRecommendation> recommendations = new ArrayList<>();

        if (precipitationProbability >= 70 && precipitation >= 10) {
            recommendations.add(new AdvisoryRecommendation(
                    "RAINFALL",
                    "WARNING",
                    "Heavy rainfall expected",
                    "Delay irrigation and avoid fertilizer application before rainfall.",
                    "The forecast shows " + format(precipitationProbability)
                            + "% precipitation probability and " + format(precipitation) + " mm of rain."));
            if ("MATURITY".equals(stage)) {
                recommendations.add(new AdvisoryRecommendation(
                        "CROP_STAGE",
                        "WARNING",
                        "Protect the maturing crop",
                        "Review harvest timing and inspect the crop for rainfall damage.",
                        "Heavy rainfall is forecast while the crop is approaching maturity."));
            }
        }

        if (precipitationProbability < 30 && precipitation < 2 && evapotranspiration >= 4) {
            recommendations.add(new AdvisoryRecommendation(
                    "IRRIGATION",
                    "ADVISORY",
                    "Irrigation may be needed",
                    "Monitor soil moisture and consider irrigation.",
                    "Rainfall probability is " + format(precipitationProbability)
                            + "%, rainfall is " + format(precipitation) + " mm, and evapotranspiration is "
                            + format(evapotranspiration) + " mm."));
        }

        if (maximumTemperature >= 38) {
            String severity = "FLOWERING".equals(stage) ? "URGENT" : "WARNING";
            recommendations.add(new AdvisoryRecommendation(
                    "TEMPERATURE",
                    severity,
                    "Extreme heat expected",
                    "Irrigate during cooler hours and monitor the crop for heat stress.",
                    "The forecast maximum temperature is " + format(maximumTemperature) + " C."
                            + ("FLOWERING".equals(stage)
                                    ? " Flowering crops require closer heat-stress monitoring."
                                    : "")));
        }

        if (minimumTemperature <= 10) {
            recommendations.add(new AdvisoryRecommendation(
                    "TEMPERATURE",
                    "WARNING",
                    "Cold stress possible",
                    "Monitor the crop for cold stress.",
                    "The forecast minimum temperature is " + format(minimumTemperature) + " C."));
        }

        if (maximumWind >= 35) {
            recommendations.add(new AdvisoryRecommendation(
                    "WIND",
                    "WARNING",
                    "Strong wind expected",
                    "Avoid spraying and inspect crops for lodging or physical damage.",
                    "The forecast maximum wind speed is " + format(maximumWind) + " km/h."));
        }

        if (evapotranspiration >= 5 && precipitation <= 2) {
            recommendations.add(new AdvisoryRecommendation(
                    "EVAPOTRANSPIRATION",
                    "ADVISORY",
                    "High water demand expected",
                    "Monitor soil moisture and irrigation demand.",
                    "Evapotranspiration is " + format(evapotranspiration)
                            + " mm while forecast rainfall is only " + format(precipitation) + " mm."));
            if ("FRUITING".equals(stage)) {
                recommendations.add(new AdvisoryRecommendation(
                        "CROP_STAGE",
                        "ADVISORY",
                        "Protect fruit development",
                        "Pay close attention to soil moisture and irrigation demand during fruiting.",
                        "The crop is fruiting while water demand is high and forecast rainfall is low."));
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(new AdvisoryRecommendation(
                    "GENERAL",
                    "INFO",
                    "No immediate weather action",
                    "Continue monitoring the crop and local field conditions.",
                    "No configured weather thresholds currently require action."));
        }

        recommendations.sort(Comparator.comparingInt(item -> SEVERITY_ORDER.get(item.severity())));
        return recommendations;
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

    public static class InvalidWeatherDataException extends RuntimeException {
    }
}
