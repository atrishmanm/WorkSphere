package com.worksphere.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility to display enhanced task data
 */
public class TaskDataViewer {
    
    public static void main(String[] args) {
        System.out.println("🔍 WorkSphere Enhanced Task Data Viewer");
        System.out.println("=====================================");
        
        try {
            displayTaskSummary();
            System.out.println();
            displaySampleTasks();
            System.out.println();
            displayCategoryBreakdown();
            System.out.println();
            displayTagStatistics();
        } catch (SQLException e) {
            System.err.println("❌ Error accessing database: " + e.getMessage());
        }
    }
    
    private static void displayTaskSummary() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) as total, " +
                 "AVG(estimated_minutes) as avg_estimated, " +
                 "AVG(actual_minutes) as avg_actual, " +
                 "COUNT(DISTINCT category_id) as categories " +
                 "FROM tasks")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("📊 TASK SUMMARY:");
                System.out.println("  Total Tasks: " + rs.getInt("total"));
                System.out.println("  Average Estimated Time: " + rs.getInt("avg_estimated") + " minutes");
                System.out.println("  Average Actual Time: " + rs.getInt("avg_actual") + " minutes");
                System.out.println("  Categories Used: " + rs.getInt("categories"));
            }
        }
    }
    
    private static void displaySampleTasks() throws SQLException {
        System.out.println("📋 SAMPLE ENHANCED TASKS:");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.id, t.title, t.status, t.priority, " +
                 "t.estimated_minutes, t.actual_minutes, " +
                 "c.name as category_name, " +
                 "GROUP_CONCAT(tt.tag_name, ', ') as tags " +
                 "FROM tasks t " +
                 "LEFT JOIN categories c ON t.category_id = c.id " +
                 "LEFT JOIN task_tags tt ON t.id = tt.task_id " +
                 "GROUP BY t.id " +
                 "ORDER BY t.id " +
                 "LIMIT 10")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("  📌 Task " + rs.getInt("id") + ": " + rs.getString("title"));
                System.out.println("    Status: " + rs.getString("status") + " | Priority: " + rs.getString("priority"));
                System.out.println("    Category: " + rs.getString("category_name"));
                System.out.println("    Time: " + rs.getInt("estimated_minutes") + "min estimated, " + 
                                 rs.getInt("actual_minutes") + "min actual");
                String tags = rs.getString("tags");
                System.out.println("    Tags: " + (tags != null ? tags : "none"));
                System.out.println();
            }
        }
    }
    
    private static void displayCategoryBreakdown() throws SQLException {
        System.out.println("📂 CATEGORY BREAKDOWN:");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.name, COUNT(t.id) as task_count, " +
                 "AVG(t.estimated_minutes) as avg_time " +
                 "FROM categories c " +
                 "LEFT JOIN tasks t ON c.id = t.category_id " +
                 "GROUP BY c.id, c.name " +
                 "ORDER BY task_count DESC")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("  " + rs.getString("name") + ": " + 
                                 rs.getInt("task_count") + " tasks (" + 
                                 rs.getInt("avg_time") + " min avg)");
            }
        }
    }
    
    private static void displayTagStatistics() throws SQLException {
        System.out.println("🏷️  TOP TAGS:");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT tag_name, COUNT(*) as usage_count " +
                 "FROM task_tags " +
                 "GROUP BY tag_name " +
                 "ORDER BY usage_count DESC " +
                 "LIMIT 10")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("  #" + rs.getString("tag_name") + ": " + 
                                 rs.getInt("usage_count") + " tasks");
            }
        }
    }
}