package result.ui;

import javax.swing.border.Border;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Custom Swing Border that renders smooth, anti-aliased semi-rounded corners.
 */
public class RoundedBorder implements Border {

    private final Color color;
    private final int radius;
    private final int thickness;

    public RoundedBorder(Color color, int radius) {
        this(color, radius, 1);
    }

    public RoundedBorder(Color color, int radius, int thickness) {
        this.color = color != null ? color : new Color(203, 213, 225);
        this.radius = radius;
        this.thickness = Math.max(1, thickness);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                width - thickness, height - thickness,
                radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int pad = Math.max(thickness, radius / 3);
        return new Insets(pad, pad, pad, pad);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    public Color getColor() {
        return color;
    }

    public int getRadius() {
        return radius;
    }

    public int getThickness() {
        return thickness;
    }
}
