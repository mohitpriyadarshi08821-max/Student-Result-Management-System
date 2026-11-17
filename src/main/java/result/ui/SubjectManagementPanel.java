package result.ui;

import result.dao.SubjectDAOImpl;
import result.exception.DatabaseException;
import result.exception.DuplicateRecordException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Subject;
import result.service.SubjectService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
 * Swing panel providing UI controls for managing academic subjects/curriculum courses
 * (Adding, updating, deleting, searching, and viewing subjects in a table).
 */
public class SubjectManagementPanel extends JPanel {

    private final SubjectService subjectService;
    private DashboardFrame parentDashboard;

    // Form inputs
    private JTextField subjectCodeField;
    private JTextField subjectNameField;
    private JTextField maxMarksField;
    private JTextField passMarksField;

    // Action buttons
    private RoundedButton saveButton;
    private RoundedButton updateButton;
    private RoundedButton deleteButton;
    private RoundedButton clearButton;

    // Search bar
    private JTextField searchField;
    private RoundedButton searchButton;
    private RoundedButton refreshButton;

    // Table
    private JTable subjectTable;
    private DefaultTableModel tableModel;
    private JLabel totalCountBadge;

    // State tracking
    private int selectedSubjectId = -1;

    public SubjectManagementPanel() {
        this(new SubjectService(new SubjectDAOImpl()), null);
    }

    public SubjectManagementPanel(SubjectService subjectService) {
        this(subjectService, null);
    }

    public SubjectManagementPanel(SubjectService subjectService, DashboardFrame parentDashboard) {
        this.subjectService = subjectService;
        this.parentDashboard = parentDashboard;
        initComponents();
        loadSubjectData();
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
        formPanel.setPreferredSize(new Dimension(390, 500));
        centerPanel.add(formPanel, BorderLayout.WEST);

        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(10, 0), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        panel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel titleLabel = new JLabel("Curriculum Subject Management");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchControls.setOpaque(false);

        JLabel searchLabel = new JLabel("Search Subject:");
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
            loadSubjectData();
        });

        searchField.addActionListener(e -> handleSearch());

        searchControls.add(searchLabel);
        searchControls.add(searchField);
        searchControls.add(searchButton);
        searchControls.add(refreshButton);

        panel.add(searchControls, BorderLayout.EAST);
        return panel;
    }

    private JPanel createFormPanel() {
        RoundedPanel formCard = new RoundedPanel(new BorderLayout(0, 10), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        formCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Clean Header Title - No etched line ("bar bar") below it
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Subject Details");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        formCard.add(headerPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Subject Code
        fieldsPanel.add(createFieldLabel("Subject Code * (e.g. CS101)"), setGbc(gbc, 0));
        subjectCodeField = createStyledTextField();
        fieldsPanel.add(subjectCodeField, setGbc(gbc, 1));

        // Subject Name
        fieldsPanel.add(createFieldLabel("Subject Name * (e.g. Java Programming)"), setGbc(gbc, 2));
        subjectNameField = createStyledTextField();
        fieldsPanel.add(subjectNameField, setGbc(gbc, 3));

        // Maximum Marks & Pass Marks Row
        JPanel marksRow = new JPanel(new GridLayout(1, 2, 12, 0));
        marksRow.setOpaque(false);

        JPanel maxPanel = new JPanel(new BorderLayout(0, 4));
        maxPanel.setOpaque(false);
        maxPanel.add(createFieldLabel("Maximum Marks *"), BorderLayout.NORTH);
        maxMarksField = createStyledTextField();
        maxMarksField.setText("100");
        maxPanel.add(maxMarksField, BorderLayout.CENTER);

        JPanel passPanel = new JPanel(new BorderLayout(0, 4));
        passPanel.setOpaque(false);
        passPanel.add(createFieldLabel("Passing Marks *"), BorderLayout.NORTH);
        passMarksField = createStyledTextField();
        passMarksField.setText("35");
        passPanel.add(passMarksField, BorderLayout.CENTER);

        marksRow.add(maxPanel);
        marksRow.add(passPanel);
        fieldsPanel.add(marksRow, setGbc(gbc, 4));

        // Add fields directly without JScrollPane - fits naturally without scrolling
        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons - Semi-rounded with high-contrast dark text
        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsGrid.setOpaque(false);
        buttonsGrid.setBorder(new EmptyBorder(12, 0, 0, 0));

        saveButton = RoundedButton.createSuccess("Save Subject");
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

        formCard.add(buttonsGrid, BorderLayout.SOUTH);
        return formCard;
    }

    private JPanel createTablePanel() {
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 10), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        tableCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Right Preview Section Header Bar
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setOpaque(false);

        JLabel tableTitle = new JLabel("Curriculum Subjects Directory");
        tableTitle.setFont(UIConstants.FONT_HEADER);
        tableTitle.setForeground(UIConstants.TEXT_PRIMARY); // High contrast dark font

        totalCountBadge = new JLabel("Total: 0 Subjects", SwingConstants.CENTER);
        totalCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalCountBadge.setForeground(UIConstants.TEXT_PRIMARY); // High contrast dark font

        RoundedPanel countPill = new RoundedPanel(new BorderLayout(), UIConstants.CORNER_RADIUS_SM, new Color(241, 245, 249), new Color(203, 213, 225), 1);
        countPill.setBorder(new EmptyBorder(4, 12, 4, 12));
        countPill.add(totalCountBadge, BorderLayout.CENTER);

        tableHeaderBar.add(tableTitle, BorderLayout.WEST);
        tableHeaderBar.add(countPill, BorderLayout.EAST);
        tableCard.add(tableHeaderBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Subject Code", "Subject Name", "Max Marks", "Pass Marks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        subjectTable = new JTable(tableModel);
        subjectTable.setFont(UIConstants.FONT_REGULAR);
        subjectTable.setRowHeight(30);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectTable.setGridColor(new Color(226, 232, 240));
        subjectTable.setShowGrid(true);
        subjectTable.setSelectionBackground(new Color(219, 234, 254));
        subjectTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Custom Table Header Renderer: High-contrast Dark Font with soft slate header background
        subjectTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        subjectTable.getTableHeader().setPreferredSize(new Dimension(subjectTable.getTableHeader().getPreferredSize().width, 34));
        subjectTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        subjectTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        subjectTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        subjectTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        subjectTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        subjectTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        subjectTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        subjectTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        subjectTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        subjectTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // When a row is selected in the table, populate the form
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && subjectTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(subjectTable);
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

    public void loadSubjectData() {
        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            populateTable(subjects);
        } catch (DatabaseException ex) {
            System.err.println("[WARN] Database not available while loading subjects: " + ex.getMessage());
        }
    }

    private void populateTable(List<Subject> subjects) {
        tableModel.setRowCount(0);
        if (subjects != null) {
            for (Subject s : subjects) {
                tableModel.addRow(new Object[]{
                        s.getSubjectId(),
                        s.getSubjectCode(),
                        s.getSubjectName(),
                        s.getMaxMarks(),
                        s.getPassMarks()
                });
            }
        }
        if (totalCountBadge != null) {
            int count = (subjects != null) ? subjects.size() : 0;
            totalCountBadge.setText("Total: " + count + " Subject" + (count == 1 ? "" : "s"));
        }
    }

    private void populateFormFromSelectedRow() {
        int viewRow = subjectTable.getSelectedRow();
        if (viewRow >= 0) {
            int row = subjectTable.convertRowIndexToModel(viewRow);
            Object idVal = tableModel.getValueAt(row, 0);
            selectedSubjectId = idVal instanceof Number ? ((Number) idVal).intValue() : Integer.parseInt(idVal.toString());

            Object codeVal = tableModel.getValueAt(row, 1);
            subjectCodeField.setText(codeVal != null ? codeVal.toString() : "");

            Object nameVal = tableModel.getValueAt(row, 2);
            subjectNameField.setText(nameVal != null ? nameVal.toString() : "");

            Object maxVal = tableModel.getValueAt(row, 3);
            maxMarksField.setText(maxVal != null ? maxVal.toString() : "100");

            Object passVal = tableModel.getValueAt(row, 4);
            passMarksField.setText(passVal != null ? passVal.toString() : "35");

            saveButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private void handleSave() {
        try {
            Subject subject = buildSubjectFromForm();
            boolean success = subjectService.registerSubject(subject);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Subject added successfully!\nCode: " + subject.getSubjectCode() + " - " + subject.getSubjectName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadSubjectData();
                notifyDashboardSubjectsUpdated();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save subject.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException | DuplicateRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Subject subject = buildSubjectFromForm();
            subject.setSubjectId(selectedSubjectId);

            boolean success = subjectService.updateSubject(subject);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Subject updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadSubjectData();
                notifyDashboardSubjectsUpdated();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update subject.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException | RecordNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = subjectCodeField.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete subject '" + code + "'?\n" +
                        "This will also permanently delete all student results recorded under this subject.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = subjectService.deleteSubject(selectedSubjectId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Subject deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadSubjectData();
                    notifyDashboardSubjectsUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RecordNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<Subject> all = subjectService.getAllSubjects();
            if (query.isEmpty()) {
                populateTable(all);
                return;
            }

            List<Subject> filtered = all.stream()
                    .filter(s -> s.getSubjectCode().toLowerCase().contains(query) ||
                            s.getSubjectName().toLowerCase().contains(query))
                    .toList();

            populateTable(filtered);
            if (filtered.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No subjects found matching: " + query,
                        "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        selectedSubjectId = -1;
        subjectCodeField.setText("");
        subjectNameField.setText("");
        maxMarksField.setText("100");
        passMarksField.setText("35");

        subjectTable.clearSelection();
        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        subjectCodeField.requestFocusInWindow();
    }

    private Subject buildSubjectFromForm() throws ValidationException {
        String code = subjectCodeField.getText().trim();
        String name = subjectNameField.getText().trim();
        String maxStr = maxMarksField.getText().trim();
        String passStr = passMarksField.getText().trim();

        int max;
        int pass;
        try {
            max = Integer.parseInt(maxStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Maximum Marks must be a valid integer.");
        }

        try {
            pass = Integer.parseInt(passStr);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Passing Marks must be a valid integer.");
        }

        Subject subject = new Subject();
        subject.setSubjectCode(code);
        subject.setSubjectName(name);
        subject.setMaxMarks(max);
        subject.setPassMarks(pass);
        return subject;
    }

    private void notifyDashboardSubjectsUpdated() {
        if (parentDashboard != null && parentDashboard.getResultEntryPanel() != null) {
            parentDashboard.getResultEntryPanel().loadSubjects();
        }
    }

    public void setParentDashboard(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
    }
}

