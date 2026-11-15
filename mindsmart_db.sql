-- ==============================================================
-- MindSmart – Smart Internship Opportunities Platform
-- MySQL Database Setup Script
-- Database Name: mindsmart_db
-- ==============================================================

CREATE DATABASE IF NOT EXISTS mindsmart_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mindsmart_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL, -- 'ROLE_STUDENT', 'ROLE_COMPANY', 'ROLE_ADMIN'
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Student Profiles
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    college VARCHAR(200),
    degree VARCHAR(100),
    department VARCHAR(100),
    graduation_year INT,
    bio TEXT,
    avatar_url VARCHAR(255),
    career_readiness_score INT DEFAULT 35,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Company Profiles
CREATE TABLE IF NOT EXISTS company_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(150) NOT NULL,
    recruiter_name VARCHAR(100),
    website VARCHAR(200),
    industry VARCHAR(100),
    location VARCHAR(120),
    description TEXT,
    logo_url VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Skills Catalog
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    category VARCHAR(80) DEFAULT 'General'
);

-- 5. Student Skills (Many-to-Many)
CREATE TABLE IF NOT EXISTS student_skills (
    student_profile_id BIGINT NOT NULL,
    skill_name VARCHAR(80) NOT NULL,
    PRIMARY KEY (student_profile_id, skill_name),
    FOREIGN KEY (student_profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE
);

-- 6. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    extracted_text LONGTEXT,
    detected_skills TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. Internships Table
CREATE TABLE IF NOT EXISTS internships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(100) NOT NULL,
    work_mode VARCHAR(50) NOT NULL, -- 'Remote', 'Hybrid', 'On-site'
    location VARCHAR(120) NOT NULL,
    duration VARCHAR(60) NOT NULL,
    stipend INT NOT NULL DEFAULT 0,
    openings INT DEFAULT 1,
    description LONGTEXT NOT NULL,
    eligibility TEXT,
    deadline DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT TRUE,
    is_reported BOOLEAN DEFAULT FALSE,
    report_reason VARCHAR(255),
    views_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company_profiles(id) ON DELETE CASCADE
);

-- 8. Internship Required Skills
CREATE TABLE IF NOT EXISTS internship_skills (
    internship_id BIGINT NOT NULL,
    skill_name VARCHAR(80) NOT NULL,
    PRIMARY KEY (internship_id, skill_name),
    FOREIGN KEY (internship_id) REFERENCES internships(id) ON DELETE CASCADE
);

-- 9. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    internship_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_id BIGINT,
    match_percentage INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'REVIEWING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'ACCEPTED', 'REJECTED'
    cover_letter TEXT,
    interview_date VARCHAR(50),
    interview_link VARCHAR(255),
    interview_notes TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (internship_id) REFERENCES internships(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    UNIQUE KEY unique_internship_student (internship_id, student_id)
);

-- 10. Saved Internships (Bookmarks)
CREATE TABLE IF NOT EXISTS saved_internships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (internship_id) REFERENCES internships(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_saved_internship (user_id, internship_id)
);

-- 11. In-App Notifications
CREATE TABLE IF NOT EXISTS in_app_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'INFO', -- 'INFO', 'SUCCESS', 'WARNING', 'STATUS_CHANGE', 'INTERVIEW'
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. Email Logs (Audit / Simulator)
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body LONGTEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'SENT', -- 'SENT', 'SIMULATED_DEMO', 'FAILED'
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
