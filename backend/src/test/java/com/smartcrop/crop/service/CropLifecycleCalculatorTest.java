package com.smartcrop.crop.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.farmer.entity.Farmer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropLifecycleCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 2);

    @Test
    void futurePlantingDatesAreNotYetPlanted() {
        assertEquals(CropLifecycle.NOT_YET_PLANTED, lifecycle("2026-09-03", "2027-01-10"));
        assertEquals(CropLifecycle.NOT_YET_PLANTED, lifecycle("2026-10-01", "2027-01-10"));
    }

    @Test
    void todayAndPastPlantingDatesAreGrowingWhenHarvestIsAhead() {
        assertEquals(CropLifecycle.GROWING, lifecycle("2026-09-02", "2027-01-10"));
        assertEquals(CropLifecycle.GROWING, lifecycle("2026-08-01", "2027-01-10"));
    }

    @Test
    void nearAndPastHarvestDatesHaveDeterministicStatuses() {
        assertEquals(CropLifecycle.NEAR_HARVEST, lifecycle("2026-08-01", "2026-09-10"));
        assertEquals(CropLifecycle.COMPLETED, lifecycle("2026-08-01", "2026-09-01"));
    }

    private CropLifecycle lifecycle(String sowingDate, String harvestDate) {
        User user = new User(1L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        Farmer farmer = new Farmer(2L, user, "Pune", "Maharashtra", 18.5, 73.8, 2.0);
        Crop crop = new Crop(3L, farmer, "Tomato", "SEEDING", LocalDate.parse(sowingDate), LocalDate.parse(harvestDate),
                null);
        return CropLifecycleCalculator.calculate(crop, TODAY);
    }
}
