package model;

import dao.StudentDAO;
import util.DatabaseConnection;
import util.MaxHeapPriorityQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Fix the missing Set import
import java.util.HashSet;
import java.util.Set;

public class StudentManager {
    private StudentDAO studentDAO;
    
 
    private List<Student> students;
    private Map<String, Student> studentMap;
    
    // Maps for searching
    private Map<String, List<Student>> nameIndex;
    private Map<String, List<Student>> scoreIndex;
    private Map<String, List<Student>> rankIndex;
    
    // Add a MaxHeapPriorityQueue for efficient access to top students
    private MaxHeapPriorityQueue studentHeap;
    
    private boolean useInMemoryStorage;

    public StudentManager() {
        studentDAO = new StudentDAO();
        
        // Khởi tạo lưu trữ trong bộ nhớ
        students = new ArrayList<>();
        studentMap = new HashMap<>();
        
        // Khởi tạo các index cho tìm kiếm
        nameIndex = new HashMap<>();
        scoreIndex = new HashMap<>();
        rankIndex = new HashMap<>();
        
        // Initialize the max heap
        studentHeap = new MaxHeapPriorityQueue();
        
        // Kiểm tra xem có sử dụng lưu tr trong bộ nhớ không
        useInMemoryStorage = DatabaseConnection.isUsingInMemoryStorage();
    }

    // Cập nhật phương thức addStudent để kiểm tra định dạng ID
    public boolean addStudent(Student student) {
        // Kiểm tra định dạng ID
        if (!isValidStudentId(student.getId())) {
            return false;
        }
        
        boolean result;
        if (useInMemoryStorage) {
            result = addStudentInMemory(student);
        } else {
            result = studentDAO.addStudent(student);
        }
        
        // If successful, make sure to refresh any cached data
        if (result) {
            // Refresh the in-memory list if we're using database storage
            if (!useInMemoryStorage) {
                students = studentDAO.getAllStudents();
                // Rebuild the student map
                studentMap.clear();
                for (Student s : students) {
                    studentMap.put(s.getId(), s);
                }
                // Rebuild search indices
                rebuildSearchIndices();
                
                // Rebuild the max heap
                studentHeap = new MaxHeapPriorityQueue(students);
            }
        }
        
        return result;
    }
    
    /**
     * Kiểm tra định dạng ID của sinh viên
     * @param id ID cần kiểm tra
     * @return true nếu ID đúng định dạng, false nếu không đúng
     */
    public boolean isValidStudentId(String id) {
        // Kiểm tra định dạng BC00000 (BC + 5 chữ số)
        return id != null && id.matches("BC\\d{5}");
    }
    
    private boolean addStudentInMemory(Student student) {
        if (studentMap.containsKey(student.getId())) {
            return false;
        }
        students.add(student);
        studentMap.put(student.getId(), student);
        
        // Cập nhật các index
        updateSearchIndices(student);
        
        // Add to max heap
        studentHeap.insert(student);
        
        return true;
    }
    
    private void updateSearchIndices(Student student) {
        // Cập nhật index theo tên
        String name = student.getName().toLowerCase();
        String[] nameParts = name.split("\\s+");
        for (String part : nameParts) {
            nameIndex.computeIfAbsent(part, k -> new ArrayList<>()).add(student);
        }
        
        // Cập nhật index theo điểm
        String score = String.valueOf(student.getScore());
        scoreIndex.computeIfAbsent(score, k -> new ArrayList<>()).add(student);
        
        // Cập nhật index theo xếp loại
        String rank = student.getRank().toLowerCase();
        rankIndex.computeIfAbsent(rank, k -> new ArrayList<>()).add(student);
    }
    
    private void removeFromSearchIndices(Student student) {
        // Xóa khỏi index theo tên
        String name = student.getName().toLowerCase();
        String[] nameParts = name.split("\\s+");
        for (String part : nameParts) {
            if (nameIndex.containsKey(part)) {
                nameIndex.get(part).remove(student);
            }
        }
        
        // Xóa khỏi index theo điểm
        String score = String.valueOf(student.getScore());
        if (scoreIndex.containsKey(score)) {
            scoreIndex.get(score).remove(student);
        }
        
        // Xóa khỏi index theo xếp loại
        String rank = student.getRank().toLowerCase();
        if (rankIndex.containsKey(rank)) {
            rankIndex.get(rank).remove(student);
        }
    }

    public boolean updateStudent(Student student) {
        boolean result;
        if (useInMemoryStorage) {
            result = updateStudentInMemory(student);
        } else {
            result = studentDAO.updateStudent(student);
        }
        
        // If successful, refresh cached data
        if (result && !useInMemoryStorage) {
            students = studentDAO.getAllStudents();
            // Rebuild the student map
            studentMap.clear();
            for (Student s : students) {
                studentMap.put(s.getId(), s);
            }
            // Rebuild search indices
            rebuildSearchIndices();
            
            // Rebuild the max heap
            studentHeap = new MaxHeapPriorityQueue(students);
        }
        
        return result;
    }
    
    private boolean updateStudentInMemory(Student student) {
        if (!studentMap.containsKey(student.getId())) {
            return false;
        }
        
        // Xóa sinh viên cũ khỏi các index
        Student oldStudent = studentMap.get(student.getId());
        removeFromSearchIndices(oldStudent);
        
        // Cập nhật thông tin sinh viên
        oldStudent.setName(student.getName());
        oldStudent.setScore(student.getScore());
        
        // Cập nhật lại các index
        updateSearchIndices(oldStudent);
        
        // Rebuild the max heap after update
        studentHeap = new MaxHeapPriorityQueue(students);
        
        return true;
    }

    public boolean deleteStudent(String id) {
        boolean result;
        if (useInMemoryStorage) {
            result = deleteStudentInMemory(id);
        } else {
            result = studentDAO.deleteStudent(id);
        }
        
        // If successful, refresh cached data
        if (result && !useInMemoryStorage) {
            students = studentDAO.getAllStudents();
            // Rebuild the student map
            studentMap.clear();
            for (Student s : students) {
                studentMap.put(s.getId(), s);
            }
            // Rebuild search indices
            rebuildSearchIndices();
            
            // Rebuild the max heap
            studentHeap = new MaxHeapPriorityQueue(students);
        }
        
        return result;
    }
    
    private boolean deleteStudentInMemory(String id) {
        if (!studentMap.containsKey(id)) {
            return false;
        }
        
        // Xóa sinh viên khỏi các index
        Student student = studentMap.get(id);
        removeFromSearchIndices(student);
        
        // Xóa sinh viên khỏi danh sách và map
        students.remove(student);
        studentMap.remove(id);
        
        // Rebuild the max heap after deletion
        studentHeap = new MaxHeapPriorityQueue(students);
        
        return true;
    }

    public Student findStudentById(String id) {
        if (useInMemoryStorage) {
            return studentMap.get(id);
        } else {
            return studentDAO.findStudentById(id);
        }
    }

    public List<Student> getAllStudents() {
        if (useInMemoryStorage) {
            return new ArrayList<>(students);
        } else {
            return studentDAO.getAllStudents();
        }
    }

    // Sắp xếp sinh viên theo điểm (giảm dần) - Now using MaxHeapPriorityQueue
    public List<Student> getSortedStudentsByScore() {
        if (useInMemoryStorage) {
            // Use the max heap to get all students sorted by score
            return studentHeap.getAllSorted();
        } else {
            List<Student> sortedStudents = studentDAO.getStudentsSortedByScore();
            // Create a heap with the sorted students for future operations
            studentHeap = new MaxHeapPriorityQueue(sortedStudents);
            return sortedStudents;
        }
    }
    
    // Get top N students efficiently using the max heap
    public List<Student> getTopStudents(int n) {
        if (useInMemoryStorage) {
            return studentHeap.getTopN(n);
        } else {
            List<Student> allSorted = studentDAO.getStudentsSortedByScore();
            studentHeap = new MaxHeapPriorityQueue(allSorted);
            return studentHeap.getTopN(n);
        }
    }
    
    // Sắp xếp sinh viên theo điểm (tăng dần)
    public List<Student> getSortedStudentsByScoreAscending() {
        if (useInMemoryStorage) {
            List<Student> sortedList = new ArrayList<>(students);
            quickSort(sortedList, 0, sortedList.size() - 1, false);
            return sortedList;
        } else {
            return studentDAO.getStudentsSortedByScoreAscending();
        }
    }
    
    // Sắp xếp sinh viên theo tên
    public List<Student> getSortedStudentsByName(boolean ascending) {
        if (useInMemoryStorage) {
            List<Student> sortedList = new ArrayList<>(students);
            Collections.sort(sortedList, new Comparator<Student>() {
                @Override
                public int compare(Student s1, Student s2) {
                    return ascending ? 
                        s1.getName().compareToIgnoreCase(s2.getName()) : 
                        s2.getName().compareToIgnoreCase(s1.getName());
                }
            });
            return sortedList;
        } else {
            return studentDAO.getStudentsSortedByName(ascending);
        }
    }
    
    // Sắp xếp sinh viên theo ID
    public List<Student> getSortedStudentsById() {
        if (useInMemoryStorage) {
            List<Student> sortedList = new ArrayList<>(students);
            Collections.sort(sortedList, new Comparator<Student>() {
                @Override
                public int compare(Student s1, Student s2) {
                    return s1.getId().compareToIgnoreCase(s2.getId());
                }
            });
            return sortedList;
        } else {
            return studentDAO.getStudentsSortedById();
        }
    }
    
    // Enhanced QuickSort implementation with descending/ascending option
    private void quickSort(List<Student> list, int low, int high, boolean descending) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, descending);
            quickSort(list, low, pivotIndex - 1, descending);
            quickSort(list, pivotIndex + 1, high, descending);
        }
    }

    private int partition(List<Student> list, int low, int high, boolean descending) {
        double pivot = list.get(high).getScore();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (descending ? list.get(j).getScore() >= pivot : list.get(j).getScore() <= pivot) {
                i++;
                swap(list, i, j);
            }
        }
        
        swap(list, i + 1, high);
        return i + 1;
    }

    private void swap(List<Student> list, int i, int j) {
        Student temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    /**
     * Tìm kiếm sinh viên theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sinh viên phù hợp với từ khóa
     */
    public List<Student> searchStudentsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        
        if (useInMemoryStorage) {
            return searchStudentsByKeywordInMemory(keyword);
        } else {
            // Nếu sử dụng cơ sở dữ liệu, thực hiện tìm kiếm trên tất cả sinh viên
            List<Student> allStudents = studentDAO.getAllStudents();
            return filterStudentsByKeyword(allStudents, keyword);
        }
    }
    
    private List<Student> searchStudentsByKeywordInMemory(String keyword) {
        keyword = keyword.toLowerCase().trim();
        
        // Tạo một Set để lưu kết quả tìm kiếm (tránh trùng lặp)
        Set<Student> resultSet = new HashSet<>();
        
        // Tìm kiếm theo ID
        if (studentMap.containsKey(keyword)) {
            resultSet.add(studentMap.get(keyword));
        } else {
            // Tìm kiếm ID chứa từ khóa
            for (Student student : students) {
                if (student.getId().toLowerCase().contains(keyword)) {
                    resultSet.add(student);
                }
            }
        }
        
        // Tìm kiếm theo tên
        for (Map.Entry<String, List<Student>> entry : nameIndex.entrySet()) {
            if (entry.getKey().contains(keyword)) {
                resultSet.addAll(entry.getValue());
            }
        }
        
        // Tìm kiếm theo điểm (nếu từ khóa là số)
        try {
            double score = Double.parseDouble(keyword);
            String scoreStr = String.valueOf(score);
            if (scoreIndex.containsKey(scoreStr)) {
                resultSet.addAll(scoreIndex.get(scoreStr));
            }
        } catch (NumberFormatException e) {
            // Không phải số, bỏ qua
        }
        
        // Tìm kiếm theo xếp loại
        for (Map.Entry<String, List<Student>> entry : rankIndex.entrySet()) {
            if (entry.getKey().contains(keyword)) {
                resultSet.addAll(entry.getValue());
            }
        }
        
        // Chuyển Set thành List và trả về
        return new ArrayList<>(resultSet);
    }
    
    // Fix the filterStudentsByKeyword method to make keyword effectively final
    private List<Student> filterStudentsByKeyword(List<Student> students, String searchKeyword) {
        final String keyword = searchKeyword.toLowerCase().trim();
        
        return students.stream()
            .filter(student -> 
                student.getId().toLowerCase().contains(keyword) ||
                student.getName().toLowerCase().contains(keyword) ||
                String.valueOf(student.getScore()).contains(keyword) ||
                student.getRank().toLowerCase().contains(keyword))
            .collect(Collectors.toList());
    }
    
    // Lấy điểm trung bình
    public double getAverageScore() {
        if (useInMemoryStorage) {
            if (students.isEmpty()) {
                return 0;
            }
            
            double sum = 0;
            for (Student student : students) {
                sum += student.getScore();
            }
            
            return sum / students.size();
        } else {
            return studentDAO.getAverageScore();
        }
    }
    
    // Lấy điểm cao nhất - Now using MaxHeapPriorityQueue
    public double getHighestScore() {
        if (useInMemoryStorage) {
            if (students.isEmpty()) {
                return 0;
            }
            
            // Use the max heap to get the highest score efficiently
            Student topStudent = studentHeap.peek();
            return topStudent != null ? topStudent.getScore() : 0;
        } else {
            return studentDAO.getHighestScore();
        }
    }
    
    // Lấy điểm thấp nhất
    public double getLowestScore() {
        if (useInMemoryStorage) {
            if (students.isEmpty()) {
                return 0;
            }
            
            double lowest = 10;
            for (Student student : students) {
                if (student.getScore() < lowest) {
                    lowest = student.getScore();
                }
            }
            
            return lowest;
        } else {
            return studentDAO.getLowestScore();
        }
    }
    
    // Đếm số sinh viên đạt
    public int getPassCount() {
        if (useInMemoryStorage) {
            int count = 0;
            for (Student student : students) {
                if (student.getScore() >= 5.0) {
                    count++;
                }
            }
            
            return count;
        } else {
            return studentDAO.getPassCount();
        }
    }
    
    // Lấy phân phối xếp loại
    public Map<String, Integer> getRankDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("Excellent", 0);
        distribution.put("Very Good", 0);
        distribution.put("Good", 0);
        distribution.put("Medium", 0);
        distribution.put("Fail", 0);
        
        List<Student> allStudents = getAllStudents();
        for (Student student : allStudents) {
            String rank = student.getRank();
            distribution.put(rank, distribution.get(rank) + 1);
        }
        
        return distribution;
    }
    
    // Thêm dữ liệu mẫu
    public void addSampleData() {
        if (useInMemoryStorage) {
            // Kiểm tra xem đã có dữ liệu chưa
            if (students.isEmpty()) {
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
                addStudent(new Student("BC00011", "James Anderson", 6.2));
                addStudent(new Student("BC00012", "Ava Thomas", 8.7));
            }
        } else {
            studentDAO.addSampleData();
            
            // Initialize the heap with the sample data
            students = studentDAO.getAllStudents();
            studentHeap = new MaxHeapPriorityQueue(students);
        }
    }
    
    // Rebuild search indices for all students
    public void rebuildSearchIndices() {
        if (useInMemoryStorage) {
            // Clear existing indices
            nameIndex.clear();
            scoreIndex.clear();
            rankIndex.clear();
            
            // Rebuild indices for all students
            for (Student student : students) {
                updateSearchIndices(student);
            }
            
            // Rebuild the max heap
            studentHeap = new MaxHeapPriorityQueue(students);
        }
    }

    // Add the missing findStudentsByScore method
    public List<Student> findStudentsByScore(double score) {
        if (useInMemoryStorage) {
            List<Student> result = new ArrayList<>();
            String scoreStr = String.valueOf(score);
            
            // Use the score index if available
            if (scoreIndex.containsKey(scoreStr)) {
                return new ArrayList<>(scoreIndex.get(scoreStr));
            }
            
            // Otherwise, search manually
            for (Student student : students) {
                if (Math.abs(student.getScore() - score) < 0.001) {
                    result.add(student);
                }
            }
            return result;
        } else {
            // For database storage, filter all students
            List<Student> allStudents = studentDAO.getAllStudents();
            List<Student> result = new ArrayList<>();
            
            for (Student student : allStudents) {
                if (Math.abs(student.getScore() - score) < 0.001) {
                    result.add(student);
                }
            }
            
            return result;
        }
    }
}
