package result.ui;

import result.dao.ResultDAO;
import result.dao.ResultDAOImpl;
import result.dao.StudentDAO;
import result.dao.StudentDAOImpl;
import result.dao.SubjectDAO;
import result.dao.SubjectDAOImpl;
import result.dao.UserDAOImpl;
import result.database.DatabaseInitializer;
import result.model.User;
import result.service.AuthService;
import result.service.ResultService;
import result.service.StudentService;
import result.service.SubjectService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main application window hosting a header, user session information,
 * logout option, and tabbed panels for Student Management, Subject Management,
 * Marks Entry, and Result Viewing.
 */
public class DashboardFrame extends JFrame {

    private final AuthService authService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ResultService resultService;

    private JTabbedPane tabbedPane;
    private StudentManagementPanel studentManagementPanel;
    private SubjectManagementPanel subjectManagementPanel;
    private ResultEntryPanel resultEntryPanel;
    private ResultViewPanel resultViewPanel;

    public DashboardFrame() {
        this(new AuthService(new UserDAOImpl()));
    }

    public DashboardFrame(AuthService authService) {
        this(
                authService,
                new StudentService(new StudentDAOImpl()),
                new SubjectService(new SubjectDAOImpl()),
                new ResultService(new ResultDAOImpl(), new StudentDAOImpl(), new SubjectDAOImpl())
        );
    }

    public DashboardFrame(AuthService authService, StudentService studentService, ResultService resultService) {
        this(
                authService,
                studentService,
                new SubjectService(new SubjectDAOImpl()),
                resultService
        );
    }

    public DashboardFrame(AuthService authService, StudentService studentService, SubjectService subjectService, ResultService resultService) {
        this.authService = authService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.resultService = resultService;

        // Ensure database tables and initial curriculum seed data are present
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception ignored) {
        }

        initComponents();
    }

    private void initComponents() {
        setTitle(UIConstants.APP_TITLE + " - Administration Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIConstants.BACKGROUND_COLOR);

        // 1. Top Header & Navigation Bar
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // 2. Tabbed Panels Container
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        // Instantiate the four main panels
        studentManagementPanel = new StudentManagementPanel(studentService, this);
        subjectManagementPanel = new SubjectManagementPanel(subjectService, this);
        resultEntryPanel = new ResultEntryPanel(resultService, studentService);
        resultViewPanel = new ResultViewPanel(resultService);

        tabbedPane.addTab("  👥  Student Management  ", studentManagementPanel);
        tabbedPane.addTab("  📚  Subject Management  ", subjectManagementPanel);
        tabbedPane.addTab("  📝  Result Entry  ", resultEntryPanel);
        tabbedPane.addTab("  📊  View Results & Marksheets  ", resultViewPanel);

        // Auto-refresh when tabs are switched
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                studentManagementPanel.loadStudentData();
            } else if (selectedIndex == 1) {
                subjectManagementPanel.loadSubjectData();
            } else if (selectedIndex == 2) {
                resultEntryPanel.loadSubjects();
            }
        });

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainContainer);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Left Branding
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        brandPanel.setOpaque(false);

        JLabel appTitleLabel = new JLabel(UIConstants.APP_TITLE);
        appTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitleLabel.setForeground(Color.WHITE);

        JLabel subTitleLabel = new JLabel("Institutional Examination & Academic Performance Management");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(230, 240, 250));

        brandPanel.add(appTitleLabel);
        brandPanel.add(subTitleLabel);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right User Badge & Logout Option
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        String username = authService.getCurrentUser()
                .map(User::getFullName)
                .orElse("System Administrator");

        JLabel userBadge = new JLabel("👤 " + username, SwingConstants.RIGHT);
        userBadge.setFont(UIConstants.FONT_BOLD);
        userBadge.setForeground(Color.WHITE);
        userBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        RoundedPanel userPill = new RoundedPanel(new BorderLayout(), UIConstants.CORNER_RADIUS_SM, new Color(255, 255, 255, 35), new Color(255, 255, 255, 65), 1);
        userPill.add(userBadge, BorderLayout.CENTER);

        RoundedButton logoutButton = RoundedButton.createDanger("🚪 Logout");
        logoutButton.setFont(UIConstants.FONT_BOLD);
        logoutButton.setPreferredSize(new Dimension(115, 34));
        logoutButton.addActionListener(e -> handleLogout());

        rightPanel.add(userPill);
        rightPanel.add(logoutButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Student Result Management System?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
                loginFrame.toFront();
                loginFrame.requestFocus();
                dispose();
            });
        }
    }

    public void navigateToResultEntry(String rollNumber) {
        tabbedPane.setSelectedIndex(2);
        if (rollNumber != null && !rollNumber.trim().isEmpty()) {
            resultEntryPanel.setStudentRoll(rollNumber.trim());
        }
    }

    public void navigateToResultView(String rollNumber) {
        tabbedPane.setSelectedIndex(3);
        if (rollNumber != null && !rollNumber.trim().isEmpty()) {
            resultViewPanel.loadResultForRoll(rollNumber.trim());
        }
    }

    public void navigateToSubjectManagement() {
        tabbedPane.setSelectedIndex(1);
        subjectManagementPanel.loadSubjectData();
    }

    // Getters for testing/inspection
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public StudentManagementPanel getStudentManagementPanel() {
        return studentManagementPanel;
    }

    public SubjectManagementPanel getSubjectManagementPanel() {
        return subjectManagementPanel;
    }

    public ResultEntryPanel getResultEntryPanel() {
        return resultEntryPanel;
    }

    public ResultViewPanel getResultViewPanel() {
        return resultViewPanel;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
