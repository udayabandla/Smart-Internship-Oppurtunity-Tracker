package com.mindsmart.controller;

import com.mindsmart.dto.AdminStatsDto;
import com.mindsmart.dto.ApiResponse;
import com.mindsmart.dto.CompanyProfileDto;
import com.mindsmart.entity.Internship;
import com.mindsmart.entity.User;
import com.mindsmart.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        try {
            AdminStatsDto stats = adminService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.ok("Admin stats retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        try {
            adminService.toggleUserStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("User status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyProfileDto>>> getAllCompanies() {
        try {
            List<CompanyProfileDto> list = adminService.getAllCompanies();
            return ResponseEntity.ok(ApiResponse.ok("Companies retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/companies/{id}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCompany(@PathVariable Long id, @RequestParam boolean verify) {
        try {
            adminService.verifyCompany(id, verify);
            String msg = verify ? "Company verified successfully!" : "Company verification revoked";
            return ResponseEntity.ok(ApiResponse.ok(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/internships/pending")
    public ResponseEntity<ApiResponse<List<Internship>>> getPendingInternships() {
        try {
            List<Internship> list = adminService.getPendingApprovalInternships();
            return ResponseEntity.ok(ApiResponse.ok("Pending approval internships retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/internships/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveInternship(@PathVariable Long id, @RequestParam boolean approve) {
        try {
            adminService.approveInternship(id, approve);
            String msg = approve ? "Internship approved & live!" : "Internship rejected";
            return ResponseEntity.ok(ApiResponse.ok(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/internships/reported")
    public ResponseEntity<ApiResponse<List<Internship>>> getReportedInternships() {
        try {
            List<Internship> list = adminService.getReportedInternships();
            return ResponseEntity.ok(ApiResponse.ok("Reported internships retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/internships/{id}/resolve-report")
    public ResponseEntity<ApiResponse<Void>> resolveReport(@PathVariable Long id, @RequestParam boolean dismiss) {
        try {
            adminService.resolveReport(id, dismiss);
            String msg = dismiss ? "Report dismissed" : "Reported listing removed";
            return ResponseEntity.ok(ApiResponse.ok(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
