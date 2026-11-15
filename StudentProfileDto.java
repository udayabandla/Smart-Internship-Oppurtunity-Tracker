package com.mindsmart.dto;

import java.util.Set;

public class StudentProfileDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String college;
    private String degree;
    private String department;
    private Integer graduationYear;
    private String bio;
    private String avatarUrl;
    private Integer careerReadinessScore;
    private Set<String> skills;
    private Long resumeId;
    private String resumeFileName;
    private String resumeUrl;
    private int appliedCount;
    private int savedCount;

    public StudentProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getCareerReadinessScore() { return careerReadinessScore; }
    public void setCareerReadinessScore(Integer careerReadinessScore) { this.careerReadinessScore = careerReadinessScore; }

    public Set<String> getSkills() { return skills; }
    public void setSkills(Set<String> skills) { this.skills = skills; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public int getAppliedCount() { return appliedCount; }
    public void setAppliedCount(int appliedCount) { this.appliedCount = appliedCount; }

    public int getSavedCount() { return savedCount; }
    public void setSavedCount(int savedCount) { this.savedCount = savedCount; }
}
