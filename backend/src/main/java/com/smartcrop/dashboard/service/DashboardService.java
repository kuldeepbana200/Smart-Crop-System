package com.smartcrop.dashboard.service;

import com.smartcrop.advisory.repository.AdvisoryRepository;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.dashboard.dto.DashboardResponse;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;
import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.repository.NotificationRepository;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.client.OpenMeteoClient;
import com.smartcrop.weather.service.WeatherService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

        private final UserRepository userRepository;
        private final FarmerRepository farmerRepository;
        private final CropRepository cropRepository;
        private final DistressAlertRepository distressAlertRepository;
        private final InterventionRepository interventionRepository;
        private final NotificationRepository notificationRepository;
        private final AdvisoryRepository advisoryRepository;
        private final MarketService marketService;
        private final WeatherService weatherService;

        public DashboardService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        DistressAlertRepository distressAlertRepository,
                        InterventionRepository interventionRepository,
                        NotificationRepository notificationRepository,
                        AdvisoryRepository advisoryRepository,
                        MarketService marketService,
                        WeatherService weatherService) {

                this.userRepository = userRepository;
                this.farmerRepository = farmerRepository;
                this.cropRepository = cropRepository;
                this.distressAlertRepository = distressAlertRepository;
                this.interventionRepository = interventionRepository;
                this.notificationRepository = notificationRepository;
                this.advisoryRepository = advisoryRepository;
                this.marketService = marketService;
                this.weatherService = weatherService;
        }

        @Transactional(readOnly = true)
        public DashboardResponse getDashboard(
                        Authentication authentication) {

                User user = userRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Authenticated user not found"));

                Farmer farmer = farmerRepository
                                .findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);

                Long farmerId = farmer.getId();

                DashboardResponse.SummaryStatistics statistics = new DashboardResponse.SummaryStatistics(
                                cropRepository.countByFarmerId(farmerId),

                                distressAlertRepository
                                                .countByFarmerIdAndStatus(
                                                                farmerId,
                                                                AlertStatus.OPEN),

                                distressAlertRepository
                                                .countByFarmerIdAndStatus(
                                                                farmerId,
                                                                AlertStatus.ACKNOWLEDGED),

                                distressAlertRepository
                                                .countByFarmerIdAndStatus(
                                                                farmerId,
                                                                AlertStatus.RESOLVED),

                                interventionRepository
                                                .countByDistressAlertFarmerIdAndStatusIn(
                                                                farmerId,
                                                                List.of(
                                                                                InterventionStatus.PLANNED,
                                                                                InterventionStatus.IN_PROGRESS)),

                                notificationRepository
                                                .countByRecipientIdAndStatus(
                                                                user.getId(),
                                                                NotificationStatus.UNREAD),

                                advisoryRepository
                                                .countByCropFarmerId(farmerId));

                List<DashboardResponse.CropSummary> recentCrops = cropRepository
                                .findTop5ByFarmerIdOrderByCreatedAtDesc(farmerId)
                                .stream()
                                .map(DashboardResponse.CropSummary::from)
                                .toList();

                List<DashboardResponse.AlertSummary> recentAlerts = distressAlertRepository
                                .findTop5ByFarmerIdOrderByCreatedAtDesc(farmerId)
                                .stream()
                                .map(DashboardResponse.AlertSummary::from)
                                .toList();

                List<DashboardResponse.AdvisorySummary> recentAdvisories = advisoryRepository
                                .findTop5ByCropFarmerIdOrderByGeneratedAtDesc(farmerId)
                                .stream()
                                .map(DashboardResponse.AdvisorySummary::from)
                                .toList();

                List<DashboardResponse.NotificationSummary> recentNotifications = notificationRepository
                                .findTop5ByRecipientIdOrderByCreatedAtDesc(user.getId())
                                .stream()
                                .map(DashboardResponse.NotificationSummary::from)
                                .toList();

                List<DashboardResponse.MarketSummary> marketSummaries = marketSummaries(farmerId);
                DashboardResponse.WeatherSummary weather = weatherSummary(authentication);

                DashboardResponse.FarmerSummary farmerSummary = new DashboardResponse.FarmerSummary(
                                farmer.getId(),
                                user.getName(),
                                farmer.getDistrict(),
                                farmer.getState());

                return new DashboardResponse(
                                farmerSummary,
                                statistics,
                                recentCrops,
                                recentAlerts,
                                recentAdvisories,
                                recentNotifications,
                                marketSummaries,
                                weather);
        }

        private DashboardResponse.WeatherSummary weatherSummary(Authentication authentication) {
                WeatherForecastResponse forecast;
                try {
                        forecast = weatherService.getForecast(authentication);
                } catch (WeatherService.FarmerProfileNotFoundException
                                | WeatherService.FarmerCoordinatesMissingException
                                | WeatherService.InvalidCoordinatesException
                                | OpenMeteoClient.MalformedWeatherResponseException exception) {
                        return null;
                }
                if (forecast == null || forecast.current() == null || forecast.hourly() == null
                                || forecast.hourly().timestamps() == null || forecast.hourly().timestamps().isEmpty()) {
                        return null;
                }

                return new DashboardResponse.WeatherSummary(
                                forecast.timezone(),
                                forecast.current(),
                                forecast.hourly().timestamps().get(0),
                                firstValue(forecast.hourly().precipitationProbability()),
                                firstValue(forecast.hourly().precipitation()));
        }

        private <T> T firstValue(List<T> values) {
                return values == null || values.isEmpty() ? null : values.get(0);
        }

        @SuppressWarnings("unchecked")
        private List<DashboardResponse.MarketSummary> marketSummaries(
                        Long farmerId) {
                List<String> cropNames = cropRepository
                                .findByFarmerId(farmerId)
                                .stream()
                                .map(Crop::getCropName)
                                .filter(this::isValidCropName)
                                .distinct()
                                .toList();

                Map<String, List<MarketPriceResponse>> pricesByCrop = marketService.getLatestForCrops(cropNames);

                return cropNames.stream()
                                .map(cropName -> pricesByCrop.getOrDefault(cropName, List.of()))
                                .filter(prices -> prices != null && !prices.isEmpty())
                                .map(this::toMarketSummary)
                                .toList();
        }

        private boolean isValidCropName(String cropName) {
                return cropName != null && !cropName.isBlank();
        }

        @SuppressWarnings("unchecked")
        private DashboardResponse.MarketSummary toMarketSummary(
                        List<MarketPriceResponse> prices) {

                MarketPriceResponse best = prices.stream()
                                .filter(price -> price.modalPrice() != null)
                                .max(Comparator.comparing(
                                                MarketPriceResponse::modalPrice))
                                .orElse(null);

                MarketPriceResponse latest = prices.stream()
                                .filter(price -> price.observedAt() != null)
                                .max(Comparator.comparing(
                                                MarketPriceResponse::observedAt))
                                .orElse(null);

                if (best == null && latest == null) {
                        throw new IllegalStateException(
                                        "Market price list contains no usable observations");
                }

                MarketPriceResponse reference = best != null ? best : latest;

                BigDecimal bestModalPrice = best == null
                                ? null
                                : BigDecimal.valueOf(best.modalPrice());

                return new DashboardResponse.MarketSummary(
                                reference.cropName(),
                                best == null ? null : best.marketName(),
                                bestModalPrice,
                                reference.unit(),
                                reference.currency(),
                                prices.size(),
                                latest == null ? null : latest.observedAt());
        }

        public static class FarmerProfileNotFoundException
                        extends RuntimeException {
        }
}