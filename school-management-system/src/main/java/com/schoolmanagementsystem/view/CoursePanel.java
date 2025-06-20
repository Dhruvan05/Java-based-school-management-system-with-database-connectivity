package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.dao.CourseDAO;
import com.schoolmanagementsystem.dao.TeacherDAO;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Panel for managing courses
 */
public class CoursePanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(CoursePanel.class);

    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;

    private JTable courseTable;
    private DefaultTableModel tableModel;

    private JTextField courseIdField;
    private JTextField courseCodeField;
    private JTextField courseNameField;
    private JTextArea descriptionArea;
    private JTextField creditsField;
    private JTextField capacityField;
    private JComboBox<String> teacherComboBox;
    private Map<String, Integer> teacherMap; // Map teacher name to teacherId

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    public CoursePanel() {
        this.courseDAO = new CourseDAO();
        this.teacherDAO = new TeacherDAO();
        initializeComponents();
        loadTeachers();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10)); // Add some padding

        // Title Panel
        JLabel titleLabel = new JLabel("Course Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Course Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Course ID (hidden but useful for internal tracking)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1;
        courseIdField = new JTextField(15);
        courseIdField.setEditable(false);
        formPanel.add(courseIdField, gbc);

        // Row 1: Course Code
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        courseCodeField = new JTextField(15);
        formPanel.add(courseCodeField, gbc);

        // Row 2: Course Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 1;
        courseNameField = new JTextField(15);
        formPanel.add(courseNameField, gbc);

        // Row 3: Description
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 15);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descriptionScrollPane, gbc);

        // Row 4: Credits
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        creditsField = new JTextField(15);
        formPanel.add(creditsField, gbc);

        // Row 5: Capacity
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        capacityField = new JTextField(15);
        formPanel.add(capacityField, gbc);

        // Row 6: Teacher
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Assigned Teacher:"), gbc);
        gbc.gridx = 1;
        teacherComboBox = new JComboBox<>();
        formPanel.add(teacherComboBox, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add Course");
        updateButton = new JButton("Update Course");
        deleteButton = new JButton("Delete Course");
        clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addCourse());
        updateButton.addActionListener(e -> updateCourse());
        deleteButton.addActionListener(e -> deleteCourse());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);


        // Table Panel
        String[] columnNames = {"ID", "Code", "Name", "Credits", "Capacity", "Teacher"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && courseTable.getSelectedRow() != -1) {
                displayCourseDetails(courseTable.getSelectedRow());
            }
        });
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setPreferredSize(new Dimension(600, 300)); // Set preferred size for the table

        // Add panels to the main layout
        add(formPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherDAO.findAll();
            teacherMap = teachers.stream()
                    .collect(Collectors.toMap(Teacher::getFullName, Teacher::getTeacherId));

            teacherComboBox.removeAllItems();
            teacherComboBox.addItem("None"); // Option for no teacher assigned
            teachers.forEach(teacher -> teacherComboBox.addItem(teacher.getFullName()));
        } catch (SQLException e) {
            logger.error("Error loading teachers: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading teachers: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Course> courses = courseDAO.findAll();
                tableModel.setRowCount(0); // Clear existing data

                for (Course course : courses) {
                    Vector<Object> row = new Vector<>();
                    row.add(course.getCourseId());
                    row.add(course.getCourseCode());
                    row.add(course.getCourseName());
                    row.add(course.getCredits());
                    row.add(course.getCapacity());
                    row.add(course.getTeacherName() != null ? course.getTeacherName() : "N/A");
                    tableModel.addRow(row);
                }
                clearForm(); // Clear form after refreshing data
            } catch (SQLException e) {
                logger.error("Error refreshing course data: {}", e.getMessage());
                JOptionPane.showMessageDialog(this, "Error refreshing course data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void displayCourseDetails(int selectedRow) {
        if (selectedRow == -1) {
            clearForm();
            return;
        }
        courseIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());
        courseCodeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
        courseNameField.setText(tableModel.getValueAt(selectedRow, 2).toString());

        try {
            // Fetch the full course object to get description and teacherId
            int courseId = (int) tableModel.getValueAt(selectedRow, 0);
            Course course = courseDAO.findById(courseId);
            if (course != null) {
                descriptionArea.setText(course.getDescription());
                creditsField.setText(String.valueOf(course.getCredits()));
                capacityField.setText(String.valueOf(course.getCapacity()));

                // Set selected teacher in combo box
                String teacherName = course.getTeacherName();
                if (teacherName != null && teacherComboBox.getItemCount() > 0) {
                    teacherComboBox.setSelectedItem(teacherName);
                } else {
                    teacherComboBox.setSelectedItem("None");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching course details for ID {}: {}", tableModel.getValueAt(selectedRow, 0), e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading course details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourse() {
        try {
            // Basic validation
            if (courseCodeField.getText().trim().isEmpty() || courseNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Course Code and Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int credits = Integer.parseInt(creditsField.getText());
            int capacity = Integer.parseInt(capacityField.getText());

            String selectedTeacherName = (String) teacherComboBox.getSelectedItem();
            Integer teacherId = null;
            if (selectedTeacherName != null && !selectedTeacherName.equals("None")) {
                teacherId = teacherMap.get(selectedTeacherName);
            }

            Course course = new Course(
                    courseCodeField.getText().trim(),
                    courseNameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    credits,
                    capacity,
                    teacherId != null ? teacherId : 0 // Pass 0 if no teacher selected, DAO handles null
            );

            courseDAO.save(course);
            JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits and Capacity must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error adding course: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error adding course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCourse() {
        try {
            if (courseIdField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a course to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (courseCodeField.getText().trim().isEmpty() || courseNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Course Code and Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int courseId = Integer.parseInt(courseIdField.getText());
            int credits = Integer.parseInt(creditsField.getText());
            int capacity = Integer.parseInt(capacityField.getText());

            String selectedTeacherName = (String) teacherComboBox.getSelectedItem();
            Integer teacherId = null;
            if (selectedTeacherName != null && !selectedTeacherName.equals("None")) {
                teacherId = teacherMap.get(selectedTeacherName);
            }

            Course course = new Course(
                    courseId,
                    courseCodeField.getText().trim(),
                    courseNameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    credits,
                    capacity,
                    teacherId != null ? teacherId : 0
            );

            courseDAO.save(course);
            JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits and Capacity must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error updating course: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        try {
            if (courseIdField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int courseId = Integer.parseInt(courseIdField.getText());
            int enrollmentCount = courseDAO.getEnrollmentCount(courseId);

            if (enrollmentCount > 0) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete course. It has " + enrollmentCount + " active enrollments. Please remove enrollments first.",
                        "Deletion Forbidden", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this course?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (courseDAO.delete(courseId)) {
                    JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Course deletion failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Course ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error deleting course: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        courseIdField.setText("");
        courseCodeField.setText("");
        courseNameField.setText("");
        descriptionArea.setText("");
        creditsField.setText("");
        capacityField.setText("");
        teacherComboBox.setSelectedItem("None");
        courseTable.clearSelection(); // Deselect any selected row
    }
}
