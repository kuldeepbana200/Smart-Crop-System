package com.smartcrop.dev.weather;

import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.weather.client.OpenMeteoClient;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DevelopmentWeatherServiceTest {

    @Test
    void isPrimaryAndDevProfileOnly() {
        assertTrue(DevelopmentWeatherService.class.isAnnotationPresent(Primary.class));
        Profile profile = DevelopmentWeatherService.class.getAnnotation(Profile.class);
        assertEquals("dev", profile.value()[0]);
    }

    @Test
    void returnsApprovedSyntheticHighRiskForecast() {
        DevelopmentWeatherService service = new DevelopmentWeatherService(
                mock(UserRepository.class), mock(FarmerRepository.class), mock(OpenMeteoClient.class));

        WeatherForecastResponse.DailyForecast daily = service.getForecast(null).daily();

        assertEquals(80.0, daily.precipitationProbabilityMax().get(0));
        assertEquals(12.0, daily.precipitationSum().get(0));
        assertEquals(39.0, daily.temperatureMax().get(0));
        assertEquals(15.0, daily.temperatureMin().get(0));
        assertEquals(10.0, daily.windSpeedMax().get(0));
        assertEquals(3.0, daily.evapotranspiration().get(0));
    }
}
