package com.mindsmart.dto;

public class CompanyProfileDto {
    private Long id;
    private Long userId;
    private String companyName;
    private String recruiterName;
    private String email;
    private String phone;
    private String website;
    private String industry;
    private String location;
    private String description;
    private String logoUrl;
    private boolean isVerified;
    private int activeInternshipsCount;
    private int totalApplicationsCount;

    public CompanyProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public int getActiveInternshipsCount() { return activeInternshipsCount; }
    public void setActiveInternshipsCount(int activeInternshipsCount) { this.activeInternshipsCount = activeInternshipsCount; }

    public int getTotalApplicationsCount() { return totalApplicationsCount; }
    public void setTotalApplicationsCount(int totalApplicationsCount) { this.totalApplicationsCount = totalApplicationsCount; }
}
