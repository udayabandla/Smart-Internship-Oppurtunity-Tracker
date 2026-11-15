package com.mindsmart.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CareerReadinessDto {
    private int overallScore;
    private String ratingLevel; // Novice, Developing, Job Ready, Top Tier
    private String levelBadgeColor;
    private Map<String, Integer> breakdown; // profileCompleteness, resumeStatus, skillsVerified, applicationsActivity, savedActivity
    private List<String> actionableRecommendations = new ArrayList<>();
    private List<String> topInDemandSkills = new ArrayList<>();

    public CareerReadinessDto() {}

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getRatingLevel() { return ratingLevel; }
    public void setRatingLevel(String ratingLevel) { this.ratingLevel = ratingLevel; }

    public String getLevelBadgeColor() { return levelBadgeColor; }
    public void setLevelBadgeColor(String levelBadgeColor) { this.levelBadgeColor = levelBadgeColor; }

    public Map<String, Integer> getBreakdown() { return breakdown; }
    public void setBreakdown(Map<String, Integer> breakdown) { this.breakdown = breakdown; }

    public List<String> getActionableRecommendations() { return actionableRecommendations; }
    public void setActionableRecommendations(List<String> actionableRecommendations) { this.actionableRecommendations = actionableRecommendations; }

    public List<String> getTopInDemandSkills() { return topInDemandSkills; }
    public void setTopInDemandSkills(List<String> topInDemandSkills) { this.topInDemandSkills = topInDemandSkills; }
}
