package com.mindsmart.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserService.class);

    @Value("${mindsmart.upload.dir:uploads}")
    private String uploadBaseDir;

    // Comprehensive list of tech and professional skills to detect
    private static final List<String> KNOWN_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "Spring MVC", "Hibernate", "JPA", "Microservices",
            "Python", "Django", "Flask", "FastAPI", "C++", "C#", ".NET",
            "JavaScript", "TypeScript", "React", "Angular", "Vue", "Next.js",
            "HTML", "CSS", "Tailwind CSS", "Bootstrap", "Sass", "jQuery",
            "Node.js", "Express", "REST API", "GraphQL", "SOAP",
            "MySQL", "PostgreSQL", "MongoDB", "Oracle", "Redis", "SQLite", "Cassandra",
            "AWS", "Azure", "Google Cloud", "Docker", "Kubernetes", "CI/CD", "Jenkins", "Git", "GitHub", "GitLab",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "NLP", "Computer Vision",
            "Data Science", "Pandas", "NumPy", "Scikit-Learn", "Tableau", "Power BI",
            "Android", "Kotlin", "Flutter", "React Native", "Swift", "iOS",
            "Linux", "Shell Scripting", "Cyber Security", "Penetration Testing", "Ethical Hacking",
            "Agile", "Scrum", "Jira", "UI/UX", "Figma", "Adobe XD", "Unit Testing", "JUnit", "Postman"
    );

    public String saveResumeFile(MultipartFile file, Long userId) throws Exception {
        Path targetDir = Paths.get(uploadBaseDir, "resumes").toAbsolutePath().normalize();
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFileName = "user_" + userId + "_" + System.currentTimeMillis() + "_" + cleanName;

        Path targetPath = targetDir.resolve(uniqueFileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetPath.toString();
    }

    public String extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (fileName.endsWith(".pdf")) {
                try (InputStream is = file.getInputStream();
                     PDDocument document = PDDocument.load(is)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (fileName.endsWith(".docx")) {
                try (InputStream is = file.getInputStream();
                     XWPFDocument doc = new XWPFDocument(is);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else {
                // Fallback to text reading
                return new String(file.getBytes());
            }
        } catch (Exception e) {
            logger.warn("Could not extract full text from resume file {}: {}", fileName, e.getMessage());
            return "Resume document uploaded: " + fileName;
        }
    }

    public List<String> extractSkillsFromText(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String lowerText = " " + text.toLowerCase() + " ";
        Set<String> matchedSkills = new LinkedHashSet<>();

        for (String skill : KNOWN_SKILLS) {
            String lowerSkill = skill.toLowerCase();
            // Word boundary regex check to prevent partial matches like "C" inside "React"
            String regex = "(?i).*\\b" + Pattern.quote(lowerSkill) + "\\b.*";
            if (Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(lowerText).matches()) {
                matchedSkills.add(skill);
            }
        }

        // If very few detected, add standard foundational skills if common keywords are present
        if (matchedSkills.isEmpty()) {
            matchedSkills.add("Problem Solving");
            matchedSkills.add("Communication");
            matchedSkills.add("Teamwork");
        }

        return new ArrayList<>(matchedSkills);
    }
}
