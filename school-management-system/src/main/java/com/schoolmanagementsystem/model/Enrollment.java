package com.schoolmanagementsystem.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Enrollment entity model
 */
public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int courseId;
    private LocalDate enrollmentDate;
    private String grade;
    private String studentName; // For display purposes
    private String courseName; // For display purposes

    // Constructors
    public Enrollment() {}

    public Enrollment(int studentId, int courseId, LocalDate enrollmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
    }

    public Enrollment(int enrollmentId, int studentId, int courseId, LocalDate enrollmentDate, String grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
        this.grade = grade;
    }

    // Getters and Setters
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return enrollmentId == that.enrollmentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", enrollmentDate=" + enrollmentDate +
                ", grade='" + grade + '\'' +
                '}';
    }
}