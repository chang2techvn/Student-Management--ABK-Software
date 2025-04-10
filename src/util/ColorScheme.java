package util;

import java.awt.Color;

public class ColorScheme {
    // Modern gradient theme
    public static final Color PRIMARY = new Color(94, 114, 228);       // Blue
    public static final Color PRIMARY_DARK = new Color(64, 84, 198);   // Darker Blue
    public static final Color SECONDARY = new Color(172, 94, 228);     // Purple
    
    // Background colors
    public static final Color BACKGROUND_START = new Color(240, 242, 255);  // Light blue-ish start
    public static final Color BACKGROUND_END = new Color(230, 230, 250);    // Light purple-ish end
    public static final Color CARD_BG = new Color(255, 255, 255, 240);      // White card
    public static final Color CARD_BG_ACCENT = new Color(248, 249, 254);    // Light accent card
    public static final Color BACKGROUND = new Color(245, 247, 250);        // Main background color
    
    // Text colors
    public static final Color TEXT = new Color(50, 50, 93);            // Dark blue text
    public static final Color TEXT_SECONDARY = new Color(120, 130, 150); // Secondary text
    public static final Color LIGHT_TEXT = new Color(255, 255, 255);   // White text for dark backgrounds
    
    // Status colors
    public static final Color SUCCESS = new Color(45, 206, 137);       // Green
    public static final Color WARNING = new Color(251, 189, 8);        // Yellow
    public static final Color DANGER = new Color(245, 54, 92);         // Red
    public static final Color INFO = new Color(17, 205, 239);          // Blue
    
    // Chart colors
    public static final Color[] CHART_COLORS = {
        new Color(94, 114, 228),   // Blue
        new Color(251, 99, 64),    // Orange
        new Color(45, 206, 137),   // Green
        new Color(251, 189, 8),    // Yellow
        new Color(172, 94, 228)    // Purple
    };
    
    // Rank colors
    public static final Color EXCELLENT_COLOR = new Color(172, 94, 228);  // Purple
    public static final Color VERY_GOOD_COLOR = new Color(45, 206, 137);  // Green
    public static final Color GOOD_COLOR = new Color(17, 205, 239);       // Blue
    public static final Color MEDIUM_COLOR = new Color(251, 189, 8);      // Yellow
    public static final Color FAIL_COLOR = new Color(245, 54, 92);        // Red
    
    // Shadow and highlight
    public static final Color SHADOW = new Color(0, 0, 0, 10);
    public static final Color HIGHLIGHT = new Color(255, 255, 255, 80);

    // Additional variables for GlassPanel
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 100);    // Transparent border
    public static final Color GLASS_SHADOW = new Color(0, 0, 0, 30);           // Light shadow
    public static final Color GLASS_HIGHLIGHT = new Color(255, 255, 255, 100); // Light highlight
}

