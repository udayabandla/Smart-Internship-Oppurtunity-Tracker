package com.mindsmart.service;

import com.mindsmart.dto.MatchResultDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    // Aliases mapping for common variations
    private static final Map<String, String> SKILL_ALIASES = new HashMap<>();

    static {
        SKILL_ALIASES.put("js", "javascript");
        SKILL_ALIASES.put("ts", "typescript");
        SKILL_ALIASES.put("react.js", "react");
        SKILL_ALIASES.put("reactjs", "react");
        SKILL_ALIASES.put("node", "node.js");
        SKILL_ALIASES.put("nodejs", "node.js");
        SKILL_ALIASES.put("spring", "spring boot");
        SKILL_ALIASES.put("springboot", "spring boot");
        SKILL_ALIASES.put("py", "python");
        SKILL_ALIASES.put("ml", "machine learning");
        SKILL_ALIASES.put("ai", "artificial intelligence");
        SKILL_ALIASES.put("postgres", "postgresql");
        SKILL_ALIASES.put("k8s", "kubernetes");
        SKILL_ALIASES.put("docker", "docker");
        SKILL_ALIASES.put("aws", "aws");
        SKILL_ALIASES.put("amazon web services", "aws");
        SKILL_ALIASES.put("gcp", "google cloud");
        SKILL_ALIASES.put("google cloud platform", "google cloud");
        SKILL_ALIASES.put("rest", "rest api");
        SKILL_ALIASES.put("restful api", "rest api");
        SKILL_ALIASES.put("html5", "html");
        SKILL_ALIASES.put("css3", "css");
    }

    public String normalizeSkill(String skill) {
        if (skill == null) return "";
        String cleaned = skill.trim().toLowerCase();
        return SKILL_ALIASES.getOrDefault(cleaned, cleaned);
    }

    /**
     * Rule-driven matching calculation:
     * Match % = (Matching Skills / Total Required Skills) * 100
     * Strictly deterministic.
     */
    public MatchResultDto calculateMatch(Set<String> studentSkills, Set<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return new MatchResultDto(100, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    studentSkills != null ? studentSkills.size() : 0, 0);
        }

        Set<String> normalizedStudentSkills = new HashSet<>();
        if (studentSkills != null) {
            for (String s : studentSkills) {
                if (s != null && !s.isBlank()) {
                    normalizedStudentSkills.add(normalizeSkill(s));
                }
            }
        }

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String reqSkill : requiredSkills) {
            if (reqSkill == null || reqSkill.isBlank()) continue;
            String normalizedReq = normalizeSkill(reqSkill);
            if (normalizedStudentSkills.contains(normalizedReq)) {
                matchingSkills.add(reqSkill);
            } else {
                missingSkills.add(reqSkill);
            }
        }

        int totalRequired = requiredSkills.size();
        int matchedCount = matchingSkills.size();

        int matchPercentage = 0;
        if (totalRequired > 0) {
            matchPercentage = (int) Math.round(((double) matchedCount / totalRequired) * 100.0);
            if (matchPercentage > 100) matchPercentage = 100;
        }

        // Suggested skills are missing skills prioritized, plus complementary industry skills
        List<String> suggestedSkills = new ArrayList<>(missingSkills);
        if (suggestedSkills.size() > 5) {
            suggestedSkills = suggestedSkills.subList(0, 5);
        }

        return new MatchResultDto(
                matchPercentage,
                matchingSkills,
                missingSkills,
                suggestedSkills,
                studentSkills != null ? studentSkills.size() : 0,
                totalRequired
        );
    }
}
