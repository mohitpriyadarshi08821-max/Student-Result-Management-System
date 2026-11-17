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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main application window hosting a header, user session information,
 * logout option, and tabbed panels for Student Management, Subject Management,
 * Marks Entry, and Result Viewing.
 */
@SuppressWarnings({"serial", "this-escape"})
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

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
        setSize(1024, 680);
        setMinimumSize(new Dimension(800, 560));
        setResizable(true);
        try {
            setLocationRelativeTo(null);
        } catch (Throwable ignored) {
        }
        if (!MainApplication.isBrowserEnvironment()) {
            try {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            } catch (Throwable ignored) {
            }
        }

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIConstants.BACKGROUND_COLOR);

        // Instantiate the four main panels
        studentManagementPanel = new StudentManagementPanel(studentService, this);
        subjectManagementPanel = new SubjectManagementPanel(subjectService, this);
        resultEntryPanel = new ResultEntryPanel(resultService, studentService);
        resultEntryPanel.setParentDashboard(this);
        resultViewPanel = new ResultViewPanel(resultService);
        resultViewPanel.setParentDashboard(this);

        // Top header banner with branding and system admin / logout actions
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // Edge-to-edge tabbed container matching modern SaaS single-window layout
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setOpaque(true);

        // Flat SaaS tabbed pane UI with dark slate text and Dark Teal / Emerald underline
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = Color.WHITE;
                lightHighlight = Color.WHITE;
                shadow = Color.WHITE;
                darkShadow = Color.WHITE;
                focus = new Color(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(13, 148, 136)); // Dark Teal #0D9488
                    g2.fillRect(x + 12, y + h - 3, w - 24, 3);
                    g2.dispose();
                }
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(226, 232, 240)); // #E2E8F0 bottom divider line
                g2.drawLine(x, y, x + w, y);
                g2.dispose();
            }

            @Override
            protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            }

            @Override
            protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            }

            @Override
            protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, java.awt.FontMetrics metrics,
                                     int tabIndex, String title, java.awt.Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                java.util.Map<?, ?> desktopHints = (java.util.Map<?, ?>) java.awt.Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
                if (desktopHints != null) {
                    g2.addRenderingHints(desktopHints);
                } else {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                }
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                if (isSelected) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.setColor(new Color(15, 23, 42)); // #0F172A
                } else {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2.setColor(new Color(100, 116, 139)); // #64748B
                }
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int ty = textRect.y + ((textRect.height - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(title, textRect.x, ty);
                if (isSelected) {
                    // Micro-reinforce for subpixel stroke completeness on bold diagonal stems (e.g. 'y')
                    Graphics2D gReinforce = (Graphics2D) g2.create();
                    gReinforce.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.45f));
                    gReinforce.drawString(title, textRect.x + 0.35f, (float) ty);
                    gReinforce.dispose();
                }
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, java.awt.Rectangle[] rects,
                                               int tabIndex, java.awt.Rectangle iconRect, java.awt.Rectangle textRect, boolean isSelected) {
                // No dashed focus border
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 44;
            }

            @Override
            protected java.awt.Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new java.awt.Insets(0, 20, 0, 20);
            }
        });

        tabbedPane.addTab("Student Management", studentManagementPanel);
        tabbedPane.addTab("Subject Management", subjectManagementPanel);
        tabbedPane.addTab("Result Entry", resultEntryPanel);
        tabbedPane.addTab("View Results & Marksheets", resultViewPanel);

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
        headerPanel.setBackground(new Color(11, 19, 43)); // Deep Dark Navy #0B132B / #0F172A
        headerPanel.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left Branding
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel appTitleLabel = new JLabel("Student Result Management System");
        appTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appTitleLabel.setForeground(Color.WHITE);

        JLabel subTitleLabel = new JLabel("Institutional Examination & Academic Performance Management");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(148, 163, 184)); // Muted slate #94A3B8

        brandPanel.add(appTitleLabel);
        brandPanel.add(subTitleLabel);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right User Badge & Logout Option
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        String username = authService.getCurrentUser()
                .map(User::getFullName)
                .orElse("System Administrator");

        JLabel userBadge = new JLabel(username, SwingConstants.CENTER);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userBadge.setForeground(Color.WHITE);
        userBadge.setBorder(new EmptyBorder(6, 16, 6, 16));

        RoundedPanel userPill = new RoundedPanel(new BorderLayout(), 8, new Color(30, 41, 59), new Color(51, 65, 85), 1);
        userPill.add(userBadge, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 226, 226)); // Soft light rose #FEE2E2
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(254, 202, 202)); // #FECACA
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setForeground(new Color(185, 28, 28)); // Deep red #B91C1C
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(new EmptyBorder(6, 18, 6, 18));
        logoutButton.setPreferredSize(new Dimension(84, 32));
        logoutButton.addActionListener(e -> handleLogout());

        rightPanel.add(userPill);
        rightPanel.add(logoutButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    public void handleLogout() {
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

    public void navigateToStudentManagement() {
        tabbedPane.setSelectedIndex(0);
        studentManagementPanel.loadStudentData();
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
