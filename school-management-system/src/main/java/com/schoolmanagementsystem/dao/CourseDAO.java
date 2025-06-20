package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course entity
 */
public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public List<Course> findAll() throws SQLException {
        List<Course> courses = new ArrayList<>();
        // Corrected column names: t.firstName, t.lastName
        String sql = "SELECT c.*, CONCAT(t.firstName, ' ', t.lastName) as teacher_name " +
                "FROM Course c LEFT JOIN Teacher t ON c.teacherId = t.teacherId " +
                "ORDER BY c.courseCode";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }

        logger.debug("Found {} courses", courses.size());
        return courses;
    }

    public Course findById(int courseId) throws SQLException {
        // Corrected column names: t.firstName, t.lastName
        String sql = "SELECT c.*, CONCAT(t.firstName, ' ', t.lastName) as teacher_name " +
                "FROM Course c LEFT JOIN Teacher t ON c.teacherId = t.teacherId " +
                "WHERE c.courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }

        return null;
    }

    public Course findByCourseCode(String courseCode) throws SQLException {
        // Corrected column names: t.firstName, t.lastName
        String sql = "SELECT c.*, CONCAT(t.firstName, ' ', t.lastName) as teacher_name " +
                "FROM Course c LEFT JOIN Teacher t ON c.teacherId = t.teacherId " +
                "WHERE c.courseCode = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }

        return null;
    }

    public Course save(Course course) throws SQLException {
        if (course.getCourseId() == 0) {
            return insert(course);
        } else {
            return update(course);
        }
    }

    private Course insert(Course course) throws SQLException {
        String sql = "INSERT INTO Course (courseCode, courseName, description, credits, capacity, teacherId) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setCourseParameters(stmt, course);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setCourseId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating course failed, no ID obtained.");
                }
            }
        }

        logger.info("Created new course: {}", course);
        return course;
    }

    private Course update(Course course) throws SQLException {
        String sql = "UPDATE Course SET courseCode = ?, courseName = ?, description = ?, " +
                "credits = ?, capacity = ?, teacherId = ? WHERE courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setCourseParameters(stmt, course);
            stmt.setInt(7, course.getCourseId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating course failed, no rows affected.");
            }
        }

        logger.info("Updated course: {}", course);
        return course;
    }

    public boolean delete(int courseId) throws SQLException {
        String sql = "DELETE FROM Course WHERE courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted course with ID: {}", courseId);
            return rowsAffected > 0;
        }
    }

    public int getEnrollmentCount(int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Enrollment WHERE courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private void setCourseParameters(PreparedStatement stmt, Course course) throws SQLException {
        stmt.setString(1, course.getCourseCode());
        stmt.setString(2, course.getCourseName());
        stmt.setString(3, course.getDescription());
        stmt.setInt(4, course.getCredits());
        stmt.setInt(5, course.getCapacity());
        // Handle potential null teacherId if it's optional
        if (course.getTeacherId() != 0) { // Assuming 0 means no teacherId set
            stmt.setInt(6, course.getTeacherId());
        } else {
            stmt.setNull(6, java.sql.Types.INTEGER);
        }
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("courseId"));
        course.setCourseCode(rs.getString("courseCode"));
        course.setCourseName(rs.getString("courseName"));
        course.setDescription(rs.getString("description"));
        course.setCredits(rs.getInt("credits"));
        course.setCapacity(rs.getInt("capacity"));
        course.setTeacherId(rs.getInt("teacherId"));
        course.setTeacherName(rs.getString("teacher_name")); // This alias is from the SQL query

        return course;
    }
}
