package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryResponse;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.advisory.entity.Advisory;
import com.smartcrop.advisory.repository.AdvisoryRepository;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvisoryService {

        private final UserRepository userRepository;
        private final FarmerRepository farmerRepository;
        private final CropRepository cropRepository;
        private final WeatherService weatherService;
        private final AdvisoryRuleEngine advisoryRuleEngine;
        private final AdvisoryRepository advisoryRepository;

        @Autowired
        public AdvisoryService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        WeatherService weatherService,
                        AdvisoryRuleEngine advisoryRuleEngine,
                        AdvisoryRepository advisoryRepository) {
                this.userRepository = userRepository;
                this.farmerRepository = farmerRepository;
                this.cropRepository = cropRepository;
                this.weatherService = weatherService;
                this.advisoryRuleEngine = advisoryRuleEngine;
                this.advisoryRepository = advisoryRepository;
        }

        public AdvisoryService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        WeatherService weatherService,
                        AdvisoryRuleEngine advisoryRuleEngine) {
                this(userRepository, farmerRepository, cropRepository, weatherService,
                                advisoryRuleEngine, null);
        }

        @Transactional
        public AdvisoryResponse generateAdvisory(
                        GenerateAdvisoryRequest request,
                        Authentication authentication) {
                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
                Farmer farmer = farmerRepository.findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);
                Crop crop = cropRepository.findByIdAndFarmerId(request.cropId(), farmer.getId())
                                .orElseThrow(CropNotFoundException::new);

                WeatherForecastResponse weather = weatherService.getForecast(authentication);
                List<AdvisoryRecommendation> recommendations = advisoryRuleEngine.generate(crop, weather);
                LocalDateTime generatedAt = LocalDateTime.now();
                if (advisoryRepository == null) {
                        return toResponse(null, crop, generatedAt, recommendations);
                }

                Advisory advisory = new Advisory(null, crop, generatedAt, null);
                recommendations.forEach(recommendation -> advisory.addRecommendation(
                                new com.smartcrop.advisory.entity.AdvisoryRecommendation(
                                                null,
                                                null,
                                                recommendation.category(),
                                                recommendation.severity(),
                                                recommendation.title(),
                                                recommendation.recommendation(),
                                                recommendation.reason())));
                return toResponse(advisoryRepository.save(advisory));
        }

        @Transactional(readOnly = true)
        public java.util.List<AdvisoryResponse> getMyAdvisories(Authentication authentication) {
                Farmer farmer = findAuthenticatedFarmer(authentication);
                return advisoryRepository.findByCropFarmerIdOrderByGeneratedAtDesc(farmer.getId()).stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public AdvisoryResponse getMyAdvisory(Long advisoryId, Authentication authentication) {
                Farmer farmer = findAuthenticatedFarmer(authentication);
                return advisoryRepository.findByIdAndCropFarmerId(advisoryId, farmer.getId())
                                .map(this::toResponse)
                                .orElseThrow(AdvisoryNotFoundException::new);
        }

        private Farmer findAuthenticatedFarmer(Authentication authentication) {
                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
                return farmerRepository.findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);
        }

        private AdvisoryResponse toResponse(Advisory advisory) {
                return toResponse(advisory, advisory.getCrop(), advisory.getGeneratedAt(),
                                advisory.getRecommendations().stream()
                                                .map(recommendation -> new AdvisoryRecommendation(
                                                                recommendation.getCategory(),
                                                                recommendation.getSeverity(),
                                                                recommendation.getTitle(),
                                                                recommendation.getRecommendation(),
                                                                recommendation.getReason()))
                                                .toList());
        }

        private AdvisoryResponse toResponse(Advisory advisory, Crop crop,
                        LocalDateTime generatedAt, java.util.List<AdvisoryRecommendation> recommendations) {
                return new AdvisoryResponse(
                                advisory == null ? null : advisory.getId(),
                                crop.getId(),
                                crop.getCropName(),
                                crop.getCropStage(),
                                generatedAt,
                                recommendations);
        }

        public static class FarmerProfileNotFoundException extends RuntimeException {
        }

        public static class CropNotFoundException extends RuntimeException {
        }

        public static class AdvisoryNotFoundException extends RuntimeException {
        }
}
