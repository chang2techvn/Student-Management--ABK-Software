package util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class BarChart extends JPanel {
    private List<Double> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private double maxValue = 100;
    private Color barColor = ColorScheme.PRIMARY;
    private boolean showValues = true;
    private boolean showLabels = true;
    private int barSpacing = 10;
    private int barRadius = 6;
    
    // Animation
    private Timer animationTimer;
    private List<Double> displayValues = new ArrayList<>();
    
    public BarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 200));
        
        // Animation timer
        animationTimer = new Timer(16, e -> {
            boolean animationComplete = true;
            
            for (int i = 0; i < values.size(); i++) {
                if (i >= displayValues.size()) {
                    displayValues.add(0.0);
                }
                
                double target = values.get(i);
                double current = displayValues.get(i);
                
                if (Math.abs(current - target) < 0.1) {
                    displayValues.set(i, target);
                } else {
                    displayValues.set(i, current + (target - current) * 0.1);
                    animationComplete = false;
                }
            }
            
            if (animationComplete) {
                ((Timer)e.getSource()).stop();
            }
            
            repaint();
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (values.isEmpty()) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        int chartHeight = height - (showLabels ? 30 : 10);
        int barWidth = (width - (values.size() + 1) * barSpacing) / values.size();
        
        // Draw bars
        for (int i = 0; i < values.size(); i++) {
            double displayValue = i < displayValues.size() ? displayValues.get(i) : 0;
            int barHeight = (int) (chartHeight * (displayValue / maxValue));
            int x = barSpacing + i * (barWidth + barSpacing);
            int y = chartHeight - barHeight;
            
            // Create gradient for bar
            GradientPaint gradient = new GradientPaint(
                x, y, barColor,
                x, y + barHeight, 
                new Color(
                    Math.min(255, barColor.getRed() + 30),
                    Math.min(255, barColor.getGreen() + 30),
                    Math.min(255, barColor.getBlue() + 30)
                )
            );
            
            // Draw bar
            g2.setPaint(gradient);
            g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barHeight, barRadius, barRadius));
            
            // Add glass effect
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawLine(x + 2, y + 2, x + barWidth - 2, y + 2);
            
            // Draw value
            if (showValues) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(ColorScheme.TEXT);
                String valueText = String.format("%.1f", values.get(i));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(valueText);
                g2.drawString(valueText, x + (barWidth - textWidth) / 2, y - 5);
            }
            
            // Draw label
            if (showLabels && i < labels.size()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(ColorScheme.TEXT_SECONDARY);
                String label = labels.get(i);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                g2.drawString(label, x + (barWidth - textWidth) / 2, height - 5);
            }
        }
        
        g2.dispose();
    }
    
    public void setData(List<Double> values, List<String> labels) {
        this.values = new ArrayList<>(values);
        this.labels = new ArrayList<>(labels);
        
        // Find max value for scaling
        this.maxValue = 0;
        for (Double value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        // Add 10% padding to max value
        this.maxValue *= 1.1;
        
        // Reset display values to trigger animation
        this.displayValues.clear();
        
        // Start animation
        animationTimer.restart();
        
        repaint();
    }
    
    public void setBarColor(Color barColor) {
        this.barColor = barColor;
        repaint();
    }
    
    public void setShowValues(boolean showValues) {
        this.showValues = showValues;
        repaint();
    }
    
    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }
    
    public void setBarSpacing(int barSpacing) {
        this.barSpacing = barSpacing;
        repaint();
    }
    
    public void setBarRadius(int barRadius) {
        this.barRadius = barRadius;
        repaint();
    }
}

