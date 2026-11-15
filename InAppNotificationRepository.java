package com.mindsmart.repository;

import com.mindsmart.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    List<InAppNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<InAppNotification> findByUserEmailOrderByCreatedAtDesc(String email);
    long countByUserIdAndIsReadFalse(Long userId);
    long countByUserEmailAndIsReadFalse(String email);
}
