package com.smartcrop.risk.engine;

import com.smartcrop.crop.entity.Crop;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.risk.dto.RiskFactor;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskEngineTest {

    private final RiskEngine riskEngine = new RiskEngine();

    @Test
    void noSignificantWeatherRiskIsLow() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 20, 1, 25, 15, 3);

        assertEquals(0, result.score());
        assertEquals("LOW", result.riskLevel());
        assertTrue(result.factors().isEmpty());
        assertEquals("Continue normal crop monitoring.", result.recommendedAction());
    }

    @Test
    void heavyRainfallIncreasesRisk() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 80, 12, 25, 15, 3);

        assertEquals(30, result.score());
        assertEquals("MODERATE", result.riskLevel());
        assertFactor(result, "HEAVY_RAINFALL", 30);
        assertEquals("Inspect drainage and avoid irrigation until rainfall risk decreases.",
                result.recommendedAction());
    }

    @Test
    void extremeHeatIncreasesRisk() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 20, 1, 39, 15, 3);

        assertEquals(25, result.score());
        assertFactor(result, "EXTREME_HEAT", 25);
        assertEquals("Increase crop monitoring and evaluate irrigation requirements.",
                result.recommendedAction());
    }

    @Test
    void coldStressIncreasesRisk() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 20, 1, 25, 8, 3);

        assertEquals(20, result.score());
        assertFactor(result, "COLD_STRESS", 20);
        assertEquals("Monitor the crop for cold damage and consider appropriate protective measures.",
                result.recommendedAction());
    }

    @Test
    void strongWindIncreasesRisk() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 20, 1, 25, 15, 36);

        assertEquals(15, result.score());
        assertFactor(result, "STRONG_WIND", 15);
        assertEquals("Inspect crop supports and protect vulnerable plants.", result.recommendedAction());
    }

    @Test
    void floweringStageIncreasesRisk() {
        RiskEngine.RiskResult result = assess("FLOWERING", 20, 1, 39, 15, 3);

        assertEquals(31, result.score());
        assertEquals("MODERATE", result.riskLevel());
    }

    @Test
    void fruitingStageIncreasesRisk() {
        RiskEngine.RiskResult result = assess("FRUITING", 20, 1, 39, 15, 3);

        assertEquals(30, result.score());
        assertEquals("MODERATE", result.riskLevel());
    }

    @Test
    void maturityStageIncreasesRisk() {
        RiskEngine.RiskResult result = assess("MATURITY", 20, 1, 39, 15, 3);

        assertEquals(29, result.score());
        assertEquals("MODERATE", result.riskLevel());
    }

    @Test
    void scoreIsCappedAtOneHundred() {
        RiskEngine.RiskResult result = assess("FLOWERING", 80, 20, 40, 5, 40);

        assertEquals(100, result.score());
        assertEquals("CRITICAL", result.riskLevel());
    }

    @Test
    void multipleRiskFactorsAreCombinedWithoutDuplicateHeavyRainProbability() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 80, 12, 39, 8, 36);

        assertEquals(90, result.score());
        assertEquals(4, result.factors().size());
        assertFalse(result.factors().stream()
                .anyMatch(factor -> "HIGH_PRECIPITATION_PROBABILITY".equals(factor.type())));
    }

    @Test
    void factorReasonsArePopulated() {
        RiskEngine.RiskResult result = assess("VEGETATIVE", 80, 12, 25, 15, 3);

        assertTrue(result.factors().stream()
                .allMatch(factor -> factor.reason() != null && !factor.reason().isBlank()));
    }

    @Test
    void maturityHeavyRainfallUsesHarvestAction() {
        RiskEngine.RiskResult result = assess("MATURITY", 80, 12, 25, 15, 3);

        assertEquals("Inspect for rainfall damage and review harvest timing.", result.recommendedAction());
    }

    private void assertFactor(RiskEngine.RiskResult result, String type, int contribution) {
        RiskFactor factor = result.factors().stream()
                .filter(candidate -> type.equals(candidate.type()))
                .findFirst()
                .orElseThrow();
        assertEquals(contribution, factor.contribution());
        assertFalse(factor.reason().isBlank());
    }

    private RiskEngine.RiskResult assess(
            String stage,
            double precipitationProbability,
            double precipitation,
            double temperatureMax,
            double temperatureMin,
            double windSpeedMax) {
        return riskEngine.assess(
                new Crop(1L, new Farmer(1L, null, "District", "State", 1.0, 2.0, 3.0),
                        "Rice", stage, null, null, null),
                weather(precipitationProbability, precipitation, temperatureMax, temperatureMin, windSpeedMax));
    }

    private WeatherForecastResponse weather(
            double precipitationProbability,
            double precipitation,
            double temperatureMax,
            double temperatureMin,
            double windSpeedMax) {
        CurrentWeatherResponse current = new CurrentWeatherResponse(
                "2026-08-23T12:00", "Asia/Kolkata", 30.0, 70.0, 0.0, 10.0, 1);
        WeatherForecastResponse.HourlyForecast hourly = new WeatherForecastResponse.HourlyForecast(
                List.of("2026-08-23T12:00"), List.of(30.0), List.of(70.0),
                List.of(precipitationProbability), List.of(precipitation), List.of(windSpeedMax), List.of(1));
        WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                List.of("2026-08-23"), List.of(1), List.of(temperatureMax), List.of(temperatureMin),
                List.of(precipitation), List.of(precipitationProbability), List.of(windSpeedMax), List.of(3.0));
        return new WeatherForecastResponse("Asia/Kolkata", current, hourly, daily);
    }
}
