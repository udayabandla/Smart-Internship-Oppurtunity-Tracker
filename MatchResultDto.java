package com.mindsmart.dto;

import java.util.ArrayList;
import java.util.List;

public class MatchResultDto {
    private int matchPercentage;
    private List<String> matchingSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> suggestedSkillsToLearn = new ArrayList<>();
    private int studentSkillCount;
    private int requiredSkillCount;
    private String matchRating;
    private String matchColor;

    public MatchResultDto() {}

    public MatchResultDto(int matchPercentage, List<String> matchingSkills, List<String> missingSkills,
                          List<String> suggestedSkillsToLearn, int studentSkillCount, int requiredSkillCount) {
        this.matchPercentage = matchPercentage;
        this.matchingSkills = matchingSkills != null ? matchingSkills : new ArrayList<>();
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
        this.suggestedSkillsToLearn = suggestedSkillsToLearn != null ? suggestedSkillsToLearn : new ArrayList<>();
        this.studentSkillCount = studentSkillCount;
        this.requiredSkillCount = requiredSkillCount;

        if (matchPercentage >= 75) {
            this.matchRating = "Excellent Match";
            this.matchColor = "#10b981"; // emerald
        } else if (matchPercentage >= 50) {
            this.matchRating = "Good Match";
            this.matchColor = "#6366f1"; // indigo
        } else if (matchPercentage >= 25) {
            this.matchRating = "Moderate Match";
            this.matchColor = "#f59e0b"; // amber
        } else {
            this.matchRating = "Learning Opportunity";
            this.matchColor = "#ec4899"; // pink
        }
    }

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getSuggestedSkillsToLearn() { return suggestedSkillsToLearn; }
    public void setSuggestedSkillsToLearn(List<String> suggestedSkillsToLearn) { this.suggestedSkillsToLearn = suggestedSkillsToLearn; }

    public int getStudentSkillCount() { return studentSkillCount; }
    public void setStudentSkillCount(int studentSkillCount) { this.studentSkillCount = studentSkillCount; }

    public int getRequiredSkillCount() { return requiredSkillCount; }
    public void setRequiredSkillCount(int requiredSkillCount) { this.requiredSkillCount = requiredSkillCount; }

    public String getMatchRating() { return matchRating; }
    public void setMatchRating(String matchRating) { this.matchRating = matchRating; }

    public String getMatchColor() { return matchColor; }
    public void setMatchColor(String matchColor) { this.matchColor = matchColor; }
}
