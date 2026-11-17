package result.ui;

import result.model.Subject;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Mesmerizing modal dialog for adding or updating curriculum subjects,
 * featuring vector icons, dark clean borders, micro-placeholders, and gradient buttons.
 */
@SuppressWarnings({"serial", "this-escape"})
public class SubjectFormDialog extends JDialog {

    private final boolean isUpdateMode;
    private final DashboardFrame parentDashboard;
    private Subject subject;
    private boolean confirmed = false;

    // Inputs
    private ModernTextField subjectCodeField;
    private ModernTextField subjectNameField;
    private ModernTextField maxMarksField;
    private ModernTextField passMarksField;
    private ModernTextField creditsField;

    public SubjectFormDialog(Window owner, Subject subjectToEdit, DashboardFrame parentDashboard) {
        super(owner, subjectToEdit == null ? "Add New Subject" : "Update Subject Record", ModalityType.APPLICATION_MODAL);
        this.subject = subjectToEdit != null ? subjectToEdit : new Subject();
        this.isUpdateMode = subjectToEdit != null && subjectToEdit.getSubjectId() > 0;
        this.parentDashboard = parentDashboard;

        initComponents();
        if (isUpdateMode) {
            populateFields();
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));

        // ==========================================
        // 1. Header Banner (Modern Dark Navy SaaS banner with emerald/teal highlights)
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Modern Dark Navy SaaS banner with emerald/teal ambient highlights
                GradientPaint gp = new GradientPaint(0, 0, new Color(12, 23, 38), w, h, new Color(15, 30, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Ambient Teal glows
                g2.setColor(new Color(13, 148, 136, 35));
                g2.fillOval(w - 95, -30, 140, 140);
                g2.setColor(new Color(13, 148, 136, 20));
                g2.fillOval(w - 180, h - 45, 120, 120);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval((int) (w * 0.45), -40, 90, 90);

                // Bottom subtle border accent
                g2.setColor(new Color(30, 41, 59));
                g2.drawLine(0, h - 1, w, h - 1);

                g2.dispose();
            }
        };
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Header Left: Icon Bubble + Titles
        JPanel leftHeaderBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftHeaderBox.setOpaque(false);

        // Dark Teal rounded emblem matching modern SaaS sidebar logo
        JPanel iconBubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(13, 148, 136));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(new Color(20, 184, 166, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBubble.setOpaque(false);
        iconBubble.setPreferredSize(new Dimension(42, 42));
        iconBubble.setLayout(new GridBagLayout());
        JLabel headerIcon = new JLabel(isUpdateMode ? UIIconFactory.createPencilIcon(22, Color.WHITE) : UIIconFactory.createBookIcon(22, Color.WHITE));
        iconBubble.add(headerIcon);
        leftHeaderBox.add(iconBubble);

        // Titles
        JPanel titleTextBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleTextBox.setOpaque(false);

        JLabel titleLabel = new JLabel(isUpdateMode ? "Update Subject Information" : "Add New Curriculum Subject");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        String subtitleText = isUpdateMode
                ? "Modify course credentials and marks criteria for " + (subject != null && subject.getSubjectCode() != null ? subject.getSubjectCode() : "")
                : "Enter curriculum subject details, course name, and passing criteria";
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));

        titleTextBox.add(titleLabel);
        titleTextBox.add(subtitleLabel);
        leftHeaderBox.add(titleTextBox);

        headerPanel.add(leftHeaderBox, BorderLayout.WEST);

        // Header Right: Status Badge
        JPanel rightHeaderBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rightHeaderBox.setOpaque(false);

        JPanel statusChip = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusChip.setOpaque(false);
        statusChip.setBorder(new EmptyBorder(2, 10, 2, 10));

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(isUpdateMode ? new Color(245, 158, 11) : new Color(16, 185, 129));
        JLabel chipText = new JLabel(isUpdateMode ? "EDIT MODE" : "NEW SUBJECT");
        chipText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chipText.setForeground(Color.WHITE);
        statusChip.add(dot);
        statusChip.add(chipText);
        rightHeaderBox.add(statusChip);

        headerPanel.add(rightHeaderBox, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. Form Body
        // ==========================================
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(18, 26, 18, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 10, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        Color slateIconColor = new Color(100, 116, 139);

        // Row 1: Subject Code
        JPanel codePanel = new JPanel(new BorderLayout(0, 4));
        codePanel.setOpaque(false);
        codePanel.add(createFieldLabel("Subject Code *"), BorderLayout.NORTH);
        subjectCodeField = new ModernTextField(UIIconFactory.createIdCardIcon(16, slateIconColor), "e.g. CS101 or MATH201");
        subjectCodeField.setToolTipText("Unique Subject Code (e.g. CS101)");
        codePanel.add(subjectCodeField, BorderLayout.CENTER);
        bodyPanel.add(codePanel, setGbc(gbc, 0));

        // Row 2: Subject Name
        JPanel namePanel = new JPanel(new BorderLayout(0, 4));
        namePanel.setOpaque(false);
        namePanel.add(createFieldLabel("Subject Name *"), BorderLayout.NORTH);
        subjectNameField = new ModernTextField(UIIconFactory.createBookIcon(16, slateIconColor), "e.g. Data Structures & Algorithms");
        namePanel.add(subjectNameField, BorderLayout.CENTER);
        bodyPanel.add(namePanel, setGbc(gbc, 1));

        // Row 3: Maximum Marks, Passing Marks & Credits (3 columns)
        JPanel marksRow = new JPanel(new GridLayout(1, 3, 12, 0));
        marksRow.setOpaque(false);

        JPanel maxPanel = new JPanel(new BorderLayout(0, 4));
        maxPanel.setOpaque(false);
        maxPanel.add(createFieldLabel("Max Marks *"), BorderLayout.NORTH);
        maxMarksField = new ModernTextField(UIIconFactory.createCalendarIcon(16, slateIconColor), "100");
        maxMarksField.setText("100");
        maxPanel.add(maxMarksField, BorderLayout.CENTER);

        JPanel passPanel = new JPanel(new BorderLayout(0, 4));
        passPanel.setOpaque(false);
        passPanel.add(createFieldLabel("Pass Marks *"), BorderLayout.NORTH);
        passMarksField = new ModernTextField(UIIconFactory.createLayersIcon(16, slateIconColor), "35");
        passMarksField.setText("35");
        passPanel.add(passMarksField, BorderLayout.CENTER);

        JPanel creditsPanel = new JPanel(new BorderLayout(0, 4));
        creditsPanel.setOpaque(false);
        creditsPanel.add(createFieldLabel("Credits *"), BorderLayout.NORTH);
        creditsField = new ModernTextField(UIIconFactory.createAwardIcon(16, slateIconColor), "4");
        creditsField.setText("4");
        creditsPanel.add(creditsField, BorderLayout.CENTER);

        marksRow.add(maxPanel);
        marksRow.add(passPanel);
        marksRow.add(creditsPanel);
        bodyPanel.add(marksRow, setGbc(gbc, 2));

        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. Footer Bar & Buttons
        // ==========================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(14, 24, 16, 24)
        ));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        RoundedButton cancelBtn = RoundedButton.createNeutral("Cancel");
        cancelBtn.setPreferredSize(new Dimension(105, 36));
        cancelBtn.addActionListener(e -> dispose());

        GradientPrimaryButton submitBtn = new GradientPrimaryButton(
                isUpdateMode ? "Update Subject" : "Save Subject",
                UIIconFactory.createCheckIcon(16, Color.WHITE)
        );
        submitBtn.setPreferredSize(new Dimension(165, 36));
        submitBtn.addActionListener(e -> handleSubmit());

        rightActions.add(cancelBtn);
        rightActions.add(submitBtn);
        footerPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Keyboard shortcuts
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().setDefaultButton(submitBtn);

        mainPanel.setPreferredSize(new Dimension(640, 440));
        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(640, 440));
        setLocationRelativeTo(getOwner());
    }

    private void populateFields() {
        if (subject != null) {
            subjectCodeField.setText(subject.getSubjectCode() != null ? subject.getSubjectCode() : "");
            subjectNameField.setText(subject.getSubjectName() != null ? subject.getSubjectName() : "");
            maxMarksField.setText(String.valueOf(subject.getMaxMarks() > 0 ? subject.getMaxMarks() : 100));
            passMarksField.setText(String.valueOf(subject.getPassMarks() > 0 ? subject.getPassMarks() : 35));
            creditsField.setText(String.valueOf(subject.getCredits() > 0 ? subject.getCredits() : 4));
        }
    }

    private void handleSubmit() {
        String code = subjectCodeField.getText().trim();
        String name = subjectNameField.getText().trim();
        String maxStr = maxMarksField.getText().trim();
        String passStr = passMarksField.getText().trim();
        String creditsStr = creditsField.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Subject Code.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            subjectCodeField.requestFocusInWindow();
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Subject Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            subjectNameField.requestFocusInWindow();
            return;
        }

        int max;
        try {
            max = Integer.parseInt(maxStr);
            if (max <= 0 || max > 1000) {
                JOptionPane.showMessageDialog(this, "Maximum Marks must be between 1 and 1000.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                maxMarksField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Maximum Marks must be a valid integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            maxMarksField.requestFocusInWindow();
            return;
        }

        int pass;
        try {
            pass = Integer.parseInt(passStr);
            if (pass <= 0 || pass > max) {
                JOptionPane.showMessageDialog(this, "Passing Marks must be between 1 and Maximum Marks (" + max + ").", "Validation Error", JOptionPane.WARNING_MESSAGE);
                passMarksField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Passing Marks must be a valid integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            passMarksField.requestFocusInWindow();
            return;
        }

        int credits = 4;
        try {
            credits = Integer.parseInt(creditsStr);
            if (credits <= 0 || credits > 20) {
                JOptionPane.showMessageDialog(this, "Credits must be between 1 and 20.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                creditsField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            creditsField.requestFocusInWindow();
            return;
        }

        subject.setSubjectCode(code);
        subject.setSubjectName(name);
        subject.setMaxMarks(max);
        subject.setPassMarks(pass);
        subject.setCredits(credits);

        confirmed = true;
        dispose();
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        return gbc;
    }

    private JLabel createFieldLabel(String text) {
        boolean required = text.endsWith("*");
        String mainText = required ? text.substring(0, text.length() - 1).trim() : text;
        String html = "<html><span style='font-family:Segoe UI; font-weight:bold; font-size:12px; color:#1E293B;'>"
                + mainText + "</span>"
                + (required ? "<span style='font-family:Segoe UI; font-weight:bold; font-size:12px; color:#EF4444;'> *</span>" : "")
                + "</html>";
        JLabel label = new JLabel(html);
        label.setBorder(new EmptyBorder(0, 1, 2, 0));
        return label;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Subject getSubject() {
        return subject;
    }

    public static class ModernTextField extends JTextField {
        private final Icon leadingIcon;
        private final String placeholder;
        private boolean isHovered = false;

        public ModernTextField(Icon leadingIcon, String placeholder) {
            this.leadingIcon = leadingIcon;
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(15, 23, 42));
            setCaretColor(new Color(15, 23, 42));
            setOpaque(false);

            int leftPad = leadingIcon != null ? leadingIcon.getIconWidth() + 18 : 12;
            setBorder(new EmptyBorder(8, leftPad, 8, 12));
            setPreferredSize(new Dimension(280, 36));

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
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;
            boolean focused = hasFocus();

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));

            Color borderColor;
            if (focused) {
                borderColor = new Color(51, 65, 85);
            } else if (isHovered) {
                borderColor = new Color(100, 116, 139);
            } else {
                borderColor = new Color(203, 213, 225);
            }

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(focused ? 1.3f : 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));

            if (leadingIcon != null) {
                int iconY = (h - leadingIcon.getIconHeight()) / 2;
                leadingIcon.paintIcon(this, g2, 10, iconY);
            }

            super.paintComponent(g2);

            if (getText().isEmpty() && placeholder != null) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(148, 163, 184));
                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                int textX = leadingIcon != null ? leadingIcon.getIconWidth() + 18 : 12;
                g2.drawString(placeholder, textX, textY);
            }

            g2.dispose();
        }
    }

    public static class GradientPrimaryButton extends JButton {
        private final Color startColor = new Color(13, 148, 136); // Teal 600 #0D9488
        private final Color endColor = new Color(15, 118, 110);   // Teal 700 #0F766E
        private final Color hoverStart = new Color(20, 184, 166); // Teal 500 #14B8A6
        private final Color hoverEnd = new Color(13, 148, 136);   // Teal 600 #0D9488
        private final Color pressedColor = new Color(17, 94, 89); // Teal 800 #115E59
        private boolean isHovered = false;
        private boolean isPressed = false;

        public GradientPrimaryButton(String text, Icon icon) {
            super(text, icon);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;

            if (isEnabled() && !isPressed) {
                g2.setColor(new Color(13, 148, 136, 45));
                g2.fill(new RoundRectangle2D.Float(1, 2, w - 2, h - 2, arc, arc));
            }

            if (!isEnabled()) {
                g2.setColor(new Color(203, 213, 225));
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 3, arc, arc));
            } else if (isPressed) {
                g2.setColor(pressedColor);
                g2.fill(new RoundRectangle2D.Float(1, 2, w - 2, h - 3, arc, arc));
            } else {
                GradientPaint gp = new GradientPaint(
                        0, 0, isHovered ? hoverStart : startColor,
                        w, h, isHovered ? hoverEnd : endColor
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 3, arc, arc));
            }

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
