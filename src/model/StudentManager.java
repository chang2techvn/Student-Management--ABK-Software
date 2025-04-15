package model;

import java.util.*;
import java.util.stream.Collectors;

public class StudentManager {
    // Primary data structures
    private final List<Student> students;               // ArrayList to store all students in order
    private final Map<String, Student> studentMap;      // HashMap to find students by ID quickly
    
    // Search indices
    private final Map<String, Set<Student>> nameIndex;  // Index for searching by name parts
    private final Map<Double, Set<Student>> scoreIndex; // Index for searching by score
    private final Map<String, Set<Student>> rankIndex;  // Index for searching by rank

    // Notification mechanism
    private final List<Runnable> dataChangeListeners = new ArrayList<>();

    public StudentManager() {
        students = new ArrayList<>();
        studentMap = new HashMap<>();
        nameIndex = new HashMap<>();
        scoreIndex = new HashMap<>();
        rankIndex = new HashMap<>();
    }

    public void addDataChangeListener(Runnable listener) {
        dataChangeListeners.add(listener);
    }

    private void notifyDataChangeListeners() {
        for (Runnable listener : dataChangeListeners) {
            listener.run();
        }
    }

    public boolean addStudent(Student student) {
        if (!isValidStudentId(student.getId()) || studentMap.containsKey(student.getId())) {
            return false;
        }
        
        students.add(student);
        studentMap.put(student.getId(), student);
        updateSearchIndices(student);
        notifyDataChangeListeners();
        return true;
    }

    private void updateSearchIndices(Student student) {
        // Index by name parts
        String[] nameParts = student.getName().toLowerCase().split("\\s+");
        for (String part : nameParts) {
            nameIndex.computeIfAbsent(part, _ -> new HashSet<>()).add(student);
        }
        
        // Index by score
        scoreIndex.computeIfAbsent(student.getScore(), _ -> new HashSet<>()).add(student);
        
        // Index by rank
        rankIndex.computeIfAbsent(student.getRank().toLowerCase(), _ -> new HashSet<>()).add(student);
    }

    private void removeFromSearchIndices(Student student) {
        // Remove from name index
        String[] nameParts = student.getName().toLowerCase().split("\\s+");
        for (String part : nameParts) {
            Set<Student> studentSet = nameIndex.get(part);
            if (studentSet != null) {
                studentSet.remove(student);
                if (studentSet.isEmpty()) {
                    nameIndex.remove(part);
                }
            }
        }
        
        // Remove from score index
        Set<Student> scoreStudents = scoreIndex.get(student.getScore());
        if (scoreStudents != null) {
            scoreStudents.remove(student);
            if (scoreStudents.isEmpty()) {
                scoreIndex.remove(student.getScore());
            }
        }
        
        // Remove from rank index
        Set<Student> rankStudents = rankIndex.get(student.getRank().toLowerCase());
        if (rankStudents != null) {
            rankStudents.remove(student);
            if (rankStudents.isEmpty()) {
                rankIndex.remove(student.getRank().toLowerCase());
            }
        }
    }

    public boolean updateStudent(Student student) {
        Student existingStudent = studentMap.get(student.getId());
        if (existingStudent == null) {
            return false;
        }
        
        removeFromSearchIndices(existingStudent);
        
        existingStudent.setName(student.getName());
        existingStudent.setScore(student.getScore());
        
        updateSearchIndices(existingStudent);
        notifyDataChangeListeners();
        return true;
    }

    public boolean deleteStudent(String id) {
        Student student = studentMap.get(id);
        if (student == null) {
            return false;
        }
        
        removeFromSearchIndices(student);
        students.remove(student);
        studentMap.remove(id);
        notifyDataChangeListeners();
        return true;
    }

    // Quick Sort implementation for sorting students by score
    public List<Student> getSortedStudentsByScore() {
        List<Student> sortedList = new ArrayList<>(students);
        quickSort(sortedList, 0, sortedList.size() - 1);
        return sortedList;
    }

    private void quickSort(List<Student> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSort(list, low, pi - 1);
            quickSort(list, pi + 1, high);
        }
    }

    private int partition(List<Student> list, int low, int high) {
        Student pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (list.get(j).getScore() >= pivot.getScore()) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    public List<Student> getSortedStudentsByScoreAscending() {
        List<Student> sortedList = new ArrayList<>(students);
        quickSortAscending(sortedList, 0, sortedList.size() - 1, Comparator.comparingDouble(Student::getScore));
        return sortedList;
    }

    public List<Student> getSortedStudentsByName(boolean ascending) {
        List<Student> sortedList = new ArrayList<>(students);
        Comparator<Student> comparator = Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        quickSortAscending(sortedList, 0, sortedList.size() - 1, comparator);
        return sortedList;
    }

    public List<Student> getSortedStudentsById() {
        List<Student> sortedList = new ArrayList<>(students);
        quickSortAscending(sortedList, 0, sortedList.size() - 1, Comparator.comparing(Student::getId));
        return sortedList;
    }

    private void quickSortAscending(List<Student> list, int low, int high, Comparator<Student> comparator) {
        if (low < high) {
            int pi = partitionAscending(list, low, high, comparator);
            quickSortAscending(list, low, pi - 1, comparator);
            quickSortAscending(list, pi + 1, high, comparator);
        }
    }

    private int partitionAscending(List<Student> list, int low, int high, Comparator<Student> comparator) {
        Student pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    // Efficient search methods using indices
    public Student findStudentById(String id) {
        return studentMap.get(id);  // O(1) lookup
    }

    public Set<Student> findStudentsByName(String name) {
        String searchTerm = name.toLowerCase();
        Set<Student> result = new HashSet<>();
        for (String part : searchTerm.split("\\s+")) {
            Set<Student> found = nameIndex.get(part);
            if (found != null) {
                if (result.isEmpty()) {
                    result.addAll(found);
                } else {
                    result.retainAll(found); // Intersection for multi-word search
                }
            }
        }
        return result;
    }

    public Set<Student> findStudentsByScore(double score) {
        Set<Student> result = scoreIndex.get(score);
        return result != null ? new HashSet<>(result) : new HashSet<>();
    }

    public Set<Student> findStudentsByRank(String rank) {
        Set<Student> result = rankIndex.get(rank.toLowerCase());
        return result != null ? new HashSet<>(result) : new HashSet<>();
    }

    // Utility methods
    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public boolean isValidStudentId(String id) {
        return id != null && id.matches("BC\\d{5}");
    }

    public double getAverageScore() {
        if (students.isEmpty()) return 0.0;
        return students.stream()
                      .mapToDouble(Student::getScore)
                      .average()
                      .orElse(0.0);
    }

    public double getHighestScore() {
        if (students.isEmpty()) return 0.0;
        return students.stream()
                      .mapToDouble(Student::getScore)
                      .max()
                      .orElse(0.0);
    }

    public double getLowestScore() {
        if (students.isEmpty()) return 0.0;
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

    public Map<String, Integer> getRankDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("Excellent", 0);
        distribution.put("Very Good", 0);
        distribution.put("Good", 0);
        distribution.put("Medium", 0);
        distribution.put("Fail", 0);
        
        for (Student student : students) {
            String rank = student.getRank();
            distribution.put(rank, distribution.get(rank) + 1);
        }
        return distribution;
    }

    public void addSampleData() {
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
        }
    }

    public List<Student> getTopStudents(int n) {
        List<Student> sortedList = new ArrayList<>(students);
        sortedList.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore())); // Sort descending
        return sortedList.stream()
                        .limit(n)
                        .collect(Collectors.toList());
    }
}
