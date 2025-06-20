package com.schoolmanagementsystem.service;

import com.schoolmanagementsystem.algorithm.scheduling.GreedyScheduler;
import com.schoolmanagementsystem.dao.CourseDAO;
import com.schoolmanagementsystem.dao.TimeSlotDAO;
import com.schoolmanagementsystem.dao.CourseScheduleDAO;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.TimeSlot;
import com.schoolmanagementsystem.model.CourseSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for scheduling operations
 */
public class SchedulingService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    private final CourseDAO courseDAO;
    private final TimeSlotDAO timeSlotDAO;
    private final CourseScheduleDAO courseScheduleDAO;

    public SchedulingService() {
        this.courseDAO = new CourseDAO();
        this.timeSlotDAO = new TimeSlotDAO();
        this.courseScheduleDAO = new CourseScheduleDAO();
    }

    public List<GreedyScheduler.ScheduleResult> generateOptimalSchedule() throws SQLException {
        logger.info("Generating optimal schedule using greedy algorithm");

        List<Course> courses = courseDAO.findAll();
        List<TimeSlot> timeSlots = timeSlotDAO.findAll();

        GreedyScheduler scheduler = new GreedyScheduler(timeSlots);

        // Create schedule requests for all courses
        List<GreedyScheduler.ScheduleRequest> requests = new ArrayList<>();
        for (Course course : courses) {
            // Priority based on course credits and capacity, higher priority for more credits/larger capacity
            int priority = course.getCredits() * course.getCapacity();
            requests.add(new GreedyScheduler.ScheduleRequest(course, null, priority));
        }

        List<GreedyScheduler.ScheduleResult> results = scheduler.scheduleOptimal(requests);

        // Clear existing schedules before saving new optimal ones to prevent duplicates and stale data
        clearAllSchedules();

        // Persist the generated schedule results to the database
        for (GreedyScheduler.ScheduleResult result : results) {
            if (result.isSuccessful()) {
                Course course = result.getCourse();
                TimeSlot timeSlot = result.getAssignedTimeSlot();
                if (course != null && timeSlot != null) {
                    CourseSchedule newSchedule = new CourseSchedule(course.getCourseId(), timeSlot.getTimeSlotId());
                    courseScheduleDAO.save(newSchedule);
                    logger.debug("Saved scheduled course: {} to time slot: {}", course.getCourseName(), timeSlot.toString());
                }
            } else {
                logger.warn("Course '{}' could not be scheduled. Reason: {}", result.getCourse().getCourseName(), result.getReason());
            }
        }

        return results;
    }

    public List<Course> getAllCourses() throws SQLException {
        return courseDAO.findAll();
    }

    public List<TimeSlot> getAllTimeSlots() throws SQLException {
        return timeSlotDAO.findAll();
    }

    /**
     * Adds a new TimeSlot.
     * Expects the dayOfWeek as java.time.DayOfWeek and converts to TimeSlot.DayOfWeek.
     * Now also takes a room parameter.
     */
    public boolean addTimeSlot(java.time.DayOfWeek javaDayOfWeek, LocalTime startTime, LocalTime endTime, String room) throws SQLException {
        // Convert java.time.DayOfWeek to custom enum
        TimeSlot.DayOfWeek dayOfWeek = TimeSlot.DayOfWeek.valueOf(javaDayOfWeek.name());
        // Check for overlapping time slots on the same day
        List<TimeSlot> existing = timeSlotDAO.findAll();
        for (TimeSlot ts : existing) {
            if (ts.getDayOfWeek().equals(dayOfWeek)) {
                boolean overlap = !(ts.getEndTime().compareTo(startTime) <= 0 || ts.getStartTime().compareTo(endTime) >= 0);
                if (overlap) return false;
            }
        }
        TimeSlot newSlot = new TimeSlot();
        newSlot.setDayOfWeek(dayOfWeek);
        newSlot.setStartTime(startTime);
        newSlot.setEndTime(endTime);
        newSlot.setRoom(room); // <- set the room
        timeSlotDAO.save(newSlot);
        return true;
    }

    // Helper method to clear all schedules
    private void clearAllSchedules() throws SQLException {
        logger.info("Clearing all existing course schedules from the database.");
        List<CourseSchedule> allSchedules = courseScheduleDAO.findAll();
        for (CourseSchedule schedule : allSchedules) {
            courseScheduleDAO.delete(schedule.getScheduleId());
        }
        logger.info("Cleared {} existing course schedules.", allSchedules.size());
    }

    /**
     * Checks for schedule conflicts among all scheduled courses.
     * Uses a simple pairwise check on timeslots rather than interval tree.
     */
    public boolean hasScheduleConflicts() throws SQLException {
        logger.debug("Checking for schedule conflicts");

        List<CourseSchedule> schedules = courseScheduleDAO.findAll();
        List<TimeSlot> allTimeSlots = timeSlotDAO.findAll();
        Map<Integer, TimeSlot> timeSlotMap = allTimeSlots.stream()
                .collect(Collectors.toMap(TimeSlot::getTimeSlotId, ts -> ts));

        // Build a list of scheduled time slots
        List<TimeSlot> scheduledTimeSlots = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            TimeSlot timeSlot = timeSlotMap.get(schedule.getTimeSlotId());
            if (timeSlot != null) {
                scheduledTimeSlots.add(timeSlot);
            }
        }

        // Simple O(n^2) pairwise conflict detection
        for (int i = 0; i < scheduledTimeSlots.size(); i++) {
            for (int j = i + 1; j < scheduledTimeSlots.size(); j++) {
                TimeSlot ts1 = scheduledTimeSlots.get(i);
                TimeSlot ts2 = scheduledTimeSlots.get(j);
                if (ts1.getDayOfWeek().equals(ts2.getDayOfWeek()) && isOverlap(ts1, ts2)) {
                    logger.warn("Schedule conflict detected between time slots: {} and {}", ts1, ts2);
                    return true;
                }
            }
        }
        return false;
    }

    // Helper to check overlap between two TimeSlots
    private boolean isOverlap(TimeSlot ts1, TimeSlot ts2) {
        LocalTime start1 = ts1.getStartTime();
        LocalTime end1 = ts1.getEndTime();
        LocalTime start2 = ts2.getStartTime();
        LocalTime end2 = ts2.getEndTime();
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    public List<TimeSlot> findAvailableTimeSlots(Course course) throws SQLException {
        logger.debug("Finding available time slots for course: {}", course.getCourseCode());

        List<TimeSlot> allTimeSlots = timeSlotDAO.findAll();
        List<CourseSchedule> existingSchedules = courseScheduleDAO.findAll();

        Set<Integer> occupiedTimeSlotIds = new HashSet<>();
        for (CourseSchedule schedule : existingSchedules) {
            occupiedTimeSlotIds.add(schedule.getTimeSlotId());
        }

        List<TimeSlot> availableSlots = new ArrayList<>();
        for (TimeSlot timeSlot : allTimeSlots) {
            if (!occupiedTimeSlotIds.contains(timeSlot.getTimeSlotId())) {
                availableSlots.add(timeSlot);
            }
        }

        logger.debug("Found {} available time slots", availableSlots.size());
        return availableSlots;
    }

    public boolean assignCourseToTimeSlot(int courseId, int timeSlotId) throws SQLException {
        logger.info("Assigning course {} to time slot {}", courseId, timeSlotId);

        // Check if the time slot is already occupied by another course (not the same course being reassigned)
        List<CourseSchedule> existingSchedulesInSlot = courseScheduleDAO.findAll().stream()
                .filter(cs -> cs.getTimeSlotId() == timeSlotId && cs.getCourseId() != courseId)
                .collect(Collectors.toList());

        if (!existingSchedulesInSlot.isEmpty()) {
            logger.warn("Time slot {} is already occupied by another course when trying to assign course {}.", timeSlotId, courseId);
            // Optionally, you could throw an exception or return false to indicate conflict
        }

        // Check if the course already has a schedule
        CourseSchedule existingScheduleForCourse = courseScheduleDAO.findByCourseId(courseId);
        if (existingScheduleForCourse != null) {
            // Update existing schedule
            existingScheduleForCourse.setTimeSlotId(timeSlotId);
            courseScheduleDAO.update(existingScheduleForCourse);
        } else {
            // Create new schedule
            CourseSchedule newSchedule = new CourseSchedule(courseId, timeSlotId);
            courseScheduleDAO.save(newSchedule);
        }

        return true;
    }

    public void removeScheduleAssignment(int courseId) throws SQLException {
        logger.info("Removing schedule assignment for course {}", courseId);
        courseScheduleDAO.deleteByCourseId(courseId);
    }

    public Map<String, Object> getSchedulingStatistics() throws SQLException {
        logger.debug("Generating scheduling statistics");

        Map<String, Object> stats = new HashMap<>();

        List<Course> courses = courseDAO.findAll();
        List<TimeSlot> timeSlots = timeSlotDAO.findAll();
        List<CourseSchedule> schedules = courseScheduleDAO.findAll();

        stats.put("totalCourses", courses.size());
        stats.put("totalTimeSlots", timeSlots.size());
        stats.put("scheduledCourses", schedules.size());
        stats.put("unscheduledCourses", courses.size() - schedules.size());
        stats.put("utilizationRate", timeSlots.isEmpty() ? 0.0 :
                (double) schedules.size() / timeSlots.size() * 100);
        stats.put("hasConflicts", hasScheduleConflicts());

        return stats;
    }
}