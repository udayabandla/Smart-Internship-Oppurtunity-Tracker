/*
 * SMART INTERNSHIP OPPORTUNITIES
 * Single-file Java desktop project for CSE 213 Advanced Java Programming.
 *
 * HOW TO RUN:
 * 1. Save this entire file as SmartInternshipOpportunities.java
 * 2. Open a terminal in the folder containing this file.
 * 3. Compile: javac SmartInternshipOpportunities.java
 * 4. Run:     java SmartInternshipOpportunities
 *
 * DEMO LOGIN:
 * Student: rahul@student.com / pass123
 * Admin:   admin@smart.com / admin123
 *
 * Note: This syllabus-focused version uses in-memory Java Collections only.
 * Data resets when the application closes. No SQL/JDBC/database is used.
 * Only SmartInternshipOpportunities is public; the supporting classes are
 * package-private in this same source file to make copy/paste simple.
 */

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

// ===== Source from Main.java =====

public class SmartInternshipOpportunities {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* Keep the default Swing look and feel. */ }
            new LoginFrame(new AppContext()).setVisible(true);
        });
    }
}

// ===== Source from exception/ApplicationException.java =====

class ApplicationException extends Exception {
    private static final long serialVersionUID = 1L;
    public ApplicationException(String message) { super(message); }
}

// ===== Source from exception/InvalidInputException.java =====

class InvalidInputException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidInputException(String message) { super(message); }
}

// ===== Source from exception/InvalidLoginException.java =====

class InvalidLoginException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidLoginException(String message) { super(message); }
}

// ===== Source from interfaces/AuthenticationInterface.java =====

interface AuthenticationInterface {
    User login(String email, String password) throws InvalidLoginException;
    Student registerStudent(String name, String email, String password, String college,
                            String department, int graduationYear, String skills, String interests)
            throws InvalidInputException;
}

// ===== Source from interfaces/RecommendationInterface.java =====

interface RecommendationInterface {
    MatchResult calculateMatch(Student student, Internship internship);
    List<MatchResult> recommend(Student student, List<Internship> internships);
    Map<String, Integer> buildScoreMap(Student student, List<Internship> internships);
}

// ===== Source from model/Admin.java =====

class Admin extends User {
    public Admin(String id, String name, String email, String password) { super(id, name, email, password); }
    @Override public String getRole() { return "Admin"; }
}

// ===== Source from model/Application.java =====

class Application implements Identifiable {
    private final String id;
    private final String studentId;
    private final String internshipId;
    private final String studentName;
    private final String internshipTitle;
    private final String company;
    private final LocalDate appliedOn;
    private ApplicationStatus status;

    public Application(String id, Student student, Internship internship) {
        this.id = id;
        this.studentId = student.getId();
        this.internshipId = internship.getId();
        this.studentName = student.getName();
        this.internshipTitle = internship.getTitle();
        this.company = internship.getCompany();
        this.appliedOn = LocalDate.now();
        this.status = ApplicationStatus.PENDING;
    }
    @Override public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getInternshipId() { return internshipId; }
    public String getStudentName() { return studentName; }
    public String getInternshipTitle() { return internshipTitle; }
    public String getCompany() { return company; }
    public LocalDate getAppliedOn() { return appliedOn; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { if (status == null) throw new IllegalArgumentException("Status is required."); this.status = status; }
}

// ===== Source from model/ApplicationStatus.java =====

enum ApplicationStatus { PENDING, ACCEPTED, REJECTED }

// ===== Source from model/Identifiable.java =====

/** Implemented by model objects that have a stable identifier. */
interface Identifiable {
    String getId();
}

// ===== Source from model/Internship.java =====

class Internship implements Identifiable {
    private final String id;
    private String title, company, description, domain, location;
    private HashSet<String> requiredSkills;
    private int durationMonths;
    private double stipend;
    private LocalDate deadline;

    public Internship(String id, String title, String company, String description, String requiredSkills,
                      String domain, String location, int durationMonths, double stipend, LocalDate deadline) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Internship ID is required.");
        this.id = id.trim();
        updateDetails(title, company, description, requiredSkills, domain, location, durationMonths, stipend, deadline);
    }

    public final void updateDetails(String title, String company, String description, String requiredSkills,
                              String domain, String location, int durationMonths, double stipend, LocalDate deadline) {
        this.title = required(title, "Title");
        this.company = required(company, "Company");
        this.description = description == null ? "" : description.trim();
        this.requiredSkills = parseSkills(requiredSkills);
        this.domain = required(domain, "Domain");
        this.location = required(location, "Location");
        if (durationMonths <= 0) throw new IllegalArgumentException("Duration must be greater than zero.");
        if (stipend < 0) throw new IllegalArgumentException("Stipend cannot be negative.");
        if (deadline == null) throw new IllegalArgumentException("Application deadline is required.");
        this.durationMonths = durationMonths;
        this.stipend = stipend;
        this.deadline = deadline;
    }

    private static String required(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }
    private static HashSet<String> parseSkills(String text) {
        if (text == null || text.isBlank()) return new HashSet<>();
        return Arrays.stream(text.split(",")).map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty()).collect(Collectors.toCollection(HashSet::new));
    }
    @Override public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getDescription() { return description; }
    public HashSet<String> getRequiredSkills() { return new HashSet<>(requiredSkills); }
    public String getRequiredSkillsText() { return String.join(", ", requiredSkills); }
    public String getDomain() { return domain; }
    public String getLocation() { return location; }
    public int getDurationMonths() { return durationMonths; }
    public double getStipend() { return stipend; }
    public LocalDate getDeadline() { return deadline; }
    public boolean isOpen() { return !deadline.isBefore(LocalDate.now()); }
}

// ===== Source from model/MatchResult.java =====

class MatchResult {
    private final Internship internship;
    private final double percentage;
    private final Set<String> matchedSkills;
    private final Set<String> missingSkills;
    public MatchResult(Internship internship, double percentage, Set<String> matchedSkills, Set<String> missingSkills) {
        this.internship = internship;
        this.percentage = percentage;
        this.matchedSkills = Collections.unmodifiableSet(Set.copyOf(matchedSkills));
        this.missingSkills = Collections.unmodifiableSet(Set.copyOf(missingSkills));
    }
    public Internship getInternship() { return internship; }
    public double getPercentage() { return percentage; }
    public Set<String> getMatchedSkills() { return matchedSkills; }
    public Set<String> getMissingSkills() { return missingSkills; }
}

// ===== Source from model/Student.java =====

class Student extends User {
    private String college;
    private String department;
    private int graduationYear;
    private HashSet<String> skills;
    private String interests;

    public Student(String id, String name, String email, String password, String college,
                   String department, int graduationYear, String skills, String interests) {
        super(id, name, email, password);
        this.college = textOrEmpty(college);
        this.department = textOrEmpty(department);
        this.graduationYear = validateYear(graduationYear);
        this.skills = parseSkills(skills);
        this.interests = textOrEmpty(interests);
    }

    @Override public String getRole() { return "Student"; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = textOrEmpty(college); }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = textOrEmpty(department); }
    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int year) { this.graduationYear = validateYear(year); }
    private static int validateYear(int year) {
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Enter a valid graduation year.");
        return year;
    }
    public HashSet<String> getSkills() { return new HashSet<>(skills); }
    public String getSkillsText() { return String.join(", ", skills); }
    public void setSkillsFromText(String text) { this.skills = parseSkills(text); }
    public void setSkills(Set<String> skills) { this.skills = parseSkills(skills == null ? "" : String.join(",", skills)); }
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = textOrEmpty(interests); }

    private static HashSet<String> parseSkills(String text) {
        if (text == null || text.isBlank()) return new HashSet<>();
        return Arrays.stream(text.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }
    private static String textOrEmpty(String value) { return value == null ? "" : value.trim(); }
}

// ===== Source from model/User.java =====

/** Shared parent for all users. Demonstrates abstraction and encapsulation. */
abstract class User implements Identifiable {
    private final String id;
    private String name;
    private String email;
    private String password; // Demo-only in-memory credentials; do not use for real accounts.

    protected User(String id, String name, String email, String password) {
        this.id = requireText(id, "User ID");
        this.name = requireText(name, "Name");
        this.email = validateEmail(email);
        this.password = validatePassword(password);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required.");
        return value.trim();
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requireText(name, "Name"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = validateEmail(email); }
    private static String validateEmail(String email) {
        String clean = requireText(email, "Email").toLowerCase();
        if (!clean.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("Enter a valid email address.");
        return clean;
    }
    public boolean passwordMatches(String candidate) { return password.equals(candidate == null ? "" : candidate); }
    public void setPassword(String password) { this.password = validatePassword(password); }
    private static String validatePassword(String password) {
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must contain at least 6 characters.");
        return password;
    }

    public abstract String getRole();
}

// ===== Source from repository/GenericRepository.java =====

/** A small generic in-memory repository. Data lives only while the app is running. */
class GenericRepository<T extends Identifiable> {
    private final LinkedHashMap<String, T> records = new LinkedHashMap<>();

    public synchronized void add(T item) throws InvalidInputException {
        if (item == null || item.getId() == null || item.getId().isBlank())
            throw new InvalidInputException("Cannot add an empty record or missing ID.");
        String key = normalize(item.getId());
        if (records.containsKey(key)) throw new InvalidInputException("A record with this ID already exists.");
        records.put(key, item);
    }
    public synchronized boolean update(T item) {
        if (item == null || item.getId() == null) return false;
        String key = normalize(item.getId());
        if (!records.containsKey(key)) return false;
        records.put(key, item);
        return true;
    }
    public synchronized Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(records.get(normalize(id)));
    }
    public synchronized boolean remove(String id) { return id != null && records.remove(normalize(id)) != null; }
    public synchronized ArrayList<T> getAll() { return new ArrayList<>(records.values()); }
    public synchronized int size() { return records.size(); }
    private String normalize(String id) { return id.trim().toLowerCase(); }
}

// ===== Source from service/AppContext.java =====

/** Wires application services and creates sample in-memory data for the demo. */
class AppContext {
    private final DataStore store = new DataStore();
    private final AuthenticationService auth = new AuthenticationService(store);
    private final InternshipService internships = new InternshipService(store);
    private final ApplicationService applications = new ApplicationService(store);
    private final SkillRecommendationService recommendations = new SkillRecommendationService();

    public AppContext() {
        seedDemoData();
    }
    private void seedDemoData() {
        try {
            store.getStudents().add(new Student("STU001", "Rahul Sharma", "rahul@student.com", "pass123",
                    "ABC College", "CSE", 2027, "Java, SQL, OOP, Git", "Software Development"));
            store.getAdmins().add(new Admin("ADM001", "System Admin", "admin@smart.com", "admin123"));
            LocalDate future = LocalDate.now().plusDays(30);
            addSeed("INT001", "Java Developer Intern", "TechNova", "Build Java applications and APIs.", "Java, OOP, Git", "Software", "Hyderabad", 3, 8000, future);
            addSeed("INT002", "Python Data Intern", "DataBridge", "Work on Python data projects.", "Python, SQL, Pandas", "Data Science", "Remote", 2, 7000, future);
            addSeed("INT003", "Backend Developer Intern", "CodeWorks", "Develop backend services.", "Java, SQL, REST API", "Software", "Bengaluru", 3, 9000, future);
            addSeed("INT004", "Frontend Intern", "PixelCraft", "Build user interfaces.", "HTML, CSS, JavaScript", "Web Development", "Remote", 2, 5000, future);
            addSeed("INT005", "Data Analyst Intern", "Insight Labs", "Analyze and visualize business data.", "SQL, Excel, Python", "Data Science", "Chennai", 3, 7500, future);
            addSeed("INT006", "UI Design Intern", "DesignNest", "Create wireframes and interface designs.", "Figma, UI Design, Prototyping", "Design", "Pune", 2, 6000, future);
            addSeed("INT007", "Cybersecurity Intern", "SafeNet", "Assist with security reviews.", "Networking, Linux, Security", "Cybersecurity", "Hyderabad", 3, 8500, future);
            addSeed("INT008", "Android Intern", "AppOrbit", "Build Android application features.", "Java, Android, XML", "Mobile Development", "Remote", 3, 7000, future);
            addSeed("INT009", "Cloud Intern", "SkyStack", "Learn cloud deployment fundamentals.", "Linux, Cloud, Git", "Cloud", "Bengaluru", 3, 10000, future);
            addSeed("INT010", "QA Automation Intern", "QualityFirst", "Create automated software tests.", "Java, Selenium, Testing", "Software Testing", "Noida", 2, 6500, future);
            addSeed("INT011", "Expired Web Intern", "OldApps", "This listing is closed.", "Python, HTML, CSS", "Web Development", "Remote", 2, 0, LocalDate.now().minusDays(5));
        } catch (InvalidInputException ex) {
            throw new IllegalStateException("Unable to load sample data: " + ex.getMessage(), ex);
        }
    }
    private void addSeed(String id, String title, String company, String description, String skills,
                         String domain, String location, int duration, double stipend, LocalDate deadline)
            throws InvalidInputException {
        store.getInternships().add(new Internship(id, title, company, description, skills, domain, location, duration, stipend, deadline));
    }
    public DataStore getStore() { return store; }
    public AuthenticationService getAuth() { return auth; }
    public InternshipService getInternships() { return internships; }
    public ApplicationService getApplications() { return applications; }
    public RecommendationService getRecommendations() { return recommendations; }
}

// ===== Source from service/ApplicationService.java =====

class ApplicationService {
    private final DataStore store;
    public ApplicationService(DataStore store) { this.store = store; }

    public Application apply(Student student, Internship internship) throws ApplicationException {
        if (student == null || internship == null) throw new ApplicationException("Student and internship are required.");
        if (!internship.isOpen()) throw new ApplicationException("The application deadline has passed.");
        boolean alreadyApplied = store.getApplications().getAll().stream()
                .anyMatch(a -> a.getStudentId().equals(student.getId()) && a.getInternshipId().equals(internship.getId()));
        if (alreadyApplied) throw new ApplicationException("You have already applied for this internship.");
        Application app = new Application("APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), student, internship);
        try { store.getApplications().add(app); }
        catch (Exception ex) { throw new ApplicationException("Could not submit application: " + ex.getMessage()); }
        return app;
    }

    public void updateStatus(String applicationId, ApplicationStatus status) throws ApplicationException {
        Application app = store.getApplications().findById(applicationId).orElseThrow(() -> new ApplicationException("Application not found."));
        app.setStatus(status);
    }

    public List<Application> getAll() { return store.getApplications().getAll(); }
    public List<Application> getApplications(Student student) {
        if (student == null) return List.of();
        return getAll().stream().filter(a -> a.getStudentId().equals(student.getId())).collect(Collectors.toList());
    }
    public Map<ApplicationStatus, Long> getStatistics(Student student) {
        Map<ApplicationStatus, Long> stats = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) stats.put(status, 0L);
        getApplications(student).forEach(a -> stats.put(a.getStatus(), stats.get(a.getStatus()) + 1L));
        return stats;
    }
}

// ===== Source from service/AuthenticationService.java =====

class AuthenticationService implements AuthenticationInterface {
    private final DataStore store;
    public AuthenticationService(DataStore store) { this.store = store; }

    @Override public User login(String email, String password) throws InvalidLoginException {
        if (email == null || email.isBlank() || password == null || password.isBlank())
            throw new InvalidLoginException("Enter both email and password.");
        String cleanEmail = email.trim();
        for (Student student : store.getStudents().getAll()) {
            if (student.getEmail().equalsIgnoreCase(cleanEmail) && student.passwordMatches(password)) return student;
        }
        for (Admin admin : store.getAdmins().getAll()) {
            if (admin.getEmail().equalsIgnoreCase(cleanEmail) && admin.passwordMatches(password)) return admin;
        }
        throw new InvalidLoginException("Invalid email or password.");
    }

    @Override public Student registerStudent(String name, String email, String password, String college,
                                             String department, int graduationYear, String skills, String interests)
            throws InvalidInputException {
        try {
            if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
                throw new InvalidInputException("Enter a valid email address.");
            boolean duplicate = store.getStudents().getAll().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(email.trim()))
                    || store.getAdmins().getAll().stream().anyMatch(a -> a.getEmail().equalsIgnoreCase(email.trim()));
            if (duplicate) throw new InvalidInputException("An account with this email already exists.");
            Student student = new Student("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    name, email, password, college, department, graduationYear, skills, interests);
            store.getStudents().add(student);
            return student;
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }
}

// ===== Source from service/DataStore.java =====

/** In-memory data store built from Java Collections; no SQL or persistence is used. */
class DataStore {
    private final GenericRepository<Student> students = new GenericRepository<>();
    private final GenericRepository<Admin> admins = new GenericRepository<>();
    private final GenericRepository<Internship> internships = new GenericRepository<>();
    private final GenericRepository<Application> applications = new GenericRepository<>();
    public GenericRepository<Student> getStudents() { return students; }
    public GenericRepository<Admin> getAdmins() { return admins; }
    public GenericRepository<Internship> getInternships() { return internships; }
    public GenericRepository<Application> getApplications() { return applications; }
}

// ===== Source from service/InternshipService.java =====

class InternshipService {
    private final DataStore store;
    private final AtomicInteger nextId = new AtomicInteger(100);
    public InternshipService(DataStore store) { this.store = store; }
    public List<Internship> getAll() { return store.getInternships().getAll(); }
    public Optional<Internship> findById(String id) { return store.getInternships().findById(id); }

    public Internship addInternship(String title, String company, String description, String requiredSkills,
                                    String domain, String location, int durationMonths, double stipend, LocalDate deadline)
            throws InvalidInputException {
        try {
            Internship item = new Internship("INT" + nextId.getAndIncrement(), title, company, description,
                    requiredSkills, domain, location, durationMonths, stipend, deadline);
            store.getInternships().add(item);
            return item;
        } catch (IllegalArgumentException ex) { throw new InvalidInputException(ex.getMessage()); }
    }

    public void updateInternship(String id, String title, String company, String description, String requiredSkills,
                                 String domain, String location, int durationMonths, double stipend, LocalDate deadline)
            throws InvalidInputException {
        Internship item = store.getInternships().findById(id).orElseThrow(() -> new InvalidInputException("Internship not found."));
        try { item.updateDetails(title, company, description, requiredSkills, domain, location, durationMonths, stipend, deadline); }
        catch (IllegalArgumentException ex) { throw new InvalidInputException(ex.getMessage()); }
    }

    public void deleteInternship(String id) throws InvalidInputException {
        if (!store.getInternships().remove(id)) throw new InvalidInputException("Internship not found.");
    }

    public List<Internship> search(String keyword) { return search(keyword, ""); }
    public List<Internship> search(String keyword, String domain) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase();
        String d = domain == null ? "" : domain.trim().toLowerCase();
        return getAll().stream()
                .filter(i -> q.isEmpty() || (i.getTitle() + " " + i.getCompany() + " " + i.getDescription() + " " + i.getRequiredSkillsText()).toLowerCase().contains(q))
                .filter(i -> d.isEmpty() || i.getDomain().toLowerCase().contains(d))
                .collect(Collectors.toList());
    }
}

// ===== Source from service/RecommendationService.java =====

/** Named service facade kept simple for the course project. */
interface RecommendationService extends RecommendationInterface { }

// ===== Source from service/SkillRecommendationService.java =====

class SkillRecommendationService implements RecommendationService {
    @Override public MatchResult calculateMatch(Student student, Internship internship) {
        Set<String> studentSkills = student == null ? Set.of() : student.getSkills();
        Set<String> required = internship == null ? Set.of() : internship.getRequiredSkills();
        Set<String> matched = new HashSet<>(required);
        matched.retainAll(studentSkills);
        Set<String> missing = new HashSet<>(required);
        missing.removeAll(studentSkills);
        double score = required.isEmpty() ? 0.0 : (matched.size() * 100.0 / required.size());
        return new MatchResult(internship, score, matched, missing);
    }

    @Override public List<MatchResult> recommend(Student student, List<Internship> internships) {
        if (student == null || internships == null) return new ArrayList<>();
        return internships.stream()
                .filter(Internship::isOpen)
                .map(i -> calculateMatch(student, i))
                .filter(result -> result.getPercentage() > 0.0)
                .sorted(Comparator.comparingDouble(MatchResult::getPercentage).reversed()
                        .thenComparing(r -> r.getInternship().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Override public Map<String, Integer> buildScoreMap(Student student, List<Internship> internships) {
        Map<String, Integer> scores = new HashMap<>();
        if (student == null || internships == null) return scores;
        internships.forEach(i -> scores.put(i.getId(), (int) Math.round(calculateMatch(student, i).getPercentage())));
        return scores;
    }
}

// ===== Source from view/AdminDashboardFrame.java =====

final class AdminDashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient AppContext context;
    private final DefaultTableModel internshipModel = tableModel("ID", "Title", "Company", "Domain", "Location", "Deadline", "Skills");
    private final DefaultTableModel studentModel = tableModel("ID", "Name", "Email", "College", "Department", "Skills");
    private final DefaultTableModel applicationModel = tableModel("Application ID", "Student", "Internship", "Company", "Applied", "Status");
    private final JTable internshipTable = new JTable(internshipModel);
    private final JTable studentTable = new JTable(studentModel);
    private final JTable applicationTable = new JTable(applicationModel);
    private final JComboBox<ApplicationStatus> statusChoice = new JComboBox<>(ApplicationStatus.values());

    public AdminDashboardFrame(AppContext context, Admin admin) {
        this.context = context;
        setTitle("Smart Internship Opportunities | Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720); setLocationRelativeTo(null);
        buildUi(admin);
        refreshAll();
    }

    private void buildUi(Admin admin) {
        JPanel root = new JPanel(new BorderLayout(8, 8)); root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); root.setBackground(UiStyle.PALE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(UiStyle.NAVY); header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("ADMIN DASHBOARD     |     " + admin.getName()); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif", Font.BOLD, 17));
        JButton logout = new JButton("Logout"); logout.addActionListener(e -> { dispose(); new LoginFrame(context).setVisible(true); });
        header.add(title, BorderLayout.CENTER); header.add(logout, BorderLayout.EAST);
        JTabbedPane tabs = new JTabbedPane(); tabs.addTab("Manage Internships", buildInternshipTab()); tabs.addTab("Students", buildStudentTab()); tabs.addTab("Applications", buildApplicationTab());
        root.add(header, BorderLayout.NORTH); root.add(tabs, BorderLayout.CENTER); setContentPane(root);
    }

    private JPanel buildInternshipTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel); panel.add(new JScrollPane(internshipTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = UiStyle.button("Add Internship"), edit = new JButton("Edit Selected"), delete = new JButton("Delete Selected"), refresh = new JButton("Refresh");
        actions.add(refresh); actions.add(add); actions.add(edit); actions.add(delete); panel.add(actions, BorderLayout.SOUTH);
        add.addActionListener(e -> showInternshipForm(null)); edit.addActionListener(e -> editSelectedInternship()); delete.addActionListener(e -> deleteSelectedInternship()); refresh.addActionListener(e -> refreshInternships());
        return panel;
    }

    private JPanel buildStudentTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel); panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh Students"); JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(refresh); panel.add(actions, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshStudents()); return panel;
    }

    private JPanel buildApplicationTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel); panel.add(new JScrollPane(applicationTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(new JLabel("Change selected status:")); actions.add(statusChoice);
        JButton update = UiStyle.button("Update Status"), refresh = new JButton("Refresh"); actions.add(update); actions.add(refresh); panel.add(actions, BorderLayout.SOUTH);
        update.addActionListener(e -> updateSelectedApplication()); refresh.addActionListener(e -> refreshApplications()); return panel;
    }

    private void showInternshipForm(Internship existing) {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
        JTextField company = new JTextField(existing == null ? "" : existing.getCompany());
        JTextField description = new JTextField(existing == null ? "" : existing.getDescription());
        JTextField skills = new JTextField(existing == null ? "" : existing.getRequiredSkillsText());
        JTextField domain = new JTextField(existing == null ? "" : existing.getDomain());
        JTextField location = new JTextField(existing == null ? "" : existing.getLocation());
        JTextField duration = new JTextField(existing == null ? "3" : String.valueOf(existing.getDurationMonths()));
        JTextField stipend = new JTextField(existing == null ? "0" : String.valueOf(existing.getStipend()));
        JTextField deadline = new JTextField(existing == null ? LocalDate.now().plusDays(30).toString() : existing.getDeadline().toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 7, 7));
        addField(form, "Internship title", title); addField(form, "Company", company); addField(form, "Description", description);
        addField(form, "Required skills (comma separated)", skills); addField(form, "Domain", domain); addField(form, "Location", location);
        addField(form, "Duration (months)", duration); addField(form, "Stipend", stipend); addField(form, "Deadline (YYYY-MM-DD)", deadline);
        int result = JOptionPane.showConfirmDialog(this, form, existing == null ? "Add Internship" : "Edit Internship", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            int months = Integer.parseInt(duration.getText().trim()); double amount = Double.parseDouble(stipend.getText().trim());
            LocalDate due = LocalDate.parse(deadline.getText().trim());
            if (existing == null) {
                Internship created = context.getInternships().addInternship(title.getText(), company.getText(), description.getText(), skills.getText(), domain.getText(), location.getText(), months, amount, due);
                JOptionPane.showMessageDialog(this, "Internship added: " + created.getId());
            } else {
                context.getInternships().updateInternship(existing.getId(), title.getText(), company.getText(), description.getText(), skills.getText(), domain.getText(), location.getText(), months, amount, due);
                JOptionPane.showMessageDialog(this, "Internship updated.");
            }
            refreshInternships();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration and stipend must be valid numbers.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter the deadline in YYYY-MM-DD format.", "Invalid date", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save internship", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editSelectedInternship() {
        int row = internshipTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select an internship first."); return; }
        String id = String.valueOf(internshipModel.getValueAt(internshipTable.convertRowIndexToModel(row), 0));
        context.getInternships().findById(id).ifPresent(this::showInternshipForm);
    }
    private void deleteSelectedInternship() {
        int row = internshipTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select an internship first."); return; }
        String id = String.valueOf(internshipModel.getValueAt(internshipTable.convertRowIndexToModel(row), 0));
        int confirm = JOptionPane.showConfirmDialog(this, "Delete internship " + id + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try { context.getInternships().deleteInternship(id); refreshInternships(); }
            catch (InvalidInputException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
    private void updateSelectedApplication() {
        int row = applicationTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select an application first."); return; }
        String id = String.valueOf(applicationModel.getValueAt(applicationTable.convertRowIndexToModel(row), 0));
        try { context.getApplications().updateStatus(id, (ApplicationStatus) statusChoice.getSelectedItem()); refreshApplications(); JOptionPane.showMessageDialog(this, "Application status updated."); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Update failed", JOptionPane.WARNING_MESSAGE); }
    }
    private void refreshAll() { refreshInternships(); refreshStudents(); refreshApplications(); }
    private void refreshInternships() {
        internshipModel.setRowCount(0);
        for (Internship i : context.getInternships().getAll()) internshipModel.addRow(new Object[]{i.getId(), i.getTitle(), i.getCompany(), i.getDomain(), i.getLocation(), i.getDeadline(), i.getRequiredSkillsText()});
    }
    private void refreshStudents() {
        studentModel.setRowCount(0);
        for (Student s : context.getStore().getStudents().getAll()) studentModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getCollege(), s.getDepartment(), s.getSkillsText()});
    }
    private void refreshApplications() {
        applicationModel.setRowCount(0);
        for (Application a : context.getApplications().getAll()) applicationModel.addRow(new Object[]{a.getId(), a.getStudentName(), a.getInternshipTitle(), a.getCompany(), a.getAppliedOn(), a.getStatus()});
    }
    private static void addField(JPanel panel, String label, Component field) { panel.add(new JLabel(label)); panel.add(field); }
    private static DefaultTableModel tableModel(String... columns) { return new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } }; }
}

// ===== Source from view/LoginFrame.java =====

final class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient AppContext context;
    private final JTextField emailField = new JTextField("rahul@student.com", 22);
    private final JPasswordField passwordField = new JPasswordField("pass123", 22);

    public LoginFrame(AppContext context) {
        this.context = context;
        setTitle("Smart Internship Opportunities | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 440);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UiStyle.PALE);
        root.setBorder(BorderFactory.createEmptyBorder(26, 34, 26, 34));
        JLabel heading = UiStyle.title("SMART INTERNSHIP OPPORTUNITIES");
        JLabel subtitle = new JLabel("Find internships that match your skills");
        subtitle.setForeground(Color.DARK_GRAY);
        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headingPanel.setOpaque(false);
        headingPanel.add(heading);
        headingPanel.add(subtitle);
        root.add(headingPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 226, 235)), BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(passwordField, gbc);
        JButton login = UiStyle.button("Login");
        JButton register = new JButton("Create student account");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        form.add(login, gbc);
        gbc.gridy = 3;
        form.add(register, gbc);
        root.add(form, BorderLayout.CENTER);

        JLabel demo = new JLabel("Demo student: rahul@student.com / pass123    |    Admin: admin@smart.com / admin123");
        demo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        demo.setForeground(new Color(80, 88, 100));
        root.add(demo, BorderLayout.SOUTH);
        setContentPane(root);

        login.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegistrationDialog(this, context, emailField).setVisible(true));
    }

    private void doLogin() {
        try {
            User user = context.getAuth().login(emailField.getText(), new String(passwordField.getPassword()));
            dispose();
            if (user instanceof Student student) new StudentDashboardFrame(context, student).setVisible(true);
            else if (user instanceof Admin admin) new AdminDashboardFrame(context, admin).setVisible(true);
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ===== Source from view/RegistrationDialog.java =====

final class RegistrationDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final transient AppContext context;
    private final JTextField name = new JTextField(20), email = new JTextField(20), college = new JTextField(20),
            department = new JTextField("CSE", 20), year = new JTextField("2027", 20), skills = new JTextField(20), interests = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JTextField loginEmail;

    RegistrationDialog(Frame owner, AppContext context, JTextField loginEmail) {
        super(owner, "Student Registration", true);
        this.context = context;
        this.loginEmail = loginEmail;
        setSize(500, 520);
        setLocationRelativeTo(owner);
        buildUi();
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 9));
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        addRow(form, "Full name", name); addRow(form, "Email", email); addRow(form, "Password (min 6)", password);
        addRow(form, "College", college); addRow(form, "Department", department); addRow(form, "Graduation year", year);
        addRow(form, "Skills (comma separated)", skills); addRow(form, "Interests", interests);
        JButton create = UiStyle.button("Create Account");
        JButton cancel = new JButton("Cancel");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(cancel); actions.add(create);
        setLayout(new BorderLayout()); add(UiStyle.title("Register as a student"), BorderLayout.NORTH); add(form, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> register());
    }
    private void addRow(JPanel panel, String label, Component field) { panel.add(new JLabel(label)); panel.add(field); }

    private void register() {
        try {
            int gradYear = Integer.parseInt(year.getText().trim());
            Student student = context.getAuth().registerStudent(name.getText(), email.getText(), new String(password.getPassword()),
                    college.getText(), department.getText(), gradYear, skills.getText(), interests.getText());
            loginEmail.setText(student.getEmail());
            JOptionPane.showMessageDialog(this, "Registration successful. You can now log in.", "Account created", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Graduation year must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.WARNING_MESSAGE);
        }
    }
}

// ===== Source from view/StudentDashboardFrame.java =====

final class StudentDashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient AppContext context;
    private final transient Student student;
    private final DefaultTableModel internshipModel = model("ID", "Title", "Company", "Domain", "Required skills", "Deadline");
    private final DefaultTableModel matchModel = model("ID", "Internship", "Company", "Match %", "Matched skills", "Missing skills");
    private final DefaultTableModel applicationModel = model("Application ID", "Internship", "Company", "Applied on", "Status");
    private final JTable internshipTable = new JTable(internshipModel);
    private final JTable matchTable = new JTable(matchModel);
    private final JTable applicationTable = new JTable(applicationModel);
    private final JTextField searchField = new JTextField(18);
    private final JTextField domainField = new JTextField(10);
    private transient List<Internship> shownInternships = List.of();
    private transient List<MatchResult> shownMatches = List.of();

    public StudentDashboardFrame(AppContext context, Student student) {
        this.context = context;
        this.student = student;
        setTitle("Smart Internship Opportunities | Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setLocationRelativeTo(null);
        buildUi();
        refreshInternships();
        refreshApplications();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10)); root.setBackground(UiStyle.PALE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(UiStyle.NAVY); header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel heading = new JLabel("SMART INTERNSHIP OPPORTUNITIES     |     Welcome, " + student.getName());
        heading.setForeground(Color.WHITE); heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        JButton logout = new JButton("Logout"); logout.addActionListener(e -> { dispose(); new LoginFrame(context).setVisible(true); });
        header.add(heading, BorderLayout.CENTER); header.add(logout, BorderLayout.EAST);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", buildProfilePanel());
        tabs.addTab("Internship Opportunities", buildInternshipPanel());
        tabs.addTab("Smart Matches", buildMatchPanel());
        tabs.addTab("My Applications", buildApplicationsPanel());
        root.add(header, BorderLayout.NORTH); root.add(tabs, BorderLayout.CENTER);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); setContentPane(root);
    }

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout()); UiStyle.pad(panel);
        JTextField name = new JTextField(student.getName(), 25), college = new JTextField(student.getCollege(), 25),
                department = new JTextField(student.getDepartment(), 25), year = new JTextField(String.valueOf(student.getGraduationYear()), 25),
                skills = new JTextField(student.getSkillsText(), 25), interests = new JTextField(student.getInterests(), 25);
        JLabel email = new JLabel(student.getEmail());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(8, 8, 8, 8); c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addFormRow(panel, c, row++, "Name", name); addFormRow(panel, c, row++, "Email", email);
        addFormRow(panel, c, row++, "College", college); addFormRow(panel, c, row++, "Department", department);
        addFormRow(panel, c, row++, "Graduation year", year); addFormRow(panel, c, row++, "Skills (comma separated)", skills);
        addFormRow(panel, c, row++, "Interests", interests);
        JButton save = UiStyle.button("Save Profile"); c.gridx = 1; c.gridy = row; c.weightx = 1; panel.add(save, c);
        save.addActionListener(e -> {
            try {
                student.setName(name.getText()); student.setCollege(college.getText()); student.setDepartment(department.getText());
                student.setGraduationYear(Integer.parseInt(year.getText().trim())); student.setSkillsFromText(skills.getText()); student.setInterests(interests.getText());
                JOptionPane.showMessageDialog(this, "Profile updated for this session.");
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Graduation year must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE); }
            catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid profile", JOptionPane.WARNING_MESSAGE); }
        });
        return panel;
    }

    private JPanel buildInternshipPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Keyword:")); search.add(searchField); search.add(new JLabel("Domain:")); search.add(domainField);
        JButton find = new JButton("Search"); JButton reset = new JButton("Show All"); search.add(find); search.add(reset);
        panel.add(search, BorderLayout.NORTH); panel.add(new JScrollPane(internshipTable), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton apply = UiStyle.button("Apply for Selected Internship"); bottom.add(apply); panel.add(bottom, BorderLayout.SOUTH);
        find.addActionListener(e -> { shownInternships = context.getInternships().search(searchField.getText(), domainField.getText()).stream().filter(Internship::isOpen).toList(); fillInternshipTable(); });
        reset.addActionListener(e -> { searchField.setText(""); domainField.setText(""); refreshInternships(); });
        apply.addActionListener(e -> applySelectedInternship());
        internshipTable.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) applySelectedInternship(); } });
        return panel;
    }

    private JPanel buildMatchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel);
        JPanel top = new JPanel(new BorderLayout());
        JLabel note = new JLabel("Recommendations are ranked by the percentage of required skills you match.");
        JButton calculate = UiStyle.button("Find My Matches"); top.add(note, BorderLayout.CENTER); top.add(calculate, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH); panel.add(new JScrollPane(matchTable), BorderLayout.CENTER);
        JButton apply = UiStyle.button("Apply for Selected Match"); JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.add(apply); panel.add(bottom, BorderLayout.SOUTH);
        calculate.addActionListener(e -> calculateMatchesInBackground()); apply.addActionListener(e -> applySelectedMatch());
        return panel;
    }

    private JPanel buildApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel);
        JButton refresh = new JButton("Refresh Applications"); JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.add(refresh);
        panel.add(top, BorderLayout.NORTH); panel.add(new JScrollPane(applicationTable), BorderLayout.CENTER);
        refresh.addActionListener(e -> refreshApplications()); return panel;
    }

    private void calculateMatchesInBackground() {
        JDialog progress = new JDialog(this, "Finding internship matches", false);
        progress.add(new JLabel("Calculating your recommendations...", SwingConstants.CENTER)); progress.setSize(300, 90); progress.setLocationRelativeTo(this);
        SwingWorker<List<MatchResult>, Void> worker = new SwingWorker<>() {
            @Override protected List<MatchResult> doInBackground() {
                return context.getRecommendations().recommend(student, context.getInternships().getAll());
            }
            @Override protected void done() {
                progress.dispose();
                try {
                    shownMatches = get(); matchModel.setRowCount(0);
                    for (MatchResult result : shownMatches) matchModel.addRow(new Object[]{result.getInternship().getId(), result.getInternship().getTitle(), result.getInternship().getCompany(), String.format("%.1f%%", result.getPercentage()), String.join(", ", result.getMatchedSkills()), String.join(", ", result.getMissingSkills())});
                    if (shownMatches.isEmpty()) JOptionPane.showMessageDialog(StudentDashboardFrame.this, "No matches found. Add more skills to your profile and try again.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Could not calculate matches: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute(); progress.setVisible(true);
    }

    private void refreshInternships() {
        shownInternships = context.getInternships().getAll().stream().filter(Internship::isOpen).toList();
        fillInternshipTable();
    }
    private void fillInternshipTable() {
        internshipModel.setRowCount(0);
        for (Internship i : shownInternships) internshipModel.addRow(new Object[]{i.getId(), i.getTitle(), i.getCompany(), i.getDomain(), i.getRequiredSkillsText(), i.getDeadline()});
    }
    private void refreshApplications() {
        applicationModel.setRowCount(0);
        for (Application a : context.getApplications().getApplications(student)) applicationModel.addRow(new Object[]{a.getId(), a.getInternshipTitle(), a.getCompany(), a.getAppliedOn(), a.getStatus()});
    }
    private void applySelectedInternship() {
        int row = internshipTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select an internship first."); return; }
        apply(shownInternships.get(internshipTable.convertRowIndexToModel(row)));
    }
    private void applySelectedMatch() {
        int row = matchTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select a recommendation first."); return; }
        apply(shownMatches.get(matchTable.convertRowIndexToModel(row)).getInternship());
    }
    private void apply(Internship internship) {
        try { context.getApplications().apply(student, internship); JOptionPane.showMessageDialog(this, "Application submitted successfully."); refreshApplications(); }
        catch (ApplicationException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Application not submitted", JOptionPane.WARNING_MESSAGE); }
    }
    private static DefaultTableModel model(String... columns) { return new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } }; }
    private static void addFormRow(JPanel panel, GridBagConstraints c, int row, String label, Component field) {
        c.gridy = row; c.gridx = 0; c.weightx = 0; panel.add(new JLabel(label), c); c.gridx = 1; c.weightx = 1; panel.add(field, c);
    }
}

// ===== Source from view/UiStyle.java =====

final class UiStyle {
    static final Color NAVY = new Color(28, 45, 75);
    static final Color BLUE = new Color(47, 112, 210);
    static final Color PALE = new Color(244, 247, 252);
    static final Color GREEN = new Color(36, 145, 104);
    private UiStyle() { }
    static JLabel title(String text) { JLabel label = new JLabel(text); label.setFont(new Font("SansSerif", Font.BOLD, 22)); label.setForeground(NAVY); return label; }
    static JButton button(String text) { JButton button = new JButton(text); button.setFont(new Font("SansSerif", Font.BOLD, 13)); button.setFocusPainted(false); button.setBackground(BLUE); button.setForeground(Color.WHITE); button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14)); return button; }
    static void pad(JComponent component) { component.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16)); }
}
