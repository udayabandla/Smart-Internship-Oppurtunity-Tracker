package com.mindsmart.util;

import com.mindsmart.entity.*;
import com.mindsmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final SkillRepository skillRepository;
    private final InternshipRepository internshipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           CompanyProfileRepository companyProfileRepository,
                           SkillRepository skillRepository,
                           InternshipRepository internshipRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.skillRepository = skillRepository;
        this.internshipRepository = internshipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedSkills();
            seedUsersAndCompanies();
            seedInternships();
            logger.info("MindSmart demo seed data initialized successfully!");
        } catch (Exception e) {
            logger.error("Error initializing demo seed data: {}", e.getMessage(), e);
        }
    }

    private void seedSkills() {
        if (skillRepository.count() > 0) return;

        List<String[]> skillsList = Arrays.asList(
                new String[]{"Java", "Backend"},
                new String[]{"Spring Boot", "Backend"},
                new String[]{"Python", "Languages"},
                new String[]{"JavaScript", "Frontend"},
                new String[]{"TypeScript", "Frontend"},
                new String[]{"React", "Frontend"},
                new String[]{"Angular", "Frontend"},
                new String[]{"HTML", "Frontend"},
                new String[]{"CSS", "Frontend"},
                new String[]{"Tailwind CSS", "Frontend"},
                new String[]{"MySQL", "Database"},
                new String[]{"PostgreSQL", "Database"},
                new String[]{"MongoDB", "Database"},
                new String[]{"Redis", "Database"},
                new String[]{"AWS", "Cloud & DevOps"},
                new String[]{"Google Cloud", "Cloud & DevOps"},
                new String[]{"Docker", "Cloud & DevOps"},
                new String[]{"Kubernetes", "Cloud & DevOps"},
                new String[]{"CI/CD", "Cloud & DevOps"},
                new String[]{"Linux", "Systems"},
                new String[]{"Git", "Tools"},
                new String[]{"REST API", "Backend"},
                new String[]{"Microservices", "Backend"},
                new String[]{"Machine Learning", "AI/ML"},
                new String[]{"Deep Learning", "AI/ML"},
                new String[]{"TensorFlow", "AI/ML"},
                new String[]{"Data Science", "Data"},
                new String[]{"Pandas", "Data"},
                new String[]{"NumPy", "Data"},
                new String[]{"Tableau", "Data"},
                new String[]{"Cyber Security", "Security"},
                new String[]{"Flutter", "Mobile"},
                new String[]{"Android", "Mobile"},
                new String[]{"Kotlin", "Mobile"},
                new String[]{"Figma", "Design"}
        );

        for (String[] pair : skillsList) {
            skillRepository.save(new Skill(pair[0], pair[1]));
        }
    }

    private void seedUsersAndCompanies() {
        // 1. Admin Account
        if (!userRepository.existsByEmail("admin@mindsmart.com")) {
            User admin = new User("Platform Administrator", "admin@mindsmart.com", "+91 9876543210",
                    passwordEncoder.encode("Admin@123"), Role.ROLE_ADMIN);
            userRepository.save(admin);
        }

        // 2. Demo Student Account 1
        if (!userRepository.existsByEmail("student@mindsmart.com")) {
            User student = new User("Rahul Sharma", "student@mindsmart.com", "+91 9123456780",
                    passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT);
            student = userRepository.save(student);

            StudentProfile profile = new StudentProfile(student);
            profile.setCollege("National Institute of Technology");
            profile.setDegree("B.Tech");
            profile.setDepartment("Computer Science and Engineering");
            profile.setGraduationYear(2025);
            profile.setBio("Passionate Full Stack Java enthusiast eager to build enterprise-scale web applications.");
            profile.setCareerReadinessScore(85);
            profile.setSkills(new HashSet<>(Arrays.asList("Java", "Spring Boot", "MySQL", "REST API", "Git", "HTML", "CSS")));
            studentProfileRepository.save(profile);
        }

        // 3. Demo Student Account 2
        if (!userRepository.existsByEmail("arun.kumar@gmail.com")) {
            User student2 = new User("Arun Kumar", "arun.kumar@gmail.com", "+91 9988776655",
                    passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT);
            student2 = userRepository.save(student2);

            StudentProfile profile2 = new StudentProfile(student2);
            profile2.setCollege("Vellore Institute of Technology");
            profile2.setDegree("B.Tech");
            profile2.setDepartment("Data Science & AI");
            profile2.setGraduationYear(2025);
            profile2.setBio("Data enthusiast working on Machine Learning and predictive analytics.");
            profile2.setCareerReadinessScore(75);
            profile2.setSkills(new HashSet<>(Arrays.asList("Python", "Machine Learning", "Data Science", "Pandas", "NumPy", "SQL", "Git")));
            studentProfileRepository.save(profile2);
        }

        // 4. Demo Company 1: TechNova Innovations
        createCompanyIfMissing("hr@technova.com", "Ananya Sen", "Company@123", "TechNova Innovations",
                "https://technova.io", "Software & AI Solutions", "Bangalore, India",
                "TechNova is a premier next-gen software engineering house creating cloud-native enterprise products.",
                true);

        // 5. Demo Company 2: CloudScale Systems
        createCompanyIfMissing("careers@cloudscale.io", "Vikram Malhotra", "Company@123", "CloudScale Systems",
                "https://cloudscale.io", "Cloud Infrastructure & DevOps", "Hyderabad, India",
                "Leading cloud engineering consultancy accelerating migration to AWS and Kubernetes.",
                true);

        // 6. Demo Company 3: CyberPulse Security
        createCompanyIfMissing("recruitment@cyberpulse.com", "Deepak Roy", "Company@123", "CyberPulse Security",
                "https://cyberpulse.security", "Cyber Security & Defense", "Mumbai, India",
                "Pioneering threat intelligence and vulnerability assessment for fintech and banks.",
                true);

        // 7. Demo Company 4: DataVibe Analytics
        createCompanyIfMissing("talent@datavibe.ai", "Pooja Hegde", "Company@123", "DataVibe Analytics",
                "https://datavibe.ai", "Data Science & Business Intelligence", "Pune, India",
                "Transforming raw business data into strategic forecasting and intelligence.",
                true);

        // 8. Demo Company 5: NexaByte Labs
        createCompanyIfMissing("jobs@nexabyte.dev", "Rohan Mehta", "Company@123", "NexaByte Labs",
                "https://nexabyte.dev", "Web & Mobile App Development", "Delhi NCR, India",
                "High-velocity product studio crafting sleek web and cross-platform mobile apps.",
                false);
    }

    private void createCompanyIfMissing(String email, String recruiterName, String pass, String companyName,
                                        String website, String industry, String location, String description,
                                        boolean isVerified) {
        if (!userRepository.existsByEmail(email)) {
            User compUser = new User(recruiterName, email, "+91 9800112233", passwordEncoder.encode(pass), Role.ROLE_COMPANY);
            compUser = userRepository.save(compUser);

            CompanyProfile compProfile = new CompanyProfile(compUser, companyName);
            compProfile.setRecruiterName(recruiterName);
            compProfile.setWebsite(website);
            compProfile.setIndustry(industry);
            compProfile.setLocation(location);
            compProfile.setDescription(description);
            compProfile.setVerified(isVerified);
            companyProfileRepository.save(compProfile);
        }
    }

    private void seedInternships() {
        if (internshipRepository.count() > 0) return;

        CompanyProfile technova = companyProfileRepository.findByUserEmail("hr@technova.com").orElse(null);
        CompanyProfile cloudscale = companyProfileRepository.findByUserEmail("careers@cloudscale.io").orElse(null);
        CompanyProfile cyberpulse = companyProfileRepository.findByUserEmail("recruitment@cyberpulse.com").orElse(null);
        CompanyProfile datavibe = companyProfileRepository.findByUserEmail("talent@datavibe.ai").orElse(null);
        CompanyProfile nexabyte = companyProfileRepository.findByUserEmail("jobs@nexabyte.dev").orElse(null);

        if (technova == null) return;

        LocalDate today = LocalDate.now();

        // 1. Full Stack Java Developer Intern
        saveInternship(technova, "Full Stack Java Developer Intern", "Software Development", WorkMode.Remote,
                "Remote / Bangalore", "6 Months", 25000, 3,
                "Join our engineering team to design and deploy scalable RESTful services using Spring Boot and responsive interfaces in React. You will work on real production microservices, participate in agile sprint ceremonies, and write clean, testable code.",
                "B.Tech / MCA / M.Tech in CS/IT. Strong foundation in Java and Object-Oriented Design.",
                today.plusDays(25),
                Arrays.asList("Java", "Spring Boot", "MySQL", "REST API", "React", "Git"));

        // 2. Cloud DevOps & Infrastructure Intern
        if (cloudscale != null) {
            saveInternship(cloudscale, "Cloud DevOps & Infrastructure Intern", "Cloud & DevOps", WorkMode.Hybrid,
                    "Hyderabad, India", "6 Months", 30000, 2,
                    "Automate cloud deployments on AWS using Docker, Terraform, and GitHub Actions CI/CD pipelines. Maintain Kubernetes clusters and monitor real-time infrastructure metrics with Prometheus and Grafana.",
                    "Knowledge of Linux CLI, basic containerization with Docker, and cloud concepts.",
                    today.plusDays(18),
                    Arrays.asList("AWS", "Docker", "Kubernetes", "Linux", "CI/CD", "Git"));
        }

        // 3. Machine Learning & AI Research Intern
        if (datavibe != null) {
            saveInternship(datavibe, "Machine Learning & AI Research Intern", "Data Science & AI", WorkMode.Remote,
                    "Remote / Pune", "4 Months", 35000, 2,
                    "Develop and optimize predictive machine learning models, analyze extensive tabular and time-series datasets, and deploy inference pipelines using Python, Scikit-Learn, and FastAPI.",
                    "Proficiency in Python, linear algebra, statistics, and machine learning libraries.",
                    today.plusDays(30),
                    Arrays.asList("Python", "Machine Learning", "Data Science", "Pandas", "NumPy", "Git"));
        }

        // 4. Cyber Security & Threat Analysis Intern
        if (cyberpulse != null) {
            saveInternship(cyberpulse, "Cyber Security & Threat Analysis Intern", "Cyber Security", WorkMode.On_Site,
                    "Mumbai, India", "6 Months", 28000, 1,
                    "Perform vulnerability scans, assist in web application penetration testing (OWASP Top 10), inspect network telemetry for anomalous activity, and produce executive vulnerability remediation reports.",
                    "Understanding of TCP/IP, Linux fundamentals, and common attack vectors.",
                    today.plusDays(14),
                    Arrays.asList("Cyber Security", "Linux", "Python", "Git", "REST API"));
        }

        // 5. Frontend UI/UX Engineer Intern
        if (nexabyte != null) {
            saveInternship(nexabyte, "Frontend UI/UX Engineer Intern", "Web Development", WorkMode.Remote,
                    "Remote / Delhi NCR", "3 Months", 20000, 4,
                    "Transform modern Figma mockups into pixel-perfect, responsive web pages using modern HTML, CSS, JavaScript, and Tailwind CSS. Ensure smooth animations and clean component architecture.",
                    "Hands-on experience with modern JavaScript, CSS flexbox/grid, and component libraries.",
                    today.plusDays(10),
                    Arrays.asList("JavaScript", "React", "HTML", "CSS", "Tailwind CSS", "Figma", "Git"));
        }

        // 6. Backend Microservices Engineer Intern
        saveInternship(technova, "Backend Microservices Engineer Intern", "Software Development", WorkMode.Hybrid,
                "Bangalore, India", "6 Months", 30000, 2,
                "Build distributed systems and event-driven microservices using Spring Cloud, Docker, Kafka, and Redis caching. Optimize SQL queries and benchmark system throughput.",
                "Solid understanding of Core Java, data structures, and relational databases.",
                today.plusDays(40),
                Arrays.asList("Java", "Spring Boot", "Microservices", "Docker", "MySQL", "REST API"));

        // 7. Data Analyst & Business Intelligence Intern
        if (datavibe != null) {
            saveInternship(datavibe, "Data Analyst & Business Intelligence Intern", "Data Science & AI", WorkMode.Hybrid,
                    "Pune, India", "3 Months", 22000, 3,
                    "Extract and transform relational data using SQL, design interactive KPI executive dashboards in Tableau and Power BI, and present data-backed growth recommendations.",
                    "Strong SQL querying capabilities, analytical curiosity, and data storytelling.",
                    today.plusDays(21),
                    Arrays.asList("MySQL", "Data Science", "Python", "Tableau", "Git"));
        }

        // 8. Mobile Application Developer (Flutter/Android)
        if (nexabyte != null) {
            saveInternship(nexabyte, "Mobile Application Developer (Flutter/Android)", "Mobile App Development", WorkMode.Remote,
                    "Remote", "4 Months", 24000, 2,
                    "Build cross-platform mobile apps with Flutter and Dart, integrating REST APIs, state management (Bloc / Provider), and push notifications. Publish builds to Play Store testing tracks.",
                    "Experience creating at least one mobile app with Flutter or native Android.",
                    today.plusDays(16),
                    Arrays.asList("Flutter", "Android", "REST API", "Git", "JavaScript"));
        }

        // 9. Cloud Solutions Architect Intern
        if (cloudscale != null) {
            saveInternship(cloudscale, "Cloud Solutions Architect Intern", "Cloud & DevOps", WorkMode.Remote,
                    "Remote / Hyderabad", "6 Months", 32000, 1,
                    "Collaborate with senior cloud architects to architect highly available, fault-tolerant infrastructure topologies on AWS and Google Cloud Platform. Draft architecture diagrams and cost optimization analyses.",
                    "Familiarity with cloud security, VPCs, compute, and storage primitives.",
                    today.plusDays(28),
                    Arrays.asList("AWS", "Google Cloud", "Docker", "Linux", "CI/CD"));
        }

        // 10. Software Quality Assurance & Automation Intern
        if (cyberpulse != null) {
            saveInternship(cyberpulse, "Software Quality Assurance & Automation Intern", "Software Testing", WorkMode.Hybrid,
                    "Pune, India", "3 Months", 18000, 2,
                    "Develop automated end-to-end and API regression suites using JUnit, Postman, and Selenium. Log, track, and verify software bugs in Jira to ensure zero-defect deployments.",
                    "Basic programming skills in Java or Python, understanding of SDLC and test methodologies.",
                    today.plusDays(12),
                    Arrays.asList("Java", "JUnit", "Postman", "REST API", "Git", "MySQL"));
        }
    }

    private void saveInternship(CompanyProfile company, String title, String category, WorkMode mode,
                                String location, String duration, int stipend, int openings,
                                String desc, String elig, LocalDate deadline, List<String> skills) {
        Internship in = new Internship();
        in.setCompany(company);
        in.setTitle(title);
        in.setCategory(category);
        in.setWorkMode(mode);
        in.setLocation(location);
        in.setDuration(duration);
        in.setStipend(stipend);
        in.setOpenings(openings);
        in.setDescription(desc);
        in.setEligibility(elig);
        in.setDeadline(deadline);
        in.setActive(true);
        in.setApproved(true);
        in.setRequiredSkills(new HashSet<>(skills));
        internshipRepository.save(in);
    }
}
