package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Student;
import model.StudentManager;
import util.*;

public class StudentPanel extends JPanel {
    private final StudentManager studentManager;
    private RoundedTextField idField, nameField, scoreField, searchField;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private GlassPanel formPanel, tablePanel, actionPanel, searchPanel;
    private JLabel statusLabel;

    public StudentPanel() {
        studentManager = new StudentManager();
        setLayout(new BorderLayout(20, 20));
        setBackground(ColorScheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        addSampleData();
    }

    private void initComponents() {
        // Form Panel
        formPanel = new GlassPanel(new GridBagLayout());
        formPanel.setRadius(15);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Title
        JLabel titleLabel = new JLabel("Student Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(ColorScheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(titleLabel, gbc);
        
        // ID Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel idLabel = new JLabel("Student ID");
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idLabel.setForeground(ColorScheme.TEXT);
        formPanel.add(idLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        idField = new RoundedTextField(20);
        formPanel.add(idField, gbc);
        
        // Name Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(ColorScheme.TEXT);
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        nameField = new RoundedTextField(20);
        formPanel.add(nameField, gbc);
        
        // Score Field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel scoreLabel = new JLabel("Score (0-10)");
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLabel.setForeground(ColorScheme.TEXT);
        formPanel.add(scoreLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        scoreField = new RoundedTextField(20);
        formPanel.add(scoreField, gbc);
        
        // Status Label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(ColorScheme.PRIMARY);
        formPanel.add(statusLabel, gbc);
        
        // Action Panel
        actionPanel = new GlassPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        CustomButton addButton = CustomButton.createPrimaryButton("Add Student");
        CustomButton updateButton = CustomButton.createSuccessButton("Update");
        CustomButton deleteButton = CustomButton.createDangerButton("Delete");
        CustomButton clearButton = CustomButton.createGlassButton("Clear");
        
        addButton.addActionListener(_ -> addStudent());
        updateButton.addActionListener(_ -> updateStudent());
        deleteButton.addActionListener(_ -> deleteStudent());
        clearButton.addActionListener(_ -> clearFields());
        
        actionPanel.add(addButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(clearButton);
        
        // Search Panel
        searchPanel = new GlassPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        
        JLabel searchLabel = new JLabel("Search by ID:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(ColorScheme.TEXT);
        
        searchField = new RoundedTextField(20);
        CustomButton searchButton = CustomButton.createPrimaryButton("Search");
        CustomButton refreshButton = CustomButton.createGlassButton("Show All");
        
        searchButton.addActionListener(_ -> searchStudent());
        refreshButton.addActionListener(_ -> refreshTable());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        // Table Panel
        tablePanel = new GlassPanel(new BorderLayout(0, 10));
        
        JLabel tableTitle = new JLabel("Student Rankings");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(ColorScheme.PRIMARY);
        tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Name", "Score", "Rank"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(35);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Custom table header
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ColorScheme.PRIMARY);
        header.setForeground(ColorScheme.LIGHT_TEXT);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        // Custom cell renderer for alternating row colors and rank styling
        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set alignment
                setHorizontalAlignment(column == 2 ? JLabel.CENTER : column == 3 ? JLabel.CENTER : JLabel.LEFT);
                
                // Set padding
                setBorder(new EmptyBorder(0, 10, 0, 10));
                
                // Lấy hàng đang hover
                Integer hoverRow = (Integer) table.getClientProperty("hoverRow");
                boolean isHover = hoverRow != null && hoverRow == row;
                
                // Set colors based on selection and hover
                if (isSelected) {
                    c.setBackground(ColorScheme.PRIMARY.brighter());
                    c.setForeground(ColorScheme.TEXT);
                } else if (isHover) {
                    // Hiệu ứng hover nhẹ nhàng hơn
                    c.setBackground(new Color(240, 240, 250));
                    c.setForeground(ColorScheme.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    c.setForeground(ColorScheme.TEXT);
                }
                
                // Style rank column
                if (column == 3 && value != null) {
                    String rank = value.toString();
                    setForeground(switch (rank) {
                        case "Excellent" -> ColorScheme.EXCELLENT_COLOR;
                        case "Very Good" -> ColorScheme.VERY_GOOD_COLOR;
                        case "Good" -> ColorScheme.GOOD_COLOR;
                        case "Medium" -> ColorScheme.MEDIUM_COLOR;
                        case "Fail" -> ColorScheme.FAIL_COLOR;
                        default -> ColorScheme.TEXT;
                    });
                }
                
                return c;
            }
        });
        
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                int row = studentTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                scoreField.setText(tableModel.getValueAt(row, 2).toString());
            }
        });
        
        // Cải thiện hiệu ứng hover cho bảng
        studentTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Không tự động chọn hàng khi di chuyển chuột
                // Chỉ lưu lại vị trí hàng để hiển thị hiệu ứng hover
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    // Lưu trữ hàng hover trong thuộc tính client
                    studentTable.putClientProperty("hoverRow", row);
                    studentTable.repaint();
                }
            }
        });

        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // Xóa hiệu ứng hover khi chuột rời khỏi bảng
                studentTable.putClientProperty("hoverRow", -1);
                studentTable.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Chỉ chọn hàng khi click
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    studentTable.setRowSelectionInterval(row, row);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add components to main panel
        JPanel leftPanel = new JPanel(new BorderLayout(0, 20));
        leftPanel.setOpaque(false);
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(actionPanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);
        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Create a split pane with a glass-like divider
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);
    }

    private void addStudent() {
        try {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            double score = Double.parseDouble(scoreField.getText().trim());

            if (id.isEmpty() || name.isEmpty()) {
                showStatus("ID and Name cannot be empty", false);
                return;
            }

            if (score < 0 || score > 10) {
                showStatus("Score must be between 0 and 10", false);
                return;
            }

            // Kiểm tra định dạng ID
            if (!id.matches("BC\\d{5}")) {
                showStatus("ID must be in format BC00000 (BC + 5 digits)", false);
                return;
            }

            Student student = new Student(id, name, score);
            boolean added = studentManager.addStudent(student);

            if (added) {
                refreshTable();
                clearFields();
                showStatus("Student added successfully", true);
            } else {
                showStatus("Student with ID " + id + " already exists or ID format is invalid", false);
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid score format", false);
        }
    }

    private void updateStudent() {
        try {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            double score = Double.parseDouble(scoreField.getText().trim());
            
            if (id.isEmpty() || name.isEmpty()) {
                showStatus("ID and Name cannot be empty", false);
                return;
            }
            
            if (score < 0 || score > 10) {
                showStatus("Score must be between 0 and 10", false);
                return;
            }
            
            Student student = new Student(id, name, score);
            boolean updated = studentManager.updateStudent(student);
            
            if (updated) {
                refreshTable();
                clearFields();
                showStatus("Student updated successfully", true);
            } else {
                showStatus("Student with ID " + id + " not found", false);
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid score format", false);
        }
    }

    private void deleteStudent() {
        String id = idField.getText().trim();
        
        if (id.isEmpty()) {
            showStatus("Please select a student to delete", false);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete student with ID " + id + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = studentManager.deleteStudent(id);
            
            if (deleted) {
                refreshTable();
                clearFields();
                showStatus("Student deleted successfully", true);
            } else {
                showStatus("Student with ID " + id + " not found", false);
            }
        }
    }

    private void searchStudent() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            showStatus("Please enter an ID or score to search", false);
            refreshTable();
            return;
        }
        
        // Try to parse as score first
        try {
            double score = Double.parseDouble(searchText);
            if (score < 0 || score > 10) {
                showStatus("Score must be between 0 and 10", false);
                return;
            }
            
            Set<Student> foundStudents = studentManager.findStudentsByScore(score);
            
            if (!foundStudents.isEmpty()) {
                tableModel.setRowCount(0);
                java.util.List<Student> sortedResults = new ArrayList<>(foundStudents);
                sortedResults.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()));
                
                for (Student s : sortedResults) {
                    Object[] row = {s.getId(), s.getName(), s.getScore(), s.getRank()};
                    tableModel.addRow(row);
                }
                showStatus("Found " + foundStudents.size() + " student(s) with score " + score, true);
            } else {
                showStatus("No students found with score " + score, false);
                refreshTable();
            }
        } catch (NumberFormatException e) {
            // Try ID search first
            Student student = studentManager.findStudentById(searchText);
            
            if (student != null) {
                tableModel.setRowCount(0);
                Object[] row = {student.getId(), student.getName(), student.getScore(), student.getRank()};
                tableModel.addRow(row);
                
                // Select the found student
                idField.setText(student.getId());
                nameField.setText(student.getName());
                scoreField.setText(String.valueOf(student.getScore()));
                showStatus("Student found", true);
                return;
            }
            
            // Try name search
            Set<Student> nameResults = studentManager.findStudentsByName(searchText);
            if (!nameResults.isEmpty()) {
                tableModel.setRowCount(0);
                java.util.List<Student> sortedResults = new ArrayList<>(nameResults);
                sortedResults.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()));
                
                for (Student s : sortedResults) {
                    Object[] row = {s.getId(), s.getName(), s.getScore(), s.getRank()};
                    tableModel.addRow(row);
                }
                showStatus("Found " + nameResults.size() + " student(s) matching name: " + searchText, true);
            } else {
                showStatus("No students found", false);
                refreshTable();
            }
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        scoreField.setText("");
        studentTable.clearSelection();
        showStatus(" ", true);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        java.util.List<Student> sortedStudents = studentManager.getSortedStudentsByScore();
        
        for (Student s : sortedStudents) {
            Object[] row = {s.getId(), s.getName(), s.getScore(), s.getRank()};
            tableModel.addRow(row);
        }
    }
    
    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setForeground(isSuccess ? ColorScheme.SUCCESS : ColorScheme.DANGER);
        
        javax.swing.Timer timer = new javax.swing.Timer(5000, _ -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private void addSampleData() {
        studentManager.addStudent(new Student("S001", "John Doe", 8.5));
        studentManager.addStudent(new Student("S002", "Jane Smith", 9.2));
        studentManager.addStudent(new Student("S003", "Bob Johnson", 6.8));
        studentManager.addStudent(new Student("S004", "Alice Brown", 4.5));
        studentManager.addStudent(new Student("S005", "Charlie Davis", 7.3));
        studentManager.addStudent(new Student("S006", "Emma Wilson", 9.7));
        studentManager.addStudent(new Student("S007", "Michael Lee", 5.9));
        refreshTable();
    }
}

