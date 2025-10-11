package com.worksphere.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Quick demo verification utility
 */
public class DemoVerification {
    
    public static void main(String[] args) {
        System.out.println("🎯 WorkSphere Demo Verification");
        System.out.println("===============================");
        
        try {
            verifyDemoData();
        } catch (SQLException e) {
            System.err.println("❌ Error accessing database: " + e.getMessage());
        }
    }
    
    private static void verifyDemoData() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Check total task count
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tasks")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int taskCount = rs.getInt(1);
                    System.out.println("📊 Total Tasks: " + taskCount);
                }
            }
            
            // Check tasks with categories
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM tasks WHERE category_id IS NOT NULL")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int categorizedTasks = rs.getInt(1);
                    System.out.println("📂 Tasks with Categories: " + categorizedTasks);
                }
            }
            
            // Check tasks with tags
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT task_id) FROM task_tags")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int taggedTasks = rs.getInt(1);
                    System.out.println("🏷️  Tasks with Tags: " + taggedTasks);
                }
            }
            
            // Show sample tasks
            System.out.println("\n📋 Sample Tasks for Demo:");
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT t.id, t.title, t.status, t.priority, c.name as category " +
                "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                "ORDER BY t.id LIMIT 10")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("  " + rs.getInt("id") + ". " + rs.getString("title") + 
                                     " [" + rs.getString("status") + "/" + rs.getString("priority") + 
                                     "/" + rs.getString("category") + "]");
                }
            }
            
            System.out.println("\n✅ Demo data is ready!");
        }
    }
}