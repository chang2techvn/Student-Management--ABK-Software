package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;

public class RoundedTextField extends JTextField {
    private Color backgroundColor = new Color(248, 249, 254);
    private Color borderColor = new Color(230, 230, 240);
    private Color focusBorderColor = ColorScheme.PRIMARY;
    private int radius = 8;
    private boolean isFocused = false;
    private String placeholder = "";
    
    // Animation
    private Timer animationTimer;
    private float glowAlpha = 0.0f;
    private float targetGlowAlpha = 0.0f;
    
    public RoundedTextField() {
        this(20);
    }
    
    public RoundedTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBorder(new EmptyBorder(12, 15, 12, 15));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setForeground(ColorScheme.TEXT);
        setCaretColor(ColorScheme.PRIMARY);
        setSelectionColor(new Color(ColorScheme.PRIMARY.getRed(), 
                                   ColorScheme.PRIMARY.getGreen(), 
                                   ColorScheme.PRIMARY.getBlue(), 100));
        
        // Animation timer
        animationTimer = new Timer(20, e -> {
            if (targetGlowAlpha > glowAlpha) {
                glowAlpha += 0.1f;
                if (glowAlpha > targetGlowAlpha) glowAlpha = targetGlowAlpha;
                repaint();
            } else if (targetGlowAlpha < glowAlpha) {
                glowAlpha -= 0.1f;
                if (glowAlpha < targetGlowAlpha) glowAlpha = targetGlowAlpha;
                repaint();
            } else {
                ((Timer)e.getSource()).stop();
            }
        });
        
        // Add focus listener to change border color
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent evt) {
                isFocused = true;
                targetGlowAlpha = 1.0f;
                animationTimer.start();
            }
            public void focusLost(FocusEvent evt) {
                isFocused = false;
                targetGlowAlpha = 0.0f;
                animationTimer.start();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw glow effect when focused
        if (glowAlpha > 0) {
            Color glowColor = new Color(
                focusBorderColor.getRed(),
                focusBorderColor.getGreen(),
                focusBorderColor.getBlue(),
                (int)(50 * glowAlpha)
            );
            g2.setColor(glowColor);
            g2.fillRoundRect(-2, -2, width + 4, height + 4, radius + 2, radius + 2);
        }
        
        // Paint background
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));
        
        // Paint border
        if (isFocused) {
            g2.setColor(focusBorderColor);
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.draw(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, radius, radius));
        
        // Draw placeholder if text is empty
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            g2.setColor(new Color(ColorScheme.TEXT_SECONDARY.getRed(), 
                               ColorScheme.TEXT_SECONDARY.getGreen(), 
                               ColorScheme.TEXT_SECONDARY.getBlue(), 150));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, getInsets().left, 
                       (height - fm.getHeight()) / 2 + fm.getAscent());
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }
    
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }
    
    public void setFocusBorderColor(Color focusBorderColor) {
        this.focusBorderColor = focusBorderColor;
        repaint();
    }
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }
}

