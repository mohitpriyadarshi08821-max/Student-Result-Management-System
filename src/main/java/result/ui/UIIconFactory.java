package result.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * High-definition vector icons rendered mathematically via Java2D.
 * Completely crisp on all display scales, resolutions, and DPIs.
 */
public final class UIIconFactory {

    private UIIconFactory() {
    }

    public static Icon createGraduationCapIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);

                // Mortarboard diamond top
                int midX = x + w / 2;
                int topY = y + h / 5;
                int rightX = x + (int) (w * 0.95);
                int midY = y + (int) (h * 0.45);
                int leftX = x + (int) (w * 0.05);
                int bottomY = y + (int) (h * 0.70);

                Polygon capTop = new Polygon();
                capTop.addPoint(midX, topY);
                capTop.addPoint(rightX, midY);
                capTop.addPoint(midX, bottomY);
                capTop.addPoint(leftX, midY);
                g2.fill(capTop);

                // Cap skull base underneath
                int baseW = (int) (w * 0.48);
                int baseH = (int) (h * 0.35);
                int baseX = x + (w - baseW) / 2;
                int baseY = y + (int) (h * 0.50);
                g2.fillArc(baseX, baseY, baseW, baseH, 180, 180);

                // Tassel hanging down left
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int tasselStartX = midX;
                int tasselStartY = bottomY - 3;
                int tasselMidX = leftX + (int) (w * 0.12);
                int tasselEndY = y + (int) (h * 0.85);

                g2.drawLine(tasselStartX, tasselStartY, tasselMidX, tasselStartY + 4);
                g2.drawLine(tasselMidX, tasselStartY + 4, tasselMidX - 2, tasselEndY);
                g2.fillOval(tasselMidX - 4, tasselEndY - 1, 5, 5);
            }
        };
    }

    public static Icon createDashboardGridIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                int pad = Math.max(2, size / 8);
                int gap = Math.max(2, size / 8);
                int boxW = (w - 2 * pad - gap) / 2;
                int boxH = (h - 2 * pad - gap) / 2;
                int arc = Math.max(3, boxW / 3);

                g2.fill(new RoundRectangle2D.Float(x + pad, y + pad, boxW, boxH, arc, arc));
                g2.fill(new RoundRectangle2D.Float(x + pad + boxW + gap, y + pad, boxW, boxH, arc, arc));
                g2.fill(new RoundRectangle2D.Float(x + pad, y + pad + boxH + gap, boxW, boxH, arc, arc));
                g2.fill(new RoundRectangle2D.Float(x + pad + boxW + gap, y + pad + boxH + gap, boxW, boxH, arc, arc));
            }
        };
    }

    public static Icon createUserPlusIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);

                // User head
                int headR = (int) (w * 0.28);
                int headX = x + (int) (w * 0.18);
                int headY = y + (int) (h * 0.12);
                g2.fillOval(headX, headY, headR, headR);

                // User body (shoulder arc)
                int bodyW = (int) (w * 0.50);
                int bodyH = (int) (h * 0.42);
                int bodyX = x + (int) (w * 0.07);
                int bodyY = y + (int) (h * 0.52);
                g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);

                // Plus sign on the right
                int plusMidX = x + (int) (w * 0.78);
                int plusMidY = y + (int) (h * 0.45);
                int arm = (int) (w * 0.16);

                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(plusMidX - arm, plusMidY, plusMidX + arm, plusMidY);
                g2.drawLine(plusMidX, plusMidY - arm, plusMidX, plusMidY + arm);
            }
        };
    }

    public static Icon createUserGroupIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);

                // Left user (front)
                int r1 = (int) (w * 0.26);
                int x1 = x + (int) (w * 0.12);
                int y1 = y + (int) (h * 0.18);
                g2.fillOval(x1, y1, r1, r1);

                int bw1 = (int) (w * 0.44);
                int bh1 = (int) (h * 0.38);
                int bx1 = x + (int) (w * 0.03);
                int by1 = y + (int) (h * 0.54);
                g2.fillArc(bx1, by1, bw1, bh1, 0, 180);

                // Right user (rear/offset)
                int r2 = (int) (w * 0.23);
                int x2 = x + (int) (w * 0.58);
                int y2 = y + (int) (h * 0.14);
                g2.fillOval(x2, y2, r2, r2);

                int bw2 = (int) (w * 0.38);
                int bh2 = (int) (h * 0.35);
                int bx2 = x + (int) (w * 0.50);
                int by2 = y + (int) (h * 0.48);
                g2.fillArc(bx2, by2, bw2, bh2, 0, 180);
            }
        };
    }

    public static Icon createPencilIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.translate(x + w / 2.0, y + h / 2.0);
                g3.rotate(Math.toRadians(45));

                int pw = Math.max(3, size / 5);
                int ph = (int) (size * 0.65);

                // Pencil body
                g3.fillRoundRect(-pw / 2, -ph / 2, pw, (int) (ph * 0.72), 2, 2);

                // Pencil tip
                Polygon tip = new Polygon();
                tip.addPoint(-pw / 2, (int) (-ph / 2 + ph * 0.72));
                tip.addPoint(pw / 2, (int) (-ph / 2 + ph * 0.72));
                tip.addPoint(0, ph / 2);
                g3.fill(tip);

                g3.dispose();
            }
        };
    }

    public static Icon createTrashIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int padX = (int) (w * 0.20);
                int topY = y + (int) (h * 0.22);
                int lidW = w - 2 * padX;

                // Lid line
                g2.drawLine(x + padX - 2, topY, x + w - padX + 2, topY);

                // Handle on top of lid
                int hw = (int) (lidW * 0.40);
                int hx = x + (w - hw) / 2;
                g2.drawRoundRect(hx, topY - 3, hw, 3, 2, 2);

                // Bin body
                int bodyTopY = topY + 2;
                int bodyW = (int) (lidW * 0.85);
                int bodyH = (int) (h * 0.58);
                int bodyX = x + (w - bodyW) / 2;
                g2.drawRoundRect(bodyX, bodyTopY, bodyW, bodyH, 3, 3);

                // Vertical stripes inside bin
                int stripeY1 = bodyTopY + 3;
                int stripeY2 = bodyTopY + bodyH - 3;
                g2.drawLine(x + w / 2 - 2, stripeY1, x + w / 2 - 2, stripeY2);
                g2.drawLine(x + w / 2 + 2, stripeY1, x + w / 2 + 2, stripeY2);
            }
        };
    }

    public static Icon createExitIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Door frame: top, left, bottom
                int dx = x + (int) (w * 0.20);
                int dy = y + (int) (h * 0.18);
                int dw = (int) (w * 0.35);
                int dh = (int) (h * 0.64);

                g2.drawLine(dx + dw, dy, dx, dy);
                g2.drawLine(dx, dy, dx, dy + dh);
                g2.drawLine(dx, dy + dh, dx + dw, dy + dh);

                // Arrow pointing right
                int midY = y + h / 2;
                int arrowStartX = dx + (int) (dw * 0.50);
                int arrowEndX = x + (int) (w * 0.85);

                g2.drawLine(arrowStartX, midY, arrowEndX, midY);
                g2.drawLine(arrowEndX - 4, midY - 4, arrowEndX, midY);
                g2.drawLine(arrowEndX - 4, midY + 4, arrowEndX, midY);
            }
        };
    }

    public static Icon createSearchIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int r = (int) (size * 0.55);
                int cx = x + (int) (size * 0.12);
                int cy = y + (int) (size * 0.12);
                g2.drawOval(cx, cy, r, r);

                int hStartX = cx + (int) (r * 0.85);
                int hStartY = cy + (int) (r * 0.85);
                int hEndX = x + (int) (size * 0.88);
                int hEndY = y + (int) (size * 0.88);
                g2.drawLine(hStartX, hStartY, hEndX, hEndY);
            }
        };
    }

    public static Icon createInfoIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Outer circle
                int pad = Math.max(1, size / 10);
                int d = size - 2 * pad;
                g2.drawOval(x + pad, y + pad, d, d);

                // 'i' dot
                int dotX = x + w / 2;
                int dotY = y + (int) (h * 0.32);
                g2.fillOval(dotX - 1, dotY - 1, 3, 3);

                // 'i' stem
                int stemTop = y + (int) (h * 0.46);
                int stemBottom = y + (int) (h * 0.72);
                g2.drawLine(dotX, stemTop, dotX, stemBottom);
            }
        };
    }

    public static Icon createIdCardIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = Math.max(1, size / 9);
                int cardW = w - 2 * pad;
                int cardH = (int) (h * 0.76);
                int cardX = x + pad;
                int cardY = y + (h - cardH) / 2;
                int arc = Math.max(3, size / 5);
                g2.drawRoundRect(cardX, cardY, cardW, cardH, arc, arc);

                // Photo placeholder box on left
                int photoW = (int) (cardW * 0.30);
                int photoH = (int) (cardH * 0.54);
                int photoX = cardX + (int) (cardW * 0.12);
                int photoY = cardY + (cardH - photoH) / 2;
                g2.drawRoundRect(photoX, photoY, photoW, photoH, 2, 2);

                // Information lines on right
                int lineX = photoX + photoW + (int) (cardW * 0.10);
                int lineW = cardX + cardW - (int) (cardW * 0.12) - lineX;
                int lineY1 = photoY + 2;
                int lineY2 = photoY + photoH / 2;
                int lineY3 = photoY + photoH - 2;

                g2.drawLine(lineX, lineY1, lineX + lineW, lineY1);
                g2.drawLine(lineX, lineY2, lineX + (int) (lineW * 0.72), lineY2);
                g2.drawLine(lineX, lineY3, lineX + lineW, lineY3);
            }
        };
    }

    public static Icon createSingleUserIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Head
                int r = (int) (w * 0.36);
                int hx = x + (w - r) / 2;
                int hy = y + (int) (h * 0.14);
                g2.drawOval(hx, hy, r, r);

                // Body / Shoulders
                int bw = (int) (w * 0.70);
                int bh = (int) (h * 0.44);
                int bx = x + (w - bw) / 2;
                int by = y + (int) (h * 0.52);
                g2.drawArc(bx, by, bw, bh, 0, 180);
            }
        };
    }

    public static Icon createMailIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int padX = Math.max(1, (int) (w * 0.10));
                int ew = w - 2 * padX;
                int eh = (int) (h * 0.68);
                int ex = x + padX;
                int ey = y + (h - eh) / 2;
                int arc = Math.max(2, size / 6);

                // Envelope rect
                g2.drawRoundRect(ex, ey, ew, eh, arc, arc);

                // Flap lines
                int midX = ex + ew / 2;
                int flapBottomY = ey + (int) (eh * 0.55);
                g2.drawLine(ex + 1, ey + 1, midX, flapBottomY);
                g2.drawLine(ex + ew - 1, ey + 1, midX, flapBottomY);
            }
        };
    }

    public static Icon createCalendarIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = Math.max(2, (int) (w * 0.12));
                int cw = w - 2 * pad;
                int ch = (int) (h * 0.72);
                int cx = x + pad;
                int cy = y + (int) (h * 0.18);
                int arc = Math.max(3, size / 5);

                // Main body
                g2.drawRoundRect(cx, cy, cw, ch, arc, arc);

                // Header bar
                int headerY = cy + (int) (ch * 0.32);
                g2.drawLine(cx, headerY, cx + cw, headerY);

                // 2 Binder pins at top
                int pin1X = cx + (int) (cw * 0.28);
                int pin2X = cx + (int) (cw * 0.72);
                int pinTopY = cy - 2;
                int pinBotY = cy + 3;
                g2.drawLine(pin1X, pinTopY, pin1X, pinBotY);
                g2.drawLine(pin2X, pinTopY, pin2X, pinBotY);

                // Grid dots inside
                int dotY = headerY + (cy + ch - headerY) / 2;
                g2.fillOval(pin1X - 1, dotY - 1, 2, 2);
                g2.fillOval(cx + cw / 2 - 1, dotY - 1, 2, 2);
                g2.fillOval(pin2X - 1, dotY - 1, 2, 2);
            }
        };
    }

    public static Icon createLayersIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + w / 2;
                int leftX = x + (int) (w * 0.14);
                int rightX = x + (int) (w * 0.86);

                // Top diamond
                int y1 = y + (int) (h * 0.18);
                int y2 = y + (int) (h * 0.38);
                int y3 = y + (int) (h * 0.58);

                Polygon topLayer = new Polygon();
                topLayer.addPoint(midX, y1);
                topLayer.addPoint(rightX, y2);
                topLayer.addPoint(midX, y3);
                topLayer.addPoint(leftX, y2);
                g2.draw(topLayer);

                // Middle layer edge
                int offset1 = (int) (h * 0.15);
                g2.drawLine(leftX, y2 + offset1, midX, y3 + offset1);
                g2.drawLine(midX, y3 + offset1, rightX, y2 + offset1);

                // Bottom layer edge
                int offset2 = (int) (h * 0.30);
                g2.drawLine(leftX, y2 + offset2, midX, y3 + offset2);
                g2.drawLine(midX, y3 + offset2, rightX, y2 + offset2);
            }
        };
    }

    public static Icon createGenderIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int r = (int) (size * 0.44);
                int cx = x + (int) (w * 0.22);
                int cy = y + (int) (h * 0.28);
                g2.drawOval(cx, cy, r, r);

                // Male arrow pointing up-right
                int ax = cx + (int) (r * 0.82);
                int ay = cy + (int) (r * 0.18);
                int ex = x + (int) (w * 0.86);
                int ey = y + (int) (h * 0.14);
                g2.drawLine(ax, ay, ex, ey);
                g2.drawLine(ex - 4, ey, ex, ey);
                g2.drawLine(ex, ey + 4, ex, ey);

                // Female cross pointing down
                int bx = cx + r / 2;
                int by1 = cy + r;
                int by2 = y + (int) (h * 0.90);
                g2.drawLine(bx, by1, bx, by2);
                g2.drawLine(bx - 3, by1 + 5, bx + 3, by1 + 5);
            }
        };
    }

    public static Icon createCheckIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int x1 = x + (int) (w * 0.20);
                int y1 = y + (int) (h * 0.52);
                int x2 = x + (int) (w * 0.44);
                int y2 = y + (int) (h * 0.74);
                int x3 = x + (int) (w * 0.82);
                int y3 = y + (int) (h * 0.26);

                g2.drawLine(x1, y1, x2, y2);
                g2.drawLine(x2, y2, x3, y3);
            }
        };
    }

    public static Icon createBookIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int midX = x + w / 2;
                int topY = y + (int) (h * 0.22);
                int botY = y + (int) (h * 0.78);
                int spineMidY = topY + (int) ((botY - topY) * 0.88);

                // Center spine
                g2.drawLine(midX, topY + 2, midX, spineMidY);

                // Left page
                int leftX = x + (int) (w * 0.12);
                g2.drawLine(midX, topY + 2, leftX, topY);
                g2.drawLine(leftX, topY, leftX, botY - 2);
                g2.drawLine(leftX, botY - 2, midX, spineMidY);

                // Right page
                int rightX = x + (int) (w * 0.88);
                g2.drawLine(midX, topY + 2, rightX, topY);
                g2.drawLine(rightX, topY, rightX, botY - 2);
                g2.drawLine(rightX, botY - 2, midX, spineMidY);

                // Internal text line marks on pages
                int lineLeftX = leftX + (int) ((midX - leftX) * 0.25);
                int lineRightX = midX - (int) ((midX - leftX) * 0.25);
                int lineY1 = topY + (int) ((botY - topY) * 0.32);
                int lineY2 = topY + (int) ((botY - topY) * 0.55);
                g2.drawLine(lineLeftX, lineY1, lineRightX, lineY1 + 1);
                g2.drawLine(lineLeftX, lineY2, lineRightX, lineY2 + 1);

                int rLineLeftX = midX + (int) ((rightX - midX) * 0.25);
                int rLineRightX = rightX - (int) ((rightX - midX) * 0.25);
                g2.drawLine(rLineLeftX, lineY1 + 1, rLineRightX, lineY1);
                g2.drawLine(rLineLeftX, lineY2 + 1, rLineRightX, lineY2);
            }
        };
    }

    public static Icon createAwardIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Trophy cup bowl
                int cw = (int) (w * 0.52);
                int cx = x + (w - cw) / 2;
                int cy = y + (int) (h * 0.18);
                int ch = (int) (h * 0.40);
                g2.drawRoundRect(cx, cy, cw, ch, 6, 6);
                g2.fillArc(cx, cy + ch / 2, cw, ch, 180, 180);

                // Handles on left & right
                int hw = (int) (w * 0.16);
                g2.drawArc(cx - hw + 2, cy + 2, hw * 2, ch / 2, 90, 180);
                g2.drawArc(cx + cw - hw - 2, cy + 2, hw * 2, ch / 2, 270, 180);

                // Stem & base
                int stemX = x + w / 2;
                int stemY1 = cy + ch;
                int stemY2 = y + (int) (h * 0.78);
                g2.drawLine(stemX, stemY1, stemX, stemY2);
                int baseW = (int) (w * 0.44);
                g2.drawLine(stemX - baseW / 2, stemY2, stemX + baseW / 2, stemY2);
            }
        };
    }

    public static Icon createPrinterIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Top paper feed tray
                int pw = (int) (w * 0.50);
                int px = x + (w - pw) / 2;
                int py = y + (int) (h * 0.14);
                int ph = (int) (h * 0.28);
                g2.drawRect(px, py, pw, ph);

                // Printer main body
                int bw = (int) (w * 0.76);
                int bx = x + (w - bw) / 2;
                int by = y + (int) (h * 0.38);
                int bh = (int) (h * 0.34);
                g2.drawRoundRect(bx, by, bw, bh, 6, 6);

                // LED indicator dot
                g2.fillOval(bx + bw - 7, by + 5, 3, 3);

                // Bottom paper sheet output
                int ow = (int) (w * 0.46);
                int ox = x + (w - ow) / 2;
                int oy = by + (int) (bh * 0.50);
                int oh = (int) (h * 0.30);
                g2.drawRect(ox, oy, ow, oh);
                g2.drawLine(ox + 3, oy + 4, ox + ow - 3, oy + 4);
                g2.drawLine(ox + 3, oy + 8, ox + ow - 3, oy + 8);
            }
        };
    }

    public static Icon createDocumentIcon(int size, Color color) {
        return new BaseIcon(size, size) {
            @Override
            protected void paint(Graphics2D g2, int x, int y, int w, int h) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int dw = (int) (w * 0.58);
                int dx = x + (w - dw) / 2;
                int dy = y + (int) (h * 0.12);
                int dh = (int) (h * 0.76);

                // Document outline with folded corner
                int fold = (int) (dw * 0.32);
                g2.drawLine(dx, dy, dx + dw - fold, dy);
                g2.drawLine(dx + dw - fold, dy, dx + dw, dy + fold);
                g2.drawLine(dx + dw, dy + fold, dx + dw, dy + dh);
                g2.drawLine(dx + dw, dy + dh, dx, dy + dh);
                g2.drawLine(dx, dy + dh, dx, dy);

                // Fold triangle
                g2.drawLine(dx + dw - fold, dy, dx + dw - fold, dy + fold);
                g2.drawLine(dx + dw - fold, dy + fold, dx + dw, dy + fold);

                // Text lines inside document
                g2.drawLine(dx + 3, dy + (int) (dh * 0.40), dx + dw - 3, dy + (int) (dh * 0.40));
                g2.drawLine(dx + 3, dy + (int) (dh * 0.60), dx + dw - 3, dy + (int) (dh * 0.60));
                g2.drawLine(dx + 3, dy + (int) (dh * 0.80), dx + dw - 7, dy + (int) (dh * 0.80));
            }
        };
    }

    private abstract static class BaseIcon implements Icon {
        private final int width;
        private final int height;

        public BaseIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            paint(g2, x, y, width, height);
            g2.dispose();
        }

        protected abstract void paint(Graphics2D g2, int x, int y, int w, int h);

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
