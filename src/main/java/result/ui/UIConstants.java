package result.ui;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;

/**
 * Common constants for UI fonts, brand colors, and semi-rounded layout dimensions
 * to ensure a clean, cohesive Java Swing presentation.
 */
public final class UIConstants {

    private UIConstants() {
        // Prevent instantiation
    }

    public static final String APP_TITLE = "Student Result Management System";

    // Theme Colors
    public static final Color PRIMARY_COLOR = new Color(37, 99, 235);        // Royal Blue
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color ACCENT_COLOR = new Color(22, 163, 74);         // Crisp Green
    public static final Color BACKGROUND_COLOR = new Color(241, 245, 249);   // Modern soft slate bg
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);          // Deep dark slate
    public static final Color DANGER_COLOR = new Color(225, 29, 72);         // Crimson Red

    // Button Dark Text Colors (Never light or white)
    public static final Color BTN_TEXT_DARK = new Color(15, 23, 42);         // Primary dark text
    public static final Color BTN_TEXT_PRIMARY = new Color(10, 48, 110);     // Dark navy text
    public static final Color BTN_TEXT_SUCCESS = new Color(16, 78, 40);      // Dark forest green text
    public static final Color BTN_TEXT_DANGER = new Color(153, 27, 27);      // Dark red text
    public static final Color BTN_TEXT_NEUTRAL = new Color(30, 41, 59);      // Dark charcoal text

    // Button Backgrounds & Borders (Designed for high contrast with dark font)
    public static final Color BTN_BG_PRIMARY = new Color(219, 234, 254);     // Soft sky blue
    public static final Color BTN_BORDER_PRIMARY = new Color(147, 197, 253);

    public static final Color BTN_BG_SUCCESS = new Color(220, 252, 231);     // Soft mint green
    public static final Color BTN_BORDER_SUCCESS = new Color(134, 239, 172);

    public static final Color BTN_BG_DANGER = new Color(254, 226, 226);      // Soft rose red
    public static final Color BTN_BORDER_DANGER = new Color(252, 165, 165);

    public static final Color BTN_BG_NEUTRAL = new Color(241, 245, 249);     // Soft neutral gray
    public static final Color BTN_BORDER_NEUTRAL = new Color(203, 213, 225);

    // Standard Semi-Rounded Corner Radii (Modern Commercial SaaS Standard)
    public static final int CORNER_RADIUS = 10;
    public static final int CORNER_RADIUS_SM = 8;
    public static final int CORNER_RADIUS_LG = 14;

    // Standard Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    /**
     * Creates a semi-rounded border with internal padding.
     */
    public static Border createRoundedBorder(Color color, int radius, int top, int left, int bottom, int right) {
        return new CompoundBorder(
                new RoundedBorder(color, radius, 1.2f),
                new EmptyBorder(top, left, bottom, right)
        );
    }

    public static Border createInputBorder() {
        return createRoundedBorder(new Color(175, 185, 200), CORNER_RADIUS_SM, 6, 10, 6, 10);
    }

    public static Border createCardBorder() {
        return createRoundedBorder(new Color(226, 232, 240), CORNER_RADIUS, 14, 16, 14, 16);
    }
}
