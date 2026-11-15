package com.mindsmart.controller;

import com.mindsmart.dto.ApiResponse;
import com.mindsmart.dto.InternshipResponseDto;
import com.mindsmart.service.InternshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
public class InternshipController {

    private final InternshipService internshipService;

    public InternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> getAllInternships(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minStipend,
            @AuthenticationPrincipal UserDetails user) {
        String userEmail = user != null ? user.getUsername() : null;
        List<InternshipResponseDto> list = internshipService.searchInternships(keyword, category, workMode, location, minStipend, userEmail);
        return ResponseEntity.ok(ApiResponse.ok("Internships retrieved", list));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> searchInternships(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minStipend,
            @AuthenticationPrincipal UserDetails user) {
        String userEmail = user != null ? user.getUsername() : null;
        List<InternshipResponseDto> list = internshipService.searchInternships(keyword, category, workMode, location, minStipend, userEmail);
        return ResponseEntity.ok(ApiResponse.ok("Search results retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> getInternshipDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        try {
            String userEmail = user != null ? user.getUsername() : null;
            InternshipResponseDto dto = internshipService.getInternshipDetails(id, userEmail);
            return ResponseEntity.ok(ApiResponse.ok("Internship details retrieved", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<ApiResponse<Void>> reportInternship(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            internshipService.reportInternship(id, reason);
            return ResponseEntity.ok(ApiResponse.ok("Thank you. Listing has been flagged for admin review."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> compareInternships(
            @RequestBody List<Long> ids,
            @AuthenticationPrincipal UserDetails user) {
        try {
            String userEmail = user != null ? user.getUsername() : null;
            List<InternshipResponseDto> comparison = internshipService.compareInternships(ids, userEmail);
            return ResponseEntity.ok(ApiResponse.ok("Comparison data loaded", comparison));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
