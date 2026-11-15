package com.mindsmart.service;

import com.mindsmart.dto.AdminStatsDto;
import com.mindsmart.dto.ApplicationResponseDto;
import com.mindsmart.dto.CompanyProfileDto;
import com.mindsmart.dto.StudentProfileDto;
import com.mindsmart.entity.*;
import com.mindsmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;

    public AdminService(UserRepository userRepository,
                        StudentProfileRepository studentProfileRepository,
                        CompanyProfileRepository companyProfileRepository,
                        InternshipRepository internshipRepository,
                        ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
    }

    public AdminStatsDto getDashboardStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalStudents(userRepository.countByRole(Role.ROLE_STUDENT));
        stats.setTotalCompanies(userRepository.countByRole(Role.ROLE_COMPANY));
        stats.setTotalInternships(internshipRepository.count());
        stats.setTotalApplications(applicationRepository.count());
        stats.setVerifiedCompanies(companyProfileRepository.countByIsVerified(true));
        stats.setPendingApprovals(internshipRepository.countByIsApprovedFalse());
        stats.setReportedCount(internshipRepository.countByIsReportedTrue());

        List<Application> recent = applicationRepository.findAll().stream()
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .limit(10)
                .collect(Collectors.toList());

        List<ApplicationResponseDto> recentDtos = recent.stream().map(a -> {
            ApplicationResponseDto dto = new ApplicationResponseDto();
            dto.setId(a.getId());
            dto.setInternshipId(a.getInternship().getId());
            dto.setInternshipTitle(a.getInternship().getTitle());
            dto.setCompanyName(a.getInternship().getCompany().getCompanyName());
            dto.setStudentId(a.getStudent().getId());
            dto.setStudentName(a.getStudent().getName());
            dto.setStudentEmail(a.getStudent().getEmail());
            dto.setStatus(a.getStatus());
            dto.setMatchPercentage(a.getMatchPercentage());
            dto.setAppliedAt(a.getAppliedAt());
            return dto;
        }).collect(Collectors.toList());

        stats.setRecentApplications(recentDtos);
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<CompanyProfileDto> getAllCompanies() {
        return companyProfileRepository.findAll().stream().map(p -> {
            CompanyProfileDto dto = new CompanyProfileDto();
            dto.setId(p.getId());
            dto.setUserId(p.getUser().getId());
            dto.setCompanyName(p.getCompanyName());
            dto.setRecruiterName(p.getUser().getName());
            dto.setEmail(p.getUser().getEmail());
            dto.setPhone(p.getUser().getPhone());
            dto.setWebsite(p.getWebsite());
            dto.setIndustry(p.getIndustry());
            dto.setLocation(p.getLocation());
            dto.setDescription(p.getDescription());
            dto.setLogoUrl(p.getLogoUrl());
            dto.setVerified(p.isVerified());
            dto.setActiveInternshipsCount(internshipRepository.findByCompanyId(p.getId()).size());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void verifyCompany(Long companyId, boolean verify) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        company.setVerified(verify);
        companyProfileRepository.save(company);
    }

    @Transactional
    public void approveInternship(Long internshipId, boolean approve) {
        Internship in = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));
        in.setApproved(approve);
        internshipRepository.save(in);
    }

    public List<Internship> getReportedInternships() {
        return internshipRepository.findByIsReportedTrueOrderByCreatedAtDesc();
    }

    public List<Internship> getPendingApprovalInternships() {
        return internshipRepository.findByIsApprovedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void resolveReport(Long internshipId, boolean dismiss) {
        Internship in = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));
        if (dismiss) {
            in.setReported(false);
            in.setReportReason(null);
            internshipRepository.save(in);
        } else {
            // Delete reported spam internship
            internshipRepository.delete(in);
        }
    }
}
