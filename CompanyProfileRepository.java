package com.mindsmart.repository;

import com.mindsmart.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByUserId(Long userId);
    Optional<CompanyProfile> findByUserEmail(String email);
    List<CompanyProfile> findByIsVerified(boolean isVerified);
    long countByIsVerified(boolean isVerified);
}
