package com.mindsmart.repository;

import com.mindsmart.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findAllByOrderBySentAtDesc();
    List<EmailLog> findTop20ByOrderBySentAtDesc();
    List<EmailLog> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);
}
