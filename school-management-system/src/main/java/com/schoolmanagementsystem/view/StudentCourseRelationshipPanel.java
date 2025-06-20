package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.dao.StudentDAO;
import com.schoolmanagementsystem.dao.CourseDAO;
import com.schoolmanagementsystem.dao.EnrollmentDAO;
import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel for visualizing student-course relationships as a graph.
 * This is a simplified graphical representation using AWT/Swing drawing.
 */
public class StudentCourseRelationshipPanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(StudentCourseRelationshipPanel.class);

    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;

    private List<Student> students;
    private List<Course> courses;
    private List<Enrollment> enrollments;

    // Maps to hold positions for drawing
    private Map<Integer, Point> studentPositions;
    private Map<Integer, Point> coursePositions;
    private int studentNodeRadius = 20;
    private int courseNodeWidth = 80;
    private int courseNodeHeight = 40;

    public StudentCourseRelationshipPanel() {
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentPositions = new HashMap<>();
        this.coursePositions = new HashMap<>();
        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Student-Course Relationships (Graph View)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // This custom JPanel will handle the drawing of the graph
        GraphCanvas graphCanvas = new GraphCanvas();
        JScrollPane scrollPane = new JScrollPane(graphCanvas);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                students = studentDAO.findAll();
                courses = courseDAO.findAll();
                enrollments = enrollmentDAO.findAll();
                logger.debug("Loaded {} students, {} courses, {} enrollments for graph.",
                        students.size(), courses.size(), enrollments.size());

                // Recalculate positions after data refresh
                calculateNodePositions();
                repaint(); // Redraw the graph
            } catch (SQLException e) {
                logger.error("Error refreshing student-course relationship data: {}", e.getMessage());
                JOptionPane.showMessageDialog(this, "Error loading relationship data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void calculateNodePositions() {
        studentPositions.clear();
        coursePositions.clear();

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Ensure minimum dimensions for layout if panel not yet rendered
        if (panelWidth == 0 || panelHeight == 0) {
            panelWidth = 800; // Default width
            panelHeight = 600; // Default height
        }

        int studentAreaWidth = panelWidth / 2;
        int courseAreaWidth = panelWidth / 2;

        int studentYPadding = 50;
        int courseYPadding = 50;

        // Position students on the left side
        if (!students.isEmpty()) {
            int studentSpacing = (panelHeight - 2 * studentYPadding) / Math.max(1, students.size() - 1);
            for (int i = 0; i < students.size(); i++) {
                int x = studentAreaWidth / 4; // Closer to the left edge of student area
                int y = studentYPadding + i * studentSpacing;
                studentPositions.put(students.get(i).getStudentId(), new Point(x, y));
            }
        }

        // Position courses on the right side
        if (!courses.isEmpty()) {
            int courseSpacing = (panelHeight - 2 * courseYPadding) / Math.max(1, courses.size() - 1);
            for (int i = 0; i < courses.size(); i++) {
                int x = studentAreaWidth + courseAreaWidth * 3 / 4; // Closer to the right edge of course area
                int y = courseYPadding + i * courseSpacing;
                coursePositions.put(courses.get(i).getCourseId(), new Point(x, y));
            }
        }
    }

    private class GraphCanvas extends JPanel {
        public GraphCanvas() {
            // Set preferred size for scroll pane to work
            setPreferredSize(new Dimension(800, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Recalculate positions to adjust to actual panel size
            calculateNodePositions();

            // Draw Enrollments (Edges)
            g2d.setColor(new Color(100, 100, 255, 150)); // Light blue with transparency
            g2d.setStroke(new BasicStroke(1.5f));
            for (Enrollment enrollment : enrollments) {
                Point studentPos = studentPositions.get(enrollment.getStudentId());
                Point coursePos = coursePositions.get(enrollment.getCourseId());

                if (studentPos != null && coursePos != null) {
                    g2d.drawLine(studentPos.x + studentNodeRadius, studentPos.y, // From right edge of student node
                            coursePos.x - courseNodeWidth / 2, coursePos.y); // To left edge of course node
                }
            }

            // Draw Students (Nodes)
            g2d.setColor(new Color(255, 150, 150)); // Reddish color
            for (Student student : students) {
                Point pos = studentPositions.get(student.getStudentId());
                if (pos != null) {
                    g2d.fillOval(pos.x - studentNodeRadius, pos.y - studentNodeRadius,
                            2 * studentNodeRadius, 2 * studentNodeRadius);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(pos.x - studentNodeRadius, pos.y - studentNodeRadius,
                            2 * studentNodeRadius, 2 * studentNodeRadius); // Outline
                    g2d.setColor(Color.WHITE);
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = student.getFirstName(); // Short name for display
                    int x = pos.x - fm.stringWidth(name) / 2;
                    int y = pos.y + fm.getAscent() / 2;
                    g2d.drawString(name, x, y);
                }
            }

            // Draw Courses (Nodes)
            g2d.setColor(new Color(150, 255, 150)); // Greenish color
            for (Course course : courses) {
                Point pos = coursePositions.get(course.getCourseId());
                if (pos != null) {
                    g2d.fillRect(pos.x - courseNodeWidth / 2, pos.y - courseNodeHeight / 2,
                            courseNodeWidth, courseNodeHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(pos.x - courseNodeWidth / 2, pos.y - courseNodeHeight / 2,
                            courseNodeWidth, courseNodeHeight); // Outline
                    g2d.setColor(Color.BLACK); // Text color for courses
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = course.getCourseCode(); // Use course code for brevity
                    int x = pos.x - fm.stringWidth(name) / 2;
                    int y = pos.y + fm.getAscent() / 2;
                    g2d.drawString(name, x, y);
                }
            }
        }
    }
}
