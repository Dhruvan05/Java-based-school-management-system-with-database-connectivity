package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.dao.CourseDAO;
import com.schoolmanagementsystem.dao.EnrollmentDAO;
import com.schoolmanagementsystem.dao.StudentDAO;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.Enrollment;
import com.schoolmanagementsystem.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Panel for managing enrollments
 */
public class EnrollmentPanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentPanel.class);

    private final EnrollmentDAO enrollmentDAO;
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;

    private JTable enrollmentTable;
    private DefaultTableModel tableModel;

    private JTextField enrollmentIdField;
    private JComboBox<String> studentComboBox;
    private JComboBox<String> courseComboBox;
    private JTextField enrollmentDateField;
    private JTextField gradeField;

    private Map<String, Integer> studentMap; // Map student name to studentId
    private Map<String, Integer> courseMap;  // Map course name to courseId

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    public EnrollmentPanel() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        initializeComponents();
        loadStudentsAndCourses();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10)); // Add some padding

        // Title Panel
        JLabel titleLabel = new JLabel("Enrollment Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Enrollment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Enrollment ID (hidden but useful for internal tracking)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Enrollment ID:"), gbc);
        gbc.gridx = 1;
        enrollmentIdField = new JTextField(15);
        enrollmentIdField.setEditable(false);
        formPanel.add(enrollmentIdField, gbc);

        // Row 1: Student
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Student:"), gbc);
        gbc.gridx = 1;
        studentComboBox = new JComboBox<>();
        formPanel.add(studentComboBox, gbc);

        // Row 2: Course
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        courseComboBox = new JComboBox<>();
        formPanel.add(courseComboBox, gbc);

        // Row 3: Enrollment Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Enrollment Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        enrollmentDateField = new JTextField(15);
        formPanel.add(enrollmentDateField, gbc);

        // Row 4: Grade
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Grade:"), gbc);
        gbc.gridx = 1;
        gradeField = new JTextField(15);
        formPanel.add(gradeField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add Enrollment");
        updateButton = new JButton("Update Enrollment");
        deleteButton = new JButton("Delete Enrollment");
        clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addEnrollment());
        updateButton.addActionListener(e -> updateEnrollment());
        deleteButton.addActionListener(e -> deleteEnrollment());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Table Panel
        String[] columnNames = {"ID", "Student", "Course", "Enrollment Date", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        enrollmentTable = new JTable(tableModel);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && enrollmentTable.getSelectedRow() != -1) {
                displayEnrollmentDetails(enrollmentTable.getSelectedRow());
            }
        });
        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        scrollPane.setPreferredSize(new Dimension(600, 300)); // Set preferred size for the table

        // Add panels to the main layout
        add(formPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadStudentsAndCourses() {
        try {
            List<Student> students = studentDAO.findAll();
            studentMap = students.stream()
                    .collect(Collectors.toMap(Student::getFullName, Student::getStudentId));

            studentComboBox.removeAllItems();
            students.forEach(student -> studentComboBox.addItem(student.getFullName()));

            List<Course> courses = courseDAO.findAll();
            courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getCourseName, Course::getCourseId));

            courseComboBox.removeAllItems();
            courses.forEach(course -> courseComboBox.addItem(course.getCourseName()));

        } catch (SQLException e) {
            logger.error("Error loading students or courses: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading students or courses: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Enrollment> enrollments = enrollmentDAO.findAll();
                tableModel.setRowCount(0); // Clear existing data

                for (Enrollment enrollment : enrollments) {
                    Vector<Object> row = new Vector<>();
                    row.add(enrollment.getEnrollmentId());
                    row.add(enrollment.getStudentName() != null ? enrollment.getStudentName() : "N/A");
                    row.add(enrollment.getCourseName() != null ? enrollment.getCourseName() : "N/A");
                    row.add(enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate().toString() : "");
                    row.add(enrollment.getGrade() != null ? enrollment.getGrade() : "");
                    tableModel.addRow(row);
                }
                clearForm(); // Clear form after refreshing data
            } catch (SQLException e) {
                logger.error("Error refreshing enrollment data: {}", e.getMessage());
                JOptionPane.showMessageDialog(this, "Error refreshing enrollment data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void displayEnrollmentDetails(int selectedRow) {
        if (selectedRow == -1) {
            clearForm();
            return;
        }
        enrollmentIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());

        try {
            // Fetch the full enrollment object to get studentId and courseId
            int enrollmentId = (int) tableModel.getValueAt(selectedRow, 0);
            Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
            if (enrollment != null) {
                // Set selected student in combo box
                String studentName = enrollment.getStudentName();
                if (studentName != null && studentComboBox.getItemCount() > 0) {
                    studentComboBox.setSelectedItem(studentName);
                }

                // Set selected course in combo box
                String courseName = enrollment.getCourseName();
                if (courseName != null && courseComboBox.getItemCount() > 0) {
                    courseComboBox.setSelectedItem(courseName);
                }

                enrollmentDateField.setText(enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate().toString() : "");
                gradeField.setText(enrollment.getGrade() != null ? enrollment.getGrade() : "");
            }
        } catch (SQLException e) {
            logger.error("Error fetching enrollment details for ID {}: {}", tableModel.getValueAt(selectedRow, 0), e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading enrollment details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEnrollment() {
        try {
            // Basic validation
            String selectedStudentName = (String) studentComboBox.getSelectedItem();
            String selectedCourseName = (String) courseComboBox.getSelectedItem();
            String dateString = enrollmentDateField.getText().trim();

            if (selectedStudentName == null || selectedStudentName.isEmpty() ||
                    selectedCourseName == null || selectedCourseName.isEmpty() ||
                    dateString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student, Course, and Enrollment Date cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer studentId = studentMap.get(selectedStudentName);
            Integer courseId = courseMap.get(selectedCourseName);
            LocalDate enrollmentDate = LocalDate.parse(dateString); // Expects YYYY-MM-DD

            if (studentId == null || courseId == null) {
                JOptionPane.showMessageDialog(this, "Selected student or course not found. Please refresh.", "Data Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Enrollment enrollment = new Enrollment(
                    studentId,
                    courseId,
                    enrollmentDate
            );
            enrollment.setGrade(gradeField.getText().trim()); // Grade can be empty

            enrollmentDAO.save(enrollment);
            JOptionPane.showMessageDialog(this, "Enrollment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error adding enrollment: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error adding enrollment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEnrollment() {
        try {
            if (enrollmentIdField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an enrollment to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String selectedStudentName = (String) studentComboBox.getSelectedItem();
            String selectedCourseName = (String) courseComboBox.getSelectedItem();
            String dateString = enrollmentDateField.getText().trim();

            if (selectedStudentName == null || selectedStudentName.isEmpty() ||
                    selectedCourseName == null || selectedCourseName.isEmpty() ||
                    dateString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student, Course, and Enrollment Date cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int enrollmentId = Integer.parseInt(enrollmentIdField.getText());
            Integer studentId = studentMap.get(selectedStudentName);
            Integer courseId = courseMap.get(selectedCourseName);
            LocalDate enrollmentDate = LocalDate.parse(dateString);

            if (studentId == null || courseId == null) {
                JOptionPane.showMessageDialog(this, "Selected student or course not found. Please refresh.", "Data Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Enrollment enrollment = new Enrollment(
                    enrollmentId,
                    studentId,
                    courseId,
                    enrollmentDate,
                    gradeField.getText().trim()
            );

            enrollmentDAO.save(enrollment);
            JOptionPane.showMessageDialog(this, "Enrollment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Enrollment ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error updating enrollment: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error updating enrollment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEnrollment() {
        try {
            if (enrollmentIdField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an enrollment to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int enrollmentId = Integer.parseInt(enrollmentIdField.getText());

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this enrollment?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (enrollmentDAO.delete(enrollmentId)) {
                    JOptionPane.showMessageDialog(this, "Enrollment deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Enrollment deletion failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Enrollment ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            logger.error("Error deleting enrollment: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Error deleting enrollment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        enrollmentIdField.setText("");
        studentComboBox.setSelectedItem(null); // Clear selection
        courseComboBox.setSelectedItem(null);  // Clear selection
        enrollmentDateField.setText("");
        gradeField.setText("");
        enrollmentTable.clearSelection(); // Deselect any selected row
    }
}
