package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.algorithm.graph.StudentCourseGraph;
import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.Enrollment;
import com.schoolmanagementsystem.service.StudentService;
import com.schoolmanagementsystem.service.EnrollmentService;
import com.schoolmanagementsystem.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Main application window with tabbed interface
 */
public class MainWindow extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private final StudentService studentService;
    private final SchedulingService schedulingService;
    private final EnrollmentService enrollmentService;
    private final StudentCourseGraph studentCourseGraph;

    private JTabbedPane tabbedPane;
    private StudentPanel studentPanel;
    private CoursePanel coursePanel;
    private EnrollmentPanel enrollmentPanel;
    private SchedulePanel schedulePanel;

    public MainWindow() {
        this.studentService = new StudentService();
        this.schedulingService = new SchedulingService();
        this.enrollmentService = new EnrollmentService();
        this.studentCourseGraph = new StudentCourseGraph();

        initializeComponents();
        setupEventHandlers();

        logger.info("Main window initialized");
    }

    private void initializeComponents() {
        setTitle("School Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Create menu bar
        createMenuBar();

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Create panels
        studentPanel = new StudentPanel(studentService);
        coursePanel = new CoursePanel();
        enrollmentPanel = new EnrollmentPanel();
        schedulePanel = new SchedulePanel(schedulingService);

        // Add tabs
        tabbedPane.addTab("👨‍🎓 Students", null, studentPanel, "Manage student records");
        tabbedPane.addTab("📚 Courses", null, coursePanel, "Manage courses and curriculum");
        tabbedPane.addTab("📝 Enrollment", null, enrollmentPanel, "Manage student enrollments");
        tabbedPane.addTab("📅 Schedule", null, schedulePanel, "Course scheduling and timetables");

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleExit());
        fileMenu.add(exitItem);

        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem analyzeGraphItem = new JMenuItem("Analyze Student Relationships");
        analyzeGraphItem.addActionListener(e -> analyzeStudentRelationships());
        toolsMenu.add(analyzeGraphItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);

        return statusBar;
    }

    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // Tab change listener for refreshing data
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                Component selectedComponent = tabbedPane.getComponentAt(selectedIndex);
                if (selectedComponent instanceof RefreshablePanel) {
                    ((RefreshablePanel) selectedComponent).refreshData();
                }
            }
        });
    }

    private void analyzeStudentRelationships() {
        SwingUtilities.invokeLater(() -> {
            try {
                populateStudentCourseGraph();
                // Show graph analysis dialog
                GraphAnalysisDialog dialog = new GraphAnalysisDialog(this, studentCourseGraph);
                dialog.setVisible(true);
            } catch (Exception e) {
                logger.error("Error showing graph analysis", e);
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
                        "Graph Analysis Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Populates the studentCourseGraph with all students, courses, and enrollments from the database.
     * Call this before showing the analysis dialog to ensure latest data.
     */
    private void populateStudentCourseGraph() {
        studentCourseGraph.clear();
        try {
            List<Student> students = studentService.getAllStudents();
            List<Course> courses = studentService.getAllCourses();
            List<Enrollment> enrollments = enrollmentService.getAllEnrollments();

            for (Student student : students) {
                studentCourseGraph.addStudent(student);
            }
            for (Course course : courses) {
                studentCourseGraph.addCourse(course);
            }
            for (Enrollment enrollment : enrollments) {
                studentCourseGraph.addEnrollment(enrollment);
            }
            logger.info("StudentCourseGraph populated: {} students, {} courses, {} enrollments",
                    studentCourseGraph.getStudentCount(),
                    studentCourseGraph.getCourseCount(),
                    studentCourseGraph.getEnrollmentCount());
        } catch (Exception ex) {
            logger.error("Failed to populate StudentCourseGraph", ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to load student, course, or enrollment data:\n" + ex.getMessage(),
                    "Data Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAboutDialog() {
        String message = "School Management System\n" +
                "Version 1.0\n\n" +
                "A comprehensive Java application for managing\n" +
                "educational institutions with relationship analysis.\n\n" +
                "Features:\n" +
                "• Student and Course Management\n" +
                "• Graph-based Relationship Analysis\n" +
                "• MySQL Database Integration";

        JOptionPane.showMessageDialog(this,
                message,
                "About School Management System",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            logger.info("Application shutting down");
            dispose();
            System.exit(0);
        }
    }

    public interface RefreshablePanel {
        void refreshData();
    }
}