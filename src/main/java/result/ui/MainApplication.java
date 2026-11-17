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
                // Apply the native operating system look and feel for a modern desktop look
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Fall back to standard CrossPlatform look and feel
                }

                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.toFront();
                loginFrame.requestFocus();

                System.out.println("[INFO] Login window launched successfully.");
            } catch (Throwable t) {
                System.err.println("[FATAL] Failed to launch application: " + t.getMessage());
                t.printStackTrace();

                JOptionPane.showMessageDialog(
                        null,
                        "Failed to launch application:\n" + t.getMessage(),
                        "Application Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
