package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entity
 */
public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        // Corrected column names: lastName, firstName
        String sql = "SELECT * FROM Student ORDER BY lastName, firstName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }

        logger.debug("Found {} students", students.size());
        return students;
    }

    public Student findById(int studentId) throws SQLException {
        // Corrected column name: studentId
        String sql = "SELECT * FROM Student WHERE studentId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }

        return null;
    }

    public List<Student> findByName(String searchTerm) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student WHERE " +
                // Corrected column names: firstName, lastName
                "LOWER(firstName) LIKE LOWER(?) OR " +
                "LOWER(lastName) LIKE LOWER(?) OR " +
                "LOWER(CONCAT(firstName, ' ', lastName)) LIKE LOWER(?) " +
                "ORDER BY lastName, firstName"; // Corrected column names

        String pattern = "%" + searchTerm + "%";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        }

        logger.debug("Found {} students matching '{}'", students.size(), searchTerm);
        return students;
    }

    public Student save(Student student) throws SQLException {
        if (student.getStudentId() == 0) {
            return insert(student);
        } else {
            return update(student);
        }
    }

    private Student insert(Student student) throws SQLException {
        String sql = "INSERT INTO Student (firstName, lastName, email, dateOfBirth, address, phoneNumber) " +
                "VALUES (?, ?, ?, ?, ?, ?)"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setStudentParameters(stmt, student);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setStudentId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating student failed, no ID obtained.");
                }
            }
        }

        logger.info("Created new student: {}", student);
        return student;
    }

    private Student update(Student student) throws SQLException {
        String sql = "UPDATE Student SET firstName = ?, lastName = ?, email = ?, " +
                "dateOfBirth = ?, address = ?, phoneNumber = ? WHERE studentId = ?"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setStudentParameters(stmt, student);
            stmt.setInt(7, student.getStudentId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating student failed, no rows affected.");
            }
        }

        logger.info("Updated student: {}", student);
        return student;
    }

    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM Student WHERE studentId = ?"; // Corrected column name

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted student with ID: {}", studentId);
            return rowsAffected > 0;
        }
    }

    private void setStudentParameters(PreparedStatement stmt, Student student) throws SQLException {
        stmt.setString(1, student.getFirstName());
        stmt.setString(2, student.getLastName());
        stmt.setString(3, student.getEmail());
        stmt.setDate(4, student.getDateOfBirth() != null ? Date.valueOf(student.getDateOfBirth()) : null);
        stmt.setString(5, student.getAddress());
        stmt.setString(6, student.getPhoneNumber());
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("studentId")); // Corrected column name
        student.setFirstName(rs.getString("firstName")); // Corrected column name
        student.setLastName(rs.getString("lastName")); // Corrected column name
        student.setEmail(rs.getString("email"));

        Date dateOfBirth = rs.getDate("dateOfBirth"); // Corrected column name
        if (dateOfBirth != null) {
            student.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        student.setAddress(rs.getString("address"));
        student.setPhoneNumber(rs.getString("phoneNumber")); // Corrected column name

        return student;
    }
}
