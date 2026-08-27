package com.smartcrop.dev.weather;

import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.weather.client.OpenMeteoClient;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@Primary
@Profile("dev")
public class DevelopmentWeatherService extends WeatherService {

        public DevelopmentWeatherService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        OpenMeteoClient openMeteoClient) {
                super(userRepository, farmerRepository, openMeteoClient);
        }

        @Override
        public WeatherForecastResponse getForecast(Authentication authentication) {
                CurrentWeatherResponse current = new CurrentWeatherResponse(
                                "2026-08-23T12:00", "Asia/Kolkata", 30.0, 70.0, 0.0, 10.0, 1);
                WeatherForecastResponse.HourlyForecast hourly = new WeatherForecastResponse.HourlyForecast(
                                List.of("2026-08-23T12:00"),
                                List.of(30.0),
                                List.of(70.0),
                                List.of(80.0),
                                List.of(12.0),
                                List.of(10.0),
                                List.of(1));
                WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                                List.of("2026-08-23"),
                                List.of(1),
                                List.of(39.0),
                                List.of(15.0),
                                List.of(12.0),
                                List.of(80.0),
                                List.of(10.0),
                                List.of(3.0));
                return new WeatherForecastResponse("Asia/Kolkata", current, hourly, daily);
        }
}
