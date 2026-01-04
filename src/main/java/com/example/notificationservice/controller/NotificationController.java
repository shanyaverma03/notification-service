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
            @RequestBody Notification notification
    ) {
        notification.setStatus(Status.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return ResponseEntity.ok(saved.getId());
    }
}
