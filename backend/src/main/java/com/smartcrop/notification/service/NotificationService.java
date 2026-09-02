package com.smartcrop.notification.service;

import com.smartcrop.advisory.entity.Advisory;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.intervention.entity.Intervention;
import com.smartcrop.notification.dto.NotificationResponse;
import com.smartcrop.notification.entity.Notification;
import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.entity.NotificationType;
import com.smartcrop.notification.repository.NotificationRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;

        public NotificationService(
                        NotificationRepository notificationRepository,
                        UserRepository userRepository) {

                this.notificationRepository = notificationRepository;
                this.userRepository = userRepository;
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> getAll(Authentication authentication) {

                User recipient = findAuthenticatedUser(authentication);

                return notificationRepository
                                .findByRecipientIdOrderByCreatedAtDesc(recipient.getId())
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> getUnread(Authentication authentication) {

                User recipient = findAuthenticatedUser(authentication);

                return notificationRepository
                                .findByRecipientIdAndStatusOrderByCreatedAtDesc(
                                                recipient.getId(),
                                                NotificationStatus.UNREAD)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public NotificationResponse getById(
                        Long notificationId,
                        Authentication authentication) {

                User recipient = findAuthenticatedUser(authentication);

                return toResponse(
                                notificationRepository
                                                .findByIdAndRecipientId(
                                                                notificationId,
                                                                recipient.getId())
                                                .orElseThrow(NotificationNotFoundException::new));
        }

        @Transactional
        public NotificationResponse markAsRead(
                        Long notificationId,
                        Authentication authentication) {

                User recipient = findAuthenticatedUser(authentication);

                Notification notification = notificationRepository
                                .findByIdAndRecipientId(
                                                notificationId,
                                                recipient.getId())
                                .orElseThrow(NotificationNotFoundException::new);

                notification.markAsRead(LocalDateTime.now());

                return toResponse(notificationRepository.save(notification));
        }

        @Transactional
        public void notifyDistressAlertCreated(DistressAlert alert) {

                create(
                                alert.getFarmer().getUser(),
                                alert,
                                null,
                                NotificationType.DISTRESS_ALERT_CREATED,
                                "New distress alert",
                                "A high-risk alert was created for "
                                                + alert.getCrop().getCropName() + ".");
        }

        @Transactional
        public void notifyDistressAlertAcknowledged(DistressAlert alert) {

                create(
                                alert.getFarmer().getUser(),
                                alert,
                                null,
                                NotificationType.DISTRESS_ALERT_ACKNOWLEDGED,
                                "Distress alert acknowledged",
                                "An officer acknowledged the distress alert for "
                                                + alert.getCrop().getCropName() + ".");
        }

        @Transactional
        public void notifyInterventionCreated(Intervention intervention) {

                create(
                                intervention.getDistressAlert().getFarmer().getUser(),
                                intervention.getDistressAlert(),
                                intervention,
                                NotificationType.INTERVENTION_CREATED,
                                "Intervention created",
                                "An officer created an intervention for "
                                                + intervention.getDistressAlert()
                                                                .getCrop()
                                                                .getCropName()
                                                + ".");
        }

        @Transactional
        public void notifyInterventionCompleted(Intervention intervention) {

                create(
                                intervention.getDistressAlert().getFarmer().getUser(),
                                intervention.getDistressAlert(),
                                intervention,
                                NotificationType.INTERVENTION_COMPLETED,
                                "Intervention completed",
                                "The intervention for "
                                                + intervention.getDistressAlert()
                                                                .getCrop()
                                                                .getCropName()
                                                + " was completed.");
        }

        @Transactional
        public void notifyInterventionCancelled(Intervention intervention) {

                create(
                                intervention.getDistressAlert().getFarmer().getUser(),
                                intervention.getDistressAlert(),
                                intervention,
                                NotificationType.INTERVENTION_CANCELLED,
                                "Intervention cancelled",
                                "The intervention for "
                                                + intervention.getDistressAlert()
                                                                .getCrop()
                                                                .getCropName()
                                                + " was cancelled.");
        }

        /**
         * Creates a notification ONLY when the advisory contains HIGH or CRITICAL
         * severity.
         * 
         * This is a defensive check that complements the backend risk check in
         * AdvisoryService.
         * A notification is created only when:
         * 1. Backend RiskEngine determined the risk is HIGH or CRITICAL
         * 2. AND at least one recommendation has HIGH or CRITICAL severity
         * 
         * This prevents notification spam from low-risk advisories.
         */
        @Transactional
        public void notifyAdvisoryGenerated(
                        User farmerUser,
                        Crop crop,
                        Advisory advisory) {

                if (farmerUser == null || crop == null || advisory == null) {
                        org.slf4j.LoggerFactory.getLogger(NotificationService.class)
                                        .warn("notifyAdvisoryGenerated called with null parameters");
                        return;
                }

                if (advisory.getRecommendations() == null
                                || advisory.getRecommendations().isEmpty()) {
                        org.slf4j.LoggerFactory.getLogger(NotificationService.class)
                                        .debug("Advisory has no recommendations, skipping notification");
                        return;
                }

                boolean highSeverity = advisory.getRecommendations()
                                .stream()
                                .map(recommendation -> recommendation.getSeverity())
                                .filter(value -> value != null)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .anyMatch(value -> value.equals("HIGH")
                                                || value.equals("CRITICAL")
                                                || value.equals("URGENT"));

                // Only HIGH and CRITICAL advisories create notifications.
                if (!highSeverity) {
                        org.slf4j.LoggerFactory.getLogger(NotificationService.class)
                                        .debug("Advisory lacks HIGH/CRITICAL severity, skipping notification for crop: {}",
                                                        crop.getCropName());
                        return;
                }

                String highestSeverity = advisory.getRecommendations()
                                .stream()
                                .map(recommendation -> recommendation.getSeverity())
                                .filter(value -> value != null && !value.isBlank())
                                .map(String::toUpperCase)
                                .filter(value -> value.equals("HIGH")
                                                || value.equals("CRITICAL")
                                                || value.equals("URGENT"))
                                .min((left, right) -> Integer.compare(
                                                severityOrder(left),
                                                severityOrder(right)))
                                .orElse("HIGH");

                String title;

                if ("CRITICAL".equals(highestSeverity) || "URGENT".equals(highestSeverity)) {
                        title = "Critical crop advisory";
                } else {
                        title = "High-priority crop advisory";
                }

                String message = "A new "
                                + highestSeverity
                                + " advisory for "
                                + crop.getCropName()
                                + " requires attention at "
                                + (crop.getCropStage() != null ? crop.getCropStage() : "unknown")
                                + " stage.";

                create(
                                farmerUser,
                                null,
                                null,
                                NotificationType.ADVISORY_ALERT,
                                title,
                                message);

                org.slf4j.LoggerFactory.getLogger(NotificationService.class)
                                .info("Advisory notification created for farmer: {}, crop: {}, severity: {}",
                                                farmerUser.getId(), crop.getCropName(), highestSeverity);
        }

        private int severityOrder(String severity) {

                return switch (severity == null
                                ? ""
                                : severity.toUpperCase()) {

                        case "CRITICAL" -> 0;
                        case "HIGH" -> 1;
                        default -> 99;
                };
        }

        private void create(
                        User recipient,
                        DistressAlert alert,
                        Intervention intervention,
                        NotificationType type,
                        String title,
                        String message) {

                Notification notification = new Notification(
                                null,
                                recipient,
                                alert,
                                intervention,
                                type,
                                title,
                                message,
                                NotificationStatus.UNREAD,
                                null,
                                null);

                notificationRepository.save(notification);
        }

        private User findAuthenticatedUser(
                        Authentication authentication) {

                return userRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Authenticated user not found"));
        }

        private NotificationResponse toResponse(
                        Notification notification) {

                DistressAlert alert = notification.getDistressAlert();

                if (alert == null
                                && notification.getIntervention() != null) {

                        alert = notification
                                        .getIntervention()
                                        .getDistressAlert();
                }

                return new NotificationResponse(
                                notification.getId(),

                                alert == null
                                                ? null
                                                : alert.getId(),

                                notification.getIntervention() == null
                                                ? null
                                                : notification.getIntervention().getId(),

                                alert == null
                                                ? null
                                                : alert.getFarmer().getId(),

                                alert == null
                                                ? null
                                                : alert.getCrop().getId(),

                                alert == null
                                                ? null
                                                : alert.getCrop().getCropName(),

                                notification.getType(),
                                notification.getTitle(),
                                notification.getMessage(),
                                notification.getStatus(),
                                notification.getCreatedAt(),
                                notification.getReadAt());
        }

        public static class NotificationNotFoundException
                        extends RuntimeException {
        }
}