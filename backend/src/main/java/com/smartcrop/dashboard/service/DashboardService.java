package com.smartcrop.dashboard.service;

import com.smartcrop.advisory.repository.AdvisoryRepository;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.dashboard.dto.DashboardResponse;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;
import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.repository.NotificationRepository;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        @org.springframework.beans.factory.annotation.Autowired
        public DashboardService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        DistressAlertRepository distressAlertRepository,
                        InterventionRepository interventionRepository,
                        NotificationRepository notificationRepository,
                        AdvisoryRepository advisoryRepository,
                        MarketService marketService) {
                this.userRepository = userRepository;
                this.farmerRepository = farmerRepository;
                this.cropRepository = cropRepository;
                this.distressAlertRepository = distressAlertRepository;
                this.interventionRepository = interventionRepository;
                this.notificationRepository = notificationRepository;
                this.advisoryRepository = advisoryRepository;
                this.marketService = marketService;
        }

        public DashboardService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        DistressAlertRepository distressAlertRepository,
                        InterventionRepository interventionRepository,
                        NotificationRepository notificationRepository,
                        AdvisoryRepository advisoryRepository) {
                this(userRepository, farmerRepository, cropRepository, distressAlertRepository,
                                interventionRepository, notificationRepository, advisoryRepository, null);
        }

        @Transactional(readOnly = true)
        public DashboardResponse getDashboard(Authentication authentication) {
                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
                Farmer farmer = farmerRepository.findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);
                Long farmerId = farmer.getId();

                DashboardResponse.SummaryStatistics statistics = new DashboardResponse.SummaryStatistics(
                                cropRepository.countByFarmerId(farmerId),
                                distressAlertRepository.countByFarmerIdAndStatus(farmerId, AlertStatus.OPEN),
                                distressAlertRepository.countByFarmerIdAndStatus(farmerId, AlertStatus.ACKNOWLEDGED),
                                distressAlertRepository.countByFarmerIdAndStatus(farmerId, AlertStatus.RESOLVED),
                                interventionRepository.countByDistressAlertFarmerIdAndStatusIn(
                                                farmerId,
                                                List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS)),
                                notificationRepository.countByRecipientIdAndStatus(user.getId(),
                                                NotificationStatus.UNREAD),
                                advisoryRepository.countByCropFarmerId(farmerId));

                return new DashboardResponse(
                                new DashboardResponse.FarmerSummary(
                                                farmer.getId(), user.getName(), farmer.getDistrict(),
                                                farmer.getState()),
                                statistics,
                                cropRepository.findTop5ByFarmerIdOrderByCreatedAtDesc(farmerId).stream()
                                                .map(DashboardResponse.CropSummary::from)
                                                .toList(),
                                distressAlertRepository.findTop5ByFarmerIdOrderByCreatedAtDesc(farmerId).stream()
                                                .map(DashboardResponse.AlertSummary::from)
                                                .toList(),
                                advisoryRepository.findTop5ByCropFarmerIdOrderByGeneratedAtDesc(farmerId).stream()
                                                .map(DashboardResponse.AdvisorySummary::from)
                                                .toList(),
                                notificationRepository.findTop5ByRecipientIdOrderByCreatedAtDesc(user.getId()).stream()
                                                .map(DashboardResponse.NotificationSummary::from)
                                                .toList(),
                                marketSummaries(farmerId));
        }

        private List<DashboardResponse.MarketSummary> marketSummaries(Long farmerId) {
                if (marketService == null) {
                        return List.of();
                }
                return cropRepository.findByFarmerId(farmerId).stream()
                                .map(crop -> marketService.getLatestForCrop(crop.getCropName()))
                                .filter(prices -> !prices.isEmpty())
                                .map(this::toMarketSummary)
                                .toList();
        }

        private DashboardResponse.MarketSummary toMarketSummary(List<MarketPriceResponse> prices) {
                MarketPriceResponse best = prices.stream()
                                .max(java.util.Comparator.comparing(MarketPriceResponse::modalPrice))
                                .orElseThrow();
                MarketPriceResponse latest = prices.stream()
                                .max(java.util.Comparator.comparing(MarketPriceResponse::observedAt))
                                .orElseThrow();
                return new DashboardResponse.MarketSummary(
                                best.cropName(), best.marketName(),
                                best.modalPrice() != null ? java.math.BigDecimal.valueOf(best.modalPrice()) : null,
                                best.unit(), best.currency(),
                                prices.size(), latest.observedAt().atStartOfDay());
        }

        public static class FarmerProfileNotFoundException extends RuntimeException {
        }
}
