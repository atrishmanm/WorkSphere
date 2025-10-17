package com.worksphere.model;

import java.time.LocalDateTime;

/**
 * TaskHistory entity for tracking all changes made to tasks
 */
public class TaskHistory {
    private int id;
    private int taskId;
    private int userId;
    private String username;
    private String action;           // CREATED, UPDATED, DELETED, STATUS_CHANGED, etc.
    private String fieldChanged;     // Field that was changed (e.g., "title", "priority", "status")
    private String oldValue;         // Previous value
    private String newValue;         // New value
    private LocalDateTime timestamp;
    
    // Default constructor
    public TaskHistory() {
    }
    
    // Constructor for creating new history entry
    public TaskHistory(int taskId, int userId, String action, String fieldChanged, 
                      String oldValue, String newValue) {
        this.taskId = taskId;
        this.userId = userId;
        this.action = action;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public TaskHistory(int id, int taskId, int userId, String action, 
                      String fieldChanged, String oldValue, String newValue, 
                      LocalDateTime timestamp) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.action = action;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
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
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getFieldChanged() {
        return fieldChanged;
    }
    
    public void setFieldChanged(String fieldChanged) {
        this.fieldChanged = fieldChanged;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get a formatted description of the change
     * @return human-readable description
     */
    public String getFormattedDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (username != null) {
            desc.append(username);
        } else {
            desc.append("User #").append(userId);
        }
        
        switch (action) {
            case "CREATED":
                desc.append(" created this task");
                break;
            case "UPDATED":
                if (fieldChanged != null && !fieldChanged.isEmpty()) {
                    desc.append(" changed ").append(fieldChanged);
                    if (oldValue != null && !oldValue.isEmpty()) {
                        desc.append(" from '").append(oldValue).append("'");
                    }
                    if (newValue != null && !newValue.isEmpty()) {
                        desc.append(" to '").append(newValue).append("'");
                    }
                } else {
                    desc.append(" updated this task");
                }
                break;
            case "STATUS_CHANGED":
                desc.append(" changed status from ").append(oldValue).append(" to ").append(newValue);
                break;
            case "COMPLETED":
                desc.append(" completed this task");
                break;
            case "DELETED":
                desc.append(" deleted this task");
                break;
            case "ASSIGNED":
                desc.append(" assigned this task to ").append(newValue);
                break;
            case "COMMENT":
                desc.append(" commented: ").append(newValue);
                break;
            default:
                desc.append(" performed ").append(action);
                if (fieldChanged != null) {
                    desc.append(" on ").append(fieldChanged);
                }
                break;
        }
        
        return desc.toString();
    }
    
    @Override
    public String toString() {
        return "TaskHistory{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", fieldChanged='" + fieldChanged + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
