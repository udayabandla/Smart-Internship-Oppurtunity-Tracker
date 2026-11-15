package com.mindsmart.controller;

import com.mindsmart.dto.*;
import com.mindsmart.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<StudentProfileDto>> getProfile(@AuthenticationPrincipal UserDetails user) {
        try {
            StudentProfileDto profile = studentService.getProfile(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Profile retrieved", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<StudentProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody StudentProfileDto dto) {
        try {
            StudentProfileDto updated = studentService.updateProfile(user.getUsername(), dto);
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeParseResultDto>> uploadResume(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Uploaded resume file is empty"));
            }
            ResumeParseResultDto result = studentService.uploadResume(user.getUsername(), file);
            return ResponseEntity.ok(ApiResponse.ok("Resume uploaded & parsed successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to parse resume: " + e.getMessage()));
        }
    }

    @PutMapping("/skills")
    public ResponseEntity<ApiResponse<Set<String>>> updateSkills(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Set<String> skills) {
        try {
            Set<String> updated = studentService.updateSkills(user.getUsername(), skills);
            return ResponseEntity.ok(ApiResponse.ok("Skills updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/readiness")
    public ResponseEntity<ApiResponse<CareerReadinessDto>> getReadiness(@AuthenticationPrincipal UserDetails user) {
        try {
            CareerReadinessDto readiness = studentService.getCareerReadiness(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Career readiness score calculated", readiness));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/skill-gap")
    public ResponseEntity<ApiResponse<SkillGapAnalysisDto>> getSkillGap(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(value = "targetRole", defaultValue = "Full Stack Java Developer") String targetRole) {
        try {
            SkillGapAnalysisDto analysis = studentService.analyzeSkillGap(user.getUsername(), targetRole);
            return ResponseEntity.ok(ApiResponse.ok("Skill-gap analysis completed", analysis));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> apply(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponseDto app = studentService.applyForInternship(user.getUsername(), request);
            return ResponseEntity.ok(ApiResponse.ok("Application submitted successfully!", app));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/bookmark/{internshipId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long internshipId) {
        try {
            boolean saved = studentService.toggleBookmark(user.getUsername(), internshipId);
            String msg = saved ? "Internship bookmarked!" : "Bookmark removed";
            return ResponseEntity.ok(ApiResponse.ok(msg, saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/saved-internships")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> getSaved(@AuthenticationPrincipal UserDetails user) {
        try {
            List<InternshipResponseDto> list = studentService.getSavedInternships(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Saved internships retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getApplications(@AuthenticationPrincipal UserDetails user) {
        try {
            List<ApplicationResponseDto> list = studentService.getMyApplications(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("My applications retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> getRecommendations(@AuthenticationPrincipal UserDetails user) {
        try {
            List<InternshipResponseDto> list = studentService.getRecommendations(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Personalized recommendations retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
