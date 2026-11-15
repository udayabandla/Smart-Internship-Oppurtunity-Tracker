package com.mindsmart.controller;

import com.mindsmart.dto.*;
import com.mindsmart.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CompanyProfileDto>> getProfile(@AuthenticationPrincipal UserDetails user) {
        try {
            CompanyProfileDto profile = companyService.getProfile(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Company profile retrieved", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CompanyProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CompanyProfileDto dto) {
        try {
            CompanyProfileDto updated = companyService.updateProfile(user.getUsername(), dto);
            return ResponseEntity.ok(ApiResponse.ok("Company profile updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/logo/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadLogo(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Logo file is empty"));
            }
            String logoUrl = companyService.uploadLogo(user.getUsername(), file);
            return ResponseEntity.ok(ApiResponse.ok("Company logo uploaded successfully", logoUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Logo upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/internships")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> getMyInternships(@AuthenticationPrincipal UserDetails user) {
        try {
            List<InternshipResponseDto> list = companyService.getMyInternships(user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Company internships retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/internships")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> postInternship(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody InternshipRequest request) {
        try {
            InternshipResponseDto created = companyService.postInternship(user.getUsername(), request);
            return ResponseEntity.ok(ApiResponse.ok("Internship opportunity posted successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/internships/{id}")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> updateInternship(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody InternshipRequest request) {
        try {
            InternshipResponseDto updated = companyService.updateInternship(user.getUsername(), id, request);
            return ResponseEntity.ok(ApiResponse.ok("Internship updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/internships/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInternship(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        try {
            companyService.deleteInternship(user.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.ok("Internship deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getApplications(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(value = "internshipId", required = false) Long internshipId) {
        try {
            List<ApplicationResponseDto> list = companyService.getCompanyApplications(user.getUsername(), internshipId);
            return ResponseEntity.ok(ApiResponse.ok("Applications retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateStatus(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        try {
            ApplicationResponseDto updated = companyService.updateApplicationStatus(user.getUsername(), id, request);
            return ResponseEntity.ok(ApiResponse.ok("Candidate status updated and notification dispatched", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
