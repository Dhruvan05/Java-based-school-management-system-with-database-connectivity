package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.algorithm.graph.StudentCourseGraph;
import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.model.Course;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.List;

/**
 * Dialog for showing graph analysis results, now shows courses for selected student.
 */
public class GraphAnalysisDialog extends JDialog {
    private final StudentCourseGraph graph;
    private JComboBox<Student> studentComboBox;
    private JTextArea resultArea;

    public GraphAnalysisDialog(Frame parent, StudentCourseGraph graph) {
        super(parent, "Student-Course Relationship Analysis", true);
        this.graph = graph;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(getParent());

        JLabel titleLabel = new JLabel("Student-Course Enrollment Viewer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Student selection combo box
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Student:"));

        List<Student> studentList = graph.getAllStudents();
        studentComboBox = new JComboBox<>(studentList.toArray(new Student[0]));
        studentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Student) {
                    value = ((Student) value).getFullName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        studentComboBox.addActionListener(e -> updateResultArea());
        topPanel.add(studentComboBox);

        add(topPanel, BorderLayout.PAGE_START);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (studentList.size() > 0) {
            studentComboBox.setSelectedIndex(0);
            updateResultArea();
        } else {
            resultArea.setText("No students in the graph.");
        }
    }

    private void updateResultArea() {
        Student selectedStudent = (Student) studentComboBox.getSelectedItem();
        if (selectedStudent == null) {
            resultArea.setText("No student selected.");
            return;
        }
        Set<Integer> courseIds = graph.getStudentCourses(selectedStudent.getStudentId());
        if (courseIds.isEmpty()) {
            resultArea.setText(selectedStudent.getFullName() + " is not enrolled in any courses.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(selectedStudent.getFullName()).append(" is enrolled in:\n\n");
        for (Integer courseId : courseIds) {
            Course course = graph.getCourse(courseId);
            if (course != null) {
                sb.append("- ").append(course.getCourseName()).append("\n");
            }
        }
        resultArea.setText(sb.toString());
    }
}