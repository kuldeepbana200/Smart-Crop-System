package com.smartcrop.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenMeteoResponse(
        String timezone,
        OpenMeteoCurrent current,
        OpenMeteoHourly hourly,
        OpenMeteoDaily daily) {

    public record OpenMeteoCurrent(
            String time,
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("relative_humidity_2m") Double relativeHumidity,
            Double precipitation,
            @JsonProperty("wind_speed_10m") Double windSpeed,
            @JsonProperty("weather_code") Integer weatherCode) {
    }

    public record OpenMeteoHourly(
            List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperature,
            @JsonProperty("relative_humidity_2m") List<Double> relativeHumidity,
            @JsonProperty("precipitation_probability") List<Double> precipitationProbability,
            List<Double> precipitation,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed,
            @JsonProperty("weather_code") List<Integer> weatherCode) {
    }

    public record OpenMeteoDaily(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum,
            @JsonProperty("precipitation_probability_max") List<Double> precipitationProbabilityMax,
            @JsonProperty("wind_speed_10m_max") List<Double> windSpeedMax,
            @JsonProperty("et0_fao_evapotranspiration") List<Double> evapotranspiration) {
    }
}
