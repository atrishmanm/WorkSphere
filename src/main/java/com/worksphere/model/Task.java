package com.worksphere.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Task entity representing a task in the system with enhanced features
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private Integer assignedTo;  // User ID
    private int createdBy;       // User ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enhanced fields for analytics and features
    private LocalDateTime completedAt;
    private int estimatedMinutes;     // Estimated time in minutes
    private int actualMinutes;        // Actual time spent in minutes
    private int categoryId;           // Category ID
    private List<String> tags;        // List of tags
    private String recurrenceRule;    // Recurrence pattern (null for non-recurring)
    private Integer parentTaskId;     // For recurring tasks - reference to original
    private LocalDateTime lastWorkedAt; // Last time user worked on this task
    
    // For display purposes
    private String assignedToUsername;
    private String createdByUsername;
    private Category category;        // Full category object for display
    
    // Default constructor
    public Task() {
        this.tags = new ArrayList<>();
    }
    
    // Constructor for creating new task
    public Task(String title, String description, Priority priority, 
                TaskStatus status, LocalDate dueDate, Integer assignedTo, int createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : TaskStatus.TODO;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.estimatedMinutes = 0;
        this.actualMinutes = 0;
    }
    
    // Constructor with all fields
    public Task(int id, String title, String description, Priority priority, 
                TaskStatus status, LocalDate dueDate, Integer assignedTo, int createdBy,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.estimatedMinutes = 0;
        this.actualMinutes = 0;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getAssignedToUsername() {
        return assignedToUsername;
    }
    
    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
    
    public String getCreatedByUsername() {
        return createdByUsername;
    }
    
    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }
    
    // New getters and setters for enhanced fields
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
    
    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }
    
    public int getActualMinutes() {
        return actualMinutes;
    }
    
    public void setActualMinutes(int actualMinutes) {
        this.actualMinutes = actualMinutes;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !this.tags.contains(tag.trim())) {
            this.tags.add(tag.trim());
        }
    }
    
    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
    
    public String getRecurrenceRule() {
        return recurrenceRule;
    }
    
    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }
    
    public Integer getParentTaskId() {
        return parentTaskId;
    }
    
    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
    
    public LocalDateTime getLastWorkedAt() {
        return lastWorkedAt;
    }
    
    public void setLastWorkedAt(LocalDateTime lastWorkedAt) {
        this.lastWorkedAt = lastWorkedAt;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }
    
    /**
     * Check if task is overdue
     * @return true if task is overdue, false otherwise
     */
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && status != TaskStatus.COMPLETED;
    }
    
    /**
     * Get formatted due date status
     * @return formatted string indicating due date status
     */
    public String getDueDateStatus() {
        if (dueDate == null) {
            return "No due date";
        }
        
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today) && status != TaskStatus.COMPLETED) {
            return "OVERDUE";
        } else if (dueDate.equals(today)) {
            return "DUE TODAY";
        } else {
            return "Due: " + dueDate;
        }
    }
    
    /**
     * Get estimated time in human readable format
     * @return formatted time string
     */
    public String getEstimatedTimeFormatted() {
        if (estimatedMinutes <= 0) {
            return "No estimate";
        }
        
        int hours = estimatedMinutes / 60;
        int minutes = estimatedMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    /**
     * Get actual time in human readable format
     * @return formatted time string
     */
    public String getActualTimeFormatted() {
        if (actualMinutes <= 0) {
            return "No time logged";
        }
        
        int hours = actualMinutes / 60;
        int minutes = actualMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    /**
     * Check if this is a recurring task
     * @return true if task has recurrence rule
     */
    public boolean isRecurring() {
        return recurrenceRule != null && !recurrenceRule.trim().isEmpty();
    }
    
    /**
     * Check if this task was generated from a recurring task
     * @return true if this task has a parent task
     */
    public boolean isRecurringInstance() {
        return parentTaskId != null;
    }
    
    /**
     * Get tags as comma-separated string
     * @return formatted tags string
     */
    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(", ", tags);
    }
    
    /**
     * Check if task has a specific tag
     * @param tag tag to check
     * @return true if task has the tag
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    /**
     * Calculate time efficiency (actual vs estimated)
     * @return efficiency ratio, or -1 if no estimate
     */
    public double getTimeEfficiency() {
        if (estimatedMinutes <= 0 || actualMinutes <= 0) {
            return -1.0;
        }
        return (double) estimatedMinutes / actualMinutes;
    }
    
    /**
     * Add time to actual minutes worked
     * @param minutes minutes to add
     */
    public void addTimeWorked(int minutes) {
        this.actualMinutes += minutes;
        this.lastWorkedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark task as completed and set completion time
     */
    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", assignedTo=" + assignedTo +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
