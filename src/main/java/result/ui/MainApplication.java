package result.ui;

import result.database.DatabaseInitializer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application bootstrap and entry point for the Student Result Management System.
 */
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Launching Student Result Management System...  ");
        System.out.println("==================================================");

        // Pre-initialize database tables and seeds in background
        new Thread(() -> {
            try {
                DatabaseInitializer.initializeDatabase();
            } catch (Exception ignored) {
            }
        }).start();

        // Schedule GUI startup on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply cross-platform Look and Feel in WebAssembly (CheerpJ) to prevent
                // NoSuchMethodError from missing native Linux GTK libraries.
                try {
                    boolean isBrowserEnv = System.getProperty("cheerpj.version") != null
                            || System.getProperty("os.name", "").toLowerCase().contains("linux");
                    if (isBrowserEnv) {
                        try {
                            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                        } catch (Throwable t) {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        }
                    } else {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Throwable t) {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        }
                    }
                } catch (Throwable ignored) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Throwable t2) {
                    }
                }

                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                try {
                    loginFrame.toFront();
                    loginFrame.requestFocus();
                } catch (Throwable ignored) {
                }

                System.out.println("[INFO] Login window launched successfully.");
            } catch (Throwable t) {
                System.err.println("[FATAL] Failed to launch application: " + t.getMessage());
                t.printStackTrace();

                java.io.StringWriter sw = new java.io.StringWriter();
                t.printStackTrace(new java.io.PrintWriter(sw));
                String fullTrace = sw.toString();

                JOptionPane.showMessageDialog(
                        null,
                        "Failed to launch application:\n" + fullTrace,
                        "Application Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
