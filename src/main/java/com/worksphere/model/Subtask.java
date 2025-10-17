package com.worksphere.model;

import java.time.LocalDateTime;

/**
 * Subtask entity representing a subtask/checklist item within a task
 */
public class Subtask {
    private int id;
    private int taskId;
    private String title;
    private boolean completed;
    private int orderIndex;          // For maintaining order
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // Default constructor
    public Subtask() {
    }
    
    // Constructor for creating new subtask
    public Subtask(int taskId, String title, int orderIndex) {
        this.taskId = taskId;
        this.title = title;
        this.completed = false;
        this.orderIndex = orderIndex;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public Subtask(int id, int taskId, String title, boolean completed, 
                   int orderIndex, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.completed = completed;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    public int getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    /**
     * Toggle completion status
     */
    public void toggleCompleted() {
        setCompleted(!completed);
    }
    
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", orderIndex=" + orderIndex +
                '}';
    }
}
