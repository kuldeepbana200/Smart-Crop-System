package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvisoryRuleEngineTest {

    private final AdvisoryRuleEngine ruleEngine = new AdvisoryRuleEngine();

    @Test
    void returnsGeneralRecommendationWhenNoRuleMatches() {
        List<AdvisoryRecommendation> recommendations = ruleEngine.generate(
                crop("Rice", "VEGETATIVE"),
                weather(20, 30, 1, 25, 3));

        assertEquals(1, recommendations.size());
        assertEquals("GENERAL", recommendations.get(0).category());
        assertEquals("INFO", recommendations.get(0).severity());
    }

    @Test
    void generatesHeavyRainAndFloweringHeatRecommendationsInSeverityOrder() {
        List<AdvisoryRecommendation> recommendations = ruleEngine.generate(
                crop("Rice", "flowering"),
                weather(80, 12, 39, 8, 2));

        assertEquals("URGENT", recommendations.get(0).severity());
        assertEquals("TEMPERATURE", recommendations.get(0).category());
        assertTrue(recommendations.stream().anyMatch(item -> "RAINFALL".equals(item.category())));
        assertTrue(recommendations.stream().anyMatch(item -> "WARNING".equals(item.severity())));
    }

    private Crop crop(String name, String stage) {
        Farmer farmer = new Farmer(1L, null, "District", "State", 1.0, 2.0, 3.0);
        return new Crop(1L, farmer, name, stage,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 10, 23),
                LocalDateTime.now());
    }

    private WeatherForecastResponse weather(
            double precipitationProbability,
            double precipitation,
            double temperatureMax,
            double temperatureMin,
            double evapotranspiration) {
        CurrentWeatherResponse current = new CurrentWeatherResponse(
                "2026-08-23T12:00", "Asia/Kolkata", 30.0, 70.0, 0.0, 10.0, 1);
        WeatherForecastResponse.HourlyForecast hourly = new WeatherForecastResponse.HourlyForecast(
                List.of("2026-08-23T12:00"),
                List.of(30.0),
                List.of(70.0),
                List.of(precipitationProbability),
                List.of(precipitation),
                List.of(10.0),
                List.of(1));
        WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                List.of("2026-08-23"),
                List.of(1),
                List.of(temperatureMax),
                List.of(temperatureMin),
                List.of(precipitation),
                List.of(precipitationProbability),
                List.of(10.0),
                List.of(evapotranspiration));
        return new WeatherForecastResponse("Asia/Kolkata", current, hourly, daily);
    }
}
