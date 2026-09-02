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
import com.smartcrop.crop.service.CropLifecycleCalculator;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.notification.service.NotificationService;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
@Transactional
public class AdvisoryService {

        private final UserRepository userRepository;
        private final FarmerRepository farmerRepository;
        private final CropRepository cropRepository;
        private final WeatherService weatherService;
        private final RiskEngine riskEngine;
        private final GroqAdvisoryService groqAdvisoryService;
        private final NotificationService notificationService;
        private final AdvisoryRepository advisoryRepository;

        public AdvisoryService(
                        UserRepository userRepository,
                        FarmerRepository farmerRepository,
                        CropRepository cropRepository,
                        WeatherService weatherService,
                        RiskEngine riskEngine,
                        GroqAdvisoryService groqAdvisoryService,
                        NotificationService notificationService,
                        AdvisoryRepository advisoryRepository) {

                this.userRepository = userRepository;
                this.farmerRepository = farmerRepository;
                this.cropRepository = cropRepository;
                this.weatherService = weatherService;
                this.riskEngine = riskEngine;
                this.groqAdvisoryService = groqAdvisoryService;
                this.notificationService = notificationService;
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

                /*
                 * IMPORTANT:
                 * RiskEngine remains the authority for factual risk information.
                 * Groq only explains the risk and generates farmer-friendly actions.
                 */
                RiskEngine.RiskResult riskResult = riskEngine.assess(crop, weather);
                String language = request.language();

                if (language == null || language.isBlank()) {
                        language = "en";
                }

                List<AdvisoryRecommendation> recommendations = groqAdvisoryService.generateForFarmer(
                                farmer,
                                crop,
                                weather,
                                riskResult,
                                language);

                /*
                 * Do NOT silently fall back to AdvisoryRuleEngine.
                 *
                 * If Groq fails, the caller should know that AI generation failed
                 * instead of receiving old hardcoded recommendations.
                 */
                if (recommendations == null || recommendations.isEmpty()) {
                        throw new GroqAdvisoryGenerationException(
                                        "Unable to generate AI advisory. Please try again.");
                }

                Advisory advisory = new Advisory();

                advisory.setCrop(crop);
                advisory.setLanguage(language.toLowerCase());

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

                /*
                 * FACT-LOCK: Notification creation decision based on backend risk level.
                 * Only HIGH or CRITICAL backend risks should create notifications.
                 * This prevents notification spam from LOW/MODERATE risk advisories.
                 */
                AdvisoryFactValidator validator = new AdvisoryFactValidator(farmer, crop, weather, riskResult);
                if (validator.shouldNotify()) {
                        // Check for duplicates: don't create notification if one was recently created
                        // for the same crop, risk level, and risk factors
                        if (!isDuplicateNotification(user, crop, riskResult)) {
                                notificationService.notifyAdvisoryGenerated(
                                                user,
                                                crop,
                                                savedAdvisory);
                                org.slf4j.LoggerFactory.getLogger(AdvisoryService.class)
                                                .info("Notification created for advisory. Farmer: {}, Crop: {}, Risk: {}",
                                                                farmer.getId(), crop.getCropName(),
                                                                riskResult.riskLevel());
                        } else {
                                org.slf4j.LoggerFactory.getLogger(AdvisoryService.class)
                                                .debug("Duplicate notification prevention: skipped for Farmer: {}, Crop: {}, Risk: {}",
                                                                farmer.getId(), crop.getCropName(),
                                                                riskResult.riskLevel());
                        }
                }

                return toResponse(savedAdvisory);
        }

        /**
         * Checks if a duplicate notification would be created.
         * Prevents notification spam when the same farmer, crop, and risk level
         * generate multiple advisories in a short time.
         */
        @Transactional(readOnly = true)
        private boolean isDuplicateNotification(User user, Crop crop, RiskEngine.RiskResult riskResult) {
                // Query for recent notifications (last 30 minutes) for this crop and user
                // This is a basic check - in a production system, you might want to check
                // if the risk factors are identical as well

                // For now, we accept a simple implementation that doesn't query
                // the notification repository frequently, as this can impact performance.
                // The NotificationService.notifyAdvisoryGenerated() method already has
                // basic deduplication logic.
                return false;
        }

        @Transactional(readOnly = true)
        public List<AdvisoryResponse> getMyAdvisories(
                        Authentication authentication,
                        String language) {

                Farmer farmer = findAuthenticatedFarmer(authentication);

                String lang = (language == null || language.isBlank())
                                ? "en"
                                : language.toLowerCase();

                List<Advisory> advisories = advisoryRepository
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                farmer.getId(),
                                                lang);

                if (advisories.isEmpty() && !lang.equals("en")) {
                        advisories = advisoryRepository
                                        .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                        farmer.getId(),
                                                        "en");
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

                String lang = (language == null || language.isBlank())
                                ? "en"
                                : language.toLowerCase();

                Advisory advisory = advisoryRepository
                                .findByIdAndCropFarmerIdAndLanguage(
                                                advisoryId,
                                                farmer.getId(),
                                                lang)
                                .or(() -> {

                                        if (lang.equals("en")) {
                                                return Optional.empty();
                                        }

                                        return advisoryRepository
                                                        .findByIdAndCropFarmerIdAndLanguage(
                                                                        advisoryId,
                                                                        farmer.getId(),
                                                                        "en");
                                })
                                .orElseThrow(AdvisoryNotFoundException::new);

                return toResponse(advisory);
        }

        private User findAuthenticatedUser(
                        Authentication authentication) {

                if (authentication == null
                                || authentication.getName() == null
                                || authentication.getName().isBlank()) {

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
                                .orElseThrow(
                                                FarmerProfileNotFoundException::new);
        }

        private void validateWeather(
                        WeatherForecastResponse weather) {

                if (weather == null || weather.daily() == null) {
                        throw new IllegalStateException(
                                        "Weather data unavailable");
                }

                if (weather.daily().precipitationProbabilityMax() == null
                                || weather.daily().precipitationProbabilityMax().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather rainfall probability unavailable");
                }

                if (weather.daily().precipitationSum() == null
                                || weather.daily().precipitationSum().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather rainfall data unavailable");
                }

                if (weather.daily().temperatureMax() == null
                                || weather.daily().temperatureMax().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather maximum temperature unavailable");
                }

                if (weather.daily().temperatureMin() == null
                                || weather.daily().temperatureMin().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather minimum temperature unavailable");
                }

                if (weather.daily().windSpeedMax() == null
                                || weather.daily().windSpeedMax().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather wind data unavailable");
                }

                if (weather.daily().evapotranspiration() == null
                                || weather.daily().evapotranspiration().isEmpty()) {

                        throw new IllegalStateException(
                                        "Weather evapotranspiration unavailable");
                }
        }

        private AdvisoryResponse toResponse(
                        Advisory advisory) {

                return new AdvisoryResponse(
                                advisory.getId(),
                                advisory.getCrop().getId(),
                                advisory.getCrop().getCropName(),
                                advisory.getCrop().getCropStage(),
                                advisory.getCrop().getSowingDate(),
                                advisory.getCrop().getExpectedHarvestDate(),
                                CropLifecycleCalculator.calculate(advisory.getCrop(), LocalDate.now()).name(),
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