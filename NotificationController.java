package com.mindsmart.controller;

import com.mindsmart.dto.ApiResponse;
import com.mindsmart.entity.EmailLog;
import com.mindsmart.entity.InAppNotification;
import com.mindsmart.service.EmailService;
import com.mindsmart.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    public NotificationController(NotificationService notificationService, EmailService emailService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InAppNotification>>> getNotifications(@AuthenticationPrincipal UserDetails user) {
        try {
            List<InAppNotification> list = notificationService.getUserNotifications(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UserDetails user) {
        try {
            long count = notificationService.getUnreadCount(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Unread count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails user) {
        notificationService.markAllAsRead(user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
    }

    @GetMapping("/recent-emails")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getRecentEmails() {
        List<EmailLog> emails = emailService.getRecentEmailLogs();
        return ResponseEntity.ok(ApiResponse.ok("Recent emails retrieved from Simulator", emails));
    }
}
