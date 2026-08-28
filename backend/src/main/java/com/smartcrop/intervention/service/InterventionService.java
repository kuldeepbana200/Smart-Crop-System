package com.smartcrop.intervention.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.intervention.dto.CreateInterventionRequest;
import com.smartcrop.intervention.dto.InterventionResponse;
import com.smartcrop.intervention.dto.UpdateInterventionRequest;
import com.smartcrop.intervention.entity.Intervention;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;
import com.smartcrop.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final DistressAlertRepository distressAlertRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public InterventionService(
            InterventionRepository interventionRepository,
            DistressAlertRepository distressAlertRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.interventionRepository = interventionRepository;
        this.distressAlertRepository = distressAlertRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public InterventionService(
            InterventionRepository interventionRepository,
            DistressAlertRepository distressAlertRepository,
            UserRepository userRepository) {
        this(interventionRepository, distressAlertRepository, userRepository, null);
    }

    @Transactional
    public InterventionResponse create(
            Long alertId,
            CreateInterventionRequest request,
            Authentication authentication) {
        User officer = findAuthenticatedOfficer(authentication);
        DistressAlert alert = findAlert(alertId);
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new DistressAlertAlreadyResolvedException();
        }
        Intervention intervention = new Intervention(
                null,
                alert,
                officer,
                request.type(),
                request.description().trim(),
                InterventionStatus.PLANNED,
                null,
                null,
                null);
        Intervention savedIntervention = interventionRepository.save(intervention);
        if (notificationService != null) {
            notificationService.notifyInterventionCreated(savedIntervention);
        }
        return toResponse(savedIntervention);
    }

    @Transactional(readOnly = true)
    public List<InterventionResponse> getForAlert(Long alertId, Authentication authentication) {
        findAuthenticatedOfficer(authentication);
        findAlert(alertId);
        return interventionRepository.findByDistressAlertIdOrderByCreatedAtDesc(alertId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InterventionResponse getById(Long interventionId, Authentication authentication) {
        findAuthenticatedOfficer(authentication);
        return toResponse(interventionRepository.findById(interventionId)
                .orElseThrow(InterventionNotFoundException::new));
    }

    @Transactional
    public InterventionResponse update(
            Long interventionId,
            UpdateInterventionRequest request,
            Authentication authentication) {
        findAuthenticatedOfficer(authentication);
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(InterventionNotFoundException::new);
        try {
            intervention.update(
                    request.status(),
                    request.description() == null ? null : request.description().trim(),
                    LocalDateTime.now());
        } catch (Intervention.InvalidInterventionTransitionException exception) {
            throw new InvalidInterventionTransitionException();
        }
        Intervention savedIntervention = interventionRepository.save(intervention);
        if (notificationService != null) {
            if (savedIntervention.getStatus() == InterventionStatus.COMPLETED) {
                notificationService.notifyInterventionCompleted(savedIntervention);
            } else if (savedIntervention.getStatus() == InterventionStatus.CANCELLED) {
                notificationService.notifyInterventionCancelled(savedIntervention);
            }
        }
        return toResponse(savedIntervention);
    }

    private DistressAlert findAlert(Long alertId) {
        return distressAlertRepository.findById(alertId)
                .orElseThrow(DistressAlertNotFoundException::new);
    }

    private User findAuthenticatedOfficer(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        if (user.getRole() != Role.OFFICER && user.getRole() != Role.ADMIN) {
            throw new OfficerAccessDeniedException();
        }
        return user;
    }

    private InterventionResponse toResponse(Intervention intervention) {
        DistressAlert alert = intervention.getDistressAlert();
        return new InterventionResponse(
                intervention.getId(),
                alert.getId(),
                alert.getFarmer().getId(),
                alert.getCrop().getId(),
                alert.getCrop().getCropName(),
                intervention.getOfficer().getId(),
                intervention.getType(),
                intervention.getDescription(),
                intervention.getStatus(),
                intervention.getCreatedAt(),
                intervention.getUpdatedAt(),
                intervention.getCompletedAt());
    }

    public static class DistressAlertNotFoundException extends RuntimeException {
    }

    public static class InterventionNotFoundException extends RuntimeException {
    }

    public static class InvalidInterventionTransitionException extends RuntimeException {
    }

    public static class DistressAlertAlreadyResolvedException extends RuntimeException {
    }

    public static class OfficerAccessDeniedException extends RuntimeException {
    }
}
