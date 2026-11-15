package com.mindsmart.repository;

import com.mindsmart.entity.Internship;
import com.mindsmart.entity.WorkMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByCompanyId(Long companyId);

    List<Internship> findByCompanyUserEmail(String email);

    List<Internship> findByIsApprovedTrueAndIsActiveTrueOrderByCreatedAtDesc();

    List<Internship> findByIsApprovedFalseOrderByCreatedAtDesc();

    List<Internship> findByIsReportedTrueOrderByCreatedAtDesc();

    long countByIsApprovedFalse();

    long countByIsReportedTrue();

    @Query("SELECT DISTINCT i FROM Internship i " +
           "WHERE i.isApproved = true AND i.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(i.company.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR EXISTS (SELECT s FROM i.requiredSkills s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "AND (:category IS NULL OR :category = '' OR i.category = :category) " +
           "AND (:workMode IS NULL OR i.workMode = :workMode) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:minStipend IS NULL OR i.stipend >= :minStipend) " +
           "ORDER BY i.createdAt DESC")
    List<Internship> searchInternships(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("workMode") WorkMode workMode,
            @Param("location") String location,
            @Param("minStipend") Integer minStipend
    );
}
