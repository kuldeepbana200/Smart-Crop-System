package com.smartcrop.risk.monitoring;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.risk-monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RiskMonitoringScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RiskMonitoringScheduler.class);

    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final WeatherService weatherService;
    private final RiskEngine riskEngine;
    private final DistressAlertService distressAlertService;

    public RiskMonitoringScheduler(
            FarmerRepository farmerRepository,
            CropRepository cropRepository,
            WeatherService weatherService,
            RiskEngine riskEngine,
            DistressAlertService distressAlertService) {
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.weatherService = weatherService;
        this.riskEngine = riskEngine;
        this.distressAlertService = distressAlertService;
    }

    @Scheduled(fixedDelayString = "${app.risk-monitoring.fixed-delay-ms:3600000}")
    public void monitor() {
        runOnce();
    }

    public void runOnce() {
        for (Farmer farmer : farmerRepository.findAll()) {
            List<Crop> activeCrops = cropRepository.findByFarmerId(farmer.getId()).stream()
                    .filter(this::isActiveCrop)
                    .toList();
            if (activeCrops.isEmpty()) {
                continue;
            }

            WeatherForecastResponse weather;
            try {
                weather = weatherService.getForecast(authenticationFor(farmer));
            } catch (RuntimeException exception) {
                logger.warn("Risk monitoring weather retrieval failed for farmer {}", farmer.getId(), exception);
                continue;
            }

            for (Crop crop : activeCrops) {
                try {
                    RiskEngine.RiskResult result = riskEngine.assess(crop, weather);
                    RiskAssessmentResponse assessment = new RiskAssessmentResponse(
                            crop.getId(),
                            crop.getCropName(),
                            crop.getCropStage(),
                            result.score(),
                            result.riskLevel(),
                            result.factors(),
                            result.recommendedAction(),
                            LocalDateTime.now());
                    if (isAlertLevel(assessment.riskLevel())) {
                        distressAlertService.createIfRequired(farmer, crop, assessment);
                    }
                } catch (RuntimeException exception) {
                    logger.warn("Risk monitoring assessment failed for crop {}", crop.getId(), exception);
                }
            }
        }
    }

    private boolean isActiveCrop(Crop crop) {
        return crop.getExpectedHarvestDate() == null
                || !crop.getExpectedHarvestDate().isBefore(LocalDate.now());
    }

    private boolean isAlertLevel(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Farmer farmer) {
        return new UsernamePasswordAuthenticationToken(
                farmer.getUser().getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + Role.FARMER.name())));
    }
}
