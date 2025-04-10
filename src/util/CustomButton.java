package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private boolean isRounded = true;
    private int radius = 8;
    private boolean isGlass = false;
    
    private Timer animationTimer;
    private float alpha = 0.0f;
    private boolean mouseOver = false;
    private boolean mousePressed = false;
    
    public CustomButton(String text, Color backgroundColor, Color hoverColor, Color textColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.hoverColor = hoverColor;
        this.pressedColor = new Color(
            Math.max(0, hoverColor.getRed() - 20),
            Math.max(0, hoverColor.getGreen() - 20),
            Math.max(0, hoverColor.getBlue() - 20)
        );
        this.textColor = textColor;
        
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Animation timer
        animationTimer = new Timer(20, e -> {
            if (mouseOver && alpha < 1.0f) {
                alpha += 0.1f;
                if (alpha > 1.0f) alpha = 1.0f;
                repaint();
            } else if (!mouseOver && alpha > 0.0f) {
                alpha -= 0.1f;
                if (alpha < 0.0f) alpha = 0.0f;
                repaint();
            } else {
                ((Timer)e.getSource()).stop();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                animationTimer.start();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                animationTimer.start();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed = false;
                repaint();
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
        
        // Calculate colors based on state
        Color currentBg;
        if (mousePressed) {
            currentBg = pressedColor;
        } else if (mouseOver) {
            // Interpolate between background and hover color
            int r = interpolateColor(backgroundColor.getRed(), hoverColor.getRed(), alpha);
            int gr = interpolateColor(backgroundColor.getGreen(), hoverColor.getGreen(), alpha);
            int b = interpolateColor(backgroundColor.getBlue(), hoverColor.getBlue(), alpha);
            currentBg = new Color(r, gr, b);
        } else {
            currentBg = backgroundColor;
        }
        
        // Draw button background
        if (isGlass) {
            // Glass effect with gradient
            GradientPaint gradient = new GradientPaint(
                0, 0, 
                new Color(currentBg.getRed(), currentBg.getGreen(), currentBg.getBlue(), 180),
                0, height, 
                new Color(currentBg.getRed(), currentBg.getGreen(), currentBg.getBlue(), 220)
            );
            g2.setPaint(gradient);
        } else {
            g2.setColor(currentBg);
        }
        
        if (isRounded) {
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));
            
            // Add subtle shadow
            if (!isGlass) {
                g2.setColor(new Color(0, 0, 0, 10));
                g2.drawRoundRect(0, 1, width - 1, height - 1, radius, radius);
            }
        } else {
            g2.fillRect(0, 0, width, height);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    private int interpolateColor(int color1, int color2, float fraction) {
        return (int)(color1 + (color2 - color1) * fraction);
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }
    
    public void setGlassEffect(boolean isGlass) {
        this.isGlass = isGlass;
        repaint();
    }
    
    public static CustomButton createPrimaryButton(String text) {
        CustomButton button = new CustomButton(text, ColorScheme.PRIMARY, ColorScheme.PRIMARY_DARK, Color.WHITE);
        button.setRadius(8);
        return button;
    }
    
    public static CustomButton createSecondaryButton(String text) {
        CustomButton button = new CustomButton(text, ColorScheme.SECONDARY, 
                                           new Color(152, 74, 208), Color.WHITE);
        button.setRadius(8);
        return button;
    }
    
    public static CustomButton createSuccessButton(String text) {
        CustomButton button = new CustomButton(text, ColorScheme.SUCCESS, 
                                           new Color(25, 186, 117), Color.WHITE);
        button.setRadius(8);
        return button;
    }
    
    public static CustomButton createDangerButton(String text) {
        CustomButton button = new CustomButton(text, ColorScheme.DANGER, 
                                           new Color(225, 34, 72), Color.WHITE);
        button.setRadius(8);
        return button;
    }
    
    public static CustomButton createGlassButton(String text) {
        CustomButton button = new CustomButton(text, new Color(255, 255, 255, 40), 
                                           new Color(255, 255, 255, 80), ColorScheme.TEXT);
        button.setGlassEffect(true);
        button.setRadius(8);
        return button;
    }
}

