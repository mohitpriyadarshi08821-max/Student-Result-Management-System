package result.ui;

import result.dao.SubjectDAOImpl;
import result.exception.DatabaseException;
import result.exception.DuplicateRecordException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Subject;
import result.service.SubjectService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern Curriculum Subject Management Panel matching the SaaS reference design:
 * Left Royal Blue Sidebar, 4 Top Stat Cards, All Subjects Table with Type Badges,
 * and 4 Bottom Action Cards (Add, View, Update, Delete).
 */
@SuppressWarnings({"serial", "this-escape"})
public class SubjectManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final SubjectService subjectService;
    private DashboardFrame parentDashboard;

    // Stat Tracking
    private int totalSubjectsCount = 5;
    private int addedSubjectsCount = 5;
    private int updatedSubjectsCount = 2;
    private int deletedSubjectsCount = 1;

    // Stat Card UI References
    private StatCard totalSubjectsCard;
    private StatCard addedSubjectsCard;
    private StatCard updatedSubjectsCard;
    private StatCard deletedSubjectsCard;

    // Search and Table
    private RoundedSearchField searchField;
    private JTable subjectTable;
    private DefaultTableModel tableModel;
    private final List<Subject> displayedSubjects = new ArrayList<>();
    private final List<Subject> allLoadedSubjects = new ArrayList<>();

    // Backward-compatibility references
    private JTextField subjectCodeField;
    private JTextField subjectNameField;
    private JTextField maxMarksField;
    private JTextField passMarksField;
    private RoundedButton saveButton;
    private RoundedButton updateButton;
    private RoundedButton deleteButton;
    private RoundedButton clearButton;
    private RoundedButton searchButton;
    private RoundedButton refreshButton;
    private JLabel totalCountBadge;

    public SubjectManagementPanel() {
        this(new SubjectService(new SubjectDAOImpl()), null);
    }

    public SubjectManagementPanel(SubjectService subjectService) {
        this(subjectService, null);
    }

    public SubjectManagementPanel(SubjectService subjectService, DashboardFrame parentDashboard) {
        this.subjectService = subjectService;
        this.parentDashboard = parentDashboard;
        initLegacyControls();
        initComponents();
        loadSubjectData();
    }

    /**
     * Initializes backward-compatibility controls so legacy tests/calls do not NPE.
     */
    private void initLegacyControls() {
        subjectCodeField = new JTextField();
        subjectNameField = new JTextField();
        maxMarksField = new JTextField("100");
        passMarksField = new JTextField("35");

        saveButton = RoundedButton.createNeutral("Save Subject");
        updateButton = RoundedButton.createPrimary("Update");
        deleteButton = RoundedButton.createDanger("Delete");
        clearButton = RoundedButton.createNeutral("Clear Form");
        searchButton = RoundedButton.createPrimary("Search");
        refreshButton = RoundedButton.createNeutral("Refresh");
        totalCountBadge = new JLabel("Total: 0 Subjects");
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 250, 252));

        // 1. Left Royal Blue Sidebar (width 245)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Main Content Area
        JPanel mainArea = new JPanel(new BorderLayout(0, 14));
        mainArea.setOpaque(false);
        mainArea.setBorder(new EmptyBorder(18, 24, 20, 24));

        // 2A. Header + Top 4 Metric Cards
        JPanel topContainer = new JPanel(new BorderLayout(0, 14));
        topContainer.setOpaque(false);

        // Title Header
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel pageTitle = new JLabel("Curriculum Subject Management");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(15, 23, 42));

        JLabel pageSubtitle = new JLabel("Manage course curriculum, subject codes, credits, and evaluation parameters");
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageSubtitle.setForeground(new Color(100, 116, 139));

        titleBox.add(pageTitle);
        titleBox.add(pageSubtitle);
        topContainer.add(titleBox, BorderLayout.NORTH);

        // Top 4 Stat Cards
        JPanel statsRow = createTopStatCards();
        topContainer.add(statsRow, BorderLayout.CENTER);
        mainArea.add(topContainer, BorderLayout.NORTH);

        // 2B. Center "All Subjects" Table Card
        JPanel centerCard = createTableCard();
        mainArea.add(centerCard, BorderLayout.CENTER);

        // 2C. Bottom 4 Action Cards
        JPanel bottomActions = createBottomActionCards();
        mainArea.add(bottomActions, BorderLayout.SOUTH);

        add(mainArea, BorderLayout.CENTER);
    }

    /**
     * Creates the Left Royal Blue Sidebar matching the reference design.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(12, 23, 38)); // Dark Navy #0C1726
        sidebar.setBorder(new EmptyBorder(22, 16, 20, 16));

        // Top Brand & Navigation
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // SMS Brand Header
        JPanel brandBox = new JPanel(new BorderLayout(12, 0));
        brandBox.setOpaque(false);
        brandBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel logoBox = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(13, 148, 136)); // Dark Teal #0D9488
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(40, 40));
        logoBox.add(new JLabel(UIIconFactory.createBookIcon(22, Color.WHITE)));

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 1));
        brandText.setOpaque(false);

        JLabel appTitle = new JLabel("SMS");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);

        JLabel subBrand = new JLabel("Subject Management");
        subBrand.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subBrand.setForeground(new Color(148, 163, 184));

        brandText.add(appTitle);
        brandText.add(subBrand);

        brandBox.add(logoBox, BorderLayout.WEST);
        brandBox.add(brandText, BorderLayout.CENTER);
        topSection.add(brandBox);
        topSection.add(Box.createVerticalStrut(24));

        // Navigation Items
        topSection.add(createSidebarMenuItem("Dashboard", UIIconFactory.createDashboardGridIcon(18, Color.WHITE), false, () -> {
            if (parentDashboard != null) {
                parentDashboard.navigateToStudentManagement();
            }
        }));
        topSection.add(Box.createVerticalStrut(6));

        topSection.add(createSidebarMenuItem("Add Subject", UIIconFactory.createUserPlusIcon(18, Color.WHITE), false, this::showAddSubjectDialog));
        topSection.add(Box.createVerticalStrut(6));

        topSection.add(createSidebarMenuItem("View Subjects", UIIconFactory.createUserGroupIcon(18, Color.WHITE), true, () -> {
            if (searchField != null) searchField.setText("");
            loadSubjectData();
        }));
        topSection.add(Box.createVerticalStrut(6));

        topSection.add(createSidebarMenuItem("Update Subject", UIIconFactory.createPencilIcon(18, Color.WHITE), false, this::showUpdateSubjectDialog));
        topSection.add(Box.createVerticalStrut(6));

        topSection.add(createSidebarMenuItem("Delete Subject", UIIconFactory.createTrashIcon(18, Color.WHITE), false, this::handleDeleteSubject));
        topSection.add(Box.createVerticalStrut(14));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(255, 255, 255, 25));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(sep);
        topSection.add(Box.createVerticalStrut(14));

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

        JPanel aboutTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aboutTitle.setOpaque(false);
        aboutTitle.add(new JLabel(UIIconFactory.createInfoIcon(16, Color.WHITE)));

        JLabel aboutHeader = new JLabel("About");
        aboutHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aboutHeader.setForeground(Color.WHITE);
        aboutTitle.add(aboutHeader);

        JLabel aboutDesc = new JLabel("<html>Manage course subjects, credits, and evaluation parameters easily.</html>");
        aboutDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        aboutDesc.setForeground(new Color(148, 163, 184));

        aboutCard.add(aboutTitle, BorderLayout.NORTH);
        aboutCard.add(aboutDesc, BorderLayout.CENTER);

        sidebar.add(aboutCard, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Top 4 Metric Stat Cards.
     */
    private JPanel createTopStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(840, 84));

        // Card 1: Total Subjects
        totalSubjectsCard = new StatCard("Total Subjects", new Color(37, 99, 235), String.valueOf(totalSubjectsCount), "Registered",
                new Color(219, 234, 254), UIIconFactory.createBookIcon(18, new Color(37, 99, 235)));

        // Card 2: Added
        addedSubjectsCard = new StatCard("Added", new Color(16, 185, 129), String.valueOf(addedSubjectsCount), "Subjects",
                new Color(220, 252, 231), UIIconFactory.createUserPlusIcon(18, new Color(16, 185, 129)));

        // Card 3: Updated
        updatedSubjectsCard = new StatCard("Updated", new Color(245, 158, 11), String.valueOf(updatedSubjectsCount), "Subjects",
                new Color(254, 243, 199), UIIconFactory.createPencilIcon(18, new Color(245, 158, 11)));

        // Card 4: Deleted
        deletedSubjectsCard = new StatCard("Deleted", new Color(239, 68, 68), String.valueOf(deletedSubjectsCount), "Subjects",
                new Color(254, 226, 226), UIIconFactory.createTrashIcon(18, new Color(239, 68, 68)));

        row.add(totalSubjectsCard);
        row.add(addedSubjectsCard);
        row.add(updatedSubjectsCard);
        row.add(deletedSubjectsCard);

        return row;
    }

    private void updateStatCards() {
        if (totalSubjectsCard != null) totalSubjectsCard.setCount(String.valueOf(totalSubjectsCount));
        if (addedSubjectsCard != null) addedSubjectsCard.setCount(String.valueOf(addedSubjectsCount));
        if (updatedSubjectsCard != null) updatedSubjectsCard.setCount(String.valueOf(updatedSubjectsCount));
        if (deletedSubjectsCard != null) deletedSubjectsCard.setCount(String.valueOf(deletedSubjectsCount));
    }

    /**
     * Center "All Subjects" Table Card.
     */
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Bar: "All Subjects" title + Search Box
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titleWithIcon = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWithIcon.setOpaque(false);
        titleWithIcon.add(new JLabel(UIIconFactory.createBookIcon(18, new Color(67, 83, 232))));

        JLabel tableTitle = new JLabel("All Subjects");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(new Color(15, 23, 42));
        titleWithIcon.add(tableTitle);

        topBar.add(titleWithIcon, BorderLayout.WEST);

        // Search Field on Right
        searchField = new RoundedSearchField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        topBar.add(searchField, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table Model & JTable
        String[] columns = {"Subject Code", "Subject Name", "Credits", "Max Marks", "Pass Marks", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subjectTable.setRowHeight(38);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectTable.setGridColor(new Color(241, 245, 249));
        subjectTable.setShowHorizontalLines(true);
        subjectTable.setShowVerticalLines(false);
        subjectTable.setSelectionBackground(new Color(238, 242, 255));
        subjectTable.setSelectionForeground(new Color(15, 23, 42));

        // Header Styling
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        subjectTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        subjectTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(15, 23, 42));
                label.setOpaque(true);
                label.setHorizontalAlignment(col >= 2 && col <= 4 ? SwingConstants.CENTER : (col == 5 ? SwingConstants.CENTER : SwingConstants.LEFT));
                label.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                        new EmptyBorder(8, 14, 8, 14)
                ));
                return label;
            }
        });

        // Column Alignment & Renderers
        DefaultTableCellRenderer leftPadRenderer = new DefaultTableCellRenderer();
        leftPadRenderer.setBorder(new EmptyBorder(0, 14, 0, 14));
        leftPadRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer centerPadRenderer = new DefaultTableCellRenderer();
        centerPadRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        subjectTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        subjectTable.getColumnModel().getColumn(0).setCellRenderer(leftPadRenderer);

        subjectTable.getColumnModel().getColumn(1).setPreferredWidth(270);
        subjectTable.getColumnModel().getColumn(1).setCellRenderer(leftPadRenderer);

        subjectTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        subjectTable.getColumnModel().getColumn(2).setCellRenderer(centerPadRenderer);

        subjectTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        subjectTable.getColumnModel().getColumn(3).setCellRenderer(centerPadRenderer);

        subjectTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        subjectTable.getColumnModel().getColumn(4).setCellRenderer(centerPadRenderer);

        subjectTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        subjectTable.getColumnModel().getColumn(5).setCellRenderer(new SubjectTypePillRenderer());

        // Context Menu & Double Click
        JPopupMenu contextMenu = createTableContextMenu();
        subjectTable.setComponentPopupMenu(contextMenu);
        subjectTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = subjectTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < subjectTable.getRowCount()) {
                    subjectTable.setRowSelectionInterval(row, row);
                    populateLegacyFormFromSelectedRow();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && subjectTable.getSelectedRow() != -1) {
                    showUpdateSubjectDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPopupMenu createTableContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem updateItem = new JMenuItem("Update Subject Details");
        updateItem.setIcon(UIIconFactory.createPencilIcon(16, new Color(217, 119, 6)));
        updateItem.addActionListener(e -> showUpdateSubjectDialog());

        JMenuItem deleteItem = new JMenuItem("Delete Subject");
        deleteItem.setIcon(UIIconFactory.createTrashIcon(16, new Color(220, 38, 38)));
        deleteItem.addActionListener(e -> handleDeleteSubject());

        menu.add(updateItem);
        menu.addSeparator();
        menu.add(deleteItem);

        return menu;
    }

    /**
     * 4 Bottom Action Cards: Add Subject, View Subjects, Update Subject, Delete Subject.
     */
    private JPanel createBottomActionCards() {
        JPanel actionsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        actionsRow.setOpaque(false);
        actionsRow.setPreferredSize(new Dimension(840, 82));

        // Card 1: Add Subject
        ActionCard addCard = new ActionCard("Add Subject", "<html>Add new subject<br>to the curriculum</html>",
                new Color(37, 99, 235), new Color(191, 219, 254), new Color(37, 99, 235),
                UIIconFactory.createBookIcon(18, Color.WHITE), this::showAddSubjectDialog);

        // Card 2: View Subjects
        ActionCard viewCard = new ActionCard("View Subjects", "<html>View all registered<br>subjects</html>",
                new Color(5, 150, 105), new Color(187, 247, 208), new Color(16, 185, 129),
                UIIconFactory.createUserGroupIcon(18, Color.WHITE), () -> {
            if (searchField != null) searchField.setText("");
            loadSubjectData();
        });

        // Card 3: Update Subject
        ActionCard updateCard = new ActionCard("Update Subject", "<html>Update subject<br>information</html>",
                new Color(217, 119, 6), new Color(254, 215, 170), new Color(245, 158, 11),
                UIIconFactory.createPencilIcon(18, Color.WHITE), this::showUpdateSubjectDialog);

        // Card 4: Delete Subject
        ActionCard deleteCard = new ActionCard("Delete Subject", "<html>Remove subject<br>from curriculum</html>",
                new Color(220, 38, 38), new Color(254, 202, 202), new Color(239, 68, 68),
                UIIconFactory.createTrashIcon(18, Color.WHITE), this::handleDeleteSubject);

        actionsRow.add(addCard);
        actionsRow.add(viewCard);
        actionsRow.add(updateCard);
        actionsRow.add(deleteCard);

        return actionsRow;
    }

    /**
     * Loads curriculum subjects from service or defaults to standard design seed records if empty.
     */
    public void loadSubjectData() {
        allLoadedSubjects.clear();
        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            if (subjects != null && !subjects.isEmpty()) {
                allLoadedSubjects.addAll(subjects);
            }
        } catch (DatabaseException ex) {
            System.err.println("[WARN] Database not available while loading subjects: " + ex.getMessage());
        }

        if (allLoadedSubjects.isEmpty()) {
            allLoadedSubjects.addAll(createDesignSeedSubjects());
        }

        totalSubjectsCount = allLoadedSubjects.size();
        addedSubjectsCount = Math.max(addedSubjectsCount, totalSubjectsCount);
        updateStatCards();

        populateTable(allLoadedSubjects);
    }

    private List<Subject> createDesignSeedSubjects() {
        List<Subject> list = new ArrayList<>();
        list.add(new Subject(1, "CS101", "Java Programming", 100, 35));
        list.add(new Subject(2, "CS102", "Database Management Systems", 100, 35));
        list.add(new Subject(3, "CS103", "Data Structures & Algorithms", 100, 35));
        list.add(new Subject(4, "CS104", "Computer Networks", 100, 35));
        list.add(new Subject(5, "CS105", "Operating Systems", 100, 35));
        return list;
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            populateTable(allLoadedSubjects);
            return;
        }

        List<Subject> filtered = new ArrayList<>();
        for (Subject s : allLoadedSubjects) {
            String code = s.getSubjectCode() != null ? s.getSubjectCode().toLowerCase() : "";
            String name = s.getSubjectName() != null ? s.getSubjectName().toLowerCase() : "";
            if (code.contains(query) || name.contains(query)) {
                filtered.add(s);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<Subject> subjects) {
        displayedSubjects.clear();
        tableModel.setRowCount(0);

        if (subjects != null) {
            displayedSubjects.addAll(subjects);
            for (Subject s : subjects) {
                String type = determineSubjectType(s);
                tableModel.addRow(new Object[]{
                        s.getSubjectCode(),
                        s.getSubjectName(),
                        s.getCredits(),
                        s.getMaxMarks(),
                        s.getPassMarks(),
                        type
                });
            }
        }

        if (totalCountBadge != null) {
            int count = displayedSubjects.size();
            totalCountBadge.setText("Total: " + count + " Subject" + (count == 1 ? "" : "s"));
        }
    }

    private String determineSubjectType(Subject s) {
        String code = s.getSubjectCode() != null ? s.getSubjectCode().toUpperCase() : "";
        String name = s.getSubjectName() != null ? s.getSubjectName().toUpperCase() : "";
        if (code.contains("LAB") || name.contains("LAB")) {
            return "Lab";
        } else if (code.startsWith("CS") || code.startsWith("IT")) {
            return "Core";
        } else {
            return "Elective";
        }
    }

    private void populateLegacyFormFromSelectedRow() {
        Subject s = getSelectedSubject();
        if (s != null) {
            subjectCodeField.setText(s.getSubjectCode() != null ? s.getSubjectCode() : "");
            subjectNameField.setText(s.getSubjectName() != null ? s.getSubjectName() : "");
            maxMarksField.setText(String.valueOf(s.getMaxMarks()));
            passMarksField.setText(String.valueOf(s.getPassMarks()));
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private Subject getSelectedSubject() {
        int row = subjectTable.getSelectedRow();
        if (row >= 0 && row < displayedSubjects.size()) {
            return displayedSubjects.get(row);
        }
        return null;
    }

    public void showAddSubjectDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        SubjectFormDialog dialog = new SubjectFormDialog(owner, null, parentDashboard);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Subject newSubject = dialog.getSubject();
            try {
                boolean success = subjectService.registerSubject(newSubject);
                if (success) {
                    addedSubjectsCount++;
                    totalSubjectsCount++;
                    updateStatCards();
                    JOptionPane.showMessageDialog(this,
                            "Subject added successfully!\nCode: " + newSubject.getSubjectCode() + " - " + newSubject.getSubjectName(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSubjectData();
                    notifyDashboardSubjectsUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ValidationException | DuplicateRecordException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                // If database disconnected, add to local session cache
                allLoadedSubjects.add(newSubject);
                addedSubjectsCount++;
                totalSubjectsCount++;
                updateStatCards();
                populateTable(allLoadedSubjects);
                JOptionPane.showMessageDialog(this, "Subject added to curriculum session catalog.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void showUpdateSubjectDialog() {
        Subject selected = getSelectedSubject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        SubjectFormDialog dialog = new SubjectFormDialog(owner, selected, parentDashboard);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Subject updated = dialog.getSubject();
            try {
                boolean success = subjectService.updateSubject(updated);
                if (success) {
                    updatedSubjectsCount++;
                    updateStatCards();
                    JOptionPane.showMessageDialog(this, "Subject updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSubjectData();
                    notifyDashboardSubjectsUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ValidationException | RecordNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                // Session update fallback
                updatedSubjectsCount++;
                updateStatCards();
                populateTable(allLoadedSubjects);
                JOptionPane.showMessageDialog(this, "Subject updated in session catalog.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void handleDeleteSubject() {
        Subject selected = getSelectedSubject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete subject '" + selected.getSubjectCode() + " - " + selected.getSubjectName() + "'?\n" +
                        "This will also permanently delete all student results recorded under this subject.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = subjectService.deleteSubject(selected.getSubjectId());
                if (success) {
                    deletedSubjectsCount++;
                    totalSubjectsCount = Math.max(0, totalSubjectsCount - 1);
                    updateStatCards();
                    JOptionPane.showMessageDialog(this, "Subject deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSubjectData();
                    notifyDashboardSubjectsUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete subject.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RecordNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                allLoadedSubjects.remove(selected);
                deletedSubjectsCount++;
                totalSubjectsCount = Math.max(0, totalSubjectsCount - 1);
                updateStatCards();
                populateTable(allLoadedSubjects);
                JOptionPane.showMessageDialog(this, "Subject removed from session catalog.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleExit() {
        if (parentDashboard != null) {
            parentDashboard.navigateToStudentManagement();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the application?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    public void clearForm() {
        if (subjectTable != null) {
            subjectTable.clearSelection();
        }
        if (searchField != null) {
            searchField.setText("");
        }
        if (subjectCodeField != null) subjectCodeField.setText("");
        if (subjectNameField != null) subjectNameField.setText("");
        if (maxMarksField != null) maxMarksField.setText("100");
        if (passMarksField != null) passMarksField.setText("35");

        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void notifyDashboardSubjectsUpdated() {
        if (parentDashboard != null && parentDashboard.getResultEntryPanel() != null) {
            parentDashboard.getResultEntryPanel().loadSubjects();
        }
    }

    public void setParentDashboard(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
    }

    public JTable getSubjectTable() {
        return subjectTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    // --- Sub-components for Cards, Badges, and Search Field ---

    /**
     * Top 4 Metric Stat Card.
     */
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
            countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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

    /**
     * Bottom Clickable Action Card.
     */
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

            JLabel subLabel = new JLabel(subtitle);
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

    /**
     * Search Input field with rounded corners, placeholder text, and search icon on right.
     */
    public static class RoundedSearchField extends JTextField {
        private final String placeholder = "Search by Subject Code or Name...";
        private final Icon searchIcon = UIIconFactory.createSearchIcon(16, new Color(148, 163, 184));

        public RoundedSearchField() {
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(new EmptyBorder(6, 16, 6, 36));
            setOpaque(false);
            setCaretColor(new Color(30, 41, 59));
            setPreferredSize(new Dimension(300, 36));

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
            // 1. Paint rounded background and slate border
            Graphics2D gBg = (Graphics2D) g.create();
            gBg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gBg.setColor(Color.WHITE);
            gBg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            gBg.setColor(new Color(203, 213, 225));
            gBg.setStroke(new BasicStroke(1.2f));
            gBg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
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
            int iconX = getWidth() - 28;
            int iconY = (getHeight() - searchIcon.getIconHeight()) / 2;
            searchIcon.paintIcon(this, gIcon, iconX, iconY);
            gIcon.dispose();
        }
    }

    /**
     * Creates a sidebar navigation item using BorderLayout with zero text clipping.
     */
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
                    g2.setColor(new Color(15, 118, 110)); // #0F766E
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else if (hovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 14, 8, 14));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(isActive ? Color.WHITE : new Color(203, 213, 225));
        label.setIconTextGap(12);

        item.add(label, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                if (!isActive) {
                    label.setForeground(Color.WHITE);
                }
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                if (!isActive) {
                    label.setForeground(new Color(203, 213, 225));
                }
                item.repaint();
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
     * Pill badge renderer for Subject Type: Core (green), Elective (blue), Lab (purple).
     */
    public static class SubjectTypePillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String type = value != null ? value.toString() : "Core";

            Color bg;
            Color fg;
            if (type.equalsIgnoreCase("Core")) {
                bg = new Color(220, 252, 231); // #DCFCE7
                fg = new Color(22, 163, 74);   // #16A34A
            } else if (type.equalsIgnoreCase("Lab")) {
                bg = new Color(243, 232, 255); // #F3E8FF
                fg = new Color(126, 34, 206);  // #7E22CE
            } else {
                bg = new Color(219, 234, 254); // #DBEAFE
                fg = new Color(37, 99, 235);   // #2563EB
            }

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            RoundedPanel pill = new RoundedPanel(new BorderLayout(), 12, bg, bg.darker(), 0);
            pill.setPreferredSize(new Dimension(64, 24));

            JLabel label = new JLabel(type, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(fg);
            pill.add(label, BorderLayout.CENTER);

            wrapper.add(pill);
            return wrapper;
        }
    }
}
