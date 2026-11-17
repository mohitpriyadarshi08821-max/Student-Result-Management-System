package result.ui;

import result.model.Student;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.Period;

/**
 * Modern, commercial-grade modal dialog for Adding or Updating student records.
 * Features vector icons, dark clean borders, micro-placeholders, and high-contrast typography,
 * preserving all attributes (Roll No, Names, Email, Course, Semester, Gender, Age).
 */
@SuppressWarnings({"serial", "this-escape"})
public class StudentFormDialog extends JDialog {

    private final boolean isUpdateMode;
    private final DashboardFrame parentDashboard;
    private Student student;
    private boolean confirmed = false;

    // Fields
    private ModernTextField rollNumberField;
    private ModernTextField firstNameField;
    private ModernTextField lastNameField;
    private ModernTextField emailField;
    private ModernComboBox<String> courseComboBox;
    private ModernComboBox<Integer> semesterComboBox;
    private ModernComboBox<String> genderComboBox;
    private ModernTextField ageField;

    public StudentFormDialog(Window owner, Student studentToEdit, DashboardFrame parentDashboard) {
        super(owner, studentToEdit == null ? "Add New Student" : "Update Student Record", ModalityType.APPLICATION_MODAL);
        this.student = studentToEdit != null ? studentToEdit : new Student();
        this.isUpdateMode = studentToEdit != null && studentToEdit.getStudentId() > 0;
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
        // 1. Header Banner
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
        JLabel headerIcon = new JLabel(isUpdateMode ? UIIconFactory.createPencilIcon(22, Color.WHITE) : UIIconFactory.createUserPlusIcon(22, Color.WHITE));
        iconBubble.add(headerIcon);
        leftHeaderBox.add(iconBubble);

        // Titles
        JPanel titleTextBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleTextBox.setOpaque(false);

        JLabel titleLabel = new JLabel(isUpdateMode ? "Update Student Information" : "Add New Student Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        String subtitleText = isUpdateMode
                ? "Modify credentials and academic details for student " + (student != null && student.getRollNumber() != null ? "#" + student.getRollNumber() : "")
                : "Enter student academic and personal details to register";
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));

        titleTextBox.add(titleLabel);
        titleTextBox.add(subtitleLabel);
        leftHeaderBox.add(titleTextBox);

        headerPanel.add(leftHeaderBox, BorderLayout.WEST);

        // Header Right: Sleek Status Badge
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
        JLabel chipText = new JLabel(isUpdateMode ? "EDIT MODE" : "NEW RECORD");
        chipText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chipText.setForeground(Color.WHITE);
        statusChip.add(dot);
        statusChip.add(chipText);
        rightHeaderBox.add(statusChip);

        headerPanel.add(rightHeaderBox, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. Form Body (Crisp, balanced layout)
        // ==========================================
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(14, 26, 14, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        Color slateIconColor = new Color(100, 116, 139);

        // Row 1: Roll Number
        JPanel rollPanel = new JPanel(new BorderLayout(0, 4));
        rollPanel.setOpaque(false);
        rollPanel.add(createFieldLabel("Roll Number *"), BorderLayout.NORTH);
        rollNumberField = new ModernTextField(UIIconFactory.createIdCardIcon(16, slateIconColor), "e.g. 101 or CS2026-001");
        rollNumberField.setToolTipText("Unique Roll Number (e.g. 101, CS2026-001)");
        rollPanel.add(rollNumberField, BorderLayout.CENTER);
        bodyPanel.add(rollPanel, setGbc(gbc, 0));

        // Row 2: First Name & Last Name (2 columns)
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 16, 0));
        nameRow.setOpaque(false);

        JPanel fnPanel = new JPanel(new BorderLayout(0, 4));
        fnPanel.setOpaque(false);
        fnPanel.add(createFieldLabel("First Name *"), BorderLayout.NORTH);
        firstNameField = new ModernTextField(UIIconFactory.createSingleUserIcon(16, slateIconColor), "Enter first name");
        fnPanel.add(firstNameField, BorderLayout.CENTER);

        JPanel lnPanel = new JPanel(new BorderLayout(0, 4));
        lnPanel.setOpaque(false);
        lnPanel.add(createFieldLabel("Last Name *"), BorderLayout.NORTH);
        lastNameField = new ModernTextField(UIIconFactory.createSingleUserIcon(16, slateIconColor), "Enter last name");
        lnPanel.add(lastNameField, BorderLayout.CENTER);

        nameRow.add(fnPanel);
        nameRow.add(lnPanel);
        bodyPanel.add(nameRow, setGbc(gbc, 1));

        // Row 3: Email Address & Age (2 columns)
        JPanel emailAgeRow = new JPanel(new GridLayout(1, 2, 16, 0));
        emailAgeRow.setOpaque(false);

        JPanel emPanel = new JPanel(new BorderLayout(0, 4));
        emPanel.setOpaque(false);
        emPanel.add(createFieldLabel("Email Address"), BorderLayout.NORTH);
        emailField = new ModernTextField(UIIconFactory.createMailIcon(16, slateIconColor), "student@university.edu");
        emPanel.add(emailField, BorderLayout.CENTER);

        JPanel agePanel = new JPanel(new BorderLayout(0, 4));
        agePanel.setOpaque(false);
        agePanel.add(createFieldLabel("Age"), BorderLayout.NORTH);
        ageField = new ModernTextField(UIIconFactory.createCalendarIcon(16, slateIconColor), "e.g. 20");
        ageField.setToolTipText("Student Age (e.g. 20)");
        agePanel.add(ageField, BorderLayout.CENTER);

        emailAgeRow.add(emPanel);
        emailAgeRow.add(agePanel);
        bodyPanel.add(emailAgeRow, setGbc(gbc, 2));

        // Row 4: Course / Department
        JPanel coursePanel = new JPanel(new BorderLayout(0, 4));
        coursePanel.setOpaque(false);
        coursePanel.add(createFieldLabel("Course / Department *"), BorderLayout.NORTH);
        String[] courses = {
                "B.Tech Computer Science",
                "B.Tech Information Technology",
                "B.Tech Electronics & Comm",
                "BCA (Computer Applications)",
                "MCA (Master of Computer Apps)",
                "B.Sc Computer Science",
                "M.Tech Software Engineering"
        };
        courseComboBox = new ModernComboBox<>(courses, UIIconFactory.createGraduationCapIcon(16, slateIconColor));
        courseComboBox.setEditable(true);
        Component editorComp = courseComboBox.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            JTextField tf = (JTextField) editorComp;
            tf.setOpaque(false);
            tf.setBorder(new EmptyBorder(0, 32, 0, 4));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setForeground(new Color(15, 23, 42));
            tf.setCaretColor(new Color(15, 23, 42));
        }
        coursePanel.add(courseComboBox, BorderLayout.CENTER);
        bodyPanel.add(coursePanel, setGbc(gbc, 3));

        // Row 5: Semester & Gender (2 columns)
        JPanel semGenderRow = new JPanel(new GridLayout(1, 2, 16, 0));
        semGenderRow.setOpaque(false);

        JPanel semPanel = new JPanel(new BorderLayout(0, 4));
        semPanel.setOpaque(false);
        semPanel.add(createFieldLabel("Semester *"), BorderLayout.NORTH);
        Integer[] sems = {1, 2, 3, 4, 5, 6, 7, 8};
        semesterComboBox = new ModernComboBox<>(sems, UIIconFactory.createLayersIcon(16, slateIconColor));
        semPanel.add(semesterComboBox, BorderLayout.CENTER);

        JPanel genPanel = new JPanel(new BorderLayout(0, 4));
        genPanel.setOpaque(false);
        genPanel.add(createFieldLabel("Gender"), BorderLayout.NORTH);
        String[] genders = {"Male", "Female", "Other"};
        genderComboBox = new ModernComboBox<>(genders, UIIconFactory.createGenderIcon(16, slateIconColor));
        genPanel.add(genderComboBox, BorderLayout.CENTER);

        semGenderRow.add(semPanel);
        semGenderRow.add(genPanel);
        bodyPanel.add(semGenderRow, setGbc(gbc, 4));

        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. Footer Bar & Action Buttons
        // ==========================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(14, 24, 16, 24)
        ));

        // Left Navigation Actions (Edit mode)
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftNav.setOpaque(false);
        if (isUpdateMode && parentDashboard != null) {
            RoundedButton enterMarksBtn = new RoundedButton("Enter Marks", new Color(12, 23, 38), new Color(30, 41, 59), new Color(15, 23, 42), Color.WHITE, new Color(30, 41, 59), 6);
            enterMarksBtn.setPreferredSize(new Dimension(115, 36));
            enterMarksBtn.addActionListener(e -> {
                String roll = rollNumberField.getText().trim();
                dispose();
                parentDashboard.navigateToResultEntry(roll);
            });

            RoundedButton viewResultBtn = new RoundedButton("View Result", new Color(13, 148, 136), new Color(15, 118, 110), new Color(17, 94, 89), Color.WHITE, new Color(15, 118, 110), 6);
            viewResultBtn.setPreferredSize(new Dimension(115, 36));
            viewResultBtn.addActionListener(e -> {
                String roll = rollNumberField.getText().trim();
                dispose();
                parentDashboard.navigateToResultView(roll);
            });

            leftNav.add(enterMarksBtn);
            leftNav.add(viewResultBtn);
        }
        footerPanel.add(leftNav, BorderLayout.WEST);

        // Right Save & Cancel Buttons
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        RoundedButton cancelBtn = RoundedButton.createNeutral("Cancel");
        cancelBtn.setPreferredSize(new Dimension(105, 36));
        cancelBtn.addActionListener(e -> dispose());

        GradientPrimaryButton submitBtn = new GradientPrimaryButton(
                isUpdateMode ? "Update Student" : "Save Student",
                UIIconFactory.createCheckIcon(16, Color.WHITE)
        );
        submitBtn.setPreferredSize(new Dimension(165, 36));
        submitBtn.addActionListener(e -> handleSubmit());

        rightActions.add(cancelBtn);
        rightActions.add(submitBtn);
        footerPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Keyboard shortcuts: ESC to close, ENTER on fields to submit
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().setDefaultButton(submitBtn);

        // Ensure spacious content dimension so Semester & Gender are never covered
        mainPanel.setPreferredSize(new Dimension(680, 580));
        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(680, 580));
        setLocationRelativeTo(getOwner());
    }

    private void populateFields() {
        if (student != null) {
            rollNumberField.setText(student.getRollNumber() != null ? student.getRollNumber() : "");
            firstNameField.setText(student.getFirstName() != null ? student.getFirstName() : "");
            lastNameField.setText(student.getLastName() != null ? student.getLastName() : "");
            emailField.setText(student.getEmail() != null ? student.getEmail() : "");

            if (student.getCourse() != null) {
                courseComboBox.setSelectedItem(student.getCourse());
            }

            semesterComboBox.setSelectedItem(student.getSemester() > 0 ? student.getSemester() : 1);

            if (student.getGender() != null) {
                genderComboBox.setSelectedItem(student.getGender());
            }

            if (student.getDateOfBirth() != null) {
                int age = Period.between(student.getDateOfBirth(), LocalDate.now()).getYears();
                ageField.setText(String.valueOf(age));
            } else {
                int defaultAge = 18 + (student.getSemester() > 0 ? student.getSemester() / 2 : 1);
                ageField.setText(String.valueOf(defaultAge));
            }
        }
    }

    private void handleSubmit() {
        String roll = rollNumberField.getText().trim();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String course = courseComboBox.getSelectedItem() != null ? courseComboBox.getSelectedItem().toString().trim() : "";
        int sem = semesterComboBox.getSelectedItem() instanceof Integer ? (Integer) semesterComboBox.getSelectedItem() : 1;
        String gender = genderComboBox.getSelectedItem() != null ? genderComboBox.getSelectedItem().toString() : "Male";

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Roll Number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            rollNumberField.requestFocusInWindow();
            return;
        }

        if (first.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter First Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            firstNameField.requestFocusInWindow();
            return;
        }

        if (last.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Last Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            lastNameField.requestFocusInWindow();
            return;
        }

        if (course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter Course / Department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            courseComboBox.requestFocusInWindow();
            return;
        }

        student.setRollNumber(roll);
        student.setFirstName(first);
        student.setLastName(last);
        student.setEmail(email);
        student.setCourse(course);
        student.setSemester(sem);
        student.setGender(gender);

        String ageStr = ageField.getText().trim();
        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                if (age > 0 && age < 120) {
                    student.setDateOfBirth(LocalDate.now().minusYears(age));
                }
            } catch (NumberFormatException ignored) {
            }
        }

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

    public Student getStudent() {
        return student;
    }

    // ==========================================
    // Custom Mesmerizing Form Components
    // ==========================================

    /**
     * Modern text field with smooth semi-rounded borders, leading vector icon,
     * clean dark/black border consistency, and micro-placeholders.
     */
    public static class ModernTextField extends JTextField {
        private final Icon leadingIcon;
        private final String placeholder;
        private boolean isHovered = false;

        public ModernTextField(Icon leadingIcon, String placeholder) {
            this.leadingIcon = leadingIcon;
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(15, 23, 42));
            setCaretColor(new Color(15, 23, 42)); // Black/charcoal caret
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

            // Background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));

            // Clean dark slate / black border like all other boxes (no blue outline)
            Color borderColor;
            if (focused) {
                borderColor = new Color(51, 65, 85); // Slate-700 / dark charcoal #334155
            } else if (isHovered) {
                borderColor = new Color(100, 116, 139); // Slate-500 #64748B
            } else {
                borderColor = new Color(203, 213, 225); // Slate-300 #CBD5E1
            }

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(focused ? 1.3f : 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));

            // Draw leading icon
            if (leadingIcon != null) {
                int iconY = (h - leadingIcon.getIconHeight()) / 2;
                leadingIcon.paintIcon(this, g2, 10, iconY);
            }

            // Paint text / caret
            super.paintComponent(g2);

            // Placeholder: Show whenever text is empty (even when focused, until user types)
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

    /**
     * Modern JComboBox with smooth semi-rounded borders, custom sleek chevron arrow,
     * and leading vector icon support.
     */
    public static class ModernComboBox<T> extends JComboBox<T> {
        private final Icon leadingIcon;
        private boolean isHovered = false;

        public ModernComboBox(T[] items, Icon leadingIcon) {
            super(items);
            this.leadingIcon = leadingIcon;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(15, 23, 42));
            setBackground(Color.WHITE);
            setOpaque(false);
            setPreferredSize(new Dimension(280, 36));

            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            int w = getWidth();
                            int h = getHeight();
                            Color arrowColor = getModel().isRollover() || hasFocus() ? new Color(51, 65, 85) : new Color(100, 116, 139);
                            g2.setColor(arrowColor);
                            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int cx = w / 2 - 2;
                            int cy = h / 2 - 1;
                            g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                            g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                            g2.dispose();
                        }
                    };
                    btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
                    btn.setContentAreaFilled(false);
                    btn.setFocusPainted(false);
                    btn.setOpaque(false);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return btn;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // Suppress default background
                }

                @Override
                public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                    int iconPad = leadingIcon != null ? leadingIcon.getIconWidth() + 16 : 8;
                    Rectangle shifted = new Rectangle(bounds.x + iconPad, bounds.y, Math.max(0, bounds.width - iconPad), bounds.height);
                    super.paintCurrentValue(g, shifted, hasFocus);
                }

                @Override
                protected ComboPopup createPopup() {
                    BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                    popup.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
                    return popup;
                }
            });

            // Clean custom item renderer with soft selection
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    int leftPad = (index < 0) ? 0 : 10;
                    label.setBorder(new EmptyBorder(6, leftPad, 6, 10));
                    label.setOpaque(index >= 0); // Transparent when painted on combobox button face, opaque in popup list
                    if (isSelected) {
                        label.setBackground(new Color(238, 242, 255));
                        label.setForeground(new Color(55, 65, 225));
                    } else {
                        label.setBackground(Color.WHITE);
                        label.setForeground(new Color(15, 23, 42));
                    }
                    return label;
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

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Primary action CTA button with rich Emerald/Teal gradient,
     * crisp vector checkmark, and subtle drop shadow.
     */
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

            // Subtle drop shadow
            if (isEnabled() && !isPressed) {
                g2.setColor(new Color(13, 148, 136, 45));
                g2.fill(new RoundRectangle2D.Float(1, 2, w - 2, h - 2, arc, arc));
            }

            // Background
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

            // Icon & text
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
