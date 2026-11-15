package com.mindsmart.dto;

import java.util.ArrayList;
import java.util.List;

public class SkillGapAnalysisDto {
    private String targetRole;
    private int matchPercentage;
    private List<String> currentSkills = new ArrayList<>();
    private List<String> targetRequiredSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<LearningPathwayDto> learningPathways = new ArrayList<>();

    public SkillGapAnalysisDto() {}

    public static class LearningPathwayDto {
        private String skill;
        private String recommendation;
        private String resourceType; // "Free Course", "Documentation", "Hands-on Project"
        private String link;

        public LearningPathwayDto() {}

        public LearningPathwayDto(String skill, String recommendation, String resourceType, String link) {
            this.skill = skill;
            this.recommendation = recommendation;
            this.resourceType = resourceType;
            this.link = link;
        }

        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public List<String> getCurrentSkills() { return currentSkills; }
    public void setCurrentSkills(List<String> currentSkills) { this.currentSkills = currentSkills; }

    public List<String> getTargetRequiredSkills() { return targetRequiredSkills; }
    public void setTargetRequiredSkills(List<String> targetRequiredSkills) { this.targetRequiredSkills = targetRequiredSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<LearningPathwayDto> getLearningPathways() { return learningPathways; }
    public void setLearningPathways(List<LearningPathwayDto> learningPathways) { this.learningPathways = learningPathways; }
}
