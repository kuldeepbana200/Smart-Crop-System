package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.advisory.dto.AdvisoryResponse;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdvisoryService {

        private final UserRepository userRepository;
        private final FarmerRepository farmerRepository;
        private final CropRepository cropRepository;
        private final WeatherService weatherService;
        private final AdvisoryRuleEngine advisoryRuleEngine;
        private final AdvisoryRepository advisoryRepository;

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

        @Transactional
        public AdvisoryResponse generateAdvisory(
                        GenerateAdvisoryRequest request,
                        Authentication authentication) {

                User user = findAuthenticatedUser(authentication);

                Farmer farmer = farmerRepository
                                .findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);

                Crop crop = cropRepository
                                .findByIdAndFarmerId(request.cropId(), farmer.getId())
                                .orElseThrow(CropNotFoundException::new);

                WeatherForecastResponse weather = weatherService.getForecast(authentication);

                validateWeather(weather);

                List<AdvisoryRecommendation> recommendations = advisoryRuleEngine.generate(crop, weather);

                Advisory advisory = new Advisory();

                advisory.setCrop(crop);
                String language = request.language();
                if (language == null || language.isBlank()) {
                        language = "en";
                }
                advisory.setLanguage(language);

                for (AdvisoryRecommendation recommendation : recommendations) {

                        com.smartcrop.advisory.entity.AdvisoryRecommendation entityRecommendation = new com.smartcrop.advisory.entity.AdvisoryRecommendation(
                                        null,
                                        null,
                                        recommendation.category(),
                                        recommendation.severity(),
                                        recommendation.title(),
                                        recommendation.recommendation(),
                                        recommendation.reason());

                        advisory.addRecommendation(entityRecommendation);
                }

                Advisory savedAdvisory = advisoryRepository.save(advisory);

                return toResponse(savedAdvisory);
        }

        @Transactional(readOnly = true)
        public List<AdvisoryResponse> getMyAdvisories(
                        Authentication authentication,
                        String language) {

                Farmer farmer = findAuthenticatedFarmer(authentication);
                String lang = (language == null || language.isBlank()) ? "en" : language.toLowerCase();
                List<Advisory> advisories = advisoryRepository
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(farmer.getId(), lang);
                if (advisories.isEmpty() && !lang.equals("en")) {
                        advisories = advisoryRepository
                                        .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(farmer.getId(), "en");
                }
                return advisories.stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public AdvisoryResponse getMyAdvisory(
                        Long advisoryId,
                        Authentication authentication,
                        String language) {

                Farmer farmer = findAuthenticatedFarmer(authentication);
                String lang = (language == null || language.isBlank()) ? "en" : language.toLowerCase();
                Advisory advisory = advisoryRepository
                                .findByIdAndCropFarmerIdAndLanguage(advisoryId, farmer.getId(), lang)
                                .or(() -> {
                                        if (lang.equals("en")) {
                                                return Optional.empty();
                                        }
                                        return advisoryRepository
                                                        .findByIdAndCropFarmerIdAndLanguage(advisoryId, farmer.getId(),
                                                                        "en");
                                })
                                .orElseThrow(AdvisoryNotFoundException::new);

                return toResponse(advisory);
        }

        private User findAuthenticatedUser(
                        Authentication authentication) {

                if (authentication == null ||
                                authentication.getName() == null ||
                                authentication.getName().isBlank()) {

                        throw new UsernameNotFoundException(
                                        "Authenticated user not found");
                }

                return userRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Authenticated user not found"));
        }

        private Farmer findAuthenticatedFarmer(
                        Authentication authentication) {

                User user = findAuthenticatedUser(authentication);

                return farmerRepository
                                .findByUserId(user.getId())
                                .orElseThrow(FarmerProfileNotFoundException::new);
        }

        private void validateWeather(
                        WeatherForecastResponse weather) {

                if (weather == null || weather.daily() == null) {
                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().precipitationProbabilityMax() == null
                                || weather.daily().precipitationProbabilityMax().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().precipitationSum() == null
                                || weather.daily().precipitationSum().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().temperatureMax() == null
                                || weather.daily().temperatureMax().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().temperatureMin() == null
                                || weather.daily().temperatureMin().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().windSpeedMax() == null
                                || weather.daily().windSpeedMax().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }

                if (weather.daily().evapotranspiration() == null
                                || weather.daily().evapotranspiration().isEmpty()) {

                        throw new AdvisoryRuleEngine.InvalidWeatherDataException();
                }
        }

        private AdvisoryResponse toResponse(
                        Advisory advisory) {

                return new AdvisoryResponse(
                                advisory.getId(),
                                advisory.getCrop().getId(),
                                advisory.getCrop().getCropName(),
                                advisory.getCrop().getCropStage(),
                                advisory.getGeneratedAt(),
                                advisory.getRecommendations()
                                                .stream()
                                                .map(recommendation -> new AdvisoryRecommendation(
                                                                recommendation.getCategory(),
                                                                recommendation.getSeverity(),
                                                                recommendation.getTitle(),
                                                                recommendation.getRecommendation(),
                                                                recommendation.getReason()))
                                                .toList());
        }

        public static class FarmerProfileNotFoundException
                        extends RuntimeException {
        }

        public static class CropNotFoundException
                        extends RuntimeException {
        }

        public static class AdvisoryNotFoundException
                        extends RuntimeException {
        }
}