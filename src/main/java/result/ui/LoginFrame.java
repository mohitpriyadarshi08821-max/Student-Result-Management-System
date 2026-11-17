package result.ui;

import result.dao.UserDAOImpl;
import result.database.DatabaseConnection;
import result.exception.DatabaseException;
import result.exception.ValidationException;
import result.service.AuthService;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Modern High-Contrast Campus Login Interface:
 * Features a full-screen campus hallway background with students walking,
 * a clean left-aligned glass card with prominent dark borders and dark typography
 * for maximum readability against bright backgrounds.
 * Branding logos/text have been completely removed as requested.
 */
@SuppressWarnings({"serial", "this-escape"})
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

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
        setSize(1280, 760);
        setMinimumSize(new Dimension(960, 600));
        setResizable(true);

        try {
            setLocationRelativeTo(null);
        } catch (Throwable ignored) {
        }

        try {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } catch (Throwable ignored) {
        }

        // Root full-screen background container
        CampusHallwayBackdropPanel mainContainer = new CampusHallwayBackdropPanel(new BorderLayout());

        // Center area with responsive left-aligned glass login card (no top brand logo/text)
        mainContainer.add(createCenterCardWrapper(), BorderLayout.CENTER);

        setContentPane(mainContainer);

        // Bind Enter key to default button (LOGIN)
        if (loginButton != null) {
            getRootPane().setDefaultButton(loginButton);
        }
    }

    /**
     * Center card wrapper: anchors the glass login card gracefully on the left.
     */
    private JPanel createCenterCardWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 64, 40, 0);

        wrapper.add(createGlassLoginCard(), gbc);
        return wrapper;
    }

    /**
     * High-contrast glass login card with dark borders and dark typography.
     */
    private JPanel createGlassLoginCard() {
        GlassPanel card = new GlassPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(400, 560));
        card.setMinimumSize(new Dimension(360, 520));
        card.setBorder(new EmptyBorder(32, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Headline: "Hey, Student !  🎓" in high-contrast dark charcoal
        JLabel titleLabel = new JLabel("Hey, Student !  \uD83C\uDF93", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42)); // #0F172A
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLabel, gbc);

        // 2. Subtitle: "Log in to continue your journey toward clarity." in dark slate
        JLabel subtitleLabel = new JLabel("Log in to continue your journey toward clarity.", SwingConstants.LEFT);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(51, 65, 85)); // #334155
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(subtitleLabel, gbc);

        // 3. Status feedback label (reserved height)
        statusLabel = new JLabel(" ", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(220, 38, 38)); // Deep red #DC2626
        statusLabel.setPreferredSize(new Dimension(336, 18));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(statusLabel, gbc);

        // 4. Username field row (Dark Person icon + Underlined transparent field)
        JPanel usernameRow = new JPanel(new BorderLayout(14, 0));
        usernameRow.setOpaque(false);
        usernameRow.setBorder(new UnderlineBorder(new Color(30, 41, 59, 220), 1));

        JLabel userIcon = new JLabel(new VectorPersonIcon(new Color(30, 41, 59)));
        userIcon.setBorder(new EmptyBorder(0, 4, 0, 2));
        usernameField = new GlassTextField("Username");
        usernameRow.add(userIcon, BorderLayout.WEST);
        usernameRow.add(usernameField, BorderLayout.CENTER);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(usernameRow, gbc);

        // 5. Password field row (Dark Lock icon + Underlined transparent field + Eye toggle)
        JPanel passwordRow = new JPanel(new BorderLayout(14, 0));
        passwordRow.setOpaque(false);
        passwordRow.setBorder(new UnderlineBorder(new Color(30, 41, 59, 220), 1));

        JLabel lockIcon = new JLabel(new VectorLockIcon(new Color(30, 41, 59)));
        lockIcon.setBorder(new EmptyBorder(0, 4, 0, 2));
        passwordField = new GlassPasswordField("Password");
        JButton eyeToggle = createEyeToggleButton();

        passwordRow.add(lockIcon, BorderLayout.WEST);
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(eyeToggle, BorderLayout.EAST);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(passwordRow, gbc);

        // 6. Action Button: "LOGIN" (High-contrast deep dark slate button with white text)
        loginButton = new RoundedButton(
                "LOGIN",
                new Color(15, 23, 42),       // #0F172A
                new Color(30, 41, 59),       // #1E293B (hover)
                new Color(51, 65, 85),       // #334155 (pressed)
                Color.WHITE,
                null,
                6
        );
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(336, 44));
        loginButton.addActionListener(e -> handleLogin());

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(loginButton, gbc);

        // 7. "Forgot Password ?" clickable link in dark slate
        JLabel forgotPasswordLink = new JLabel("Forgot Password ?", SwingConstants.LEFT);
        forgotPasswordLink.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        forgotPasswordLink.setForeground(new Color(30, 41, 59));
        forgotPasswordLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Please contact your campus administrator to reset your credentials.\n\n"
                                + "Default Administrator Account:\n"
                                + "Username: admin\nPassword: admin123",
                        "Password Assistance",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLink.setText("<html><u>Forgot Password ?</u></html>");
                forgotPasswordLink.setForeground(new Color(37, 99, 235)); // Royal blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLink.setText("Forgot Password ?");
                forgotPasswordLink.setForeground(new Color(30, 41, 59));
            }
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(forgotPasswordLink, gbc);

        // 8. Secondary Exit action and Administrator hint
        JPanel footerPanel = new JPanel(new BorderLayout(0, 8));
        footerPanel.setOpaque(false);

        exitButton = new RoundedButton(
                "Exit",
                new Color(241, 245, 249),
                new Color(226, 232, 240),
                new Color(203, 213, 225),
                new Color(15, 23, 42),
                new Color(15, 23, 42, 160),
                6
        );
        exitButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exitButton.setPreferredSize(new Dimension(336, 34));
        exitButton.addActionListener(e -> handleExit());

        JLabel adminHint = new JLabel("Default Administrator: admin / admin123", SwingConstants.CENTER);
        adminHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        adminHint.setForeground(new Color(71, 85, 105)); // #475569

        footerPanel.add(exitButton, BorderLayout.NORTH);
        footerPanel.add(adminHint, BorderLayout.SOUTH);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(footerPanel, gbc);

        return card;
    }

    private JButton createEyeToggleButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;

                g2.setColor(new Color(71, 85, 105)); // Dark slate eye icon
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Eye outline
                g2.drawArc(cx - 8, cy - 5, 16, 10, 30, 120);
                g2.drawArc(cx - 8, cy - 5, 16, 10, 210, 120);
                // Pupil
                g2.fillOval(cx - 2, cy - 2, 4, 4);

                // Diagonal slash if password hidden
                if (passwordField != null && passwordField.getEchoChar() != (char) 0) {
                    g2.drawLine(cx - 7, cy + 5, cx + 7, cy - 5);
                }

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (passwordField.getEchoChar() != (char) 0) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
            btn.repaint();
        });
        return btn;
    }

    /**
     * Authenticates credentials using AuthService.
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            setStatus("Please enter your username.", new Color(220, 38, 38));
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            setStatus("Please enter your password.", new Color(220, 38, 38));
            passwordField.requestFocusInWindow();
            return;
        }

        loginButton.setEnabled(false);
        setStatus("Authenticating, please wait...", new Color(217, 119, 6));

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
                        setStatus("Login successful! Loading dashboard...", new Color(5, 150, 105));
                        DashboardFrame dashboard = new DashboardFrame(authService);
                        try {
                            dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        } catch (Throwable ignored) {
                        }
                        dashboard.setVisible(true);
                        try {
                            dashboard.toFront();
                            dashboard.requestFocus();
                        } catch (Throwable ignored) {
                        }
                        dispose();
                    } else {
                        setStatus("Invalid username or password.", new Color(220, 38, 38));
                        loginButton.setEnabled(true);
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof ValidationException) {
                        setStatus(cause.getMessage(), new Color(220, 38, 38));
                    } else if (cause instanceof DatabaseException) {
                        setStatus("Database error: Could not reach MySQL.", new Color(220, 38, 38));
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
                        setStatus("An unexpected error occurred during login.", new Color(220, 38, 38));
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

    // Component getters for programmatic inspection & test suites
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

    // --- Custom UI Architectural & Input Components ---

    /**
     * Photorealistic full-screen campus hallway background panel.
     */
    @SuppressWarnings("serial")
    static class CampusHallwayBackdropPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static BufferedImage bgImage;

        static {
            loadBackgroundImage();
        }

        private static void loadBackgroundImage() {
            try (InputStream in = LoginFrame.class.getResourceAsStream("/images/campus_bg.png")) {
                if (in != null) {
                    bgImage = ImageIO.read(in);
                    return;
                }
            } catch (Exception ignored) {
            }

            String[] fallbackPaths = {
                    "src/main/resources/images/campus_bg.png",
                    "target/classes/images/campus_bg.png",
                    "resources/images/campus_bg.png",
                    "campus_bg.png"
            };

            for (String path : fallbackPaths) {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    try {
                        bgImage = ImageIO.read(file);
                        if (bgImage != null) {
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        public CampusHallwayBackdropPanel(BorderLayout layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            if (bgImage != null) {
                // High-fidelity aspect-ratio cover scaling to fill 100% of the screen
                double imgW = bgImage.getWidth();
                double imgH = bgImage.getHeight();
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int drawX = (w - drawW) / 2;
                int drawY = (h - drawH) / 2;

                g2.drawImage(bgImage, drawX, drawY, drawW, drawH, null);

                // Subtle moody cinematic vignette overlay to enhance text readability
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRect(0, 0, w, h);
            } else {
                g2.setColor(new Color(110, 39, 43)); // Rich red pillar base fallback
                g2.fillRect(0, 0, w, h);
            }

            g2.dispose();
        }
    }

    /**
     * Frosted-glass translucent panel with prominent dark border and high-contrast surface.
     */
    @SuppressWarnings("serial")
    static class GlassPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 18; // Rounded corner radius

            // Frosted high-contrast translucent white surface
            g2.setColor(new Color(255, 255, 255, 225));
            g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);

            // Prominent dark defined border stroke
            g2.setColor(new Color(15, 23, 42, 230)); // Dark obsidian/charcoal
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Bottom underline border for sleek modern input fields.
     */
    static class UnderlineBorder implements Border {
        private final Color lineColor;
        private final int thickness;

        public UnderlineBorder(Color color, int thickness) {
            this.lineColor = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(lineColor);
            g2.fillRect(x, y + height - thickness, width, thickness);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 8, 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /**
     * Transparent glass text field with dark text, dark caret, and custom placeholder.
     */
    @SuppressWarnings("serial")
    static class GlassTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private final String placeholder;

        public GlassTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(new Color(15, 23, 42)); // High-contrast dark charcoal #0F172A
            setCaretColor(new Color(15, 23, 42));
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(new EmptyBorder(4, 12, 4, 6));
            setMargin(new Insets(4, 12, 4, 6));
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

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
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(100, 116, 139)); // Slate gray placeholder #64748B
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                Insets insets = getInsets();
                int x = (insets != null) ? insets.left : 12;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    /**
     * Transparent glass password field with dark text, dark caret, and custom placeholder.
     */
    @SuppressWarnings("serial")
    static class GlassPasswordField extends JPasswordField {
        private static final long serialVersionUID = 1L;
        private final String placeholder;

        public GlassPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(new Color(15, 23, 42)); // High-contrast dark charcoal #0F172A
            setCaretColor(new Color(15, 23, 42));
            setEchoChar('•');
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(new EmptyBorder(4, 12, 4, 6));
            setMargin(new Insets(4, 12, 4, 6));
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

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
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(100, 116, 139)); // Slate gray placeholder #64748B
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                Insets insets = getInsets();
                int x = (insets != null) ? insets.left : 12;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    /**
     * Vector Person outline icon matching the user field design.
     */
    static class VectorPersonIcon implements Icon {
        private final Color iconColor;

        public VectorPersonIcon(Color color) {
            this.iconColor = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Head circle
            g2.drawOval(x + 5, y + 1, 9, 9);
            // Shoulders arc
            g2.drawArc(x + 1, y + 11, 17, 12, 0, 180);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }

    /**
     * Vector Padlock outline icon matching the password field design.
     */
    static class VectorLockIcon implements Icon {
        private final Color iconColor;

        public VectorLockIcon(Color color) {
            this.iconColor = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Shackle arc
            g2.drawArc(x + 4, y + 1, 11, 12, 0, 180);
            // Lock body
            g2.drawRoundRect(x + 2, y + 8, 15, 11, 3, 3);
            // Keyhole circle
            g2.fillOval(x + 8, y + 11, 3, 3);
            g2.drawLine(x + 9, y + 14, x + 9, y + 16);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }
}
