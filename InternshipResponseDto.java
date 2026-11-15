package com.mindsmart.dto;

import com.mindsmart.entity.WorkMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class InternshipResponseDto {
    private Long id;
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private String companyLocation;
    private boolean isCompanyVerified;
    private String title;
    private String category;
    private WorkMode workMode;
    private String location;
    private String duration;
    private Integer stipend;
    private Integer openings;
    private String description;
    private String eligibility;
    private LocalDate deadline;
    private boolean isActive;
    private boolean isApproved;
    private boolean isReported;
    private String reportReason;
    private Integer viewsCount;
    private Set<String> requiredSkills;
    private LocalDateTime createdAt;

    // Student personalized context
    private MatchResultDto matchResult;
    private boolean isSaved;
    private boolean hasApplied;
    private String applicationStatus;

    public InternshipResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getCompanyLocation() { return companyLocation; }
    public void setCompanyLocation(String companyLocation) { this.companyLocation = companyLocation; }

    public boolean isCompanyVerified() { return isCompanyVerified; }
    public void setCompanyVerified(boolean companyVerified) { isCompanyVerified = companyVerified; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public Integer getStipend() { return stipend; }
    public void setStipend(Integer stipend) { this.stipend = stipend; }

    public Integer getOpenings() { return openings; }
    public void setOpenings(Integer openings) { this.openings = openings; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public boolean isReported() { return isReported; }
    public void setReported(boolean reported) { isReported = reported; }

    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }

    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

    public Set<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public MatchResultDto getMatchResult() { return matchResult; }
    public void setMatchResult(MatchResultDto matchResult) { this.matchResult = matchResult; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public boolean isHasApplied() { return hasApplied; }
    public void setHasApplied(boolean hasApplied) { this.hasApplied = hasApplied; }

    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }
}
