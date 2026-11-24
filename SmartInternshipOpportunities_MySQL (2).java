/*
 * SMART INTERNSHIP OPPORTUNITIES
 * Single-file Java desktop project for CSE 213 Advanced Java Programming.
 *
 * HOW TO RUN:
 * 1. Save this entire file as SmartInternshipOpportunities_MySQL.java
 * 2. Open a terminal in the folder containing this file.
 * 3. Compile: javac SmartInternshipOpportunities_MySQL.java
 * 4. Run:     java SmartInternshipOpportunities_MySQL
 *
 * DEMO LOGIN:
 * Student: rahul@student.com / pass123
 * Admin:   admin@smart.com / admin123
 *
 * MySQL database: smart_internship_db (JDBC)
 * Set DB_USER and DB_PASSWORD in DatabaseManager before running.
 * Features: explainable skill matching, resume upload, employer applicant pipeline,
 * direct accept/reject, and built-in interview scheduling using Java Swing + JDBC.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// ===== Source from Main.java =====

public class SmartInternshipOpportunities_MySQL{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* Keep the default Swing look and feel. */ }
            try {
                DatabaseManager.initialize();
                new LoginFrame(new AppContext()).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL. Check MySQL Server and DB settings.\n" + ex.getMessage(),
                    "Database connection error", JOptionPane.ERROR_MESSAGE);
            }
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

// Registered employer account. Company users can create internship listings.
class Company extends User {
    private String industry, location, description;
    public Company(String id, String name, String email, String password, String industry, String location, String description) {
        super(id, name, email, password);
        this.industry = industry == null ? "" : industry.trim();
        this.location = location == null ? "" : location.trim();
        this.description = description == null ? "" : description.trim();
    }
    @Override public String getRole() { return "Company"; }
    public String getIndustry() { return industry; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
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
    public Application(String id, String studentId, String internshipId, String studentName,
                       String internshipTitle, String company, LocalDate appliedOn, ApplicationStatus status) {
        this.id = id; this.studentId = studentId; this.internshipId = internshipId;
        this.studentName = studentName; this.internshipTitle = internshipTitle; this.company = company;
        this.appliedOn = appliedOn; this.status = status;
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

enum MeetingStatus { SCHEDULED, ACCEPTED, DECLINED, COMPLETED, CANCELLED }

class Meeting implements Identifiable {
    private final String id;
    private final String applicationId;
    private final String studentId;
    private final String studentName;
    private final String company;
    private final String internshipTitle;
    private final LocalDateTime meetingDateTime;
    private final String mode;
    private final String notes;
    private MeetingStatus status;

    public Meeting(String id, String applicationId, String studentId, String studentName, String company,
                   String internshipTitle, LocalDateTime meetingDateTime, String mode, String notes, MeetingStatus status) {
        this.id = id; this.applicationId = applicationId; this.studentId = studentId; this.studentName = studentName;
        this.company = company; this.internshipTitle = internshipTitle; this.meetingDateTime = meetingDateTime;
        this.mode = mode == null ? "Online" : mode.trim(); this.notes = notes == null ? "" : notes.trim();
        this.status = status == null ? MeetingStatus.SCHEDULED : status;
    }
    @Override public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getCompany() { return company; }
    public String getInternshipTitle() { return internshipTitle; }
    public LocalDateTime getMeetingDateTime() { return meetingDateTime; }
    public String getMode() { return mode; }
    public String getNotes() { return notes; }
    public MeetingStatus getStatus() { return status; }
    public void setStatus(MeetingStatus status) { this.status = status; }
}

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
    private String resumePath = "";

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
    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = textOrEmpty(resumePath); }

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
    String getPasswordForDatabase() { return password; }
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

// ===== MySQL JDBC DATABASE LAYER =====
// Change DB_USER and DB_PASSWORD to match your local MySQL installation.
class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/smart_internship_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "2011";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    public static void initialize() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC", DB_USER, DB_PASSWORD);
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS smart_internship_db");
        }
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(40) PRIMARY KEY, name VARCHAR(120) NOT NULL, email VARCHAR(160) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL, college VARCHAR(160), department VARCHAR(100), " +
                "graduation_year INT, skills TEXT, interests TEXT, industry VARCHAR(120), location VARCHAR(160), description TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS internships (" +
                "id VARCHAR(40) PRIMARY KEY, title VARCHAR(180) NOT NULL, company VARCHAR(160) NOT NULL, description TEXT, " +
                "required_skills TEXT, domain VARCHAR(120), location VARCHAR(160), duration_months INT, stipend DOUBLE, deadline DATE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS applications (" +
                "id VARCHAR(40) PRIMARY KEY, student_id VARCHAR(40) NOT NULL, internship_id VARCHAR(40) NOT NULL, " +
                "student_name VARCHAR(120), internship_title VARCHAR(180), company VARCHAR(160), applied_on DATE, status VARCHAR(20), " +
                "UNIQUE KEY unique_student_internship (student_id, internship_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS resumes (" +
                "student_id VARCHAR(40) PRIMARY KEY, resume_path VARCHAR(500) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS meetings (" +
                "id VARCHAR(40) PRIMARY KEY, application_id VARCHAR(40) NOT NULL, student_id VARCHAR(40) NOT NULL, " +
                "student_name VARCHAR(120), company VARCHAR(160), internship_title VARCHAR(180), " +
                "meeting_date DATETIME NOT NULL, mode VARCHAR(40), notes TEXT, status VARCHAR(20))");
        }
    }

    public static void saveUser(User user) throws SQLException {
        String role = user.getRole();
        String college = "", department = "", skills = "", interests = "", industry = "", location = "", description = "";
        int year = 0;
        if (user instanceof Student) {
            Student x = (Student) user; college=x.getCollege(); department=x.getDepartment();
            year=x.getGraduationYear(); skills=x.getSkillsText(); interests=x.getInterests();
        } else if (user instanceof Company) {
            Company x = (Company) user; industry=x.getIndustry(); location=x.getLocation(); description=x.getDescription();
        }
        String sql = "INSERT INTO users(id,name,email,password,role,college,department,graduation_year,skills,interests,industry,location,description) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), role=VALUES(role), college=VALUES(college), department=VALUES(department), graduation_year=VALUES(graduation_year), skills=VALUES(skills), interests=VALUES(interests), industry=VALUES(industry), location=VALUES(location), description=VALUES(description)";
        try (Connection c=getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,user.getId()); ps.setString(2,user.getName()); ps.setString(3,user.getEmail());
            // For a classroom demo. A production system should store salted password hashes.
            ps.setString(4, user.passwordMatches("") ? "" : getPasswordForStorage(user));
            ps.setString(5,role); ps.setString(6,college); ps.setString(7,department); ps.setInt(8,year);
            ps.setString(9,skills); ps.setString(10,interests); ps.setString(11,industry); ps.setString(12,location); ps.setString(13,description);
            ps.executeUpdate();
        }
    }

    // Package-private accessor is intentionally provided by User for this single-file academic demo.
    private static String getPasswordForStorage(User user) { return user.getPasswordForDatabase(); }

    public static void saveInternship(Internship i) throws SQLException {
        String sql="INSERT INTO internships VALUES(?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE title=VALUES(title),company=VALUES(company),description=VALUES(description),required_skills=VALUES(required_skills),domain=VALUES(domain),location=VALUES(location),duration_months=VALUES(duration_months),stipend=VALUES(stipend),deadline=VALUES(deadline)";
        try(Connection c=getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,i.getId()); p.setString(2,i.getTitle()); p.setString(3,i.getCompany()); p.setString(4,i.getDescription());
            p.setString(5,i.getRequiredSkillsText()); p.setString(6,i.getDomain()); p.setString(7,i.getLocation());
            p.setInt(8,i.getDurationMonths()); p.setDouble(9,i.getStipend()); p.setDate(10,java.sql.Date.valueOf(i.getDeadline())); p.executeUpdate();
        }
    }
    public static void deleteInternship(String id) throws SQLException {
        try(Connection c=getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM internships WHERE id=?")){p.setString(1,id);p.executeUpdate();}
    }
    public static void saveApplication(Application a) throws SQLException {
        String sql="INSERT INTO applications VALUES(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status)";
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,a.getId());p.setString(2,a.getStudentId());p.setString(3,a.getInternshipId());p.setString(4,a.getStudentName());
            p.setString(5,a.getInternshipTitle());p.setString(6,a.getCompany());p.setDate(7,java.sql.Date.valueOf(a.getAppliedOn()));p.setString(8,a.getStatus().name());p.executeUpdate();
        }
    }
    public static void updateApplicationStatus(String id, ApplicationStatus status) throws SQLException {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE applications SET status=? WHERE id=?")){p.setString(1,status.name());p.setString(2,id);p.executeUpdate();}
    }

    public static void saveResumePath(String studentId, String path) throws SQLException {
        String sql="INSERT INTO resumes(student_id,resume_path) VALUES(?,?) ON DUPLICATE KEY UPDATE resume_path=VALUES(resume_path)";
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,studentId);p.setString(2,path);p.executeUpdate();}
    }

    public static String getResumePath(String studentId) throws SQLException {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT resume_path FROM resumes WHERE student_id=?")){
            p.setString(1,studentId);
            try(ResultSet r=p.executeQuery()){ return r.next() ? r.getString(1) : ""; }
        }
    }

    public static void saveMeeting(Meeting m) throws SQLException {
        String sql="INSERT INTO meetings VALUES(?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE meeting_date=VALUES(meeting_date),mode=VALUES(mode),notes=VALUES(notes),status=VALUES(status)";
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,m.getId()); p.setString(2,m.getApplicationId()); p.setString(3,m.getStudentId()); p.setString(4,m.getStudentName());
            p.setString(5,m.getCompany()); p.setString(6,m.getInternshipTitle()); p.setTimestamp(7,java.sql.Timestamp.valueOf(m.getMeetingDateTime()));
            p.setString(8,m.getMode()); p.setString(9,m.getNotes()); p.setString(10,m.getStatus().name()); p.executeUpdate();
        }
    }

    public static void updateMeetingStatus(String id, MeetingStatus status) throws SQLException {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE meetings SET status=? WHERE id=?")){p.setString(1,status.name());p.setString(2,id);p.executeUpdate();}
    }
    public static void saveAllCurrentData(DataStore store) throws SQLException {
        for(Student x:store.getStudents().getAll()) saveUser(x);
        for(Admin x:store.getAdmins().getAll()) saveUser(x);
        for(Company x:store.getCompanies().getAll()) saveUser(x);
        for(Internship x:store.getInternships().getAll()) saveInternship(x);
        for(Application x:store.getApplications().getAll()) saveApplication(x);
        for(Meeting x:store.getMeetings().getAll()) saveMeeting(x);
    }
    public static void loadInto(DataStore store) throws SQLException {
        try(Connection c=getConnection();Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT * FROM users")){
            while(r.next()) {
                String id=r.getString("id"), name=r.getString("name"), email=r.getString("email"), pass=r.getString("password"), role=r.getString("role");
                try {
                    if("Student".equals(role) && !contains(store.getStudents().getAll(),id)) {
                        Student loaded = new Student(id,name,email,pass,r.getString("college"),r.getString("department"),r.getInt("graduation_year"),r.getString("skills"),r.getString("interests"));
                        try { loaded.setResumePath(getResumePath(id)); } catch(SQLException ignored) { }
                        store.getStudents().add(loaded);
                    }
                    else if("Admin".equals(role) && !contains(store.getAdmins().getAll(),id))
                        store.getAdmins().add(new Admin(id,name,email,pass));
                    else if("Company".equals(role) && !contains(store.getCompanies().getAll(),id))
                        store.getCompanies().add(new Company(id,name,email,pass,r.getString("industry"),r.getString("location"),r.getString("description")));
                } catch(Exception ignored) { }
            }
        }
        try(Connection c=getConnection();Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT * FROM internships")){
            while(r.next()) { String id=r.getString("id"); if(!contains(store.getInternships().getAll(),id)) {
                Internship i=new Internship(id,r.getString("title"),r.getString("company"),r.getString("description"),r.getString("required_skills"),r.getString("domain"),r.getString("location"),r.getInt("duration_months"),r.getDouble("stipend"),r.getDate("deadline").toLocalDate());
                store.getInternships().add(i);
            }}
        } catch(InvalidInputException ex){throw new SQLException(ex);}
        try(Connection c=getConnection();Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT * FROM applications")){
            while(r.next()) { String id=r.getString("id"); if(!contains(store.getApplications().getAll(),id)) {
                Application a=new Application(id,r.getString("student_id"),r.getString("internship_id"),r.getString("student_name"),r.getString("internship_title"),r.getString("company"),r.getDate("applied_on").toLocalDate(),ApplicationStatus.valueOf(r.getString("status")));
                store.getApplications().add(a);
            }}
        } catch(InvalidInputException ex){throw new SQLException(ex);}
        try(Connection c=getConnection();Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT * FROM meetings")){
            while(r.next()) { String id=r.getString("id"); if(!contains(store.getMeetings().getAll(),id)) {
                Meeting m=new Meeting(id,r.getString("application_id"),r.getString("student_id"),r.getString("student_name"),r.getString("company"),r.getString("internship_title"),r.getTimestamp("meeting_date").toLocalDateTime(),r.getString("mode"),r.getString("notes"),MeetingStatus.valueOf(r.getString("status")));
                store.getMeetings().add(m);
            }}
        } catch(InvalidInputException ex){throw new SQLException(ex);}
    }
    private static boolean contains(List<? extends Identifiable> list,String id){return list.stream().anyMatch(x->x.getId().equalsIgnoreCase(id));}
}

// ===== Source from service/AppContext.java =====

/** Wires application services and creates sample in-memory data for the demo. */
class AppContext {
    private final DataStore store = new DataStore();
    private final AuthenticationService auth = new AuthenticationService(store);
    private final InternshipService internships = new InternshipService(store);
    private final ApplicationService applications = new ApplicationService(store);
    private final SkillRecommendationService recommendations = new SkillRecommendationService();
    private final MeetingService meetings = new MeetingService(store);

    public AppContext() {
        seedDemoData();
        try {
            DatabaseManager.saveAllCurrentData(store);
            DatabaseManager.loadInto(store);
        } catch (SQLException ex) {
            throw new IllegalStateException("Database startup failed: " + ex.getMessage(), ex);
        }
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
    public MeetingService getMeetings() { return meetings; }
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
        try {
            store.getApplications().add(app);
            DatabaseManager.saveApplication(app);
        } catch (Exception ex) { throw new ApplicationException("Could not submit application: " + ex.getMessage()); }
        return app;
    }

    public void updateStatus(String applicationId, ApplicationStatus status) throws ApplicationException {
        Application app = store.getApplications().findById(applicationId).orElseThrow(() -> new ApplicationException("Application not found."));
        app.setStatus(status);
        try { DatabaseManager.updateApplicationStatus(applicationId, status); }
        catch (SQLException ex) { throw new ApplicationException("Could not update database: " + ex.getMessage()); }
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

// ===== Interview / Meeting service =====

class MeetingService {
    private final DataStore store;
    public MeetingService(DataStore store) { this.store = store; }

    public Meeting schedule(Application application, Company company, LocalDateTime dateTime, String mode, String notes) throws ApplicationException {
        if (application == null || company == null) throw new ApplicationException("Application and company are required.");
        if (!company.getName().equalsIgnoreCase(application.getCompany())) throw new ApplicationException("You can only manage your company's applications.");
        if (application.getStatus() != ApplicationStatus.ACCEPTED) throw new ApplicationException("Accept the application before scheduling an interview.");
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) throw new ApplicationException("Choose a future interview date and time.");
        Meeting m = new Meeting("MTG-" + UUID.randomUUID().toString().substring(0,8).toUpperCase(), application.getId(), application.getStudentId(), application.getStudentName(), application.getCompany(), application.getInternshipTitle(), dateTime, mode, notes, MeetingStatus.SCHEDULED);
        try { store.getMeetings().add(m); DatabaseManager.saveMeeting(m); return m; }
        catch(Exception ex) { throw new ApplicationException("Could not schedule meeting: " + ex.getMessage()); }
    }

    public List<Meeting> getForStudent(Student student) {
        if (student == null) return List.of();
        return store.getMeetings().getAll().stream().filter(m -> m.getStudentId().equals(student.getId())).sorted(Comparator.comparing(Meeting::getMeetingDateTime)).collect(Collectors.toList());
    }

    public List<Meeting> getForCompany(Company company) {
        if (company == null) return List.of();
        return store.getMeetings().getAll().stream().filter(m -> m.getCompany().equalsIgnoreCase(company.getName())).sorted(Comparator.comparing(Meeting::getMeetingDateTime)).collect(Collectors.toList());
    }

    public void updateStatus(String meetingId, MeetingStatus status) throws ApplicationException {
        Meeting m = store.getMeetings().findById(meetingId).orElseThrow(() -> new ApplicationException("Meeting not found."));
        m.setStatus(status);
        try { DatabaseManager.updateMeetingStatus(meetingId, status); } catch(SQLException ex) { throw new ApplicationException("Could not update meeting: " + ex.getMessage()); }
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
        for (Company company : store.getCompanies().getAll()) {
            if (company.getEmail().equalsIgnoreCase(cleanEmail) && company.passwordMatches(password)) return company;
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
            DatabaseManager.saveUser(student);
            return student;
        } catch (IllegalArgumentException | SQLException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }


    public Admin registerAdmin(String name, String email, String password) throws InvalidInputException {
        try {
            ensureUniqueEmail(email);
            Admin admin = new Admin("ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), name, email, password);
            store.getAdmins().add(admin);
            DatabaseManager.saveUser(admin);
            return admin;
        } catch (IllegalArgumentException | SQLException ex) { throw new InvalidInputException(ex.getMessage()); }
    }


    public Company registerCompany(String name, String email, String password, String industry, String location, String description) throws InvalidInputException {
        try {
            ensureUniqueEmail(email);
            Company company = new Company("CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), name, email, password, industry, location, description);
            store.getCompanies().add(company);
            DatabaseManager.saveUser(company);
            return company;
        } catch (IllegalArgumentException | SQLException ex) { throw new InvalidInputException(ex.getMessage()); }
    }
    private void ensureUniqueEmail(String email) throws InvalidInputException {
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new InvalidInputException("Enter a valid email address.");
        String clean = email.trim();
        boolean duplicate = store.getStudents().getAll().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(clean))
                || store.getAdmins().getAll().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(clean))
                || store.getCompanies().getAll().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(clean));
        if (duplicate) throw new InvalidInputException("An account with this email already exists.");
    }
}
// ===== Source from service/DataStore.java =====

/** In-memory data store built from Java Collections; no SQL or persistence is used. */
class DataStore {
    private final GenericRepository<Student> students = new GenericRepository<>();
    private final GenericRepository<Admin> admins = new GenericRepository<>();
    private final GenericRepository<Company> companies = new GenericRepository<>();
    private final GenericRepository<Internship> internships = new GenericRepository<>();
    private final GenericRepository<Application> applications = new GenericRepository<>();
    private final GenericRepository<Meeting> meetings = new GenericRepository<>();
    public GenericRepository<Student> getStudents() { return students; }
    public GenericRepository<Admin> getAdmins() { return admins; }
    public GenericRepository<Company> getCompanies() { return companies; }
    public GenericRepository<Internship> getInternships() { return internships; }
    public GenericRepository<Application> getApplications() { return applications; }
    public GenericRepository<Meeting> getMeetings() { return meetings; }
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
            DatabaseManager.saveInternship(item);
            return item;
        } catch (IllegalArgumentException | SQLException ex) { throw new InvalidInputException(ex.getMessage()); }
    }

    public void updateInternship(String id, String title, String company, String description, String requiredSkills,
                                 String domain, String location, int durationMonths, double stipend, LocalDate deadline)
            throws InvalidInputException {
        Internship item = store.getInternships().findById(id).orElseThrow(() -> new InvalidInputException("Internship not found."));
        try {
            item.updateDetails(title, company, description, requiredSkills, domain, location, durationMonths, stipend, deadline);
            DatabaseManager.saveInternship(item);
        } catch (IllegalArgumentException | SQLException ex) { throw new InvalidInputException(ex.getMessage()); }
    }

    public void deleteInternship(String id) throws InvalidInputException {
        if (!store.getInternships().remove(id)) throw new InvalidInputException("Internship not found.");
        try { DatabaseManager.deleteInternship(id); }
        catch (SQLException ex) { throw new InvalidInputException("Could not delete from database: " + ex.getMessage()); }
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
        JButton registerAdmin = new JButton("Register Admin");
        JButton registerCompany = new JButton("Register Company");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        form.add(login, gbc);
        gbc.gridy = 3;
        form.add(register, gbc);
        gbc.gridy = 4; form.add(registerAdmin, gbc);
        gbc.gridy = 5; form.add(registerCompany, gbc);
        root.add(form, BorderLayout.CENTER);

        JLabel demo = new JLabel("Demo student: rahul@student.com / pass123    |    Admin: admin@smart.com / admin123");
        demo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        demo.setForeground(new Color(80, 88, 100));
        root.add(demo, BorderLayout.SOUTH);
        setContentPane(root);

        login.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegistrationDialog(this, context, emailField).setVisible(true));
        registerAdmin.addActionListener(e -> new AdminRegistrationDialog(this, context, emailField).setVisible(true));
        registerCompany.addActionListener(e -> new CompanyRegistrationDialog(this, context, emailField).setVisible(true));
    }

    private void doLogin() {
        try {
            User user = context.getAuth().login(emailField.getText(), new String(passwordField.getPassword()));
            dispose();
            if (user instanceof Student student) new StudentDashboardFrame(context, student).setVisible(true);
            else if (user instanceof Admin admin) new AdminDashboardFrame(context, admin).setVisible(true);
            else if (user instanceof Company company) new CompanyDashboardFrame(context, company).setVisible(true);
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

// ===== Admin and Company Registration dialogs =====
final class AdminRegistrationDialog extends JDialog {
    private final transient AppContext context; private final JTextField name = new JTextField(20), email = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20); private final JTextField loginEmail;
    AdminRegistrationDialog(Frame owner, AppContext context, JTextField loginEmail) {
        super(owner, "Admin Registration", true); this.context = context; this.loginEmail = loginEmail; setSize(420, 260); setLocationRelativeTo(owner); build();
    }
    private void build() {
        JPanel form = new JPanel(new GridLayout(0,2,8,8)); form.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        form.add(new JLabel("Admin name")); form.add(name); form.add(new JLabel("Email")); form.add(email); form.add(new JLabel("Password (min 6)")); form.add(password);
        JButton create = UiStyle.button("Register Admin"), cancel = new JButton("Cancel"); JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(cancel); actions.add(create);
        setLayout(new BorderLayout()); add(form,BorderLayout.CENTER); add(actions,BorderLayout.SOUTH); cancel.addActionListener(e -> dispose()); create.addActionListener(e -> {
            try { Admin a = context.getAuth().registerAdmin(name.getText(),email.getText(),new String(password.getPassword())); loginEmail.setText(a.getEmail()); JOptionPane.showMessageDialog(this,"Admin registered. You can log in now."); dispose(); }
            catch (InvalidInputException | IllegalArgumentException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Registration failed",JOptionPane.WARNING_MESSAGE); }
        });
    }
}
final class CompanyRegistrationDialog extends JDialog {
    private final transient AppContext context; private final JTextField name = new JTextField(20), email = new JTextField(20), industry = new JTextField(20), location = new JTextField(20);
    private final JTextField description = new JTextField(20); private final JPasswordField password = new JPasswordField(20); private final JTextField loginEmail;
    CompanyRegistrationDialog(Frame owner, AppContext context, JTextField loginEmail) { super(owner,"Company Registration",true); this.context=context; this.loginEmail=loginEmail; setSize(500,360); setLocationRelativeTo(owner); build(); }
    private void build() {
        JPanel form = new JPanel(new GridLayout(0,2,8,8)); form.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        addRow(form,"Company name",name); addRow(form,"Email",email); addRow(form,"Password (min 6)",password); addRow(form,"Industry",industry); addRow(form,"Location",location); addRow(form,"Description",description);
        JButton create=UiStyle.button("Register Company"), cancel=new JButton("Cancel"); JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(cancel); actions.add(create);
        setLayout(new BorderLayout()); add(form,BorderLayout.CENTER); add(actions,BorderLayout.SOUTH); cancel.addActionListener(e->dispose()); create.addActionListener(e->{
            try { Company c=context.getAuth().registerCompany(name.getText(),email.getText(),new String(password.getPassword()),industry.getText(),location.getText(),description.getText()); loginEmail.setText(c.getEmail()); JOptionPane.showMessageDialog(this,"Company registered. Log in to post internships."); dispose(); }
            catch(InvalidInputException | IllegalArgumentException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Registration failed",JOptionPane.WARNING_MESSAGE);}
        });
    }
    private void addRow(JPanel p,String label,Component c){p.add(new JLabel(label));p.add(c);}
}

final class CompanyDashboardFrame extends JFrame {
    private final transient AppContext context; private final transient Company company;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Internship","Domain","Location","Skills","Stipend","Deadline"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final DefaultTableModel applicationModel = new DefaultTableModel(new Object[]{"Application ID","Student","Internship","Applied","Status","Resume"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final DefaultTableModel meetingModel = new DefaultTableModel(new Object[]{"Meeting ID","Student","Internship","Date & Time","Mode","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table = new JTable(model), applicationTable = new JTable(applicationModel), meetingTable = new JTable(meetingModel);
    CompanyDashboardFrame(AppContext context, Company company) { this.context=context; this.company=company; setTitle("Smart Internship Opportunities | Company Dashboard"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(1100,700); setLocationRelativeTo(null); build(); refresh(); refreshApplications(); refreshMeetings(); }
    private void build(){
        JPanel root=new JPanel(new BorderLayout(8,8)); root.setBackground(UiStyle.PALE); JPanel header=new JPanel(new BorderLayout()); header.setBackground(UiStyle.NAVY); header.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        JLabel title=new JLabel("COMPANY DASHBOARD | "+company.getName()); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif",Font.BOLD,17)); JButton logout=new JButton("Logout"); logout.addActionListener(e->{dispose();new LoginFrame(context).setVisible(true);}); header.add(title,BorderLayout.CENTER); header.add(logout,BorderLayout.EAST);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("My Internships", buildInternshipTab());
        tabs.addTab("Applicant Pipeline", buildApplicantTab());
        tabs.addTab("Interview Calendar", buildMeetingTab());
        root.add(header,BorderLayout.NORTH); root.add(tabs,BorderLayout.CENTER); root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8)); setContentPane(root);
    }
    private JPanel buildInternshipTab(){
        JPanel panel=new JPanel(new BorderLayout(8,8)); UiStyle.pad(panel); panel.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton post=UiStyle.button("Post Internship"), refresh=new JButton("Refresh"); actions.add(refresh); actions.add(post); panel.add(actions,BorderLayout.SOUTH); post.addActionListener(e->postInternship()); refresh.addActionListener(e->refresh()); return panel;
    }
    private JPanel buildApplicantTab(){
        JPanel panel=new JPanel(new BorderLayout(8,8)); UiStyle.pad(panel);
        JLabel info=new JLabel("Select an applicant: Accept / Reject, view resume, or schedule an interview.");
        panel.add(info,BorderLayout.NORTH); panel.add(new JScrollPane(applicationTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton accept=UiStyle.button("Accept"), reject=new JButton("Reject"), resume=new JButton("View Resume"), schedule=UiStyle.button("Schedule Interview"), refresh=new JButton("Refresh");
        actions.add(refresh); actions.add(resume); actions.add(accept); actions.add(reject); actions.add(schedule); panel.add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->refreshApplications()); accept.addActionListener(e->changeSelectedApplication(ApplicationStatus.ACCEPTED)); reject.addActionListener(e->changeSelectedApplication(ApplicationStatus.REJECTED)); resume.addActionListener(e->viewSelectedResume()); schedule.addActionListener(e->scheduleSelectedInterview());
        return panel;
    }
    private JPanel buildMeetingTab(){
        JPanel panel=new JPanel(new BorderLayout(8,8)); UiStyle.pad(panel); panel.add(new JScrollPane(meetingTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton refresh=new JButton("Refresh Calendar"), cancel=new JButton("Cancel Meeting"); actions.add(refresh); actions.add(cancel); panel.add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->refreshMeetings()); cancel.addActionListener(e->cancelSelectedMeeting()); return panel;
    }
    private void postInternship(){
        JTextField title=new JTextField(), description=new JTextField(), skills=new JTextField(), domain=new JTextField(company.getIndustry()), location=new JTextField(company.getLocation()), months=new JTextField("3"), stipend=new JTextField("0"), deadline=new JTextField(LocalDate.now().plusDays(30).toString());
        JPanel form=new JPanel(new GridLayout(0,2,7,7)); addRow(form,"Title",title); addRow(form,"Description",description); addRow(form,"Required skills (comma separated)",skills); addRow(form,"Domain",domain); addRow(form,"Location",location); addRow(form,"Duration (months)",months); addRow(form,"Stipend",stipend); addRow(form,"Deadline (YYYY-MM-DD)",deadline);
        int choice=JOptionPane.showConfirmDialog(this,form,"Post internship - "+company.getName(),JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE); if(choice!=JOptionPane.OK_OPTION)return;
        try { Internship created=context.getInternships().addInternship(title.getText(),company.getName(),description.getText(),skills.getText(),domain.getText(),location.getText(),Integer.parseInt(months.getText().trim()),Double.parseDouble(stipend.getText().trim()),LocalDate.parse(deadline.getText().trim())); JOptionPane.showMessageDialog(this,"Internship posted: "+created.getId()); refresh(); }
        catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Duration and stipend must be numeric.","Invalid input",JOptionPane.WARNING_MESSAGE);}
        catch(DateTimeParseException ex){JOptionPane.showMessageDialog(this,"Enter deadline in YYYY-MM-DD format.","Invalid date",JOptionPane.WARNING_MESSAGE);}
        catch(InvalidInputException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Could not post",JOptionPane.WARNING_MESSAGE);}
    }
    private void refresh(){model.setRowCount(0); for(Internship i:context.getInternships().getAll()) if(i.getCompany().equalsIgnoreCase(company.getName())) model.addRow(new Object[]{i.getId(),i.getTitle(),i.getDomain(),i.getLocation(),i.getRequiredSkillsText(),i.getStipend(),i.getDeadline()});}
    private void refreshApplications(){ applicationModel.setRowCount(0); for(Application a:context.getApplications().getAll()) if(a.getCompany().equalsIgnoreCase(company.getName())) { String resume="Not uploaded"; try { String path=DatabaseManager.getResumePath(a.getStudentId()); if(path!=null && !path.isBlank()) resume=new File(path).getName(); } catch(SQLException ignored){} applicationModel.addRow(new Object[]{a.getId(),a.getStudentName(),a.getInternshipTitle(),a.getAppliedOn(),a.getStatus(),resume}); } }
    private void refreshMeetings(){ meetingModel.setRowCount(0); for(Meeting m:context.getMeetings().getForCompany(company)) meetingModel.addRow(new Object[]{m.getId(),m.getStudentName(),m.getInternshipTitle(),m.getMeetingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),m.getMode(),m.getStatus()}); }
    private Application getSelectedApplication(){ int row=applicationTable.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select an application first.");return null;} String id=String.valueOf(applicationModel.getValueAt(applicationTable.convertRowIndexToModel(row),0)); return context.getApplications().getAll().stream().filter(a->a.getId().equals(id)).findFirst().orElse(null); }
    private void changeSelectedApplication(ApplicationStatus status){ Application a=getSelectedApplication(); if(a==null)return; if(!a.getCompany().equalsIgnoreCase(company.getName())){JOptionPane.showMessageDialog(this,"You can only manage your own applicants.");return;} try{context.getApplications().updateStatus(a.getId(),status); refreshApplications(); JOptionPane.showMessageDialog(this,"Application marked as "+status+".");}catch(ApplicationException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Update failed",JOptionPane.WARNING_MESSAGE);} }
    private void viewSelectedResume(){ Application a=getSelectedApplication(); if(a==null)return; try{String path=DatabaseManager.getResumePath(a.getStudentId()); if(path==null||path.isBlank()){JOptionPane.showMessageDialog(this,"This student has not uploaded a resume yet.");return;} File f=new File(path); if(!f.exists()){JOptionPane.showMessageDialog(this,"Resume file is missing: "+path);return;} if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(f); else JOptionPane.showMessageDialog(this,"Resume location: "+f.getAbsolutePath());}catch(Exception ex){JOptionPane.showMessageDialog(this,"Could not open resume: "+ex.getMessage(),"Resume error",JOptionPane.ERROR_MESSAGE);} }
    private void scheduleSelectedInterview(){ Application a=getSelectedApplication(); if(a==null)return; if(a.getStatus()!=ApplicationStatus.ACCEPTED){JOptionPane.showMessageDialog(this,"Accept the applicant first, then schedule an interview.");return;} JTextField date=new JTextField(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))); JComboBox<String> mode=new JComboBox<>(new String[]{"Online","Phone","In-person"}); JTextField notes=new JTextField("Interview for internship application"); JPanel form=new JPanel(new GridLayout(0,2,7,7)); addRow(form,"Date & time (yyyy-MM-dd HH:mm)",date); addRow(form,"Mode",mode); addRow(form,"Notes",notes); int choice=JOptionPane.showConfirmDialog(this,form,"Schedule interview - "+a.getStudentName(),JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE); if(choice!=JOptionPane.OK_OPTION)return; try{LocalDateTime dt=LocalDateTime.parse(date.getText().trim(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); Meeting m=context.getMeetings().schedule(a,company,dt,String.valueOf(mode.getSelectedItem()),notes.getText()); refreshMeetings(); JOptionPane.showMessageDialog(this,"Interview scheduled for "+m.getMeetingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))+".");}catch(DateTimeParseException ex){JOptionPane.showMessageDialog(this,"Use yyyy-MM-dd HH:mm, for example 2026-12-05 14:30.","Invalid date/time",JOptionPane.WARNING_MESSAGE);}catch(ApplicationException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Could not schedule",JOptionPane.WARNING_MESSAGE);} }
    private void cancelSelectedMeeting(){ int row=meetingTable.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a meeting first.");return;} String id=String.valueOf(meetingModel.getValueAt(meetingTable.convertRowIndexToModel(row),0)); try{context.getMeetings().updateStatus(id,MeetingStatus.CANCELLED);refreshMeetings();}catch(ApplicationException ex){JOptionPane.showMessageDialog(this,ex.getMessage());} }
    private void addRow(JPanel p,String label,Component c){p.add(new JLabel(label));p.add(c);}
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
        tabs.addTab("Interview Schedule", buildStudentMeetingsPanel());
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
        JLabel resumeLabel = new JLabel(student.getResumePath().isBlank() ? "No resume uploaded" : new File(student.getResumePath()).getName());
        JButton uploadResume = new JButton("Upload / Replace Resume");
        c.gridx = 0; c.gridy = row; c.weightx = 0; panel.add(new JLabel("Resume"), c); c.gridx = 1; c.weightx = 1; JPanel resumePanel = new JPanel(new BorderLayout(8,0)); resumePanel.add(resumeLabel, BorderLayout.CENTER); resumePanel.add(uploadResume, BorderLayout.EAST); panel.add(resumePanel, c); row++;
        uploadResume.addActionListener(e -> uploadResume(resumeLabel));
        JButton save = UiStyle.button("Save Profile"); c.gridx = 1; c.gridy = row; c.weightx = 1; panel.add(save, c);
        save.addActionListener(e -> {
            try {
                student.setName(name.getText()); student.setCollege(college.getText()); student.setDepartment(department.getText());
                student.setGraduationYear(Integer.parseInt(year.getText().trim())); student.setSkillsFromText(skills.getText()); student.setInterests(interests.getText());
                DatabaseManager.saveUser(student);
                JOptionPane.showMessageDialog(this, "Profile updated and saved.");
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Graduation year must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE); }
            catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid profile", JOptionPane.WARNING_MESSAGE); }
            catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Could not save profile to MySQL: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE); }
        });
        return panel;
    }

    private void uploadResume(JLabel resumeLabel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select your resume (PDF/DOC/DOCX)");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File source = chooser.getSelectedFile();
        String lower = source.getName().toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            JOptionPane.showMessageDialog(this, "Please select a PDF, DOC, or DOCX resume.", "Invalid file", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            Path folder = Paths.get("resumes"); Files.createDirectories(folder);
            String extension = source.getName().substring(source.getName().lastIndexOf('.'));
            Path target = folder.resolve(student.getId() + "_resume" + extension);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            student.setResumePath(target.toAbsolutePath().toString());
            DatabaseManager.saveResumePath(student.getId(), student.getResumePath());
            resumeLabel.setText(source.getName());
            JOptionPane.showMessageDialog(this, "Resume uploaded successfully. Companies can now view it when reviewing your application.");
        } catch(IOException | SQLException ex) { JOptionPane.showMessageDialog(this, "Could not upload resume: " + ex.getMessage(), "Upload failed", JOptionPane.ERROR_MESSAGE); }
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
        JButton apply = UiStyle.button("Apply for Selected Match"); JButton plan = new JButton("Build Skill-Gap Action Plan"); JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.add(plan); bottom.add(apply); panel.add(bottom, BorderLayout.SOUTH);
        calculate.addActionListener(e -> calculateMatchesInBackground()); apply.addActionListener(e -> applySelectedMatch()); plan.addActionListener(e -> showSkillGapPlan());
        return panel;
    }

    private JPanel buildApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); UiStyle.pad(panel);
        JButton refresh = new JButton("Refresh Applications"); JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)); top.add(refresh);
        panel.add(top, BorderLayout.NORTH); panel.add(new JScrollPane(applicationTable), BorderLayout.CENTER);
        refresh.addActionListener(e -> refreshApplications()); return panel;
    }

    private JPanel buildStudentMeetingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8)); UiStyle.pad(panel);
        DefaultTableModel meetingModel = model("Meeting ID","Internship","Company","Date & Time","Mode","Notes","Status");
        JTable meetingTable = new JTable(meetingModel);
        JButton refresh = new JButton("Refresh Schedule"); JButton accept = UiStyle.button("Accept Meeting"); JButton decline = new JButton("Decline Meeting");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(refresh); actions.add(accept); actions.add(decline);
        panel.add(new JScrollPane(meetingTable), BorderLayout.CENTER); panel.add(actions, BorderLayout.SOUTH);
        Runnable load = () -> { meetingModel.setRowCount(0); for(Meeting m:context.getMeetings().getForStudent(student)) meetingModel.addRow(new Object[]{m.getId(),m.getInternshipTitle(),m.getCompany(),m.getMeetingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),m.getMode(),m.getNotes(),m.getStatus()}); };
        refresh.addActionListener(e -> load.run());
        accept.addActionListener(e -> updateSelectedMeeting(meetingTable, meetingModel, MeetingStatus.ACCEPTED, load));
        decline.addActionListener(e -> updateSelectedMeeting(meetingTable, meetingModel, MeetingStatus.DECLINED, load));
        load.run(); return panel;
    }

    private void updateSelectedMeeting(JTable table, DefaultTableModel model, MeetingStatus status, Runnable load) {
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a meeting first.");return;}
        String id=String.valueOf(model.getValueAt(table.convertRowIndexToModel(row),0));
        try { context.getMeetings().updateStatus(id,status); load.run(); JOptionPane.showMessageDialog(this,"Meeting status updated to " + status + "."); }
        catch(ApplicationException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Update failed",JOptionPane.WARNING_MESSAGE);}
    }

    private void showSkillGapPlan() {
        int row = matchTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a smart match first."); return; }
        MatchResult result = shownMatches.get(matchTable.convertRowIndexToModel(row));
        StringBuilder plan = new StringBuilder();
        plan.append("WHY THIS INTERNSHIP FITS YOU\n\n");
        plan.append("Match score: ").append(String.format("%.1f%%", result.getPercentage())).append("\n");
        plan.append("Matched skills: ").append(result.getMatchedSkills().isEmpty() ? "None" : String.join(", ", result.getMatchedSkills())).append("\n");
        plan.append("Missing skills: ").append(result.getMissingSkills().isEmpty() ? "None - you already cover the listed skills." : String.join(", ", result.getMissingSkills())).append("\n\n");
        if (!result.getMissingSkills().isEmpty()) {
            plan.append("PERSONAL SKILL-GAP ACTION PLAN\n");
            int step=1;
            for(String skill:result.getMissingSkills()) {
                plan.append(step++).append(". Learn ").append(skill).append(" fundamentals and practice 2-3 small problems.\n");
                plan.append("   Build one mini-project using ").append(skill).append(" and add it to your portfolio.\n");
            }
            plan.append("\nTip: update your profile after gaining a skill and run Smart Matches again.");
        } else {
            plan.append("You have all the required skills. Focus on a strong resume and interview preparation.");
        }
        JTextArea area=new JTextArea(plan.toString(),16,58); area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true); area.setFont(new Font("SansSerif",Font.PLAIN,13));
        JOptionPane.showMessageDialog(this,new JScrollPane(area),"Smart Skill-Gap Coach",JOptionPane.INFORMATION_MESSAGE);
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
