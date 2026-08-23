package com.smartcrop.weather.dto;

public record CurrentWeatherResponse(
        String timestamp,
        String timezone,
        Double temperature,
        Double relativeHumidity,
        Double precipitation,
        Double windSpeed,
        Integer weatherCode
) {
}
