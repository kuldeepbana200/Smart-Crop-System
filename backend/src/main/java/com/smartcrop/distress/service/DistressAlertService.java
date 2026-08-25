package com.smartcrop.distress.service;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.dto.AcknowledgeAlertRequest;
import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.dto.ResolveAlertRequest;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.notification.service.NotificationService;
import com.smartcrop.officer.dto.AssignAlertRequest;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.dto.RiskFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DistressAlertService {

    private final com.smartcrop.distress.repository.DistressAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final NotificationService notificationService;

    @Autowired
    public DistressAlertService(
            com.smartcrop.distress.repository.DistressAlertRepository alertRepository,
            UserRepository userRepository,
            FarmerRepository farmerRepository,
            NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.notificationService = notificationService;
    }

    public DistressAlertService(
            com.smartcrop.distress.repository.DistressAlertRepository alertRepository,
            UserRepository userRepository,
            FarmerRepository farmerRepository) {
        this(alertRepository, userRepository, farmerRepository, null);
    }

    @Transactional
    public void createIfRequired(Farmer farmer, Crop crop, RiskAssessmentResponse assessment) {
        if (!isAlertLevel(assessment.riskLevel()) || assessment.factors().isEmpty()) {
            return;
        }

        String conditionKey = assessment.factors().stream()
                .map(RiskFactor::type)
                .map(type -> type.toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .collect(Collectors.joining("|"));

        if (hasActiveAlert(farmer.getId(), crop.getId(), conditionKey)) {
            return;
        }

        DistressAlert alert = new DistressAlert(
                null,
                farmer,
                crop,
                null,
                assessment.riskScore(),
                assessment.riskLevel(),
                conditionKey,
                assessment.factors().stream()
                        .max(Comparator.comparingInt(RiskFactor::contribution))
                        .map(RiskFactor::type)
                        .orElse("UNKNOWN"),
                serializeFactors(assessment.factors()),
                assessment.recommendedAction(),
                AlertStatus.OPEN,
                null,
                null,
                null,
                null);
        alertRepository.save(alert);
        if (notificationService != null) {
            notificationService.notifyDistressAlertCreated(alert);
        }
    }

    @Transactional(readOnly = true)
    public List<DistressAlertResponse> getFarmerAlerts(Authentication authentication) {
        Farmer farmer = findAuthenticatedFarmer(authentication);
        return alertRepository.findByFarmerIdOrderByCreatedAtDesc(farmer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DistressAlertResponse getFarmerAlert(Long alertId, Authentication authentication) {
        Farmer farmer = findAuthenticatedFarmer(authentication);
        return alertRepository.findByIdAndFarmerId(alertId, farmer.getId())
                .map(this::toResponse)
                .orElseThrow(AlertNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<DistressAlertResponse> getOfficerAlerts(AlertStatus status) {
        List<DistressAlert> alerts = status == null
                ? alertRepository.findAll().stream()
                        .sorted(Comparator.comparing(DistressAlert::getCreatedAt).reversed())
                        .toList()
                : alertRepository.findByStatusOrderByCreatedAtDesc(status);
        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DistressAlertResponse getOfficerAlert(Long alertId) {
        return alertRepository.findById(alertId)
                .map(this::toResponse)
                .orElseThrow(AlertNotFoundException::new);
    }

    @Transactional
    public DistressAlertResponse assign(
            Long alertId, AssignAlertRequest request) {
        DistressAlert alert = findAlert(alertId);
        User officer = userRepository.findById(request.officerId())
                .orElseThrow(AssignedOfficerNotFoundException::new);
        if (officer.getRole() != com.smartcrop.auth.entity.Role.OFFICER) {
            throw new InvalidAssignedOfficerException();
        }
        alert.assignOfficer(officer);
        return toResponse(alertRepository.save(alert));
    }

    @Transactional
    public DistressAlertResponse acknowledge(
            Long alertId, AcknowledgeAlertRequest request, Authentication authentication) {
        DistressAlert alert = findAlert(alertId);
        User officer = findAuthenticatedUser(authentication);
        try {
            alert.acknowledge(officer, request.note().trim(), LocalDateTime.now());
        } catch (DistressAlert.AssignedOfficerConflictException exception) {
            throw new AssignedOfficerConflictException();
        } catch (DistressAlert.InvalidAlertTransitionException exception) {
            throw new InvalidAlertTransitionException();
        }
        DistressAlert savedAlert = alertRepository.save(alert);
        if (notificationService != null) {
            notificationService.notifyDistressAlertAcknowledged(savedAlert);
        }
        return toResponse(savedAlert);
    }

    @Transactional
    public DistressAlertResponse resolve(
            Long alertId, ResolveAlertRequest request, Authentication authentication) {
        DistressAlert alert = findAlert(alertId);
        User officer = findAuthenticatedUser(authentication);
        try {
            alert.resolve(officer, request.note().trim(), LocalDateTime.now());
        } catch (DistressAlert.InvalidAlertTransitionException exception) {
            throw new InvalidAlertTransitionException();
        }
        return toResponse(alertRepository.save(alert));
    }

    private boolean hasActiveAlert(Long farmerId, Long cropId, String conditionKey) {
        return alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                farmerId, cropId, conditionKey, AlertStatus.OPEN).isPresent()
                || alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                        farmerId, cropId, conditionKey, AlertStatus.ACKNOWLEDGED).isPresent();
    }

    private boolean isAlertLevel(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    private Farmer findAuthenticatedFarmer(Authentication authentication) {
        User user = findAuthenticatedUser(authentication);
        if (user.getId() == null) {
            throw new FarmerProfileNotFoundException();
        }
        return farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);
    }

    private User findAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }

    private DistressAlert findAlert(Long alertId) {
        return alertRepository.findById(alertId).orElseThrow(AlertNotFoundException::new);
    }

    private DistressAlertResponse toResponse(DistressAlert alert) {
        return new DistressAlertResponse(
                alert.getId(),
                alert.getFarmer().getId(),
                alert.getCrop().getId(),
                alert.getCrop().getCropName(),
                alert.getRiskScore(),
                alert.getRiskLevel(),
                alert.getDominantFactor(),
                deserializeFactors(alert.getFactorSummary()),
                alert.getRecommendedAction(),
                alert.getStatus(),
                alert.getAssignedOfficer() == null ? null : alert.getAssignedOfficer().getId(),
                alert.getOfficerNote(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt());
    }

    private String serializeFactors(List<RiskFactor> factors) {
        return factors.stream()
                .map(factor -> encode(factor.type()) + ":"
                        + encode(factor.severity()) + ":"
                        + factor.contribution() + ":"
                        + encode(factor.reason()))
                .collect(Collectors.joining(","));
    }

    private List<RiskFactor> deserializeFactors(String factors) {
        if (factors == null || factors.isBlank()) {
            return List.of();
        }
        List<RiskFactor> result = new ArrayList<>();
        for (String value : factors.split(",")) {
            String[] fields = value.split(":", -1);
            if (fields.length != 4) {
                throw new AlertSerializationException();
            }
            try {
                result.add(new RiskFactor(
                        decode(fields[0]),
                        decode(fields[1]),
                        Integer.valueOf(fields[2]),
                        decode(fields[3])));
            } catch (IllegalArgumentException exception) {
                throw new AlertSerializationException();
            }
        }
        return result;
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static class AlertNotFoundException extends RuntimeException {
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class InvalidAlertTransitionException extends RuntimeException {
    }

    public static class AlertSerializationException extends RuntimeException {
    }

    public static class AssignedOfficerNotFoundException extends RuntimeException {
    }

    public static class InvalidAssignedOfficerException extends RuntimeException {
    }

    public static class AssignedOfficerConflictException extends RuntimeException {
    }
}
