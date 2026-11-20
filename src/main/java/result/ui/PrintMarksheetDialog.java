package result.ui;

import result.model.Student;
import result.model.StudentResultSummary;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern, high-end modal dialog for Printing and Exporting Student Marksheets.
 * Provides interactive, beautifully styled format cards for Adobe PDF,
 * Interactive HTML Transcript, and Hardware Printing.
 * Compatible with both CheerpJ WebAssembly browser execution and Native Swing desktop mode.
 */
@SuppressWarnings({"serial", "this-escape"})
public class PrintMarksheetDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public PrintMarksheetDialog(Window owner, StudentResultSummary summary,
                                Runnable onDownloadPdf, Runnable onDownloadHtml, Runnable onPrintPhysical) {
        super(owner, "Print / Download Marksheet", ModalityType.APPLICATION_MODAL);
        initComponents(summary, onDownloadPdf, onDownloadHtml, onPrintPhysical);
    }

    private void initComponents(StudentResultSummary summary,
                                Runnable onDownloadPdf, Runnable onDownloadHtml, Runnable onPrintPhysical) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(buildModernPanel(summary, onDownloadPdf, onDownloadHtml, onPrintPhysical));
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Shows the modern print and download dialog.
     * Uses JOptionPane to guarantee 100% reliable rendering inside CheerpJ WebAssembly
     * as well as standard desktop Java Swing.
     */
    public static void show(Component parent, StudentResultSummary summary,
                            Runnable onDownloadPdf, Runnable onDownloadHtml, Runnable onPrintPhysical) {
        JPanel modernPanel = buildModernPanel(summary, onDownloadPdf, onDownloadHtml, onPrintPhysical);
        JOptionPane.showOptionDialog(
                parent,
                modernPanel,
                "Print / Download Marksheet",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{},
                null
        );
    }

    public static JPanel buildModernPanel(StudentResultSummary summary,
                                          Runnable onDownloadPdf, Runnable onDownloadHtml, Runnable onPrintPhysical) {
        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(580, 460));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // 1. Top Header Banner (Dark Navy SaaS gradient with ambient highlights)
        JPanel header = createHeaderBanner();
        root.add(header, BorderLayout.NORTH);

        // 2. Center Content Area (Student Profile Strip + 3 Modern Format Cards)
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Student Info Summary Strip
        center.add(createStudentInfoStrip(summary));
        center.add(Box.createVerticalStrut(10));

        // Format Selection Cards
        center.add(createOptionCard(
                "Adobe PDF Marksheet",
                "ISO 32000-1 binary vector document with official seal, ideal for print & archiving",
                new Color(244, 63, 94),          // Rose icon
                new Color(255, 241, 242),        // Light Rose bg
                UIIconFactory.createDocumentIcon(22, new Color(225, 29, 72)),
                "Export PDF",
                new Color(13, 148, 136),         // Teal button
                new Color(15, 118, 110),
                onDownloadPdf
        ));

        center.add(Box.createVerticalStrut(8));

        center.add(createOptionCard(
                "Interactive HTML Transcript",
                "Standalone responsive web marksheet viewable in Chrome, Edge, Safari, and Firefox",
                new Color(16, 185, 129),         // Emerald icon
                new Color(236, 253, 245),        // Light Emerald bg
                UIIconFactory.createBookIcon(22, new Color(16, 185, 129)),
                "Export HTML",
                new Color(16, 185, 129),         // Emerald button
                new Color(5, 150, 105),
                onDownloadHtml
        ));

        center.add(Box.createVerticalStrut(8));

        center.add(createOptionCard(
                "Hardware / Physical Printer",
                "Opens system print dialogue to output directly to local or network printers",
                new Color(79, 70, 229),          // Indigo icon
                new Color(238, 242, 255),        // Light Indigo bg
                UIIconFactory.createPrinterIcon(22, new Color(79, 70, 229)),
                "Print Now",
                new Color(79, 70, 229),          // Indigo button
                new Color(67, 56, 202),
                onPrintPhysical
        ));

        root.add(center, BorderLayout.CENTER);

        // 3. Bottom Footer Strip (Close)
        JPanel footer = createFooter();
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private static JPanel createHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), w, h, new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Subtle ambient glow
                g2.setColor(new Color(13, 148, 136, 30));
                g2.fillOval(w - 110, -30, 150, 150);
                g2.setColor(new Color(59, 130, 246, 20));
                g2.fillOval(w - 200, h - 40, 120, 120);

                // Bottom divider
                g2.setColor(new Color(51, 65, 85));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        banner.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        // Rounded Icon Emblem
        JPanel emblem = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(13, 148, 136, 45));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(13, 148, 136, 140));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        emblem.setOpaque(false);
        emblem.setPreferredSize(new Dimension(42, 42));
        emblem.add(new JLabel(UIIconFactory.createPrinterIcon(22, Color.WHITE)));
        left.add(emblem);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);

        JLabel titleLabel = new JLabel("Print / Download Marksheet");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        text.add(titleLabel);

        JLabel subLabel = new JLabel("Select output format or send to hardware printer");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(148, 163, 184));
        text.add(subLabel);

        left.add(text);
        banner.add(left, BorderLayout.WEST);

        return banner;
    }

    private static JPanel createStudentInfoStrip(StudentResultSummary summary) {
        RoundedPanel panel = new RoundedPanel(10, Color.WHITE, new Color(226, 232, 240), 1);
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(8, 14, 8, 14));

        Student st = summary != null ? summary.getStudent() : null;
        String name = st != null && st.getFullName() != null ? st.getFullName() : "Student";
        String roll = st != null && st.getRollNumber() != null ? st.getRollNumber() : "N/A";
        String course = st != null && st.getCourse() != null ? st.getCourse() : "Academic Program";
        int sem = st != null ? st.getSemester() : 1;

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel nameLabel = new JLabel(name + "  (Roll No: " + roll + ")");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(15, 23, 42));
        left.add(nameLabel);

        JLabel courseLabel = new JLabel(course + " • Semester " + sem);
        courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        courseLabel.setForeground(new Color(100, 116, 139));
        left.add(courseLabel);

        panel.add(left, BorderLayout.CENTER);

        // Right side: Quick stats pills
        if (summary != null) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
            right.setOpaque(false);

            String scoreText = String.format("%.1f%%", summary.getPercentage());
            right.add(createPill("Score: " + scoreText, new Color(37, 99, 235), new Color(239, 246, 255)));

            String sgpaText = String.format("SGPA: %.2f", summary.getSgpa());
            right.add(createPill(sgpaText, new Color(217, 119, 6), new Color(254, 243, 199)));

            String status = summary.getStatus() != null ? summary.getStatus() : "PASS";
            Color statusTextCol = "FAIL".equalsIgnoreCase(status) ? new Color(225, 29, 72) : new Color(16, 185, 129);
            Color statusBgCol = "FAIL".equalsIgnoreCase(status) ? new Color(255, 241, 242) : new Color(236, 253, 245);
            right.add(createPill(status, statusTextCol, statusBgCol));

            panel.add(right, BorderLayout.EAST);
        }

        return panel;
    }

    private static JLabel createPill(String text, Color textCol, Color bgCol) {
        JLabel pill = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCol);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(textCol.getRed(), textCol.getGreen(), textCol.getBlue(), 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(textCol);
        pill.setBorder(new EmptyBorder(3, 7, 3, 7));
        return pill;
    }

    private static JPanel createOptionCard(String title, String description,
                                           Color themeColor, Color iconBg, javax.swing.Icon icon,
                                           String buttonLabel, Color btnBg, Color btnHover, Runnable action) {
        RoundedPanel card = new RoundedPanel(10, Color.WHITE, new Color(226, 232, 240), 1);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Left Icon Emblem
        JPanel iconEmblem = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        iconEmblem.setOpaque(false);
        iconEmblem.setPreferredSize(new Dimension(40, 40));
        iconEmblem.add(new JLabel(icon));
        card.add(iconEmblem, BorderLayout.WEST);

        // Center: Title & Description
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(15, 23, 42));
        textPanel.add(titleLabel);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(100, 116, 139));
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // Right: Primary Action Button
        RoundedButton btn = new RoundedButton(buttonLabel, 6, btnBg, btnHover, Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(108, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Runnable execute = () -> {
            Window w = SwingUtilities.getWindowAncestor(card);
            if (w != null) {
                w.dispose();
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    if (action != null) action.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        };

        btn.addActionListener(e -> execute.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 3));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btn);
        card.add(btnWrapper, BorderLayout.EAST);

        // Hover Effect on the entire card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                execute.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackgroundColor(new Color(250, 252, 255));
                card.setBorderColor(themeColor);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackgroundColor(Color.WHITE);
                card.setBorderColor(new Color(226, 232, 240));
                card.repaint();
            }
        });

        return card;
    }

    private static JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setForeground(new Color(71, 85, 105));
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 16, 6, 16)
        ));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(closeBtn);
            if (w != null) {
                w.dispose();
            }
        });
        footer.add(closeBtn);

        return footer;
    }
}
