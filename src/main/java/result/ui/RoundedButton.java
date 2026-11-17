package result.ui;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Commercial-grade Swing JButton that renders smooth semi-rounded corners,
 * high-contrast dark button typography, subtle hover/pressed animations,
 * and zero vertex clipping across all operating systems.
 */
@SuppressWarnings({"serial", "this-escape"})
public class RoundedButton extends JButton {

    private static final long serialVersionUID = 1L;

    private int cornerRadius;
    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private Color disabledBg;

    private Color darkTextColor;
    private Color disabledTextColor;

    private Color borderColor;
    private Color hoverBorderColor;
    private float borderThickness = 1.0f;

    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text) {
        this(text,
                UIConstants.BTN_BG_PRIMARY,
                new Color(191, 219, 254),
                new Color(147, 197, 253),
                UIConstants.BTN_TEXT_PRIMARY,
                UIConstants.BTN_BORDER_PRIMARY,
                UIConstants.CORNER_RADIUS_SM);
    }

    public RoundedButton(String text, Color normalBg, Color hoverBg, Color pressedBg,
                         Color darkTextColor, Color borderColor, int cornerRadius) {
        super(text);
        this.normalBg = normalBg;
        this.hoverBg = hoverBg;
        this.pressedBg = pressedBg;
        this.disabledBg = new Color(241, 245, 249);

        this.darkTextColor = darkTextColor != null ? darkTextColor : UIConstants.BTN_TEXT_DARK;
        this.disabledTextColor = new Color(148, 163, 184);

        this.borderColor = borderColor != null ? borderColor : new Color(203, 213, 225);
        this.hoverBorderColor = borderColor != null ? borderColor.darker() : new Color(148, 163, 184);
        this.cornerRadius = cornerRadius > 0 ? cornerRadius : UIConstants.CORNER_RADIUS_SM;

        setFont(UIConstants.FONT_BOLD);
        setForeground(this.darkTextColor);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(6, 14, 6, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public static RoundedButton createPrimary(String text) {
        return new RoundedButton(text,
                UIConstants.BTN_BG_PRIMARY,
                new Color(191, 219, 254),
                new Color(147, 197, 253),
                UIConstants.BTN_TEXT_PRIMARY,
                UIConstants.BTN_BORDER_PRIMARY,
                UIConstants.CORNER_RADIUS_SM);
    }

    public static RoundedButton createSuccess(String text) {
        return new RoundedButton(text,
                UIConstants.BTN_BG_SUCCESS,
                new Color(187, 247, 208),
                new Color(134, 239, 172),
                UIConstants.BTN_TEXT_SUCCESS,
                UIConstants.BTN_BORDER_SUCCESS,
                UIConstants.CORNER_RADIUS_SM);
    }

    public static RoundedButton createDanger(String text) {
        return new RoundedButton(text,
                UIConstants.BTN_BG_DANGER,
                new Color(254, 202, 202),
                new Color(252, 165, 165),
                UIConstants.BTN_TEXT_DANGER,
                UIConstants.BTN_BORDER_DANGER,
                UIConstants.CORNER_RADIUS_SM);
    }

    public static RoundedButton createNeutral(String text) {
        return new RoundedButton(text,
                UIConstants.BTN_BG_NEUTRAL,
                new Color(226, 232, 240),
                new Color(203, 213, 225),
                UIConstants.BTN_TEXT_NEUTRAL,
                UIConstants.BTN_BORDER_NEUTRAL,
                UIConstants.CORNER_RADIUS_SM);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setForeground(enabled ? darkTextColor : disabledTextColor);
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Suppress default Look and Feel borders to prevent square vertex clipping
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }

        // 1. Determine background color based on state
        Color currentBg;
        if (!isEnabled()) {
            currentBg = disabledBg;
        } else if (isPressed) {
            currentBg = pressedBg;
        } else if (isHovered) {
            currentBg = hoverBg;
        } else {
            currentBg = normalBg;
        }

        // 2. Determine border color based on state
        Color currentBorder;
        if (!isEnabled()) {
            currentBorder = new Color(226, 232, 240);
        } else if (isHovered || isPressed) {
            currentBorder = hoverBorderColor;
        } else {
            currentBorder = borderColor;
        }

        // 3. Inset by 1px so anti-aliased curved borders never clip at the component bounding box vertices
        float pad = 1.0f;
        float w = width - 2.0f * pad;
        float h = height - 2.0f * pad;
        float arc = Math.min(cornerRadius, Math.min(w, h));
        RoundRectangle2D.Float roundShape = new RoundRectangle2D.Float(pad, pad, w, h, arc, arc);

        // 4. Fill smooth semi-rounded background
        g2.setColor(currentBg);
        g2.fill(roundShape);

        // 5. Draw smooth semi-rounded border
        if (currentBorder != null && borderThickness > 0) {
            g2.setColor(currentBorder);
            g2.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(roundShape);
        }

        // 6. Draw Icon and Dark Text without any Look and Feel clipping
        String text = getText();
        Icon icon = getIcon();
        Font font = getFont();
        g2.setFont(font);
        g2.setColor(isEnabled() ? darkTextColor : disabledTextColor);

        Insets insets = getInsets();
        int left = insets != null ? insets.left : 4;
        int right = insets != null ? insets.right : 4;
        int top = insets != null ? insets.top : 4;
        int bottom = insets != null ? insets.bottom : 4;

        Rectangle viewRect = new Rectangle(left, top, Math.max(0, width - (left + right)), Math.max(0, height - (top + bottom)));
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        FontMetrics fm = g2.getFontMetrics(font);
        String clippedText = SwingUtilities.layoutCompoundLabel(
                this, fm, text, icon,
                getVerticalAlignment(), getHorizontalAlignment(),
                getVerticalTextPosition(), getHorizontalTextPosition(),
                viewRect, iconRect, textRect,
                text == null ? 0 : getIconTextGap()
        );

        int pressOffset = (isPressed && isEnabled()) ? 1 : 0;

        if (icon != null) {
            icon.paintIcon(this, g2, iconRect.x, iconRect.y + pressOffset);
        }

        if (clippedText != null && !clippedText.isEmpty()) {
            g2.drawString(clippedText, textRect.x, textRect.y + fm.getAscent() + pressOffset);
        }

        g2.dispose();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public Color getDarkTextColor() {
        return darkTextColor;
    }

    public void setDarkTextColor(Color darkTextColor) {
        this.darkTextColor = darkTextColor;
        if (isEnabled()) {
            setForeground(darkTextColor);
        }
        repaint();
    }

    public Color getNormalBg() {
        return normalBg;
    }

    public void setNormalBg(Color normalBg) {
        this.normalBg = normalBg;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }
}
