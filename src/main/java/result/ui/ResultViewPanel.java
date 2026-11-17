package result.ui;

import result.dao.ResultDAOImpl;
import result.dao.StudentDAOImpl;
import result.dao.SubjectDAOImpl;
import result.exception.DatabaseException;
import result.exception.RecordNotFoundException;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;
import result.service.ResultService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Swing panel providing UI controls to search a student by roll number
 * and display a formatted mark sheet, percentage, and PASS/FAIL status.
 */
public class ResultViewPanel extends JPanel {

    private final ResultService resultService;

    // Search controls
    private JTextField searchRollNumberField;
    private RoundedButton searchButton;
    private RoundedButton printButton;

    // Report display elements
    private JPanel reportContainer;
    private JLabel studentNameLabel;
    private JLabel studentRollLabel;
    private JLabel studentCourseLabel;
    private JLabel studentSemesterLabel;
    private JLabel reportDateLabel;

    // Marksheet table
    private JTable markSheetTable;
    private DefaultTableModel tableModel;

    // Summary metrics
    private JLabel totalMarksLabel;
    private JLabel percentageLabel;
    private JLabel overallGradeLabel;
    private JLabel resultStatusBadge;

    private StudentResultSummary currentSummary;

    public ResultViewPanel() {
        this(new ResultService(new ResultDAOImpl(), new StudentDAOImpl(), new SubjectDAOImpl()));
    }

    public ResultViewPanel(ResultService resultService) {
        this.resultService = resultService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Top Search & Action Bar
        JPanel topBar = createSearchBar();
        add(topBar, BorderLayout.NORTH);

        // 2. Center Marksheet Document Card
        reportContainer = createMarksheetReportCard();
        add(new JScrollPane(reportContainer), BorderLayout.CENTER);
    }

    private JPanel createSearchBar() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(15, 0), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel label = new JLabel("Enter Student Roll Number:");
        label.setFont(UIConstants.FONT_BOLD);
        label.setForeground(UIConstants.TEXT_PRIMARY);

        searchRollNumberField = new JTextField(16);
        searchRollNumberField.setFont(UIConstants.FONT_REGULAR);
        searchRollNumberField.setPreferredSize(new Dimension(180, 36));
        searchRollNumberField.setBorder(UIConstants.createInputBorder());
        searchRollNumberField.addActionListener(e -> handleSearch());

        searchButton = RoundedButton.createPrimary("View Marksheet");
        searchButton.setPreferredSize(new Dimension(160, 36));
        searchButton.addActionListener(e -> handleSearch());

        leftPanel.add(label);
        leftPanel.add(searchRollNumberField);
        leftPanel.add(searchButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        printButton = RoundedButton.createSuccess("🖨️ Print Marksheet");
        printButton.setPreferredSize(new Dimension(175, 36));
        printButton.setEnabled(false);
        printButton.addActionListener(e -> handlePrint());
        rightPanel.add(printButton);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel createMarksheetReportCard() {
        RoundedPanel reportCard = new RoundedPanel(new BorderLayout(0, 16), UIConstants.CORNER_RADIUS, Color.WHITE, new Color(226, 232, 240), 1);
        reportCard.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Section
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        headerPanel.setBackground(Color.WHITE);

        JLabel institutionTitle = new JLabel("STUDENT RESULT MANAGEMENT SYSTEM", SwingConstants.CENTER);
        institutionTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        institutionTitle.setForeground(UIConstants.PRIMARY_COLOR);

        JLabel documentTitle = new JLabel("OFFICIAL STATEMENT OF ACADEMIC MARKS", SwingConstants.CENTER);
        documentTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        documentTitle.setForeground(UIConstants.TEXT_PRIMARY);

        reportDateLabel = new JLabel("Date of Issue: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), SwingConstants.CENTER);
        reportDateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        reportDateLabel.setForeground(new Color(127, 140, 141));

        headerPanel.add(institutionTitle);
        headerPanel.add(documentTitle);
        headerPanel.add(reportDateLabel);

        // Student Info Grid
        RoundedPanel studentInfoPanel = new RoundedPanel(new GridLayout(2, 2, 20, 8), UIConstants.CORNER_RADIUS_SM, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        studentInfoPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        studentNameLabel = new JLabel("Student Name: —");
        studentNameLabel.setFont(UIConstants.FONT_BOLD);

        studentRollLabel = new JLabel("Roll Number: —");
        studentRollLabel.setFont(UIConstants.FONT_BOLD);

        studentCourseLabel = new JLabel("Course / Program: —");
        studentCourseLabel.setFont(UIConstants.FONT_REGULAR);

        studentSemesterLabel = new JLabel("Semester: —");
        studentSemesterLabel.setFont(UIConstants.FONT_REGULAR);

        studentInfoPanel.add(studentNameLabel);
        studentInfoPanel.add(studentRollLabel);
        studentInfoPanel.add(studentCourseLabel);
        studentInfoPanel.add(studentSemesterLabel);

        JPanel topGroup = new JPanel(new BorderLayout(0, 12));
        topGroup.setOpaque(false);
        topGroup.add(headerPanel, BorderLayout.NORTH);
        topGroup.add(studentInfoPanel, BorderLayout.SOUTH);
        reportCard.add(topGroup, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"Subject Code", "Subject Name", "Max Marks", "Pass Marks", "Marks Obtained", "Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        markSheetTable = new JTable(tableModel);
        markSheetTable.setFont(UIConstants.FONT_REGULAR);
        markSheetTable.setRowHeight(30);
        markSheetTable.setGridColor(new Color(226, 232, 240));
        markSheetTable.setShowGrid(true);
        markSheetTable.setSelectionBackground(new Color(219, 234, 254));
        markSheetTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Custom Table Header Renderer: High-contrast Dark Font with soft slate header background
        markSheetTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        markSheetTable.getTableHeader().setPreferredSize(new Dimension(markSheetTable.getTableHeader().getPreferredSize().width, 34));
        markSheetTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        markSheetTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        markSheetTable.getColumnModel().getColumn(0).setCellRenderer(center);
        markSheetTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        markSheetTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        markSheetTable.getColumnModel().getColumn(2).setCellRenderer(center);
        markSheetTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        markSheetTable.getColumnModel().getColumn(3).setCellRenderer(center);
        markSheetTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        markSheetTable.getColumnModel().getColumn(4).setCellRenderer(center);
        markSheetTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        markSheetTable.getColumnModel().getColumn(5).setCellRenderer(center);

        // Status column renderer
        markSheetTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        markSheetTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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

        JScrollPane tableScroll = new JScrollPane(markSheetTable);
        tableScroll.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        tableScroll.getViewport().setBackground(Color.WHITE);
        reportCard.add(tableScroll, BorderLayout.CENTER);

        // Bottom Summary Section
        JPanel summaryPanel = createSummaryPanel();
        reportCard.add(summaryPanel, BorderLayout.SOUTH);

        return reportCard;
    }

    private JPanel createSummaryPanel() {
        RoundedPanel summaryCard = new RoundedPanel(new BorderLayout(15, 0), UIConstants.CORNER_RADIUS_SM, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        summaryCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);

        totalMarksLabel = new JLabel("Total Marks: — / —");
        totalMarksLabel.setFont(UIConstants.FONT_BOLD);
        totalMarksLabel.setForeground(UIConstants.TEXT_PRIMARY);

        percentageLabel = new JLabel("Percentage: — %");
        percentageLabel.setFont(UIConstants.FONT_BOLD);
        percentageLabel.setForeground(UIConstants.PRIMARY_COLOR);

        overallGradeLabel = new JLabel("Overall Grade: —");
        overallGradeLabel.setFont(UIConstants.FONT_BOLD);
        overallGradeLabel.setForeground(UIConstants.TEXT_PRIMARY);

        statsPanel.add(totalMarksLabel);
        statsPanel.add(percentageLabel);
        statsPanel.add(overallGradeLabel);

        summaryCard.add(statsPanel, BorderLayout.CENTER);

        resultStatusBadge = new JLabel("STATUS: —", SwingConstants.CENTER);
        resultStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 15));
        resultStatusBadge.setOpaque(true);
        resultStatusBadge.setBackground(new Color(241, 245, 249));
        resultStatusBadge.setForeground(UIConstants.TEXT_PRIMARY);
        resultStatusBadge.setPreferredSize(new Dimension(145, 38));
        resultStatusBadge.setBorder(UIConstants.createRoundedBorder(new Color(203, 213, 225), UIConstants.CORNER_RADIUS_SM, 4, 10, 4, 10));

        summaryCard.add(resultStatusBadge, BorderLayout.EAST);
        return summaryCard;
    }

    public void loadResultForRoll(String rollNumber) {
        if (rollNumber != null) {
            searchRollNumberField.setText(rollNumber.trim());
            handleSearch();
        }
    }

    private void handleSearch() {
        String roll = searchRollNumberField.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Roll Number.",
                    "Missing Roll Number", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentSummary = resultService.generateResultSummary(roll);
            displaySummary(currentSummary);
            printButton.setEnabled(true);
        } catch (RecordNotFoundException ex) {
            clearMarksheet();
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No Results Found", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displaySummary(StudentResultSummary summary) {
        Student s = summary.getStudent();
        studentNameLabel.setText("Student Name: " + s.getFullName());
        studentRollLabel.setText("Roll Number: " + s.getRollNumber());
        studentCourseLabel.setText("Course / Program: " + s.getCourse());
        studentSemesterLabel.setText("Semester: " + s.getSemester());

        tableModel.setRowCount(0);
        for (Result r : summary.getResults()) {
            Subject sub = r.getSubject();
            int max = sub != null ? sub.getMaxMarks() : 100;
            int pass = sub != null ? sub.getPassMarks() : 35;
            String status = r.getMarksObtained() >= pass ? "PASS" : "FAIL";

            tableModel.addRow(new Object[]{
                    sub != null ? sub.getSubjectCode() : "N/A",
                    sub != null ? sub.getSubjectName() : "N/A",
                    max,
                    pass,
                    r.getMarksObtained(),
                    r.getGrade(),
                    status
            });
        }

        totalMarksLabel.setText(String.format("Total Marks: %.1f / %.0f",
                summary.getTotalMarksObtained(), summary.getTotalMaxMarks()));
        percentageLabel.setText(String.format("Percentage: %.2f %%", summary.getPercentage()));
        overallGradeLabel.setText("Overall Grade: " + summary.getOverallGrade());

        if ("PASS".equalsIgnoreCase(summary.getStatus())) {
            resultStatusBadge.setText("STATUS: PASS");
            resultStatusBadge.setBackground(new Color(220, 252, 231));
            resultStatusBadge.setForeground(UIConstants.BTN_TEXT_SUCCESS);
            resultStatusBadge.setBorder(UIConstants.createRoundedBorder(new Color(134, 239, 172), UIConstants.CORNER_RADIUS_SM, 4, 10, 4, 10));
        } else {
            resultStatusBadge.setText("STATUS: FAIL");
            resultStatusBadge.setBackground(new Color(254, 226, 226));
            resultStatusBadge.setForeground(UIConstants.BTN_TEXT_DANGER);
            resultStatusBadge.setBorder(UIConstants.createRoundedBorder(new Color(252, 165, 165), UIConstants.CORNER_RADIUS_SM, 4, 10, 4, 10));
        }
    }

    private void clearMarksheet() {
        studentNameLabel.setText("Student Name: —");
        studentRollLabel.setText("Roll Number: —");
        studentCourseLabel.setText("Course / Program: —");
        studentSemesterLabel.setText("Semester: —");
        tableModel.setRowCount(0);
        totalMarksLabel.setText("Total Marks: — / —");
        percentageLabel.setText("Percentage: — %");
        overallGradeLabel.setText("Overall Grade: —");
        resultStatusBadge.setText("STATUS: —");
        resultStatusBadge.setBackground(new Color(220, 225, 230));
        resultStatusBadge.setForeground(UIConstants.TEXT_PRIMARY);
        resultStatusBadge.setBorder(new LineBorder(new Color(200, 205, 210), 1, true));
        printButton.setEnabled(false);
    }

    private void handlePrint() {
        if (currentSummary == null) {
            JOptionPane.showMessageDialog(this, "No marksheet available to print.",
                    "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            MessageFormat header = new MessageFormat(
                    "Student Result Management System - Marksheet: " +
                            currentSummary.getStudent().getFullName() + " (" + currentSummary.getStudent().getRollNumber() + ")"
            );
            MessageFormat footer = new MessageFormat(
                    String.format("Percentage: %.2f%% | Result: %s | Page - {0}",
                            currentSummary.getPercentage(), currentSummary.getStatus())
            );

            boolean complete = markSheetTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing completed successfully.",
                        "Print Status", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Failed to print marksheet: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
