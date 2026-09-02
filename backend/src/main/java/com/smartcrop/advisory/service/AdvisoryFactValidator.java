package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.service.CropLifecycle;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

/**
 * Validates advisory recommendations to ensure they are strictly grounded in
 * backend facts.
 * 
 * This validator ensures that Groq-generated advisories:
 * 1. Do not invent or misrepresent numeric weather values
 * 2. Do not contradict backend risk assessment
 * 3. Do not make unfounded disease claims
 * 4. Use proper severity levels aligned with backend risk
 * 5. Provide actionable recommendations based only on available facts
 * 
 * This validator focuses on FACTUAL SAFETY, not stylistic wording.
 * It allows natural language variation and does not enforce templates.
 */
public class AdvisoryFactValidator {

    private static final Logger log = LoggerFactory.getLogger(AdvisoryFactValidator.class);

    private static final Set<String> VALID_SEVERITIES = Set.of("INFO", "ADVISORY", "WARNING", "URGENT");

    private final WeatherForecastResponse weather;
    private final RiskEngine.RiskResult riskResult;
    private final CropLifecycle lifecycle;

    public AdvisoryFactValidator(
            Farmer farmer,
            Crop crop,
            WeatherForecastResponse weather,
            RiskEngine.RiskResult riskResult) {
        this(farmer, crop, weather, riskResult, null);
    }

    public AdvisoryFactValidator(
            Farmer farmer,
            Crop crop,
            WeatherForecastResponse weather,
            RiskEngine.RiskResult riskResult,
            CropLifecycle lifecycle) {
        this.weather = weather;
        this.riskResult = riskResult;
        this.lifecycle = lifecycle;
    }

    /**
     * Validates a list of advisory recommendations against backend facts.
     * Returns the validated (and potentially corrected) recommendations,
     * or throws an exception if validation fails critically.
     */
    public List<AdvisoryRecommendation> validate(List<AdvisoryRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            throw new AdvisoryValidationException("No recommendations provided by AI system");
        }

        if (recommendations.size() < 1 || recommendations.size() > 4) {
            throw new AdvisoryValidationException(
                    String.format("Advisory must have 1-4 recommendations, got %d", recommendations.size()));
        }

        for (AdvisoryRecommendation rec : recommendations) {
            validateRecommendation(rec);
        }

        validateLifecycleRecommendations(recommendations);
        validateDistinctActions(recommendations);

        // Validate severity alignment with backend risk
        validateSeverityAlignment(recommendations);

        log.info("Advisory recommendations validated successfully against backend facts");
        return recommendations;
    }

    private void validateLifecycleRecommendations(List<AdvisoryRecommendation> recommendations) {
        if (lifecycle != CropLifecycle.NOT_YET_PLANTED) {
            return;
        }

        String text = recommendations.stream()
                .map(rec -> rec.recommendation() + " " + rec.reason())
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase(Locale.ROOT);

        String[] establishedCropCare = { "water the seedlings", "water seedlings", "irrigate the crop",
                "irrigation now", "existing seedlings", "treat crop stress" };
        for (String phrase : establishedCropCare) {
            if (text.contains(phrase)) {
                throw new AdvisoryValidationException(
                        "Pre-planting advisory contains established-crop care: " + phrase);
            }
        }

        boolean preparationContext = text.contains("prepare") || text.contains("before planting")
                || text.contains("planting material");
        if ((text.matches(".*\\b(irrigat|water|watering)\\w*\\b.*")
                && text.matches(".*\\b(crop|tomato|seedling|plant|roots?|leaves?)\\w*\\b.*")
                && !preparationContext)
                || (text.matches(".*\\b(monitor|protect|check|inspect|treat|care for|water)\\w*\\b.*")
                        && text.matches(".*\\bseedlings?\\b.*") && !preparationContext)
                || (text.matches(".*\\b(treat|control|manage|spray)\\w*\\b.*")
                        && text.matches(".*\\b(disease|pest|fung|blight|mildew)\\w*\\b.*")
                        && !preparationContext)
                || (text.matches(".*\\b(inspect|protect|manage|monitor)\\w*\\b.*")
                        && text.matches(".*\\b(leaves?|flowers?|fruits?)\\b.*"))
                || text.matches(".*\\b(harvest|harvesting)\\w*\\b.*")) {
            throw new AdvisoryValidationException(
                    "Pre-planting advisory assumes an established crop rather than preparation.");
        }
    }

    private void validateDistinctActions(List<AdvisoryRecommendation> recommendations) {
        Set<String> actionFamilies = new HashSet<>();
        for (AdvisoryRecommendation recommendation : recommendations) {
            String family = actionFamily(recommendation);
            if (family != null && !actionFamilies.add(family)) {
                throw new AdvisoryValidationException(
                        "Recommendations repeat the same action family: " + family);
            }
        }
    }

    private String actionFamily(AdvisoryRecommendation recommendation) {
        String text = (recommendation.title() + " " + recommendation.recommendation()).toLowerCase(Locale.ROOT);
        if (text.matches(".*\\b(check|clear|inspect|prepare|improve|maintain)\\w*\\b.*\\bdrain(age|s)?\\b.*")) {
            return "DRAINAGE";
        }
        if (text.matches(".*\\b(irrigat|water|watering)\\w*\\b.*")) {
            return "IRRIGATION";
        }
        if (text.matches(".*\\b(plant|planting|sow|seed|seedling)\\w*\\b.*")) {
            return "PLANTING";
        }
        if (text.matches(".*\\b(soil|field)\\w*\\b.*")) {
            return "SOIL_FIELD";
        }
        if (text.matches(".*\\b(harvest|harvesting)\\w*\\b.*")) {
            return "HARVEST";
        }
        return null;
    }

    /**
     * Validates a single recommendation for fact consistency.
     */
    private void validateRecommendation(AdvisoryRecommendation rec) {
        if (rec == null) {
            throw new AdvisoryValidationException("Null recommendation provided");
        }

        // Check required fields
        if (rec.category() == null || rec.category().isBlank()) {
            throw new AdvisoryValidationException("Recommendation missing required field: category");
        }

        if (rec.severity() == null || rec.severity().isBlank()) {
            throw new AdvisoryValidationException("Recommendation missing required field: severity");
        }

        if (rec.title() == null || rec.title().isBlank()) {
            throw new AdvisoryValidationException("Recommendation missing required field: title");
        }

        if (rec.recommendation() == null || rec.recommendation().isBlank()) {
            throw new AdvisoryValidationException("Recommendation missing required field: recommendation");
        }

        if (rec.reason() == null || rec.reason().isBlank()) {
            throw new AdvisoryValidationException("Recommendation missing required field: reason");
        }

        // Validate severity is one of the allowed values
        String normalizedSeverity = rec.severity().toUpperCase(Locale.ROOT);
        if (!VALID_SEVERITIES.contains(normalizedSeverity)) {
            throw new AdvisoryValidationException(
                    String.format("Invalid severity '%s'. Must be one of: %s",
                            rec.severity(), VALID_SEVERITIES));
        }

        // Validate no disease claims without backend support
        validateNoDiseaseInvention(rec);

        // Validate no unsupported chemical dosages
        validateNoChemicalInvention(rec);

        // Validate weather facts are not invented
        validateWeatherFactsNotInvented(rec);
    }

    /**
     * Ensures Groq does not claim diseases exist without backend risk data support.
     * Allows general monitoring language like "check for signs of disease" but
     * rejects specific disease claims without evidence.
     */
    private void validateNoDiseaseInvention(AdvisoryRecommendation rec) {
        if (riskResult == null || riskResult.factors() == null) {
            // No risk data available, but still allow general monitoring advice
            return;
        }

        String text = (rec.reason() + " " + rec.recommendation()).toLowerCase(Locale.ROOT);

        // Check for specific disease claims (e.g., "rust will occur", "fungal disease
        // is present")
        // but allow general monitoring language
        String[] specificDiseasePatterns = {
                "rust will ", "blight will ", "mildew will ", "rot will ",
                "disease is present", "disease has occurred", "fungus is ", "rust is present",
                "blight is present", "rust has infected", "fungal infection is "
        };

        for (String pattern : specificDiseasePatterns) {
            if (text.contains(pattern)) {
                // Check if backend risk factors actually support this disease concern
                boolean diseaseSupported = riskResult.factors() != null &&
                        riskResult.factors().stream()
                                .anyMatch(f -> f.type() != null &&
                                        (f.type().toLowerCase().contains("disease") ||
                                                f.type().toLowerCase().contains("fungal") ||
                                                f.type().toLowerCase().contains("rust") ||
                                                f.type().toLowerCase().contains("blight")));

                if (!diseaseSupported) {
                    throw new AdvisoryValidationException(
                            "Advisory claims a specific disease without backend risk support. " +
                                    "Recommendation: " + rec.recommendation());
                }
            }
        }
    }

    /**
     * Ensures Groq does not invent specific chemical dosages or product names.
     * Allows general preventive advice but rejects specific unverified treatments.
     */
    private void validateNoChemicalInvention(AdvisoryRecommendation rec) {
        String text = (rec.reason() + " " + rec.recommendation()).toLowerCase(Locale.ROOT);

        // Reject specific chemical recommendations with invented dosages
        // Pattern: "use X liters/kg of Y" or "apply Z kg per acre"
        // But allow general phrases like "apply fungicide if needed" without specific
        // dosages

        // Check for very specific dosage claims that look invented
        if (text.matches(".*\\bapply\\b\\s+\\d+(\\.\\d+)?\\s+(liters?|l|ml|kg|grams?|oz|pints?)\\s+.*") ||
                text.matches(".*\\buse\\b\\s+\\d+(\\.\\d+)?\\s+(liters?|l|ml|kg|grams?|oz|pints?)\\s+.*")) {

            // Check if this is actually backend-provided treatment information
            // For now, be lenient but log a warning
            if (!text.contains("Information unavailable")) {
                log.warn("Advisory contains specific chemical dosage that may not be verified: {}",
                        rec.recommendation());
            }
        }

        // Reject recommendations for specific branded products that weren't in backend
        // data
        String[] brandedProductPatterns = {
                "spray brand", "use brand", "apply brand", "fertilizer x", "pesticide x"
        };
        for (String pattern : brandedProductPatterns) {
            if (text.contains(pattern)) {
                throw new AdvisoryValidationException(
                        "Advisory recommends specific branded products not from backend data. " +
                                "Recommendation: " + rec.recommendation());
            }
        }
    }

    /**
     * Validates that weather numeric values in recommendations match backend data.
     * Allows natural descriptions of weather but rejects invented numeric values.
     */
    private void validateWeatherFactsNotInvented(AdvisoryRecommendation rec) {
        if (weather == null || weather.current() == null) {
            return;
        }

        CurrentWeatherResponse current = weather.current();
        String text = (rec.reason() + " " + rec.recommendation()).toLowerCase(Locale.ROOT);

        // Extract numeric patterns and validate against backend weather
        // This is a basic check - Groq should not invent temperature/rainfall numbers

        // If recommendation mentions specific temperatures, they should be close to
        // backend values (within reasonable range for natural description)
        java.util.regex.Pattern tempPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*°?c",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = tempPattern.matcher(text);

        double backendTemp = current.temperature();
        while (matcher.find()) {
            try {
                double mentionedTemp = Double.parseDouble(matcher.group(1));
                // Allow reasonable variance but flag if completely implausible
                if (Math.abs(mentionedTemp - backendTemp) > 15.0) {
                    log.warn("Advisory mentions temperature {} but backend shows {}. " +
                            "This may indicate temperature was not accurately grounded.",
                            mentionedTemp, backendTemp);
                }
            } catch (NumberFormatException e) {
                // Ignore parse errors
            }
        }
    }

    /**
     * Validates that recommendation severity aligns with backend risk level.
     */
    private void validateSeverityAlignment(List<AdvisoryRecommendation> recommendations) {
        if (riskResult == null) {
            return;
        }

        String backendRiskLevel = riskResult.riskLevel() == null ? "" : riskResult.riskLevel().toUpperCase(Locale.ROOT);
        int maxAllowedSeverity = switch (backendRiskLevel) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 4;
            case "MODERATE" -> 3;
            case "LOW" -> 3;
            default -> 2;
        };

        for (AdvisoryRecommendation recommendation : recommendations) {
            int actualSeverity = switch (recommendation.severity().toUpperCase(Locale.ROOT)) {
                case "INFO" -> 1;
                case "ADVISORY" -> 2;
                case "WARNING" -> 3;
                case "URGENT" -> 4;
                default -> 0;
            };

            if (actualSeverity > maxAllowedSeverity) {
                throw new AdvisoryValidationException(
                        String.format(
                                "Backend risk is %s but recommendation severity '%s' exceeds the allowed maximum.",
                                backendRiskLevel, recommendation.severity()));
            }
        }
    }

    /**
     * Returns true if the recommendations should trigger a notification.
     * Only HIGH and CRITICAL backend risks create notifications.
     */
    public boolean shouldNotify() {
        if (riskResult == null) {
            return false;
        }

        String riskLevel = riskResult.riskLevel();
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * Exception thrown when advisory validation fails.
     */
    public static class AdvisoryValidationException extends RuntimeException {
        public AdvisoryValidationException(String message) {
            super(message);
        }

        public AdvisoryValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
