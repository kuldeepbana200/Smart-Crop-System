package com.smartcrop.notification.dto;

import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long distressAlertId,
        Long interventionId,
        Long farmerId,
        Long cropId,
        String cropName,
        NotificationType type,
        String title,
        String message,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime readAt) {
}
