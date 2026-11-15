package com.mindsmart.service;

import com.mindsmart.dto.*;
import com.mindsmart.entity.*;
import com.mindsmart.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchingService matchingService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${mindsmart.upload.dir:uploads}")
    private String uploadBaseDir;

    public CompanyService(UserRepository userRepository,
                          CompanyProfileRepository companyProfileRepository,
                          StudentProfileRepository studentProfileRepository,
                          InternshipRepository internshipRepository,
                          ApplicationRepository applicationRepository,
                          MatchingService matchingService,
                          EmailService emailService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
        this.matchingService = matchingService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public CompanyProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company user not found: " + email));

        CompanyProfile profile = companyProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> companyProfileRepository.save(new CompanyProfile(user, user.getName())));

        return toCompanyDto(user, profile);
    }

    @Transactional
    public CompanyProfileDto updateProfile(String email, CompanyProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company user not found: " + email));

        if (dto.getRecruiterName() != null) user.setName(dto.getRecruiterName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        userRepository.save(user);

        CompanyProfile profile = companyProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> new CompanyProfile(user, dto.getCompanyName()));

        if (dto.getCompanyName() != null) profile.setCompanyName(dto.getCompanyName());
        if (dto.getRecruiterName() != null) profile.setRecruiterName(dto.getRecruiterName());
        if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());
        if (dto.getIndustry() != null) profile.setIndustry(dto.getIndustry());
        if (dto.getLocation() != null) profile.setLocation(dto.getLocation());
        if (dto.getDescription() != null) profile.setDescription(dto.getDescription());

        companyProfileRepository.save(profile);
        return toCompanyDto(user, profile);
    }

    @Transactional
    public String uploadLogo(String email, MultipartFile file) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        CompanyProfile profile = companyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Company profile not found"));

        Path targetDir = Paths.get(uploadBaseDir, "images").toAbsolutePath().normalize();
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo.png";
        String cleanName = orig.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueName = "company_" + profile.getId() + "_" + System.currentTimeMillis() + "_" + cleanName;
        Path targetPath = targetDir.resolve(uniqueName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String logoUrl = "/uploads/images/" + uniqueName;
        profile.setLogoUrl(logoUrl);
        companyProfileRepository.save(profile);

        return logoUrl;
    }

    @Transactional
    public InternshipResponseDto postInternship(String email, InternshipRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        CompanyProfile company = companyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Company profile not found for user: " + email));

        Internship in = new Internship();
        in.setCompany(company);
        in.setTitle(request.getTitle());
        in.setCategory(request.getCategory());
        in.setWorkMode(request.getWorkMode());
        in.setLocation(request.getLocation());
        in.setDuration(request.getDuration());
        in.setStipend(request.getStipend());
        in.setOpenings(request.getOpenings() != null ? request.getOpenings() : 1);
        in.setDescription(request.getDescription());
        in.setEligibility(request.getEligibility());
        in.setDeadline(request.getDeadline());
        in.setActive(true);
        in.setApproved(true); // Auto-approved for verified/demo companies

        Set<String> skills = new HashSet<>();
        if (request.getRequiredSkills() != null) {
            for (String s : request.getRequiredSkills()) {
                if (s != null && !s.isBlank()) skills.add(s.trim());
            }
        }
        in.setRequiredSkills(skills);

        Internship saved = internshipRepository.save(in);
        return toInternshipDto(saved);
    }

    @Transactional
    public InternshipResponseDto updateInternship(String email, Long internshipId, InternshipRequest request) {
        Internship in = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));

        if (!in.getCompany().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this internship posting!");
        }

        in.setTitle(request.getTitle());
        in.setCategory(request.getCategory());
        in.setWorkMode(request.getWorkMode());
        in.setLocation(request.getLocation());
        in.setDuration(request.getDuration());
        in.setStipend(request.getStipend());
        if (request.getOpenings() != null) in.setOpenings(request.getOpenings());
        in.setDescription(request.getDescription());
        in.setEligibility(request.getEligibility());
        in.setDeadline(request.getDeadline());

        if (request.getRequiredSkills() != null) {
            Set<String> cleanSkills = request.getRequiredSkills().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toSet());
            in.setRequiredSkills(cleanSkills);
        }

        Internship updated = internshipRepository.save(in);
        return toInternshipDto(updated);
    }

    @Transactional
    public void deleteInternship(String email, Long internshipId) {
        Internship in = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));

        if (!in.getCompany().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not own this internship posting!");
        }

        internshipRepository.delete(in);
    }

    public List<InternshipResponseDto> getMyInternships(String email) {
        List<Internship> list = internshipRepository.findByCompanyUserEmail(email);
        return list.stream().map(this::toInternshipDto).collect(Collectors.toList());
    }

    public List<ApplicationResponseDto> getCompanyApplications(String email, Long internshipId) {
        List<Application> apps;
        if (internshipId != null && internshipId > 0) {
            apps = applicationRepository.findByInternshipIdOrderByAppliedAtDesc(internshipId);
        } else {
            apps = applicationRepository.findByInternshipCompanyUserEmailOrderByAppliedAtDesc(email);
        }

        return apps.stream().map(a -> {
            StudentProfile prof = studentProfileRepository.findByUserId(a.getStudent().getId()).orElse(null);
            MatchResultDto match = null;
            if (prof != null) {
                match = matchingService.calculateMatch(prof.getSkills(), a.getInternship().getRequiredSkills());
            }
            return toApplicationDto(a, prof, match);
        }).collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponseDto updateApplicationStatus(String email, Long applicationId, StatusUpdateRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        if (!application.getInternship().getCompany().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized: You do not have permission to manage this application!");
        }

        application.setStatus(request.getStatus());

        if (request.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) {
            application.setInterviewDate(request.getInterviewDate());
            application.setInterviewLink(request.getInterviewLink());
            application.setInterviewNotes(request.getInterviewNotes());
        }

        Application updated = applicationRepository.save(application);

        // Notify student applicant
        String title = "Application Update: " + application.getInternship().getTitle();
        String msg = "Your application status was updated to " + request.getStatus().name() + " by " + application.getInternship().getCompany().getCompanyName();
        if (request.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) {
            msg += ". Interview Date: " + request.getInterviewDate() + ". Check email for meeting link.";
        }
        notificationService.notify(application.getStudent(), title, msg, "STATUS_CHANGE");

        // Send Email notification to candidate
        emailService.sendStatusUpdateEmail(
                application.getStudent().getEmail(),
                application.getStudent().getName(),
                application.getInternship().getTitle(),
                application.getInternship().getCompany().getCompanyName(),
                request.getStatus(),
                request.getInterviewDate(),
                request.getInterviewLink(),
                request.getInterviewNotes()
        );

        StudentProfile prof = studentProfileRepository.findByUserId(application.getStudent().getId()).orElse(null);
        MatchResultDto match = prof != null ? matchingService.calculateMatch(prof.getSkills(), application.getInternship().getRequiredSkills()) : null;

        return toApplicationDto(updated, prof, match);
    }

    private CompanyProfileDto toCompanyDto(User user, CompanyProfile p) {
        CompanyProfileDto dto = new CompanyProfileDto();
        dto.setId(p.getId());
        dto.setUserId(user.getId());
        dto.setCompanyName(p.getCompanyName());
        dto.setRecruiterName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setWebsite(p.getWebsite());
        dto.setIndustry(p.getIndustry());
        dto.setLocation(p.getLocation());
        dto.setDescription(p.getDescription());
        dto.setLogoUrl(p.getLogoUrl());
        dto.setVerified(p.isVerified());
        dto.setActiveInternshipsCount(internshipRepository.findByCompanyId(p.getId()).size());
        dto.setTotalApplicationsCount((int) applicationRepository.countByInternshipCompanyId(p.getId()));
        return dto;
    }

    private InternshipResponseDto toInternshipDto(Internship in) {
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

    private ApplicationResponseDto toApplicationDto(Application a, StudentProfile prof, MatchResultDto match) {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(a.getId());
        dto.setInternshipId(a.getInternship().getId());
        dto.setInternshipTitle(a.getInternship().getTitle());
        dto.setCompanyName(a.getInternship().getCompany().getCompanyName());
        dto.setCompanyLogo(a.getInternship().getCompany().getLogoUrl());

        dto.setStudentId(a.getStudent().getId());
        dto.setStudentName(a.getStudent().getName());
        dto.setStudentEmail(a.getStudent().getEmail());
        dto.setPhone(a.getStudent().getPhone());

        if (prof != null) {
            dto.setStudentCollege(prof.getCollege());
            dto.setStudentDegree(prof.getDegree());
            dto.setStudentDepartment(prof.getDepartment());
        }

        if (a.getResume() != null) {
            dto.setResumeId(a.getResume().getId());
            dto.setResumeFileName(a.getResume().getFileName());
            dto.setResumeUrl("/uploads/resumes/" + new java.io.File(a.getResume().getStoredPath()).getName());
        }

        dto.setMatchPercentage(a.getMatchPercentage());
        dto.setStatus(a.getStatus());
        dto.setCoverLetter(a.getCoverLetter());
        dto.setInterviewDate(a.getInterviewDate());
        dto.setInterviewLink(a.getInterviewLink());
        dto.setInterviewNotes(a.getInterviewNotes());
        dto.setAppliedAt(a.getAppliedAt());
        dto.setUpdatedAt(a.getUpdatedAt());

        if (match != null) {
            dto.setMatchingSkills(match.getMatchingSkills());
            dto.setMissingSkills(match.getMissingSkills());
        }

        return dto;
    }
}
