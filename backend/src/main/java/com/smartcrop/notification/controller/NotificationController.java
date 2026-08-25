package com.smartcrop.notification.controller;

import com.smartcrop.notification.dto.NotificationResponse;
import com.smartcrop.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAll(Authentication authentication) {
        return notificationService.getAll(authentication);
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnread(Authentication authentication) {
        return notificationService.getUnread(authentication);
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(
            @PathVariable Long id,
            Authentication authentication) {
        return notificationService.getById(id, authentication);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        return notificationService.markAsRead(id, authentication);
    }
}
