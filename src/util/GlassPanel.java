package util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GlassPanel extends JPanel {
    private int radius = 20;
    private Color backgroundColor = ColorScheme.CARD_BG;
    private Color borderColor = ColorScheme.GLASS_BORDER;
    private boolean drawBorder = true;
    private boolean drawShadow = true;
    private boolean drawHighlight = true;
    private float opacity = 0.8f;
    
    public GlassPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    public GlassPanel(LayoutManager layout) {
        setOpaque(false);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int width = getWidth();
        int height = getHeight();
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw shadow if enabled
        if (drawShadow) {
            g2d.setColor(ColorScheme.GLASS_SHADOW);
            g2d.fillRoundRect(5, 5, width - 10, height - 10, radius, radius);
        }
        
        // Create glass effect with gradient
        Paint oldPaint = g2d.getPaint();
        
        // Create a subtle gradient for the glass effect
        GradientPaint gradient = new GradientPaint(
            0, 0, 
            new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 
                      (int)(backgroundColor.getAlpha() * opacity * 0.95)),
            0, height, 
            new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 
                      (int)(backgroundColor.getAlpha() * opacity * 1.05))
        );
        
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width - 1, height - 1, radius, radius);
        
        // Restore original paint
        g2d.setPaint(oldPaint);
        
        // Draw border if enabled
        if (drawBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
        }
        
        // Draw highlight if enabled
        if (drawHighlight) {
            g2d.setColor(ColorScheme.GLASS_HIGHLIGHT);
            g2d.setStroke(new BasicStroke(1.0f));
            
            // Draw top highlight (curved)
            g2d.drawLine(radius, 5, width - radius, 5);
            
            // Draw left highlight (curved)
            g2d.drawLine(5, radius, 5, height - radius);
        }
        
        g2d.dispose();
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }
    
    public void setGlassColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }
    
    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }
    
    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        repaint();
    }
    
    public void setDrawHighlight(boolean drawHighlight) {
        this.drawHighlight = drawHighlight;
        repaint();
    }
    
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        repaint();
    }
}

