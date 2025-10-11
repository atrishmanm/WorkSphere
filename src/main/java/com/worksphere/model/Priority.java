package com.worksphere.model;

/**
 * Enum representing task priorities
 */
public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");
    
    private final String displayName;
    
    Priority(String displayName) {
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
     * Get Priority enum from string value
     * @param value String value
     * @return Priority enum or null if not found
     */
    public static Priority fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value) || 
                priority.displayName.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        return null;
    }
}
