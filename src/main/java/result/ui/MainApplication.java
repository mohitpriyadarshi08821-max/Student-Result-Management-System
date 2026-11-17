package result.ui;

import result.database.DatabaseInitializer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application bootstrap and entry point for the Student Result Management System.
 * Supports both high-performance native desktop execution (Windows, macOS, Linux)
 * and WebAssembly deployment via CheerpJ.
 */
public class MainApplication {

    private static volatile boolean isBrowserMode = false;

    /**
     * Determines whether the application is running inside a browser sandbox (CheerpJ WebAssembly).
     */
    public static boolean isBrowserEnvironment() {
        return isBrowserMode
                || System.getProperty("cheerpj.home") != null
                || System.getProperty("cheerpj.version") != null
                || System.getProperty("browser.mode") != null
                || System.getProperty("java.vm.name", "").toLowerCase().contains("cheerp")
                || System.getProperty("java.vendor", "").toLowerCase().contains("leaning")
                || "true".equalsIgnoreCase(System.getProperty("browser.mode"));
    }

    public static void main(String[] args) {
        // Inspect command line arguments for browser execution flags
        if (args != null) {
            for (String arg : args) {
                if ("--browser".equalsIgnoreCase(arg) || "-browser".equalsIgnoreCase(arg) || "browser".equalsIgnoreCase(arg)) {
                    isBrowserMode = true;
                }
            }
        }

        System.out.println("==================================================");
        System.out.println("  Launching Student Result Management System...  ");
        System.out.println("==================================================");
        System.out.println("[SRMS-DIAGNOSTIC] STAGE:MAIN_INVOKED");
        System.out.println("[INFO] Runtime Environment: " + (isBrowserEnvironment() ? "CheerpJ WebAssembly (Browser)" : "Native Desktop JVM"));

        // Only pre-initialize MySQL server schema on desktop mode where raw TCP sockets are allowed.
        // In browser WebAssembly, browser sandboxes block raw TCP connections to localhost:3306.
        if (!isBrowserEnvironment()) {
            new Thread(() -> {
                try {
                    DatabaseInitializer.initializeDatabase();
                } catch (Throwable ignored) {
                }
            }, "SRMS-DatabaseInitializer").start();
        } else {
            System.out.println("[INFO] Browser environment detected: Skipping background MySQL socket initialization.");
        }

        System.out.println("[SRMS-DIAGNOSTIC] STAGE:INITIALIZING_SWING_UI");

        // Schedule GUI startup on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply Look and Feel safely:
                // In WebAssembly (CheerpJ), ALWAYS use pure Java CrossPlatform (Metal) Look and Feel.
                // This prevents NoSuchMethodError from missing native Linux GTK libraries.
                if (isBrowserEnvironment() || System.getProperty("os.name", "").toLowerCase().contains("linux")) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Throwable ignored) {
                    }
                } else {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Throwable t) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        } catch (Throwable ignored) {
                        }
                    }
                }

                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

                try {
                    loginFrame.toFront();
                    loginFrame.requestFocus();
                } catch (Throwable ignored) {
                }

                System.out.println("[SRMS-DIAGNOSTIC] STAGE:SWING_UI_READY");
                System.out.println("[INFO] Login window launched successfully.");
            } catch (Throwable t) {
                System.err.println("[FATAL] Failed to launch application: " + t.getMessage());
                t.printStackTrace();

                java.io.StringWriter sw = new java.io.StringWriter();
                t.printStackTrace(new java.io.PrintWriter(sw));
                String fullTrace = sw.toString();

                System.err.println("[SRMS-DIAGNOSTIC] STAGE:STARTUP_ERROR: " + fullTrace);

                try {
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to launch application:\n" + fullTrace,
                            "Application Startup Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (Throwable ignored) {
                }
            }
        });
    }
}
