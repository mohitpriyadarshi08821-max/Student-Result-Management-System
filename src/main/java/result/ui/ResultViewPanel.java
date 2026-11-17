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
import result.service.MarksheetExporter;
import result.service.ResultService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern SaaS Examination Marksheet & Results Viewer.
 * Features Dark Navy sidebar (#0C1726), active Dark Teal (#0D9488) navigation pill,
 * 4 Top Metric Stat Cards, Center Academic Statement Card with JTable,
 * an integrated Lower Summary Ribbon, and 4 Bottom Action Cards.
 */
@SuppressWarnings({"serial", "this-escape"})
public class ResultViewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ResultService resultService;
    private DashboardFrame parentDashboard;

    // Search controls
    private JTextField searchRollNumberField;
    private RoundedButton searchButton;
    private RoundedButton printButton;
    private RoundedButton clearButton;
    private JLabel studentInfoLabel;
    private RoundedSearchField searchFilterField;

    // Report display elements (preserved for backward compatibility)
    private JPanel reportContainer;
    private JLabel studentNameLabel;
    private JLabel studentRollLabel;
    private JLabel studentCourseLabel;
    private JLabel studentSemesterLabel;
    private JLabel reportDateLabel;

    // Marksheet table
    private JTable markSheetTable;
    private DefaultTableModel tableModel;
    private final List<Object[]> allCurrentRows = new ArrayList<>();

    // Summary metrics (in top stat cards & lower summary ribbon)
    private JLabel totalMarksLabel;
    private JLabel percentageLabel;
    private JLabel overallGradeLabel;
    private JLabel resultStatusBadge;

    // Top 4 Stat Cards
    private StatCard totalSubjectsCard;
    private StatCard totalMarksCard;
    private StatCard percentageCard;
    private StatCard overallStatusCard;

    private StudentResultSummary currentSummary;

    public ResultViewPanel() {
        this(new ResultService(new ResultDAOImpl(), new StudentDAOImpl(), new SubjectDAOImpl()));
    }

    public ResultViewPanel(ResultService resultService) {
        this.resultService = resultService;
        initComponents();
        initDefaultStudentMarksheet();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 250, 252));

        // 1. Left Sidebar (Dark Navy #0C1726)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Right Main Content Area
        JPanel mainContent = createMainContentPanel();
        add(mainContent, BorderLayout.CENTER);
    }

    /**
     * Left Sidebar matching the SaaS Dashboard design language:
     * - Dark Navy Background (#0C1726)
     * - 40x40 Rounded Teal Brand Emblem (#0D9488)
     * - Active Dark Teal Navigation Pill for "View Marksheet"
     * - Glassy About Card at bottom
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(12, 23, 38)); // Dark Navy #0C1726
        sidebar.setBorder(new EmptyBorder(18, 16, 18, 16));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Brand Logo Row: using BorderLayout to prevent any title/subtitle truncation
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel logoBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(13, 148, 136)); // Dark Teal #0D9488
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setPreferredSize(new Dimension(40, 40));
        logoBadge.setOpaque(false);
        logoBadge.add(new JLabel(UIIconFactory.createAwardIcon(22, Color.WHITE)));
        brandPanel.add(logoBadge, BorderLayout.WEST);

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        brandTextPanel.setOpaque(false);
        JLabel brandTitle = new JLabel("SMS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Result & Marksheet");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(148, 163, 184));

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSub);
        brandPanel.add(brandTextPanel, BorderLayout.CENTER);

        topSection.add(brandPanel);
        topSection.add(Box.createVerticalStrut(24));

        // Navigation Items
        // 1. Dashboard
        topSection.add(createSidebarMenuItem("Dashboard", UIIconFactory.createDashboardGridIcon(18, Color.WHITE), false, () -> {
            if (parentDashboard != null) {
                parentDashboard.navigateToStudentManagement();
            }
        }));
        topSection.add(Box.createVerticalStrut(6));

        // 2. View Marksheet (Active Pill)
        topSection.add(createSidebarMenuItem("View Marksheet", UIIconFactory.createDocumentIcon(18, Color.WHITE), true, () -> {
            handleSearch();
        }));
        topSection.add(Box.createVerticalStrut(6));

        // 3. Print Marksheet
        topSection.add(createSidebarMenuItem("Print Marksheet", UIIconFactory.createPrinterIcon(18, Color.WHITE), false, this::handlePrint));
        topSection.add(Box.createVerticalStrut(6));

        // 4. Enter Marks
        topSection.add(createSidebarMenuItem("Enter Marks", UIIconFactory.createUserPlusIcon(18, Color.WHITE), false, () -> {
            if (parentDashboard != null) {
                String roll = searchRollNumberField != null ? searchRollNumberField.getText().trim() : "";
                parentDashboard.navigateToResultEntry(roll);
            }
        }));
        topSection.add(Box.createVerticalStrut(6));

        // 5. Clear View
        topSection.add(createSidebarMenuItem("Clear View", UIIconFactory.createTrashIcon(18, Color.WHITE), false, this::clearMarksheet));
        topSection.add(Box.createVerticalStrut(14));

        // 6. Exit
        topSection.add(createSidebarMenuItem("Exit", UIIconFactory.createExitIcon(18, Color.WHITE), false, this::handleExit));

        sidebar.add(topSection, BorderLayout.NORTH);

        // Bottom "About" Card
        JPanel aboutCard = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        aboutCard.setOpaque(false);
        aboutCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        aboutCard.setPreferredSize(new Dimension(218, 110));

        JPanel aboutTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aboutTitle.setOpaque(false);
        aboutTitle.add(new JLabel(UIIconFactory.createInfoIcon(16, Color.WHITE)));

        JLabel aboutHeader = new JLabel("About");
        aboutHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aboutHeader.setForeground(Color.WHITE);
        aboutTitle.add(aboutHeader);

        JLabel aboutDesc = new JLabel("<html>Generate and print official student marksheets with cumulative scores and final grades.</html>");
        aboutDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        aboutDesc.setForeground(new Color(148, 163, 184));

        aboutCard.add(aboutTitle, BorderLayout.NORTH);
        aboutCard.add(aboutDesc, BorderLayout.CENTER);

        sidebar.add(aboutCard, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createSidebarMenuItem(String text, Icon icon, boolean isActive, Runnable action) {
        final boolean[] hovered = {false};
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(new Color(13, 148, 136)); // Dark Teal #0D9488
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(new Color(15, 118, 110));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else if (hovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 16));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setBorder(new EmptyBorder(8, 14, 8, 14));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(isActive ? Color.WHITE : new Color(203, 213, 225));
        label.setIconTextGap(12);

        item.add(label, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) {
                    hovered[0] = true;
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) {
                    hovered[0] = false;
                    item.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run();
                }
            }
        });

        return item;
    }

    /**
     * Right main container:
     * - Top Header & 4 Stat Cards
     * - Center Academic Statement Card with verification toolbar, JTable, and lower summary ribbon
     * - Bottom 4 Action Cards
     */
    private JPanel createMainContentPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 18, 12, 18));

        // 1. Top Section: Title, Subtitle, and 4 Top Stat Cards
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel pageTitle = new JLabel("Student Marksheet & Result Summary");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(15, 23, 42));

        JLabel pageSubtitle = new JLabel("Official statement of academic marks, cumulative performance, and evaluation standing");
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pageSubtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(pageTitle);
        titlePanel.add(pageSubtitle);
        topSection.add(titlePanel, BorderLayout.NORTH);

        // 4 Top Metric Stat Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        totalSubjectsCard = new StatCard("Total Subjects", new Color(100, 116, 139), "5", "Evaluated",
                new Color(204, 251, 241), UIIconFactory.createBookIcon(22, new Color(13, 148, 136)));

        totalMarksCard = new StatCard("Total Marks", new Color(100, 116, 139), "415.0 / 500", "Obtained / Max",
                new Color(220, 252, 231), UIIconFactory.createCheckIcon(22, new Color(22, 163, 74)));

        percentageCard = new StatCard("Semester SGPA", new Color(100, 116, 139), "8.44", "10-Point Scale",
                new Color(254, 243, 199), UIIconFactory.createAwardIcon(22, new Color(245, 158, 11)));

        overallStatusCard = new StatCard("Overall Status", new Color(100, 116, 139), "PASS (Grade A)", "Standing",
                new Color(220, 252, 231), UIIconFactory.createCheckIcon(22, new Color(22, 163, 74)));

        statsRow.add(totalSubjectsCard);
        statsRow.add(totalMarksCard);
        statsRow.add(percentageCard);
        statsRow.add(overallStatusCard);

        topSection.add(statsRow, BorderLayout.CENTER);
        content.add(topSection, BorderLayout.NORTH);

        // 2. Center Card: Rounded Marksheet Card with search, toolbar, table, and lower summary ribbon
        JPanel centerCard = createCenterMarksheetCard();
        content.add(centerCard, BorderLayout.CENTER);

        // 3. Bottom 4 Action Cards
        JPanel bottomActions = createBottomActionCards();
        content.add(bottomActions, BorderLayout.SOUTH);

        return content;
    }

    /**
     * Center Card containing:
     * - Header Bar (Title + Search Field)
     * - Search & Verification Strip (Roll Number field, View Marksheet, student info, Print, Clear)
     * - Marksheet JTable
     * - Lower Integrated Summary Ribbon (Total Marks, Percentage, Overall Grade, Date, Status Badge)
     */
    private JPanel createCenterMarksheetCard() {
        reportContainer = new RoundedPanel(new BorderLayout(0, 8), 12, Color.WHITE, new Color(226, 232, 240), 1);
        reportContainer.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Header Bar: Left "Academic Statement of Marks", Right Search Filter Field
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JLabel titleLabel = new JLabel("Academic Statement of Marks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(15, 23, 42));
        headerBar.add(titleLabel, BorderLayout.WEST);

        searchFilterField = new RoundedSearchField();
        searchFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMarksheetTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMarksheetTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMarksheetTable();
            }
        });
        searchFilterField.addActionListener(e -> filterMarksheetTable());
        headerBar.add(searchFilterField, BorderLayout.EAST);

        // Student Lookup & Action Toolbar Strip
        JPanel entryStrip = createLookupToolbar();

        JPanel northContainer = new JPanel(new BorderLayout(0, 8));
        northContainer.setOpaque(false);
        northContainer.add(headerBar, BorderLayout.NORTH);
        northContainer.add(entryStrip, BorderLayout.SOUTH);
        reportContainer.add(northContainer, BorderLayout.NORTH);

        // Marksheet Table
        String[] cols = {"Subject Code", "Subject Name", "Credits", "Max Marks", "Pass Marks", "Marks Obtained", "Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        markSheetTable = new JTable(tableModel);
        markSheetTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        markSheetTable.setRowHeight(32);
        markSheetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        markSheetTable.setGridColor(new Color(241, 245, 249));
        markSheetTable.setShowVerticalLines(false);
        markSheetTable.setShowHorizontalLines(true);
        markSheetTable.setSelectionBackground(new Color(241, 245, 249));
        markSheetTable.setSelectionForeground(new Color(15, 23, 42));

        // Header Styling
        markSheetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        markSheetTable.getTableHeader().setPreferredSize(new Dimension(markSheetTable.getTableHeader().getPreferredSize().width, 32));
        markSheetTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(new Color(248, 250, 252));
                label.setForeground(new Color(51, 65, 85));
                label.setOpaque(true);
                label.setHorizontalAlignment(col == 0 || col == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                label.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                        new EmptyBorder(8, 4, 8, 4)
                ));
                return label;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, 8, 0, 8));

        markSheetTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        markSheetTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        markSheetTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        markSheetTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        markSheetTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        markSheetTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        markSheetTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        markSheetTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        markSheetTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        markSheetTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        markSheetTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        markSheetTable.getColumnModel().getColumn(5).setMinWidth(110);
        markSheetTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(new Color(15, 23, 42));
                return l;
            }
        });

        // Column 6: Grade Pill Badges
        markSheetTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        markSheetTable.getColumnModel().getColumn(6).setCellRenderer(new ResultEntryPanel.PillBadgeRenderer());

        // Column 7: Status Pill Badges
        markSheetTable.getColumnModel().getColumn(7).setPreferredWidth(75);
        markSheetTable.getColumnModel().getColumn(7).setCellRenderer(new ResultEntryPanel.StatusPillRenderer());

        JScrollPane scrollPane = new JScrollPane(markSheetTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        reportContainer.add(scrollPane, BorderLayout.CENTER);

        // Lower Part: Fixed Integrated Marksheet Summary Ribbon
        JPanel lowerSummaryRibbon = createLowerSummaryRibbon();
        reportContainer.add(lowerSummaryRibbon, BorderLayout.SOUTH);

        return reportContainer;
    }

    /**
     * Clean, responsive 2-line toolbar:
     * Line 1: Roll Number input, View Marksheet button, Print Marksheet button, Clear button
     * Line 2: Student Verified Information label spanning full width without overlapping
     */
    private JPanel createLookupToolbar() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(0, 6), 10, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Line 1: Controls
        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line1.setOpaque(false);

        JLabel rollLbl = new JLabel("Roll Number:");
        rollLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rollLbl.setForeground(new Color(51, 65, 85));

        searchRollNumberField = new JTextField(12);
        searchRollNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchRollNumberField.setPreferredSize(new Dimension(140, 32));
        searchRollNumberField.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        searchRollNumberField.addActionListener(e -> handleSearch());

        searchButton = new RoundedButton("View Marksheet", new Color(30, 41, 59), new Color(51, 65, 85), new Color(15, 23, 42), Color.WHITE, new Color(51, 65, 85), 6);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.setPreferredSize(new Dimension(135, 32));
        searchButton.addActionListener(e -> handleSearch());

        printButton = new RoundedButton("Print Marksheet", new Color(13, 148, 136), new Color(15, 118, 110), new Color(17, 94, 89), Color.WHITE, new Color(15, 118, 110), 6);
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printButton.setPreferredSize(new Dimension(140, 32));
        printButton.addActionListener(e -> handlePrint());

        clearButton = new RoundedButton("Clear", new Color(241, 245, 249), new Color(226, 232, 240), new Color(203, 213, 225), new Color(51, 65, 85), new Color(203, 213, 225), 6);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearButton.setPreferredSize(new Dimension(75, 32));
        clearButton.addActionListener(e -> clearMarksheet());

        line1.add(rollLbl);
        line1.add(searchRollNumberField);
        line1.add(searchButton);
        line1.add(printButton);
        line1.add(clearButton);

        // Line 2: Student Verified Information Label
        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line2.setOpaque(false);

        studentInfoLabel = new JLabel("Please enter roll number (e.g. 2510001320) and click View Marksheet.");
        studentInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentInfoLabel.setForeground(new Color(100, 116, 139));
        line2.add(studentInfoLabel);

        panel.add(line1, BorderLayout.NORTH);
        panel.add(line2, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Lower Part of the Marksheet:
     * A clean, modern summary ribbon containing Total Marks, Percentage, Overall Grade,
     * Date of Issue, and high-contrast Status Badge.
     */
    private JPanel createLowerSummaryRibbon() {
        RoundedPanel ribbon = new RoundedPanel(new BorderLayout(16, 0), 8, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        ribbon.setBorder(new EmptyBorder(8, 14, 8, 14));
        ribbon.setPreferredSize(new Dimension(840, 48));

        // Left Metrics Row
        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
        metricsPanel.setOpaque(false);

        totalMarksLabel = new JLabel("Total Marks: 415.0 / 500");
        totalMarksLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalMarksLabel.setForeground(new Color(15, 23, 42));

        percentageLabel = new JLabel("Percentage: 83.00 %");
        percentageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        percentageLabel.setForeground(new Color(13, 148, 136));

        overallGradeLabel = new JLabel("Overall Grade: A");
        overallGradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        overallGradeLabel.setForeground(new Color(15, 23, 42));

        reportDateLabel = new JLabel("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        reportDateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        reportDateLabel.setForeground(new Color(100, 116, 139));

        metricsPanel.add(totalMarksLabel);
        metricsPanel.add(createDivider());
        metricsPanel.add(percentageLabel);
        metricsPanel.add(createDivider());
        metricsPanel.add(overallGradeLabel);
        metricsPanel.add(createDivider());
        metricsPanel.add(reportDateLabel);

        ribbon.add(metricsPanel, BorderLayout.WEST);

        // Right Status Badge
        resultStatusBadge = new JLabel("STATUS: PASS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor("STATUS: PASS".equals(getText()) ? new Color(134, 239, 172) : new Color(252, 165, 165));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        resultStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultStatusBadge.setOpaque(false);
        resultStatusBadge.setBackground(new Color(220, 252, 231));
        resultStatusBadge.setForeground(new Color(22, 163, 74));
        resultStatusBadge.setPreferredSize(new Dimension(130, 32));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(resultStatusBadge);
        ribbon.add(badgeWrapper, BorderLayout.EAST);

        // Backward compatibility labels
        studentNameLabel = new JLabel("");
        studentRollLabel = new JLabel("");
        studentCourseLabel = new JLabel("");
        studentSemesterLabel = new JLabel("");

        return ribbon;
    }

    private JComponent createDivider() {
        JPanel div = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(203, 213, 225));
                g.fillRect(getWidth() / 2, 2, 1, getHeight() - 4);
            }
        };
        div.setOpaque(false);
        div.setPreferredSize(new Dimension(14, 16));
        return div;
    }

    /**
     * 4 Bottom Action Cards matching the SaaS layout:
     * Print Marksheet, Enter Marks, Update Student, Dashboard Overview
     */
    private JPanel createBottomActionCards() {
        JPanel actionsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        actionsRow.setOpaque(false);
        actionsRow.setPreferredSize(new Dimension(840, 82));

        // Card 1: Print Marksheet
        ActionCard printCard = new ActionCard("Print Marksheet", "Generate physical or PDF\nofficial marksheet",
                new Color(15, 23, 42), new Color(226, 232, 240), new Color(204, 251, 241),
                UIIconFactory.createPrinterIcon(18, new Color(13, 148, 136)), this::handlePrint);

        // Card 2: Enter Marks
        ActionCard enterCard = new ActionCard("Enter Marks", "Record new subject marks\nfor student examinations",
                new Color(15, 23, 42), new Color(226, 232, 240), new Color(220, 252, 231),
                UIIconFactory.createUserPlusIcon(18, new Color(16, 185, 129)), () -> {
            if (parentDashboard != null) {
                String roll = searchRollNumberField != null ? searchRollNumberField.getText().trim() : "";
                parentDashboard.navigateToResultEntry(roll);
            }
        });

        // Card 3: Update Student
        ActionCard updateCard = new ActionCard("Update Student", "Modify student profile\nand course enrollment",
                new Color(15, 23, 42), new Color(226, 232, 240), new Color(254, 243, 199),
                UIIconFactory.createPencilIcon(18, new Color(245, 158, 11)), () -> {
            if (parentDashboard != null) {
                parentDashboard.navigateToStudentManagement();
            }
        });

        // Card 4: Dashboard Overview
        ActionCard dashCard = new ActionCard("Dashboard Overview", "Return to main student\nrecords overview",
                new Color(15, 23, 42), new Color(226, 232, 240), new Color(241, 245, 249),
                UIIconFactory.createDashboardGridIcon(18, new Color(15, 23, 42)), () -> {
            if (parentDashboard != null) {
                parentDashboard.navigateToStudentManagement();
            }
        });

        actionsRow.add(printCard);
        actionsRow.add(enterCard);
        actionsRow.add(updateCard);
        actionsRow.add(dashCard);

        return actionsRow;
    }

    private void initDefaultStudentMarksheet() {
        if (searchRollNumberField != null) {
            searchRollNumberField.setText("2510001320");
            handleSearch();
        }
    }

    public void setParentDashboard(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
    }

    public void loadResultForRoll(String rollNumber) {
        if (rollNumber != null) {
            searchRollNumberField.setText(rollNumber.trim());
            handleSearch();
        }
    }

    public void handleSearch() {
        String roll = searchRollNumberField.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Roll Number.",
                    "Missing Roll Number", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentSummary = resultService.generateResultSummary(roll);
        } catch (Throwable ex) {
            // Check design seed summary fallback for offline / preview mode
            currentSummary = findSeedSummaryByRoll(roll);
        }

        if (currentSummary != null) {
            displaySummary(currentSummary);
            printButton.setEnabled(true);
        } else {
            clearMarksheet();
            studentInfoLabel.setText("Student marksheet not found for Roll Number: " + roll);
            studentInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            studentInfoLabel.setForeground(new Color(220, 38, 38));
            JOptionPane.showMessageDialog(this, "No exam results found for Roll Number: " + roll,
                    "No Results Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Result makeSeedResult(int resultId, int studentId, int subjectId, Subject subject, double marks, String grade) {
        Result r = new Result(resultId, studentId, subjectId, marks, grade);
        r.setSubject(subject);
        return r;
    }

    private StudentResultSummary findSeedSummaryByRoll(String roll) {
        if ("2510001320".equals(roll)) {
            Student s = new Student(1, "2510001320", "Mohit", "Priyadarshi", "mohit@example.com", "B.Tech Computer Science", 4, "Male", LocalDate.now().minusYears(19));
            List<Result> results = new ArrayList<>();
            results.add(makeSeedResult(1, 1, 1, new Subject(1, "CS101", "Java Programming", 100, 35, 4), 88.0, "B"));
            results.add(makeSeedResult(2, 1, 2, new Subject(2, "CS102", "Database Management Systems", 100, 35, 4), 92.0, "A"));
            results.add(makeSeedResult(3, 1, 3, new Subject(3, "CS103", "Data Structures & Algorithms", 100, 35, 4), 85.0, "A-"));
            results.add(makeSeedResult(4, 1, 4, new Subject(4, "CS104", "Computer Networks", 100, 35, 3), 78.0, "B"));
            results.add(makeSeedResult(5, 1, 5, new Subject(5, "CS105", "Operating Systems", 100, 35, 3), 72.0, "B"));

            StudentResultSummary sum = new StudentResultSummary(s, results);
            sum.setTotalMarksObtained(415.0);
            sum.setTotalMaxMarks(500.0);
            sum.setPercentage(83.0);
            sum.setTotalCredits(18);
            sum.setTotalCreditPoints(152.0);
            sum.setSgpa(8.44);
            sum.setOverallGrade("A");
            sum.setDivision("First Class with Distinction");
            sum.setStatus("PASS");
            return sum;
        } else if ("2510001331".equals(roll)) {
            Student s = new Student(2, "2510001331", "Moulik", "Tiwari", "moulik@example.com", "BCA", 2, "Male", LocalDate.now().minusYears(19));
            List<Result> results = new ArrayList<>();
            results.add(makeSeedResult(6, 2, 1, new Subject(1, "CS101", "Java Programming", 100, 35, 4), 90.0, "A"));
            results.add(makeSeedResult(7, 2, 2, new Subject(2, "CS102", "Database Management Systems", 100, 35, 4), 86.0, "A-"));

            StudentResultSummary sum = new StudentResultSummary(s, results);
            sum.setTotalMarksObtained(176.0);
            sum.setTotalMaxMarks(200.0);
            sum.setPercentage(88.0);
            sum.setTotalCredits(8);
            sum.setTotalCreditPoints(72.0);
            sum.setSgpa(9.00);
            sum.setOverallGrade("A");
            sum.setDivision("First Class with Distinction");
            sum.setStatus("PASS");
            return sum;
        } else if ("2510001319".equals(roll)) {
            Student s = new Student(3, "2510001319", "Adarsh", "Kumar", "adarsh@example.com", "B.Tech IT", 4, "Male", LocalDate.now().minusYears(20));
            List<Result> results = new ArrayList<>();
            results.add(makeSeedResult(8, 3, 1, new Subject(1, "CS101", "Java Programming", 100, 35, 4), 94.0, "A"));

            StudentResultSummary sum = new StudentResultSummary(s, results);
            sum.setTotalMarksObtained(94.0);
            sum.setTotalMaxMarks(100.0);
            sum.setPercentage(94.0);
            sum.setTotalCredits(4);
            sum.setTotalCreditPoints(40.0);
            sum.setSgpa(10.00);
            sum.setOverallGrade("A+");
            sum.setDivision("First Class with Distinction");
            sum.setStatus("PASS");
            return sum;
        } else if ("2510001212".equals(roll)) {
            Student s = new Student(4, "2510001212", "Akshay", "Bharadwaj", "akshay@example.com", "MCA", 2, "Male", LocalDate.now().minusYears(19));
            List<Result> results = new ArrayList<>();
            results.add(makeSeedResult(9, 4, 1, new Subject(1, "CS101", "Java Programming", 100, 35, 4), 76.0, "B"));

            StudentResultSummary sum = new StudentResultSummary(s, results);
            sum.setTotalMarksObtained(76.0);
            sum.setTotalMaxMarks(100.0);
            sum.setPercentage(76.0);
            sum.setTotalCredits(4);
            sum.setTotalCreditPoints(32.0);
            sum.setSgpa(8.00);
            sum.setOverallGrade("B");
            sum.setDivision("First Class");
            sum.setStatus("PASS");
            return sum;
        }
        return null;
    }

    public void displaySummary(StudentResultSummary summary) {
        Student s = summary.getStudent();
        studentNameLabel.setText("Student Name: " + s.getFullName());
        studentRollLabel.setText("Roll Number: " + s.getRollNumber());
        studentCourseLabel.setText("Course / Program: " + s.getCourse());
        studentSemesterLabel.setText("Semester: " + s.getSemester());

        studentInfoLabel.setText("Verified: " + s.getFullName() + " | " + s.getCourse() + " (Sem " + s.getSemester() + ")");
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentInfoLabel.setForeground(new Color(13, 148, 136));

        allCurrentRows.clear();
        tableModel.setRowCount(0);
        int passedCount = 0;
        int failedCount = 0;

        for (Result r : summary.getResults()) {
            Subject sub = r.getSubject();
            int max = sub != null ? sub.getMaxMarks() : 100;
            int pass = sub != null ? sub.getPassMarks() : 35;
            int credits = sub != null ? sub.getCredits() : 4;
            String status = r.getMarksObtained() >= pass ? "PASS" : "FAIL";
            if ("PASS".equals(status)) {
                passedCount++;
            } else {
                failedCount++;
            }

            Object[] row = new Object[]{
                    sub != null ? sub.getSubjectCode() : "N/A",
                    sub != null ? sub.getSubjectName() : "N/A",
                    credits,
                    max,
                    pass,
                    r.getMarksObtained(),
                    r.getGrade(),
                    status
            };
            allCurrentRows.add(row);
            tableModel.addRow(row);
        }

        // Update Top 4 Metric Stat Cards
        totalSubjectsCard.setCount(String.valueOf(summary.getResults().size()));
        totalMarksCard.setCount(String.format("%.1f / %.0f", summary.getTotalMarksObtained(), summary.getTotalMaxMarks()));
        percentageCard.setCount(String.format("%.2f", summary.getSgpa() > 0 ? summary.getSgpa() : (summary.getPercentage() / 10.0)));
        overallStatusCard.setCount(summary.getStatus() + " (" + summary.getOverallGrade() + ")");

        // Update Lower Summary Ribbon
        totalMarksLabel.setText(String.format("Total Marks: %.1f / %.0f", summary.getTotalMarksObtained(), summary.getTotalMaxMarks()));
        percentageLabel.setText(String.format("Percentage: %.2f %% | SGPA: %.2f", summary.getPercentage(), summary.getSgpa()));
        String division = summary.getDivision() != null && !summary.getDivision().isEmpty() ? summary.getDivision() : "First Class";
        overallGradeLabel.setText("Grade: " + summary.getOverallGrade() + " (" + division + ")");

        if ("PASS".equalsIgnoreCase(summary.getStatus())) {
            resultStatusBadge.setText("STATUS: PASS");
            resultStatusBadge.setBackground(new Color(220, 252, 231));
            resultStatusBadge.setForeground(new Color(22, 163, 74));
        } else {
            resultStatusBadge.setText("STATUS: FAIL");
            resultStatusBadge.setBackground(new Color(254, 226, 226));
            resultStatusBadge.setForeground(new Color(220, 38, 38));
        }
        resultStatusBadge.repaint();
    }

    private void filterMarksheetTable() {
        if (searchFilterField == null) return;
        String query = searchFilterField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tableModel.setRowCount(0);
            for (Object[] row : allCurrentRows) {
                tableModel.addRow(row);
            }
            return;
        }

        List<Object[]> matchingRows = new ArrayList<>();
        for (Object[] row : allCurrentRows) {
            boolean match = false;
            for (Object col : row) {
                if (col != null && String.valueOf(col).toLowerCase().contains(query)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                matchingRows.add(row);
            }
        }
        tableModel.setRowCount(0);
        for (Object[] r : matchingRows) {
            tableModel.addRow(r);
        }
    }

    public void clearMarksheet() {
        if (searchRollNumberField != null) searchRollNumberField.setText("");
        if (searchFilterField != null) searchFilterField.setText("");
        if (studentInfoLabel != null) {
            studentInfoLabel.setText("Please enter roll number (e.g. 2510001320) and click View Marksheet.");
            studentInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            studentInfoLabel.setForeground(new Color(100, 116, 139));
        }
        tableModel.setRowCount(0);
        allCurrentRows.clear();
        totalSubjectsCard.setCount("0");
        totalMarksCard.setCount("0.0 / 0");
        percentageCard.setCount("0.0%");
        overallStatusCard.setCount("PENDING");

        totalMarksLabel.setText("Total Marks: — / —");
        percentageLabel.setText("Percentage: — %");
        overallGradeLabel.setText("Overall Grade: —");
        resultStatusBadge.setText("STATUS: —");
        resultStatusBadge.setBackground(new Color(241, 245, 249));
        resultStatusBadge.setForeground(new Color(100, 116, 139));
        resultStatusBadge.repaint();

        printButton.setEnabled(false);
        currentSummary = null;
    }

    public void handlePrint() {
        if (currentSummary == null) {
            JOptionPane.showMessageDialog(this, "No marksheet available to print or download. Please view a student marksheet first.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = currentSummary.getStudent();
        String studentInfo = (s != null ? s.getFullName() + " (Roll No: " + s.getRollNumber() + ")" : "Student");

        String[] options = {"Download PDF Marksheet", "Download HTML Marksheet", "Send to Physical Printer", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Official Marksheet for: " + studentInfo + "\n\n" +
                        "Please select how you would like to output or save this marksheet:",
                "Print / Download Marksheet",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                UIIconFactory.createPrinterIcon(28, new Color(13, 148, 136)),
                options,
                options[0]
        );

        if (choice == 0) {
            downloadPdfMarksheet();
        } else if (choice == 1) {
            downloadHtmlMarksheet();
        } else if (choice == 2) {
            printToPhysicalPrinter();
        }
    }

    public void downloadPdfMarksheet() {
        if (currentSummary == null) return;
        Student st = currentSummary.getStudent();
        String roll = st != null && st.getRollNumber() != null ? st.getRollNumber().trim() : "000";
        String rawName = st != null && st.getFullName() != null ? st.getFullName() : "Student";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String defaultFileName = "Marksheet_" + roll + "_" + safeName + ".pdf";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save / Download Marksheet as PDF");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
            }

            try {
                MarksheetExporter.exportToPdf(currentSummary, targetFile);
                int openChoice = JOptionPane.showOptionDialog(
                        this,
                        "Official Marksheet PDF downloaded successfully!\n\nSaved to: " + targetFile.getAbsolutePath() + "\n\nWould you like to open the PDF now?",
                        "Download Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        UIIconFactory.createCheckIcon(26, new Color(16, 185, 129)),
                        new String[]{"Open PDF", "Close"},
                        "Open PDF"
                );

                if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(targetFile);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save PDF marksheet: " + ex.getMessage(),
                        "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void downloadHtmlMarksheet() {
        if (currentSummary == null) return;
        Student st = currentSummary.getStudent();
        String roll = st != null && st.getRollNumber() != null ? st.getRollNumber().trim() : "000";
        String rawName = st != null && st.getFullName() != null ? st.getFullName() : "Student";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String defaultFileName = "Marksheet_" + roll + "_" + safeName + ".html";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save / Download Marksheet as HTML");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Web Report (*.html)", "html"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".html") && !targetFile.getName().toLowerCase().endsWith(".htm")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".html");
            }

            try {
                MarksheetExporter.exportToHtml(currentSummary, targetFile);
                int openChoice = JOptionPane.showOptionDialog(
                        this,
                        "Official Marksheet HTML report downloaded successfully!\n\nSaved to: " + targetFile.getAbsolutePath() + "\n\nWould you like to open it in your browser now?",
                        "Download Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        UIIconFactory.createCheckIcon(26, new Color(16, 185, 129)),
                        new String[]{"Open in Browser", "Close"},
                        "Open in Browser"
                );

                if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(targetFile);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save HTML marksheet: " + ex.getMessage(),
                        "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void printToPhysicalPrinter() {
        if (currentSummary == null) return;
        try {
            MessageFormat header = new MessageFormat(
                    "Student Result Management System - Official Marksheet: " +
                            currentSummary.getStudent().getFullName() + " (" + currentSummary.getStudent().getRollNumber() + ")"
            );
            MessageFormat footer = new MessageFormat(
                    String.format("Percentage: %.2f%% | Standing: %s | Grade: %s | Page - {0}",
                            currentSummary.getPercentage(), currentSummary.getStatus(), currentSummary.getOverallGrade())
            );

            boolean complete = markSheetTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing completed successfully.",
                        "Print Status", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            int fallback = JOptionPane.showConfirmDialog(
                    this,
                    "No physical printer was found or the print job failed:\n" + ex.getMessage() +
                            "\n\nWould you like to download and save the official marksheet as PDF instead?",
                    "Printer Error - Download PDF Instead?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (fallback == JOptionPane.YES_OPTION) {
                downloadPdfMarksheet();
            }
        }
    }

    private void handleExit() {
        if (parentDashboard != null) {
            parentDashboard.handleLogout();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    // Getters for testing
    public JTable getMarkSheetTable() {
        return markSheetTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTextField getSearchRollNumberField() {
        return searchRollNumberField;
    }

    public RoundedButton getSearchButton() {
        return searchButton;
    }

    public RoundedButton getPrintButton() {
        return printButton;
    }

    public StudentResultSummary getCurrentSummary() {
        return currentSummary;
    }

    // --- Sub-components matching the standardized SaaS UI ---

    public static class StatCard extends JPanel {
        private final JLabel countLabel;

        public StatCard(String title, Color titleColor, String initialCount, String subtitle, Color circleBg, Icon icon) {
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));

            JPanel circleContainer = new JPanel(new GridBagLayout());
            circleContainer.setOpaque(false);
            circleContainer.setPreferredSize(new Dimension(46, 46));

            JPanel circle = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(circleBg);
                    int d = Math.min(getWidth(), getHeight()) - 2;
                    int ox = (getWidth() - d) / 2;
                    int oy = (getHeight() - d) / 2;
                    g2.fillOval(ox, oy, d, d);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            circle.setOpaque(false);
            circle.setPreferredSize(new Dimension(42, 42));
            circle.add(new JLabel(icon));
            circleContainer.add(circle);
            add(circleContainer, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 0));
            textPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            titleLabel.setForeground(titleColor);

            countLabel = new JLabel(initialCount);
            countLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            countLabel.setForeground(new Color(15, 23, 42));

            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            subLabel.setForeground(new Color(148, 163, 184));

            textPanel.add(titleLabel);
            textPanel.add(countLabel);
            textPanel.add(subLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        public void setCount(String count) {
            countLabel.setText(count);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ActionCard extends JPanel {
        private final Color normalBorder;
        private final Color hoverBorder;
        private boolean hovered = false;

        public ActionCard(String title, String subtitle, Color titleColor, Color borderColor, Color circleBg, Icon icon, Runnable onClick) {
            this.normalBorder = borderColor;
            this.hoverBorder = borderColor.darker();

            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel circleContainer = new JPanel(new GridBagLayout());
            circleContainer.setOpaque(false);
            circleContainer.setPreferredSize(new Dimension(42, 42));

            JPanel circle = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(circleBg);
                    int d = Math.min(getWidth(), getHeight()) - 2;
                    int ox = (getWidth() - d) / 2;
                    int oy = (getHeight() - d) / 2;
                    g2.fillOval(ox, oy, d, d);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            circle.setOpaque(false);
            circle.setPreferredSize(new Dimension(38, 38));
            circle.add(new JLabel(icon));
            circleContainer.add(circle);
            add(circleContainer, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
            textPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            titleLabel.setForeground(titleColor);

            JLabel subLabel = new JLabel(subtitle.startsWith("<html>") ? subtitle : "<html>" + subtitle.replace("\n", "<br>") + "</html>");
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            subLabel.setForeground(new Color(100, 116, 139));

            textPanel.add(titleLabel);
            textPanel.add(subLabel);
            add(textPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.setColor(hovered ? hoverBorder : normalBorder);
            g2.setStroke(new BasicStroke(hovered ? 1.5f : 1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedSearchField extends JTextField {
        private final String placeholder = "Search results...";
        private final Icon searchIcon = UIIconFactory.createSearchIcon(16, new Color(148, 163, 184));

        public RoundedSearchField() {
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(new EmptyBorder(6, 16, 6, 34));
            setOpaque(false);
            setCaretColor(new Color(30, 41, 59));
            setPreferredSize(new Dimension(240, 34));

            // Force complete component repaint on focus and mouse hover to prevent partial caret clipping
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            // 1. Paint rounded background and subtle slate border
            Graphics2D gBg = (Graphics2D) g.create();
            gBg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gBg.setColor(Color.WHITE);
            gBg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            gBg.setColor(new Color(203, 213, 225));
            gBg.setStroke(new BasicStroke(1.2f));
            gBg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            gBg.dispose();

            // 2. Paint placeholder FIRST if empty (keeps placeholder visible even when cursor/focus moves to it)
            if (getText().isEmpty()) {
                Graphics2D gText = (Graphics2D) g.create();
                gText.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
                if (desktopHints != null) {
                    gText.addRenderingHints(desktopHints);
                } else {
                    gText.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                }
                gText.setColor(new Color(148, 163, 184));
                gText.setFont(getFont());
                FontMetrics fm = gText.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                int x = getInsets().left;
                gText.drawString(placeholder, x, y);
                gText.dispose();
            }

            // 3. Delegate to JTextField UI for text/caret
            super.paintComponent(g);

            // 4. Paint search icon on the right
            Graphics2D gIcon = (Graphics2D) g.create();
            gIcon.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int iconX = getWidth() - 26;
            int iconY = (getHeight() - searchIcon.getIconHeight()) / 2;
            searchIcon.paintIcon(this, gIcon, iconX, iconY);
            gIcon.dispose();
        }
    }
}
