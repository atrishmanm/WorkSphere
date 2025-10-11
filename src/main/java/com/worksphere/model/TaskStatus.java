package com.worksphere.model;

/**
 * Enum representing task status
 */
public enum TaskStatus {
    TODO("To-Do"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Get TaskStatus enum from string value
     * @param value String value
     * @return TaskStatus enum or null if not found
     */
    public static TaskStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (TaskStatus status : TaskStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || 
                status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
