-- Drop tables if they exist to allow clean recreations
DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS student_coding_status;
DROP TABLE IF EXISTS user_coding_profiles;
DROP TABLE IF EXISTS coding_questions;
DROP TABLE IF EXISTS results;
DROP TABLE IF EXISTS test_assigned_students;
DROP TABLE IF EXISTS test_questions;
DROP TABLE IF EXISTS options;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS tests;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS users;

-- Users Table (base user table for JOINED inheritance)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    batch VARCHAR(255) NULL
);

-- Faculty Table (extends users)
CREATE TABLE faculty (
    id BIGINT PRIMARY KEY,
    department VARCHAR(255),
    specialization VARCHAR(255),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- Questions Table
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    topic VARCHAR(255),
    difficulty VARCHAR(50),
    marks INT NOT NULL DEFAULT 1,
    faculty_id BIGINT NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

-- Options Table
CREATE TABLE options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    question_id BIGINT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Tests Table
CREATE TABLE tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT NOT NULL DEFAULT 60,
    start_time DATETIME,
    end_time DATETIME,
    total_marks INT NOT NULL DEFAULT 100,
    faculty_id BIGINT NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

-- Test Questions Join Table (ManyToMany)
CREATE TABLE test_questions (
    test_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    PRIMARY KEY (test_id, question_id),
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Test Assigned Students Join Table (ManyToMany for targeted scheduling)
CREATE TABLE test_assigned_students (
    test_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (test_id, student_id),
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Results Table
CREATE TABLE results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    score INT NOT NULL,
    percentage DOUBLE NOT NULL,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

-- Coding Questions Table
CREATE TABLE coding_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    problem_link VARCHAR(500) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    topic VARCHAR(255),
    subject VARCHAR(255) NOT NULL,
    score INT NOT NULL DEFAULT 10,
    deadline DATETIME NULL,
    faculty_id BIGINT NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

-- User Coding Profiles Table
CREATE TABLE user_coding_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    leetcode_username VARCHAR(255) NULL,
    hackerrank_username VARCHAR(255) NULL,
    codechef_username VARCHAR(255) NULL,
    connected_at DATETIME NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Student Coding Status Table
CREATE TABLE student_coding_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    coding_question_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    submission_date DATETIME NULL,
    score INT NOT NULL DEFAULT 0,
    UNIQUE KEY unique_student_question (student_id, coding_question_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coding_question_id) REFERENCES coding_questions(id) ON DELETE CASCADE
);

-- User Achievements Table
CREATE TABLE user_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    badge_name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    awarded_at DATETIME NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed default Administrator account
INSERT INTO users (id, name, email, password, role, active, batch)
VALUES (1, 'System Admin', 'admin@examcloud.com', 'admin123', 'ADMIN', TRUE, NULL)
ON DUPLICATE KEY UPDATE name='System Admin';

-- Seed default Faculty Instructor account
INSERT INTO users (id, name, email, password, role, active, batch)
VALUES (2, 'Dr. Charles Xavier', 'faculty@examcloud.com', 'faculty123', 'FACULTY', TRUE, NULL)
ON DUPLICATE KEY UPDATE name='Dr. Charles Xavier';

INSERT INTO faculty (id, department, specialization)
VALUES (2, 'Computer Science', 'Software Architecture')
ON DUPLICATE KEY UPDATE department='Computer Science';

-- Seed default Student Scholar account
INSERT INTO users (id, name, email, password, role, active, batch)
VALUES (3, 'Peter Parker', 'student@examcloud.com', 'student123', 'STUDENT', TRUE, 'Batch A')
ON DUPLICATE KEY UPDATE name='Peter Parker';

-- Seed default Coding Questions for Java, DSA, and Python to showcase beautiful dashboard immediately
INSERT INTO coding_questions (id, title, problem_link, difficulty, platform, topic, subject, score, deadline, faculty_id) VALUES
(1, 'Two Sum', 'https://leetcode.com/problems/two-sum/', 'Easy', 'LeetCode', 'Arrays & Hashing', 'DSA', 10, '2026-06-01 23:59:59', 2),
(2, 'Reverse String', 'https://leetcode.com/problems/reverse-string/', 'Easy', 'LeetCode', 'Two Pointers', 'Java', 10, '2026-06-05 23:59:59', 2),
(3, 'Java Anagrams', 'https://www.hackerrank.com/challenges/java-anagrams/problem', 'Easy', 'HackerRank', 'Strings', 'Java', 10, '2026-06-10 23:59:59', 2),
(4, 'Valid Parentheses', 'https://leetcode.com/problems/valid-parentheses/', 'Medium', 'LeetCode', 'Stacks', 'DSA', 20, '2026-06-15 23:59:59', 2),
(5, 'Longest Substring Without Repeating Characters', 'https://leetcode.com/problems/longest-substring-without-repeating-characters/', 'Medium', 'LeetCode', 'Sliding Window', 'JavaScript', 20, '2026-06-20 23:59:59', 2),
(6, 'Merge k Sorted Lists', 'https://leetcode.com/problems/merge-k-sorted-lists/', 'Hard', 'LeetCode', 'Divide & Conquer', 'DSA', 30, '2026-06-25 23:59:59', 2),
(7, 'Rotate Array', 'https://leetcode.com/problems/rotate-array/', 'Medium', 'LeetCode', 'Arrays', 'Python', 20, '2026-06-30 23:59:59', 2),
(8, 'Basic Regex Parser', 'https://www.codechef.com/problems/REGEX', 'Hard', 'CodeChef', 'Dynamic Programming', 'Python', 30, '2026-07-05 23:59:59', 2);