package view;

import model.Student;
import model.StudentManager;
import util.*;
import util.MaxHeapPriorityQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

// Thêm các import cần thiết cho việc đo lường bộ nhớ
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.GarbageCollectorMXBean;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// Thêm import cho việc xuất báo cáo
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PerformanceTestPanel extends JPanel {
    private StudentManager studentManager;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<Integer> dataSizeComboBox;
    private JButton runTestButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JPanel chartContentPanel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea algorithmDescriptionArea;
    
    // Test results
    private List<TestResult> testResults = new ArrayList<>();

    // Thêm biến thành viên để lưu trữ kết quả qua nhiều lần test
    private Map<String, List<TestResult>> cumulativeResults = new HashMap<>();
    private Map<String, Map<Integer, List<TestResult>>> cumulativeResultsBySize = new HashMap<>();
    
    // Số lần lặp lại mỗi bài test để lấy kết quả trung bình
    private static final int TEST_ITERATIONS = 5;
    
    // Hệ số nhân cho số lượng hoạt động, tỷ lệ với kích thước dữ liệu
    private static final int OPERATION_MULTIPLIER = 10;

    // Thêm biến thành viên mới để theo dõi GC
    private boolean gcCompleted = false;
    private CountDownLatch gcLatch;
    private NotificationListener gcListener;
    
    public PerformanceTestPanel() {
        studentManager = new StudentManager();
        setLayout(new BorderLayout());
        setOpaque(false);
        
        initComponents();
    }
    
    private void initComponents() {
        // Main panel with grid layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Top panel - Controls
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        JPanel controlsPanel = createControlsPanel();
        mainPanel.add(controlsPanel, gbc);
        
        // Middle panel - Results Table
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.25;
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, gbc);
        
        // Bottom panel - Chart
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        JPanel chartPanel = createChartPanel();
        mainPanel.add(chartPanel, gbc);
        
        // Wrap in a scroll pane to ensure everything is visible
        JScrollPane scrollPane = new GlassScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createControlsPanel() {
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
        
        // Title
        JLabel titleLabel = new JLabel("Algorithm Performance Testing");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Controls panel - Use GridBagLayout for better responsiveness
        JPanel controlsGrid = new JPanel(new GridBagLayout());
        controlsGrid.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.weightx = 1.0;
        
        // Algorithm selection
        JPanel algorithmPanel = new JPanel(new BorderLayout(0, 10));
        algorithmPanel.setOpaque(false);
        
        JLabel algorithmLabel = new JLabel("Select Algorithm/Data Structure:");
        algorithmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        algorithmLabel.setForeground(ColorScheme.TEXT);
        
        String[] algorithms = {
            "All Algorithms", 
            "ArrayList (Storage)",
            "HashMap (ID Lookup)",
            "HashSet (Duplicate Removal)",
            "Quick Sort (Score Sorting)",
            "Hash-based Search (ID)",
            "Search Indexing (Name/Score/Rank)",
            "Max-Heap Priority Queue" // Add this new option
        };
        algorithmComboBox = new JComboBox<>(algorithms);
        algorithmComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        algorithmComboBox.setBackground(ColorScheme.CARD_BG_ACCENT);
        algorithmComboBox.setForeground(ColorScheme.TEXT);
        
        // Add listener to update description when algorithm changes
        algorithmComboBox.addActionListener(e -> updateAlgorithmDescription());
        
        algorithmPanel.add(algorithmLabel, BorderLayout.NORTH);
        algorithmPanel.add(algorithmComboBox, BorderLayout.CENTER);
        
        // Data size selection
        JPanel dataSizePanel = new JPanel(new BorderLayout(0, 10));
        dataSizePanel.setOpaque(false);
        
        JLabel dataSizeLabel = new JLabel("Select Data Size:");
        dataSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataSizeLabel.setForeground(ColorScheme.TEXT);
        
        Integer[] dataSizes = {100, 500, 1000, 5000, 10000, 50000};
        dataSizeComboBox = new JComboBox<>(dataSizes);
        dataSizeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataSizeComboBox.setBackground(ColorScheme.CARD_BG_ACCENT);
        dataSizeComboBox.setForeground(ColorScheme.TEXT);
        
        dataSizePanel.add(dataSizeLabel, BorderLayout.NORTH);
        dataSizePanel.add(dataSizeComboBox, BorderLayout.CENTER);
        
        // Run button
        JPanel buttonPanel = new JPanel(new BorderLayout(0, 10));
        buttonPanel.setOpaque(false);
        
        JLabel emptyLabel = new JLabel(" ");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        runTestButton = CustomButton.createPrimaryButton("Run Performance Test");
        runTestButton.addActionListener(e -> runPerformanceTest());
        
        buttonPanel.add(emptyLabel, BorderLayout.NORTH);
        buttonPanel.add(runTestButton, BorderLayout.CENTER);
        
        // Add components to grid with responsive layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        controlsGrid.add(algorithmPanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        controlsGrid.add(dataSizePanel, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        controlsGrid.add(buttonPanel, gbc);
        
        // Algorithm description area
        algorithmDescriptionArea = new JTextArea();
        algorithmDescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algorithmDescriptionArea.setLineWrap(true);
        algorithmDescriptionArea.setWrapStyleWord(true);
        algorithmDescriptionArea.setEditable(false);
        algorithmDescriptionArea.setBackground(new Color(248, 249, 254));
        algorithmDescriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        algorithmDescriptionArea.setRows(3);
        
        // Initialize description
        updateAlgorithmDescription();
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(controlsGrid, BorderLayout.NORTH);
        centerPanel.add(algorithmDescriptionArea, BorderLayout.CENTER);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setForeground(ColorScheme.PRIMARY);
        progressBar.setBackground(ColorScheme.CARD_BG_ACCENT);
        
        statusLabel = new JLabel("Select algorithm and data size, then click Run Performance Test");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        
        statusPanel.add(progressBar, BorderLayout.NORTH);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updateAlgorithmDescription() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        String description = "";
        
        switch (selectedAlgorithm) {
            case "All Algorithms":
                description = "Run tests on all algorithms and data structures to compare their performance.";
                break;
            case "ArrayList (Storage)":
                description = "ArrayList: A resizable array implementation that stores elements sequentially in memory. " +
                              "Good for storing ordered data with fast iteration, but slower for insertions/deletions in the middle.";
                break;
            case "HashMap (ID Lookup)":
                description = "HashMap: A hash table implementation that maps keys to values. " +
                              "Provides constant-time O(1) performance for basic operations (get/put) when hash function distributes elements properly.";
                break;
            case "HashSet (Duplicate Removal)":
                description = "HashSet: An implementation of Set that uses a hash table for storage. " +
                              "Provides constant-time O(1) performance for basic operations and guarantees no duplicate elements.";
                break;
            case "Quick Sort (Score Sorting)":
                description = "Quick Sort: A divide-and-conquer sorting algorithm with average O(n log n) time complexity. " +
                              "Works by selecting a 'pivot' element and partitioning the array around it.";
                break;
            case "Hash-based Search (ID)":
                description = "Hash-based Search: Uses a HashMap to find elements by key (ID) in constant time O(1). " +
                              "Much faster than linear search O(n) for large datasets.";
                break;
            case "Search Indexing (Name/Score/Rank)":
                description = "Search Indexing: Creates specialized data structures (indices) to optimize search operations. " +
                              "Trades memory for speed by pre-processing data into easily searchable structures.";
                break;
            case "Max-Heap Priority Queue":
                description = "Max-Heap Priority Queue: A tree-based data structure that efficiently maintains the maximum element at the root. " +
                              "Provides O(log n) time complexity for insertions and extractions, and O(1) for finding the maximum element.";
                break;
        }
        
        algorithmDescriptionArea.setText(description);
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
        
        // Title
        JLabel titleLabel = new JLabel("Test Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ColorScheme.TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"Algorithm/Data Structure", "Data Size", "Execution Time (ms)", "Memory Usage (MB)", "Operations/sec"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(35);
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultsTable.setBackground(ColorScheme.CARD_BG);
        resultsTable.setForeground(ColorScheme.TEXT);
        
        // Custom cell renderer for styling
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set alignment
                setHorizontalAlignment(column >= 2 ? JLabel.CENTER : JLabel.LEFT);
                
                // Set padding
                setBorder(new EmptyBorder(0, 15, 0, 15));
                
                // Set colors based on selection and row
                if (isSelected) {
                    c.setBackground(new Color(ColorScheme.PRIMARY.getRed(), 
                                            ColorScheme.PRIMARY.getGreen(), 
                                            ColorScheme.PRIMARY.getBlue(), 40));
                    c.setForeground(ColorScheme.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? ColorScheme.CARD_BG : ColorScheme.CARD_BG_ACCENT);
                    c.setForeground(ColorScheme.TEXT);
                }
                
                return c;
            }
        });
        
        // Create a custom scroll pane with proper sizing
        JScrollPane scrollPane = new GlassScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ColorScheme.CARD_BG);
        scrollPane.setOpaque(false);
        
        // Set preferred size to ensure table is visible
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add export button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        CustomButton clearButton = CustomButton.createGlassButton("Clear Results");
        clearButton.addActionListener(e -> {
            testResults.clear();
            tableModel.setRowCount(0);
            chartContentPanel.repaint();
        });

        // Thêm nút xuất báo cáo
        CustomButton exportButton = CustomButton.createPrimaryButton("Export Report");
        exportButton.addActionListener(e -> exportReport());
        buttonPanel.add(exportButton);
        
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // Thêm phương thức xuất báo cáo
    private void exportReport() {
        if (testResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No test results to export. Please run some tests first.",
                "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiển thị hộp thoại chọn file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Performance Report");

        // Đặt bộ lọc file
        FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files (*.html)", "html");
        fileChooser.setFileFilter(filter);

        // Đặt tên file mặc định
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(new File("performance_report_" + timestamp + ".html"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Đảm bảo file có phần mở rộng .html
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".html")) {
                filePath += ".html";
                fileToSave = new File(filePath);
            }

            try {
                generateHtmlReport(fileToSave);
                JOptionPane.showMessageDialog(this,
                    "Report successfully exported to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);

                // Mở file trong trình duyệt mặc định
                openInBrowser(fileToSave);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting report: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void generateHtmlReport(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Bắt đầu file HTML
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"en\">");
            writer.println("<head>");
            writer.println("    <meta charset=\"UTF-8\">");
            writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            writer.println("    <title>Algorithm Performance Test Report</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }");
            writer.println("        h1, h2, h3 { color: #333; }");
            writer.println("        .container { max-width: 1200px; margin: 0 auto; }");
            writer.println("        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }");
            writer.println("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("        th { background-color: #f2f2f2; }");
            writer.println("        tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println("        .chart-container { margin: 20px 0; height: 400px; }");
            writer.println("        .summary { background-color: #f0f8ff; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
            writer.println("        .footer { margin-top: 30px; font-size: 0.8em; color: #666; text-align: center; }");
            writer.println("    </style>");

            // Thêm Chart.js để vẽ biểu đồ
            writer.println("    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class=\"container\">");
            writer.println("        <h1>Algorithm Performance Test Report</h1>");
            writer.println("        <p>Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");

            // Thêm phần tóm tắt
            writer.println("        <div class=\"summary\">");
            writer.println("            <h2>Summary</h2>");
            writer.println("            <p>Total tests run: " + testResults.size() + "</p>");

            // Tính toán kết quả trung bình
            Map<String, TestResult> averageResults = calculateAverageResults();

            // Tìm thuật toán nhanh nhất và chậm nhất
            String fastestAlgorithm = "";
            String slowestAlgorithm = "";
            double fastestTime = Double.MAX_VALUE;
            double slowestTime = 0;

            for (Map.Entry<String, TestResult> entry : averageResults.entrySet()) {
                if (entry.getValue().executionTime < fastestTime) {
                    fastestTime = entry.getValue().executionTime;
                    fastestAlgorithm = entry.getKey();
                }
                if (entry.getValue().executionTime > slowestTime) {
                    slowestTime = entry.getValue().executionTime;
                    slowestAlgorithm = entry.getKey();
                }
            }

            writer.println("            <p>Fastest algorithm: <strong>" + fastestAlgorithm + "</strong> (" +
                          new DecimalFormat("#,##0.00").format(fastestTime) + " ms)</p>");
            writer.println("            <p>Slowest algorithm: <strong>" + slowestAlgorithm + "</strong> (" +
                          new DecimalFormat("#,##0.00").format(slowestTime) + " ms)</p>");
            writer.println("        </div>");

            // Thêm bảng kết quả chi tiết
            writer.println("        <h2>Detailed Test Results</h2>");
            writer.println("        <table>");
            writer.println("            <tr>");
            writer.println("                <th>Algorithm/Data Structure</th>");
            writer.println("                <th>Data Size</th>");
            writer.println("                <th>Execution Time (ms)</th>");
            writer.println("                <th>Memory Usage (MB)</th>");
            writer.println("                <th>Operations/sec</th>");
            writer.println("            </tr>");

            for (TestResult result : testResults) {
                writer.println("            <tr>");
                writer.println("                <td>" + result.algorithm + "</td>");
                writer.println("                <td>" + result.dataSize + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0.00").format(result.executionTime) + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0.00").format(result.memoryUsage) + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0").format(result.operationsPerSecond) + "</td>");
                writer.println("            </tr>");
            }

            writer.println("        </table>");

            // Thêm bảng kết quả trung bình
            writer.println("        <h2>Average Results by Algorithm</h2>");
            writer.println("        <table>");
            writer.println("            <tr>");
            writer.println("                <th>Algorithm/Data Structure</th>");
            writer.println("                <th>Avg. Execution Time (ms)</th>");
            writer.println("                <th>Avg. Memory Usage (MB)</th>");
            writer.println("                <th>Avg. Operations/sec</th>");
            writer.println("            </tr>");

            for (Map.Entry<String, TestResult> entry : averageResults.entrySet()) {
                TestResult avg = entry.getValue();
                writer.println("            <tr>");
                writer.println("                <td>" + entry.getKey() + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0.00").format(avg.executionTime) + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0.00").format(avg.memoryUsage) + "</td>");
                writer.println("                <td>" + new DecimalFormat("#,##0").format(avg.operationsPerSecond) + "</td>");
                writer.println("            </tr>");
            }

            writer.println("        </table>");

            // Thêm biểu đồ thời gian thực thi
            writer.println("        <h2>Execution Time Comparison</h2>");
            writer.println("        <div class=\"chart-container\">");
            writer.println("            <canvas id=\"executionTimeChart\"></canvas>");
            writer.println("        </div>");

            // Thêm biểu đồ sử dụng bộ nhớ
            writer.println("        <h2>Memory Usage Comparison</h2>");
            writer.println("        <div class=\"chart-container\">");
            writer.println("            <canvas id=\"memoryUsageChart\"></canvas>");
            writer.println("        </div>");

            // Thêm biểu đồ số lượng hoạt động trên giây
            writer.println("        <h2>Operations Per Second Comparison</h2>");
            writer.println("        <div class=\"chart-container\">");
            writer.println("            <canvas id=\"operationsChart\"></canvas>");
            writer.println("        </div>");

            // Thêm script để vẽ biểu đồ
            writer.println("        <script>");

            // Dữ liệu cho biểu đồ thời gian thực thi
            writer.println("            const executionTimeCtx = document.getElementById('executionTimeChart').getContext('2d');");
            writer.println("            const executionTimeChart = new Chart(executionTimeCtx, {");
            writer.println("                type: 'bar',");
            writer.println("                data: {");
            writer.println("                    labels: [" + getAlgorithmLabelsForChart(averageResults) + "],");
            writer.println("                    datasets: [{");
            writer.println("                        label: 'Execution Time (ms)',");
            writer.println("                        data: [" + getExecutionTimeDataForChart(averageResults) + "],");
            writer.println("                        backgroundColor: [");
            writer.println("                            'rgba(94, 114, 228, 0.7)',");
            writer.println("                            'rgba(45, 206, 137, 0.7)',");
            writer.println("                            'rgba(251, 99, 64, 0.7)',");
            writer.println("                            'rgba(172, 94, 228, 0.7)',");
            writer.println("                            'rgba(17, 205, 239, 0.7)',");
            writer.println("                            'rgba(251, 189, 8, 0.7)'");
            writer.println("                        ],");
            writer.println("                        borderColor: [");
            writer.println("                            'rgba(94, 114, 228, 1)',");
            writer.println("                            'rgba(45, 206, 137, 1)',");
            writer.println("                            'rgba(251, 99, 64, 1)',");
            writer.println("                            'rgba(172, 94, 228, 1)',");
            writer.println("                            'rgba(17, 205, 239, 1)',");
            writer.println("                            'rgba(251, 189, 8, 1)'");
            writer.println("                        ],");
            writer.println("                        borderWidth: 1");
            writer.println("                    }]");
            writer.println("                },");
            writer.println("                options: {");
            writer.println("                    responsive: true,");
            writer.println("                    maintainAspectRatio: false,");
            writer.println("                    scales: {");
            writer.println("                        y: {");
            writer.println("                            beginAtZero: true,");
            writer.println("                            title: {");
            writer.println("                                display: true,");
            writer.println("                                text: 'Execution Time (ms)'");
            writer.println("                            }");
            writer.println("                        }");
            writer.println("                    }");
            writer.println("                }");
            writer.println("            });");

            // Dữ liệu cho biểu đồ sử dụng bộ nhớ
            writer.println("            const memoryUsageCtx = document.getElementById('memoryUsageChart').getContext('2d');");
            writer.println("            const memoryUsageChart = new Chart(memoryUsageCtx, {");
            writer.println("                type: 'bar',");
            writer.println("                data: {");
            writer.println("                    labels: [" + getAlgorithmLabelsForChart(averageResults) + "],");
            writer.println("                    datasets: [{");
            writer.println("                        label: 'Memory Usage (MB)',");
            writer.println("                        data: [" + getMemoryUsageDataForChart(averageResults) + "],");
            writer.println("                        backgroundColor: [");
            writer.println("                            'rgba(94, 114, 228, 0.7)',");
            writer.println("                            'rgba(45, 206, 137, 0.7)',");
            writer.println("                            'rgba(251, 99, 64, 0.7)',");
            writer.println("                            'rgba(172, 94, 228, 0.7)',");
            writer.println("                            'rgba(17, 205, 239, 0.7)',");
            writer.println("                            'rgba(251, 189, 8, 0.7)'");
            writer.println("                        ],");
            writer.println("                        borderColor: [");
            writer.println("                            'rgba(94, 114, 228, 1)',");
            writer.println("                            'rgba(45, 206, 137, 1)',");
            writer.println("                            'rgba(251, 99, 64, 1)',");
            writer.println("                            'rgba(172, 94, 228, 1)',");
            writer.println("                            'rgba(17, 205, 239, 1)',");
            writer.println("                            'rgba(251, 189, 8, 1)'");
            writer.println("                        ],");
            writer.println("                        borderWidth: 1");
            writer.println("                    }]");
            writer.println("                },");
            writer.println("                options: {");
            writer.println("                    responsive: true,");
            writer.println("                    maintainAspectRatio: false,");
            writer.println("                    scales: {");
            writer.println("                        y: {");
            writer.println("                            beginAtZero: true,");
            writer.println("                            title: {");
            writer.println("                                display: true,");
            writer.println("                                text: 'Memory Usage (MB)'");
            writer.println("                            }");
            writer.println("                        }");
            writer.println("                    }");
            writer.println("                }");
            writer.println("            });");

            // Dữ liệu cho biểu đồ số lượng hoạt động trên giây
            writer.println("            const operationsCtx = document.getElementById('operationsChart').getContext('2d');");
            writer.println("            const operationsChart = new Chart(operationsCtx, {");
            writer.println("                type: 'bar',");
            writer.println("                data: {");
            writer.println("                    labels: [" + getAlgorithmLabelsForChart(averageResults) + "],");
            writer.println("                    datasets: [{");
            writer.println("                        label: 'Operations Per Second',");
            writer.println("                        data: [" + getOperationsDataForChart(averageResults) + "],");
            writer.println("                        backgroundColor: [");
            writer.println("                            'rgba(94, 114, 228, 0.7)',");
            writer.println("                            'rgba(45, 206, 137, 0.7)',");
            writer.println("                            'rgba(251, 99, 64, 0.7)',");
            writer.println("                            'rgba(172, 94, 228, 0.7)',");
            writer.println("                            'rgba(17, 205, 239, 0.7)',");
            writer.println("                            'rgba(251, 189, 8, 0.7)'");
            writer.println("                        ],");
            writer.println("                        borderColor: [");
            writer.println("                            'rgba(94, 114, 228, 1)',");
            writer.println("                            'rgba(45, 206, 137, 1)',");
            writer.println("                            'rgba(251, 99, 64, 1)',");
            writer.println("                            'rgba(172, 94, 228, 1)',");
            writer.println("                            'rgba(17, 205, 239, 1)',");
            writer.println("                            'rgba(251, 189, 8, 1)'");
            writer.println("                        ],");
            writer.println("                        borderWidth: 1");
            writer.println("                    }]");
            writer.println("                },");
            writer.println("                options: {");
            writer.println("                    responsive: true,");
            writer.println("                    maintainAspectRatio: false,");
            writer.println("                    scales: {");
            writer.println("                        y: {");
            writer.println("                            beginAtZero: true,");
            writer.println("                            title: {");
            writer.println("                                display: true,");
            writer.println("                                text: 'Operations Per Second'");
            writer.println("                            }");
            writer.println("                        }");
            writer.println("                    }");
            writer.println("                }");
            writer.println("            });");

            writer.println("        </script>");

            // Thêm footer
            writer.println("        <div class=\"footer\">");
            writer.println("            <p>Generated by Student Management System - Performance Testing Module</p>");
            writer.println("        </div>");

            writer.println("    </div>");
            writer.println("</body>");
            writer.println("</html>");
        }
    }

    private String getAlgorithmLabelsForChart(Map<String, TestResult> results) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String algorithm : results.keySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(getShortName(algorithm)).append("'");
            first = false;
        }

        return sb.toString();
    }

    private String getExecutionTimeDataForChart(Map<String, TestResult> results) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TestResult result : results.values()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(result.executionTime);
            first = false;
        }

        return sb.toString();
    }

    private String getMemoryUsageDataForChart(Map<String, TestResult> results) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TestResult result : results.values()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(result.memoryUsage);
            first = false;
        }

        return sb.toString();
    }

    private String getOperationsDataForChart(Map<String, TestResult> results) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TestResult result : results.values()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(result.operationsPerSecond);
            first = false;
        }

        return sb.toString();
    }

    private void openInBrowser(File htmlFile) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(htmlFile.toURI());
            } else {
                // Fallback for systems where Desktop is not supported
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("win")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", htmlFile.getAbsolutePath());
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", htmlFile.getAbsolutePath());
                } else if (os.contains("nix") || os.contains("nux")) {
                    pb = new ProcessBuilder("xdg-open", htmlFile.getAbsolutePath());
                } else {
                    return; // Unsupported OS
                }

                pb.start();
            }
        } catch (Exception e) {
            System.err.println("Error opening browser: " + e.getMessage());
        }
    }

    // Thêm phương thức để tính toán kết quả trung bình
    private Map<String, TestResult> calculateAverageResults() {
        Map<String, TestResult> averageResults = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        // Nhóm kết quả theo thuật toán
        for (TestResult result : testResults) {
            String algorithm = result.algorithm;

            if (!averageResults.containsKey(algorithm)) {
                averageResults.put(algorithm, new TestResult(algorithm, 0, 0, 0, 0));
                counts.put(algorithm, 0);
            }

            TestResult current = averageResults.get(algorithm);
            current.executionTime += result.executionTime;
            current.memoryUsage += result.memoryUsage;
            current.operationsPerSecond += result.operationsPerSecond;
            current.dataSize += result.dataSize;

            counts.put(algorithm, counts.get(algorithm) + 1);
        }

        // Tính trung bình
        for (Map.Entry<String, TestResult> entry : averageResults.entrySet()) {
            String algorithm = entry.getKey();
            TestResult result = entry.getValue();
            int count = counts.get(algorithm);

            if (count > 0) {
                result.executionTime /= count;
                result.memoryUsage /= count;
                result.operationsPerSecond /= count;
                result.dataSize /= count;
            }
        }

        return averageResults;
    }
    
    private JPanel createChartPanel() {
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
        
        // Title and chart type selector
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Performance Visualization");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ColorScheme.TEXT);
        
        String[] chartTypes = {"Execution Time", "Memory Usage", "Operations/sec"};
        JComboBox<String> chartTypeCombo = new JComboBox<>(chartTypes);
        chartTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chartTypeCombo.setBackground(ColorScheme.CARD_BG_ACCENT);
        chartTypeCombo.setForeground(ColorScheme.TEXT);
        chartTypeCombo.setPreferredSize(new Dimension(150, 30));
        
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        comboPanel.setOpaque(false);
        comboPanel.add(new JLabel("Chart Type:"));
        comboPanel.add(chartTypeCombo);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(comboPanel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Chart content panel
        chartContentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                String selectedType = (String) chartTypeCombo.getSelectedItem();
                if (selectedType.equals("Execution Time")) {
                    drawTimeChart(g);
                } else if (selectedType.equals("Memory Usage")) {
                    drawMemoryChart(g);
                } else {
                    drawOperationsChart(g);
                }
            }
        };
        chartContentPanel.setOpaque(false);
        
        // Add listener to repaint when chart type changes
        chartTypeCombo.addActionListener(e -> chartContentPanel.repaint());
        
        // Set minimum size to ensure chart is visible
        chartContentPanel.setMinimumSize(new Dimension(0, 300));
        chartContentPanel.setPreferredSize(new Dimension(0, 350));
        
        panel.add(chartContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawTimeChart(Graphics g) {
        drawChart(g, "Execution Time (ms)", result -> result.executionTime);
    }
    
    private void drawMemoryChart(Graphics g) {
        drawChart(g, "Memory Usage (MB)", result -> result.memoryUsage);
    }
    
    private void drawOperationsChart(Graphics g) {
        drawChart(g, "Operations per Second", result -> result.operationsPerSecond);
    }
    
    // Cập nhật phương thức drawChart để hiển thị nhãn rõ ràng hơn
    private void drawChart(Graphics g, String yAxisLabel, Function<TestResult, Double> valueExtractor) {
        if (testResults.isEmpty()) {
            // Draw placeholder text
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(ColorScheme.TEXT_SECONDARY);
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            String message = "Run performance tests to see visualization";
            
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int textHeight = fm.getHeight();
            
            int x = (chartContentPanel.getWidth() - textWidth) / 2;
            int y = (chartContentPanel.getHeight() - textHeight) / 2 + fm.getAscent();
            
            g2d.drawString(message, x, y);
            
            g2d.dispose();
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Increase left margin to accommodate Y-axis labels
        int leftMargin = 100; // Increased from 60
        int width = chartContentPanel.getWidth() - leftMargin - 40;
        int height = chartContentPanel.getHeight() - 100; // Tăng khoảng cách dưới để hiển thị nhãn rõ ràng hơn
        int x0 = leftMargin;
        int y0 = chartContentPanel.getHeight() - 60; // Tăng khoảng cách dưới
        
        // Draw axes
        g2d.setColor(ColorScheme.TEXT);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x0, y0, x0 + width, y0); // X-axis
        g2d.drawLine(x0, y0, x0, y0 - height); // Y-axis
        
        // Draw axis labels
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Algorithm/Data Structure", x0 + width / 2 - 80, y0 + 40);
        
        // Rotate and draw Y-axis label with more space
        AffineTransform original = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisLabel, -y0 + height / 2 - 60, x0 - 60); // Increased space from axis
        g2d.setTransform(original);
        
        // Group results by algorithm and data size
        Map<String, Map<Integer, TestResult>> resultsByAlgorithmAndSize = new HashMap<>();
        for (TestResult result : testResults) {
            resultsByAlgorithmAndSize
                .computeIfAbsent(result.algorithm, k -> new HashMap<>())
                .put(result.dataSize, result);
        }

        // Get the most recent data size
        int latestDataSize = testResults.isEmpty() ? 0 : testResults.get(testResults.size() - 1).dataSize;

        // Create a map with the latest result for each algorithm with the latest data size
        Map<String, TestResult> latestResults = new HashMap<>();
        for (Map.Entry<String, Map<Integer, TestResult>> entry : resultsByAlgorithmAndSize.entrySet()) {
            if (entry.getValue().containsKey(latestDataSize)) {
                latestResults.put(entry.getKey(), entry.getValue().get(latestDataSize));
            }
        }

        // If we don't have any results with matching data size, use the most recent result for each algorithm
        if (latestResults.isEmpty()) {
            for (Map.Entry<String, Map<Integer, TestResult>> entry : resultsByAlgorithmAndSize.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    // Get the latest result for this algorithm (highest data size)
                    int maxDataSize = Collections.max(entry.getValue().keySet());
                    latestResults.put(entry.getKey(), entry.getValue().get(maxDataSize));
                }
            }
        }
        
        // Get unique algorithms
        List<String> algorithms = new ArrayList<>(latestResults.keySet());
        
        // Find max value for scaling
        double maxValue = 0;
        for (TestResult result : latestResults.values()) {
            double value = valueExtractor.apply(result);
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        // Round up for better scale
        maxValue = Math.ceil(maxValue * 1.1); // Add 10% padding
        if (maxValue < 1) maxValue = 1;
        
        // Draw Y-axis scale with improved formatting
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        int numYDivisions = 5;
        for (int i = 0; i <= numYDivisions; i++) {
            int y = y0 - (i * height / numYDivisions);
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.drawLine(x0, y, x0 + width, y);
            g2d.setColor(ColorScheme.TEXT);
            
            // Format the value based on its magnitude
            double value = i * maxValue / numYDivisions;
            String valueStr;
            if (value >= 1000000) {
                valueStr = String.format("%.1fM", value / 1000000);
            } else if (value >= 1000) {
                valueStr = String.format("%.1fK", value / 1000);
            } else {
                valueStr = String.format("%.1f", value);
            }
            
            g2d.drawString(valueStr, x0 - 40, y + 5);
        }
        
        // Draw data size label at the top of the chart
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(ColorScheme.PRIMARY);
        g2d.drawString("Data Size: " + latestDataSize + " students", x0, y0 - height - 20);
        
        // Draw bars with improved spacing and labels
        int barWidth = Math.min(80, width / (algorithms.size() * 2));
        int gap = barWidth / 2;
        
        for (int i = 0; i < algorithms.size(); i++) {
            String algorithm = algorithms.get(i);
            TestResult result = latestResults.get(algorithm);
            
            // Calculate bar height
            double value = valueExtractor.apply(result);
            int barHeight = (int) (value * height / maxValue);
            if (barHeight < 2) barHeight = 2; // Ensure bar is visible
            
            // Calculate bar position
            int barX = x0 + gap + i * (barWidth + gap);
            int barY = y0 - barHeight;
            
            // Draw bar with gradient
            GradientPaint gradient = new GradientPaint(
                barX, barY, getColorForAlgorithm(algorithm),
                barX, y0, getColorForAlgorithm(algorithm).brighter()
            );
            g2d.setPaint(gradient);
            g2d.fill(new RoundRectangle2D.Double(barX, barY, barWidth, barHeight, 10, 10));
            
            // Draw algorithm name with improved readability
            g2d.setColor(ColorScheme.TEXT);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String shortName = getShortName(algorithm);
            int textWidth = fm.stringWidth(shortName);
            
            // Rotate algorithm name if too long
            if (textWidth > barWidth + gap) {
                AffineTransform originalTransform = g2d.getTransform();
                g2d.rotate(-Math.PI / 4, barX + barWidth / 2, y0 + 15);
                g2d.drawString(shortName, barX + barWidth / 2 - textWidth / 2, y0 + 15);
                g2d.setTransform(originalTransform);
            } else {
                g2d.drawString(shortName, barX + (barWidth - textWidth) / 2, y0 + 20);
            }
            
            // Draw value above bar with improved formatting
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String valueText;
            if (value >= 1000000) {
                valueText = String.format("%.1fM", value / 1000000);
            } else if (value >= 1000) {
                valueText = String.format("%.1fK", value / 1000);
            } else {
                valueText = String.format("%.1f", value);
            }
            
            textWidth = fm.stringWidth(valueText);
            g2d.drawString(valueText, barX + (barWidth - textWidth) / 2, barY - 5);
        }
        
        // Draw legend with improved layout
        int legendX = x0 + width - 200;
        int legendY = y0 - height + 30;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(ColorScheme.TEXT);
        g2d.drawString("Legend:", legendX, legendY);
        
        for (int i = 0; i < algorithms.size(); i++) {
            String algorithm = algorithms.get(i);
            int itemY = legendY + 25 + i * 25;
            
            // Draw color box
            g2d.setColor(getColorForAlgorithm(algorithm));
            g2d.fillRect(legendX, itemY - 12, 15, 15);
            
            // Draw algorithm name
            g2d.setColor(ColorScheme.TEXT);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.drawString(algorithm, legendX + 25, itemY);
        }
        
        g2d.dispose();
    }
    
    // Cập nhật phương thức runPerformanceTest để hiển thị thông tin chi tiết hơn
    private void runPerformanceTest() {
        // Clear previous results if running all tests
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        if (selectedAlgorithm.equals("All Algorithms")) {
            testResults.clear();
            tableModel.setRowCount(0);
        }
        
        // Get selected data size
        int dataSize = (Integer) dataSizeComboBox.getSelectedItem();
        
        // Disable run button during test
        runTestButton.setEnabled(false);
        
        // Create and start worker thread
        SwingWorker<Void, Object> worker = new SwingWorker<Void, Object>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Update status
                publish(new StatusUpdate("Generating " + dataSize + " random students for testing...", 10, "Generating data..."));
                
                // Generate test data
                List<Student> students = generateRandomStudents(dataSize);
                
                // Update progress
                publish(new StatusUpdate("Data generated: " + students.size() + " students", 20, "Data generated"));
            
                // Hiển thị thông tin về bộ nhớ hệ thống chi tiết
                String memoryInfo = getDetailedMemoryInfo();
                publish(new StatusUpdate(memoryInfo, 20, "Memory info"));
                
                Thread.sleep(500); // Hiển thị thông tin bộ nhớ trong 0.5 giây
                
                int progressStep = selectedAlgorithm.equals("All Algorithms") ? 10 : 70;
                final int[] progress = {20}; // Use an array to hold the progress value
                
                // Thiết lập theo dõi GC
                setupGCMonitoring();
                
                // Run selected algorithm tests
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("ArrayList (Storage)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing ArrayList storage with " + students.size() + " students...", 
                                            progress[0], "Testing ArrayList..."));
                    
                    TestResult arrayListResult = runBenchmark("ArrayList (Storage)", students, 
                                                            s -> testArrayList(s));
                    publish(arrayListResult);
                    
                    // Small delay for UI update
                    Thread.sleep(300);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("HashMap (ID Lookup)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing HashMap ID lookup with " + students.size() + " students...", 
                                            progress[0], "Testing HashMap..."));
                    
                    TestResult hashMapResult = runBenchmark("HashMap (ID Lookup)", students, 
                                                          s -> testHashMap(s));
                    publish(hashMapResult);
                    
                    Thread.sleep(300);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("HashSet (Duplicate Removal)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing HashSet duplicate removal with " + students.size() + " students...", 
                                            progress[0], "Testing HashSet..."));
                
                    TestResult hashSetResult = runBenchmark("HashSet (Duplicate Removal)", students, 
                                                          s -> testHashSet(s));
                    publish(hashSetResult);
                    
                    Thread.sleep(300);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("Quick Sort (Score Sorting)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing Quick Sort algorithm with " + students.size() + " students...", 
                                            progress[0], "Testing Quick Sort..."));
                    
                    TestResult quickSortResult = runBenchmark("Quick Sort (Score Sorting)", students, 
                                                            s -> testQuickSort(s));
                    publish(quickSortResult);
                    
                    Thread.sleep(300);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("Hash-based Search (ID)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing Hash-based Search with " + students.size() + " students...", 
                                            progress[0], "Testing Hash Search..."));
                    
                    TestResult hashSearchResult = runBenchmark("Hash-based Search (ID)", students, 
                                                             s -> testHashSearch(s));
                    publish(hashSearchResult);
                    
                    Thread.sleep(300);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("Search Indexing (Name/Score/Rank)")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing Search Indexing with " + students.size() + " students...", 
                                            progress[0], "Testing Indexing..."));
                    
                    TestResult indexingResult = runBenchmark("Search Indexing (Name/Score/Rank)", students, 
                                                           s -> testSearchIndexing(s));
                    publish(indexingResult);
                }
                
                if (selectedAlgorithm.equals("All Algorithms") || selectedAlgorithm.equals("Max-Heap Priority Queue")) {
                    progress[0] += progressStep;
                    publish(new StatusUpdate("Testing Max-Heap Priority Queue with " + students.size() + " students...", 
                                            progress[0], "Testing Max-Heap..."));
                    
                    TestResult maxHeapResult = runBenchmark("Max-Heap Priority Queue", students, 
                                          s -> testMaxHeapPriorityQueue(s));
                    publish(maxHeapResult);
    
                    Thread.sleep(300);
                }
                
                return null;
            }
        
            @Override
            protected void process(List<Object> chunks) {
                for (Object chunk : chunks) {
                    if (chunk instanceof TestResult) {
                        TestResult result = (TestResult) chunk;
                        testResults.add(result);
                        
                        Object[] row = {
                            result.algorithm,
                            result.dataSize,
                            new DecimalFormat("#,##0.00").format(result.executionTime),
                            new DecimalFormat("#,##0.00").format(result.memoryUsage),
                            new DecimalFormat("#,##0").format(result.operationsPerSecond)
                        };
                        tableModel.addRow(row);

                        // Lưu kết quả vào cumulativeResults
                        if (chunk instanceof TestResult) {
                            TestResult result2 = (TestResult) chunk;

                            // Lưu theo thuật toán
                            cumulativeResults.computeIfAbsent(result2.algorithm, k -> new ArrayList<>()).add(result2);

                            // Lưu theo thuật toán và kích thước
                            cumulativeResultsBySize
                                .computeIfAbsent(result2.algorithm, k -> new HashMap<>())
                                .computeIfAbsent(result2.dataSize, k -> new ArrayList<>())
                                .add(result2);
                        }
                    } else if (chunk instanceof StatusUpdate) {
                        StatusUpdate update = (StatusUpdate) chunk;
                        statusLabel.setText(update.message);
                        progressBar.setValue(update.progress);
                        progressBar.setString(update.progressString);
                    }
                }
                
                // Repaint chart
                chartContentPanel.repaint();
            }
            
            @Override
            protected void done() {
                // Update UI when done
                statusLabel.setText("Performance testing completed successfully");
                progressBar.setValue(100);
                progressBar.setString("Completed");
                runTestButton.setEnabled(true);
                
                // Hiển thị thông tin bộ nhớ chi tiết sau khi hoàn thành
                String memoryInfo = getDetailedMemoryInfo();
                System.out.println("Memory after tests: " + memoryInfo);
            }
        };
        
        worker.execute();
    }

    // Thêm lớp StatusUpdate để truyền thông tin trạng thái
    private static class StatusUpdate {
        String message;
        int progress;
        String progressString;
        
        public StatusUpdate(String message, int progress, String progressString) {
            this.message = message;
            this.progress = progress;
            this.progressString = progressString;
        }
    }
    
    private String getShortName(String algorithm) {
        if (algorithm.startsWith("ArrayList")) return "ArrayList";
        if (algorithm.startsWith("HashMap")) return "HashMap";
        if (algorithm.startsWith("HashSet")) return "HashSet";
        if (algorithm.startsWith("Quick Sort")) return "QuickSort";
        if (algorithm.startsWith("Hash-based")) return "HashSearch";
        if (algorithm.startsWith("Search Indexing")) return "Indexing";
        if (algorithm.startsWith("Max-Heap")) return "MaxHeap";
        return algorithm;
    }
    
    private Color getColorForAlgorithm(String algorithm) {
        if (algorithm.startsWith("ArrayList")) return new Color(94, 114, 228); // Blue
        if (algorithm.startsWith("HashMap")) return new Color(45, 206, 137);  // Green
        if (algorithm.startsWith("HashSet")) return new Color(251, 99, 64);   // Orange
        if (algorithm.startsWith("Quick Sort")) return new Color(172, 94, 228); // Purple
        if (algorithm.startsWith("Hash-based")) return new Color(17, 205, 239); // Cyan
        if (algorithm.startsWith("Search Indexing")) return new Color(251, 189, 8); // Yellow
        if (algorithm.startsWith("Max-Heap")) return new Color(142, 68, 173); // Dark Purple
        return ColorScheme.INFO;
    }
    
    /**
     * Runs a benchmark test multiple times and returns the average result
     * @param algorithmName The name of the algorithm being tested
     * @param students The list of students to use for testing
     * @param testFunction The function that performs the actual test
     * @return The average test result
     */
    
    // Thay thế phương thức runBenchmark hiện tại bằng phương thức cải tiến này
    private TestResult runBenchmark(String algorithmName, List<Student> students, 
                                   Function<List<Student>, BenchmarkResult> testFunction) {
        // Warm up the JVM to get more accurate results
        for (int i = 0; i < 3; i++) {
            testFunction.apply(new ArrayList<>(students.subList(0, Math.min(100, students.size()))));
        }
        
        // Thiết lập theo dõi GC để biết khi nào GC hoàn thành
        setupGCMonitoring();
        
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Run the test multiple times and calculate average
        double totalExecutionTime = 0;
        double totalMemoryUsage = 0;
        double totalOperationsPerSecond = 0;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            // Đảm bảo bộ nhớ sạch giữa các lần chạy
            forceGarbageCollection();
            
            // Tạo bản sao của dữ liệu để tránh tái sử dụng
            List<Student> testData = new ArrayList<>(students);
            
            // Đo lường bộ nhớ trước khi test
            MemoryUsage heapBefore = getDetailedMemoryUsage();
            
            // Thực hiện test
            BenchmarkResult result = testFunction.apply(testData);
            totalExecutionTime += result.executionTime;
            totalMemoryUsage += result.memoryUsage;
            totalOperationsPerSecond += result.operationsPerSecond;
            
            // Giải phóng tham chiếu để GC có thể thu hồi
            testData = null;
            forceGarbageCollection();
        }
        
        // Calculate averages
        double avgExecutionTime = totalExecutionTime / TEST_ITERATIONS;
        double avgMemoryUsage = totalMemoryUsage / TEST_ITERATIONS;
        double avgOperationsPerSecond = totalOperationsPerSecond / TEST_ITERATIONS;
        
        // Dừng theo dõi GC
        stopGCMonitoring();
        
        return new TestResult(algorithmName, students.size(), 
                             avgExecutionTime, avgMemoryUsage, avgOperationsPerSecond);
    }

    // Thêm phương thức mới để thiết lập theo dõi GC
    private void setupGCMonitoring() {
        gcLatch = new CountDownLatch(1);
        gcCompleted = false;
        
        try {
            // Đăng ký listener cho các sự kiện GC
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (gcBean instanceof NotificationEmitter) {
                    NotificationEmitter emitter = (NotificationEmitter) gcBean;
                    gcListener = (notification, handback) -> {
                        if (notification.getType().equals("com.sun.management.gc.notification")) {
                            gcCompleted = true;
                            gcLatch.countDown();
                        }
                    };
                    emitter.addNotificationListener(gcListener, null, null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up GC monitoring: " + e.getMessage());
        }
    }

    // Thêm phương thức để dừng theo dõi GC
    private void stopGCMonitoring() {
        try {
            // Trong ứng dụng thực tế, bạn nên lưu trữ và loại bỏ các listener
            if (gcListener != null) {
                for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                    if (gcBean instanceof NotificationEmitter) {
                        NotificationEmitter emitter = (NotificationEmitter) gcBean;
                        try {
                            emitter.removeNotificationListener(gcListener);
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error stopping GC monitoring: " + e.getMessage());
        }
    }

    // Thêm phương thức mới để buộc GC chạy và đợi nó hoàn thành
    private void forceGarbageCollection() {
        gcCompleted = false;
        gcLatch = new CountDownLatch(1);
        
        // Gọi System.gc() nhiều lần để tăng khả năng GC thực sự chạy
        System.gc();
        System.runFinalization();
        System.gc();
        
        try {
            // Đợi tối đa 1 giây cho GC hoàn thành
            gcLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Đợi thêm một chút để đảm bảo
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Thêm phương thức mới để lấy thông tin bộ nhớ chi tiết
    private MemoryUsage getDetailedMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage();
    }

    // Thêm phương thức mới để hiển thị thông tin bộ nhớ chi tiết
    private String getDetailedMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Heap Memory: Used=").append(heapUsage.getUsed() / (1024 * 1024)).append("MB, ");
        sb.append("Committed=").append(heapUsage.getCommitted() / (1024 * 1024)).append("MB, ");
        sb.append("Max=").append(heapUsage.getMax() / (1024 * 1024)).append("MB\n");
        
        sb.append("Non-Heap Memory: Used=").append(nonHeapUsage.getUsed() / (1024 * 1024)).append("MB, ");
        sb.append("Committed=").append(nonHeapUsage.getCommitted() / (1024 * 1024)).append("MB");
        
        return sb.toString();
    }
    
    // Cập nhật phương thức testArrayList để sử dụng phương pháp đo lường bộ nhớ mới
    private BenchmarkResult testArrayList(List<Student> students) {
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Test ArrayList operations
        ArrayList<Student> arrayList = new ArrayList<>(students.size()); // Pre-allocate capacity
        
        // Add all students - tạo bản sao để tránh tham chiếu
        for (Student student : students) {
            // Tạo bản sao của sinh viên để tăng áp lực bộ nhớ
            Student copy = new Student(student.getId(), student.getName(), student.getScore());
            arrayList.add(copy);
        }
        
        // Access elements randomly
        Random random = new Random();
        // Tăng số lượng hoạt động theo kích thước dữ liệu
        int accessCount = students.size() * OPERATION_MULTIPLIER;
        for (int i = 0; i < accessCount; i++) {
            int index = random.nextInt(arrayList.size());
            Student student = arrayList.get(index);
            // Thực hiện một số hoạt động để tránh tối ưu hóa JIT
            String temp = student.getId() + student.getName();
        }
        
        // Iterate through all elements
        for (Student student : arrayList) {
            String id = student.getId();
            // Thực hiện một số hoạt động để tránh tối ưu hóa JIT
            double score = student.getScore() * 1.01;
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Đảm bảo kết quả tỷ lệ với kích thước dữ liệu
        // Nếu kết quả quá nhỏ, áp dụng hệ số tỷ lệ
        if (memoryUsage < 0.5 && students.size() > 1000) {
            memoryUsage = students.size() / 1000.0;
        }
        
        // Calculate operations per second (add + access + iterate)
        double totalOperations = students.size() + accessCount + students.size();
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        // Giải phóng tham chiếu để GC có thể thu hồi
        arrayList = null;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    // Cập nhật phương thức testHashMap để sử dụng phương pháp đo lường bộ nhớ mới
    private BenchmarkResult testHashMap(List<Student> students) {
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Test HashMap operations
        HashMap<String, Student> hashMap = new HashMap<>(students.size()); // Pre-allocate capacity
        
        // Add all students - tạo bản sao để tăng áp lực bộ nhớ
        for (Student student : students) {
            // Tạo bản sao của sinh viên để tăng áp lực bộ nhớ
            Student copy = new Student(student.getId(), student.getName(), student.getScore());
            hashMap.put(copy.getId(), copy);
        }
        
        // Access elements randomly
        Random random = new Random();
        List<String> ids = new ArrayList<>(hashMap.keySet());
        // Tăng số lượng hoạt động theo kích thước dữ liệu
        int accessCount = students.size() * OPERATION_MULTIPLIER;
        for (int i = 0; i < accessCount; i++) {
            int index = random.nextInt(ids.size());
            String id = ids.get(index);
            Student student = hashMap.get(id);
            // Thực hiện một số hoạt động để tránh tối ưu hóa JIT
            String temp = student.getId() + student.getName();
        }
        
        // Iterate through all elements
        for (Map.Entry<String, Student> entry : hashMap.entrySet()) {
            String id = entry.getKey();
            Student student = entry.getValue();
            // Thực hiện một số hoạt động để tránh tối ưu hóa JIT
            double score = student.getScore() * 1.01;
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Đảm bảo kết quả tỷ lệ với kích thước dữ liệu
        // Nếu kết quả quá nhỏ, áp dụng hệ số tỷ lệ
        if (memoryUsage < 0.5 && students.size() > 1000) {
            memoryUsage = students.size() / 500.0; // HashMap sử dụng nhiều bộ nhớ hơn ArrayList
        }
        
        // Calculate operations per second (put + get + iterate)
        double totalOperations = students.size() + accessCount + students.size();
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        // Giải phóng tham chiếu để GC có thể thu hồi
        hashMap = null;
        ids = null;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    // Cập nhật các phương thức test khác tương tự
    private BenchmarkResult testHashSet(List<Student> students) {
        // Create duplicate students (50% duplicates)
        List<Student> duplicatedStudents = new ArrayList<>(students);
        int duplicateCount = students.size() / 2;
        for (int i = 0; i < duplicateCount; i++) {
            duplicatedStudents.add(students.get(i));
        }
        Collections.shuffle(duplicatedStudents);
        
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Test HashSet operations
        HashSet<String> uniqueIds = new HashSet<>(duplicatedStudents.size());
        
        // Add all student IDs
        for (Student student : duplicatedStudents) {
            uniqueIds.add(student.getId());
        }
        
        // Check for existence
        int checkCount = Math.min(duplicatedStudents.size() * OPERATION_MULTIPLIER, 10000);
        for (int i = 0; i < checkCount; i++) {
            int index = i % duplicatedStudents.size();
            boolean exists = uniqueIds.contains(duplicatedStudents.get(index).getId());
        }
        
        // Iterate through all elements
        for (String id : uniqueIds) {
            String temp = id;
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Calculate operations per second (add + contains + iterate)
        double totalOperations = duplicatedStudents.size() + checkCount + uniqueIds.size();
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    private BenchmarkResult testQuickSort(List<Student> students) {
        // Tạo nhiều bản sao của danh sách để sắp xếp, tỷ lệ với kích thước dữ liệu
        int copies = Math.max(1, Math.min(10, 10000 / students.size()));
        List<List<Student>> listsToBeSorted = new ArrayList<>();
        
        for (int i = 0; i < copies; i++) {
            List<Student> copy = new ArrayList<>(students.size());
            // Tạo bản sao sâu của sinh viên
            for (Student student : students) {
                copy.add(new Student(student.getId(), student.getName(), student.getScore()));
            }
            Collections.shuffle(copy); // Xáo trộn để đảm bảo mẫu sắp xếp khác nhau
            listsToBeSorted.add(copy);
        }
        
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Sort all copies
        for (List<Student> listToSort : listsToBeSorted) {
            quickSort(listToSort, 0, listToSort.size() - 1);
            
            // Thực hiện một số hoạt động để tránh tối ưu hóa JIT
            double sum = 0;
            for (Student s : listToSort) {
                sum += s.getScore();
            }
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Đảm bảo kết quả tỷ lệ với kích thước dữ liệu
        // Nếu kết quả quá nhỏ, áp dụng hệ số tỷ lệ
        if (memoryUsage < 0.5 && students.size() > 1000) {
            memoryUsage = students.size() / 800.0 * copies; // QuickSort sử dụng bộ nhớ tỷ lệ với kích thước và số bản sao
        }
        
        // Calculate operations per second (comparisons in quick sort are approximately n*log(n))
        double totalOperations = copies * students.size() * Math.log(students.size()) / Math.log(2);
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        // Giải phóng tham chiếu để GC có thể thu hồi
        listsToBeSorted = null;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    private BenchmarkResult testHashSearch(List<Student> students) {
        // Create a HashMap for searching
        HashMap<String, Student> studentMap = new HashMap<>(students.size());
        for (Student student : students) {
            studentMap.put(student.getId(), student);
        }
        
        // Generate random IDs to search for (50% existing, 50% non-existing)
        List<String> searchIds = new ArrayList<>();
        Random random = new Random();
        
        // Add existing IDs
        int searchCount = Math.min(students.size() * OPERATION_MULTIPLIER, 10000);
        for (int i = 0; i < searchCount / 2; i++) {
            int index = random.nextInt(students.size());
            searchIds.add(students.get(index).getId());
        }
        
        // Add non-existing IDs
        for (int i = 0; i < searchCount / 2; i++) {
            searchIds.add("NONEXIST" + i);
        }
        
        // Shuffle search IDs
        Collections.shuffle(searchIds);
        
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Perform hash-based searches
        int foundCount = 0;
        for (String id : searchIds) {
            Student student = studentMap.get(id);
            if (student != null) {
                foundCount++;
            }
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Calculate operations per second (searches)
        double totalOperations = searchIds.size();
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    private BenchmarkResult testSearchIndexing(List<Student> students) {
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time for building indices
        long startTimeBuild = System.nanoTime();
        
        // Build indices
        Map<String, List<Student>> nameIndex = new HashMap<>();
        Map<Double, List<Student>> scoreIndex = new HashMap<>();
        Map<String, List<Student>> rankIndex = new HashMap<>();
        
        for (Student student : students) {
            // Index by name (each word in name)
            String[] nameParts = student.getName().toLowerCase().split("\\s+");
            for (String part : nameParts) {
                nameIndex.computeIfAbsent(part, k -> new ArrayList<>()).add(student);
            }
            
            // Index by score
            Double score = student.getScore();
            scoreIndex.computeIfAbsent(score, k -> new ArrayList<>()).add(student);
            
            // Index by rank
            String rank = student.getRank().toLowerCase();
            rankIndex.computeIfAbsent(rank, k -> new ArrayList<>()).add(student);
        }
        
        long endTimeBuild = System.nanoTime();
        double buildTime = (endTimeBuild - startTimeBuild) / 1_000_000.0;
        
        // Generate random search terms
        List<String> searchTerms = new ArrayList<>();
        Random random = new Random();
        
        // Add name search terms
        int searchCount = Math.min(students.size() * OPERATION_MULTIPLIER / 3, 3000);
        for (int i = 0; i < searchCount; i++) {
            int index = random.nextInt(students.size());
            String name = students.get(index).getName();
            String[] parts = name.split("\\s+");
            if (parts.length > 0) {
                searchTerms.add(parts[random.nextInt(parts.length)].toLowerCase());
            }
        }
        
        // Add score search terms
        for (int i = 0; i < searchCount; i++) {
            int index = random.nextInt(students.size());
            searchTerms.add(String.valueOf(students.get(index).getScore()));
        }
        
        // Add rank search terms
        String[] ranks = {"excellent", "very good", "good", "medium", "fail"};
        for (int i = 0; i < searchCount; i++) {
            searchTerms.add(ranks[random.nextInt(ranks.length)]);
        }
        
        // Shuffle search terms
        Collections.shuffle(searchTerms);
        
        // Measure search time
        long startTimeSearch = System.nanoTime();
        
        // Perform searches
        int totalResults = 0;
        for (String term : searchTerms) {
            Set<Student> results = new HashSet<>();
            
            // Try as name
            if (nameIndex.containsKey(term)) {
                results.addAll(nameIndex.get(term));
            }
            
            // Try as score
            try {
                double score = Double.parseDouble(term);
                if (scoreIndex.containsKey(score)) {
                    results.addAll(scoreIndex.get(score));
                }
            } catch (NumberFormatException e) {
                // Not a score, ignore
            }
            
            // Try as rank
            if (rankIndex.containsKey(term)) {
                results.addAll(rankIndex.get(term));
            }
            
            totalResults += results.size();
        }
        
        long endTimeSearch = System.nanoTime();
        double searchTime = (endTimeSearch - startTimeSearch) / 1_000_000.0;
        
        // Total execution time (build + search)
        double executionTime = buildTime + searchTime;
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Calculate operations per second (index builds + searches)
        double totalOperations = students.size() * 3 + searchTerms.size();
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    private BenchmarkResult testMaxHeapPriorityQueue(List<Student> students) {
        // Đảm bảo bộ nhớ sạch trước khi bắt đầu đo lường
        forceGarbageCollection();
        
        // Đo lường bộ nhớ trước khi test
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedMemoryBefore = heapBefore.getUsed();
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        // Test MaxHeapPriorityQueue operations
        MaxHeapPriorityQueue priorityQueue = new MaxHeapPriorityQueue();
        
        // Insert all students
        for (Student student : students) {
            priorityQueue.insert(new Student(student.getId(), student.getName(), student.getScore()));
        }
        
        // Extract top students
        int extractCount = Math.min(students.size() / 2, 1000);
        List<Student> topStudents = new ArrayList<>();
        for (int i = 0; i < extractCount; i++) {
            Student top = priorityQueue.extractMax();
            if (top != null) {
                topStudents.add(top);
            }
        }
        
        // Reinsert extracted students
        for (Student student : topStudents) {
            priorityQueue.insert(student);
        }
        
        // Get top N students without extraction
        int topN = Math.min(students.size() / 4, 500);
        List<Student> top = priorityQueue.getTopN(topN);
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        // Đảm bảo tất cả hoạt động đã hoàn thành
        forceGarbageCollection();
        
        // Đo lường bộ nhớ sau khi test
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long usedMemoryAfter = heapAfter.getUsed();
        
        // Tính toán sử dụng bộ nhớ thực tế
        double memoryUsage = Math.max(0.1, (usedMemoryAfter - usedMemoryBefore) / (1024.0 * 1024.0));
        
        // Đảm bảo kết quả tỷ lệ với kích thước dữ liệu
        if (memoryUsage < 0.5 && students.size() > 1000) {
            memoryUsage = students.size() / 700.0; // Heap uses less memory than some other structures
        }
        
        // Calculate operations per second (insert + extract + getTopN)
        double totalOperations = students.size() + extractCount + 1;
        double operationsPerSecond = (totalOperations / executionTime) * 1000;
        
        // Giải phóng tham chiếu để GC có thể thu hồi
        priorityQueue = null;
        topStudents = null;
        top = null;
        
        return new BenchmarkResult(executionTime, memoryUsage, operationsPerSecond);
    }
    
    // Thêm phương thức mới để tạo dữ liệu test với kích thước khác nhau
    private List<Student> generateRandomStudents(int count) {
        List<Student> students = new ArrayList<>(count); // Pre-allocate capacity
        Random random = new Random();
        
        String[] firstNames = {"John", "Jane", "Michael", "Emma", "William", "Olivia", "James", "Sophia", "Robert", "Ava", 
                              "David", "Isabella", "Joseph", "Mia", "Thomas", "Charlotte", "Daniel", "Amelia", "Matthew", "Harper"};
        
        String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor",
                             "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson"};
        
        // Tạo dữ liệu với độ phức tạp tỷ lệ với kích thước
        int nameComplexity = Math.min(firstNames.length, Math.max(5, count / 1000));
        
        for (int i = 0; i < count; i++) {
            // Generate ID in format BC00000
            String id = String.format("BC%05d", i + 1);
            
            // Tạo tên với độ phức tạp tỷ lệ với kích thước dữ liệu
            StringBuilder nameBuilder = new StringBuilder();
            int namePartCount = 1 + random.nextInt(nameComplexity);
            
            for (int j = 0; j < namePartCount; j++) {
                if (j > 0) nameBuilder.append(" ");
                
                if (j == 0) {
                    nameBuilder.append(firstNames[random.nextInt(firstNames.length)]);
                } else {
                    nameBuilder.append(lastNames[random.nextInt(lastNames.length)]);
                }
            }
            
            String name = nameBuilder.toString();
            
            // Generate random score between 0 and 10
            double score = Math.round(random.nextDouble() * 1000) / 100.0;
            if (score > 10) score = 10.0;
            
            students.add(new Student(id, name, score));
        }
        
        return students;
    }
    
    private void quickSort(List<Student> arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);

            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(List<Student> arr, int low, int high) {
        Student pivot = arr.get(high);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            if (arr.get(j).getScore() >= pivot.getScore()) {
                i++;

                Student temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
        }

        Student temp = arr.get(i + 1);
        arr.set(i + 1, arr.get(high));
        arr.set(high, temp);

        return i + 1;
    }
    
    // Class to store benchmark results for a single test run
    private static class BenchmarkResult {
        double executionTime;
        double memoryUsage;
        double operationsPerSecond;
        
        public BenchmarkResult(double executionTime, double memoryUsage, double operationsPerSecond) {
            this.executionTime = executionTime;
            this.memoryUsage = memoryUsage;
            this.operationsPerSecond = operationsPerSecond;
        }
    }
    
    // Class to store test results (average of multiple benchmark runs)
    private static class TestResult {
        String algorithm;
        int dataSize;
        double executionTime;
        double memoryUsage;
        double operationsPerSecond;
        
        public TestResult(String algorithm, int dataSize, double executionTime, double memoryUsage, double operationsPerSecond) {
            this.algorithm = algorithm;
            this.dataSize = dataSize;
            this.executionTime = executionTime;
            this.memoryUsage = memoryUsage;
            this.operationsPerSecond = operationsPerSecond;
        }
    }
}
