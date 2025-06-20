package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.TimeSlot;
import com.schoolmanagementsystem.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class SchedulePanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(SchedulePanel.class);

    private final SchedulingService schedulingService;
    private JComboBox<Course> courseComboBox;
    private JComboBox<TimeSlot> timeSlotComboBox;
    private JButton assignButton;
    private JTextArea statusArea;

    // Components for adding new time slot
    private JComboBox<TimeSlot.DayOfWeek> dayOfWeekComboBox;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField roomField;
    private JButton addTimeSlotButton;

    public SchedulePanel(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Class Scheduling", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Manual Assignment Section ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(new JLabel("Select Course:"), gbc);

        gbc.gridx = 1;
        courseComboBox = new JComboBox<>();
        contentPanel.add(courseComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Select Time Slot:"), gbc);

        gbc.gridx = 1;
        timeSlotComboBox = new JComboBox<>();
        contentPanel.add(timeSlotComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        assignButton = new JButton("Assign Course to Time Slot");
        assignButton.addActionListener(e -> assignCourseToTimeSlot());
        contentPanel.add(assignButton, gbc);

        // --- Status Area ---
        gbc.gridy++;
        statusArea = new JTextArea(3, 30);
        statusArea.setEditable(false);
        contentPanel.add(new JScrollPane(statusArea), gbc);

        // --- Separator ---
        gbc.gridy++;
        contentPanel.add(new JSeparator(), gbc);

        // --- New Time Slot Section ---
        gbc.gridy++;
        gbc.gridwidth = 2;
        contentPanel.add(new JLabel("Add New Time Slot"), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1;
        dayOfWeekComboBox = new JComboBox<>(TimeSlot.DayOfWeek.values());
        contentPanel.add(dayOfWeekComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Start Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        startTimeField = new JTextField(5);
        contentPanel.add(startTimeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("End Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        endTimeField = new JTextField(5);
        contentPanel.add(endTimeField, gbc);

        // --- Room field ---
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        roomField = new JTextField(10);
        contentPanel.add(roomField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        addTimeSlotButton = new JButton("Add Time Slot");
        addTimeSlotButton.addActionListener(e -> addTimeSlot());
        contentPanel.add(addTimeSlotButton, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                courseComboBox.removeAllItems();
                timeSlotComboBox.removeAllItems();

                List<Course> courses = schedulingService.getAllCourses();
                for (Course course : courses) {
                    courseComboBox.addItem(course);
                }

                List<TimeSlot> timeSlots = schedulingService.getAllTimeSlots();
                for (TimeSlot timeSlot : timeSlots) {
                    timeSlotComboBox.addItem(timeSlot);
                }
                statusArea.setText("Ready.");
            } catch (SQLException e) {
                logger.error("Error loading courses or time slots", e);
                statusArea.setText("Error loading data: " + e.getMessage());
            }
        });
    }

    private void assignCourseToTimeSlot() {
        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        TimeSlot selectedTimeSlot = (TimeSlot) timeSlotComboBox.getSelectedItem();
        if (selectedCourse == null || selectedTimeSlot == null) {
            statusArea.setText("Please select both a course and a time slot.");
            return;
        }
        try {
            boolean success = schedulingService.assignCourseToTimeSlot(selectedCourse.getCourseId(), selectedTimeSlot.getTimeSlotId());
            if (success) {
                statusArea.setText("Course assigned successfully.");
                refreshData();
            } else {
                statusArea.setText("Failed to assign course. Time slot may already be occupied.");
            }
        } catch (SQLException e) {
            logger.error("Error assigning course to time slot", e);
            statusArea.setText("Error: " + e.getMessage());
        }
    }

    private void addTimeSlot() {
        try {
            TimeSlot.DayOfWeek customDay = (TimeSlot.DayOfWeek) dayOfWeekComboBox.getSelectedItem();
            String startTimeStr = startTimeField.getText().trim();
            String endTimeStr = endTimeField.getText().trim();
            String room = roomField.getText().trim();
            if (customDay == null || startTimeStr.isEmpty() || endTimeStr.isEmpty() || room.isEmpty()) {
                statusArea.setText("Please fill all time slot fields.");
                return;
            }
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                statusArea.setText("End time must be after start time.");
                return;
            }
            // You must update your service to accept the room parameter!
            boolean success = schedulingService.addTimeSlot(
                    java.time.DayOfWeek.valueOf(customDay.name()),
                    startTime,
                    endTime,
                    room
            );
            if (success) {
                statusArea.setText("Time slot added successfully.");
                startTimeField.setText("");
                endTimeField.setText("");
                roomField.setText("");
                refreshData();
            } else {
                statusArea.setText("Failed to add time slot. It may overlap with an existing slot.");
            }
        } catch (Exception e) {
            logger.error("Error adding time slot", e);
            statusArea.setText("Error: " + e.getMessage());
        }
    }
}