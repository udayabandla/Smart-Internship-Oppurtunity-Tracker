package com.mindsmart.repository;

import com.mindsmart.entity.Application;
import com.mindsmart.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    List<Application> findByStudentEmailOrderByAppliedAtDesc(String email);

    List<Application> findByInternshipIdOrderByAppliedAtDesc(Long internshipId);

    List<Application> findByInternshipCompanyIdOrderByAppliedAtDesc(Long companyId);

    List<Application> findByInternshipCompanyUserEmailOrderByAppliedAtDesc(String email);

    boolean existsByInternshipIdAndStudentId(Long internshipId, Long studentId);

    Optional<Application> findByInternshipIdAndStudentId(Long internshipId, Long studentId);

    long countByInternshipCompanyId(Long companyId);

    long countByStatus(ApplicationStatus status);
}
