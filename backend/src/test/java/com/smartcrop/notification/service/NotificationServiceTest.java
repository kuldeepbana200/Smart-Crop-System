package com.smartcrop.notification.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.intervention.entity.Intervention;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.entity.InterventionType;
import com.smartcrop.notification.dto.NotificationResponse;
import com.smartcrop.notification.entity.Notification;
import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.entity.NotificationType;
import com.smartcrop.notification.repository.NotificationRepository;
import com.smartcrop.farmer.entity.Farmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private Authentication authentication;
    private User farmerUser;
    private DistressAlert alert;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = new NotificationService(notificationRepository, userRepository);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("farmer@example.com");
        farmerUser = new User(1L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        Farmer farmer = new Farmer(10L, farmerUser, "Pune", "Maharashtra", 18.5, 73.8, 2.0);
        Crop crop = new Crop(20L, farmer, "Rice", "FLOWERING", null, null, null);
        alert = new DistressAlert(
                30L, farmer, crop, null, 80, "CRITICAL", "EXTREME_HEAT", "EXTREME_HEAT",
                "summary", "Inspect crop.", AlertStatus.OPEN, null, null, null, null);
    }

    @Test
    void createsAlertNotificationAsUnread() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyDistressAlertCreated(alert);

        var captor = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();
        assertEquals(farmerUser, notification.getRecipient());
        assertEquals(alert, notification.getDistressAlert());
        assertEquals(NotificationType.DISTRESS_ALERT_CREATED, notification.getType());
        assertEquals(NotificationStatus.UNREAD, notification.getStatus());
        assertNull(notification.getReadAt());
    }

    @Test
    void createsInterventionNotificationThroughAlertRelationship() {
        User officer = new User(2L, "Officer", "officer@example.com", null, "hash", Role.OFFICER, null, null);
        Intervention intervention = new Intervention(
                40L, alert, officer, InterventionType.FIELD_VISIT, "Visited field.",
                InterventionStatus.COMPLETED, null, null, LocalDateTime.now());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyInterventionCompleted(intervention);

        var captor = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();
        assertEquals(farmerUser, notification.getRecipient());
        assertEquals(alert, notification.getDistressAlert());
        assertEquals(intervention, notification.getIntervention());
        assertEquals(NotificationType.INTERVENTION_COMPLETED, notification.getType());
    }

    @Test
    void mapsAlertSourceFieldsIntoResponse() {
        Notification notification = new Notification(
                50L, farmerUser, alert, null, NotificationType.DISTRESS_ALERT_CREATED,
                "New distress alert", "An alert was created.", NotificationStatus.UNREAD,
                LocalDateTime.now(), null);
        when(notificationRepository.findByIdAndRecipientId(50L, 1L))
                .thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getById(50L, authentication);

        assertEquals(50L, response.id());
        assertEquals(30L, response.distressAlertId());
        assertNull(response.interventionId());
        assertEquals(10L, response.farmerId());
        assertEquals(20L, response.cropId());
        assertEquals("Rice", response.cropName());
        assertEquals(NotificationStatus.UNREAD, response.status());
    }

    @Test
    void listsOnlyAuthenticatedRecipientsNotifications() {
        Notification notification = new Notification(
                50L, farmerUser, alert, null, NotificationType.DISTRESS_ALERT_CREATED,
                "New distress alert", "An alert was created.", NotificationStatus.UNREAD,
                LocalDateTime.now(), null);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response = notificationService.getAll(authentication);

        assertEquals(1, response.size());
        assertEquals(50L, response.get(0).id());
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void unreadNotificationsAreQueriedByRecipientAndStatus() {
        when(notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(
                1L, NotificationStatus.UNREAD)).thenReturn(List.of());

        assertEquals(List.of(), notificationService.getUnread(authentication));
        verify(notificationRepository).findByRecipientIdAndStatusOrderByCreatedAtDesc(
                1L, NotificationStatus.UNREAD);
    }

    @Test
    void otherUsersCannotAccessNotification() {
        when(notificationRepository.findByIdAndRecipientId(50L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotificationService.NotificationNotFoundException.class,
                () -> notificationService.getById(50L, authentication));
    }

    @Test
    void markingUnreadNotificationSetsReadAt() {
        Notification notification = new Notification(
                50L, farmerUser, alert, null, NotificationType.DISTRESS_ALERT_CREATED,
                "New distress alert", "An alert was created.", NotificationStatus.UNREAD,
                LocalDateTime.now(), null);
        when(notificationRepository.findByIdAndRecipientId(50L, 1L))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(50L, authentication);

        assertEquals(NotificationStatus.READ, response.status());
        assertNotNull(response.readAt());
    }

    @Test
    void markingReadNotificationPreservesOriginalReadAt() {
        LocalDateTime originalReadAt = LocalDateTime.now().minusMinutes(5);
        Notification notification = new Notification(
                50L, farmerUser, alert, null, NotificationType.DISTRESS_ALERT_CREATED,
                "New distress alert", "An alert was created.", NotificationStatus.READ,
                LocalDateTime.now().minusHours(1), originalReadAt);
        when(notificationRepository.findByIdAndRecipientId(50L, 1L))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(50L, authentication);

        assertEquals(NotificationStatus.READ, response.status());
        assertEquals(originalReadAt, response.readAt());
    }
}
