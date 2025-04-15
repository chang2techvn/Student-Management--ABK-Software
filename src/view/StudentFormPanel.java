package view;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import model.Student;
import model.StudentManager;
import util.*;

public class StudentFormPanel extends JPanel {
    private final StudentManager studentManager;
    private final DashboardPanel dashboardPanel;
    private final RoundedTextField idField;
    private final RoundedTextField nameField;
    private final RoundedTextField scoreField; 
    private final RoundedTextField searchField;
    private final JTable studentTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private Timer autoUpdateTimer;
    private boolean isEditing = false;
    
    // Fields for drag-and-drop functionality
    private int dragStartRow = -1;
    private int dragEndRow = -1;
    private boolean isDragging = false;

    public StudentFormPanel(StudentManager studentManager, DashboardPanel dashboardPanel) {
        this.studentManager = studentManager;
        this.dashboardPanel = dashboardPanel;
        
        // Initialize all final fields
        String[] columns = {"", "ID", "Name", "Score", "Rank"};
        this.tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }
        };
        
        this.studentTable = new JTable(this.tableModel);
        this.idField = new RoundedTextField(20);
        this.nameField = new RoundedTextField(20);
        this.scoreField = new RoundedTextField(20);
        this.searchField = new RoundedTextField(20);
        this.statusLabel = new JLabel(" ");
        this.autoUpdateTimer = new Timer(500, _ -> {
            if (!isEditing) {
                updateFromModel();
            }
        });
        
        setLayout(new BorderLayout());
        setOpaque(false);
        
        initComponents();
        setupAutoUpdate();
        setupEditingListeners();
        refreshTable();
    }

    private void initComponents() {
        // Main panel with grid layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Left panel - Form
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, gbc);
        
        // Right panel - Table
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Student Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 15, 0);
        gbc.anchor = GridBagConstraints.WEST;
        
        // ID Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel idLabel = new JLabel("Student ID");
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idLabel.setForeground(ColorScheme.TEXT);
        fieldsPanel.add(idLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        idField.setPlaceholder("Enter student ID");
        fieldsPanel.add(idField, gbc);
        
        // Name Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(ColorScheme.TEXT);
        fieldsPanel.add(nameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        nameField.setPlaceholder("Enter student name");
        fieldsPanel.add(nameField, gbc);
        
        // Score Field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel scoreLabel = new JLabel("Score (0-10)");
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLabel.setForeground(ColorScheme.TEXT);
        fieldsPanel.add(scoreLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        scoreField.setPlaceholder("Enter score (0-10)");
        fieldsPanel.add(scoreField, gbc);
        
        // Status Label
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(ColorScheme.PRIMARY);
        fieldsPanel.add(statusLabel, gbc);
        
        // Search Field
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 0, 15, 0);
        
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        
        searchField.setPlaceholder("Search by keyword (ID, name, score, rank)");
        
        CustomButton searchButton = CustomButton.createPrimaryButton("Search");
        searchButton.addActionListener(_ -> searchStudent());
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        fieldsPanel.add(searchPanel, gbc);
        
        panel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        CustomButton addButton = CustomButton.createPrimaryButton("Add Student");
        CustomButton updateButton = CustomButton.createSecondaryButton("Update");
        CustomButton deleteButton = CustomButton.createDangerButton("Delete");
        CustomButton clearButton = CustomButton.createGlassButton("Clear");
        
        addButton.addActionListener(_ -> addStudent());
        updateButton.addActionListener(_ -> updateStudent());
        deleteButton.addActionListener(_ -> deleteStudent());
        clearButton.addActionListener(_ -> clearFields());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel with title and sort options
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Student Rankings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Sort options
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        sortPanel.setOpaque(false);
        
        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortLabel.setForeground(ColorScheme.TEXT);
        
        String[] sortOptions = {"Score (High to Low)", "Score (Low to High)", "Name (A-Z)", "Name (Z-A)", "ID"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortComboBox.setBackground(ColorScheme.CARD_BG_ACCENT);
        sortComboBox.setForeground(ColorScheme.TEXT);
        sortComboBox.setPreferredSize(new Dimension(180, 30));
        
        sortComboBox.addActionListener(_ -> {
            String selected = (String) sortComboBox.getSelectedItem();
            sortStudents(selected);
        });
        
        sortPanel.add(sortLabel);
        sortPanel.add(sortComboBox);
        
        headerPanel.add(sortPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create table
        studentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        studentTable.setRowHeight(40);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setBackground(ColorScheme.CARD_BG);
        studentTable.setForeground(ColorScheme.TEXT);

        // Set checkbox column width
        TableColumn selectColumn = studentTable.getColumnModel().getColumn(0);
        selectColumn.setPreferredWidth(30);
        selectColumn.setMaxWidth(30);
        selectColumn.setMinWidth(30);
        
        // Set width for other columns
        TableColumn idColumn = studentTable.getColumnModel().getColumn(1);
        idColumn.setPreferredWidth(100);
        
        TableColumn scoreColumn = studentTable.getColumnModel().getColumn(3);
        scoreColumn.setPreferredWidth(80);
        scoreColumn.setMaxWidth(100);
        
        TableColumn rankColumn = studentTable.getColumnModel().getColumn(4);
        rankColumn.setPreferredWidth(100);
        rankColumn.setMaxWidth(120);

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
                
                // Skip checkbox column
                if (column == 0) {
                    return c;
                }
                
                // Set alignment
                setHorizontalAlignment(column == 3 ? JLabel.CENTER : column == 4 ? JLabel.CENTER : JLabel.LEFT);
                
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
                
                // Style rank column
                if (column == 4 && value != null) {
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
        
        // Add selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                int row = studentTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 1).toString());
                nameField.setText(tableModel.getValueAt(row, 2).toString());
                scoreField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        // Remove hover effect if exists (find and remove MouseMotionListener code if present)
        // Add new MouseListener to handle hover correctly
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // When mouse leaves table, keep selected row
                if (studentTable.getSelectedRow() != -1) {
                    studentTable.repaint();
                }
            }
            
            // Update mousePressed method to handle checkbox clicks better
            @Override
            public void mousePressed(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    // Start dragging from this row
                    dragStartRow = row;
                    isDragging = true;
                    
                    // If Shift is not pressed, only select the current row
                    if (!e.isShiftDown()) {
                        studentTable.setRowSelectionInterval(row, row);
                        
                        // If clicking on checkbox column, toggle its state
                        if (studentTable.columnAtPoint(e.getPoint()) == 0) {
                            boolean currentValue = (Boolean) tableModel.getValueAt(row, 0);
                            tableModel.setValueAt(!currentValue, row, 0);
                        } else {
                            // If clicking on row (not checkbox), also select checkbox
                            tableModel.setValueAt(true, row, 0);
                        }
                    }
                }
            }
            
            // Update mouseReleased method to ensure all checkboxes are marked
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    // End dragging, update checkboxes
                    if (dragStartRow >= 0 && dragEndRow >= 0) {
                        int startRow = Math.min(dragStartRow, dragEndRow);
                        int endRow = Math.max(dragStartRow, dragEndRow);
                        
                        for (int i = startRow; i <= endRow; i++) {
                            tableModel.setValueAt(true, i, 0);
                        }
                    }
                }
                
                // Reset dragging state
                isDragging = false;
                dragStartRow = -1;
                dragEndRow = -1;
            }
        });

        studentTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Do not automatically select row when moving mouse
                // Only highlight row without selecting
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0 && studentTable.getSelectedRow() != row) {
                    // Only repaint to show hover effect without selecting row
                    studentTable.repaint();
                }
            }
            
            // Update mouseDragged method to mark checkboxes while dragging
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    int row = studentTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        dragEndRow = row;
                        
                        // If Shift is pressed, select all rows from dragStartRow to dragEndRow
                        if (e.isShiftDown()) {
                            int startRow = Math.min(dragStartRow, dragEndRow);
                            int endRow = Math.max(dragStartRow, dragEndRow);
                            
                            // Select rows in range
                            studentTable.setRowSelectionInterval(startRow, endRow);
                            
                            // Mark checkboxes for all rows in range
                            for (int i = startRow; i <= endRow; i++) {
                                tableModel.setValueAt(true, i, 0);
                            }
                        }
                        
                        studentTable.repaint();
                    }
                }
            }
        });
        
        // Create a custom scroll pane with proper sizing
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ColorScheme.CARD_BG);
        scrollPane.setOpaque(false);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        CustomButton selectAllButton = CustomButton.createGlassButton("Select All");
        selectAllButton.addActionListener(_ -> selectAllStudents());

        CustomButton deselectAllButton = CustomButton.createGlassButton("Deselect All");
        deselectAllButton.addActionListener(_ -> deselectAllStudents());

        CustomButton deleteSelectedButton = CustomButton.createDangerButton("Delete Selected");
        deleteSelectedButton.addActionListener(_ -> deleteSelectedStudents());

        CustomButton refreshButton = CustomButton.createGlassButton("Refresh");
        refreshButton.addActionListener(_ -> refreshTable());

        JLabel hintLabel = new JLabel("Tip: Hold Shift and drag to select multiple rows");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(ColorScheme.TEXT_SECONDARY);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(hintLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(selectAllButton);
        rightPanel.add(deselectAllButton);
        rightPanel.add(deleteSelectedButton);
        rightPanel.add(refreshButton);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        refreshTable();
        
        return panel;
    }
    
    private void setupAutoUpdate() {
        autoUpdateTimer.start();
    }

    private void setupEditingListeners() {
        // Add focus listeners to all text fields
        idField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isEditing = true;
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (!nameField.hasFocus() && !scoreField.hasFocus() && !searchField.hasFocus()) {
                    isEditing = false;
                }
            }
        });

        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isEditing = true;
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (!idField.hasFocus() && !scoreField.hasFocus() && !searchField.hasFocus()) {
                    isEditing = false;
                }
            }
        });

        scoreField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isEditing = true;
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (!idField.hasFocus() && !nameField.hasFocus() && !searchField.hasFocus()) {
                    isEditing = false;
                }
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isEditing = true;
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (!idField.hasFocus() && !nameField.hasFocus() && !scoreField.hasFocus()) {
                    isEditing = false;
                }
            }
        });

        // Add action listeners with ActionEvent parameter
        CustomButton searchButton = CustomButton.createPrimaryButton("Search");
        searchButton.addActionListener(_ -> searchStudent());
        CustomButton addButton = CustomButton.createPrimaryButton("Add Student");
        addButton.addActionListener(_ -> addStudent());
        CustomButton updateButton = CustomButton.createSecondaryButton("Update");
        updateButton.addActionListener(_ -> updateStudent());
        CustomButton deleteButton = CustomButton.createDangerButton("Delete");
        deleteButton.addActionListener(_ -> deleteStudent());
        CustomButton clearButton = CustomButton.createGlassButton("Clear");
        clearButton.addActionListener(_ -> clearFields());
        CustomButton selectAllButton = CustomButton.createGlassButton("Select All");
        selectAllButton.addActionListener(_ -> selectAllStudents());
        CustomButton deselectAllButton = CustomButton.createGlassButton("Deselect All");
        deselectAllButton.addActionListener(_ -> deselectAllStudents());
        CustomButton deleteSelectedButton = CustomButton.createDangerButton("Delete Selected");
        deleteSelectedButton.addActionListener(_ -> deleteSelectedStudents());
        CustomButton refreshButton = CustomButton.createGlassButton("Refresh");
        refreshButton.addActionListener(_ -> refreshTable());

        JComboBox<String> sortComboBox = new JComboBox<>();
        sortComboBox.addActionListener(_ -> {
            String selected = (String) sortComboBox.getSelectedItem();
            sortStudents(selected);
        });

        this.autoUpdateTimer = new Timer(500, _ -> {
            if (!isEditing) {
                updateFromModel();
            }
        });

        // Add selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1 && !isEditing) {
                int row = studentTable.getSelectedRow();
                isEditing = true;
                idField.setText(tableModel.getValueAt(row, 1).toString());
                nameField.setText(tableModel.getValueAt(row, 2).toString());
                scoreField.setText(tableModel.getValueAt(row, 3).toString());
                isEditing = false;
            }
        });
    }

    private void updateFromModel() {
        if (studentManager != null) {
            List<Student> currentStudents = studentManager.getAllStudents();
            
            // Compare current table data with model data
            boolean needsUpdate = false;
            if (tableModel.getRowCount() != currentStudents.size()) {
                needsUpdate = true;
            } else {
                for (int i = 0; i < currentStudents.size(); i++) {
                    Student student = currentStudents.get(i);
                    if (tableModel.getValueAt(i, 1) == null || 
                        !student.getId().equals(tableModel.getValueAt(i, 1).toString())) {
                        needsUpdate = true;
                        break;
                    }
                }
            }
            
            // Only update if there are changes
            if (needsUpdate) {
                SwingUtilities.invokeLater(() -> {
                    // Store selection and scroll state
                    int[] selectedRows = studentTable.getSelectedRows();
                    Rectangle visibleRect = studentTable.getVisibleRect();
                    
                    refreshTable();
                    
                    // Restore selection and scroll state
                    for (int row : selectedRows) {
                        if (row < tableModel.getRowCount()) {
                            studentTable.addRowSelectionInterval(row, row);
                        }
                    }
                    studentTable.scrollRectToVisible(visibleRect);
                });
            }
        }
    }

    private void sortStudents(String sortOption) {
        List<Student> sortedStudents = switch (sortOption) {
            case "Score (High to Low)" -> studentManager.getSortedStudentsByScore();
            case "Score (Low to High)" -> studentManager.getSortedStudentsByScoreAscending();
            case "Name (A-Z)" -> studentManager.getSortedStudentsByName(true);
            case "Name (Z-A)" -> studentManager.getSortedStudentsByName(false);
            case "ID" -> studentManager.getSortedStudentsById();
            default -> studentManager.getSortedStudentsByScore();
        };
        
        updateTableWithStudents(sortedStudents);
    }
    
    private void updateTableWithStudents(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student student : students) {
            Object[] row = {false, student.getId(), student.getName(), student.getScore(), student.getRank()};
            tableModel.addRow(row);
        }
        studentTable.repaint();
    }
    
    private void addStudent() {
        isEditing = true;
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

            // Check ID format
            if (!id.matches("BC\\d{5}")) {
                showStatus("ID must be in format BC00000 (BC + 5 digits)", false);
                return;
            }

            Student student = new Student(id, name, score);
            boolean added = studentManager.addStudent(student);

            if (added) {
                clearFields();
                refreshTable();
                showStatus("Student added successfully", true);
                dashboardPanel.updateDashboard();
            } else {
                showStatus("Student with ID " + id + " already exists", false);
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid score format", false);
        } finally {
            isEditing = false;
        }
    }
    
    private void updateStudent() {
        isEditing = true;
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
                clearFields();
                refreshTable();
                showStatus("Student updated successfully", true);
                dashboardPanel.updateDashboard();
            } else {
                showStatus("Student with ID " + id + " not found", false);
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid score format", false);
        } finally {
            isEditing = false;
        }
    }
    
    private void deleteStudent() {
        isEditing = true;
        try {
            String id = idField.getText().trim();
            
            if (id.isEmpty()) {
                showStatus("Please enter a student ID to delete", false);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete student with ID " + id + "?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = studentManager.deleteStudent(id);
                
                if (deleted) {
                    clearFields();
                    refreshTable();
                    showStatus("Student deleted successfully", true);
                    dashboardPanel.updateDashboard();
                } else {
                    showStatus("Student with ID " + id + " not found", false);
                }
            }
        } finally {
            isEditing = false;
        }
    }
    
    private void searchStudent() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }
        
        // Try to parse as score first
        try {
            double score = Double.parseDouble(keyword);
            if (score >= 0 && score <= 10) {
                Set<Student> foundStudents = studentManager.findStudentsByScore(score);
                updateTableWithStudents(new ArrayList<>(foundStudents));
                showStatus("Found " + foundStudents.size() + " student(s) with score " + score, true);
                return;
            }
        } catch (NumberFormatException e) {
            // Not a number, continue with name/ID search
        }

        // Try exact ID match
        Student student = studentManager.findStudentById(keyword);
        if (student != null) {
            List<Student> result = new ArrayList<>();
            result.add(student);
            updateTableWithStudents(result);
            showStatus("Found student with ID: " + keyword, true);
            return;
        }

        // Search by name
        Set<Student> nameResults = studentManager.findStudentsByName(keyword);
        if (!nameResults.isEmpty()) {
            List<Student> sortedResults = new ArrayList<>(nameResults);
            sortedResults.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()));
            updateTableWithStudents(sortedResults);
            showStatus("Found " + nameResults.size() + " student(s) matching: " + keyword, true);
        } else {
            showStatus("No students found matching: " + keyword, false);
            refreshTable();
        }
    }
    
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        scoreField.setText("");
        searchField.setText("");
        studentTable.clearSelection();
        statusLabel.setText(" ");
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Student> sortedStudents = studentManager.getSortedStudentsByScore();
        updateTableWithStudents(sortedStudents);
    }
    
    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setForeground(isSuccess ? ColorScheme.SUCCESS : ColorScheme.DANGER);
        
        Timer timer = new Timer(5000, _ -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private void deleteSelectedStudents() {
        int totalRows = tableModel.getRowCount();
        List<String> idsToDelete = new ArrayList<>();

        // Collect all selected student IDs
        for (int i = 0; i < totalRows; i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                String id = (String) tableModel.getValueAt(i, 1);
                idsToDelete.add(id);
            }
        }

        if (idsToDelete.isEmpty()) {
            showStatus("No students selected for deletion", false);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + idsToDelete.size() + " selected student(s)?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int successCount = 0;

            for (String id : idsToDelete) {
                boolean deleted = studentManager.deleteStudent(id);
                if (deleted) {
                    successCount++;
                }
            }

            refreshTable();
            clearFields();

            if (successCount > 0) {
                showStatus(successCount + " student(s) deleted successfully", true);
                dashboardPanel.updateDashboard();
            } else {
                showStatus("Failed to delete students", false);
            }
        }
    }

    private void selectAllStudents() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(true, i, 0);
        }
        studentTable.repaint();
    }

    private void deselectAllStudents() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, 0);
        }
        studentTable.repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (autoUpdateTimer != null) {
            autoUpdateTimer.stop();
        }
    }
}

