package com.worksphere.util;

import java.sql.SQLException;

/**
 * Database utility class for administrative operations
 */
public class DatabaseUtility {
    
    /**
     * Reset task auto-increment counter to start from 1
     * This is useful if you want to clean up task numbering
     */
    public static void resetTaskNumbers() {
        try {
            DatabaseConnection.resetTaskAutoIncrement();
            System.out.println("✅ Task numbering has been reset. New tasks will start from ID 1.");
        } catch (SQLException e) {
            System.err.println("❌ Failed to reset task numbers: " + e.getMessage());
        }
    }
    
    /**
     * Show current task auto-increment information
     */
    public static void showTaskAutoIncrementInfo() {
        try {
            int nextId = DatabaseConnection.getCurrentTaskAutoIncrement();
            System.out.println("ℹ️  Next task will be assigned ID: " + nextId);
        } catch (SQLException e) {
            System.err.println("❌ Failed to get task auto-increment info: " + e.getMessage());
        }
    }
    
    /**
     * Test database connection
     */
    public static void testConnection() {
        boolean isConnected = DatabaseConnection.testConnection();
        if (isConnected) {
            System.out.println("✅ Database connection is working properly");
        } else {
            System.out.println("❌ Database connection failed");
        }
    }
    
    /**
     * Main method for running database utilities from command line
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("WorkSphere Database Utility");
            System.out.println("===========================");
            System.out.println("Available commands:");
            System.out.println("  reset-task-numbers  - Reset task auto-increment to start from 1");
            System.out.println("  show-task-info      - Show next task ID that will be assigned");
            System.out.println("  test-connection     - Test database connection");
            System.out.println();
            System.out.println("Usage: java com.worksphere.util.DatabaseUtility <command>");
            return;
        }
        
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "reset-task-numbers":
                System.out.println("Resetting task auto-increment counter...");
                resetTaskNumbers();
                break;
                
            case "show-task-info":
                showTaskAutoIncrementInfo();
                break;
                
            case "test-connection":
                System.out.println("Testing database connection...");
                testConnection();
                break;
                
            default:
                System.out.println("Unknown command: " + command);
                System.out.println("Use 'reset-task-numbers', 'show-task-info', or 'test-connection'");
                break;
        }
    }
}