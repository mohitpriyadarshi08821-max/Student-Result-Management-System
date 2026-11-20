package result.ui;

import result.model.Student;
import result.model.StudentResultSummary;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.io.File;

/**
 * Modern, high-end dialog for Saving and Downloading Marksheets (PDF and HTML).
 * Replaces the dated Swing JFileChooser with an interactive, beautifully styled
 * format and destination selector matching the SaaS dashboard aesthetic.
 * 100% compatible with both CheerpJ WebAssembly browser mode and Desktop Swing.
 */
public class ModernSaveFileDialog {

    private ModernSaveFileDialog() {
        // Utility class
    }

    /**
     * Presents the modern Save/Download dialog and returns the selected File target,
     * or null if the user cancelled.
     */
    public static File showSaveDialog(Component parent,
                                      String title,
                                      StudentResultSummary summary,
                                      String defaultFileName,
                                      String fileTypeLabel,
                                      String extension) {
        final File[] selected = new File[1];

        // Default folder determination (check downloads dir or desktop home)
        String initialFolder = "downloads";
        File downloadsDir = new File("downloads");
        if (!downloadsDir.exists()) {
            try {
                downloadsDir.mkdirs();
            } catch (Exception ignored) {}
        }

        // Clean default filename
        String rawBase = defaultFileName != null ? defaultFileName : "Marksheet";
        if (rawBase.toLowerCase().endsWith(extension.toLowerCase())) {
            rawBase = rawBase.substring(0, rawBase.length() - extension.length());
        }
        final String baseName = rawBase;

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(590, 480));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        boolean isPdf = extension.equalsIgnoreCase(".pdf");
        Color themeColor = isPdf ? new Color(244, 63, 94) : new Color(16, 185, 129);
        Color themeLight = isPdf ? new Color(255, 241, 242) : new Color(236, 253, 245);
        Color btnNormal = isPdf ? new Color(13, 148, 136) : new Color(16, 185, 129);
        Color btnHover = isPdf ? new Color(15, 118, 110) : new Color(5, 150, 105);

        // 1. Header Banner (Dark Navy SaaS gradient with ambient glow)
        JPanel header = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), w, h, new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 25));
                g2.fillOval(w - 120, -30, 160, 160);
                g2.setColor(new Color(51, 65, 85));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel leftHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHdr.setOpaque(false);

        // Icon Badge
        JPanel emblem = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(themeLight);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        emblem.setOpaque(false);
        emblem.setPreferredSize(new Dimension(42, 42));
        emblem.add(new JLabel(isPdf ?
                UIIconFactory.createDocumentIcon(22, new Color(225, 29, 72)) :
                UIIconFactory.createBookIcon(22, new Color(16, 185, 129))));
        leftHdr.add(emblem);

        JPanel textHdr = new JPanel(new GridLayout(2, 1, 0, 2));
        textHdr.setOpaque(false);

        JLabel titleLbl = new JLabel(title != null ? title : (isPdf ? "Save / Download Marksheet as PDF" : "Save / Download Marksheet as HTML"));
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        textHdr.add(titleLbl);

        JLabel subLbl = new JLabel("Configure destination filename and directory to export official document");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(148, 163, 184));
        textHdr.add(subLbl);

        leftHdr.add(textHdr);
        header.add(leftHdr, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // 2. Center Content Area
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Student Info Summary Strip
        RoundedPanel studentCard = new RoundedPanel(10, Color.WHITE, new Color(226, 232, 240), 1);
        studentCard.setLayout(new BorderLayout(12, 0));
        studentCard.setBorder(new EmptyBorder(8, 14, 8, 14));

        Student st = summary != null ? summary.getStudent() : null;
        String name = st != null && st.getFullName() != null ? st.getFullName() : "Student";
        String roll = st != null && st.getRollNumber() != null ? st.getRollNumber() : "N/A";
        String course = st != null && st.getCourse() != null ? st.getCourse() : "Academic Program";
        int sem = st != null ? st.getSemester() : 1;

        JPanel sLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        sLeft.setOpaque(false);
        JLabel sName = new JLabel(name + "  (Roll No: " + roll + ")");
        sName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sName.setForeground(new Color(15, 23, 42));
        sLeft.add(sName);

        JLabel sCourse = new JLabel(course + " • Semester " + sem);
        sCourse.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sCourse.setForeground(new Color(100, 116, 139));
        sLeft.add(sCourse);
        studentCard.add(sLeft, BorderLayout.CENTER);

        JPanel sRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        sRight.setOpaque(false);
        sRight.add(createPill(isPdf ? "PDF Document" : "HTML Transcript", themeColor, themeLight));
        if (summary != null) {
            sRight.add(createPill(String.format("SGPA: %.2f", summary.getSgpa()), new Color(217, 119, 6), new Color(254, 243, 199)));
            String status = summary.getStatus() != null ? summary.getStatus() : "PASS";
            Color statusTextCol = "FAIL".equalsIgnoreCase(status) ? new Color(225, 29, 72) : new Color(16, 185, 129);
            Color statusBgCol = "FAIL".equalsIgnoreCase(status) ? new Color(255, 241, 242) : new Color(236, 253, 245);
            sRight.add(createPill(status, statusTextCol, statusBgCol));
        }
        studentCard.add(sRight, BorderLayout.EAST);
        center.add(studentCard);
        center.add(Box.createVerticalStrut(10));

        // Form Card (Output File Name & Destination Folder)
        RoundedPanel formCard = new RoundedPanel(10, Color.WHITE, new Color(226, 232, 240), 1);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. Output File Name Row
        JLabel fnLabel = new JLabel("Output File Name:");
        fnLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fnLabel.setForeground(new Color(30, 41, 59));
        formCard.add(fnLabel);
        formCard.add(Box.createVerticalStrut(4));

        JPanel fnRow = new JPanel(new BorderLayout(8, 0));
        fnRow.setOpaque(false);

        JTextField fileNameField = new JTextField(baseName);
        fileNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileNameField.setForeground(new Color(15, 23, 42));
        fileNameField.setBackground(new Color(248, 250, 252));
        fileNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        fnRow.add(fileNameField, BorderLayout.CENTER);

        JLabel extBadge = new JLabel(extension.toLowerCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        extBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        extBadge.setForeground(new Color(71, 85, 105));
        extBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        fnRow.add(extBadge, BorderLayout.EAST);
        formCard.add(fnRow);

        formCard.add(Box.createVerticalStrut(10));

        // 2. Destination Folder Row
        JLabel dirLabel = new JLabel("Destination Folder / Directory:");
        dirLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dirLabel.setForeground(new Color(30, 41, 59));
        formCard.add(dirLabel);
        formCard.add(Box.createVerticalStrut(4));

        JTextField folderField = new JTextField(initialFolder);
        folderField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        folderField.setForeground(new Color(15, 23, 42));
        folderField.setBackground(new Color(248, 250, 252));
        folderField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        formCard.add(folderField);

        formCard.add(Box.createVerticalStrut(6));

        // Quick Preset Buttons
        JPanel presetsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        presetsRow.setOpaque(false);
        JLabel pLabel = new JLabel("Quick Presets: ");
        pLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pLabel.setForeground(new Color(100, 116, 139));
        presetsRow.add(pLabel);

        presetsRow.add(createPresetButton("📁 downloads", () -> folderField.setText("downloads")));
        presetsRow.add(createPresetButton("📁 uploads", () -> folderField.setText("uploads")));
        presetsRow.add(createPresetButton("📁 . (Current)", () -> folderField.setText(".")));
        formCard.add(presetsRow);

        formCard.add(Box.createVerticalStrut(10));

        // 3. Live Preview Path Strip
        JPanel previewCard = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        previewCard.setOpaque(false);
        previewCard.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel saveToIcon = new JLabel(UIIconFactory.createDocumentIcon(16, btnNormal));
        previewCard.add(saveToIcon, BorderLayout.WEST);

        JLabel previewPathLabel = new JLabel("Target: " + initialFolder + "/" + baseName + extension);
        previewPathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        previewPathLabel.setForeground(new Color(51, 65, 85));
        previewCard.add(previewPathLabel, BorderLayout.CENTER);

        formCard.add(previewCard);
        center.add(formCard);
        root.add(center, BorderLayout.CENTER);

        // Document Listener for live target preview updates
        Runnable updatePath = () -> {
            String f = folderField.getText().trim();
            String n = fileNameField.getText().trim();
            if (f.isEmpty()) f = ".";
            if (n.isEmpty()) n = baseName;
            if (!n.toLowerCase().endsWith(extension.toLowerCase())) {
                n = n + extension.toLowerCase();
            }
            previewPathLabel.setText("Target: " + f + "/" + n);
        };

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePath.run(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePath.run(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePath.run(); }
        };
        fileNameField.getDocument().addDocumentListener(dl);
        folderField.getDocument().addDocumentListener(dl);

        // 3. Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        footer.setPreferredSize(new Dimension(590, 52));

        JLabel infoLeft = new JLabel(isPdf ? "Adobe PDF (ISO 32000-1) • Ready to export" : "Interactive HTML Transcript • Ready");
        infoLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLeft.setForeground(new Color(100, 116, 139));
        infoLeft.setBorder(new EmptyBorder(0, 18, 0, 0));
        footer.add(infoLeft, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnBar.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 16, 6, 16)
        ));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> {
            selected[0] = null;
            Window w = SwingUtilities.getWindowAncestor(cancelBtn);
            if (w != null) w.dispose();
        });
        btnBar.add(cancelBtn);

        RoundedButton saveBtn = new RoundedButton(isPdf ? "Save & Export PDF" : "Save & Export HTML", 8, btnNormal, btnHover, Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setPreferredSize(new Dimension(150, 34));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String dirStr = folderField.getText().trim();
            if (dirStr.isEmpty()) dirStr = ".";
            String fn = fileNameField.getText().trim();
            if (fn.isEmpty()) {
                fn = baseName;
            }
            if (!fn.toLowerCase().endsWith(extension.toLowerCase())) {
                fn = fn + extension.toLowerCase();
            }

            File dir = new File(dirStr);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            selected[0] = new File(dir, fn);

            Window w = SwingUtilities.getWindowAncestor(saveBtn);
            if (w != null) w.dispose();
        });
        btnBar.add(saveBtn);

        footer.add(btnBar, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        // Display modal via JOptionPane for 100% CheerpJ and Swing compatibility
        JOptionPane.showOptionDialog(
                parent,
                root,
                title != null ? title : (isPdf ? "Save / Download Marksheet as PDF" : "Save / Download Marksheet as HTML"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{},
                null
        );

        return selected[0];
    }

    /**
     * Modern success notification matching dashboard UI after successful export.
     */
    public static void showSuccessDialog(Component parent, File targetFile, boolean isPdf, Runnable onOpen) {
        if (targetFile == null) return;

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(530, 270));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), w, h, new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(16, 185, 129, 30));
                g2.fillOval(w - 90, -25, 120, 120);
                g2.setColor(new Color(51, 65, 85));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHdr.setOpaque(false);

        JPanel emblem = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 45));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(16, 185, 129, 140));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        emblem.setOpaque(false);
        emblem.setPreferredSize(new Dimension(38, 38));
        emblem.add(new JLabel(UIIconFactory.createCheckIcon(20, Color.WHITE)));
        leftHdr.add(emblem);

        JPanel textHdr = new JPanel(new GridLayout(2, 1, 0, 2));
        textHdr.setOpaque(false);
        JLabel titleLbl = new JLabel(isPdf ? "Official PDF Marksheet Ready!" : "Official HTML Report Ready!");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        textHdr.add(titleLbl);

        JLabel subLbl = new JLabel("Document exported and downloaded successfully");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(148, 163, 184));
        textHdr.add(subLbl);
        leftHdr.add(textHdr);
        header.add(leftHdr, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(14, 18, 14, 18));

        RoundedPanel infoCard = new RoundedPanel(10, Color.WHITE, new Color(226, 232, 240), 1);
        infoCard.setLayout(new BorderLayout(10, 0));
        infoCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel pathBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        pathBlock.setOpaque(false);

        JLabel destHeading = new JLabel("File Saved Successfully to:");
        destHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        destHeading.setForeground(new Color(30, 41, 59));
        pathBlock.add(destHeading);

        JLabel pathText = new JLabel(targetFile.getAbsolutePath());
        pathText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pathText.setForeground(new Color(13, 148, 136));
        pathBlock.add(pathText);
        infoCard.add(pathBlock, BorderLayout.CENTER);
        center.add(infoCard);

        root.add(center, BorderLayout.CENTER);

        // Footer
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
            if (w != null) w.dispose();
        });
        footer.add(closeBtn);

        RoundedButton openBtn = new RoundedButton(isPdf ? "Open PDF" : "Open in Browser", 8,
                new Color(16, 185, 129), new Color(5, 150, 105), Color.WHITE);
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openBtn.setPreferredSize(new Dimension(135, 34));
        openBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(openBtn);
            if (w != null) w.dispose();
            if (onOpen != null) {
                SwingUtilities.invokeLater(onOpen);
            }
        });
        footer.add(openBtn);
        root.add(footer, BorderLayout.SOUTH);

        JOptionPane.showOptionDialog(
                parent,
                root,
                "Download Successful",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{},
                null
        );
    }

    /**
     * Attempts to trigger native browser download in CheerpJ WebAssembly environment.
     */
    public static void triggerBrowserDownload(File file) {
        if (file == null) return;
        try {
            boolean isBrowser = "true".equalsIgnoreCase(System.getProperty("browser.mode")) ||
                    System.getProperty("cheerpj.home") != null ||
                    System.getProperty("cheerpj.version") != null;
            if (isBrowser) {
                try {
                    Class<?> globalClass = Class.forName("com.leaningtech.client.Global");
                    java.lang.reflect.Method jsCall = globalClass.getMethod("jsCall", String.class, Object[].class);
                    jsCall.invoke(null, "downloadVirtualFile", new Object[]{file.getPath(), file.getName()});
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
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

    private static JButton createPresetButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(51, 65, 85));
        btn.setBackground(new Color(241, 245, 249));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        return btn;
    }
}
