package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class GlassScrollPane extends JScrollPane {
    
    public GlassScrollPane(Component view) {
        super(view);
        customize();
    }
    
    public GlassScrollPane() {
        super();
        customize();
    }
    
    private void customize() {
        // Set transparent background
        setOpaque(false);
        getViewport().setOpaque(false);
        
        // Remove border
        setBorder(BorderFactory.createEmptyBorder());
        
        // Customize scrollbars
        JScrollBar verticalScrollBar = getVerticalScrollBar();
        JScrollBar horizontalScrollBar = getHorizontalScrollBar();
        
        // Set custom UI for scrollbars
        verticalScrollBar.setUI(new GlassScrollBarUI());
        horizontalScrollBar.setUI(new GlassScrollBarUI());
        
        // Make scrollbars blend with background
        verticalScrollBar.setOpaque(false);
        horizontalScrollBar.setOpaque(false);
        
        // Set scrollbar dimensions
        verticalScrollBar.setPreferredSize(new Dimension(8, 0));
        horizontalScrollBar.setPreferredSize(new Dimension(0, 8));
        
        // Only show scrollbars when needed
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
    
    // Custom ScrollBar UI for glass effect
    private static class GlassScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(ColorScheme.PRIMARY.getRed(), 
                                       ColorScheme.PRIMARY.getGreen(), 
                                       ColorScheme.PRIMARY.getBlue(), 100);
            this.trackColor = new Color(60, 60, 70, 80);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Paint thumb with rounded corners
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, 
                            thumbBounds.width, thumbBounds.height, 
                            thumbBounds.width, thumbBounds.width);
            
            // Add highlight
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(thumbBounds.x, thumbBounds.y, 
                            thumbBounds.width - 1, thumbBounds.height - 1, 
                            thumbBounds.width, thumbBounds.width);
            
            g2.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Paint track with rounded corners
            g2.setColor(trackColor);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, 
                            trackBounds.width, trackBounds.height, 
                            10, 10);
            
            g2.dispose();
        }
    }
}

