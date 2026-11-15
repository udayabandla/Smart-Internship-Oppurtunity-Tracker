package com.mindsmart.service;

import com.mindsmart.dto.InternshipResponseDto;
import com.mindsmart.dto.MatchResultDto;
import com.mindsmart.entity.Internship;
import com.mindsmart.entity.StudentProfile;
import com.mindsmart.entity.User;
import com.mindsmart.entity.WorkMode;
import com.mindsmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final SavedInternshipRepository savedInternshipRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchingService matchingService;

    public InternshipService(InternshipRepository internshipRepository,
                             StudentProfileRepository studentProfileRepository,
                             UserRepository userRepository,
                             SavedInternshipRepository savedInternshipRepository,
                             ApplicationRepository applicationRepository,
                             MatchingService matchingService) {
        this.internshipRepository = internshipRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.savedInternshipRepository = savedInternshipRepository;
        this.applicationRepository = applicationRepository;
        this.matchingService = matchingService;
    }

    public List<InternshipResponseDto> searchInternships(String keyword,
                                                         String category,
                                                         String workModeStr,
                                                         String location,
                                                         Integer minStipend,
                                                         String currentUserEmail) {
        WorkMode workMode = null;
        if (workModeStr != null && !workModeStr.isBlank()) {
            try {
                workMode = WorkMode.valueOf(workModeStr);
            } catch (Exception ignored) {}
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cat = (category != null && !category.isBlank()) ? category.trim() : null;
        String loc = (location != null && !location.isBlank()) ? location.trim() : null;

        List<Internship> list = internshipRepository.searchInternships(kw, cat, workMode, loc, minStipend);

        // Fetch student profile if authenticated to compute personalized match %
        StudentProfile studentProfile = null;
        Long currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            Optional<User> uOpt = userRepository.findByEmail(currentUserEmail);
            if (uOpt.isPresent()) {
                currentUserId = uOpt.get().getId();
                studentProfile = studentProfileRepository.findByUserId(currentUserId).orElse(null);
            }
        }

        final StudentProfile finalProfile = studentProfile;
        final Long finalUserId = currentUserId;

        return list.stream().map(in -> {
            MatchResultDto match = null;
            boolean isSaved = false;
            boolean hasApplied = false;
            String status = null;

            if (finalProfile != null && finalUserId != null) {
                match = matchingService.calculateMatch(finalProfile.getSkills(), in.getRequiredSkills());
                isSaved = savedInternshipRepository.existsByUserIdAndInternshipId(finalUserId, in.getId());
                var appOpt = applicationRepository.findByInternshipIdAndStudentId(in.getId(), finalUserId);
                if (appOpt.isPresent()) {
                    hasApplied = true;
                    status = appOpt.get().getStatus().name();
                }
            }

            InternshipResponseDto dto = toDto(in);
            dto.setMatchResult(match);
            dto.setSaved(isSaved);
            dto.setHasApplied(hasApplied);
            dto.setApplicationStatus(status);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public InternshipResponseDto getInternshipDetails(Long id, String currentUserEmail) {
        Internship in = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + id));

        in.setViewsCount(in.getViewsCount() + 1);
        internshipRepository.save(in);

        InternshipResponseDto dto = toDto(in);

        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            userRepository.findByEmail(currentUserEmail).ifPresent(user -> {
                studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                    dto.setMatchResult(matchingService.calculateMatch(profile.getSkills(), in.getRequiredSkills()));
                });
                dto.setSaved(savedInternshipRepository.existsByUserIdAndInternshipId(user.getId(), in.getId()));
                var appOpt = applicationRepository.findByInternshipIdAndStudentId(in.getId(), user.getId());
                if (appOpt.isPresent()) {
                    dto.setHasApplied(true);
                    dto.setApplicationStatus(appOpt.get().getStatus().name());
                }
            });
        }

        return dto;
    }

    @Transactional
    public void reportInternship(Long id, String reason) {
        Internship in = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + id));
        in.setReported(true);
        in.setReportReason(reason != null ? reason : "Reported by user as potentially misleading or inappropriate");
        internshipRepository.save(in);
    }

    public List<InternshipResponseDto> compareInternships(List<Long> ids, String currentUserEmail) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Internship> list = internshipRepository.findAllById(ids);
        StudentProfile studentProfile = null;
        Long currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            Optional<User> uOpt = userRepository.findByEmail(currentUserEmail);
            if (uOpt.isPresent()) {
                currentUserId = uOpt.get().getId();
                studentProfile = studentProfileRepository.findByUserId(currentUserId).orElse(null);
            }
        }

        final StudentProfile finalProfile = studentProfile;
        final Long finalUserId = currentUserId;

        return list.stream().map(in -> {
            MatchResultDto match = null;
            boolean isSaved = false;
            boolean hasApplied = false;

            if (finalProfile != null && finalUserId != null) {
                match = matchingService.calculateMatch(finalProfile.getSkills(), in.getRequiredSkills());
                isSaved = savedInternshipRepository.existsByUserIdAndInternshipId(finalUserId, in.getId());
                hasApplied = applicationRepository.existsByInternshipIdAndStudentId(in.getId(), finalUserId);
            }

            InternshipResponseDto dto = toDto(in);
            dto.setMatchResult(match);
            dto.setSaved(isSaved);
            dto.setHasApplied(hasApplied);
            return dto;
        }).collect(Collectors.toList());
    }

    private InternshipResponseDto toDto(Internship in) {
        InternshipResponseDto dto = new InternshipResponseDto();
        dto.setId(in.getId());
        dto.setCompanyId(in.getCompany().getId());
        dto.setCompanyName(in.getCompany().getCompanyName());
        dto.setCompanyLogo(in.getCompany().getLogoUrl());
        dto.setCompanyLocation(in.getCompany().getLocation());
        dto.setCompanyVerified(in.getCompany().isVerified());
        dto.setTitle(in.getTitle());
        dto.setCategory(in.getCategory());
        dto.setWorkMode(in.getWorkMode());
        dto.setLocation(in.getLocation());
        dto.setDuration(in.getDuration());
        dto.setStipend(in.getStipend());
        dto.setOpenings(in.getOpenings());
        dto.setDescription(in.getDescription());
        dto.setEligibility(in.getEligibility());
        dto.setDeadline(in.getDeadline());
        dto.setActive(in.isActive());
        dto.setApproved(in.isApproved());
        dto.setReported(in.isReported());
        dto.setReportReason(in.getReportReason());
        dto.setViewsCount(in.getViewsCount());
        dto.setRequiredSkills(in.getRequiredSkills());
        dto.setCreatedAt(in.getCreatedAt());
        return dto;
    }
}
