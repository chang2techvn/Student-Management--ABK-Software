package util;

import java.util.ArrayList;
import java.util.List;
import model.Student;

/**
 * A priority queue implementation using a max-heap data structure.
 * This implementation prioritizes students based on their scores (higher scores have higher priority).
 */
public class MaxHeapPriorityQueue {
    private List<Student> heap;
    
    /**
     * Constructs an empty priority queue.
     */
    public MaxHeapPriorityQueue() {
        heap = new ArrayList<>();
    }
    
    /**
     * Constructs a priority queue with the given students.
     * @param students The initial list of students
     */
    public MaxHeapPriorityQueue(List<Student> students) {
        heap = new ArrayList<>(students);
        buildHeap();
    }
    
    /**
     * Builds a max heap from an unordered list of students.
     */
    private void buildHeap() {
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }
    
    /**
     * Inserts a student into the priority queue.
     * @param student The student to insert
     */
    public void insert(Student student) {
        heap.add(student);
        heapifyUp(heap.size() - 1);
    }
    
    /**
     * Removes and returns the student with the highest priority (highest score).
     * @return The student with the highest score, or null if the queue is empty
     */
    public Student extractMax() {
        if (isEmpty()) {
            return null;
        }
        
        Student max = heap.get(0);
        Student last = heap.remove(heap.size() - 1);
        
        if (!isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        
        return max;
    }
    
    /**
     * Returns the student with the highest priority without removing it.
     * @return The student with the highest score, or null if the queue is empty
     */
    public Student peek() {
        return isEmpty() ? null : heap.get(0);
    }
    
    /**
     * Restores the heap property by moving a node up the tree.
     * @param index The index of the node to move up
     */
    private void heapifyUp(int index) {
        int current = index;
        int parent = getParentIndex(current);
        
        while (current > 0 && heap.get(current).getScore() > heap.get(parent).getScore()) {
            swap(current, parent);
            current = parent;
            parent = getParentIndex(current);
        }
    }
    
    /**
     * Restores the heap property by moving a node down the tree.
     * @param index The index of the node to move down
     */
    private void heapifyDown(int index) {
        int largest = index;
        int leftChild = getLeftChildIndex(index);
        int rightChild = getRightChildIndex(index);
        
        if (leftChild < heap.size() && heap.get(leftChild).getScore() > heap.get(largest).getScore()) {
            largest = leftChild;
        }
        
        if (rightChild < heap.size() && heap.get(rightChild).getScore() > heap.get(largest).getScore()) {
            largest = rightChild;
        }
        
        if (largest != index) {
            swap(index, largest);
            heapifyDown(largest);
        }
    }
    
    /**
     * Swaps two elements in the heap.
     * @param i The index of the first element
     * @param j The index of the second element
     */
    private void swap(int i, int j) {
        Student temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    /**
     * Returns the index of the parent node.
     * @param index The index of the child node
     * @return The index of the parent node
     */
    private int getParentIndex(int index) {
        return (index - 1) / 2;
    }
    
    /**
     * Returns the index of the left child node.
     * @param index The index of the parent node
     * @return The index of the left child node
     */
    private int getLeftChildIndex(int index) {
        return 2 * index + 1;
    }
    
    /**
     * Returns the index of the right child node.
     * @param index The index of the parent node
     * @return The index of the right child node
     */
    private int getRightChildIndex(int index) {
        return 2 * index + 2;
    }
    
    /**
     * Returns the number of students in the priority queue.
     * @return The number of students
     */
    public int size() {
        return heap.size();
    }
    
    /**
     * Checks if the priority queue is empty.
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    /**
     * Returns the top N students with the highest scores.
     * This method does not modify the original heap.
     * @param n The number of top students to return
     * @return A list of the top N students
     */
    public List<Student> getTopN(int n) {
        if (isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        
        // Create a copy of the heap to avoid modifying the original
        MaxHeapPriorityQueue copy = new MaxHeapPriorityQueue(new ArrayList<>(heap));
        List<Student> result = new ArrayList<>();
        
        for (int i = 0; i < n && !copy.isEmpty(); i++) {
            result.add(copy.extractMax());
        }
        
        return result;
    }
    
    /**
     * Returns all students in the priority queue as a list, sorted by score in descending order.
     * This method does not modify the original heap.
     * @return A list of all students sorted by score
     */
    public List<Student> getAllSorted() {
        return getTopN(heap.size());
    }
}
