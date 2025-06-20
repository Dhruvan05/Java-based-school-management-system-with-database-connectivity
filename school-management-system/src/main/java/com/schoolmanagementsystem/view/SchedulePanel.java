package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Panel for scheduling management
 */
public class SchedulePanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(SchedulePanel.class);

    private final SchedulingService schedulingService;
    private JLabel statsLabel;

    public SchedulePanel(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Schedule Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        statsLabel = new JLabel("Loading statistics...");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(statsLabel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("Generate Optimal Schedule");
        generateButton.addActionListener(e -> generateSchedule());
        buttonPanel.add(generateButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Object> stats = schedulingService.getSchedulingStatistics();
                updateStatistics(stats);
            } catch (Exception e) {
                logger.error("Error refreshing schedule data", e);
                statsLabel.setText("Error loading statistics: " + e.getMessage());
            }
        });
    }

    private void updateStatistics(Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<h3>Scheduling Statistics</h3>");
        sb.append("<p><b>Total Courses:</b> ").append(stats.get("totalCourses")).append("</p>");
        sb.append("<p><b>Total Time Slots:</b> ").append(stats.get("totalTimeSlots")).append("</p>");
        sb.append("<p><b>Scheduled Courses:</b> ").append(stats.get("scheduledCourses")).append("</p>");
        sb.append("<p><b>Unscheduled Courses:</b> ").append(stats.get("unscheduledCourses")).append("</p>");
        sb.append("<p><b>Utilization Rate:</b> ").append(String.format("%.1f%%", stats.get("utilizationRate"))).append("</p>");
        sb.append("<p><b>Has Conflicts:</b> ").append(stats.get("hasConflicts")).append("</p>");
        sb.append("</html>");

        statsLabel.setText(sb.toString());
    }

    private void generateSchedule() {
        SwingUtilities.invokeLater(() -> {
            try {
                schedulingService.generateOptimalSchedule();
                refreshData();
                JOptionPane.showMessageDialog(this, "Schedule generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                logger.error("Error generating schedule", e);
                JOptionPane.showMessageDialog(this, "Error generating schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}