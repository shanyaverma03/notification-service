package com.example.notificationservice.controller;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.Status;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final RateLimiterService rateLimiterService;

    public NotificationController(
            NotificationRepository notificationRepository,
            RateLimiterService rateLimiterService) {
        this.notificationRepository = notificationRepository;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    public ResponseEntity<?> sendNotification(
            @RequestBody Notification notification) {

        // 🔴 Redis rate limiting
        if (!rateLimiterService.allowRequest(notification.getUserId())) {
            return ResponseEntity
                    .status(429)
                    .body("Rate limit exceeded. Max 5 notifications per minute.");
        }

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

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long notificationId) {
        var notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Notification not found");
        }

        var notification = notificationOpt.get();
        notification.setStatus(Status.READ);
        notificationRepository.save(notification);

        return ResponseEntity.ok("Notification marked as READ");
    }
}
