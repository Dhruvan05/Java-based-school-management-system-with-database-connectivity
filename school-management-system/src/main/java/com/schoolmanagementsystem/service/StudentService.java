package com.schoolmanagementsystem.service;

import com.schoolmanagementsystem.dao.StudentDAO;
import com.schoolmanagementsystem.dao.CourseDAO;
import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Student business logic
 */
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO; // Added for course operations

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO(); // Initialize CourseDAO
    }

    public List<Student> getAllStudents() throws SQLException {
        logger.debug("Retrieving all students");
        return studentDAO.findAll();
    }

    public Student getStudentById(int studentId) throws SQLException {
        logger.debug("Retrieving student with ID: {}", studentId);
        return studentDAO.findById(studentId);
    }

    public List<Student> searchStudents(String searchTerm) throws SQLException {
        logger.debug("Searching students with term: {}", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentDAO.findByName(searchTerm.trim());
    }

    public Student saveStudent(Student student) throws SQLException, ValidationException {
        validateStudent(student);

        logger.info("Saving student: {}", student);
        return studentDAO.save(student);
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        logger.info("Deleting student with ID: {}", studentId);
        return studentDAO.delete(studentId);
    }

    public List<Course> getAllCourses() throws SQLException {
        logger.debug("Retrieving all courses");
        return courseDAO.findAll();
    }

    private void validateStudent(Student student) throws ValidationException {
        if (!ValidationUtil.isNotEmpty(student.getFirstName())) {
            throw new ValidationException("First name is required");
        }

        if (!ValidationUtil.isNotEmpty(student.getLastName())) {
            throw new ValidationException("Last name is required");
        }

        if (!ValidationUtil.isValidEmail(student.getEmail())) {
            throw new ValidationException("Valid email address is required");
        }

        if (student.getPhoneNumber() != null &&
                !student.getPhoneNumber().isEmpty() &&
                !ValidationUtil.isValidPhoneNumber(student.getPhoneNumber())) {
            throw new ValidationException("Invalid phone number format");
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}