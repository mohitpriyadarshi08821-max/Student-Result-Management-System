package result.ui;

import result.database.DatabaseConnection;
import result.dao.UserDAOImpl;
import result.exception.DatabaseException;
import result.exception.ValidationException;
import result.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Professional Java Swing login interface for the Student Result Management System.
 * Connects to AuthService for administrative and faculty authentication.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private RoundedButton loginButton;
    private RoundedButton exitButton;
    private JLabel statusLabel;

    public LoginFrame() {
        this(new AuthService(new UserDAOImpl()));
    }

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        initComponents();
    }

    private void initComponents() {
        setTitle(UIConstants.APP_TITLE + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 530);
        setResizable(false);
        setLocationRelativeTo(null);

        // Main layout container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIConstants.BACKGROUND_COLOR);

        // Header Panel (Top)
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel (Center)
        JPanel formCardPanel = createFormCardPanel();
        mainContainer.add(formCardPanel, BorderLayout.CENTER);

        // Footer Panel (Bottom)
        JPanel footerPanel = createFooterPanel();
        mainContainer.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainContainer);

        // Bind Enter key to default button (Login)
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(2, 1, 0, 6));
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel titleLabel = new JLabel(UIConstants.APP_TITLE, SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Institutional Result Management System • Sign In", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(230, 240, 250));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JPanel createFormCardPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        outerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        RoundedPanel card = new RoundedPanel(new GridBagLayout(), UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND, new Color(226, 232, 240), 1);
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // Row 0: Status message label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(UIConstants.DANGER_COLOR);
        statusLabel.setPreferredSize(new Dimension(340, 24));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(statusLabel, gbc);

        // Row 1: Username Label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UIConstants.FONT_BOLD);
        usernameLabel.setForeground(UIConstants.TEXT_PRIMARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(usernameLabel, gbc);

        // Row 2: Username Field
        usernameField = new JTextField(20);
        usernameField.setFont(UIConstants.FONT_REGULAR);
        usernameField.setForeground(UIConstants.TEXT_PRIMARY);
        usernameField.setPreferredSize(new Dimension(340, 38));
        usernameField.setBorder(createInputBorder());
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(usernameField, gbc);

        // Row 3: Password Label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIConstants.FONT_BOLD);
        passwordLabel.setForeground(UIConstants.TEXT_PRIMARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(passwordLabel, gbc);

        // Row 4: Password Field
        passwordField = new JPasswordField(20);
        passwordField.setFont(UIConstants.FONT_REGULAR);
        passwordField.setForeground(UIConstants.TEXT_PRIMARY);
        passwordField.setPreferredSize(new Dimension(340, 38));
        passwordField.setBorder(createInputBorder());
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(passwordField, gbc);

        // Row 5: Action Buttons (Login & Exit) - Semi-rounded with high-contrast dark font
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        buttonPanel.setOpaque(false);

        loginButton = RoundedButton.createPrimary("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(150, 42));

        exitButton = RoundedButton.createNeutral("Exit");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(150, 42));

        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> handleExit());

        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(buttonPanel, gbc);

        outerPanel.add(card, BorderLayout.CENTER);
        return outerPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        footerPanel.setBorder(new EmptyBorder(6, 10, 14, 10));

        JLabel footerLabel = new JLabel("Default Administrator: admin / admin123", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(127, 140, 141));
        footerPanel.add(footerLabel);

        return footerPanel;
    }

    private Border createInputBorder() {
        return UIConstants.createRoundedBorder(new Color(203, 213, 225), UIConstants.CORNER_RADIUS_SM, 8, 12, 8, 12);
    }

    /**
     * Authenticates the user credentials using the integrated AuthService.
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Requirement 9: Validate that username and password are not empty
        if (username.isEmpty()) {
            setStatus("Please enter your username.", UIConstants.DANGER_COLOR);
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            setStatus("Please enter your password.", UIConstants.DANGER_COLOR);
            passwordField.requestFocusInWindow();
            return;
        }

        loginButton.setEnabled(false);
        setStatus("Authenticating, please wait...", UIConstants.PRIMARY_COLOR);

        // Run authentication asynchronously to prevent freezing Swing UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                DatabaseConnection.getInstance().loadConfiguration();
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean authenticated = get();
                    if (authenticated) {
                        setStatus("Login successful! Loading dashboard...", UIConstants.ACCENT_COLOR);
                        DashboardFrame dashboard = new DashboardFrame(authService);
                        dashboard.setVisible(true);
                        dispose();
                    } else {
                        setStatus("Invalid username or password.", UIConstants.DANGER_COLOR);
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                        loginButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof ValidationException) {
                        setStatus(cause.getMessage(), UIConstants.DANGER_COLOR);
                    } else if (cause instanceof DatabaseException) {
                        setStatus("Database error: Could not reach MySQL.", UIConstants.DANGER_COLOR);
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Unable to connect to MySQL database.\n\n"
                                        + "Please check:\n"
                                        + "1. MySQL Server is active on localhost:3306\n"
                                        + "2. Database 'student_result_db' has been imported (see schema.sql)\n"
                                        + "3. Credentials in database.properties are correct.\n\n"
                                        + "Error: " + cause.getMessage(),
                                "Database Connection Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        setStatus("An unexpected error occurred during login.", UIConstants.DANGER_COLOR);
                    }
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }

    // Component getters for programmatic inspection/testing
    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
