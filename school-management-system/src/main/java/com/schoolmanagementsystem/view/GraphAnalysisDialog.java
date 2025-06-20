package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.algorithm.graph.StudentCourseGraph;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for showing graph analysis results
 */
public class GraphAnalysisDialog extends JDialog {
    private final StudentCourseGraph graph;

    public GraphAnalysisDialog(Frame parent, StudentCourseGraph graph) {
        super(parent, "Student-Course Relationship Analysis", true);
        this.graph = graph;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(getParent());

        JLabel titleLabel = new JLabel("Graph Analysis Results", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JTextArea analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder analysis = new StringBuilder();
        analysis.append("Student-Course Graph Statistics:\n\n");
        analysis.append("Total Students: ").append(graph.getStudentCount()).append("\n");
        analysis.append("Total Courses: ").append(graph.getCourseCount()).append("\n");
        analysis.append("Total Enrollments: ").append(graph.getEnrollmentCount()).append("\n\n");
        analysis.append("Graph analysis features:\n");
        analysis.append("• Student similarity detection using Jaccard coefficient\n");
        analysis.append("• Connected components analysis\n");
        analysis.append("• Course recommendation system\n");
        analysis.append("• Bipartite graph representation\n");

        analysisArea.setText(analysis.toString());

        JScrollPane scrollPane = new JScrollPane(analysisArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}