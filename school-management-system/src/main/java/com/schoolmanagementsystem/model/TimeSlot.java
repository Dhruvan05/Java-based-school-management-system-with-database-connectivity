package com.schoolmanagementsystem.model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * TimeSlot entity model
 */
public class TimeSlot {
    private int timeSlotId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    // Constructors
    public TimeSlot() {}

    public TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public TimeSlot(int timeSlotId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
        this.timeSlotId = timeSlotId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    // Getters and Setters
    public int getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(int timeSlotId) { this.timeSlotId = timeSlotId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public boolean overlaps(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return timeSlotId == timeSlot.timeSlotId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSlotId);
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + "-" + endTime + " (" + room + ")";
    }
}