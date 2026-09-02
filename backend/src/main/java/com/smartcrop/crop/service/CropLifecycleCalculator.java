package com.smartcrop.crop.service;

import com.smartcrop.crop.entity.Crop;

import java.time.LocalDate;

public final class CropLifecycleCalculator {

    private static final int NEAR_HARVEST_DAYS = 14;

    private CropLifecycleCalculator() {
    }

    public static CropLifecycle calculate(Crop crop, LocalDate today) {
        if (crop.getSowingDate() != null && crop.getSowingDate().isAfter(today)) {
            return CropLifecycle.NOT_YET_PLANTED;
        }

        if (crop.getExpectedHarvestDate() != null && crop.getExpectedHarvestDate().isBefore(today)) {
            return CropLifecycle.COMPLETED;
        }

        if (crop.getExpectedHarvestDate() != null
                && !crop.getExpectedHarvestDate().isAfter(today.plusDays(NEAR_HARVEST_DAYS))) {
            return CropLifecycle.NEAR_HARVEST;
        }

        return CropLifecycle.GROWING;
    }
}
