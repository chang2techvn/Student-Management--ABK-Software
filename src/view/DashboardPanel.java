package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Student;
import model.StudentManager;
import util.*;

public class DashboardPanel extends JPanel {
    private final StudentManager studentManager;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private CircularProgressBar averageScoreChart;
    private CircularProgressBar passRateChart;
    private BarChart rankDistributionChart;
    private Timer autoUpdateTimer;
    private JPanel summaryPanel;
    private Map<String, Integer> lastRankDistribution = new HashMap<>();

    public DashboardPanel(StudentManager studentManager) {
        this.studentManager = studentManager;
        setLayout(new BorderLayout());
        setOpaque(false);

        // Initialize components
        String[] columns = {"ID", "Name", "Score", "Rank"};
        this.tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        this.studentTable = new JTable(this.tableModel);
        this.averageScoreChart = new CircularProgressBar();
        this.passRateChart = new CircularProgressBar();
        this.rankDistributionChart = new BarChart();
        this.summaryPanel = createSummaryPanel();
        
        initComponents();
        setupAutoUpdate();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateDashboard(); // Move initial update here
    }

    private void setupAutoUpdate() {
        autoUpdateTimer = new Timer(500, _ -> updateDashboard());
        autoUpdateTimer.start();
    }

    private void initComponents() {
        // Main panel with grid layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        
        // Top row - Summary cards
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        mainPanel.add(summaryPanel, gbc);
        
        // Middle row - Charts
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        JPanel chartsPanel = createChartsPanel();
        mainPanel.add(chartsPanel, gbc);
        
        // Bottom row - Student table
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        
        // Total Students Card
        panel.add(createSummaryCard("Total Students", "0", ColorScheme.PRIMARY));
        
        // Average Score Card
        panel.add(createSummaryCard("Average Score", "0.0", ColorScheme.SUCCESS));
        
        // Pass Rate Card
        panel.add(createSummaryCard("Pass Rate", "0%", ColorScheme.INFO));
        
        // Excellent Students Card
        panel.add(createSummaryCard("Excellent Students", "0", ColorScheme.SECONDARY));
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(ColorScheme.CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                // Draw accent line at top
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), 5, 5, 5);
                
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ColorScheme.TEXT);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);
        
        // Left chart - Average Score and Pass Rate
        JPanel leftChartPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        leftChartPanel.setOpaque(false);
        
        // Average Score Chart
        JPanel avgScorePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(ColorScheme.CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.dispose();
            }
        };
        avgScorePanel.setOpaque(false);
        avgScorePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel avgScoreTitle = new JLabel("Average Score");
        avgScoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avgScoreTitle.setForeground(ColorScheme.TEXT);
        
        averageScoreChart.setProgressColor(ColorScheme.SUCCESS);
        averageScoreChart.setMaxValue(10.0);
        averageScoreChart.setValue(0);
        averageScoreChart.setSubText("out of 10.0");
        
        avgScorePanel.add(avgScoreTitle, BorderLayout.NORTH);
        avgScorePanel.add(averageScoreChart, BorderLayout.CENTER);
        
        // Pass Rate Chart
        JPanel passRatePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(ColorScheme.CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.dispose();
            }
        };
        passRatePanel.setOpaque(false);
        passRatePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel passRateTitle = new JLabel("Pass Rate");
        passRateTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passRateTitle.setForeground(ColorScheme.TEXT);
        
        passRateChart.setProgressColor(ColorScheme.INFO);
        passRateChart.setValue(0);
        passRateChart.setSubText("of students");
        
        passRatePanel.add(passRateTitle, BorderLayout.NORTH);
        passRatePanel.add(passRateChart, BorderLayout.CENTER);
        
        leftChartPanel.add(avgScorePanel);
        leftChartPanel.add(passRatePanel);
        
        // Right chart - Rank Distribution
        JPanel rankDistPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(ColorScheme.CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.dispose();
            }
        };
        rankDistPanel.setOpaque(false);
        rankDistPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel rankDistTitle = new JLabel("Rank Distribution");
        rankDistTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rankDistTitle.setForeground(ColorScheme.TEXT);
        
        rankDistributionChart.setBarColor(ColorScheme.PRIMARY);
        
        rankDistPanel.add(rankDistTitle, BorderLayout.NORTH);
        rankDistPanel.add(rankDistributionChart, BorderLayout.CENTER);
        
        panel.add(leftChartPanel);
        panel.add(rankDistPanel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2d.setColor(ColorScheme.CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel tableTitle = new JLabel("Top Students");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(ColorScheme.TEXT);
        
        headerPanel.add(tableTitle, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(40);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setBackground(ColorScheme.CARD_BG);
        studentTable.setForeground(ColorScheme.TEXT);
        
        // Custom table header
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ColorScheme.CARD_BG_ACCENT);
        header.setForeground(ColorScheme.TEXT);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        // Custom cell renderer for styling
        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
                // Set alignment
                setHorizontalAlignment(column == 2 ? JLabel.CENTER : column == 3 ? JLabel.CENTER : JLabel.LEFT);
                
                // Set padding
                setBorder(new EmptyBorder(0, 15, 0, 15));
                
                // Lấy hàng đang hover
                Integer hoverRow = (Integer) table.getClientProperty("hoverRow");
                boolean isHover = hoverRow != null && hoverRow == row;
                
                // Set colors based on selection and hover
                if (isSelected) {
                    c.setBackground(new Color(ColorScheme.PRIMARY.getRed(), 
                                            ColorScheme.PRIMARY.getGreen(), 
                                            ColorScheme.PRIMARY.getBlue(), 40));
                    c.setForeground(ColorScheme.TEXT);
                } else if (isHover) {
                    // Hiệu ứng hover nhẹ nhàng hơn
                    c.setBackground(new Color(ColorScheme.PRIMARY.getRed(), 
                                            ColorScheme.PRIMARY.getGreen(), 
                                            ColorScheme.PRIMARY.getBlue(), 20));
                    c.setForeground(ColorScheme.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? ColorScheme.CARD_BG : ColorScheme.CARD_BG_ACCENT);
                    c.setForeground(ColorScheme.TEXT);
                }
                
                // Style rank column
                if (column == 3 && value != null) {
                    setForeground(getRankColor(value.toString()));
                }
                
                return c;
            }
        });

        // Thêm MouseMotionListener và MouseListener mới
        studentTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    studentTable.putClientProperty("hoverRow", row);
                    studentTable.repaint();
                }
            }
        });

        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                studentTable.putClientProperty("hoverRow", -1);
                studentTable.repaint();
            }
        });
        
        // Create a custom scroll pane with proper sizing
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ColorScheme.CARD_BG);
        scrollPane.setOpaque(false);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void updateDashboard() {
        if (studentManager == null) return;

        List<Student> students = studentManager.getAllStudents();
        
        // Update summary stats
        int totalStudents = students.size();
        double averageScore = studentManager.getAverageScore();
        int passCount = studentManager.getPassCount();
        double passRate = totalStudents > 0 ? (double) passCount / totalStudents * 100 : 0;
        
        // Update components
        updateSummaryCards(totalStudents, averageScore, passRate);
        updateCharts(averageScore, passRate);
        updateRankDistribution();
        updateTopStudentsTable();
        
        revalidate();
        repaint();
    }

    private void updateSummaryCards(int totalStudents, double averageScore, double passRate) {
        JPanel totalStudentsCard = (JPanel) summaryPanel.getComponent(0);
        JPanel totalStudentsContent = (JPanel) totalStudentsCard.getComponent(0);
        JLabel totalStudentsValue = (JLabel) totalStudentsContent.getComponent(1);
        totalStudentsValue.setText(String.valueOf(totalStudents));
        
        JPanel avgScoreCard = (JPanel) summaryPanel.getComponent(1);
        JPanel avgScoreContent = (JPanel) avgScoreCard.getComponent(0);
        JLabel avgScoreValue = (JLabel) avgScoreContent.getComponent(1);
        avgScoreValue.setText(String.format("%.2f", averageScore));
        
        JPanel passRateCard = (JPanel) summaryPanel.getComponent(2);
        JPanel passRateContent = (JPanel) passRateCard.getComponent(0);
        JLabel passRateValue = (JLabel) passRateContent.getComponent(1);
        passRateValue.setText(String.format("%.1f%%", passRate));
        
        int excellentCount = 0;
        for (Student student : studentManager.getAllStudents()) {
            if ("Excellent".equals(student.getRank())) {
                excellentCount++;
            }
        }
        JPanel excellentCard = (JPanel) summaryPanel.getComponent(3);
        JPanel excellentContent = (JPanel) excellentCard.getComponent(0);
        JLabel excellentValue = (JLabel) excellentContent.getComponent(1);
        excellentValue.setText(String.valueOf(excellentCount));
    }

    private void updateCharts(double averageScore, double passRate) {
        averageScoreChart.setValue(averageScore);
        averageScoreChart.setText(String.format("%.2f", averageScore));
        
        passRateChart.setValue(passRate);
        passRateChart.setText(String.format("%.1f%%", passRate));
    }

    private void updateRankDistribution() {
        Map<String, Integer> currentRankCounts = studentManager.getRankDistribution();

        // Check if the data has changed
        if (!currentRankCounts.equals(lastRankDistribution)) {
            lastRankDistribution = new HashMap<>(currentRankCounts);

            List<Double> values = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            values.add((double) currentRankCounts.get("Excellent"));
            values.add((double) currentRankCounts.get("Very Good"));
            values.add((double) currentRankCounts.get("Good"));
            values.add((double) currentRankCounts.get("Medium"));
            values.add((double) currentRankCounts.get("Fail"));

            labels.add("Excellent");
            labels.add("Very Good");
            labels.add("Good");
            labels.add("Medium");
            labels.add("Fail");

            rankDistributionChart.setData(values, labels);
        }
    }

    private void updateTopStudentsTable() {
        tableModel.setRowCount(0);
        List<Student> topStudents = studentManager.getTopStudents(10);
        
        for (Student student : topStudents) {
            Object[] row = {student.getId(), student.getName(), student.getScore(), student.getRank()};
            tableModel.addRow(row);
        }
    }

    private Color getRankColor(String rank) {
        return switch (rank) {
            case "Excellent" -> ColorScheme.EXCELLENT_COLOR;
            case "Very Good" -> ColorScheme.VERY_GOOD_COLOR;
            case "Good" -> ColorScheme.GOOD_COLOR;
            case "Medium" -> ColorScheme.MEDIUM_COLOR;
            case "Fail" -> ColorScheme.FAIL_COLOR;
            default -> ColorScheme.TEXT;
        };
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (autoUpdateTimer != null) {
            autoUpdateTimer.stop();
        }
    }
}
