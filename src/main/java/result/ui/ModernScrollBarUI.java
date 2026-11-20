package result.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Modern, minimalist, floating pill scrollbar UI for Swing applications.
 * Replaces bulky retro 1990s scrollbars and arrow buttons with a sleek,
 * rounded capsule thumb and smooth hover transitions matching modern SaaS dashboards.
 */
public class ModernScrollBarUI extends BasicScrollBarUI {

    private static final int SCROLLBAR_THICKNESS = 8;
    private static final Color TRACK_COLOR = new Color(248, 250, 252, 180); // Subtle Slate-50 #F8FAFC
    private static final Color THUMB_IDLE = new Color(203, 213, 225);       // Slate-300 #CBD5E1
    private static final Color THUMB_HOVER = new Color(148, 163, 184);      // Slate-400 #94A3B8
    private static final Color THUMB_DRAG = new Color(100, 116, 139);       // Slate-500 #64748B

    public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
        return new ModernScrollBarUI();
    }

    public static void apply(JScrollPane scrollPane) {
        if (scrollPane == null) return;
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        if (vertical != null) {
            vertical.setUI(new ModernScrollBarUI());
            vertical.setPreferredSize(new Dimension(SCROLLBAR_THICKNESS, 0));
            vertical.setUnitIncrement(18);
            vertical.setBlockIncrement(72);
            vertical.setOpaque(false);
        }

        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
        if (horizontal != null) {
            horizontal.setUI(new ModernScrollBarUI());
            horizontal.setPreferredSize(new Dimension(0, SCROLLBAR_THICKNESS));
            horizontal.setUnitIncrement(18);
            horizontal.setBlockIncrement(72);
            horizontal.setOpaque(false);
        }
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusable(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setVisible(false);
        return button;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (scrollbar == null || scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(SCROLLBAR_THICKNESS, 48);
        } else {
            return new Dimension(48, SCROLLBAR_THICKNESS);
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TRACK_COLOR);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds == null || thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color thumbColor;
        if (isDragging) {
            thumbColor = THUMB_DRAG;
        } else if (isThumbRollover()) {
            thumbColor = THUMB_HOVER;
        } else {
            thumbColor = THUMB_IDLE;
        }

        g2.setColor(thumbColor);

        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            int pad = 1;
            int x = thumbBounds.x + pad;
            int y = thumbBounds.y + 2;
            int w = Math.max(4, thumbBounds.width - (pad * 2));
            int h = Math.max(16, thumbBounds.height - 4);
            int arc = Math.min(w, 8);
            g2.fillRoundRect(x, y, w, h, arc, arc);
        } else {
            int pad = 1;
            int x = thumbBounds.x + 2;
            int y = thumbBounds.y + pad;
            int w = Math.max(16, thumbBounds.width - 4);
            int h = Math.max(4, thumbBounds.height - (pad * 2));
            int arc = Math.min(h, 8);
            g2.fillRoundRect(x, y, w, h, arc, arc);
        }

        g2.dispose();
    }
}
