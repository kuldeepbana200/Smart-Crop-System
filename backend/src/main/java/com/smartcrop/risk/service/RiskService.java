package com.smartcrop.risk.service;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.dto.AssessRiskRequest;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RiskService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final WeatherService weatherService;
    private final RiskEngine riskEngine;
    private final DistressAlertService distressAlertService;

    public RiskService(
            UserRepository userRepository,
            FarmerRepository farmerRepository,
            CropRepository cropRepository,
            WeatherService weatherService,
            RiskEngine riskEngine,
            DistressAlertService distressAlertService) {
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.weatherService = weatherService;
        this.riskEngine = riskEngine;
        this.distressAlertService = distressAlertService;
    }

    @Transactional
    public RiskAssessmentResponse assessRisk(
            AssessRiskRequest request,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);
        Crop crop = cropRepository.findByIdAndFarmerId(request.cropId(), farmer.getId())
                .orElseThrow(CropNotFoundException::new);

        WeatherForecastResponse weather = weatherService.getForecast(authentication);
        RiskEngine.RiskResult result = riskEngine.assess(crop, weather);
        RiskAssessmentResponse response = new RiskAssessmentResponse(
                crop.getId(),
                crop.getCropName(),
                crop.getCropStage(),
                result.score(),
                result.riskLevel(),
                result.factors(),
                result.recommendedAction(),
                LocalDateTime.now());
                if ("HIGH".equalsIgnoreCase(response.riskLevel())
                                || "CRITICAL".equalsIgnoreCase(response.riskLevel())) {
                        distressAlertService.createIfRequired(farmer, crop, response);
                }
        return response;
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class CropNotFoundException extends RuntimeException {
    }
}
