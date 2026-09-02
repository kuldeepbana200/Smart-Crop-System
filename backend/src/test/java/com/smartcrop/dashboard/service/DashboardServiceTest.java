package com.smartcrop.dashboard.service;

import com.smartcrop.advisory.repository.AdvisoryRepository;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;

import com.smartcrop.crop.repository.CropRepository;

import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;

import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;

import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;

import com.smartcrop.market.service.MarketService;

import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.repository.NotificationRepository;
import com.smartcrop.weather.service.WeatherService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

        private UserRepository userRepository;
        private FarmerRepository farmerRepository;
        private CropRepository cropRepository;
        private DistressAlertRepository alertRepository;
        private InterventionRepository interventionRepository;
        private NotificationRepository notificationRepository;
        private AdvisoryRepository advisoryRepository;
        private MarketService marketService;
        private WeatherService weatherService;

        private DashboardService dashboardService;

        private Authentication authentication;

        private User user;
        private Farmer farmer;

        @BeforeEach
        void setUp() {

                userRepository = mock(UserRepository.class);
                farmerRepository = mock(FarmerRepository.class);
                cropRepository = mock(CropRepository.class);
                alertRepository = mock(DistressAlertRepository.class);
                interventionRepository = mock(InterventionRepository.class);
                notificationRepository = mock(NotificationRepository.class);
                advisoryRepository = mock(AdvisoryRepository.class);
                marketService = mock(MarketService.class);
                weatherService = mock(WeatherService.class);

                dashboardService = new DashboardService(
                                userRepository,
                                farmerRepository,
                                cropRepository,
                                alertRepository,
                                interventionRepository,
                                notificationRepository,
                                advisoryRepository,
                                marketService,
                                weatherService);

                authentication = mock(Authentication.class);

                when(authentication.getName())
                                .thenReturn("farmer@example.com");

                user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                farmer = new Farmer(
                                20L,
                                user,
                                "Pune",
                                "Maharashtra",
                                18.5,
                                73.8,
                                2.0);

                when(userRepository.findByEmail("farmer@example.com"))
                                .thenReturn(Optional.of(user));

                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.of(farmer));

                // Statistics

                when(cropRepository.countByFarmerId(20L))
                                .thenReturn(4L);

                when(alertRepository.countByFarmerIdAndStatus(
                                20L,
                                AlertStatus.OPEN))
                                .thenReturn(2L);

                when(alertRepository.countByFarmerIdAndStatus(
                                20L,
                                AlertStatus.ACKNOWLEDGED))
                                .thenReturn(1L);

                when(alertRepository.countByFarmerIdAndStatus(
                                20L,
                                AlertStatus.RESOLVED))
                                .thenReturn(3L);

                when(interventionRepository
                                .countByDistressAlertFarmerIdAndStatusIn(
                                                20L,
                                                List.of(
                                                                InterventionStatus.PLANNED,
                                                                InterventionStatus.IN_PROGRESS)))
                                .thenReturn(2L);

                when(notificationRepository.countByRecipientIdAndStatus(
                                10L,
                                NotificationStatus.UNREAD))
                                .thenReturn(5L);

                when(advisoryRepository.countByCropFarmerId(20L))
                                .thenReturn(6L);

                // Recent data

                when(cropRepository.findTop5ByFarmerIdOrderByCreatedAtDesc(20L))
                                .thenReturn(List.of());

                when(alertRepository.findTop5ByFarmerIdOrderByCreatedAtDesc(20L))
                                .thenReturn(List.of());

                when(advisoryRepository
                                .findTop5ByCropFarmerIdOrderByGeneratedAtDesc(20L))
                                .thenReturn(List.of());

                when(notificationRepository
                                .findTop5ByRecipientIdOrderByCreatedAtDesc(10L))
                                .thenReturn(List.of());

                // Market data

                when(cropRepository.findByFarmerId(20L))
                                .thenReturn(List.of());
        }

        @Test
        void dashboardContainsFarmerSummaryAndStatistics() {

                var dashboard = dashboardService.getDashboard(authentication);

                assertEquals(
                                20L,
                                dashboard.farmer().id());

                assertEquals(
                                "Farmer",
                                dashboard.farmer().name());

                assertEquals(
                                "Pune",
                                dashboard.farmer().district());

                assertEquals(
                                4L,
                                dashboard.statistics().totalCrops());

                assertEquals(
                                2L,
                                dashboard.statistics().openDistressAlerts());

                assertEquals(
                                1L,
                                dashboard.statistics().acknowledgedDistressAlerts());

                assertEquals(
                                3L,
                                dashboard.statistics().resolvedDistressAlerts());

                assertEquals(
                                2L,
                                dashboard.statistics().activeInterventions());

                assertEquals(
                                5L,
                                dashboard.statistics().unreadNotifications());

                assertEquals(
                                6L,
                                dashboard.statistics().totalAdvisories());
        }

        @Test
        void dashboardUsesAuthenticatedFarmerForAllQueries() {

                dashboardService.getDashboard(authentication);

                verify(cropRepository)
                                .countByFarmerId(20L);

                verify(alertRepository)
                                .countByFarmerIdAndStatus(
                                                20L,
                                                AlertStatus.OPEN);

                verify(interventionRepository)
                                .countByDistressAlertFarmerIdAndStatusIn(
                                                20L,
                                                List.of(
                                                                InterventionStatus.PLANNED,
                                                                InterventionStatus.IN_PROGRESS));

                verify(notificationRepository)
                                .countByRecipientIdAndStatus(
                                                10L,
                                                NotificationStatus.UNREAD);

                verify(advisoryRepository)
                                .countByCropFarmerId(20L);
        }

        @Test
        void missingFarmerProfileIsRejected() {

                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                DashboardService.FarmerProfileNotFoundException.class,
                                () -> dashboardService.getDashboard(authentication));
        }

        @Test
        void dashboardIncludesWeatherWhenWeatherServiceReturnsNoData() {

                var dashboard = dashboardService.getDashboard(authentication);

                assertEquals(
                                List.of(),
                                dashboard.recentCrops());

                assertEquals(
                                List.of(),
                                dashboard.recentAlerts());

                assertEquals(
                                List.of(),
                                dashboard.recentAdvisories());

                assertEquals(
                                List.of(),
                                dashboard.recentNotifications());

                assertEquals(
                                List.of(),
                                dashboard.marketSummaries());

                assertEquals(null, dashboard.weather());
        }
}