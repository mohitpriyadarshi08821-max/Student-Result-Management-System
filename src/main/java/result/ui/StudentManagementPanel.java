package result.ui;

import result.dao.StudentDAOImpl;
import result.exception.DatabaseException;
import result.exception.DuplicateRecordException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Student;
import result.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Swing panel providing UI controls for managing student records
 * (Adding, updating, deleting, searching, and viewing students in a table).
 */
public class StudentManagementPanel extends JPanel {

    private final StudentService studentService;
    private DashboardFrame parentDashboard;

    // Form inputs
    private JTextField rollNumberField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<String> courseComboBox;
    private JComboBox<Integer> semesterComboBox;
    private JComboBox<String> genderComboBox;

    // Action buttons
    private RoundedButton saveButton;
    private RoundedButton updateButton;
    private RoundedButton deleteButton;
    private RoundedButton clearButton;
    private RoundedButton enterMarksButton;
    private RoundedButton viewResultButton;

    // Search bar
    private JTextField searchField;
    private RoundedButton searchButton;
    private RoundedButton refreshButton;

    // Table
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel totalCountBadge;

    // State tracking
    private int selectedStudentId = -1;

    public StudentManagementPanel() {
        this(new StudentService(new StudentDAOImpl()), null);
    }

    public StudentManagementPanel(StudentService studentService) {
        this(studentService, null);
    }

    public StudentManagementPanel(StudentService studentService, DashboardFrame parentDashboard) {
        this.studentService = studentService;
        this.parentDashboard = parentDashboard;
        initComponents();
        loadStudentData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Top Search Header
        JPanel topSearchPanel = createSearchPanel();
        add(topSearchPanel, BorderLayout.NORTH);

        // 2. Center Content: Left Form + Right Table
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        JPanel formPanel = createFormPanel();
        formPanel.setPreferredSize(new Dimension(420, 540));
        centerPanel.add(formPanel, BorderLayout.WEST);

        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(10, 0), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        panel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel titleLabel = new JLabel("Student Management Directory");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchControls.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UIConstants.FONT_BOLD);
        searchLabel.setForeground(UIConstants.TEXT_PRIMARY);

        searchField = new JTextField(16);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.setBorder(UIConstants.createInputBorder());

        searchButton = RoundedButton.createPrimary("Search");
        searchButton.setPreferredSize(new Dimension(95, 34));
        searchButton.addActionListener(e -> handleSearch());

        refreshButton = RoundedButton.createNeutral("Refresh");
        refreshButton.setPreferredSize(new Dimension(95, 34));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadStudentData();
        });

        // Press Enter in search field to search
        searchField.addActionListener(e -> handleSearch());

        searchControls.add(searchLabel);
        searchControls.add(searchField);
        searchControls.add(searchButton);
        searchControls.add(refreshButton);

        panel.add(searchControls, BorderLayout.EAST);
        return panel;
    }

    private JPanel createFormPanel() {
        RoundedPanel formCard = new RoundedPanel(new BorderLayout(0, 8), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        formCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Clean Header Title - No etched line ("bar bar") below it
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Student Details");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        formCard.add(headerPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 5, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Roll Number - Prominently at the top
        fieldsPanel.add(createFieldLabel("Roll Number *"), setGbc(gbc, 0));
        rollNumberField = createStyledTextField();
        rollNumberField.setToolTipText("Enter Unique Student Roll Number (e.g. CS2026-001)");
        fieldsPanel.add(rollNumberField, setGbc(gbc, 1));

        // 2. First Name & Last Name (Side by Side in 2 Columns)
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 10, 0));
        nameRow.setOpaque(false);

        JPanel fnPanel = new JPanel(new BorderLayout(0, 3));
        fnPanel.setOpaque(false);
        fnPanel.add(createFieldLabel("First Name *"), BorderLayout.NORTH);
        firstNameField = createStyledTextField();
        fnPanel.add(firstNameField, BorderLayout.CENTER);

        JPanel lnPanel = new JPanel(new BorderLayout(0, 3));
        lnPanel.setOpaque(false);
        lnPanel.add(createFieldLabel("Last Name *"), BorderLayout.NORTH);
        lastNameField = createStyledTextField();
        lnPanel.add(lastNameField, BorderLayout.CENTER);

        nameRow.add(fnPanel);
        nameRow.add(lnPanel);
        fieldsPanel.add(nameRow, setGbc(gbc, 2));

        // 3. Email Address
        fieldsPanel.add(createFieldLabel("Email Address"), setGbc(gbc, 3));
        emailField = createStyledTextField();
        emailField.setToolTipText("Enter Student Email Address (e.g. student@gmail.com)");
        fieldsPanel.add(emailField, setGbc(gbc, 4));

        // 4. Course / Department
        fieldsPanel.add(createFieldLabel("Course / Department *"), setGbc(gbc, 5));
        String[] courses = {
                "B.Tech Computer Science",
                "B.Tech Information Technology",
                "B.Tech Electronics & Comm",
                "BCA (Computer Applications)",
                "MCA (Master of Computer Apps)",
                "B.Sc Computer Science",
                "M.Tech Software Engineering"
        };
        courseComboBox = new JComboBox<>(courses);
        courseComboBox.setEditable(true);
        courseComboBox.setFont(UIConstants.FONT_REGULAR);
        courseComboBox.setPreferredSize(new Dimension(300, 32));
        courseComboBox.setBorder(UIConstants.createInputBorder());
        fieldsPanel.add(courseComboBox, setGbc(gbc, 6));

        // 5. Semester & Gender row (Side by Side in 2 Columns)
        JPanel comboRow = new JPanel(new GridLayout(1, 2, 10, 0));
        comboRow.setOpaque(false);

        JPanel semPanel = new JPanel(new BorderLayout(0, 3));
        semPanel.setOpaque(false);
        semPanel.add(createFieldLabel("Semester *"), BorderLayout.NORTH);
        Integer[] semesters = {1, 2, 3, 4, 5, 6, 7, 8};
        semesterComboBox = new JComboBox<>(semesters);
        semesterComboBox.setFont(UIConstants.FONT_REGULAR);
        semesterComboBox.setPreferredSize(new Dimension(130, 32));
        semesterComboBox.setBorder(UIConstants.createInputBorder());
        semPanel.add(semesterComboBox, BorderLayout.CENTER);

        JPanel genderPanel = new JPanel(new BorderLayout(0, 3));
        genderPanel.setOpaque(false);
        genderPanel.add(createFieldLabel("Gender"), BorderLayout.NORTH);
        String[] genders = {"Male", "Female", "Other"};
        genderComboBox = new JComboBox<>(genders);
        genderComboBox.setFont(UIConstants.FONT_REGULAR);
        genderComboBox.setPreferredSize(new Dimension(130, 32));
        genderComboBox.setBorder(UIConstants.createInputBorder());
        genderPanel.add(genderComboBox, BorderLayout.CENTER);

        comboRow.add(semPanel);
        comboRow.add(genderPanel);
        fieldsPanel.add(comboRow, setGbc(gbc, 7));

        // Add fieldsPanel directly without JScrollPane so there is ZERO scrolling up/down or left/right
        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons at bottom of form - Semi-rounded with high-contrast dark text
        JPanel bottomButtonsContainer = new JPanel(new BorderLayout(0, 8));
        bottomButtonsContainer.setOpaque(false);
        bottomButtonsContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsGrid.setOpaque(false);

        saveButton = RoundedButton.createSuccess("Save Student");
        saveButton.setPreferredSize(new Dimension(140, 34));
        saveButton.addActionListener(e -> handleSave());

        updateButton = RoundedButton.createPrimary("Update");
        updateButton.setPreferredSize(new Dimension(140, 34));
        updateButton.setEnabled(false);
        updateButton.addActionListener(e -> handleUpdate());

        deleteButton = RoundedButton.createDanger("Delete");
        deleteButton.setPreferredSize(new Dimension(140, 34));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> handleDelete());

        clearButton = RoundedButton.createNeutral("Clear Form");
        clearButton.setPreferredSize(new Dimension(140, 34));
        clearButton.addActionListener(e -> clearForm());

        buttonsGrid.add(saveButton);
        buttonsGrid.add(updateButton);
        buttonsGrid.add(deleteButton);
        buttonsGrid.add(clearButton);

        // Quick Navigation to Results
        JPanel quickNavPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        quickNavPanel.setOpaque(false);

        enterMarksButton = RoundedButton.createPrimary("📝 Enter Marks ➔");
        enterMarksButton.setPreferredSize(new Dimension(140, 34));
        enterMarksButton.setEnabled(false);
        enterMarksButton.addActionListener(e -> {
            String roll = rollNumberField.getText().trim();
            if (parentDashboard != null && !roll.isEmpty()) {
                parentDashboard.navigateToResultEntry(roll);
            }
        });

        viewResultButton = RoundedButton.createPrimary("📊 View Result ➔");
        viewResultButton.setPreferredSize(new Dimension(140, 34));
        viewResultButton.setEnabled(false);
        viewResultButton.addActionListener(e -> {
            String roll = rollNumberField.getText().trim();
            if (parentDashboard != null && !roll.isEmpty()) {
                parentDashboard.navigateToResultView(roll);
            }
        });

        quickNavPanel.add(enterMarksButton);
        quickNavPanel.add(viewResultButton);

        bottomButtonsContainer.add(buttonsGrid, BorderLayout.NORTH);
        bottomButtonsContainer.add(quickNavPanel, BorderLayout.SOUTH);

        formCard.add(bottomButtonsContainer, BorderLayout.SOUTH);
        return formCard;
    }

    private JPanel createTablePanel() {
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 10), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        tableCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Right Preview Section Header Bar
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setOpaque(false);

        JLabel tableTitle = new JLabel("Student Records Directory");
        tableTitle.setFont(UIConstants.FONT_HEADER);
        tableTitle.setForeground(UIConstants.TEXT_PRIMARY); // High contrast dark font

        totalCountBadge = new JLabel("Total: 0 Students", SwingConstants.CENTER);
        totalCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalCountBadge.setForeground(UIConstants.TEXT_PRIMARY); // High contrast dark font

        RoundedPanel countPill = new RoundedPanel(new BorderLayout(), UIConstants.CORNER_RADIUS_SM, new Color(241, 245, 249), new Color(203, 213, 225), 1);
        countPill.setBorder(new EmptyBorder(4, 12, 4, 12));
        countPill.add(totalCountBadge, BorderLayout.CENTER);

        tableHeaderBar.add(tableTitle, BorderLayout.WEST);
        tableHeaderBar.add(countPill, BorderLayout.EAST);
        tableCard.add(tableHeaderBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Roll Number", "First Name", "Last Name", "Email", "Course", "Sem", "Gender"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setFont(UIConstants.FONT_REGULAR);
        studentTable.setRowHeight(30);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setGridColor(new Color(226, 232, 240));
        studentTable.setShowGrid(true);
        studentTable.setSelectionBackground(new Color(219, 234, 254));
        studentTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Custom Table Header Renderer: High-contrast Dark Font with soft slate header background
        studentTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        studentTable.getTableHeader().setPreferredSize(new Dimension(studentTable.getTableHeader().getPreferredSize().width, 34));
        studentTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                label.setFont(UIConstants.FONT_BOLD);
                label.setBackground(new Color(241, 245, 249)); // Clean modern soft slate header bg
                label.setForeground(UIConstants.TEXT_PRIMARY); // High contrast dark font (never white)
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 1, new Color(203, 213, 225)),
                        new EmptyBorder(6, 6, 6, 6)
                ));
                return label;
            }
        });

        // Center align ID, Roll No, Sem, and Gender columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(6).setPreferredWidth(45);
        studentTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        studentTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // When a row is selected in the table, populate the form
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        return gbc;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(UIConstants.TEXT_PRIMARY);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.FONT_REGULAR);
        tf.setPreferredSize(new Dimension(300, 32));
        tf.setBorder(UIConstants.createInputBorder());
        return tf;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 36));
    }

    public void loadStudentData() {
        try {
            List<Student> students = studentService.getAllStudents();
            populateTable(students);
        } catch (DatabaseException ex) {
            System.err.println("[WARN] Database not available while loading students: " + ex.getMessage());
        }
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        if (students != null) {
            for (Student s : students) {
                tableModel.addRow(new Object[]{
                        s.getStudentId(),
                        s.getRollNumber(),
                        s.getFirstName(),
                        s.getLastName(),
                        s.getEmail() != null ? s.getEmail() : "",
                        s.getCourse(),
                        s.getSemester(),
                        s.getGender() != null ? s.getGender() : ""
                });
            }
        }
        if (totalCountBadge != null) {
            int count = (students != null) ? students.size() : 0;
            totalCountBadge.setText("Total: " + count + " Student" + (count == 1 ? "" : "s"));
        }
    }

    private void populateFormFromSelectedRow() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow >= 0) {
            int row = studentTable.convertRowIndexToModel(viewRow);
            Object idVal = tableModel.getValueAt(row, 0);
            selectedStudentId = idVal instanceof Number ? ((Number) idVal).intValue() : Integer.parseInt(idVal.toString());

            Object rollVal = tableModel.getValueAt(row, 1);
            rollNumberField.setText(rollVal != null ? rollVal.toString() : "");

            Object fnVal = tableModel.getValueAt(row, 2);
            firstNameField.setText(fnVal != null ? fnVal.toString() : "");

            Object lnVal = tableModel.getValueAt(row, 3);
            lastNameField.setText(lnVal != null ? lnVal.toString() : "");

            Object emVal = tableModel.getValueAt(row, 4);
            emailField.setText(emVal != null ? emVal.toString() : "");

            Object course = tableModel.getValueAt(row, 5);
            if (course != null) {
                courseComboBox.setSelectedItem(course.toString());
            }

            Object semVal = tableModel.getValueAt(row, 6);
            if (semVal instanceof Number) {
                semesterComboBox.setSelectedItem(((Number) semVal).intValue());
            } else if (semVal != null) {
                try {
                    semesterComboBox.setSelectedItem(Integer.parseInt(semVal.toString()));
                } catch (Exception ignored) {
                }
            }

            Object gender = tableModel.getValueAt(row, 7);
            if (gender != null) {
                genderComboBox.setSelectedItem(gender.toString());
            }

            saveButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            enterMarksButton.setEnabled(true);
            viewResultButton.setEnabled(true);
        }
    }

    private void handleSave() {
        try {
            Student student = buildStudentFromForm();
            boolean success = studentService.registerStudent(student);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Student registered successfully!\nRoll Number: " + student.getRollNumber(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudentData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save student record.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException | DuplicateRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = buildStudentFromForm();
            student.setStudentId(selectedStudentId);

            boolean success = studentService.updateStudent(student);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Student record updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudentData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update student record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException | RecordNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rollNo = rollNumberField.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student with Roll Number '" + rollNo + "'?\n" +
                        "This will also permanently delete all associated exam results.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = studentService.deleteStudent(selectedStudentId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudentData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RecordNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = searchField.getText().trim();
        try {
            List<Student> results = studentService.searchStudents(query);
            populateTable(results);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students found matching: " + query,
                        "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        selectedStudentId = -1;
        rollNumberField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        if (courseComboBox.getItemCount() > 0) courseComboBox.setSelectedIndex(0);
        if (semesterComboBox.getItemCount() > 0) semesterComboBox.setSelectedIndex(0);
        if (genderComboBox.getItemCount() > 0) genderComboBox.setSelectedIndex(0);

        studentTable.clearSelection();
        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        enterMarksButton.setEnabled(false);
        viewResultButton.setEnabled(false);
        rollNumberField.requestFocusInWindow();
    }

    private Student buildStudentFromForm() {
        Student student = new Student();
        student.setRollNumber(rollNumberField.getText().trim());
        student.setFirstName(firstNameField.getText().trim());
        student.setLastName(lastNameField.getText().trim());
        student.setEmail(emailField.getText().trim());

        Object selectedCourse = courseComboBox.getSelectedItem();
        student.setCourse(selectedCourse != null ? selectedCourse.toString().trim() : "");

        Object selectedSem = semesterComboBox.getSelectedItem();
        student.setSemester(selectedSem instanceof Integer ? (Integer) selectedSem : 1);

        Object selectedGender = genderComboBox.getSelectedItem();
        student.setGender(selectedGender != null ? selectedGender.toString() : "Male");

        return student;
    }

    public void setParentDashboard(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
    }
}
