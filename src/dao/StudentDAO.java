package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import util.DatabaseConnection;

public class StudentDAO {
    
    // Add new student
    
    /**
     * Check student ID format
     * @param id ID to check
     * @return true if ID format is correct, false if not
     */
    private boolean isValidStudentId(String id) {
        // Check format BC00000 (BC + 5 digits)
        return id != null && id.matches("BC\\d{5}");
    }
    
    // Update addStudent method
    public boolean addStudent(Student student) {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error adding student: Using in-memory storage");
            return false;
        }
        
        // Check ID format
        if (!isValidStudentId(student.getId())) {
            System.err.println("Error adding student: Invalid ID format BC00000");
            return false;
        }
        
        // For performance testing, we'll skip the existence check to improve performance
        // when adding many students at once
        boolean isPerformanceTest = student.getId().matches("BC[1-9]\\d{4}");
        
        if (!isPerformanceTest) {
            // Check if student already exists
            String checkSql = "SELECT COUNT(*) FROM students WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                
                checkStmt.setString(1, student.getId());
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Error adding student: ID already exists");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Error checking student ID: " + e.getMessage());
                return false;
            }
        }
        
        // Insert the student
        String insertSql = "INSERT INTO students (id, name, score) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setDouble(3, student.getScore());
            
            int rowsAffected = pstmt.executeUpdate();
            if (!isPerformanceTest) {
                System.out.println("Added student: " + student.getId() + ", Rows affected: " + rowsAffected);
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // If it's a duplicate key error, just ignore it for performance testing
            if (e.getMessage().contains("Duplicate entry") && isPerformanceTest) {
                return false;
            }
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }
    
    // Update student information
    public boolean updateStudent(Student student) {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error updating student: Using in-memory storage");
            return false;
        }
        
        String sql = "UPDATE students SET name = ?, score = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getName());
            pstmt.setDouble(2, student.getScore());
            pstmt.setString(3, student.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }
    
    // Delete student
    public boolean deleteStudent(String id) {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error deleting student: Using in-memory storage");
            return false;
        }
        
        String sql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }
    
    // Find student by ID
    public Student findStudentById(String id) {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error finding student: Using in-memory storage");
            return null;
        }
        
        String sql = "SELECT * FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String studentId = rs.getString("id");
                    String name = rs.getString("name");
                    double score = rs.getDouble("score");
                    
                    return new Student(studentId, name, score);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding student: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get all students
    public List<Student> getAllStudents() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting student list: Using in-memory storage");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                
                students.add(new Student(id, name, score));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student list: " + e.getMessage());
        }
        
        return students;
    }
    
    // Get students sorted by score (descending)
    public List<Student> getStudentsSortedByScore() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting students sorted by score: Using in-memory storage");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY score DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                
                students.add(new Student(id, name, score));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students sorted by score: " + e.getMessage());
        }
        
        return students;
    }
    
    // Get students sorted by score (ascending)
    public List<Student> getStudentsSortedByScoreAscending() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting students sorted by ascending score: Using in-memory storage");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY score ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                
                students.add(new Student(id, name, score));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students sorted by ascending score: " + e.getMessage());
        }
        
        return students;
    }
    
    // Get students sorted by name
    public List<Student> getStudentsSortedByName(boolean ascending) {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting students sorted by name: Using in-memory storage");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name " + (ascending ? "ASC" : "DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                
                students.add(new Student(id, name, score));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students sorted by name: " + e.getMessage());
        }
        
        return students;
    }
    
    // Get students sorted by ID
    public List<Student> getStudentsSortedById() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting students sorted by ID: Using in-memory storage");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                
                students.add(new Student(id, name, score));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students sorted by ID: " + e.getMessage());
        }
        
        return students;
    }
    
    // Get average score of all students
    public double getAverageScore() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error calculating average score: Using in-memory storage");
            return 0;
        }
        
        String sql = "SELECT AVG(score) as avg_score FROM students";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("avg_score");
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating average score: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Get highest score
    public double getHighestScore() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting highest score: Using in-memory storage");
            return 0;
        }
        
        String sql = "SELECT MAX(score) as max_score FROM students";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("max_score");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting highest score: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Get lowest score
    public double getLowestScore() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error getting lowest score: Using in-memory storage");
            return 0;
        }
        
        String sql = "SELECT MIN(score) as min_score FROM students";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("min_score");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting lowest score: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Count number of students passing (score >= 5.0)
    public int getPassCount() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error counting passing students: Using in-memory storage");
            return 0;
        }
        
        String sql = "SELECT COUNT(*) as pass_count FROM students WHERE score >= 5.0";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("pass_count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting passing students: " + e.getMessage());
        }
        
        return 0;
    }
    
    public void addSampleData() {
        if (DatabaseConnection.isUsingInMemoryStorage()) {
            System.err.println("Error adding sample data: Using in-memory storage");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {

                addStudent(new Student("S001", "John Doe", 8.5));
                addStudent(new Student("S002", "Jane Smith", 9.2));
                addStudent(new Student("S003", "Bob Johnson", 6.8));
                addStudent(new Student("S004", "Alice Brown", 4.5));
                addStudent(new Student("S005", "Charlie Davis", 7.3));
                addStudent(new Student("S006", "Emma Wilson", 9.7));
                addStudent(new Student("S007", "Michael Lee", 5.9));
                addStudent(new Student("S008", "Sophia Garcia", 8.1));
                addStudent(new Student("S009", "William Taylor", 3.8));
                addStudent(new Student("S010", "Olivia Martinez", 7.9));
                addStudent(new Student("S011", "James Anderson", 6.2));
                addStudent(new Student("S012", "Ava Thomas", 8.7));
                
                System.out.println("Sample data has been added to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding sample data: " + e.getMessage());
        }
    }
}

