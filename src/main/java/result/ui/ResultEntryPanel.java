package result.ui;

import result.dao.ResultDAOImpl;
import result.dao.StudentDAOImpl;
import result.dao.SubjectDAOImpl;
import result.exception.DatabaseException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Result;
import result.model.Student;
import result.model.Subject;
import result.service.ResultService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Swing panel providing UI controls for recording and updating subject-wise marks
 * for enrolled students.
 */
public class ResultEntryPanel extends JPanel {

    private final ResultService resultService;
    private final StudentService studentService;

    // Student verification
    private JTextField studentRollField;
    private RoundedButton verifyStudentButton;
    private JLabel studentInfoLabel;
    private Student currentStudent;

    // Marks entry controls
    private JComboBox<SubjectComboBoxItem> subjectComboBox;
    private JLabel maxMarksDisplayLabel;
    private JLabel passMarksDisplayLabel;
    private JTextField marksField;
    private RoundedButton submitMarksButton;
    private RoundedButton clearButton;

    // Table of current results for student
    private JTable studentResultsTable;
    private DefaultTableModel resultsTableModel;
    private RoundedButton deleteResultButton;

    // Cached subjects
    private List<Subject> subjectList = new ArrayList<>();

    public ResultEntryPanel() {
        this(
                new ResultService(new ResultDAOImpl(), new StudentDAOImpl(), new SubjectDAOImpl()),
                new StudentService(new StudentDAOImpl())
        );
    }

    public ResultEntryPanel(ResultService resultService, StudentService studentService) {
        this.resultService = resultService;
        this.studentService = studentService;
        initComponents();
        loadSubjects();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Top Section: Student Verification Card
        JPanel topPanel = createStudentLookupPanel();
        add(topPanel, BorderLayout.NORTH);

        // 2. Center Section: Left Entry Form + Right Current Marks Table
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        JPanel formCard = createMarksFormPanel();
        formCard.setPreferredSize(new Dimension(420, 400));
        centerPanel.add(formCard, BorderLayout.WEST);

        JPanel tableCard = createExistingResultsPanel();
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStudentLookupPanel() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(10, 8), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        inputRow.setOpaque(false);

        JLabel label = new JLabel("Enter Student Roll Number:");
        label.setFont(UIConstants.FONT_BOLD);
        label.setForeground(UIConstants.TEXT_PRIMARY);

        studentRollField = new JTextField(15);
        studentRollField.setFont(UIConstants.FONT_REGULAR);
        studentRollField.setPreferredSize(new Dimension(170, 34));
        studentRollField.setBorder(UIConstants.createInputBorder());
        studentRollField.addActionListener(e -> handleVerifyStudent());

        verifyStudentButton = RoundedButton.createPrimary("Verify Student");
        verifyStudentButton.setPreferredSize(new Dimension(135, 34));
        verifyStudentButton.addActionListener(e -> handleVerifyStudent());

        inputRow.add(label);
        inputRow.add(studentRollField);
        inputRow.add(verifyStudentButton);

        studentInfoLabel = new JLabel("Please enter a roll number and click Verify Student.");
        studentInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        studentInfoLabel.setForeground(new Color(110, 120, 130));

        card.add(inputRow, BorderLayout.NORTH);
        card.add(studentInfoLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMarksFormPanel() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Clean Header Title - No etched line ("bar bar") below it
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Subject Marks Entry");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        card.add(headerPanel, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Subject selector
        formGrid.add(createFieldLabel("Select Subject *"), setGbc(gbc, 0));
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(UIConstants.FONT_REGULAR);
        subjectComboBox.setPreferredSize(new Dimension(320, 34));
        subjectComboBox.setBorder(UIConstants.createInputBorder());
        subjectComboBox.addActionListener(e -> updateSubjectMetrics());
        formGrid.add(subjectComboBox, setGbc(gbc, 1));

        // Metric displays (Max marks & passing marks)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        metricsPanel.setOpaque(false);

        JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        maxPanel.setOpaque(false);
        JLabel maxLbl = new JLabel("Max Marks:");
        maxLbl.setFont(UIConstants.FONT_REGULAR);
        maxMarksDisplayLabel = new JLabel("100");
        maxMarksDisplayLabel.setFont(UIConstants.FONT_BOLD);
        maxMarksDisplayLabel.setForeground(UIConstants.PRIMARY_COLOR);
        maxPanel.add(maxLbl);
        maxPanel.add(maxMarksDisplayLabel);

        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        passPanel.setOpaque(false);
        JLabel passLbl = new JLabel("Pass Marks:");
        passLbl.setFont(UIConstants.FONT_REGULAR);
        passMarksDisplayLabel = new JLabel("35");
        passMarksDisplayLabel.setFont(UIConstants.FONT_BOLD);
        passMarksDisplayLabel.setForeground(UIConstants.ACCENT_COLOR);
        passPanel.add(passLbl);
        passPanel.add(passMarksDisplayLabel);

        metricsPanel.add(maxPanel);
        metricsPanel.add(passPanel);
        formGrid.add(metricsPanel, setGbc(gbc, 2));

        // Marks Obtained Input
        formGrid.add(createFieldLabel("Marks Obtained *"), setGbc(gbc, 3));
        marksField = new JTextField();
        marksField.setFont(new Font("Segoe UI", Font.BOLD, 15));
        marksField.setPreferredSize(new Dimension(320, 36));
        marksField.setBorder(UIConstants.createInputBorder());
        formGrid.add(marksField, setGbc(gbc, 4));

        card.add(formGrid, BorderLayout.CENTER);

        // Buttons - Semi-rounded with high-contrast dark text
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        submitMarksButton = RoundedButton.createSuccess("Save / Update Marks");
        submitMarksButton.setPreferredSize(new Dimension(160, 38));
        submitMarksButton.addActionListener(e -> handleSubmitMarks());

        clearButton = RoundedButton.createNeutral("Clear");
        clearButton.setPreferredSize(new Dimension(100, 38));
        clearButton.addActionListener(e -> {
            marksField.setText("");
            marksField.requestFocusInWindow();
        });

        buttonRow.add(submitMarksButton);
        buttonRow.add(clearButton);
        card.add(buttonRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createExistingResultsPanel() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Clean Header Title - No etched line ("bar bar") below it
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Recorded Subject Results for Selected Student");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        card.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Result ID", "Subject Code", "Subject Name", "Max Marks", "Marks", "Grade", "Status"};
        resultsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        studentResultsTable = new JTable(resultsTableModel);
        studentResultsTable.setFont(UIConstants.FONT_REGULAR);
        studentResultsTable.setRowHeight(30);
        studentResultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentResultsTable.setGridColor(new Color(226, 232, 240));
        studentResultsTable.setShowGrid(true);
        studentResultsTable.setSelectionBackground(new Color(219, 234, 254));
        studentResultsTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Custom Table Header Renderer: High-contrast Dark Font with soft slate header background
        studentResultsTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        studentResultsTable.getTableHeader().setPreferredSize(new Dimension(studentResultsTable.getTableHeader().getPreferredSize().width, 34));
        studentResultsTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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

        // Center alignments
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        studentResultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentResultsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        studentResultsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        studentResultsTable.getColumnModel().getColumn(1).setCellRenderer(center);
        studentResultsTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        studentResultsTable.getColumnModel().getColumn(3).setCellRenderer(center);
        studentResultsTable.getColumnModel().getColumn(4).setPreferredWidth(65);
        studentResultsTable.getColumnModel().getColumn(4).setCellRenderer(center);
        studentResultsTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        studentResultsTable.getColumnModel().getColumn(5).setCellRenderer(center);

        // Custom status renderer (Green PASS, Red FAIL)
        studentResultsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UIConstants.FONT_BOLD);
                if ("PASS".equals(value)) {
                    setForeground(new Color(39, 174, 96));
                } else if ("FAIL".equals(value)) {
                    setForeground(UIConstants.DANGER_COLOR);
                }
                return c;
            }
        });

        // When a row is selected in the results table, auto-populate subject and marks in the form
        studentResultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentResultsTable.getSelectedRow() != -1) {
                int viewRow = studentResultsTable.getSelectedRow();
                int row = studentResultsTable.convertRowIndexToModel(viewRow);
                String code = String.valueOf(resultsTableModel.getValueAt(row, 1));
                Object marks = resultsTableModel.getValueAt(row, 4);
                if (marks != null) {
                    marksField.setText(marks.toString());
                }
                for (int i = 0; i < subjectComboBox.getItemCount(); i++) {
                    SubjectComboBoxItem item = subjectComboBox.getItemAt(i);
                    if (item != null && item.getSubject() != null && code.equalsIgnoreCase(item.getSubject().getSubjectCode())) {
                        subjectComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(studentResultsTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        // Bottom action: Delete entry
        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomAction.setOpaque(false);
        deleteResultButton = RoundedButton.createDanger("Delete Selected Result");
        deleteResultButton.setPreferredSize(new Dimension(190, 34));
        deleteResultButton.addActionListener(e -> handleDeleteResult());
        bottomAction.add(deleteResultButton);
        card.add(bottomAction, BorderLayout.SOUTH);

        return card;
    }

    public void setStudentRoll(String rollNumber) {
        if (rollNumber != null) {
            studentRollField.setText(rollNumber.trim());
            handleVerifyStudent();
        }
    }

    private void handleVerifyStudent() {
        String roll = studentRollField.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a student Roll Number.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentStudent = studentService.getStudentByRollNumber(roll);
            studentInfoLabel.setText("Student: " + currentStudent.getFullName() +
                    " | Course: " + currentStudent.getCourse() +
                    " | Semester: " + currentStudent.getSemester());
            studentInfoLabel.setForeground(new Color(39, 174, 96));

            loadCurrentStudentResults();
            marksField.requestFocusInWindow();
        } catch (RecordNotFoundException ex) {
            currentStudent = null;
            studentInfoLabel.setText("❌ " + ex.getMessage());
            studentInfoLabel.setForeground(UIConstants.DANGER_COLOR);
            resultsTableModel.setRowCount(0);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmitMarks() {
        if (currentStudent == null) {
            handleVerifyStudent();
            if (currentStudent == null) {
                return;
            }
        }

        SubjectComboBoxItem selectedItem = (SubjectComboBoxItem) subjectComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an academic subject.",
                    "Missing Subject", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String marksStr = marksField.getText().trim();
        if (marksStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter marks obtained.",
                    "Missing Marks", JOptionPane.WARNING_MESSAGE);
            marksField.requestFocusInWindow();
            return;
        }

        double marks;
        try {
            marks = Double.parseDouble(marksStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for marks.",
                    "Invalid Format", JOptionPane.WARNING_MESSAGE);
            marksField.requestFocusInWindow();
            return;
        }

        // Validate marks against subject max marks
        Subject subject = selectedItem.getSubject();
        if (marks < 0) {
            JOptionPane.showMessageDialog(this, "Marks cannot be negative.",
                    "Invalid Marks", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (marks > subject.getMaxMarks()) {
            JOptionPane.showMessageDialog(this,
                    "Marks (" + marks + ") exceed the maximum allowed marks (" + subject.getMaxMarks() + ") for " + subject.getSubjectName() + ".",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            marksField.requestFocusInWindow();
            return;
        }

        Result result = new Result();
        result.setStudentId(currentStudent.getStudentId());
        result.setSubjectId(subject.getSubjectId());
        result.setMarksObtained(marks);

        try {
            boolean success = resultService.recordResult(result);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Marks recorded successfully for " + subject.getSubjectName() + "!\n" +
                                "Marks: " + marks + " / " + subject.getMaxMarks() + " | Grade: " + result.getGrade(),
                        "Result Saved", JOptionPane.INFORMATION_MESSAGE);
                marksField.setText("");
                loadCurrentStudentResults();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record result.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteResult() {
        int viewRow = studentResultsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a result row from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = studentResultsTable.convertRowIndexToModel(viewRow);
        Object idVal = resultsTableModel.getValueAt(row, 0);
        int resultId = idVal instanceof Number ? ((Number) idVal).intValue() : Integer.parseInt(idVal.toString());
        String subjectName = String.valueOf(resultsTableModel.getValueAt(row, 2));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the result for '" + subjectName + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = resultService.deleteResult(resultId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Result deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCurrentStudentResults();
                }
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCurrentStudentResults() {
        if (currentStudent == null) {
            resultsTableModel.setRowCount(0);
            return;
        }

        resultsTableModel.setRowCount(0);
        try {
            List<Result> results = resultService.getResultsByStudentId(currentStudent.getStudentId());
            for (Result r : results) {
                Subject s = r.getSubject();
                int max = s != null ? s.getMaxMarks() : 100;
                int pass = s != null ? s.getPassMarks() : 35;
                String status = r.getMarksObtained() >= pass ? "PASS" : "FAIL";

                resultsTableModel.addRow(new Object[]{
                        r.getResultId(),
                        s != null ? s.getSubjectCode() : "N/A",
                        s != null ? s.getSubjectName() : "N/A",
                        max,
                        r.getMarksObtained(),
                        r.getGrade(),
                        status
                });
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load student results: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadSubjects() {
        try {
            subjectList = resultService.getAllSubjects();
            subjectComboBox.removeAllItems();
            for (Subject s : subjectList) {
                subjectComboBox.addItem(new SubjectComboBoxItem(s));
            }
            updateSubjectMetrics();
        } catch (DatabaseException ex) {
            // Quietly suppress or log
        }
    }

    private void updateSubjectMetrics() {
        SubjectComboBoxItem selected = (SubjectComboBoxItem) subjectComboBox.getSelectedItem();
        if (selected != null && selected.getSubject() != null) {
            maxMarksDisplayLabel.setText(String.valueOf(selected.getSubject().getMaxMarks()));
            passMarksDisplayLabel.setText(String.valueOf(selected.getSubject().getPassMarks()));
        } else {
            maxMarksDisplayLabel.setText("—");
            passMarksDisplayLabel.setText("—");
        }
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

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Wrapper class for displaying Subjects in JComboBox
     */
    private static class SubjectComboBoxItem {
        private final Subject subject;

        public SubjectComboBoxItem(Subject subject) {
            this.subject = subject;
        }

        public Subject getSubject() {
            return subject;
        }

        @Override
        public String toString() {
            return subject.getSubjectCode() + " - " + subject.getSubjectName() +
                    " (Max: " + subject.getMaxMarks() + ")";
        }
    }
}
