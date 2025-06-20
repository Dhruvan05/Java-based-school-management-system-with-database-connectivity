package com.schoolmanagementsystem.service;

import com.schoolmanagementsystem.dao.EnrollmentDAO;
import com.schoolmanagementsystem.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer for Enrollment-related operations.
 */
public class EnrollmentService {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentDAO enrollmentDAO;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
    }

    /**
     * Get all enrollments from the database.
     * @return List of Enrollment objects, or empty list if an error occurs.
     */
    public List<Enrollment> getAllEnrollments() {
        try {
            return enrollmentDAO.findAll();
        } catch (SQLException e) {
            logger.error("Failed to fetch enrollments from database", e);
            return Collections.emptyList();
        }
    }

    // You can add more service methods as needed, e.g. save, delete, etc.
}