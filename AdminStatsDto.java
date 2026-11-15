package com.mindsmart.dto;

import java.util.List;

public class AdminStatsDto {
    private long totalStudents;
    private long totalCompanies;
    private long totalInternships;
    private long totalApplications;
    private long verifiedCompanies;
    private long pendingApprovals;
    private long reportedCount;
    private List<ApplicationResponseDto> recentApplications;

    public AdminStatsDto() {}

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getTotalInternships() { return totalInternships; }
    public void setTotalInternships(long totalInternships) { this.totalInternships = totalInternships; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getVerifiedCompanies() { return verifiedCompanies; }
    public void setVerifiedCompanies(long verifiedCompanies) { this.verifiedCompanies = verifiedCompanies; }

    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public long getReportedCount() { return reportedCount; }
    public void setReportedCount(long reportedCount) { this.reportedCount = reportedCount; }

    public List<ApplicationResponseDto> getRecentApplications() { return recentApplications; }
    public void setRecentApplications(List<ApplicationResponseDto> recentApplications) { this.recentApplications = recentApplications; }
}
