package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryResponse;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
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

import java.time.LocalDateTime;

@Service
public class AdvisoryService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final WeatherService weatherService;
    private final AdvisoryRuleEngine advisoryRuleEngine;

    public AdvisoryService(
            UserRepository userRepository,
            FarmerRepository farmerRepository,
            CropRepository cropRepository,
            WeatherService weatherService,
            AdvisoryRuleEngine advisoryRuleEngine) {
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.weatherService = weatherService;
        this.advisoryRuleEngine = advisoryRuleEngine;
    }

    @Transactional(readOnly = true)
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
        return new AdvisoryResponse(
                crop.getId(),
                crop.getCropName(),
                crop.getCropStage(),
                LocalDateTime.now(),
                advisoryRuleEngine.generate(crop, weather));
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class CropNotFoundException extends RuntimeException {
    }
}
