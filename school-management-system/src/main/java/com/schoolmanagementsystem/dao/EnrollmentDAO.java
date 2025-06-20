package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.Enrollment;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Enrollment entity
 */
public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public List<Enrollment> findAll() throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "CONCAT(s.firstName, ' ', s.lastName) as studentName, " +
                "c.courseName as courseName " +
                "FROM Enrollment e " +
                "JOIN Student s ON e.studentId = s.studentId " +
                "JOIN Course c ON e.courseId = c.courseId " +
                "ORDER BY e.enrollmentDate DESC, studentName, courseName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        }
        logger.debug("Found {} enrollments", enrollments.size());
        return enrollments;
    }

    public Enrollment findById(int enrollmentId) throws SQLException {
        String sql = "SELECT e.*, " +
                "CONCAT(s.firstName, ' ', s.lastName) as studentName, " +
                "c.courseName as courseName " +
                "FROM Enrollment e " +
                "JOIN Student s ON e.studentId = s.studentId " +
                "JOIN Course c ON e.courseId = c.courseId " +
                "WHERE e.enrollmentId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnrollment(rs);
                }
            }
        }
        return null;
    }

    public List<Enrollment> findByStudentId(int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "CONCAT(s.firstName, ' ', s.lastName) as studentName, " +
                "c.courseName as courseName " +
                "FROM Enrollment e " +
                "JOIN Student s ON e.studentId = s.studentId " +
                "JOIN Course c ON e.courseId = c.courseId " +
                "WHERE e.studentId = ? " +
                "ORDER BY e.enrollmentDate DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }
        logger.debug("Found {} enrollments for student ID {}", enrollments.size(), studentId);
        return enrollments;
    }

    public List<Enrollment> findByCourseId(int courseId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "CONCAT(s.firstName, ' ', s.lastName) as studentName, " +
                "c.courseName as courseName " +
                "FROM Enrollment e " +
                "JOIN Student s ON e.studentId = s.studentId " +
                "JOIN Course c ON e.courseId = c.courseId " +
                "WHERE e.courseId = ? " +
                "ORDER BY e.enrollmentDate DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }
        logger.debug("Found {} enrollments for course ID {}", enrollments.size(), courseId);
        return enrollments;
    }

    public Enrollment save(Enrollment enrollment) throws SQLException {
        if (enrollment.getEnrollmentId() == 0) {
            return insert(enrollment);
        } else {
            return update(enrollment);
        }
    }

    private Enrollment insert(Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO Enrollment (studentId, courseId, enrollmentDate, grade) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setEnrollmentParameters(stmt, enrollment);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating enrollment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    enrollment.setEnrollmentId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating enrollment failed, no ID obtained.");
                }
            }
        }
        logger.info("Created new enrollment: {}", enrollment);
        return enrollment;
    }

    private Enrollment update(Enrollment enrollment) throws SQLException {
        String sql = "UPDATE Enrollment SET studentId = ?, courseId = ?, enrollmentDate = ?, grade = ? WHERE enrollmentId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setEnrollmentParameters(stmt, enrollment);
            stmt.setInt(5, enrollment.getEnrollmentId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("Updating enrollment failed, no rows affected for ID: {}", enrollment.getEnrollmentId());
                throw new SQLException("Updating enrollment failed, no rows affected.");
            }
        }
        logger.info("Updated enrollment: {}", enrollment);
        return enrollment;
    }

    public boolean delete(int enrollmentId) throws SQLException {
        String sql = "DELETE FROM Enrollment WHERE enrollmentId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted enrollment with ID: {}, rows affected: {}", enrollmentId, rowsAffected);
            return rowsAffected > 0;
        }
    }

    /**
     * Sets parameters for an Enrollment insert/update statement.
     */
    private void setEnrollmentParameters(PreparedStatement stmt, Enrollment enrollment) throws SQLException {
        stmt.setInt(1, enrollment.getStudentId());
        stmt.setInt(2, enrollment.getCourseId());
        if (enrollment.getEnrollmentDate() != null) {
            stmt.setDate(3, Date.valueOf(enrollment.getEnrollmentDate()));
        } else {
            stmt.setNull(3, Types.DATE);
        }
        stmt.setString(4, enrollment.getGrade());
    }

    /**
     * Maps a ResultSet row to an Enrollment object.
     */
    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollmentId"));
        enrollment.setStudentId(rs.getInt("studentId"));
        enrollment.setCourseId(rs.getInt("courseId"));

        Date enrollmentDate = rs.getDate("enrollmentDate");
        if (enrollmentDate != null) {
            enrollment.setEnrollmentDate(enrollmentDate.toLocalDate());
        } else {
            enrollment.setEnrollmentDate(null);
        }

        enrollment.setGrade(rs.getString("grade"));
        enrollment.setStudentName(rs.getString("studentName"));
        enrollment.setCourseName(rs.getString("courseName"));

        return enrollment;
    }
}