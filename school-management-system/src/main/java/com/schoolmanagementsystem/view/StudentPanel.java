package com.schoolmanagementsystem.view;

import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel for managing students
 */
public class StudentPanel extends JPanel implements MainWindow.RefreshablePanel {
    private static final Logger logger = LoggerFactory.getLogger(StudentPanel.class);

    private final StudentService studentService;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField dobField;
    private JTextField addressField;
    private JTextField phoneField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    private Student selectedStudent;

    public StudentPanel(StudentService studentService) {
        this.studentService = studentService;
        initializeComponents();
        setupEventHandlers();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Create components
        createSearchPanel();
        createTablePanel();
        createFormPanel();
    }

    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Students"));

        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        searchPanel.add(refreshButton);

        add(searchPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        String[] columns = {"ID", "First Name", "Last Name", "Email", "Date of Birth", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedStudent();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gbc = new GridBagConstraints();

        // Create form fields
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        formPanel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        formPanel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dobField = new JTextField(20);
        formPanel.add(dobField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(20);
        formPanel.add(addressField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        addButton = new JButton("Add Student");
        updateButton = new JButton("Update Student");
        deleteButton = new JButton("Delete Student");
        clearButton = new JButton("Clear Form");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());

        // Enable search on Enter key
        searchField.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        try {
            String searchTerm = searchField.getText().trim();
            List<Student> students = studentService.searchStudents(searchTerm);
            updateTable(students);
        } catch (Exception e) {
            logger.error("Error searching students", e);
            showErrorMessage("Error searching students: " + e.getMessage());
        }
    }

    @Override
    public void refreshData() {
        try {
            List<Student> students = studentService.getAllStudents();
            updateTable(students);
        } catch (Exception e) {
            logger.error("Error refreshing student data", e);
            showErrorMessage("Error loading students: " + e.getMessage());
        }
    }

    private void updateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student student : students) {
            Object[] row = {
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getDateOfBirth() != null ? student.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                student.getPhoneNumber()
            };
            tableModel.addRow(row);
        }
    }

    private void loadSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
                selectedStudent = studentService.getStudentById(studentId);
                if (selectedStudent != null) {
                    populateForm(selectedStudent);
                }
            } catch (Exception e) {
                logger.error("Error loading selected student", e);
                showErrorMessage("Error loading student details: " + e.getMessage());
            }
        }
    }

    private void populateForm(Student student) {
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        emailField.setText(student.getEmail());
        dobField.setText(student.getDateOfBirth() != null ? 
            student.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        addressField.setText(student.getAddress());
        phoneField.setText(student.getPhoneNumber());
    }

    private void addStudent() {
        try {
            Student student = createStudentFromForm();
            studentService.saveStudent(student);
            refreshData();
            clearForm();
            showSuccessMessage("Student added successfully!");
        } catch (Exception e) {
            logger.error("Error adding student", e);
            showErrorMessage("Error adding student: " + e.getMessage());
        }
    }

    private void updateStudent() {
        if (selectedStudent == null) {
            showErrorMessage("Please select a student to update.");
            return;
        }

        try {
            Student student = createStudentFromForm();
            student.setStudentId(selectedStudent.getStudentId());
            studentService.saveStudent(student);
            refreshData();
            clearForm();
            showSuccessMessage("Student updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating student", e);
            showErrorMessage("Error updating student: " + e.getMessage());
        }
    }

    private void deleteStudent() {
        if (selectedStudent == null) {
            showErrorMessage("Please select a student to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + selectedStudent.getFullName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(selectedStudent.getStudentId());
                refreshData();
                clearForm();
                showSuccessMessage("Student deleted successfully!");
            } catch (Exception e) {
                logger.error("Error deleting student", e);
                showErrorMessage("Error deleting student: " + e.getMessage());
            }
        }
    }

    private Student createStudentFromForm() throws Exception {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String dobText = dobField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        LocalDate dateOfBirth = null;
        if (!dobText.isEmpty()) {
            try {
                dateOfBirth = LocalDate.parse(dobText, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new Exception("Invalid date format. Please use YYYY-MM-DD format.");
            }
        }

        return new Student(firstName, lastName, email, dateOfBirth, address, phone);
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        dobField.setText("");
        addressField.setText("");
        phoneField.setText("");
        selectedStudent = null;
        studentTable.clearSelection();
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}