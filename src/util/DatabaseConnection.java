package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER = "root";
    private static final String PASSWORD = "0397571231Aa"; // Change password if needed
    
    private static Connection connection;
    private static boolean useInMemoryStorage = false;
    
    public static Connection getConnection() throws SQLException {
        if (useInMemoryStorage) {
            System.out.println("Using in-memory storage - no DB connection");
            throw new SQLException("Using in-memory storage");
        }
        
        if (connection == null || connection.isClosed()) {
            try {
                // Register MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("MySQL JDBC Driver has been registered");
                
                // Create connection
                System.out.println("Connecting to: " + URL);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection successful!");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                useInMemoryStorage = true;
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                useInMemoryStorage = true;
                throw e;
            }
        } else {
            // Test if connection is still valid
            try {
                if (!connection.isValid(5)) { // 5 second timeout
                    System.out.println("Connection is no longer valid, recreating...");
                    connection.close();
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Connection successfully recreated");
                }
            } catch (SQLException e) {
                System.err.println("Error checking connection: " + e.getMessage());
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public static void initializeDatabase() {
        try {
            Connection conn = getConnection();
            System.out.println("Connected to database for initialization");
            
            // Check if students table exists
            boolean tableExists = false;
            try {
                conn.createStatement().executeQuery("SELECT 1 FROM students LIMIT 1");
                tableExists = true;
                System.out.println("Students table already exists");
            } catch (SQLException e) {
                // Table doesn't exist
                System.out.println("Students table does not exist: " + e.getMessage());
            }
            
            if (!tableExists) {
                // Create students table if it doesn't exist
                String createTableSQL = "CREATE TABLE students (" +
                                        "id VARCHAR(10) PRIMARY KEY, " +
                                        "name VARCHAR(100) NOT NULL, " +
                                        "score DOUBLE NOT NULL)";
                
                conn.createStatement().executeUpdate(createTableSQL);
                System.out.println("Students table created successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            useInMemoryStorage = true;
            System.out.println("Switching to in-memory storage.");
        }
    }
    
    public static boolean isUsingInMemoryStorage() {
        return useInMemoryStorage;
    }
    
    public static void setUseInMemoryStorage(boolean value) {
        useInMemoryStorage = value;
    }
}

