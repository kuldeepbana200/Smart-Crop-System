package com.smartcrop.weather.dto;

import java.util.List;

public record WeatherForecastResponse(
        String timezone,
        CurrentWeatherResponse current,
        HourlyForecast hourly,
        DailyForecast daily
) {

    public record HourlyForecast(
            List<String> timestamps,
            List<Double> temperature,
            List<Double> relativeHumidity,
            List<Double> precipitationProbability,
            List<Double> precipitation,
            List<Double> windSpeed,
            List<Integer> weatherCode
    ) {
    }

    public record DailyForecast(
            List<String> timestamps,
            List<Integer> weatherCode,
            List<Double> temperatureMax,
            List<Double> temperatureMin,
            List<Double> precipitationSum,
            List<Double> precipitationProbabilityMax,
            List<Double> windSpeedMax,
            List<Double> evapotranspiration
    ) {
    }
}
