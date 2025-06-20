package com.schoolmanagementsystem.algorithm.scheduling;

import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.TimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Greedy algorithm implementation for course scheduling
 */
public class GreedyScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GreedyScheduler.class);

    public static class ScheduleRequest {
        private final Course course;
        private final TimeSlot preferredTimeSlot;
        private final int priority;

        public ScheduleRequest(Course course, TimeSlot preferredTimeSlot, int priority) {
            this.course = course;
            this.preferredTimeSlot = preferredTimeSlot;
            this.priority = priority;
        }

        public Course getCourse() { return course; }
        public TimeSlot getPreferredTimeSlot() { return preferredTimeSlot; }
        public int getPriority() { return priority; }
    }

    public static class ScheduleResult {
        private final Course course;
        private final TimeSlot assignedTimeSlot;
        private final boolean successful;
        private final String reason;

        public ScheduleResult(Course course, TimeSlot assignedTimeSlot, boolean successful, String reason) {
            this.course = course;
            this.assignedTimeSlot = assignedTimeSlot;
            this.successful = successful;
            this.reason = reason;
        }

        public Course getCourse() { return course; }
        public TimeSlot getAssignedTimeSlot() { return assignedTimeSlot; }
        public boolean isSuccessful() { return successful; }
        public String getReason() { return reason; }
    }

    private final IntervalTree scheduledIntervals;
    private final List<TimeSlot> availableTimeSlots;

    public GreedyScheduler(List<TimeSlot> availableTimeSlots) {
        this.scheduledIntervals = new IntervalTree();
        this.availableTimeSlots = new ArrayList<>(availableTimeSlots);

        // Sort time slots by end time (greedy choice)
        this.availableTimeSlots.sort((ts1, ts2) -> {
            int dayComparison = ts1.getDayOfWeek().compareTo(ts2.getDayOfWeek());
            if (dayComparison != 0) {
                return dayComparison;
            }
            return ts1.getEndTime().compareTo(ts2.getEndTime());
        });
    }

    public List<ScheduleResult> scheduleOptimal(List<ScheduleRequest> requests) {
        logger.info("Starting optimal scheduling for {} requests", requests.size());

        // Sort requests by priority (higher priority first), then by preferred end time
        List<ScheduleRequest> sortedRequests = requests.stream()
                .sorted((r1, r2) -> {
                    int priorityComparison = Integer.compare(r2.getPriority(), r1.getPriority());
                    if (priorityComparison != 0) {
                        return priorityComparison;
                    }
                    if (r1.getPreferredTimeSlot() != null && r2.getPreferredTimeSlot() != null) {
                        return r1.getPreferredTimeSlot().getEndTime().compareTo(r2.getPreferredTimeSlot().getEndTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        List<ScheduleResult> results = new ArrayList<>();

        for (ScheduleRequest request : sortedRequests) {
            ScheduleResult result = scheduleClass(request);
            results.add(result);
        }

        logger.info("Scheduling completed. Successful: {}, Failed: {}", 
                   results.stream().mapToInt(r -> r.isSuccessful() ? 1 : 0).sum(),
                   results.stream().mapToInt(r -> r.isSuccessful() ? 0 : 1).sum());

        return results;
    }

    public ScheduleResult scheduleClass(ScheduleRequest request) {
        Course course = request.getCourse();
        TimeSlot preferredSlot = request.getPreferredTimeSlot();

        // First, try to schedule at preferred time slot
        if (preferredSlot != null) {
            if (canScheduleAt(preferredSlot)) {
                addToSchedule(course, preferredSlot);
                logger.debug("Scheduled {} at preferred time slot: {}", course.getCourseCode(), preferredSlot);
                return new ScheduleResult(course, preferredSlot, true, "Scheduled at preferred time");
            }
        }

        // If preferred slot is not available, find the best alternative using greedy approach
        for (TimeSlot timeSlot : availableTimeSlots) {
            if (canScheduleAt(timeSlot)) {
                addToSchedule(course, timeSlot);
                logger.debug("Scheduled {} at alternative time slot: {}", course.getCourseCode(), timeSlot);
                return new ScheduleResult(course, timeSlot, true, "Scheduled at alternative time");
            }
        }

        logger.debug("Failed to schedule {}: No available time slots", course.getCourseCode());
        return new ScheduleResult(course, null, false, "No available time slots");
    }

    private boolean canScheduleAt(TimeSlot timeSlot) {
        int startMinutes = timeSlotToMinutes(timeSlot.getStartTime());
        int endMinutes = timeSlotToMinutes(timeSlot.getEndTime());
        int dayOffset = timeSlot.getDayOfWeek().ordinal() * 24 * 60; // Convert day to minutes

        IntervalTree.Interval queryInterval = new IntervalTree.Interval(
                dayOffset + startMinutes, 
                dayOffset + endMinutes, 
                timeSlot);

        return !scheduledIntervals.hasOverlap(queryInterval);
    }

    private void addToSchedule(Course course, TimeSlot timeSlot) {
        int startMinutes = timeSlotToMinutes(timeSlot.getStartTime());
        int endMinutes = timeSlotToMinutes(timeSlot.getEndTime());
        int dayOffset = timeSlot.getDayOfWeek().ordinal() * 24 * 60;

        IntervalTree.Interval interval = new IntervalTree.Interval(
                dayOffset + startMinutes, 
                dayOffset + endMinutes, 
                new CourseTimeSlot(course, timeSlot));

        scheduledIntervals.insert(interval);
    }

    private int timeSlotToMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    public List<CourseTimeSlot> getScheduledClasses() {
        return scheduledIntervals.getAllIntervals().stream()
                .map(interval -> (CourseTimeSlot) interval.getData())
                .collect(Collectors.toList());
    }

    public boolean hasConflicts() {
        List<IntervalTree.Interval> intervals = scheduledIntervals.getAllIntervals();
        for (int i = 0; i < intervals.size(); i++) {
            for (int j = i + 1; j < intervals.size(); j++) {
                if (intervals.get(i).overlaps(intervals.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearSchedule() {
        scheduledIntervals.clear();
    }

    public static class CourseTimeSlot {
        private final Course course;
        private final TimeSlot timeSlot;

        public CourseTimeSlot(Course course, TimeSlot timeSlot) {
            this.course = course;
            this.timeSlot = timeSlot;
        }

        public Course getCourse() { return course; }
        public TimeSlot getTimeSlot() { return timeSlot; }

        @Override
        public String toString() {
            return course.getCourseCode() + " - " + timeSlot.toString();
        }
    }
}