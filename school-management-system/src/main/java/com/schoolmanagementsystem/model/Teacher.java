package com.schoolmanagementsystem.model;

import java.util.Objects;

/**
 * Teacher entity model
 */
public class Teacher {
    private int teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String phoneNumber;

    // Constructors
    public Teacher() {}

    public Teacher(String firstName, String lastName, String email, String department, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.phoneNumber = phoneNumber;
    }

    public Teacher(int teacherId, String firstName, String lastName, String email, String department, String phoneNumber) {
        this.teacherId = teacherId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return teacherId == teacher.teacherId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId=" + teacherId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}