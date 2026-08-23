package com.smartcrop.dev.risk;

import com.smartcrop.risk.dto.AssessRiskRequest;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.service.RiskService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api/dev/risk")
public class DevelopmentRiskController {

    private final RiskService riskService;

    public DevelopmentRiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @PostMapping("/assess")
    @PreAuthorize("hasRole('FARMER')")
    public RiskAssessmentResponse assessRisk(
            @Valid @RequestBody AssessRiskRequest request,
            Authentication authentication) {
        return riskService.assessRisk(request, authentication);
    }
}
