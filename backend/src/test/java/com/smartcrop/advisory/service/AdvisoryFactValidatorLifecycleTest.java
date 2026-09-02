package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.crop.service.CropLifecycle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdvisoryFactValidatorLifecycleTest {

    @Test
    void prePlantingHeavyRainRejectsEstablishedCropCare() {
        AdvisoryFactValidator validator = new AdvisoryFactValidator(null, null, null, null,
                CropLifecycle.NOT_YET_PLANTED);
        List<AdvisoryRecommendation> recommendations = List.of(
                recommendation("Prepare drainage before planting."),
                recommendation("Water the seedlings every morning."));

        assertThrows(AdvisoryFactValidator.AdvisoryValidationException.class,
                () -> validator.validate(recommendations));
    }

    @Test
    void prePlantingRejectsDiseaseTreatmentAndAllowsPreparation() {
        AdvisoryFactValidator validator = new AdvisoryFactValidator(null, null, null, null,
                CropLifecycle.NOT_YET_PLANTED);

        assertDoesNotThrow(() -> validator.validate(List.of(
                recommendation("Prepare the field and clear drainage before planting."))));

        assertThrows(AdvisoryFactValidator.AdvisoryValidationException.class,
                () -> validator.validate(List.of(
                        recommendation("Treat the tomato disease on the crop."))));
    }

    @Test
    void growingHeavyRainAllowsRelevantCropCare() {
        AdvisoryFactValidator validator = new AdvisoryFactValidator(null, null, null, null,
                CropLifecycle.GROWING);
        List<AdvisoryRecommendation> recommendations = List.of(
                recommendation("Check field drainage after heavy rain."),
                recommendation("Irrigate the crop only when the soil needs it."));

        assertDoesNotThrow(() -> validator.validate(recommendations));
    }

    @Test
    void oneUsefulRecommendationIsAccepted() {
        AdvisoryFactValidator validator = new AdvisoryFactValidator(null, null, null, null,
                CropLifecycle.NEAR_HARVEST);

        assertDoesNotThrow(() -> validator.validate(List.of(
                recommendation("Check whether the crop is ready for harvest."))));
    }

    @Test
    void repetitiveDrainageActionsAreRejected() {
        AdvisoryFactValidator validator = new AdvisoryFactValidator(null, null, null, null,
                CropLifecycle.NOT_YET_PLANTED);
        List<AdvisoryRecommendation> recommendations = List.of(
                recommendation("Check drainage around the field."),
                recommendation("Clear drainage channels before planting."));

        assertThrows(AdvisoryFactValidator.AdvisoryValidationException.class,
                () -> validator.validate(recommendations));
    }

    private AdvisoryRecommendation recommendation(String text) {
        return new AdvisoryRecommendation("WEATHER", "ADVISORY", "Field action", text,
                "Heavy rain makes this useful.");
    }
}
