package com.mindsmart.service;

import com.mindsmart.entity.InAppNotification;
import com.mindsmart.entity.User;
import com.mindsmart.repository.InAppNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final InAppNotificationRepository notificationRepository;

    public NotificationService(InAppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public InAppNotification notify(User user, String title, String message, String type) {
        InAppNotification notification = new InAppNotification(user, title, message, type);
        return notificationRepository.save(notification);
    }

    public List<InAppNotification> getUserNotifications(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserEmailAndIsReadFalse(userEmail);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        List<InAppNotification> list = notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        for (InAppNotification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
