package view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import model.StudentManager;
import util.ColorScheme;

public class MainFrame extends JFrame {
    private StudentManager studentManager;
    private DashboardPanel dashboardPanel;
    private StudentFormPanel studentFormPanel;
    private PerformanceTestPanel performanceTestPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel headerPanel;
    private JButton dashboardButton;
    private JButton studentsButton;
    private JButton performanceButton;
    
    public MainFrame() {
        setTitle("ABK Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setPreferredSize(new Dimension(1400, 900));
        
        // Initialize student manager
        studentManager = new StudentManager();
        
        initComponents();
        
        // Add sample data after initializing interface
        studentManager.addSampleData();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set content pane with background
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, ColorScheme.BACKGROUND_START,
                    getWidth(), getHeight(), ColorScheme.BACKGROUND_END
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new BorderLayout(15, 15));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create main container panel with rounded corners
        JPanel mainContainer = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                g2d.dispose();
            }
        };
        mainContainer.setOpaque(false);
        backgroundPanel.add(mainContainer, BorderLayout.CENTER);
        
        // Create sidebar panel
        sidebarPanel = createSidebarPanel();
        mainContainer.add(sidebarPanel, BorderLayout.WEST);
        
        // Create content panel with card layout
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        
        // Create header panel
        headerPanel = createHeaderPanel();
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        
        // Create dashboard panel
        dashboardPanel = new DashboardPanel(studentManager);
        contentPanel.add(dashboardPanel, "dashboard");
        
        // Create student form panel
        studentFormPanel = new StudentFormPanel(studentManager, dashboardPanel);
        contentPanel.add(studentFormPanel, "studentForm");
        
        // Create performance test panel
        performanceTestPanel = new PerformanceTestPanel();
        contentPanel.add(performanceTestPanel, "performanceTest");
        
        rightPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Show dashboard by default
        cardLayout.show(contentPanel, "dashboard");
        updateActiveButton("dashboard");
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(getWidth(), 60));
        
        // Add welcome message
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        welcomePanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome to");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        
        JLabel titleLabel = new JLabel("Student Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ColorScheme.TEXT);
        
        JPanel labelContainer = new JPanel(new GridLayout(2, 1));
        labelContainer.setOpaque(false);
        labelContainer.add(welcomeLabel);
        labelContainer.add(titleLabel);
        
        welcomePanel.add(labelContainer);
        panel.add(welcomePanel, BorderLayout.WEST);
        
        // Add search and user profile
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // User profile
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        

                
        rightPanel.add(userPanel);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, getHeight()));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        
        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);
        

        
        JLabel logoText = new JLabel("ABK Software");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoText.setForeground(ColorScheme.PRIMARY);
        
        logoPanel.add(logoText);
        
        panel.add(logoPanel, BorderLayout.NORTH);
        
        // Menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        // Dashboard button
        dashboardButton = createSidebarButton("Dashboard", "dashboard");
        
        // Students button
        studentsButton = createSidebarButton("Manage Students", "studentForm");
        
        // Performance Test button
        performanceButton = createSidebarButton("Performance Testing", "performanceTest");
        
        menuPanel.add(dashboardButton);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(studentsButton);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(performanceButton);
        menuPanel.add(Box.createVerticalGlue());
        
        panel.add(menuPanel, BorderLayout.CENTER);
        
        // Footer with version
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(versionLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Modify createSidebarButton to ensure text doesn't wrap
    private JButton createSidebarButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(ColorScheme.TEXT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        
        // Add icon and padding
        button.setIcon(new ImageIcon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(ColorScheme.PRIMARY);
                g2d.fillOval(x, y + 4, 8, 8);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 8;
            }
            
            @Override
            public int getIconHeight() {
                return 8;
            }
        });
        
        button.setIconTextGap(10);
        button.setMargin(new Insets(0, 5, 0, 0));
        
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            updateActiveButton(cardName);
        });
        
        return button;
    }
    
    // Update updateActiveButton to match new display style
    private void updateActiveButton(String cardName) {
        // Reset all buttons
        dashboardButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dashboardButton.setForeground(ColorScheme.TEXT);
        
        studentsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentsButton.setForeground(ColorScheme.TEXT);
        
        performanceButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        performanceButton.setForeground(ColorScheme.TEXT);
        
        // Set active button
        if (cardName.equals("dashboard")) {
            dashboardButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dashboardButton.setForeground(ColorScheme.PRIMARY);
        } else if (cardName.equals("studentForm")) {
            studentsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            studentsButton.setForeground(ColorScheme.PRIMARY);
        } else if (cardName.equals("performanceTest")) {
            performanceButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            performanceButton.setForeground(ColorScheme.PRIMARY);
        }
    }
}

