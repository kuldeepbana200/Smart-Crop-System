package com.smartcrop.weather.service;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.weather.client.OpenMeteoClient;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.OpenMeteoResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WeatherService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final OpenMeteoClient openMeteoClient;

    public WeatherService(
            UserRepository userRepository,
            FarmerRepository farmerRepository,
            OpenMeteoClient openMeteoClient) {
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.openMeteoClient = openMeteoClient;
    }

    @Transactional(readOnly = true)
    public CurrentWeatherResponse getCurrentWeather(Authentication authentication) {
        Coordinates coordinates = getCoordinates(authentication);
        OpenMeteoResponse response = openMeteoClient.getForecast(coordinates.latitude(), coordinates.longitude());
        validateResponse(response, false);
        return toCurrentResponse(response);
    }

    @Transactional(readOnly = true)
    public WeatherForecastResponse getForecast(Authentication authentication) {
        Coordinates coordinates = getCoordinates(authentication);
        OpenMeteoResponse response = openMeteoClient.getForecast(coordinates.latitude(), coordinates.longitude());
        validateResponse(response, true);
        return new WeatherForecastResponse(
                response.timezone(),
                toCurrentResponse(response),
                toHourlyForecast(response.hourly()),
                toDailyForecast(response.daily()));
    }

    private Coordinates getCoordinates(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);

        if (farmer.getLatitude() == null || farmer.getLongitude() == null) {
            throw new FarmerCoordinatesMissingException();
        }
        if (!Double.isFinite(farmer.getLatitude()) || !Double.isFinite(farmer.getLongitude())
                || farmer.getLatitude() < -90 || farmer.getLatitude() > 90
                || farmer.getLongitude() < -180 || farmer.getLongitude() > 180) {
            throw new InvalidCoordinatesException();
        }
        return new Coordinates(farmer.getLatitude(), farmer.getLongitude());
    }

    private void validateResponse(OpenMeteoResponse response, boolean forecastRequired) {
        if (response.timezone() == null || response.timezone().isBlank() || response.current() == null
                || (forecastRequired && (response.hourly() == null || response.daily() == null))) {
            throw new OpenMeteoClient.MalformedWeatherResponseException();
        }
    }

    private CurrentWeatherResponse toCurrentResponse(OpenMeteoResponse response) {
        OpenMeteoResponse.OpenMeteoCurrent current = response.current();
        if (current.time() == null || current.temperature() == null || current.relativeHumidity() == null
                || current.precipitation() == null || current.windSpeed() == null || current.weatherCode() == null) {
            throw new OpenMeteoClient.MalformedWeatherResponseException();
        }
        return new CurrentWeatherResponse(
                current.time(),
                response.timezone(),
                current.temperature(),
                current.relativeHumidity(),
                current.precipitation(),
                current.windSpeed(),
                current.weatherCode());
    }

    private WeatherForecastResponse.HourlyForecast toHourlyForecast(OpenMeteoResponse.OpenMeteoHourly hourly) {
        return new WeatherForecastResponse.HourlyForecast(
                requiredList(hourly.time()),
                requiredList(hourly.temperature()),
                requiredList(hourly.relativeHumidity()),
                requiredList(hourly.precipitationProbability()),
                requiredList(hourly.precipitation()),
                requiredList(hourly.windSpeed()),
                requiredList(hourly.weatherCode()));
    }

    private WeatherForecastResponse.DailyForecast toDailyForecast(OpenMeteoResponse.OpenMeteoDaily daily) {
        return new WeatherForecastResponse.DailyForecast(
                requiredList(daily.time()),
                requiredList(daily.weatherCode()),
                requiredList(daily.temperatureMax()),
                requiredList(daily.temperatureMin()),
                requiredList(daily.precipitationSum()),
                requiredList(daily.precipitationProbabilityMax()),
                requiredList(daily.windSpeedMax()),
                requiredList(daily.evapotranspiration()));
    }

    private <T> List<T> requiredList(List<T> values) {
        if (values == null || values.isEmpty()) {
            throw new OpenMeteoClient.MalformedWeatherResponseException();
        }
        return values;
    }

    private record Coordinates(double latitude, double longitude) {
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class FarmerCoordinatesMissingException extends RuntimeException {
    }

    public static class InvalidCoordinatesException extends RuntimeException {
    }
}
