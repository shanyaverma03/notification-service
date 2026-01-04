package com.example.notificationservice.controller;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.Status;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<Long> sendNotification(
            @RequestBody Notification notification) {
        notification.setStatus(Status.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return ResponseEntity.ok(saved.getId());
    }

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<?> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "10") int limit) {

        if (limit <= 0) {
            return ResponseEntity.badRequest().body("Limit must be greater than 0");
        }

        var notifications = unreadOnly
                ? notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId, Status.UNREAD)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return ResponseEntity.ok(
                notifications.stream().limit(limit).toList());
    }

}
