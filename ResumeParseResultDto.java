package com.mindsmart.dto;

import java.util.List;

public class ResumeParseResultDto {
    private Long resumeId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private List<String> detectedSkills;
    private String extractedTextPreview;
    private int detectedSkillCount;

    public ResumeParseResultDto() {}

    public ResumeParseResultDto(Long resumeId, String fileName, String fileType, Long fileSize,
                                String downloadUrl, List<String> detectedSkills, String extractedTextPreview) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.detectedSkills = detectedSkills;
        this.extractedTextPreview = extractedTextPreview;
        this.detectedSkillCount = (detectedSkills != null) ? detectedSkills.size() : 0;
    }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public List<String> getDetectedSkills() { return detectedSkills; }
    public void setDetectedSkills(List<String> detectedSkills) { this.detectedSkills = detectedSkills; }

    public String getExtractedTextPreview() { return extractedTextPreview; }
    public void setExtractedTextPreview(String extractedTextPreview) { this.extractedTextPreview = extractedTextPreview; }

    public int getDetectedSkillCount() { return detectedSkillCount; }
    public void setDetectedSkillCount(int detectedSkillCount) { this.detectedSkillCount = detectedSkillCount; }
}
