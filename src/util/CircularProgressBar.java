package util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class CircularProgressBar extends JPanel {
    private double value = 0;
    private double maxValue = 100;
    private Color progressColor = ColorScheme.PRIMARY;
    private Color backgroundColor = new Color(60, 60, 70, 150);
    private String text = "";
    private String subText = "";
    private boolean showText = true;
    private int strokeWidth = 15;
    
    // Animation
    private Timer animationTimer;
    private double displayValue = 0;
    private double targetValue = 0;
    
    public CircularProgressBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(150, 150));
        
        // Animation timer
        animationTimer = new Timer(16, e -> {
            if (Math.abs(displayValue - targetValue) < 0.1) {
                displayValue = targetValue;
                ((Timer)e.getSource()).stop();
            } else {
                displayValue += (targetValue - displayValue) * 0.1;
            }
            repaint();
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Calculate center and radius
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = (size - strokeWidth * 2) / 2;
        
        // Draw background circle with glass effect
        g2.setColor(backgroundColor);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 360);
        
        // Draw progress arc with gradient
        double angle = (displayValue / maxValue) * 360;
        
        // Create gradient for progress arc
        Point2D start = new Point2D.Float(centerX, centerY - radius);
        Point2D end = new Point2D.Float(centerX, centerY + radius);
        
        Color startColor = progressColor;
        Color endColor = new Color(
            Math.min(255, progressColor.getRed() + 30),
            Math.min(255, progressColor.getGreen() + 30),
            Math.min(255, progressColor.getBlue() + 30)
        );
        
        LinearGradientPaint gradient = new LinearGradientPaint(
            start, end, new float[] {0.0f, 1.0f}, new Color[] {startColor, endColor}
        );
        
        g2.setPaint(gradient);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -(int)angle);
        
        // Add glow effect to progress arc
        g2.setColor(new Color(progressColor.getRed(), progressColor.getGreen(), progressColor.getBlue(), 50));
        g2.setStroke(new BasicStroke(strokeWidth + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -(int)angle);
        
        // Draw text if enabled
        if (showText) {
            // Draw main text (percentage)
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 5));
            g2.setColor(ColorScheme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String valueText = text.isEmpty() ? String.format("%.1f%%", (displayValue / maxValue) * 100) : text;
            int textWidth = fm.stringWidth(valueText);
            g2.drawString(valueText, centerX - textWidth / 2, centerY + fm.getAscent() / 2);
            
            // Draw sub text if provided
            if (!subText.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, size / 8));
                fm = g2.getFontMetrics();
                textWidth = fm.stringWidth(subText);
                g2.setColor(ColorScheme.TEXT_SECONDARY);
                g2.drawString(subText, centerX - textWidth / 2, centerY + fm.getHeight() + 5);
            }
        }
        
        g2.dispose();
    }
    
    public void setValue(double value) {
        this.value = Math.min(Math.max(0, value), maxValue);
        this.targetValue = this.value;
        animationTimer.restart();
        repaint();
    }
    
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        repaint();
    }
    
    public void setProgressColor(Color progressColor) {
        this.progressColor = progressColor;
        repaint();
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }
    
    public void setText(String text) {
        this.text = text;
        repaint();
    }
    
    public void setSubText(String subText) {
        this.subText = subText;
        repaint();
    }
    
    public void setShowText(boolean showText) {
        this.showText = showText;
        repaint();
    }
    
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        repaint();
    }
}

