package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.Teacher;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Teacher entity
 */
public class TeacherDAO {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDAO.class);

    public List<Teacher> findAll() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        // Corrected column names: lastName, firstName
        String sql = "SELECT * FROM Teacher ORDER BY lastName, firstName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                teachers.add(mapResultSetToTeacher(rs));
            }
        }
        logger.debug("Found {} teachers", teachers.size());
        return teachers;
    }

    public Teacher findById(int teacherId) throws SQLException {
        // Corrected table and column name: Teacher, teacherId
        String sql = "SELECT * FROM Teacher WHERE teacherId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTeacher(rs);
                }
            }
        }
        return null;
    }

    public List<Teacher> findByName(String searchTerm) throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM Teacher WHERE " +
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
                    teachers.add(mapResultSetToTeacher(rs));
                }
            }
        }
        logger.debug("Found {} teachers matching '{}'", teachers.size(), searchTerm);
        return teachers;
    }

    public Teacher save(Teacher teacher) throws SQLException {
        if (teacher.getTeacherId() == 0) {
            return insert(teacher);
        } else {
            return update(teacher);
        }
    }

    private Teacher insert(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO Teacher (firstName, lastName, email, department, phoneNumber) " +
                "VALUES (?, ?, ?, ?, ?)"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setTeacherParameters(stmt, teacher);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating teacher failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    teacher.setTeacherId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating teacher failed, no ID obtained.");
                }
            }
        }
        logger.info("Created new teacher: {}", teacher);
        return teacher;
    }

    private Teacher update(Teacher teacher) throws SQLException {
        String sql = "UPDATE Teacher SET firstName = ?, lastName = ?, email = ?, " +
                "department = ?, phoneNumber = ? WHERE teacherId = ?"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setTeacherParameters(stmt, teacher);
            stmt.setInt(6, teacher.getTeacherId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("Updating teacher failed, no rows affected for ID: {}", teacher.getTeacherId());
                throw new SQLException("Updating teacher failed, no rows affected.");
            }
        }
        logger.info("Updated teacher: {}", teacher);
        return teacher;
    }

    public boolean delete(int teacherId) throws SQLException {
        // Corrected table and column name: Teacher, teacherId
        String sql = "DELETE FROM Teacher WHERE teacherId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted teacher with ID: {}", teacherId);
            return rowsAffected > 0;
        }
    }

    private void setTeacherParameters(PreparedStatement stmt, Teacher teacher) throws SQLException {
        stmt.setString(1, teacher.getFirstName());
        stmt.setString(2, teacher.getLastName());
        stmt.setString(3, teacher.getEmail());
        stmt.setString(4, teacher.getDepartment());
        stmt.setString(5, teacher.getPhoneNumber());
    }

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(rs.getInt("teacherId")); // Corrected column name
        teacher.setFirstName(rs.getString("firstName")); // Corrected column name
        teacher.setLastName(rs.getString("lastName")); // Corrected column name
        teacher.setEmail(rs.getString("email"));
        teacher.setDepartment(rs.getString("department"));
        teacher.setPhoneNumber(rs.getString("phoneNumber")); // Corrected column name
        return teacher;
    }
}
