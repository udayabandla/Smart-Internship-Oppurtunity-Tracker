package com.mindsmart.repository;

import com.mindsmart.entity.SavedInternship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedInternshipRepository extends JpaRepository<SavedInternship, Long> {
    List<SavedInternship> findByUserIdOrderBySavedAtDesc(Long userId);
    List<SavedInternship> findByUserEmailOrderBySavedAtDesc(String email);
    boolean existsByUserIdAndInternshipId(Long userId, Long internshipId);
    Optional<SavedInternship> findByUserIdAndInternshipId(Long userId, Long internshipId);
    void deleteByUserIdAndInternshipId(Long userId, Long internshipId);
}
