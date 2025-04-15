package dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.Student;

public class StudentDAO {
    private List<Student> students;
    
    public StudentDAO() {
        students = new ArrayList<>();
    }
    
    private boolean isValidStudentId(String id) {
        return id != null && id.matches("BC\\d{5}");
    }
    
    public boolean addStudent(Student student) {
        if (!isValidStudentId(student.getId())) {
            System.err.println("Error adding student: Invalid ID format BC00000");
            return false;
        }
        
        // Check if student already exists
        if (findStudentById(student.getId()) != null) {
            System.err.println("Error adding student: ID already exists");
            return false;
        }
        
        return students.add(student);
    }
    
    public boolean updateStudent(Student student) {
        if (!isValidStudentId(student.getId())) {
            return false;
        }
        
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(student.getId())) {
                students.set(i, student);
                return true;
            }
        }
        return false;
    }
    
    public boolean deleteStudent(String id) {
        return students.removeIf(s -> s.getId().equals(id));
    }
    
    public Student findStudentById(String id) {
        return students.stream()
                      .filter(s -> s.getId().equals(id))
                      .findFirst()
                      .orElse(null);
    }
    
    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }
    
    public List<Student> getStudentsSortedByScore() {
        return students.stream()
                      .sorted((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()))
                      .collect(Collectors.toList());
    }
    
    public List<Student> getStudentsSortedByScoreAscending() {
        return students.stream()
                      .sorted((s1, s2) -> Double.compare(s1.getScore(), s2.getScore()))
                      .collect(Collectors.toList());
    }
    
    public List<Student> getStudentsSortedByName(boolean ascending) {
        return students.stream()
                      .sorted((s1, s2) -> ascending ? 
                             s1.getName().compareTo(s2.getName()) :
                             s2.getName().compareTo(s1.getName()))
                      .collect(Collectors.toList());
    }
    
    public List<Student> getStudentsSortedById() {
        return students.stream()
                      .sorted((s1, s2) -> s1.getId().compareTo(s2.getId()))
                      .collect(Collectors.toList());
    }
    
    public double getAverageScore() {
        if (students.isEmpty()) {
            return 0.0;
        }
        return students.stream()
                      .mapToDouble(Student::getScore)
                      .average()
                      .orElse(0.0);
    }
    
    public double getHighestScore() {
        if (students.isEmpty()) {
            return 0.0;
        }
        return students.stream()
                      .mapToDouble(Student::getScore)
                      .max()
                      .orElse(0.0);
    }
    
    public double getLowestScore() {
        if (students.isEmpty()) {
            return 0.0;
        }
        return students.stream()
                      .mapToDouble(Student::getScore)
                      .min()
                      .orElse(0.0);
    }
    
    public int getPassCount() {
        return (int) students.stream()
                           .filter(s -> s.getScore() >= 5.0)
                           .count();
    }
    
    public void addSampleData() {
        addStudent(new Student("BC00001", "John Doe", 8.5));
        addStudent(new Student("BC00002", "Jane Smith", 9.2));
        addStudent(new Student("BC00003", "Bob Johnson", 6.8));
        addStudent(new Student("BC00004", "Alice Brown", 4.5));
        addStudent(new Student("BC00005", "Charlie Davis", 7.3));
        addStudent(new Student("BC00006", "Emma Wilson", 9.7));
        addStudent(new Student("BC00007", "Michael Lee", 5.9));
        addStudent(new Student("BC00008", "Sophia Garcia", 8.1));
        addStudent(new Student("BC00009", "William Taylor", 3.8));
        addStudent(new Student("BC00010", "Olivia Martinez", 7.9));
    }
}

