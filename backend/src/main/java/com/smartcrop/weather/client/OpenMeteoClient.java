package com.smartcrop.weather.client;

import com.smartcrop.weather.dto.OpenMeteoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;

@Component
public class OpenMeteoClient {

    private static final String FORECAST_PATH = "/v1/forecast";

    private static final String CURRENT_VARIABLES =
            "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code";

    private static final String HOURLY_VARIABLES =
            "temperature_2m,relative_humidity_2m,precipitation_probability,"
                    + "precipitation,wind_speed_10m,weather_code";

    private static final String DAILY_VARIABLES =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,"
                    + "precipitation_probability_max,wind_speed_10m_max,"
                    + "et0_fao_evapotranspiration";

    private final RestClient restClient;

    public OpenMeteoClient(
            @Value("${app.weather.connect-timeout-ms:5000}") long connectTimeoutMs,
            @Value("${app.weather.read-timeout-ms:10000}") long readTimeoutMs) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .requestFactory(requestFactory)
                .build();
    }

    public OpenMeteoResponse getForecast(double latitude, double longitude) {

        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(FORECAST_PATH)
                            .queryParam("latitude", String.format(Locale.ROOT, "%.6f", latitude))
                            .queryParam("longitude", String.format(Locale.ROOT, "%.6f", longitude))
                            .queryParam("current", CURRENT_VARIABLES)
                            .queryParam("hourly", HOURLY_VARIABLES)
                            .queryParam("daily", DAILY_VARIABLES)
                            .queryParam("timezone", "auto")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response == null) {
                throw new MalformedWeatherResponseException();
            }

            return response;

        } catch (ResourceAccessException exception) {
            throw new WeatherTimeoutException(exception);
        } catch (HttpMessageConversionException exception) {
            throw new MalformedWeatherResponseException(exception);
        } catch (RestClientResponseException exception) {
            throw new WeatherProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new WeatherProviderUnavailableException(exception);
        }
    }

    public static class WeatherTimeoutException extends RuntimeException {
        public WeatherTimeoutException(Throwable cause) {
            super(cause);
        }
    }

    public static class WeatherProviderUnavailableException extends RuntimeException {
        public WeatherProviderUnavailableException(Throwable cause) {
            super(cause);
        }
    }

    public static class MalformedWeatherResponseException extends RuntimeException {
        public MalformedWeatherResponseException() {
        }

        public MalformedWeatherResponseException(Throwable cause) {
            super(cause);
        }
    }
}