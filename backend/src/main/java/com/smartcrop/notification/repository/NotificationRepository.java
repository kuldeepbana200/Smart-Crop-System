package com.smartcrop.notification.repository;

import com.smartcrop.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findTop5ByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndStatus(
            Long recipientId, com.smartcrop.notification.entity.NotificationStatus status);

    List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(
            Long recipientId, com.smartcrop.notification.entity.NotificationStatus status);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
}
