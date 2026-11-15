package com.mindsmart.service;

import com.mindsmart.dto.*;
import com.mindsmart.entity.*;
import com.mindsmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final ResumeRepository resumeRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedInternshipRepository savedInternshipRepository;
    private final ResumeParserService resumeParserService;
    private final MatchingService matchingService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public StudentService(UserRepository userRepository,
                          StudentProfileRepository profileRepository,
                          ResumeRepository resumeRepository,
                          InternshipRepository internshipRepository,
                          ApplicationRepository applicationRepository,
                          SavedInternshipRepository savedInternshipRepository,
                          ResumeParserService resumeParserService,
                          MatchingService matchingService,
                          EmailService emailService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.resumeRepository = resumeRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
        this.savedInternshipRepository = savedInternshipRepository;
        this.resumeParserService = resumeParserService;
        this.matchingService = matchingService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public StudentProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        return toDto(user, profile);
    }

    @Transactional
    public StudentProfileDto updateProfile(String email, StudentProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new StudentProfile(user));

        if (dto.getCollege() != null) profile.setCollege(dto.getCollege());
        if (dto.getDegree() != null) profile.setDegree(dto.getDegree());
        if (dto.getDepartment() != null) profile.setDepartment(dto.getDepartment());
        if (dto.getGraduationYear() != null) profile.setGraduationYear(dto.getGraduationYear());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getSkills() != null) profile.setSkills(new HashSet<>(dto.getSkills()));

        profile.setCareerReadinessScore(calculateReadinessScore(user, profile));
        profileRepository.save(profile);

        return toDto(user, profile);
    }

    @Transactional
    public ResumeParseResultDto uploadResume(String email, MultipartFile file) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        // 1. Save resume file to disk
        String storedPath = resumeParserService.saveResumeFile(file, user.getId());

        // 2. Extract text from PDF or DOCX
        String extractedText = resumeParserService.extractText(file);

        // 3. Extract skills from text
        List<String> detectedSkills = resumeParserService.extractSkillsFromText(extractedText);

        // 4. Save Resume record
        Resume resume = new Resume(
                user,
                file.getOriginalFilename(),
                storedPath,
                file.getContentType() != null ? file.getContentType() : "application/pdf",
                file.getSize()
        );
        resume.setExtractedText(extractedText);
        resume.setDetectedSkills(String.join(",", detectedSkills));
        resume = resumeRepository.save(resume);

        // 5. Automatically add newly detected skills to student's profile skills
        Set<String> currentSkills = new HashSet<>(profile.getSkills());
        currentSkills.addAll(detectedSkills);
        profile.setSkills(currentSkills);
        profile.setCareerReadinessScore(calculateReadinessScore(user, profile));
        profileRepository.save(profile);

        // 6. Notify student
        notificationService.notify(
                user,
                "Resume Processed & Skills Extracted",
                "Successfully parsed " + file.getOriginalFilename() + ". Detected " + detectedSkills.size() + " skills: " + String.join(", ", detectedSkills),
                "SUCCESS"
        );

        String preview = extractedText.length() > 300 ? extractedText.substring(0, 300) + "..." : extractedText;

        return new ResumeParseResultDto(
                resume.getId(),
                resume.getFileName(),
                resume.getFileType(),
                resume.getFileSize(),
                "/uploads/resumes/" + new java.io.File(storedPath).getName(),
                detectedSkills,
                preview
        );
    }

    @Transactional
    public Set<String> updateSkills(String email, Set<String> skills) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        profile.setSkills(skills != null ? new HashSet<>(skills) : new HashSet<>());
        profile.setCareerReadinessScore(calculateReadinessScore(user, profile));
        profileRepository.save(profile);

        return profile.getSkills();
    }

    public CareerReadinessDto getCareerReadiness(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        int score = calculateReadinessScore(user, profile);
        profile.setCareerReadinessScore(score);
        profileRepository.save(profile);

        CareerReadinessDto dto = new CareerReadinessDto();
        dto.setOverallScore(score);

        if (score >= 80) {
            dto.setRatingLevel("Top Candidate");
            dto.setLevelBadgeColor("#10b981");
        } else if (score >= 60) {
            dto.setRatingLevel("Job Ready");
            dto.setLevelBadgeColor("#6366f1");
        } else if (score >= 40) {
            dto.setRatingLevel("Developing");
            dto.setLevelBadgeColor("#f59e0b");
        } else {
            dto.setRatingLevel("Beginner");
            dto.setLevelBadgeColor("#ec4899");
        }

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        int profPoints = (profile.getCollege() != null && profile.getDegree() != null && profile.getDepartment() != null) ? 20 : 10;
        Optional<Resume> resumeOpt = resumeRepository.findTopByUserIdOrderByUploadedAtDesc(user.getId());
        int resumePoints = resumeOpt.isPresent() ? 25 : 0;
        int skillPoints = Math.min(25, profile.getSkills().size() * 5);
        long appCount = applicationRepository.findByStudentIdOrderByAppliedAtDesc(user.getId()).size();
        int appPoints = (int) Math.min(15, appCount * 5);
        long saveCount = savedInternshipRepository.findByUserIdOrderBySavedAtDesc(user.getId()).size();
        int savePoints = (int) Math.min(15, saveCount * 5);

        breakdown.put("Profile Details", profPoints);
        breakdown.put("Resume Uploaded", resumePoints);
        breakdown.put("Skills Catalog", skillPoints);
        breakdown.put("Application Activity", appPoints);
        breakdown.put("Saved Bookmarks", savePoints);
        dto.setBreakdown(breakdown);

        List<String> recs = new ArrayList<>();
        if (resumePoints == 0) recs.add("Upload your latest PDF/DOCX resume to unlock full matching score (+25 pts)");
        if (profile.getSkills().size() < 5) recs.add("Add at least " + (5 - profile.getSkills().size()) + " more verified skills to your profile (+5 pts each)");
        if (appCount == 0) recs.add("Apply to at least 2 internships matching your skillset (+10 pts)");
        if (profPoints < 20) recs.add("Complete your college, degree, and department details (+10 pts)");
        if (recs.isEmpty()) recs.add("Your profile is in peak condition! Apply to new openings early to maximize hiring velocity.");
        dto.setActionableRecommendations(recs);

        dto.setTopInDemandSkills(Arrays.asList("Java", "Spring Boot", "React", "Docker", "AWS", "MySQL", "REST API", "Git"));

        return dto;
    }

    public SkillGapAnalysisDto analyzeSkillGap(String email, String targetRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        Set<String> studentSkills = profile.getSkills();

        Set<String> roleSkills;
        if ("Frontend Engineer".equalsIgnoreCase(targetRole)) {
            roleSkills = new LinkedHashSet<>(Arrays.asList("JavaScript", "TypeScript", "React", "HTML", "CSS", "Tailwind CSS", "Git", "REST API"));
        } else if ("Data Scientist".equalsIgnoreCase(targetRole)) {
            roleSkills = new LinkedHashSet<>(Arrays.asList("Python", "Machine Learning", "Pandas", "NumPy", "SQL", "Scikit-Learn", "Data Science", "Git"));
        } else if ("Cloud & DevOps Engineer".equalsIgnoreCase(targetRole)) {
            roleSkills = new LinkedHashSet<>(Arrays.asList("Linux", "Docker", "Kubernetes", "AWS", "CI/CD", "Git", "Shell Scripting", "Python"));
        } else {
            // Default "Full Stack Java Developer"
            targetRole = "Full Stack Java Developer";
            roleSkills = new LinkedHashSet<>(Arrays.asList("Java", "Spring Boot", "MySQL", "REST API", "React", "Git", "Docker", "Microservices"));
        }

        MatchResultDto matchResult = matchingService.calculateMatch(studentSkills, roleSkills);

        SkillGapAnalysisDto dto = new SkillGapAnalysisDto();
        dto.setTargetRole(targetRole);
        dto.setMatchPercentage(matchResult.getMatchPercentage());
        dto.setCurrentSkills(new ArrayList<>(studentSkills));
        dto.setTargetRequiredSkills(new ArrayList<>(roleSkills));
        dto.setMissingSkills(matchResult.getMissingSkills());

        List<SkillGapAnalysisDto.LearningPathwayDto> pathways = new ArrayList<>();
        for (String missing : matchResult.getMissingSkills()) {
            pathways.add(new SkillGapAnalysisDto.LearningPathwayDto(
                    missing,
                    "Master core fundamentals of " + missing + " with practical hands-on exercises.",
                    "Free Tutorial & Documentation",
                    "https://www.google.com/search?q=" + missing.replace(" ", "+") + "+tutorial+for+beginners"
            ));
        }
        dto.setLearningPathways(pathways);

        return dto;
    }

    @Transactional
    public ApplicationResponseDto applyForInternship(String email, ApplicationRequest request) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found: " + email));

        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new RuntimeException("Internship not found: " + request.getInternshipId()));

        if (applicationRepository.existsByInternshipIdAndStudentId(internship.getId(), student.getId())) {
            throw new RuntimeException("You have already applied for this internship!");
        }

        StudentProfile profile = profileRepository.findByUserId(student.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(student)));

        // Calculate rule-driven match percentage
        MatchResultDto matchResult = matchingService.calculateMatch(profile.getSkills(), internship.getRequiredSkills());

        Resume resume = null;
        if (request.getResumeId() != null) {
            resume = resumeRepository.findById(request.getResumeId()).orElse(null);
        }
        if (resume == null) {
            resume = resumeRepository.findTopByUserIdOrderByUploadedAtDesc(student.getId()).orElse(null);
        }

        Application application = new Application();
        application.setInternship(internship);
        application.setStudent(student);
        application.setResume(resume);
        application.setMatchPercentage(matchResult.getMatchPercentage());
        application.setStatus(ApplicationStatus.PENDING);
        application.setCoverLetter(request.getCoverLetter());
        application = applicationRepository.save(application);

        // Update student readiness score
        profile.setCareerReadinessScore(calculateReadinessScore(student, profile));
        profileRepository.save(profile);

        // Create student in-app notification
        notificationService.notify(
                student,
                "Application Submitted: " + internship.getTitle(),
                "Your application to " + internship.getCompany().getCompanyName() + " was submitted with a " + matchResult.getMatchPercentage() + "% skill match.",
                "INFO"
        );

        // Create company in-app notification
        User companyUser = internship.getCompany().getUser();
        notificationService.notify(
                companyUser,
                "New Applicant: " + student.getName(),
                student.getName() + " applied for " + internship.getTitle() + " (" + matchResult.getMatchPercentage() + "% match).",
                "INFO"
        );

        // Dispatch Email confirmation via JavaMailSender
        emailService.sendApplicationConfirmation(
                student.getEmail(),
                student.getName(),
                internship.getTitle(),
                internship.getCompany().getCompanyName()
        );

        return toApplicationDto(application, profile, matchResult);
    }

    @Transactional
    public boolean toggleBookmark(String email, Long internshipId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));

        Optional<SavedInternship> existing = savedInternshipRepository.findByUserIdAndInternshipId(user.getId(), internship.getId());
        if (existing.isPresent()) {
            savedInternshipRepository.delete(existing.get());
            return false;
        } else {
            savedInternshipRepository.save(new SavedInternship(user, internship));
            return true;
        }
    }

    public List<InternshipResponseDto> getSavedInternships(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        List<SavedInternship> savedList = savedInternshipRepository.findByUserIdOrderBySavedAtDesc(user.getId());
        List<InternshipResponseDto> dtos = new ArrayList<>();

        for (SavedInternship si : savedList) {
            Internship in = si.getInternship();
            MatchResultDto match = matchingService.calculateMatch(profile.getSkills(), in.getRequiredSkills());
            boolean applied = applicationRepository.existsByInternshipIdAndStudentId(in.getId(), user.getId());

            InternshipResponseDto dto = toInternshipDto(in, match, true, applied);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<ApplicationResponseDto> getMyApplications(String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(student.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(student)));

        List<Application> apps = applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId());
        return apps.stream().map(a -> {
            MatchResultDto match = matchingService.calculateMatch(profile.getSkills(), a.getInternship().getRequiredSkills());
            return toApplicationDto(a, profile, match);
        }).collect(Collectors.toList());
    }

    public List<InternshipResponseDto> getRecommendations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(new StudentProfile(user)));

        List<Internship> activeList = internshipRepository.findByIsApprovedTrueAndIsActiveTrueOrderByCreatedAtDesc();

        List<InternshipResponseDto> results = new ArrayList<>();
        for (Internship in : activeList) {
            MatchResultDto match = matchingService.calculateMatch(profile.getSkills(), in.getRequiredSkills());
            boolean saved = savedInternshipRepository.existsByUserIdAndInternshipId(user.getId(), in.getId());
            boolean applied = applicationRepository.existsByInternshipIdAndStudentId(in.getId(), user.getId());

            results.add(toInternshipDto(in, match, saved, applied));
        }

        // Sort by Match % descending
        results.sort((a, b) -> Integer.compare(
                b.getMatchResult() != null ? b.getMatchResult().getMatchPercentage() : 0,
                a.getMatchResult() != null ? a.getMatchResult().getMatchPercentage() : 0
        ));

        return results;
    }

    private int calculateReadinessScore(User user, StudentProfile profile) {
        int score = 10; // Base score
        if (profile.getCollege() != null && !profile.getCollege().isBlank()) score += 5;
        if (profile.getDegree() != null && !profile.getDegree().isBlank()) score += 5;
        if (profile.getDepartment() != null && !profile.getDepartment().isBlank()) score += 5;

        // Resume uploaded check
        if (resumeRepository.findTopByUserIdOrderByUploadedAtDesc(user.getId()).isPresent()) {
            score += 25;
        }

        // Skills (up to 25 pts)
        if (profile.getSkills() != null) {
            score += Math.min(25, profile.getSkills().size() * 5);
        }

        // Applications submitted (up to 15 pts)
        long apps = applicationRepository.findByStudentIdOrderByAppliedAtDesc(user.getId()).size();
        score += (int) Math.min(15, apps * 5);

        // Bookmarks (up to 10 pts)
        long bookmarks = savedInternshipRepository.findByUserIdOrderBySavedAtDesc(user.getId()).size();
        score += (int) Math.min(10, bookmarks * 5);

        return Math.min(100, Math.max(0, score));
    }

    private StudentProfileDto toDto(User user, StudentProfile profile) {
        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCollege(profile.getCollege());
        dto.setDegree(profile.getDegree());
        dto.setDepartment(profile.getDepartment());
        dto.setGraduationYear(profile.getGraduationYear());
        dto.setBio(profile.getBio());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setCareerReadinessScore(profile.getCareerReadinessScore());
        dto.setSkills(profile.getSkills());

        resumeRepository.findTopByUserIdOrderByUploadedAtDesc(user.getId()).ifPresent(r -> {
            dto.setResumeId(r.getId());
            dto.setResumeFileName(r.getFileName());
            dto.setResumeUrl("/uploads/resumes/" + new java.io.File(r.getStoredPath()).getName());
        });

        dto.setAppliedCount(applicationRepository.findByStudentIdOrderByAppliedAtDesc(user.getId()).size());
        dto.setSavedCount(savedInternshipRepository.findByUserIdOrderBySavedAtDesc(user.getId()).size());

        return dto;
    }

    private InternshipResponseDto toInternshipDto(Internship in, MatchResultDto match, boolean isSaved, boolean hasApplied) {
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

        dto.setMatchResult(match);
        dto.setSaved(isSaved);
        dto.setHasApplied(hasApplied);

        return dto;
    }

    private ApplicationResponseDto toApplicationDto(Application a, StudentProfile profile, MatchResultDto match) {
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

        if (profile != null) {
            dto.setStudentCollege(profile.getCollege());
            dto.setStudentDegree(profile.getDegree());
            dto.setStudentDepartment(profile.getDepartment());
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
