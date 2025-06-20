package com.schoolmanagementsystem.dao;

import com.schoolmanagementsystem.model.TimeSlot;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TimeSlot entity
 */
public class TimeSlotDAO {
    private static final Logger logger = LoggerFactory.getLogger(TimeSlotDAO.class);

    public List<TimeSlot> findAll() throws SQLException {
        List<TimeSlot> timeSlots = new ArrayList<>();
        // Corrected table and column names: TimeSlot, dayOfWeek, startTime, endTime, room
        String sql = "SELECT * FROM TimeSlot ORDER BY dayOfWeek, startTime";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                timeSlots.add(mapResultSetToTimeSlot(rs));
            }
        }
        logger.debug("Found {} time slots", timeSlots.size());
        return timeSlots;
    }

    public TimeSlot findById(int timeSlotId) throws SQLException {
        // Corrected table and column names: TimeSlot, timeSlotId
        String sql = "SELECT * FROM TimeSlot WHERE timeSlotId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, timeSlotId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTimeSlot(rs);
                }
            }
        }
        return null;
    }

    public TimeSlot save(TimeSlot timeSlot) throws SQLException {
        if (timeSlot.getTimeSlotId() == 0) {
            return insert(timeSlot);
        } else {
            return update(timeSlot);
        }
    }

    private TimeSlot insert(TimeSlot timeSlot) throws SQLException {
        String sql = "INSERT INTO TimeSlot (dayOfWeek, startTime, endTime, room) " +
                "VALUES (?, ?, ?, ?)"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setTimeSlotParameters(stmt, timeSlot);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating time slot failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    timeSlot.setTimeSlotId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating time slot failed, no ID obtained.");
                }
            }
        }
        logger.info("Created new time slot: {}", timeSlot);
        return timeSlot;
    }

    private TimeSlot update(TimeSlot timeSlot) throws SQLException {
        String sql = "UPDATE TimeSlot SET dayOfWeek = ?, startTime = ?, endTime = ?, room = ? " +
                "WHERE timeSlotId = ?"; // Corrected column names

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setTimeSlotParameters(stmt, timeSlot);
            stmt.setInt(5, timeSlot.getTimeSlotId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("Updating time slot failed, no rows affected for ID: {}", timeSlot.getTimeSlotId());
                throw new SQLException("Updating time slot failed, no rows affected.");
            }
        }
        logger.info("Updated time slot: {}", timeSlot);
        return timeSlot;
    }

    public boolean delete(int timeSlotId) throws SQLException {
        // Corrected table and column name: TimeSlot, timeSlotId
        String sql = "DELETE FROM TimeSlot WHERE timeSlotId = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, timeSlotId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted time slot with ID: {}", timeSlotId);
            return rowsAffected > 0;
        }
    }

    private void setTimeSlotParameters(PreparedStatement stmt, TimeSlot timeSlot) throws SQLException {
        stmt.setString(1, timeSlot.getDayOfWeek().name());
        stmt.setTime(2, Time.valueOf(timeSlot.getStartTime()));
        stmt.setTime(3, Time.valueOf(timeSlot.getEndTime()));
        stmt.setString(4, timeSlot.getRoom());
    }

    private TimeSlot mapResultSetToTimeSlot(ResultSet rs) throws SQLException {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setTimeSlotId(rs.getInt("timeSlotId")); // Corrected column name
        timeSlot.setDayOfWeek(TimeSlot.DayOfWeek.valueOf(rs.getString("dayOfWeek"))); // Corrected column name
        timeSlot.setStartTime(rs.getTime("startTime").toLocalTime()); // Corrected column name
        timeSlot.setEndTime(rs.getTime("endTime").toLocalTime()); // Corrected column name
        timeSlot.setRoom(rs.getString("room"));
        return timeSlot;
    }
}
