package tech.lin2j.idea.plugin.ui.component;

import com.intellij.ui.components.JBLabel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * @author linjinjia
 * @date 2024/5/19 11:30
 */
public class ArrowLabel extends JBLabel {
    private final boolean curved;
    private final boolean leftToRight;

    public ArrowLabel(boolean curved, boolean leftToRight) {
        this.curved = curved;
        this.leftToRight = leftToRight;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int midY = getHeight() / 2;
        int startX = leftToRight ? 0 : getWidth();
        int endX = leftToRight ? getWidth() : 0;

        if (curved) {
            startX = endX = getWidth() / 2;
            int startY = 0;
            int endY = getHeight();
            g2d.drawLine(startX, startY, endX, endY);
            g2d.drawLine(endX, endY, endX - 5, endY - 5);
            g2d.drawLine(endX, endY, endX + 5, endY - 5);
        } else {
            g2d.drawLine(startX, midY, endX, midY);
            g2d.drawLine(endX, midY, endX - (leftToRight ? 5 : -5), midY - 5);
            g2d.drawLine(endX, midY, endX - (leftToRight ? 5 : -5), midY + 5);
        }
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(30, 30);
    }
}