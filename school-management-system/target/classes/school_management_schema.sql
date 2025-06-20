
-- School Management System Database Schema

-- Create database
CREATE DATABASE IF NOT EXISTS school_management_system;
USE school_management_system;

-- Students table
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    date_of_birth DATE,
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade_level INT,
    student_status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Teachers/Faculty table
CREATE TABLE teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    hire_date DATE,
    department VARCHAR(50),
    teacher_status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    credits INT DEFAULT 3,
    max_capacity INT DEFAULT 30,
    teacher_id INT,
    semester VARCHAR(20),
    academic_year VARCHAR(10),
    course_status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

-- Student-Course Enrollment table (many-to-many relationship)
CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade VARCHAR(5),
    enrollment_status ENUM('ENROLLED', 'DROPPED', 'COMPLETED') DEFAULT 'ENROLLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, course_id)
);

-- Time slots for scheduling
CREATE TABLE time_slots (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration INT, -- in minutes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Course Schedule table
CREATE TABLE course_schedules (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    slot_id INT NOT NULL,
    classroom VARCHAR(20),
    effective_date DATE,
    end_date DATE,
    schedule_status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id) ON DELETE CASCADE
);

-- Attendance table
CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'EXCUSED') DEFAULT 'PRESENT',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (student_id, course_id, attendance_date)
);

-- Sample data insertions
INSERT INTO teachers (first_name, last_name, email, phone, hire_date, department) VALUES
('John', 'Smith', 'john.smith@school.edu', '555-0101', '2020-08-15', 'Mathematics'),
('Sarah', 'Johnson', 'sarah.johnson@school.edu', '555-0102', '2019-09-01', 'Science'),
('Michael', 'Brown', 'michael.brown@school.edu', '555-0103', '2021-01-10', 'English'),
('Emily', 'Davis', 'emily.davis@school.edu', '555-0104', '2020-03-20', 'History'),
('David', 'Wilson', 'david.wilson@school.edu', '555-0105', '2018-07-12', 'Computer Science');

INSERT INTO students (first_name, last_name, email, phone, date_of_birth, grade_level) VALUES
('Alice', 'Anderson', 'alice.anderson@student.edu', '555-1001', '2005-03-15', 10),
('Bob', 'Baker', 'bob.baker@student.edu', '555-1002', '2004-07-22', 11),
('Carol', 'Clark', 'carol.clark@student.edu', '555-1003', '2005-11-08', 10),
('Daniel', 'Davis', 'daniel.davis@student.edu', '555-1004', '2003-12-03', 12),
('Eva', 'Evans', 'eva.evans@student.edu', '555-1005', '2004-05-18', 11),
('Frank', 'Fisher', 'frank.fisher@student.edu', '555-1006', '2005-09-12', 10),
('Grace', 'Green', 'grace.green@student.edu', '555-1007', '2004-01-25', 11),
('Henry', 'Harris', 'henry.harris@student.edu', '555-1008', '2003-08-14', 12);

INSERT INTO courses (course_code, course_name, description, credits, max_capacity, teacher_id, semester, academic_year) VALUES
('MATH101', 'Algebra I', 'Introduction to algebraic concepts', 4, 25, 1, 'Fall', '2024-25'),
('SCI201', 'Biology', 'Basic principles of biology', 4, 20, 2, 'Fall', '2024-25'),
('ENG101', 'English Literature', 'Introduction to English literature', 3, 30, 3, 'Fall', '2024-25'),
('HIST101', 'World History', 'Survey of world history', 3, 25, 4, 'Fall', '2024-25'),
('CS101', 'Introduction to Programming', 'Basic programming concepts', 4, 15, 5, 'Fall', '2024-25'),
('MATH201', 'Geometry', 'Geometric principles and proofs', 4, 25, 1, 'Spring', '2024-25'),
('SCI301', 'Chemistry', 'Introduction to chemistry', 4, 20, 2, 'Spring', '2024-25');

INSERT INTO time_slots (day_of_week, start_time, end_time, slot_duration) VALUES
('MONDAY', '08:00:00', '09:30:00', 90),
('MONDAY', '09:45:00', '11:15:00', 90),
('MONDAY', '11:30:00', '13:00:00', 90),
('MONDAY', '14:00:00', '15:30:00', 90),
('TUESDAY', '08:00:00', '09:30:00', 90),
('TUESDAY', '09:45:00', '11:15:00', 90),
('TUESDAY', '11:30:00', '13:00:00', 90),
('TUESDAY', '14:00:00', '15:30:00', 90),
('WEDNESDAY', '08:00:00', '09:30:00', 90),
('WEDNESDAY', '09:45:00', '11:15:00', 90),
('WEDNESDAY', '11:30:00', '13:00:00', 90),
('WEDNESDAY', '14:00:00', '15:30:00', 90),
('THURSDAY', '08:00:00', '09:30:00', 90),
('THURSDAY', '09:45:00', '11:15:00', 90),
('THURSDAY', '11:30:00', '13:00:00', 90),
('THURSDAY', '14:00:00', '15:30:00', 90),
('FRIDAY', '08:00:00', '09:30:00', 90),
('FRIDAY', '09:45:00', '11:15:00', 90),
('FRIDAY', '11:30:00', '13:00:00', 90),
('FRIDAY', '14:00:00', '15:30:00', 90);

INSERT INTO course_schedules (course_id, slot_id, classroom, effective_date, end_date) VALUES
(1, 1, 'Room-101', '2024-09-01', '2024-12-15'),  -- MATH101 Monday 8:00-9:30
(1, 5, 'Room-101', '2024-09-01', '2024-12-15'),  -- MATH101 Tuesday 8:00-9:30
(2, 2, 'Lab-201', '2024-09-01', '2024-12-15'),   -- SCI201 Monday 9:45-11:15
(2, 6, 'Lab-201', '2024-09-01', '2024-12-15'),   -- SCI201 Tuesday 9:45-11:15
(3, 3, 'Room-301', '2024-09-01', '2024-12-15'),  -- ENG101 Monday 11:30-13:00
(3, 7, 'Room-301', '2024-09-01', '2024-12-15'),  -- ENG101 Tuesday 11:30-13:00
(4, 4, 'Room-401', '2024-09-01', '2024-12-15'),  -- HIST101 Monday 14:00-15:30
(4, 8, 'Room-401', '2024-09-01', '2024-12-15'),  -- HIST101 Tuesday 14:00-15:30
(5, 9, 'Lab-501', '2024-09-01', '2024-12-15'),   -- CS101 Wednesday 8:00-9:30
(5, 13, 'Lab-501', '2024-09-01', '2024-12-15');  -- CS101 Thursday 8:00-9:30

-- Sample enrollments
INSERT INTO enrollments (student_id, course_id, enrollment_status) VALUES
(1, 1, 'ENROLLED'), (1, 2, 'ENROLLED'), (1, 3, 'ENROLLED'),
(2, 1, 'ENROLLED'), (2, 4, 'ENROLLED'), (2, 5, 'ENROLLED'),
(3, 2, 'ENROLLED'), (3, 3, 'ENROLLED'), (3, 4, 'ENROLLED'),
(4, 1, 'ENROLLED'), (4, 5, 'ENROLLED'), (4, 3, 'ENROLLED'),
(5, 2, 'ENROLLED'), (5, 4, 'ENROLLED'), (5, 5, 'ENROLLED'),
(6, 1, 'ENROLLED'), (6, 3, 'ENROLLED'), (6, 5, 'ENROLLED'),
(7, 2, 'ENROLLED'), (7, 4, 'ENROLLED'), (7, 1, 'ENROLLED'),
(8, 3, 'ENROLLED'), (8, 4, 'ENROLLED'), (8, 5, 'ENROLLED');
