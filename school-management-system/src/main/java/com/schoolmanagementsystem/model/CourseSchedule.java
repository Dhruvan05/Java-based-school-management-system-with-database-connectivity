package com.schoolmanagementsystem.model;

import java.util.Objects;

/**
 * CourseSchedule entity model
 */
public class CourseSchedule {
    private int scheduleId;
    private int courseId;
    private int timeSlotId;
    private String courseName; // For display purposes
    private String timeSlotInfo; // For display purposes

    // Constructors
    public CourseSchedule() {}

    public CourseSchedule(int courseId, int timeSlotId) {
        this.courseId = courseId;
        this.timeSlotId = timeSlotId;
    }

    public CourseSchedule(int scheduleId, int courseId, int timeSlotId) {
        this.scheduleId = scheduleId;
        this.courseId = courseId;
        this.timeSlotId = timeSlotId;
    }

    // Getters and Setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(int timeSlotId) { this.timeSlotId = timeSlotId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getTimeSlotInfo() { return timeSlotInfo; }
    public void setTimeSlotInfo(String timeSlotInfo) { this.timeSlotInfo = timeSlotInfo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseSchedule that = (CourseSchedule) o;
        return scheduleId == that.scheduleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return "CourseSchedule{" +
                "scheduleId=" + scheduleId +
                ", courseId=" + courseId +
                ", timeSlotId=" + timeSlotId +
                '}';
    }
}