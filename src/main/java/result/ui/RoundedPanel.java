package result.ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * A JPanel container with anti-aliased semi-rounded corners and an optional border.
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private int borderThickness;

    public RoundedPanel() {
        this(UIConstants.CORNER_RADIUS, UIConstants.CARD_BACKGROUND);
    }

    public RoundedPanel(int cornerRadius) {
        this(cornerRadius, UIConstants.CARD_BACKGROUND);
    }

    public RoundedPanel(int cornerRadius, Color backgroundColor) {
        this(cornerRadius, backgroundColor, new Color(226, 232, 240), 1);
    }

    public RoundedPanel(int cornerRadius, Color backgroundColor, Color borderColor, int borderThickness) {
        super();
        this.cornerRadius = cornerRadius;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int cornerRadius, Color backgroundColor) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.backgroundColor = backgroundColor;
        this.borderColor = new Color(226, 232, 240);
        this.borderThickness = 1;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int cornerRadius, Color backgroundColor, Color borderColor, int borderThickness) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }

        float pad = borderThickness > 0 ? (borderThickness / 2.0f) : 0f;
        float w = width - 2.0f * pad;
        float h = height - 2.0f * pad;
        float arc = Math.min(cornerRadius, Math.min(w, h));
        java.awt.geom.RoundRectangle2D.Float roundShape = new java.awt.geom.RoundRectangle2D.Float(pad, pad, w, h, arc, arc);

        // Fill semi-rounded background
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fill(roundShape);
        }

        // Draw semi-rounded border
        if (borderColor != null && borderThickness > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(roundShape);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        repaint();
    }
}
