package result.ui;

import javax.swing.border.Border;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom Swing Border that renders smooth, anti-aliased semi-rounded corners
 * with zero vertex clipping, solid high-contrast edges, and active focus styling.
 */
public class RoundedBorder implements Border {

    private final Color color;
    private final int radius;
    private final float thickness;

    public RoundedBorder(Color color, int radius) {
        this(color, radius, 1.2f);
    }

    public RoundedBorder(Color color, int radius, float thickness) {
        this.color = color != null ? color : new Color(175, 185, 200);
        this.radius = radius;
        this.thickness = Math.max(1.0f, thickness);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Inset by 1px so the stroke is completely inside the component bounds.
        // This guarantees upper vertices, top line, and corner arcs are NEVER clipped or faded.
        float pad = Math.max(1.0f, thickness / 2.0f);
        float drawX = x + pad;
        float drawY = y + pad;
        float drawW = width - 2.0f * pad;
        float drawH = height - 2.0f * pad;
        float arc = Math.min(radius, Math.min(drawW, drawH));

        RoundRectangle2D.Float roundShape = new RoundRectangle2D.Float(drawX, drawY, drawW, drawH, arc, arc);

        Color drawColor = (c != null && c.hasFocus() && c.isEnabled()) ? UIConstants.PRIMARY_COLOR : color;
        g2.setColor(drawColor);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(roundShape);

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int pad = (int) Math.ceil(thickness);
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

    public float getThickness() {
        return thickness;
    }
}
