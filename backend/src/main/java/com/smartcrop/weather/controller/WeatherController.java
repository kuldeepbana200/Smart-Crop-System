package com.smartcrop.weather.controller;

import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('FARMER')")
    public CurrentWeatherResponse getCurrentWeather(Authentication authentication) {
        return weatherService.getCurrentWeather(authentication);
    }

    @GetMapping("/forecast")
    @PreAuthorize("hasRole('FARMER')")
    public WeatherForecastResponse getForecast(Authentication authentication) {
        return weatherService.getForecast(authentication);
    }
}
