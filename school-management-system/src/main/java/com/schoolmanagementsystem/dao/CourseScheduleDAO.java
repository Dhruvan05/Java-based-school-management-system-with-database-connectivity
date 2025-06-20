package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.CourseSchedule;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CourseSchedule entity
 */
public class CourseScheduleDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseScheduleDAO.class);

    public List<CourseSchedule> findAll() throws SQLException {
        List<CourseSchedule> schedules = new ArrayList<>();
        // Corrected table and column names: CourseSchedule, scheduleId, courseId, timeSlotId
        String sql = "SELECT cs.*, c.courseName as courseName, ts.dayOfWeek, ts.startTime, ts.endTime, ts.room " +
                "FROM CourseSchedule cs " +
                "JOIN Course c ON cs.courseId = c.courseId " +
                "JOIN TimeSlot ts ON cs.timeSlotId = ts.timeSlotId " +
                "ORDER BY ts.dayOfWeek, ts.startTime, c.courseName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                schedules.add(mapResultSetToCourseSchedule(rs));
            }
        }
        logger.debug("Found {} course schedules", schedules.size());
        return schedules;
    }

    public CourseSchedule findById(int scheduleId) throws SQLException {
        // Corrected table and column names: CourseSchedule, scheduleId
        String sql = "SELECT cs.*, c.courseName as courseName, ts.dayOfWeek, ts.startTime, ts.endTime, ts.room " +
                "FROM CourseSchedule cs " +
                "JOIN Course c ON cs.courseId = c.courseId " +
                "JOIN TimeSlot ts ON cs.timeSlotId = ts.timeSlotId " +
                "WHERE cs.scheduleId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scheduleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourseSchedule(rs);
                }
            }
        }
        return null;
    }

    public CourseSchedule findByCourseId(int courseId) throws SQLException {
        // Corrected table and column names: CourseSchedule, courseId
        String sql = "SELECT cs.*, c.courseName as courseName, ts.dayOfWeek, ts.startTime, ts.endTime, ts.room " +
                "FROM CourseSchedule cs " +
                "JOIN Course c ON cs.courseId = c.courseId " +
                "JOIN TimeSlot ts ON cs.timeSlotId = ts.timeSlotId " +
                "WHERE cs.courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourseSchedule(rs);
                }
            }
        }
        return null;
    }


    public CourseSchedule save(CourseSchedule schedule) throws SQLException {
        if (schedule.getScheduleId() == 0) {
            return insert(schedule);
        } else {
            return update(schedule);
        }
    }

    private CourseSchedule insert(CourseSchedule schedule) throws SQLException {
        String sql = "INSERT INTO CourseSchedule (courseId, timeSlotId) VALUES (?, ?)"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setCourseScheduleParameters(stmt, schedule);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating course schedule failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setScheduleId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating course schedule failed, no ID obtained.");
                }
            }
        }
        logger.info("Created new course schedule: {}", schedule);
        return schedule;
    }

    public CourseSchedule update(CourseSchedule schedule) throws SQLException {
        String sql = "UPDATE CourseSchedule SET courseId = ?, timeSlotId = ? WHERE scheduleId = ?"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setCourseScheduleParameters(stmt, schedule);
            stmt.setInt(3, schedule.getScheduleId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("Updating course schedule failed, no rows affected for ID: {}", schedule.getScheduleId());
                throw new SQLException("Updating course schedule failed, no rows affected.");
            }
        }
        logger.info("Updated course schedule: {}", schedule);
        return schedule;
    }

    public boolean delete(int scheduleId) throws SQLException {
        // Corrected table and column name: CourseSchedule, scheduleId
        String sql = "DELETE FROM CourseSchedule WHERE scheduleId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scheduleId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted course schedule with ID: {}", scheduleId);
            return rowsAffected > 0;
        }
    }

    public boolean deleteByCourseId(int courseId) throws SQLException {
        // Added for removing schedules linked to a course
        String sql = "DELETE FROM CourseSchedule WHERE courseId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted {} schedules for courseId: {}", rowsAffected, courseId);
            return rowsAffected > 0;
        }
    }


    private void setCourseScheduleParameters(PreparedStatement stmt, CourseSchedule schedule) throws SQLException {
        stmt.setInt(1, schedule.getCourseId());
        stmt.setInt(2, schedule.getTimeSlotId());
    }

    private CourseSchedule mapResultSetToCourseSchedule(ResultSet rs) throws SQLException {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setScheduleId(rs.getInt("scheduleId")); // Corrected column name
        schedule.setCourseId(rs.getInt("courseId"));     // Corrected column name
        schedule.setTimeSlotId(rs.getInt("timeSlotId")); // Corrected column name
        schedule.setCourseName(rs.getString("courseName")); // From JOIN

        // Construct timeSlotInfo for display
        String dayOfWeek = rs.getString("dayOfWeek");
        String startTime = rs.getTime("startTime").toLocalTime().toString();
        String endTime = rs.getTime("endTime").toLocalTime().toString();
        String room = rs.getString("room");
        schedule.setTimeSlotInfo(dayOfWeek + " " + startTime + "-" + endTime + " (" + room + ")");

        return schedule;
    }
}
