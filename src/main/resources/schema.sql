-- Database Schema for Student Result Management System
CREATE DATABASE IF NOT EXISTS student_result_db;
USE student_result_db;

-- 1. Users Table (System Admins / Staff)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    course VARCHAR(50) NOT NULL,
    semester INT NOT NULL,
    gender VARCHAR(10),
    dob DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Subjects Table
CREATE TABLE IF NOT EXISTS subjects (
    subject_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    max_marks INT DEFAULT 100,
    pass_marks INT DEFAULT 35
);

-- 4. Results Table
CREATE TABLE IF NOT EXISTS results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    marks_obtained DECIMAL(5, 2) NOT NULL,
    grade VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_result_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_subject UNIQUE (student_id, subject_id)
);

-- Default Admin User (username: admin, password: admin123)
INSERT IGNORE INTO users (username, password, full_name, role)
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN');

