package com.mindsmart.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationRequest {
    @NotNull(message = "Internship ID is required")
    private Long internshipId;

    private Long resumeId;

    private String coverLetter;

    public ApplicationRequest() {}

    public Long getInternshipId() { return internshipId; }
    public void setInternshipId(Long internshipId) { this.internshipId = internshipId; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
